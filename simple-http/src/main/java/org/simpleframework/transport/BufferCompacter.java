/*
 * BufferCompacter.java February 2008
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

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * The <code>BufferCompacter</code> object is used to build segments
 * such that they represent segments of data from the the packets that
 * are provided to the builder. This enables packets to be compacted
 * into each other so that packets can be freed when they are no
 * longer needed. This enables the transport as a whole to perform
 * better as it ensures the packet pool is not exhausted when there
 * is sufficient space in other queued packets. Also this will copy
 * shared packets in to allocated space if requested, ensuring that
 * the writing thread does not need to block.
 * 
 * @author Niall Gallagher
 *
 * @see org.simpleframework.transport.PacketWriter
 */
class BufferCompacter {
   
   /**
    * This is a compact queue which contains the compact packets.
    */
   private final Queue<Packet> compact;

   /**
    * This is the packet queue that is used to queue packets.
    */
   private final Queue<Packet> ready;
   
   /**
    * This is the maximum size a packet can be duplicated as.
    */
   private final int limit;
   
   /**
    * Constructor for the <code>BufferCompacter</code> object. This 
    * is used to create a queue of packets such that each packet is
    * of a minimum size. To ensure packets are of a minimum size
    * this aggregates them by moving bytes between packets.
    */
   public BufferCompacter() {
      this(20480);
   }
   
   /**
    * Constructor for the <code>BufferCompacter</code> object. This 
    * is used to create a queue of packets such that each packet is
    * of a minimum size. To ensure packets are of a minimum size
    * this aggregates them by moving bytes between packets.
    *
    * @param limit this is the threshold for asynchronous buffers
    */   
   public BufferCompacter(int limit) {
      this.compact = new PriorityQueue<Packet>();
      this.ready = new PriorityQueue<Packet>();
      this.limit = limit;
   }
   
   /**
    * This is used to determine if the builder contains any references.
    * If the segment builder contains any reference packets it forces 
    * the <code>PacketWriter</code> to block. Blocking is required so
    * that a race condition is avoided where the writing thread and
    * the flushing thread to not confuse each other.
    * 
    * @return true if there are any referenced buffers in the builder
    */
   public boolean isReference() {
      for(Packet packet : ready) {
         if(packet.isReference()) {
            return true;
         }
      }
      return false;
   }
   
   /**
    * This will aggregate the queued packets in to a packet that is
    * at least the minimum required size. If there are none in the
    * queue then this will return null. Also if the queued packets
    * are of zero length this will return null.
    * 
    * @return this returns a packet from the queue of packets
    */
   public BufferSegment build() throws IOException {
      Packet packet = ready.peek();
      
      if(packet == null) {
         return null;
      }
      return create(packet);
   }

   /**
    * This will aggregate the queued packets in to a packet that is
    * at least the minimum required size. If there are none in the
    * queue then this will return null. Also if the queued packets
    * are of zero length this will return null.
    * 
    * @param packet this is the packet to wrap within a closer packet
    * 
    * @return this returns a packet from the queue of packets
    */
   private BufferSegment create(Packet packet) throws IOException {
      int length = packet.length();
      
      if(length <= 0) {
         packet.close(); 
         ready.poll();
      
         return build(); 
      }
      return new BufferSegment(packet, ready);
   }
   
   /**
    * This will aggregate the queued packets in to a packet that is
    * at least the minimum required size. If there are none in the
    * queue then this will return null. Also if the queued packets
    * are of zero length this will return null.
    * 
    * @param packet this is a new packet to be added to the packet queue
    * 
    * @return this returns a packet from the queue of packets
    */
   public BufferSegment build(Packet packet) throws IOException {
      boolean update = ready.offer(packet);
      long sequence = packet.sequence();
      
      if(!update) {
         throw new PacketException("Could not add packet " + sequence);
      }      
      return build();
   }
   
   /**
    * This method is used to compact the packets within the builder
    * such that it duplicates any shared packets and closes them.
    * Duplicating and closing shared packets is done so that the
    * writing thread does not need to block. Duplication of shared
    * packets only occurs if the remaining length is less that 
    * than the maximum duplication size specified.
    */
   public void compact() throws IOException {
      Packet packet = ready.peek();
      
      while(packet != null) {
         packet = ready.poll();
         
         if(packet != null) {
            compact.offer(packet);
         }
      }
      extract();  
   }
   
   /**
    * This is used to take all packets queued in to the compact queue
    * and determine whether they need to be extracted in to separate
    * private packets. Extracting shared packets ensures that they 
    * do not suffer from race conditions when the writing thread is
    * released. This increases the concurrency capability.
    */
   private void extract() throws IOException {
      int count = limit;
      
      for(Packet packet : compact) {
         int length = packet.length();
         
         if(length <= count) { 
            packet = packet.extract();
            count -= length;
         }
         if(packet != null) {
            ready.offer(packet);
         }
      }
      compact.clear();
   }
   
   /**
    * This returns the total length of all packets within the queue.
    * This can be used to determine if any packets can be created
    * using the <code>compact</code> method. If the length is zero
    * there are no packets waiting to be aggregated.
    * 
    * @return this returns the total length of all queued packets
    */
   public int length() throws IOException{
      int count = 0;
      
      for(Packet packet : ready) {
         count += packet.length();
      }
      return count;
   }
   
   /**
    * This is used to close all packets within the builder. This
    * is done when there is an error or the client has closed the
    * connection from their size. This is important as it releases
    * any resources occupied by the queued packets.
    */
   public void close() throws IOException {
      for(Packet packet : ready) {
         packet.close();
      }
      ready.clear();
   }
}
