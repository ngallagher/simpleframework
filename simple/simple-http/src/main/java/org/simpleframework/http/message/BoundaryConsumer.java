/*
 * BoundaryConsumer.java February 2007
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
 * The <code>BoundaryConsumer</code> is used to consume a boundary
 * for a multipart message. This ensures that the boundary complies
 * with the multipart specification in that it ends with a carriage
 * return and line feed. This consumer implementation can be used
 * multiple times as its internal buffer can be cleared and reset. 
 *
 * @author Niall Gallagher
 */ 
class BoundaryConsumer extends ArrayConsumer {
  
   /**
    * This is the terminal token for a multipart boundary entity.
    */           
   private static final byte[] LAST = {  '-', '-', '\r', '\n', };
   
   /**
    * This is the terminal token for a multipart boundary line.
    */ 
   private static final byte[] LINE = { '\r', '\n' };
   
   /**
    * This represents the start of the boundary line for the part.
    */
   private static final byte[] TOKEN = { '-', '-' };
   
   /**
    * This is used to allocate a buffer for for the boundary. 
    */
   private Allocator allocator;
   
   /**
    * This is used to consume the contents of the consumed buffer.
    */
   private Buffer buffer;
   
   /**
    * This is the actual boundary value that is to be consumed.
    */ 
   private byte[] boundary;
   
   /**
    * This counts the number of characters read from the start.
    */ 
   private int seek;
   
   /**
    * Constructor for the <code>BoundaryConsumer</code> object. This 
    * is used to create a boundary consumer for validating boundaries
    * and consuming them from a provided source. This is used to help
    * in reading multipart messages by removing boundaries from the
    * stream.
    *
    * @param boundary this is the boundary value to be consumed
    */ 
   public BoundaryConsumer(Allocator allocator, byte[] boundary) {
      this.chunk = boundary.length + LAST.length + TOKEN.length;
      this.allocator = allocator;
      this.boundary = boundary;    
   }   
   
   /**
    * This does not perform any processing after the boundary has 
    * been consumed. Because the boundary consumer is used only as a
    * means to remove the boundary from the underlying stream there
    * is no need to perform any processing of the value consumed.
    */ 
   @Override
   protected void process() throws IOException {
      if(count < boundary.length + 4) {
         throw new IOException("Invalid boundary processed");
      }
   }
   
   /**
    * This method is used to scan for the terminal token. It searches
    * for the token and returns the number of bytes in the buffer 
    * after the terminal token. Returning the excess bytes allows the
    * consumer to reset the bytes within the consumer object.
    *
    * @return this returns the number of excess bytes consumed
    */
   @Override
   protected int scan() throws IOException {
      int size = boundary.length;

      if(count >= 2 && seek < 2) {
         if(scan(TOKEN)) {
            append(TOKEN);
         }
      }
      if(count >= 2 + size && seek < 2 + size) {
         if(scan(boundary)) {
            append(boundary);
         }
      }
      if(count >= 4 + size && seek < 4 + size) {
         if(array[size + 2] == TOKEN[0]) {           
            if(scan(TOKEN)) {
               append(TOKEN);
            } 
         } else if(array[size + 2] == LINE[0]) {
            if(scan(LINE)) {
               append(LINE);
            }
            done = true;
            return count - seek;
         }
      } 
      if(count >= 6 + size && seek < 6 + size) {
         if(scan(LINE)) {
            append(LINE);
         }
         done = true;
         return count - seek;
      }
      return 0;
   }
   
   /**
    * This is used to append a token to the underlying buffer. Adding
    * various tokens ensures that the whole message is reconstructed
    * and can be forwarded to any connected service if used as a proxy.
    * 
    * @param token this is the token that is to be appended
    */
   private void append(byte[] token) throws IOException {
      if(buffer == null) {
         buffer = allocator.allocate(chunk);
      }
      buffer.append(token);
   }
   
   /**
    * This is used to scan the specified token from the consumed bytes.
    * If the data scanned does not match the token provided then this
    * will throw an exception to signify a bad boundary. This will
    * return true only when the whole boundary has been consumed.
    * 
    * @param data this is the token to scan from the consumed bytes
    * 
    * @return this returns true of the token has been read
    */
   private boolean scan(byte[] data) throws IOException {
      int size = data.length;
      int pos = 0;
      
      while(seek < count) {
         if(array[seek++] != data[pos++]) {   
            throw new IOException("Invalid boundary");
         }
         if(pos == data.length) {
            return true;
         }
      }
      return pos == size;
   }
   
   /**
    * This is used to determine whether the boundary has been read
    * from the underlying stream. This is true only when the very
    * last boundary has been read. This will be the boundary value
    * that ends with the two <code>-</code> characters.
    *
    * @return this returns true with the terminal boundary is read
    */ 
   public boolean isEnd() {  
      return seek == chunk;
   }
  
   /**
    * This is used to clear the state of the of boundary consumer
    * such that it can be reused. This is required as the multipart
    * body may contain many parts, all delimited with the same 
    * boundary. Clearing allows the next boundary to be consumed.
    */  
   public void clear() {
      done = false;
      count = seek = 0;
   }
}