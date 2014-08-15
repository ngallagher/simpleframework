/*
 * SocketController.java February 2008
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

import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>SocketController</code> is used to represent the means 
 * to write packets to an underlying transport. This manages all of 
 * the selection required to determine if the socket is write ready.
 * If the packet to be written is to block then this will wait 
 * until all queue packets are fully written.
 * 
 * @author Niall Gallagher
 */
class SocketController implements PacketController {
   
   /**
    * This is the flusher that is used to asynchronously flush.
    */
   private final PacketFlusher flusher;

   /**
    * This is the writer that is used to queue the packets.
    */
   private final PacketWriter writer;
   
   /**
    * Constructor for the <code>SocketWriter</code> object. This is 
    * used to create a writer that can write packets to the socket
    * in such a way that it write either asynchronously or block 
    * the calling thread until such time as the packets are written.
    * 
    * @param socket this is the pipeline that this writes to 
    * @param reactor this is the writer used to scheduler writes
    * @param threshold this is the threshold for asynchronous buffers  
    */
   public SocketController(Socket socket, Reactor reactor, int threshold) throws IOException {
      this.writer = new SocketWriter(socket, threshold);
      this.flusher = new SocketFlusher(socket, reactor, writer);
   }

   /**
    * This method is used to deliver the provided packet of bytes to
    * the underlying transport. This will not modify the data that
    * is to be written, this will simply queue the packets in the
    * order that they are provided.
    *
    * @param packet this is the array of bytes to send to the client
    */  
   public void write(Packet packet) throws IOException {
      boolean done = writer.write(packet);

      if(!done) {
         flusher.flush();
      }
   }

   /**
    * This method is used to flush all of the queued packets to 
    * the client. This method will block not block but will simply
    * flush any data to the underlying transport. Internally the
    * data will be queued for delivery to the connected entity.    
    */ 
   public void flush() throws IOException {
      boolean done = writer.flush();

      if(!done) {
         flusher.flush();
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
