/*
 * PacketManager.java October 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The <code>PacketManager</code> object is used to create buffers
 * used to buffer output. Buffers are created lazily so that they
 * are allocated only on demand. Typically buffers are only created
 * when small chunks of data are written to the transport and the
 * socket is blocking. This ensures that writing can continue 
 * without waiting for the data to be fully drained. 
 * 
 * @author Niall Gallagher
 */
class PacketManager implements BufferRecycler { 
   
   /**
    * This is the queue that is used to recycle the buffers.
    */
   private BlockingQueue<ByteBuffer> queue;
   
   /**
    * Determines how many buffers can be lazily created.
    */
   private int allow;
   
   /**
    * Determines the size of the buffers that are created.
    */
   private int size;  
   
   /**
    * Constructor for the <code>PacketManager</code> object. This
    * requires the size of the buffers that will be allocated and
    * the number of buffers that can be lazily created before it
    * will block waiting for the next buffer to be returned.
    */
   public PacketManager() {
      this(3);
   }
   
   /**
    * Constructor for the <code>PacketManager</code> object. This
    * requires the size of the buffers that will be allocated and
    * the number of buffers that can be lazily created before it
    * will block waiting for the next buffer to be returned.
    * 
    * @param allow this is the size of the buffers to be allocated
    */
   public PacketManager(int allow) { 
      this(allow, 4096);
   }
   
   /**
    * Constructor for the <code>PacketManager</code> object. This
    * requires the size of the buffers that will be allocated and
    * the number of buffers that can be lazily created before it
    * will block waiting for the next buffer to be returned.
    * 
    * @param allow this is the number of buffers to be created
    * @param size this is the size of the buffers to be allocated
    */
   public PacketManager(int allow, int size) { 
      this.queue = new LinkedBlockingQueue<ByteBuffer>();
      this.allow = allow; 
      this.size = size; 
   } 
   
   /** 
    * This checks to see if there is a buffer ready within the queue. 
    * If there is one ready then this returns it, if not then this
    * checks how many buffers have been created. If we can create one
    * then return a newly instantiated buffer, otherwise block and 
    * wait for one to be recycled.
    * 
    * @return this returns the next ready buffer within the manager
    */ 
   public ByteBuffer allocate() throws PacketException { 
      ByteBuffer next = queue.poll(); 
     
      if(next != null) {
         return next;
      }
      return create(); 
   } 

   /** 
    * This checks to see if there is a buffer ready within the queue. 
    * If there is one ready then this returns it, if not then this
    * checks how many buffers have been created. If we can create one
    * then return a newly instantiated buffer, otherwise block and 
    * wait for one to be enqueued.
    * 
    * @return this returns the next ready buffer within the queue
    */ 
   private ByteBuffer create() throws PacketException {
      if(allow-- >= 0) {
         return build();
      }
      try {
         return queue.take();
      } catch(Exception e) {
         throw new PacketException("Thread interrupt", e);
      }
   }
   
   /**
    * This method is used to recycle the buffer. Invoking this with
    * a buffer instance will pass the buffer back in to the pool.
    * Once passed back in to the pool the buffer should no longer
    * be used as it may affect future uses of the buffer.
    *
    * @param buffer this is the buffer that is to be recycled
    */
   public void recycle(ByteBuffer buffer) {
      buffer.clear();
      queue.offer(buffer);
   }
   
   /**
    * This is used to allocate a buffer if there is no buffer ready
    * within the queue. The size of the buffer is determined from
    * the size specified when the buffer queue is created.
    * 
    * @return this returns a newly allocated byte buffer
    */
   private ByteBuffer build() { 
      try {
         return ByteBuffer.allocateDirect(size); 
      }catch(Throwable e) {
         return ByteBuffer.allocate(size);
      }
   } 
 } 


