/*
 * Packet.java February 2008
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

/**
 * The <code>Packet</code> object is used to represent a collection
 * of bytes that can be written to a byte channel. The packet is used
 * to provide a uniform interface to byte sequences that need to be
 * transferred to the connected client. It ensures that regardless of
 * the backing memory store the transport can deal with the packets
 * transparently. In particular packets provide a means to ensure the
 * order requested is the order delivered. It uses sequence numbers
 * to ensure that the delivery is performed in an orderly manner.
 * <p>
 * When using a packet it is important to note that they must always
 * be closed with the <code>close</code> method when finished with.
 * This ensures any occupied resource is released. Resources such as
 * buffers can be placed back in to a pool and locks released when a
 * packet is closed. Failure to close can lead to a leak in resources.
 *
 * @author Niall Gallagher
 *
 * @param org.simpleframework.transport.Writer
 */ 
interface Packet extends Comparable<Packet> {

   /**        
    * This is used to determine how many bytes remain within this
    * packet. It represents the number of write ready bytes, so if
    * the length is greater than zero the packet can be written to
    * a byte channel. When length is zero the packet can be closed.
    * 
    * @return this is the number of bytes remaining in this packet
    */ 
   int length(); 

   /**
    * This represents the capacity of the backing store. The buffer
    * is full when length is equal to capacity and it can typically
    * be appended to when the length is less than the capacity. The
    * only exception is when <code>space</code> returns zero, which
    * means that the packet can not have bytes appended to it.
    *
    * @return this is the capacity of other backing byte storage
    */ 
   int capacity(); 

   /**
    * This is used to determine how much space is left to append 
    * data to this packet. This is typically equivelant to capacity
    * minus the length. However in the event that the packet uses 
    * a private memory store that can not be written to then this
    * can return zero regardless of the capacity and length.
    *
    * @return the space left within the buffer to append data to
    */         
   int space(); 

   /**
    * The sequence number represents the order with which this is
    * to be delivered to the underlying network. This allows safer
    * transfer of packets in an asynchronous environment where it may
    * be possible for a packet to be written out of sequence. The
    * sequence number also determines the order of closure.
    *
    * @return this returns an increasing packet sequence number
    */ 
   long sequence();

   /**
    * This method is used to determine if the buffer is a reference
    * to a byte buffer rather than a copy. It is important to know if
    * a packet is shared as it tells the writer whether it needs to
    * block the writing thread while the packet is pending a write
    * to the socket channel.
    * 
    * @return true if the packet is a reference to the byte buffer
    */
   boolean isReference();
   
   /**
    * This is used to that packets can be entered in to a priority
    * queue such that they are ordered based on their sequence
    * numbers. Ordering based on sequence numbers ensures that
    * packets can be removed and inserted back in to the queue 
    * without concern for the order of their insertion.
    * 
    * @param packet this is the packet that is to be compared
    * 
    * @return this is negative is less than otherwise its positive 
    */
   int compareTo(Packet packet);
   
   /**
    * This method is used to extract the contents of the packet in
    * to a duplicate packet. The purpose of this is to ensure that
    * when a packet wraps a shared buffer the contents of that
    * buffer can be drained in to an allocated buffer, resulting
    * in a packet that can be used without read write conflicts.
    *  
    * @return this returns the packets contents in a new buffer
    */
   Packet extract() throws IOException;
   
   /**
    * This is used to encode the underlying byte sequence to text.
    * Converting the byte sequence to text can be useful when either
    * debugging what exactly is being sent. Also, for transports 
    * that require string delivery of packets this can be used. 
    *
    * @return this returns the bytes sequence as a string object
    */  
   String encode() throws IOException; 

   /**
    * This is used to encode the underlying byte sequence to text.
    * Converting the byte sequence to text can be useful when either
    * debugging what exactly is being sent. Also, for transports 
    * that require string delivery of packets this can be used. 
    *
    * @param charset this is the character set to use for encoding
    *
    * @return this returns the bytes sequence as a string object
    */   
   String encode(String charset) throws IOException;

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
   int append(ByteBuffer buffer) throws IOException; 

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
   int append(ByteBuffer buffer, int count) throws IOException; 

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
   int write(ByteChannel channel) throws IOException; 

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
   int write(ByteChannel channel, int count) throws IOException; 

   /**
    * The <code>close</code> method for the packet is used to ensure
    * that any resources occupied by the packet are released. These
    * could be anything from internally pooled buffers to locks. If
    * the packet is not closed on completion then this can result in
    * a leak of resources within the associated transport.
    */ 
   void close() throws IOException;

   /**
    * Provides a string representation of the state of the packet. 
    * This can be useful for debugging the state transitions that a
    * packet will go through when being written and appended to.
    *
    * @return this returns a string representation for the packet
    */ 
   String toString(); 
 }
