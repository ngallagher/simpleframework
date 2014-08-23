/*
 * FlushScheduler.java February 2008
 *
 * Copyright (C) 2008, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.simpleframework.transport;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static org.simpleframework.transport.TransportEvent.WRITE_BLOCKING;
import static org.simpleframework.transport.TransportEvent.WRITE_WAIT;

import java.io.IOException;

import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>FlushScheduler</code> object is used to schedule a task 
 * for execution when it is write ready. This is used by the socket
 * flusher to ensure that the writing thread can be blocked until
 * such time as all the bytes required to be written are written.
 * <p>
 * All methods are invoked by a <code>SocketFlusher</code> object
 * which is synchronized. This ensures that the methods of the 
 * scheduler are thread safe in that only one thread will access
 * them at any given time. The lock used by the socket flusher can
 * thus be safely as it will be synchronized on by the flusher.
 * 
 * @author Niall Gallagher
 *
 * @see org.simpleframework.transport.SocketFlusher
 */
class FlushScheduler {
   
   /**
    * This is the operation that is scheduled for execution.
    */
   private Operation task;
   
   /**
    * This is the reactor to used to execute the operation.
    */
   private Reactor reactor;
   
   /**
    * This is the trace that listens to all transport events.
    */
   private Trace trace;
   
   /**
    * This is the lock that is used to signal a blocked thread.
    */
   private Object lock;
   
   /**
    * This is used to determine if the scheduler is running.
    */
   private volatile boolean running;
   
   /**
    * This is used to determine if the scheduler is interrupted.
    */
   private volatile boolean closed;
   
   /**
    * This is used to determine if there is currently a flush.
    */
   private volatile boolean flushing;
   
   /**
    * Constructor for the <code>FlushScheduler</code> object. This 
    * is* used to create a scheduler that will execute the provided
    * task when the associated socket is write ready. 
    * 
    * @param socket this is the associated socket for the scheduler
    * @param reactor this is the rector used to schedule execution
    * @param task this is the task that is executed when writable
    * @param lock this is the lock used to signal blocking threads
    */
   public FlushScheduler(Socket socket, Reactor reactor, Operation task, Object lock) {
      this.trace = socket.getTrace();
      this.reactor = reactor;
      this.task = task;
      this.lock = lock;
   }
   
   /**
    * This is used to repeat schedule the operation for execution.
    * This is executed if the operation has not fully completed
    * its task. If the scheduler is not in a running state then
    * this will not schedule the task for a repeat execution.
    */
   public void repeat() throws IOException {
      if(closed) {
         throw new TransportException("Socket closed");
      }
      if(running) {
         trace.trace(WRITE_WAIT);
         reactor.process(task, OP_WRITE);
      }
   }

   /**
    * This is used to schedule the task for execution. If this is
    * given a boolean true to indicate that it wishes to block
    * then this will block the calling thread until such time as
    * the <code>ready</code> method is invoked.
    * 
    * @param block indicates whether the thread should block
    */
   public void schedule(boolean block) throws IOException {
      if(closed) {
         throw new TransportException("Socket closed");
      }
      if(!running) {
         trace.trace(WRITE_WAIT);
         reactor.process(task, OP_WRITE);
         running = true;
      }
      if(block) {
         listen();
      }
   }
   
   /**
    * This is used to listen for a notification from the reactor to
    * tell the thread that the write operation has completed. If
    * the thread is interrupted upon this call then this will throw
    * an <code>IOException</code> with the root cause.
    */
   private void listen() throws IOException {
      if(flushing) {
         throw new TransportException("Socket already flushing");
      }
      try {
         if(!closed) {
            try {
               flushing = true;
               trace.trace(WRITE_BLOCKING);
               lock.wait(120000);
            } finally {
               flushing = false;
            }
         }
      } catch(Exception e) {
         throw new TransportException("Could not schedule for flush", e);
      }
      if(closed) {
         throw new TransportException("Socket closed");
      }
   }
      
   /**
    * This is used to notify any waiting threads that they no longer
    * need to wait. This is used when the flusher no longer needs
    * the waiting thread to block. Such an occurrence happens when
    * all shared data has been written or has been duplicated.    
    */
   public void release() {
      lock.notifyAll();
   }

   /**
    * This is used to signal any blocking threads to wake up. When
    * this is invoked blocking threads are signaled and they can
    * return. This is typically done when the task has finished.
    */
   public void ready() {
      lock.notifyAll();
      running = false;
   }
   
   /**
    * This is used to close the scheduler when the reactor is
    * closed by the server. An close will happen when the server
    * has been shutdown, it ensures there are no threads lingering
    * waiting for a notification when the reactor has closed.
    */
   public void close() {
      lock.notifyAll();
      closed = true;
   }
}
