/*
 * FilterAllocator.java February 2001
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
 * The <code>FilterAllocator</code> object is used to provide a means 
 * to provide a general set of constraints around buffer allocation. 
 * It can ensure that a minimum capacity is used for default allocation
 * and that an upper limit is used for allocation. In general this can
 * be used in conjunction with another <code>Allocator</code> which may
 * not have such constraints. It ensures that a set of requirements can
 * be observed when allocating buffers.
 * 
 * @author Niall Gallagher
 */ 
public class FilterAllocator implements Allocator {

   /**
    * This is the allocator the underlying buffer is allocated with.
    */         
   protected Allocator source;    

   /**
    * This is the default initial minimum capacity of the buffer.
    */ 
   protected long capacity;

   /**
    * This is the maximum number of bytes that can be allocated.
    */ 
   protected long limit;

   /**
    * Constructor for the <code>FilterAllocator</code> object. This is
    * used to instantiate the allocator with a default buffer size of
    * half a kilobyte. This ensures that it can be used for general 
    * purpose byte storage and for minor I/O tasks.
    *
    * @param source this is where the underlying buffer is allocated
    */   
   public FilterAllocator(Allocator source) {
      this(source, 512, 1048576);
   }

   /**
    * Constructor for the <code>FilterAllocator</code> object. This is
    * used to instantiate the allocator with a specified buffer size.
    * This is typically used when a very specific buffer capacity is
    * required, for example a request body with a known length.
    *
    * @param source this is where the underlying buffer is allocated    
    * @param capacity the initial capacity of the allocated buffers
    */
   public FilterAllocator(Allocator source, long capacity) {
      this(source, capacity, 1048576);
   }

   /**
    * Constructor for the <code>FilterAllocator</code> object. This is
    * used to instantiate the allocator with a specified buffer size.
    * This is typically used when a very specific buffer capacity is
    * required, for example a request body with a known length.
    *
    * @param source this is where the underlying buffer is allocated    
    * @param capacity the initial capacity of the allocated buffers
    * @param limit this is the maximum buffer size created by this
    */   
   public FilterAllocator(Allocator source, long capacity, long limit) {
      this.limit = Math.max(capacity, limit);
      this.capacity = capacity;
      this.source = source;
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
   public Buffer allocate(long size) throws IOException {
      if(size > limit) {
         throw new BufferException("Specified size %s beyond limit", size);
      }           
      if(capacity > size) {
         size = capacity;
      }    
      return source.allocate(size);
   }
}