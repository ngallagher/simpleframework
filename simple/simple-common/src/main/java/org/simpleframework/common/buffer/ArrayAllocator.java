/*
 * ArrayAllocator.java February 2001
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

/**
 * The <code>ArrayAllocator</code> object is used to provide a means 
 * to allocate buffers using a single byte array. This essentially uses 
 * the heap to allocate all buffers. As a result the performance of the
 * resulting buffers is good, however for very large buffers this will
 * use quote allot of the usable heap space. For very large buffers a
 * mapped region of shared memory of a file should be considered.
 *
 * @author Niall Gallagher
 */ 
public class ArrayAllocator implements Allocator {

   /**
    * This represents the largest portion of memory that is allowed.
    */         
   private int limit;

   /**
    * This represents the default capacity of all allocated buffers.
    */ 
   private int size;

   /**
    * Constructor for the <code>ArrayAllocator</code> object. This is
    * used to instantiate the allocator with a default buffer size of
    * half a kilobyte. This ensures that it can be used for general 
    * purpose byte storage and for minor I/O tasks.
    */ 
   public ArrayAllocator() {
      this(512);
   }

   /**
    * Constructor for the <code>ArrayAllocator</code> object. This is
    * used to instantiate the allocator with a specified buffer size.
    * This is typically used when a very specific buffer capacity is
    * required, for example a request body with a known length.
    *
    * @param size the initial capacity of the allocated buffers
    */ 
   public ArrayAllocator(int size) {
      this(size, 1048576);
   }

   /**
    * Constructor for the <code>ArrayAllocator</code> object. This is
    * used to instantiate the allocator with a specified buffer size.
    * This is typically used when a very specific buffer capacity is
    * required, for example a request body with a known length.
    *
    * @param size the initial capacity of the allocated buffers
    * @param limit this is the maximum buffer size created by this
    */
   public ArrayAllocator(int size, int limit) {      
      this.limit = Math.max(size, limit);
      this.size = size;
   }

   /**
    * This method is used to allocate a default buffer. This will 
    * allocate a buffer of predetermined size, allowing it to grow 
    * to an upper limit to accommodate extra data. If the buffer
    * requested is larger than the limit an exception is thrown.
    *
    * @return this returns an allocated buffer with a default size
    */      
   public Buffer allocate() throws IOException {
      return allocate(size);
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
   public Buffer allocate(long size) throws IOException {
      int required = (int)size;
      
      if(size > limit) {
         throw new BufferException("Specified size %s beyond limit", size);
      }
      return new ArrayBuffer(required, limit);
   }
}
