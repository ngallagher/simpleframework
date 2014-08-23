/*
 * SocketFlusher.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

import java.io.IOException;

import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>SocketFlusher</code> flushes bytes to the underlying
 * socket channel. This allows asynchronous writes to the socket
 * to be managed in such a way that there is order to the way data
 * is delivered over the socket. This uses a selector to dispatch
 * flush invocations to the underlying socket when the socket is
 * write ready. This allows the writing thread to continue without
 * having to wait for all the data to be written to the socket.
 *
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.SocketBufferWriter
 */
class SocketFlusher {    
   
   /**
    * This is the signaller used to determine when to flush.
    */
   private FlushSignaller signaller;
   
   /**
    * This is the scheduler used to block and signal the writer.
    */
   private FlushScheduler scheduler;    
   
   /**
    * This is the writer used to queue the buffers written.
    */
   private SocketBuffer buffer;    
   
   /**
    * This is used to determine if the socket flusher is closed.
    */
   private boolean closed;
   
   /**
    * Constructor for the <code>SocketFlusher</code> object. This is
    * used to flush buffers to the underlying socket asynchronously.
    * When finished flushing all of the buffered data this signals
    * any threads that are blocking waiting for the write to finish.
    *
    * @param buffer this is used to write the buffered buffers
    * @param reactor this is used to perform asynchronous writes
    * @param socket this is the socket used to select with
    */
   public SocketFlusher(SocketBuffer buffer, Socket socket, Reactor reactor) throws IOException {
      this.signaller = new FlushSignaller(this, socket);
      this.scheduler = new FlushScheduler(socket, reactor, signaller, this);
      this.buffer = buffer;
   }

   /**
    * Here in this method we schedule a flush when the underlying
    * writer is write ready. This allows the writer thread to return
    * without having to fully flush the content to the underlying
    * transport. If there are references queued this will block.
    */  
   public synchronized void flush() throws IOException { 
      if(closed) {
         throw new TransportException("Flusher is closed");
      }
      boolean block = !buffer.ready();

      if(!closed) {
         scheduler.schedule(block);
      }
   }
   
   /**
    * This is executed when the flusher is to write all of the data to
    * the underlying socket. In this situation the writes are attempted
    * in a non blocking way, if the task does not complete then this
    * will simply enqueue the writing task for OP_WRITE and leave the
    * method. This returns true if all the buffers are written.
    */   
   public synchronized void execute() throws IOException {      
      boolean ready = buffer.flush(); 

      if(!ready) { 
         boolean block = !buffer.ready(); 

         if(!block && !closed) {
            scheduler.release(); 
         }
         scheduler.repeat();
      } else{
         scheduler.ready();
      }
   }
   
   /**
    * This is used to abort the flushing process when the reactor has
    * been stopped. An abort to the flusher typically happens when the
    * server has been shutdown. It prevents threads lingering waiting
    * for a I/O operation which prevents the server from shutting down.
    */
   public synchronized void abort() throws IOException {
      scheduler.close();
      buffer.close();
   }
   
   /**
    * This is used to close the flusher ensuring that all of the
    * data within the writer will be flushed regardless of the 
    * amount of data within the writer that needs to be written. If
    * the writer does not block then this waits to be finished.
    */
   public synchronized void close() throws IOException {
      boolean ready = buffer.flush();
      
      if(!closed) {
         closed = true;
      }
      if(!ready) {
         scheduler.schedule(true); 
      }
   }   
}
