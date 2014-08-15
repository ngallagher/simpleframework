/*
 * PacketBuilder.java February 2007
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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The <code>PacketBuilder</code> object is used to accumulate octets
 * from provided byte buffers. This will create <code>Packet</code>
 * objects which encapsulate all of the information associated with
 * the byte buffer, and can tell the transport how the buffer should
 * be written. Packets can be either references or copies.
 *
 * @author Niall Gallagher
 */
class PacketBuilder {

   /**
    * This is packet allocator that is used to create packets.
    */
   private PacketAllocator allocator;
   
   /**
    * This is the packet within the packet builder being built.
    */
   private Packet packet;
   
   /**
    * This is the size of the packets that will be created.
    */
   private int size;
   
   /**
    * Constructor for the <code>PacketBuilder</code> object. This is
    * used to create a builder that can be used to aggregate multiple
    * byte buffers in to a single packet object. This will limit the
    * number of packets that can be created by this builder.
    */
   public PacketBuilder() {
      this(3);
   }
   
   /**
    * Constructor for the <code>PacketBuilder</code> object. This is
    * used to create a builder that can be used to aggregate multiple
    * byte buffers in to a single packet object. This will limit the
    * number of packets that can be created by this builder.
    * 
    * @param queue this is a limit to the number of packets built
    */
   public PacketBuilder(int queue) {
      this(queue, 4096);
   }
   
   /**
    * Constructor for the <code>PacketBuilder</code> object. This is
    * used to create a builder that can be used to aggregate multiple
    * byte buffers in to a single packet object. This will limit the
    * number of packets that can be created by this builder.
    * 
    * @param queue this is a limit to the number of packets built
    * @param size this is the size of the packets to be built
    */
   public PacketBuilder(int queue, int size) {
      this.allocator = new PacketAllocator(queue, size);
      this.size = size;
   }

   /**
    * This is used to acquire the current packet that has been built
    * within the builder. When the packet is returned from this
    * method another packet is allocated for the next build.
    * 
    * @return this returns the current packet within the builder
    */
   public Packet build() throws IOException {
      Packet local = null;
      
      if(packet != null) {
         int length = packet.length();
         
         if(length <= 0) {
            packet.close();
         } else {
            local = packet;
         }
         packet = null;
      }
      return local;
   }
   
   /**
    * This is used to build the a <code>Packet</code> within the
    * builder using the provided buffer. The returned packet will
    * contain the accumulated bytes from calls to the build method.
    * If the previous packets are not closed this method will 
    * block until such time as the packet is closed. 
    * 
    * @param buffer this is the buffer to be added to the packet
    * 
    * @return this returns the packet containing the bytes
    */
   public Packet build(ByteBuffer buffer) throws IOException {
      int ready = buffer.remaining();
      
      if(packet != null) {
         return build(buffer, packet);
      }
      if(ready > size) {
         return allocator.allocate(buffer);
      }
      if(ready > 0) {
         if(packet == null) {
            packet = allocator.allocate();
         }
         return build(buffer, packet);   
      }
      return null;
   }
   
   /**
    * This is used to build the a <code>Packet</code> within the
    * builder using the provided buffer. The returned packet will
    * contain the accumulated bytes from calls to the build method.
    * If the previous packets are not closed this method will 
    * block until such time as the packet is closed. 
    * 
    * @param buffer this is the buffer to be added to the packet
    * @param packet this is the packet to add the buffer to
    * 
    * @return this returns the packet containing the bytes
    */
   private Packet build(ByteBuffer buffer, Packet packet) throws IOException {
      int ready = buffer.remaining();
      int length = packet.length();
      int space = packet.space();
      
      if(ready <= space) { 
         packet.append(buffer);
      } else { 
        int capacity = buffer.capacity(); 

        if(length == 0) {
           return allocator.allocate(buffer);
        }
        if(space < capacity) { 
          if(space > 0) { 
             packet.append(buffer);
          } 
          return build();
        }
        return allocator.allocate(buffer);
      } 
      if(space == ready) {
         return build();
      }
      return null;
   }
}
