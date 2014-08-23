/*
 * BufferAllocator.java February 2001
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

package org.simpleframework.common.buffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * The <code>BufferAllocator</code> object is used to provide a means 
 * to allocate buffers using a single underlying buffer. This uses a
 * buffer from a existing allocator to create the region of memory to
 * use to allocate all other buffers. As a result this allows a single
 * buffer to acquire the bytes in a number of associated buffers. This
 * has the advantage of allowing bytes to be read in sequence without
 * joining data from other buffers or allocating multiple regions.
 *
 * @author Niall Gallagher
 */ 
public class BufferAllocator extends FilterAllocator implements Buffer {   

   /**
    * This is the underlying buffer all other buffers are within.
    */ 
   private Buffer buffer;

   /**
    * Constructor for the <code>BufferAllocator</code> object. This is
    * used to instantiate the allocator with a default buffer size of
    * half a kilobyte. This ensures that it can be used for general 
    * purpose byte storage and for minor I/O tasks.
    *
    * @param source this is where the underlying buffer is allocated
    */   
   public BufferAllocator(Allocator source) {
      super(source);
   }

   /**
    * Constructor for the <code>BufferAllocator</code> object. This is
    * used to instantiate the allocator with a specified buffer size.
    * This is typically used when a very specific buffer capacity is
    * required, for example a request body with a known length.
    *
    * @param source this is where the underlying buffer is allocated    
    * @param capacity the initial capacity of the allocated buffers
    */
   public BufferAllocator(Allocator source, long capacity) {
      super(source, capacity);
   }

   /**
    * Constructor for the <code>BufferAllocator</code> object. This is
    * used to instantiate the allocator with a specified buffer size.
    * This is typically used when a very specific buffer capacity is
    * required, for example a request body with a known length.
    *
    * @param source this is where the underlying buffer is allocated    
    * @param capacity the initial capacity of the allocated buffers
    * @param limit this is the maximum buffer size created by this
    */   
   public BufferAllocator(Allocator source, long capacity, long limit) {
      super(source, capacity, limit);
   }

   /**
    * This method is used so that a buffer can be represented as a
    * stream of bytes. This provides a quick means to access the data
    * that has been written to the buffer. It wraps the buffer within
    * an input stream so that it can be read directly.
    *
    * @return a stream that can be used to read the buffered bytes
    */    
   public InputStream open() throws IOException {  
      if(buffer == null) {
         allocate();
      }
      return buffer.open();
   }   
   /**
    * This method is used to acquire the buffered bytes as a string.
    * This is useful if the contents need to be manipulated as a
    * string or transferred into another encoding. If the UTF-8
    * content encoding is not supported the platform default is 
    * used, however this is unlikely as UTF-8 should be supported.
    *
    * @return this returns a UTF-8 encoding of the buffer contents
    */ 
   public String encode() throws IOException { 
      if(buffer == null) {
         allocate();
      }
      return buffer.encode();
   }

   /**
    * This method is used to acquire the buffered bytes as a string.
    * This is useful if the contents need to be manipulated as a
    * string or transferred into another encoding. This will convert
    * the bytes using the specified character encoding format.
    *
    * @return this returns the encoding of the buffer contents
    */      
   public String encode(String charset) throws IOException {   
      if(buffer == null) {
         allocate();
      }      
      return buffer.encode(charset);
   }
   
   /**
    * This method is used to append bytes to the end of the buffer. 
    * This will expand the capacity of the buffer if there is not
    * enough space to accommodate the extra bytes.
    *
    * @param array this is the byte array to append to this buffer
    *
    * @return this returns this buffer for another operation
    */ 
   public Buffer append(byte[] array) throws IOException {      
      return append(array, 0, array.length);
   }

   /**
    * This method is used to append bytes to the end of the buffer. 
    * This will expand the capacity of the buffer if there is not
    * enough space to accommodate the extra bytes.
    *
    * @param array this is the byte array to append to this buffer
    * @param size the number of bytes to be read from the array
    * @param off this is the offset to begin reading the bytes from
    *
    * @return this returns this buffer for another operation    
    */    
   public Buffer append(byte[] array, int off, int size) throws IOException {
      if(buffer == null) {
         allocate(size);
      }
      return buffer.append(array, off, size);
   }

   /**
    * This will clear all data from the buffer. This simply sets the
    * count to be zero, it will not clear the memory occupied by the
    * instance as the internal buffer will remain. This allows the
    * memory occupied to be reused as many times as is required.
    */    
   public void clear() throws IOException {
      if(buffer != null) {
         buffer.clear();
      }
   }

   /**
    * This method is used to ensure the buffer can be closed. Once
    * the buffer is closed it is an immutable collection of bytes and
    * can not longer be modified. This ensures that it can be passed
    * by value without the risk of modification of the bytes.
    */     
   public void close() throws IOException {
      if(buffer == null) {
         allocate();
      }
      buffer.close();      
   }

   /**
    * This method is used to allocate a default buffer. This will 
    * allocate a buffer of predetermined size, allowing it to grow 
    * to an upper limit to accommodate extra data. If the buffer
    * requested is larger than the limit an exception is thrown.
    *
    * @return this returns an allocated buffer with a default size
    */ 
   @Override
   public Buffer allocate() throws IOException {
      return allocate(capacity);
   }

   /**
    * This method is used to allocate a default buffer. This will 
    * allocate a buffer of predetermined size, allowing it to grow 
    * to an upper limit to accommodate extra data. If the buffer
    * requested is larger than the limit an exception is thrown.
    *
    * @param size the initial capacity of the allocated buffer
    *
    * @return this returns an allocated buffer with a default size
    */   
   @Override
   public Buffer allocate(long size) throws IOException {
      if(size > limit) {
         throw new BufferException("Specified size %s beyond limit", size);
      }           
      if(capacity > size) { // lazily create backing buffer
         size = capacity;
      }           
      if(buffer == null) {
         buffer = source.allocate(size);
      }          
      return buffer.allocate();
   }  
   
   /**
    * This is used to provide the number of bytes that have been
    * written to the buffer. This increases as bytes are appended
    * to the buffer. if the buffer is cleared this resets to zero.
    *  
    * @return this returns the number of bytes within the buffer
    */
   public long length() {
      return buffer.length();
   }
}
