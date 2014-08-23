/*
 * FlushSignaller.java February 2008
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

import static org.simpleframework.transport.TransportEvent.ERROR;

import java.nio.channels.SocketChannel;

import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>FlushSignaller</code> is an operation that performs 
 * writes operation asynchronously. This will basically determine
 * if the socket is write ready and drain each queued buffer to
 * the socket until there are no more pending buffers.
 * 
 * @author Niall Gallagher
 */
class FlushSignaller implements Operation {
   
   /**
    * This is the writer that is used to write the data.
    */
   private final SocketFlusher writer;
   
   /**
    * This is the socket that this will be flushing.
    */
   private final Socket socket;
   
   /**
    * This is used to trace the activity for the operation.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>FlushSignaller</code> object. This 
    * will create an operation that is used to flush the buffer 
    * queue to the underlying socket. This ensures that the data 
    * is written to the socket in the queued order.
    *
    * @param writer this is the writer to flush the data to
    * @param socket this is the socket to be flushed
    */
   public FlushSignaller(SocketFlusher writer, Socket socket) {
      this.trace = socket.getTrace();
      this.socket = socket;
      this.writer = writer;
   }
   
   /**
    * This is used to acquire the trace object that is associated
    * with the operation. A trace object is used to collection details
    * on what operations are being performed. For instance it may 
    * contain information relating to I/O events or errors. 
    * 
    * @return this returns the trace associated with this operation
    */    
   public Trace getTrace() {
      return trace;
   }   
   
   /**
    * This returns the socket channel for the connected pipeline. It
    * is this channel that is used to determine if there are bytes
    * that can be written. When closed this is no longer selectable.
    *
    * @return this returns the connected channel for the pipeline
    */
   public SocketChannel getChannel() {
      return socket.getChannel();
   }

   /**
    * This is used to perform the drain of the pending buffer
    * queue. This will drain each pending queue if the socket is
    * write ready. If the socket is not write ready the operation
    * is enqueued for selection and this returns. This ensures
    * that all the data will eventually be delivered.
    */
   public void run() {
      try {
         writer.execute();
      } catch(Exception cause) {           
         trace.trace(ERROR, cause);
         cancel();
      }
   }
   
   /**
    * This is used to cancel the operation if it has timed out.
    * If the delegate is waiting too long to flush the contents
    * of the buffers to the underlying transport then the socket
    * is closed and the flusher times out to avoid deadlock.
    */
   public void cancel() {
      try {       
         writer.abort();
      }catch(Exception cause){    
         trace.trace(ERROR, cause);
      }
   }
} 
