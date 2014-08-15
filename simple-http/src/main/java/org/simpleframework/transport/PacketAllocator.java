/*
 * PacketAllocator.java February 2008
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

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The <code>PacketAllocator</code> object is used to create packets 
 * that contain increasing sequence numbers. This acts as a pool for
 * buffers which can be recycled by closing the <code>Packet</code>
 * objects created by this allocator. As well as creating buffers
 * from a pool of byte buffers this can wrap an existing buffer
 * within a packet so it can be used uniformly.
 * 
 * @author Niall Gallagher
 */
class PacketAllocator {
   
   /**
    * This is the memory manager used to recycle the buffers.
    */
   private final PacketManager manager;
   
   /**
    * This is the counter used to generate the sequence numbers.
    */
   private final AtomicLong count;
   
   /**
    * Constructor for the <code>PacketAllocator</code> object. This 
    * is provided the size of the buffers that will be allocated and
    * the number of buffers that can be lazily created before it
    * will block waiting for the next buffer to be returned.
    */
   public PacketAllocator() {
      this(3);
   }
   /**
    * Constructor for the <code>PacketAllocator</code> object. This 
    * is provided the size of the buffers that will be allocated and
    * the number of buffers that can be lazily created before it
    * will block waiting for the next buffer to be returned.
    * 
    * @param allow this is the queue size for asynchronous writes
    */
   public PacketAllocator(int allow) {
      this(allow, 4096);
   }
   
   /**
    * Constructor for the <code>PacketAllocator</code> object. This 
    * is provided the size of the buffers that will be allocated and
    * the number of buffers that can be lazily created before it
    * will block waiting for the next buffer to be returned.
    * 
    * @param allow this is the queue size for asynchronous writes
    * @param size this is the size of the buffers to be allocated
    */
   public PacketAllocator(int allow, int size) { 
      this.manager = new PacketManager(allow, size);
      this.count = new AtomicLong();
   }

   /**
    * This creates a <code>Packet</code> from a buffer within the
    * pool of buffers. The buffer provided can be modified up until
    * such point as it is recycled. To recycle the buffer the packet
    * must be closed, when closed the buffer can be reused.
    * 
    * @return this returns a packet backed by a pooled buffer
    */
   public Packet allocate() throws PacketException {
      long sequence = count.getAndIncrement();
      ByteBuffer buffer = manager.allocate();
      
      return new BufferAdapter(buffer, manager, sequence);
   }

   /**
    * This creates a <code>Packet</code> by wrapping the provided
    * buffer within the packet interface. The buffer provided will
    * be read only such that the buffer it wraps is not modified.
    * 
    * @param buffer this is the buffer that has been wrapped
    * 
    * @return this returns a packet backed by a pooled buffer
    */
   public Packet allocate(ByteBuffer buffer) throws PacketException {
      long sequence = count.getAndIncrement();

      return new BufferWrapper(buffer, sequence);
   }
}
