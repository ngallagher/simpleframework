/*
 * FrameHeaderConsumer.java February 2014
 *
 * Copyright (C) 2014, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.socket.service;

import java.io.IOException;

import org.simpleframework.http.socket.FrameType;
import org.simpleframework.transport.ByteCursor;

/**
 * The <code>FrameHeaderConsumer</code> is used to consume frames from
 * a connected TCP channel. This is a state machine that can consume 
 * the data one byte at a time until the entire header has been consumed.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.FrameConsumer
 */
class FrameHeaderConsumer implements FrameHeader {

   /**
    * This is the frame type which represents the opcode.
    */
   private FrameType type;
   
   /**
    * If header consumed was from a client frame the data is masked.
    */
   private boolean masked;
   
   /**
    * Determines if this frame is part of a larger sequence.
    */
   private boolean last;
   
   /**
    * This is the mask that is used to obfuscate client frames.
    */
   private byte[] mask;
   
   /**
    * This is the octet that is used to read one byte  at a time.
    */
   private byte[] octet;
   
   /**
    * Required number of bytes within the frame header.
    */
   private int required;
   
   /**
    * This represents the length of the frame payload.
    */
   private int length;
   
   /**
    * This determines the count of the mask bytes read.
    */
   private int count;

   /**
    * Constructor for the <code>FrameHeaderConsumer</code> object. This
    * is used to create a consumer to read the bytes that form the
    * frame header from an underlying TCP connection.
    */
   public FrameHeaderConsumer() {
      this.octet = new byte[1];
      this.mask = new byte[4];
      this.length = -1;
   }
   
   /**
    * This provides the length of the payload within the frame. It 
    * is used to determine how much data to consume from the underlying
    * TCP stream in order to recreate the frame to dispatch.     
    * 
    * @return the number of bytes used in the frame
    */   
   public int getLength() {
      return length;
   }
   
   /**
    * This provides the client mask send with the request. The mask is 
    * a 32 bit value that is used as an XOR bitmask of the client
    * payload. Masking applies only in the client to server direction. 
    * 
    * @return this returns the 32 bit mask used for this frame
    */   
   public byte[] getMask() {
      return mask;
   }
   
   /**
    * This is used to determine the type of frame. Interpretation of
    * this type is outlined in RFC 6455 and can be loosely categorised
    * as control frames and either data or binary frames.     
    * 
    * @return this returns the type of frame that this represents
    */   
   public FrameType getType() {
      return type;
   }   
   
   /**
    * This is used to determine if the frame is masked. All client 
    * frames should be masked according to RFC 6455. If masked the 
    * payload will have its contents bitmasked with a 32 bit value.
    * 
    * @return this returns true if the payload has been masked
    */   
   public boolean isMasked() {
      return masked;
   }
   
   /**
    * This is used to determine if the frame is the final frame in
    * a sequence of fragments or a whole frame. If this returns false
    * then the frame is a continuation from from a sequence of 
    * fragments, otherwise it is a whole frame or the last fragment.
    * 
    * @return this returns false if the frame is a fragment
    */   
   public boolean isFinal() {
      return last;
   }

   /**
    * This consumes frame bytes using the provided cursor. The consumer
    * acts as a state machine by consuming the data as that data 
    * becomes available, this allows it to consume data asynchronously
    * and dispatch once the whole frame has been consumed.
    * 
    * @param cursor the cursor to consume the frame data from
    */
   public void consume(ByteCursor cursor) throws IOException {
      if (cursor.isReady()) {
         if (type == null) {
            int count = cursor.read(octet);

            if (count <= 0) {
               throw new IOException("Ready cursor produced no data");
            }            
            type = FrameType.resolveType(octet[0] & 0x0f);
                   
            if(type == null) {
               throw new IOException("Frame type code not supported");
            }
            last = (octet[0] & 0x80) != 0;
         } else {
            if (length < 0) {
               int count = cursor.read(octet);

               if (count <= 0) {
                  throw new IOException("Ready cursor produced no data");
               }
               masked = (octet[0] & 0x80) != 0;
               length = (octet[0] & 0x7F);

               if (length == 0x7F) { // 8 byte extended payload length
                  required = 8;
                  length = 0;
               } else if (length == 0x7E) { // 2 bytes extended payload length
                  required = 2;
                  length = 0;
               }
            } else if (required > 0) {
               int count = cursor.read(octet);

               if (count == -1) {
                  throw new IOException("Could not read length");
               }
               length |= (octet[0] & 0xFF) << (8 * --required);               
            } else {
               if (masked && count < mask.length) {
                  int size = cursor.read(mask, count, mask.length - count);

                  if (size == -1) {
                     throw new IOException("Could not read mask");
                  }
                  count += size;
               }
            }
         }
      }
   }
   
   /**
    * This is used to determine if the collector has finished. If it
    * is not finished the collector will be registered to listen for
    * an I/O intrrupt to read further bytes of the frame.
    * 
    * @return true if the collector has finished consuming
    */
   public boolean isFinished() {
      if(type != null) {
         if(length >= 0 && required == 0) {
            if(masked) {
               return count == mask.length;
            }
            return true;
         }
      }
      return false;
   }

   /**
    * This resets the collector to its original state so that it can 
    * be reused. Reusing the collector has obvious benefits as it will
    * reduce the amount of memory churn for the server.  
    */
   public void clear() {
      type = null;
      length = -1;
      required = 0;
      masked = false;
      count = 0;
   }
}
