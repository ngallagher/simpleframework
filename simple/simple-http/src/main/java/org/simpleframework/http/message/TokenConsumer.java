/*
 * TokenConsumer.java February 2007
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
 * The <code>TokenConsumer</code> object is used to consume a token
 * from the cursor. Once the token has been consumed the consumer
 * is finished and the contents of the consumed token is appended
 * to an allocated buffer so that it can be extracted.
 * 
 * @author Niall Gallagher
 */
class TokenConsumer extends ArrayConsumer {
   
   /**
    * This is used to allocate a buffer to append the contents.
    */
   private Allocator allocator;
   
   /**
    * This is used to append the contents of consumed token.
    */
   private Buffer buffer;
   
   /**
    * This is the token that is to be consumed from the cursor.
    */
   private byte[] token;
   
   /**
    * This tracks the number of bytes that are read from the token.
    */
   private int seek;
   
   /**
    * This is the length of the token that is to be consumed.
    */
   private int length;
   
   /**
    * The <code>TokenConsumer</code> object is used to read a token
    * from the cursor. This tracks the bytes read from the cursor, 
    * when it has fully read the token bytes correctly it will 
    * finish and append the consumed bytes to a buffer.
    * 
    * @param allocator the allocator used to create a buffer
    * @param token this is the token that is to be consumed
    */
   public TokenConsumer(Allocator allocator, byte[] token) {
      this.allocator = allocator;
      this.length = token.length;
      this.token = token;
      this.chunk = length;
   }
   
   /**
    * This is used to append the consumed bytes to a created buffer
    * so that it can be used when he is finished. This allows the
    * contents to be read from an input stream or as a string.
    */
   @Override
   protected void process() throws IOException {
      if(buffer == null) {
         buffer = allocator.allocate(length);
      }
      buffer.append(token);
   }
   
   /**
    * This is used to scan the token from the array. Once the bytes
    * have been read from the consumed bytes this will return the
    * number of bytes that need to be reset within the buffer. 
    *
    * @return this returns the number of bytes to be reset
    */
   @Override
   protected int scan() throws IOException {
      int size = token.length;
      int pos = 0;
      
      if(count >= size) {
         while(seek < count) {
            if(array[seek++] != token[pos++]) {
               throw new IOException("Invalid token");
            }
         }
         done = true;
         return count - seek;
      }
      return 0;
   }
}
