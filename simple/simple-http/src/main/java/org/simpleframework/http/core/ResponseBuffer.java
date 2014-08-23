/*
 * ResponseBuffer.java February 2007
 *
 * Copyright (C) 2001, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.simpleframework.http.Response;
import org.simpleframework.http.message.Entity;
import org.simpleframework.transport.Channel;

/**
 * The <code>ResponseBuffer</code> object is an output stream that can
 * buffer bytes written up to a given size. This is used if a buffer
 * is requested for the response output. Such a mechanism allows the
 * response to be written without committing the response. Also it
 * enables content that has been written to be reset, by simply
 * clearing the response buffer. If the response buffer overflows
 * then the response is committed.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.core.ResponseEncoder
 */ 
class ResponseBuffer extends OutputStream implements WritableByteChannel {
  
   /**
    * This is the transfer object used to transfer the response.
    */         
   private ResponseEncoder encoder;
   
   /**
    * This is the buffer used to accumulate the response bytes.
    */ 
   private byte[] buffer;
   
   /**
    * This is used to determine if the accumulate was flushed.
    */ 
   private boolean flushed;
   
   /**
    * This is used to determine if the accumulator was closed.
    */ 
   private boolean closed;
   
   /**
    * This counts the number of bytes that have been accumulated.
    */ 
   private int count;
  
   /**
    * Constructor for the <code>ResponseBuffer</code> object. This will
    * create a buffering output stream which will flush data to the
    * underlying transport provided with the entity. All I/O events
    * are reported to the monitor so the server can process other
    * requests within the pipeline when the current one is finished.
    *
    * @param observer this is used to notify of response completion
    * @param response this is the response header for this buffer       
    * @param support this is used to determine the response semantics
    * @param entity this is used to acquire the underlying transport   
    */ 
   public ResponseBuffer(BodyObserver observer, Response response, Conversation support, Entity entity) {
      this(observer, response, support, entity.getChannel());
   }   
   
   /**
    * Constructor for the <code>ResponseBuffer</code> object. This will
    * create a buffering output stream which will flush data to the
    * underlying transport provided with the channel. All I/O events
    * are reported to the monitor so the server can process other
    * requests within the pipeline when the current one is finished.
    *
    * @param observer this is used to notify of response completion
    * @param response this is the response header for this buffer    
    * @param support this is used to determine the response semantics
    * @param channel this is the channel used to write the data to 
    */       
   public ResponseBuffer(BodyObserver observer, Response response, Conversation support, Channel channel) {
      this.encoder = new ResponseEncoder(observer, response, support, channel);
      this.buffer = new byte[] {};
   }
   
   /**
    * This is used to determine if the accumulator is still open. If
    * the accumulator is still open then data can still be written to
    * it and this transmitted to the client. When the accumulator is
    * closed the data is committed and this can not be used.
    * 
    * @return this returns true if the accumulator object is open
    */
   public boolean isOpen() {
      return !closed;
   }
  
   /**
    * This is used to reset the buffer so that it can be written to
    * again. If the accumulator has already been flushed then the
    * stream can not be reset. Resetting the stream is typically 
    * done if there is an error in writing the response and an error
    * message is generated to replaced the partial response.
    */  
   public void reset() throws IOException {
      if(flushed) {
         throw new IOException("Response has been flushed");
      }
      count = 0;
   }
   
   /**
    * This is used to write the provided octet to the buffer. If the
    * buffer is full it will be flushed and the octet is appended to
    * the start of the buffer. If however the buffer is zero length
    * then this will write directly to the underlying transport.
    *
    * @param octet this is the octet that is to be written
    */ 
   public void write(int octet) throws IOException {
      byte value = (byte) octet;  
      
      if(closed) {
         throw new IOException("Response has been transferred");
      }
      write(new byte[] { value });
   }
   
   /**
    * This is used to write the provided array to the buffer. If the
    * buffer is full it will be flushed and the array is appended to
    * the start of the buffer. If however the buffer is zero length
    * then this will write directly to the underlying transport.
    *
    * @param array this is the array of bytes to send to the client
    * @param off this is the offset within the array to send from
    * @param size this is the number of bytes that are to be sent
    */ 
   public void write(byte[] array, int off, int size) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(array, off, size);
      
      if(size > 0) {
         write(buffer);
      }      
   }
   
   /**
    * This is used to write the provided buffer to the buffer. If the
    * buffer is full it will be flushed and the buffer is appended to
    * the start of the buffer. If however the buffer is zero length
    * then this will write directly to the underlying transport.
    *
    * @param source this is the byte buffer to send to the client
    * 
    * @return this returns the number of bytes that have been sent
    */ 
   public int write(ByteBuffer source) throws IOException {
      int mark = source.position();
      int size = source.limit();
      
      if(mark > size) {
         throw new ResponseException("Buffer position greater than limit");
      }
      return write(source, 0, size - mark);
   }
   
   /**
    * This is used to write the provided buffer to the buffer. If the
    * buffer is full it will be flushed and the buffer is appended to
    * the start of the buffer. If however the buffer is zero length
    * then this will write directly to the underlying transport.
    *
    * @param source this is the byte buffer to send to the client
    * @param off this is the offset within the array to send from
    * @param size this is the number of bytes that are to be sent 
    * 
    * @return this returns the number of bytes that have been sent
    */ 
   public int write(ByteBuffer source, int off, int size) throws IOException {
      if(closed) {
         throw new IOException("Response has been transferred");
      }
      int mark = source.position();
      int limit = source.limit();
      
      if(limit - mark < size) { // not enough data
         size = limit - mark; // reduce expectation
      }
      if(count + size > buffer.length) {
         flush(false);
      }
      if(size > buffer.length){
         encoder.write(source);
      } else {
         source.get(buffer, count, size);
         count += size;
      }
      return size;
   }
   
   /**
    * This is used to expand the capacity of the internal buffer. If
    * there is already content that has been appended to the buffer
    * this will copy that data to the newly created buffer. This 
    * will not decrease the size of the buffer if it is larger than
    * the requested capacity.
    *
    * @param capacity this is the capacity to expand the buffer to
    */ 
   public void expand(int capacity) throws IOException {
      if(buffer.length < capacity) {
         int size = buffer.length * 2;
         int resize = Math.max(capacity, size);
         byte[] temp = new byte[resize];
         
         System.arraycopy(buffer, 0, temp, 0, count);
         buffer = temp;
      }
   }
   
   /**
    * This is used to flush the contents of the buffer to the 
    * underlying transport. Once the accumulator is flushed the HTTP
    * headers are written such that the semantics of the connection
    * match the protocol version and the existing response headers.
    */ 
   public void flush() throws IOException {    
      flush(true);
   }  
   
   /**
    * This is used to flush the contents of the buffer to the 
    * underlying transport. Once the accumulator is flushed the HTTP
    * headers are written such that the semantics of the connection
    * match the protocol version and the existing response headers.
    * 
    * @param flush indicates whether the transport should be flushed
    */ 
   private void flush(boolean flush) throws IOException {
      if(!flushed) {
         encoder.start();
      }
      if(count > 0) {
         encoder.write(buffer, 0, count);
      }
      if(flush) {
         encoder.flush();
      }
      flushed = true;
      count = 0;   
   }
   
   /**
    * This will flush the buffer to the underlying transport and 
    * close the stream.  Once the accumulator is flushed the HTTP
    * headers are written such that the semantics of the connection
    * match the protocol version and the existing response headers.
    * Closing this stream does not mean the connection is closed.
    */ 
   public void close() throws IOException {
      if(!closed) {
         commit();
      }
      flushed = true;
      closed = true;      
   }
   
   /**
    * This will close the underlying transfer object which will 
    * notify the server kernel that the next request is read to be
    * processed. If the accumulator is unflushed then this will set
    * a Content-Length header such that it matches the number of
    * bytes that are buffered within the internal buffer.
    */ 
   private void commit() throws IOException {
      if(!flushed) {
         encoder.start(count);
      }
      if(count > 0) {		      
         encoder.write(buffer, 0, count);
      }
      encoder.close();      
   }
}


