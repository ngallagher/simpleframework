/*
 * ContentConsumer.java February 2007
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
import org.simpleframework.http.Part;
import org.simpleframework.transport.ByteCursor;

/**
 * The <code>ContentConsumer</code> object represents a consumer for
 * a multipart body part. This will read the contents of the cursor
 * until such time as it reads the terminal boundary token, which is
 * used to frame the content. Once the boundary token has been read
 * this will add itself as a part to a part list. This part list can
 * then be used with the HTTP request to examine and use the part. 
 * 
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.message.PartConsumer
 */
class ContentConsumer extends UpdateConsumer {
    
   /**
    * This represents the start of the boundary token for the body.
    */      
   private static final byte[] START = { '\r', '\n', '-', '-' };  
   
   /**
    * This is the part list that this part is to be added to.
    */ 
   private PartSeries series;
   
   /**
    * This is used to allocate the internal buffer when required.
    */ 
   private Allocator allocator;
   
   /**
    * Represents the HTTP headers that were provided for the part.
    */ 
   private Segment segment;      
   
   /**
    * This is the internal buffer used to house the part body.
    */ 
   private Buffer buffer;
   
   /**
    * Represents the message boundary that terminates the part body.
    */ 
   private byte[] boundary;
   
   /**
    * This is used to determine if the start token had been read.
    */
   private int start;
   
   /**
    * This is used to determine how many boundary tokens are read.
    */ 
   private int seek;
  
   /**
    * Constructor for the <code>ContentConsumer</code> object. This 
    * is used to create a consumer that reads the body of a part in
    * a multipart request body. The terminal token must be provided
    * so that the end of the part body can be determined.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param segment this represents the headers for the part body
    * @param series this is the part list that this body belongs in
    * @param boundary this is the message boundary for the body part
    */  
   public ContentConsumer(Allocator allocator, Segment segment, PartSeries series, byte[] boundary) {
      this.allocator = allocator;
      this.boundary = boundary; 
      this.segment = segment;      
      this.series = series;
   }
   
   /**
    * This is used to acquire the body for this HTTP entity. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Part</code> objects. Each
    * part can then be read as an individual message.
    *  
    * @return the body provided by the HTTP request message
    */
   public Body getBody() {
      return new BufferBody(buffer);
   }

   /**
    * This is used to acquire the part for this HTTP entity. This
    * will return a part which can be used to read the content of
    * the message, the part created contains the contents of the
    * body and the headers associated with it.
    *  
    * @return the part provided by the HTTP request message
    */
   public Part getPart() {
      return new BufferPart(segment, buffer);
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
         buffer = allocator.allocate();
      }
      buffer.append(array, off, len);
   }
   
   /**
    * This is used to push the start and boundary back on to the
    * cursor. Pushing the boundary back on to the cursor is required
    * to ensure that the next consumer will have valid data to 
    * read from it. Simply resetting the boundary is not enough as
    * this can cause an infinite loop if the connection is bad.
    * 
    * @param cursor this is the cursor used by this consumer
    */
   @Override
   protected void commit(ByteCursor cursor) throws IOException {
      cursor.push(boundary);
      cursor.push(START);
   }
   
   /**
    * This is used to process the bytes that have been read from the
    * cursor. This will search for the boundary token within the body
    * of the message part, when it is found this will returns the 
    * number of bytes that represent the overflow.
    *
    * @param array this is a chunk read from the cursor
    * @param off this is the offset within the array the chunk starts
    * @param size this is the number of bytes within the array
    *
    * @return this returns the number of bytes overflow that is read
    */        
   @Override
   protected int update(byte[] array, int off, int size) throws IOException {
      int skip = start + seek; // did we skip previously
      int last = off + size;
      int next = start;
      int mark = off;      
      
      while(off < last) {
         if(start == START.length) { // search for boundary      
            if(array[off++] != boundary[seek++]) { // boundary not found
               if(skip > 0) {
                  append(START, 0, next); // write skipped start
                  append(boundary, 0, skip - next); // write skipped boundary
               }
               skip = start = seek = 0; // reset scan position
            }         
            if(seek == boundary.length) { // boundary found
               int excess = seek + start; // boundary bytes read
               int total = off - mark; // total bytes read
               int valid = total - excess; // body bytes read
               
               finished = true;

               if(valid > 0) {
                  append(array, mark, valid);
               }
               Part part = getPart();
               
               if(part != null) {
                  series.addPart(part);  
               }
               return size - total; // remaining excluding boundary
            }
         } else {
            byte octet = array[off++]; // current
            
            if(octet != START[start++]) {               
               if(skip > 0) {
                  append(START, 0, next); // write skipped start
               }   
               skip = start = 0; // reset
               
               if(octet == START[0]) { // is previous byte the start
                  start++;
               }               
            }
         }
      }      
      int excess = seek + start; // boundary bytes read
      int total = off - mark; // total bytes read
      int valid = total - excess; // body bytes read
      
      if(valid > 0) { // can we append processed data
         append(array, mark, valid);
      }
      return 0;
   } 
}
