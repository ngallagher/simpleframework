/*
 * PartHeaderConsumer.java February 2007
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

package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.Buffer;

/**
 * The <code>PartHeaderConsumer</code> object is used to consume the
 * header for a multipart message. This performs a parse of the 
 * HTTP headers within the message up to the terminal carriage return
 * and line feed token. Once this had been read the contents of the
 * header are appended to a buffer so they can be read later.
 *
 * @author Niall Gallagher 
 */ 
class PartHeaderConsumer extends SegmentConsumer {
   
   /**
    * This is used to allocate the internal buffer for the header.
    */
   private Allocator allocator;
   
   /**
    * This is the internal buffer used to store the header.
    */ 
   private Buffer buffer;
   
   /**
    * Constructor for the <code>PartHeaderConsumer</code> object. An
    * allocator is required so that the header consumer can create a
    * buffer to store the contents of the consumed message. 
    *
    * @param allocator this is the allocator used to create a buffer
    */ 
   public PartHeaderConsumer(Allocator allocator) {
      this.allocator = allocator;
   }
   
   /**
    * This is used to process the header consumer once all of the 
    * headers have been read. This will simply parse all of the
    * headers and append the consumed bytes to the internal buffer.
    * Appending the bytes ensures that the whole upload can be
    * put back together as a single byte stream if required.
    */ 
   @Override
   protected void process() throws IOException {
      headers();
      append();
   }

   /**
    * This is used to allocate the internal buffer and append the
    * consumed bytes to the buffer. Once the  header is added to
    * the internal buffer this is finished and the next part of
    * the upload can be consumed.
    */ 
   private void append() throws IOException {      
      if(buffer == null) {
         buffer = allocator.allocate(count);
      }
      buffer.append(array, 0, count);
   }
}


