/*
 * FrameConsumer.java February 2014
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

import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.transport.ByteCursor;

/**
 * The <code>FrameConsumer</code> object is used to read a WebSocket
 * frame as defined by RFC 6455. This is a state machine that can read
 * the data one byte at a time until the entire frame has been consumed.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.FrameCollector
 */
class FrameConsumer {
   
   /**
    * This is used to consume the header part of the frame.
    */
   private FrameHeaderConsumer header;
   
   /**
    * This is used to interpret the header and create a frame.
    */
   private FrameBuilder builder;
   
   /**
    * This is used to buffer the bytes that form the frame.
    */
   private byte[] buffer;
   
   /**
    * This is a count of the payload bytes currently consumed.
    */
   private int count;

   /**
    * Constructor for the <code>FrameConsumer</code> object. This is
    * used to create a consumer to read the bytes that form the frame 
    * from an underlying TCP connection. Internally a buffer is created
    * to allow bytes to be consumed and collected in chunks.
    */
   public FrameConsumer() {
      this.header = new FrameHeaderConsumer();
      this.builder = new FrameBuilder(header);
      this.buffer = new byte[2048];
   }
   
   /**
    * This is used to determine the type of frame. Interpretation of
    * this type is outlined in RFC 6455 and can be loosely categorised
    * as control frames and either data or binary frames.     
    * 
    * @return this returns the type of frame that this represents
    */
   public FrameType getType() {
      return header.getType();
   }

   /**
    * This is used to create a frame object to represent the data that
    * has been consumed. The frame created will make a copy of the 
    * internal byte buffer so this method should be used sparingly.
    * 
    * @return this returns a frame created from the consumed bytes
    */
   public Frame getFrame() {    
      return builder.create(buffer, count);
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
      while (cursor.isReady()) {
         if(!header.isFinished()) {
            header.consume(cursor);
         }
         if(header.isFinished()) {            
            int length = header.getLength();
            
            if(count <= length) {            
               if(buffer.length < length) {
                  buffer = new byte[length];
               }
               if(count < length) {
                  int size = cursor.read(buffer, count, length - count);
                  
                  if(size == -1) {
                     throw new IOException("Could only read " + count + " of length " + length);
                  }
                  count += size;
               }
               if(count == length) {
                  if(header.isMasked()) {
                     byte[] mask = header.getMask();
                     
                     for (int i = 0; i < count; i++) {               
                        buffer[i] ^= mask[i % 4];
                     }                     
                  }   
                  break;
               }
            }
         }
      }
   }

   /**
    * This is used to determine if the collector has finished. If it
    * is not finished the collector will be registered to listen for
    * an I/O interrupt to read further bytes of the frame.
    * 
    * @return true if the collector has finished consuming
    */
   public boolean isFinished() {
      if(header.isFinished()) {
         int length = header.getLength();
         
         if(count == length) {
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
      header.clear();
      count = 0;
   }
}
