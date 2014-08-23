/*
 * PartBodyConsumer.java February 2007
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
import org.simpleframework.transport.ByteCursor;

/**
 * The <code>PartBodyConsumer</code> object is used to consume a part
 * the contents of a multipart body. This will consume the part and
 * add it to a part list, once the part has been consumed and added
 * to the part list a terminal token is consumed, which is a carriage
 * return and line feed.
 *
 * @author Niall Gallagher
 */ 
class PartBodyConsumer implements BodyConsumer {

   /**
    * This is the token that is consumed after the content body. 
    */
   private static final byte[] LINE = { '\r', '\n' };
   
   /**
    * This is used to consume the content from the multipart upload.
    */  
   private ContentConsumer content;
   
   /**
    * This is used to consume the final terminal token from the part.
    */  
   private ByteConsumer token;
   
   /**
    * Constructor for the <code>PartBodyConsumer</code> object. This 
    * is used to create a consumer that reads the body of a part in
    * a multipart request body. The terminal token must be provided
    * so that the end of the part body can be determined.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param segment this represents the headers for the part body
    * @param boundary this is the message boundary for the body part
    */   
   public PartBodyConsumer(Allocator allocator, Segment segment, byte[] boundary) {
      this(allocator, segment, new PartData(), boundary);
   }
   
   /**
    * Constructor for the <code>PartBodyConsumer</code> object. This 
    * is used to create a consumer that reads the body of a part in
    * a multipart request body. The terminal token must be provided
    * so that the end of the part body can be determined.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param segment this represents the headers for the part body
    * @param series this is the part list that this body belongs in
    * @param boundary this is the message boundary for the body part
    */    
   public PartBodyConsumer(Allocator allocator, Segment segment, PartSeries series, byte[] boundary) {
      this.content = new ContentConsumer(allocator, segment, series, boundary);
      this.token = new TokenConsumer(allocator, LINE);
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
      return content.getBody();
   }
  
   /** 
    * This is used to consume the part body from the cursor. This
    * initially reads the body of the part, which represents the
    * actual payload exposed via the <code>Part</code> interface
    * once the payload has been consumed the terminal is consumed.
    *
    * @param cursor this is the cursor to consume the body from
    */ 
   public void consume(ByteCursor cursor) throws IOException {
      while(cursor.isReady()) {
         if(content.isFinished()) {
            if(token.isFinished()) {
               break;
            }
            token.consume(cursor);
         } else {
            content.consume(cursor);
         }
      }
   }
  
   /**
    * This is used to determine whether the part body has been read
    * from the cursor successfully. In order to determine if all of
    * the bytes have been read successfully this will check to see
    * of the terminal token had been consumed.
    *
    * @return true if the part body and terminal have been read 
    */ 
   public boolean isFinished() {
      return token.isFinished();
   }
}


