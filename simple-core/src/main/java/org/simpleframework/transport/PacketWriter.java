/*
 * PacketWriter.java February 2008
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
import java.nio.channels.SocketChannel;

/**
 * The <code>PacketWriter</code> object provides a means to coalesce
 * packets at a single point before being written to the socket. It
 * is used to ensure all packets are queued in order of sequence
 * number. Any packets that are partially written to the underlying
 * socket can be coalesced in to a single packet so that a larger
 * packet can be delivered over the network.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.PacketFlusher
 */
interface PacketWriter {
  
   /**
    * This is used to determine if the writer should block or not.
    * A writer will block only if there are shared packets still
    * within the write queue. When all shared packets have either
    * been written or duplicated then the writer does not need to
    * block any waiting threads and they can be released.
    * 
    * @return true if any writing thread should be blocked
    */
   boolean isBlocking() throws IOException;
   
   /**
    * This provides the socket for the writer. Providing this 
    * enables a <code>Reactor</code> to be used to determine when
    * the writer is write ready and thus when the writer can
    * be flushed if it contains packets that have not been written.
    * 
    * @return this returns the socket associated with this
    */
   SocketChannel getChannel();
   
   /**
    * This is used to write the packets to the writer which will
    * be either written to the underlying socket or queued until
    * such time as the socket is write ready. This will return true
    * if the packet has been written to the underlying transport.
    * 
    * @param packet this is the packet that is the be written
    * 
    * @return true if the packet has been written to the transport
    */
   boolean write(Packet packet) throws IOException;
   
   /**
    * This is used to flush all queued packets to the underlying
    * socket. If all of the queued packets have been fully written
    * then this returns true, otherwise this will return false.
    * 
    * @return true if all queued packets are flushed to the socket
    */
   boolean flush() throws IOException;
   
   /**
    * This is used to close the writer and the underlying socket.
    * If a close is performed on the writer then no more bytes 
    * can be read from or written to the writer and the client 
    * will receive a connection close on their side.
    */  
   void close() throws IOException;   
}
