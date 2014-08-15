/*
 * SocketWriter.java February 2008
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

import static org.simpleframework.transport.TransportEvent.CLOSE;
import static org.simpleframework.transport.TransportEvent.ERROR;
import static org.simpleframework.transport.TransportEvent.WRITE;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.simpleframework.transport.trace.Trace;

/**
 * The <code>SocketWriter</code> object is used to coalesce the
 * packets to be written in to a minimum size. Also this will queue
 * the packets to be written in the order they are provided to that
 * if the contents of the packets can not be fully written they 
 * can be flushed in the correct order.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.BufferCompacter
 */
class SocketWriter implements PacketWriter {
   
   /**
    * This is the manager used to build the segments to be written.
    */
   private BufferCompacter compacter;
   
   /**
    * The socket channel that the byte contents are written to.
    */
   private SocketChannel channel;
   
   /**
    * The trace that is used to monitor events within the writer.
    */
   private Trace trace;
   
   /**
    * This is used to determine whether the socket is closed.
    */
   private boolean closed;
   
   /**
    * Constructor for the <code>SocketWriter</code> object. This
    * is used to wrap the socket in an object that will queue and
    * coalesce the packets written. It ensures that the packets
    * that are sent are of a minimum size for performance.
    * 
    * @param socket this is the pipeline instance this wraps
    */
   public SocketWriter(Socket socket) {
      this(socket, 20480);
   }
    
   /**
    * Constructor for the <code>SocketWriter</code> object. This
    * is used to wrap the socket in an object that will queue and
    * coalesce the packets written. It ensures that the packets
    * that are sent are of a minimum size for performance.
    * 
    * @param socket this is the pipeline instance this wraps
    * @param limit this is the threshold for asynchronous buffers
    */
   public SocketWriter(Socket socket, int limit) {
      this.compacter = new BufferCompacter(limit);
      this.channel = socket.getChannel();
      this.trace = socket.getTrace();
   }

   /**
    * This provides the socket for the writer. Providing this 
    * enables a <code>Reactor</code> to be used to determine when
    * the writer is write ready and thus when the writer can
    * be flushed if it contains packets that have not been written.
    * 
    * @return this returns the socket associated with this
    */
   public synchronized SocketChannel getChannel() {
      return channel;
   }
   
   /**
    * This is used to determine if the writer should block or not.
    * A writer will block only if there are shared packets still
    * within the write queue. When all shared packets have either
    * been written or duplicated then the writer does not need to
    * block any waiting threads and they can be released.
    * 
    * @return true if any writing thread should be blocked
    */
   public synchronized boolean isBlocking() throws IOException {
      return compacter.isReference();
   }

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
   public synchronized boolean write(Packet packet) throws IOException {
      BufferSegment segment = compacter.build(packet); 
      
      if(segment == null) {
         return true;
      }
      return flush(); 
   }
   
   /**
    * This is used to send the packets to the socket. This attempts
    * to write the provided packet to the underlying socket, if 
    * all of the bytes are written the the packet is closed. This
    * will return the number of bytes that have been written.
    * 
    * @param segment this is the packet that is the be sent
    * 
    * @return the number of bytes written to the underlying socket
    */
   private synchronized int write(BufferSegment segment) throws IOException {   
      int size = segment.write(channel);      
      int left = segment.length();
      
      if(left == 0) {
         segment.close();
      }
      if(trace != null) {
         trace.trace(WRITE, size);
      }    
      if(size < 0) {
         throw new TransportException("Socket is closed");
      }
      return size;
   }

   /**
    * This is used to flush all queued packets to the underlying
    * socket. If all of the queued packets have been fully written
    * then this returns true, otherwise this will return false.
    * 
    * @return true if all queued packets are flushed to the socket
    */   
   public synchronized boolean flush() throws IOException {
      BufferSegment segment = compacter.build(); 
      
      while(segment != null) {         
         int size = write(segment); 
         
         if(size < 0) { 
            throw new TransportException("Connection reset");
         }
         if(size == 0) {
            break;
         }
         segment = compacter.build();   
      }
      return complete();
   }
   
   /**
    * This is used to determine the the writer is done writing all
    * of the enqueued packets. This is determined by checking if
    * the sum of the number of packets remaining is greater than
    * zero. If there are remaining packets they are compacted.
    * 
    * @return this returns true if the writer is now empty
    */
   private synchronized boolean complete() throws IOException {      
      int count = compacter.length();
      
      if(count > 0) {
         compacter.compact();
      }
      return count <= 0; 
   }

   /**
    * This is used to close the writer and the underlying socket.
    * If a close is performed on the writer then no more bytes 
    * can be read from or written to the writer and the client 
    * will receive a connection close on their side. This also 
    * ensures that the TCP FIN ACK is sent before the actual
    * channel is closed. This is required for a clean shutdown.
    */  
   public synchronized void close() throws IOException {
      if(!closed) {    
         closed = true;
         compacter.close();
         try{
            trace.trace(CLOSE);
            channel.socket().shutdownOutput();
         }catch(Throwable cause){  
            trace.trace(ERROR, cause);
         }
         channel.close();         
      }
   }
}
