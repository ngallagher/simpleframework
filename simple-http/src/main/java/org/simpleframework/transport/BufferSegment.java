/*
 * BufferSegment.java February 2008
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
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Queue;

/**
 * The <code>BufferSegment</code> object is used to provide a means 
 * to remove a packet from an segment builder when closed. This can 
 * be used to wrap an existing packet and add functionality to it.
 * Once closed this segment should no longer be used.
 * 
 * @author Niall Gallagher
 */
class BufferSegment implements Packet {
   
   /**
    * This is the packet that is being wrapped by this instance.
    */
   private final Packet packet;
   
   /**
    * This is the queue that the packet is to be removed from.
    */
   private final Queue queue;
   
   /**
    * Constructor for the <code>BufferSegment</code> object. This is 
    * used to create a packet that wraps another packet, and removes 
    * that packet from the head of a given queue when it is closed.
    *
    * @param packet this is the packet that is to be wrapped
    * @param queue this is the queue to remove the packet from
    */
   public BufferSegment(Packet packet, Queue queue) {
      this.packet = packet;
      this.queue = queue;
   }

   /**
    * This is used to determine how much space is left to append 
    * data to this packet. This is typically equivalent to capacity
    * minus the length. However in the event that the packet uses 
    * a private memory store that can not be written to then this
    * can return zero regardless of the capacity and length.
    *
    * @return the space left within the buffer to append data to
    */      
   public int space() {
      return packet.space();
   }

   /**
    * The sequence number represents the order with which this is
    * to be delivered to the underlying network. This allows safer
    * transfer of packets in an asynchronous environment where it may
    * be possible for a packet to be written out of sequence. The
    * sequence number also determines the order of closure.
    *
    * @return this returns an increasing packet sequence number
    */ 
   public long sequence() {
      return packet.sequence();
   }

   /**
    * This represents the capacity of the backing store. The buffer
    * is full when length is equal to capacity and it can typically
    * be appended to when the length is less than the capacity. The
    * only exception is when <code>space</code> returns zero, which
    * means that the packet can not have bytes appended to it.
    *
    * @return this is the capacity of other backing byte storage
    */  
   public int capacity() {
      return packet.capacity();
   }

   /**        
    * This is used to determine how many bytes remain within this
    * packet. It represents the number of write ready bytes, so if
    * the length is greater than zero the packet can be written to
    * a byte channel. When length is zero the packet can be closed.
    * 
    * @return this is the number of bytes remaining in this packet
    */     
   public int length() {
      return packet.length();
   }
   
   /**
    * This is used to that packets can be entered in to a priority
    * queue such that they are ordered based on their sequence
    * numbers. Ordering based on sequence numbers ensures that
    * packets can be remove and inserted back in to the equeue 
    * without concern for othe order of their insertion.
    * 
    * @param other this is the packet that is to be compared
    * 
    * @return this is negative is less than otherwise its positive 
    */
   public int compareTo(Packet other) {
      return packet.compareTo(other);
   }
   
   /**
    * This method is used to extract the contents of the packet in
    * to a duplicate packet. The purpose of this is to ensure that
    * when a packet wraps a shared buffer the contents of that
    * buffer can be drained in to an allocated buffer, resulting
    * in a packet that can be used without read write conflicts.
    *  
    * @return this returns the packets contents in a new buffer
    */
   public Packet extract() throws IOException {
      return packet.extract();
   }

   /**
    * This is used to encode the underlying byte sequence to text.
    * Converting the byte sequence to text can be useful when either
    * debugging what exactly is being sent. Also, for transports 
    * that require string delivery of packets this can be used. 
    *
    * @return this returns the bytes sequence as a string object
    */    
   public String encode() throws IOException {
      return packet.encode();
   }

   /**
    * This is used to encode the underlying byte sequence to text.
    * Converting the byte sequence to text can be useful when either
    * debugging what exactly is being sent. Also, for transports 
    * that require string delivery of packets this can be used. 
    *
    * @param encoding this is the character set to use for encoding
    *
    * @return this returns the bytes sequence as a string object
    */       
   public String encode(String encoding) throws IOException {
      return packet.encode(encoding);
   }

   /**
    * This will append bytes within the given buffer to the packet.
    * Once invoked the packet will contain the buffer bytes, which
    * will have been drained from the buffer. This effectively moves
    * the bytes in the buffer to the end of the packet instance.
    *
    * @param buffer this is the buffer containing the bytes
    *
    * @return returns the number of bytes that have been moved
    */       
   public int append(ByteBuffer buffer) throws IOException {
      return packet.append(buffer);
   }

   /**
    * This will append bytes within the given buffer to the packet.
    * Once invoked the packet will contain the buffer bytes, which
    * will have been drained from the buffer. This effectively moves
    * the bytes in the buffer to the end of the packet instance.
    *
    * @param buffer this is the buffer containing the bytes
    * @param count this is the number of bytes that should be used
    *
    * @return returns the number of bytes that have been moved
    */       
   public int append(ByteBuffer buffer, int count) throws IOException {      
      return packet.append(buffer, count);
   }
      
   /**
    * This write method will write the contents of the packet to the
    * provided byte channel. If the whole packet can be be written
    * then this will simply return the number of bytes that have. 
    * The number of bytes remaining within the packet after a write
    * can be acquired from the <code>length</code> method. Once all
    * of the bytes are written the packet must be closed.
    *
    * @param channel this is the channel to write the packet to
    *    
    * @return this returns the number of bytes that were written
    */    
   public int write(ByteChannel channel) throws IOException {
      return packet.write(channel);
   }

   /**
    * This write method will write the contents of the packet to the
    * provided byte channel. If the whole packet can be be written
    * then this will simply return the number of bytes that have. 
    * The number of bytes remaining within the packet after a write
    * can be acquired from the <code>length</code> method. Once all
    * of the bytes are written the packet must be closed.
    *
    * @param channel this is the channel to write the packet to
    * @param count the number of bytes to write to the channel
    *
    * @return this returns the number of bytes that were written
    */         
   public int write(ByteChannel channel, int count) throws IOException {
      return packet.write(channel, count);
   }
   
   /**
    * This method is used to determine if the buffer is shared with
    * another thread or service. It is important to know whether a
    * packet is shared as it tells the writer whether it needs to
    * block the writing thread while the packet is pending a write
    * to the socket channel.
    * 
    * @return true if the buffer is shared with another service
    */
   public boolean isReference() {
      return packet.isReference();
   }

   /**
    * The <code>close</code> method for the packet is used to ensure
    * that any resources occupied by the packet are released. The
    * resources held by this instance include pooled buffers. If the 
    * packet is not closed on completion then this can result in a 
    * leak of resources within the associated transport.
    */  
   public void close() throws IOException {
      Object top = queue.peek();

      if(top != packet) {
         throw new PacketException("Close out of sequence");
      }
      packet.close();
      queue.poll();
   } 

   /**
    * Provides a string representation of the state of the packet. 
    * This can be useful for debugging the state transitions that a
    * packet will go through when being written and appended to.
    *
    * @return this returns a string representation for the packet
    */ 
   @Override
   public String toString() {
      return packet.toString();           
   }
}
