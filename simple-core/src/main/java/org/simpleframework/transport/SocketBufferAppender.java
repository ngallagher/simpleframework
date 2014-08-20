/*
 * SocketBufferAppender.java February 2008
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

import static org.simpleframework.transport.TransportEvent.WRITE;
import static org.simpleframework.transport.TransportEvent.WRITE_BUFFER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;

import org.simpleframework.transport.trace.Trace;

/**
 * The <code>SocketBufferAppender</code> represents a buffer fragment
 * collector. This provides write access to a direct byte buffer which
 * is used to collect fragments. Once a sufficient amount of data
 * has been collected by this then can be written out to a channel. 
 * 
 * @author Niall Gallagher
 */
class SocketBufferAppender {

   /**
    * This is the buffer used to store the contents of the buffer.
    */
   private ByteBuffer buffer;
   
   /**
    * This is the trace used to watch the buffering events.
    */
   private Trace trace;
  
   /**
    * This represents the the initial size of the buffer to use.
    */
   private int chunk;
   
   /**
    * This represents the largest this appender can grow to.
    */
   private int limit;

   /**
    * Constructor for the <code>SocketBufferAppender</code> object. This
    * is used to create an appender that can collect smaller fragments
    * in to a larger buffer so that it can be delivered more efficiently.
    * 
    * @param socket this is the socket to append data for
    * @param chunk this is the initial size of the buffer 
    * @param limit this is the maximum size of the buffer
    */
   public SocketBufferAppender(Socket socket, int chunk, int limit) {
      this.buffer = ByteBuffer.allocateDirect(chunk);
      this.trace = socket.getTrace();
      this.chunk = chunk;
      this.limit = limit;
   }
   
   /**
    * This is used to determine how much space is left to append 
    * data to this buffer. This is typically equivalent to capacity
    * minus the length. However in the event that the buffer uses 
    * a private memory store that can not be written to then this
    * can return zero regardless of the capacity and length.
    *
    * @return the space left within the buffer to append data to
    */    
   public int space() {         
      return buffer.remaining();
   }

   /**
    * This represents the capacity of the backing store. The buffer
    * is full when length is equal to capacity and it can typically
    * be appended to when the length is less than the capacity. The
    * only exception is when <code>space</code> returns zero, which
    * means that the buffer can not have bytes appended to it.
    *
    * @return this is the capacity of other backing byte storage
    */    
   public int capacity() {    
      return buffer.capacity();
   }

   /**        
    * This is used to determine how mnay bytes remain within this
    * buffer. It represents the number of write ready bytes, so if
    * the length is greater than zero the buffer can be written to
    * a byte channel. When length is zero the buffer can be closed.
    * 
    * @return this is the number of bytes remaining in this buffer
    */    
   public int length() {         
      return capacity() - space();
   }

   /**
    * This is used to encode the underlying byte sequence to text.
    * Converting the byte sequence to text can be useful when either
    * debugging what exactly is being sent. Also, for transports 
    * that require string delivery of buffers this can be used. 
    *
    * @return this returns the bytes sequence as a string object
    */    
   public String encode() throws IOException {
      return encode("UTF-8"); 
   }

   /**
    * This is used to encode the underlying byte sequence to text.
    * Converting the byte sequence to text can be useful when either
    * debugging what exactly is being sent. Also, for transports 
    * that require string delivery of buffers this can be used. 
    *
    * @param encoding this is the character set to use for encoding
    *
    * @return this returns the bytes sequence as a string object
    */      
   public String encode(String encoding) throws IOException {
      ByteBuffer segment = buffer.duplicate();

      if(segment != null) {
         segment.flip();
      }
      return encode(encoding, segment);
   }

   /**
    * This is used to encode the underlying byte sequence to text.
    * Converting the byte sequence to text can be useful when either
    * debugging what exactly is being sent. Also, for transports 
    * that require string delivery of buffers this can be used. 
    *
    * @param encoding this is the character set to use for encoding
    * @param segment this is the buffer that is to be encoded
    *
    * @return this returns the bytes sequence as a string object
    */
   private String encode(String encoding, ByteBuffer segment) throws IOException {
      Charset charset = Charset.forName(encoding); 
      CharBuffer text = charset.decode(segment);

      return text.toString();
   }

   /**
    * This will append bytes within the given buffer to the buffer.
    * Once invoked the buffer will contain the buffer bytes, which
    * will have been drained from the buffer. This effectively moves
    * the bytes in the buffer to the end of the buffer instance.
    *
    * @param data this is the buffer containing the bytes
    *
    * @return returns the number of bytes that have been moved
    */     
   public int append(ByteBuffer data) throws IOException {
      int require = data.remaining(); 
      int space = space();
      
      if(require > space) {
         require = space;
      }
      return append(data, require);      
   }

   /**
    * This will append bytes within the given buffer to the buffer.
    * Once invoked the buffer will contain the buffer bytes, which
    * will have been drained from the buffer. This effectively moves
    * the bytes in the buffer to the end of the buffer instance.
    *
    * @param data this is the buffer containing the bytes
    * @param count this is the number of bytes that should be used
    *
    * @return returns the number of bytes that have been moved
    */    
   public int append(ByteBuffer data, int count) throws IOException {
      ByteBuffer segment = data.slice();
      int mark = data.position();
      int size = mark + count;
      
      if(count > 0) {
         if(trace != null) {
            trace.trace(WRITE_BUFFER, count);
         }
         data.position(size); 
         segment.limit(count); 
         buffer.put(segment); 
      }
      return count;
   }
   
   /**
    * This write method will write the contents of the buffer to the
    * provided byte channel. If the whole buffer can be be written
    * then this will simply return the number of bytes that have. 
    * The number of bytes remaining within the buffer after a write
    * can be acquired from the <code>length</code> method. Once all
    * of the bytes are written the buffer must be closed.
    *
    * @param channel this is the channel to write the buffer to
    *    
    * @return this returns the number of bytes that were written
    */ 
   public int write(ByteChannel channel) throws IOException {
      int size = length();     

      if(size <= 0) { 
         return 0;
      }
      return write(channel, size);
   }

   /**
    * This write method will write the contents of the buffer to the
    * provided byte channel. If the whole buffer can be be written
    * then this will simply return the number of bytes that have. 
    * The number of bytes remaining within the buffer after a write
    * can be acquired from the <code>length</code> method. Once all
    * of the bytes are written the buffer must be closed.
    *
    * @param channel this is the channel to write the buffer to
    * @param count the number of bytes to write to the channel
    *
    * @return this returns the number of bytes that were written
    */    
   public int write(ByteChannel channel, int count) throws IOException {       
      if(count > 0) {
         buffer.flip();
      } else { 
         return 0;
      }
      return write(channel, buffer);
   }

   /**
    * This write method will write the contents of the buffer to the
    * provided byte channel. If the whole buffer can be be written
    * then this will simply return the number of bytes that have. 
    * The number of bytes remaining within the buffer after a write
    * can be acquired from the <code>length</code> method. Once all
    * of the bytes are written the buffer must be closed.
    *
    * @param channel this is the channel to write the buffer to
    * @param segment this is the buffer that is to be written
    *
    * @return this returns the number of bytes that were written
    */     
   private int write(ByteChannel channel, ByteBuffer segment) throws IOException {
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
      if(count >= 0) {
         segment.compact(); 
      }
      return count;
   }
}

