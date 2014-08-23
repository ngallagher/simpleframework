/*
 * FixedLengthConsumer.java February 2007
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

package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.Buffer;

/**
 * The <code>FixedLengthConsumer</code> object reads a fixed number of 
 * bytes from a cursor. This is typically used when the Content-Length
 * header is used as the body delimiter. In order to determine when
 * the full body has been consumed this counts the bytes read. Once
 * all the bytes have been read any overflow will be reset. All of the
 * bytes read are appended to the internal buffer so they can be read.
 *
 * @author Niall Gallagher
 */ 
public class FixedLengthConsumer extends UpdateConsumer {
   
   /**
    * This is the allocator used to allocate the buffer used.
    */         
   private Allocator allocator;
   
   /**
    * This is the internal buffer used to accumulate the body.
    */ 
   private Buffer buffer;
   
   /**
    * This is the number of bytes to be consumed from the cursor.
    */ 
   private long limit;
   
   /**
    * Constructor for the <code>FixedLengthConsumer</code> object. This
    * is used to create a consumer that reads a fixed number of bytes
    * from a cursor and accumulates those bytes in an internal buffer 
    * so that it can be read at a later stage.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param limit this is the number of bytes that are to be read
    */ 
   public FixedLengthConsumer(Allocator allocator, long limit) {
      this.allocator = allocator;
      this.limit = limit;
   }
   
   
   /**
    * This is used to acquire the body that has been consumed. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Attachment</code> objects. 
    * Each part can then be read as an individual message.
    *  
    * @return the body that has been consumed by this instance
    */
   public Body getBody() {
      return new BufferBody(buffer);
   }
   
   /** 
    * This method is used to append the contents of the array to the
    * internal buffer. The appended bytes can be acquired from the
    * internal buffer using an <code>InputStream</code>, or the text
    * of the appended bytes can be acquired by encoding the bytes.   
    *
    * @param array this is the array of bytes to be appended
    * @param off this is the start offset in the array to read from
    * @param len this is the number of bytes to write to the buffer  
    */
   private void append(byte[] array, int off, int len) throws IOException {
      if(buffer == null) {
         buffer = allocator.allocate(limit);
      }
      buffer.append(array, off, len);
   }

   /**
    * This is used to process the bytes that have been read from the
    * cursor. This will count the number of bytes read, once all of 
    * the bytes that form the body have been read this returns the 
    * number of bytes that represent the overflow.
    *
    * @param array this is a chunk read from the cursor
    * @param off this is the offset within the array the chunk starts
    * @param count this is the number of bytes within the array
    *
    * @return this returns the number of bytes overflow that is read
    */       
   @Override
   protected int update(byte[] array, int off, int count) throws IOException {
      int mark = (int)limit;
      
      if(count >= limit) {
         append(array, off, mark);
         finished = true;
         limit = 0;
         return count - mark;
      } 
      if(count > 0) {
         append(array, off, count);
         limit -= count;         
      }
      return 0;
   }
}


