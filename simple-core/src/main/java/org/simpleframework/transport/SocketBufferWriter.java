/*
 * SocketBufferWriter.java February 2008
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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>SocketBufferWriter</code> is used to represent the means 
 * to write buffers to an underlying transport. This manages all of 
 * the selection required to determine if the socket is write ready.
 * If the buffer to be written is to block then this will wait 
 * until all queue buffers are fully written.
 * 
 * @author Niall Gallagher
 */
class SocketBufferWriter {   
   
   /**
    * This is the flusher that is used to asynchronously flush.
    */
   private final SocketFlusher flusher;   

   /**
    * This is the writer that is used to queue the buffers.
    */
   private final SocketBuffer writer;   
   
   /**
    * Constructor for the <code>SocketBufferWriter</code> object. This 
    * is used to create a writer that can write buffers to the socket
    * in such a way that it write either asynchronously or block 
    * the calling thread until such time as the buffers are written.
    * 
    * @param socket this is the pipeline that this writes to 
    * @param reactor this is the writer used to scheduler writes
    * @param buffer this is the initial size of the output buffer
    * @param threshold this is the maximum size of the buffer 
    */
   public SocketBufferWriter(Socket socket, Reactor reactor, int buffer, int threshold) throws IOException {
      this.writer = new SocketBuffer(socket,  buffer, threshold);
      this.flusher = new SocketFlusher(writer, socket, reactor);
   }

   /**
    * This method is used to deliver the provided buffer of bytes to
    * the underlying transport. This will not modify the data that
    * is to be written, this will simply queue the buffers in the
    * order that they are provided.
    *
    * @param buffer this is the array of bytes to send to the client
    */  
   public void write(ByteBuffer buffer) throws IOException {
      boolean done = writer.write(buffer); // returns true if we can buffer

      if(!done) {
         flusher.flush(); // we could not fully write or buffer the data so we must flush
      }
   }

   /**
    * This method is used to flush all of the queued buffers to 
    * the client. This method will not block but will simply flush 
    * any data to the underlying transport. Internally the data 
    * will be queued for delivery to the connected entity.    
    */ 
   public void flush() throws IOException {
      boolean done = writer.flush(); // returns true only if everything is delivered

      if(!done) {
         flusher.flush(); // here we will block for an op write event if the buffer contains a reference
      }
   }

   /**
    * This is used to close the writer and the underlying socket.
    * If a close is performed on the writer then no more bytes 
    * can be read from or written to the writer and the client 
    * will receive a connection close on their side.
    */ 
   public void close() throws IOException {
      flusher.close();
      writer.close();
   }
}
