/*
 * SocketBuffer.java February 2014
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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.simpleframework.transport.trace.Trace;

/**
 * The <code>SocketBuffer</code> represents a buffer that aggregates 
 * small fragments in to a single buffer before sending them. This
 * is primarily used as a means to avoid sending many small packets
 * rather than reasonable size ones for performance. This also
 * enables a higher level of concurrency, as it will allow data
 * that can't be sent over the socket to be buffered until it gets
 * the signal that says it can be sent on. 
 * 
 * @author Niall Gallagher
 */
class SocketBuffer {  
   
   /**
    * This is a small internal buffer to collect fragments.
    */
   private SocketBufferAppender appender;      
   
   /**
    * This is the underlying socket to sent to the data over.
    */
   private SocketChannel channel;   
   
   /**
    * This is a reference to the last buffer to be sent.
    */
   private ByteBuffer reference;   
   
   /**
    * This is used to trace various events that occur.
    */
   private Trace trace;
   
   /**
    * This is the recommended minimum packet size to send.
    */
   private int chunk;
   
   /**
    * This is used to determine if the buffer was closed.
    */
   private boolean closed;

   /**
    * Constructor for the <code>SocketBuffer</code> object. This is 
    * used to create a buffer that will collect small fragments sent
    * in to a more reasonably sized packet. 
    *  
    * @param socket this is the socket to write the data to
    * @param chunk this is the minimum packet size to used
    * @param limit this is the maximum size of the output buffer 
    */
   public SocketBuffer(Socket socket, int chunk, int limit) {
      this.appender = new SocketBufferAppender(socket, chunk, limit);
      this.channel = socket.getChannel();
      this.trace = socket.getTrace();
      this.chunk = chunk;
   }
   
   /**
    * This is used to determine if the buffer is ready to be written
    * to. A buffer is ready when it does not hold a reference to
    * any other buffer internally. The the <code>flush</code> method
    * must return true for a buffer to be considered ready.
    * 
    * @return returns true if the buffer is ready to write to
    */
   public synchronized boolean ready() throws IOException {
      if(closed) {
         throw new TransportException("Buffer has been closed");
      }
      if(reference != null) { 
         int remaining = reference.remaining();
         
         if(remaining <= 0) {
            reference = null;
            return true;
         }
         return false;
      }
      return true;
   }
   
   /**
    * This will write the bytes to underlying channel if the data is
    * greater than the minimum buffer size. If it is less than the
    * minimum size then it will be appended to the internal buffer.
    * If it is larger than the maximum size of the internal buffer 
    * a reference is kept to it. This reference can only be cleared
    * with the <code>flush</code> method, which will attempt to
    * write the data to the channel, and buffer any remaining data
    * if the underly connection is busy.
    * 
    * @param data this is the data to write the the channel.
    * 
    * @return this returns true if no reference was held
    */     
   public synchronized boolean write(ByteBuffer duplicate) throws IOException {
      if(closed) {
         throw new TransportException("Buffer has been closed");
      }
      if(reference != null) {
         throw new IOException("Buffer already pending write");
      }
      int count = appender.length();
      
      if(count > 0) {
         return merge(duplicate);
      }
      int remaining = duplicate.remaining();
      
      if(remaining < chunk) {
         appender.append(duplicate);// just save it..
         return true;
      }
      if(!flush(duplicate)) { // attempt a write
         int space = appender.space();
         
         if(remaining < space) {
            appender.append(duplicate);
            return true;
         }
         reference = duplicate;
         return false;         
      }
      return true;
   }
   
   /**
    * This method is used to perform a merge of the buffer to be sent
    * with the current buffer. If the internal buffer is large enough
    * to send after the merge then it will be sent. Also, if the 
    * remaining bytes in the buffer are large enough for a packet
    * then that too will be sent over the socket.
    * 
    * @param duplicate this is the buffer to be merged
    * 
    * @return this returns true if no reference was held
    */
   private synchronized boolean merge(ByteBuffer duplicate) throws IOException {
      if(closed) {
         throw new TransportException("Buffer has been closed");
      }
      int count = appender.length();
      int merged = appender.append(duplicate);
      int payload = merged + count;
      
      if(payload >= chunk) { // viable packet size
         int written = appender.write(channel);

         if(written < payload) {// count not fully flush buffer               
            reference = duplicate;
            return false;
         }
         return write(duplicate); // we are back at zero
      }         
      return true; // everything was buffered as chunk >= capacity
   }   
   
   /**
    * This method is used to fully flush the contents of the buffer to
    * the underlying output stream. This will only ever return true
    * if there are no references held and no data internally buffered.
    * If before this method is invoked a reference to a byte buffer 
    * is held then this will attempt to merge it with the internal
    * buffer so that the <code>ready</code> method can return true.
    * This ensures that the writing thread does not need to block.
    * 
    * @return this returns true if all of the bytes are sent
    */
   public synchronized boolean flush() throws IOException {
      if(closed) {
         throw new TransportException("Buffer has been closed");
      }      
      int count = appender.length();
      
      if(count > 0) {
         int written = appender.write(channel);

         if(written < count) {            
            compact();
            return false; // we are still buffering
         }           
      }
      if(reference != null) {
         if(!flush(reference)) {          
            compact();
            return false;   
         }
         reference = null;
      }
      return true; // no more data buffered      
   }

   /**
    * This write method will write the contents of the buffer to the
    * provided byte channel. If the whole buffer can be be written
    * then this will simply return the number of bytes that have. 
    * The number of bytes remaining within the packet after a write
    * can be acquired from the <code>length</code> method. Once all
    * of the bytes are written the packet must be closed.
    *
    * @param channel this is the channel to write the packet to
    * @param segment this is the segment that is to be written
    *
    * @return this returns the number of bytes that were written
    */ 
   private synchronized boolean flush(ByteBuffer segment) throws IOException {
      if(closed) {
         throw new TransportException("Buffer has been closed");
      }      
      int require = segment.remaining();
      int count = 0;
      
      while(count < require) { 
         int size = channel.write(segment);

         if(size <= 0) {
            break;            
         }
         if(trace != null) { 
            trace.trace(WRITE, size);
         }          
         count += size;
      }
      if(count == require) {
         return true;
      }
      return false;
   }
   
   /**
    * To ensure that we can release any references and thus avoid a
    * blocking thread this method will attempt to merge references
    * in to the internal buffer. Compacting in this manner is done
    * only if the full reference can fit in to the available space.
    */
   private synchronized void compact() throws IOException {
      if(closed) {
         throw new TransportException("Buffer has been closed");
      }         
      if(reference != null) {
         int remaining = reference.remaining();            
         int space = appender.space();
         
         if(remaining < space) {
            appender.append(reference); // try to release the buffer
            reference = null;
         }
      }
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
      if(closed) {
         throw new TransportException("Buffer has been closed");
      }
      if(!closed) {    
         try{            
            closed = true;           
            trace.trace(CLOSE);
            channel.socket().shutdownOutput();
         }catch(Throwable cause){  
            trace.trace(ERROR, cause);
         }
         channel.close();         
      }
   }   
}


