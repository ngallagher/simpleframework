/*
 * PartSeriesConsumer.java February 2007
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
import org.simpleframework.common.buffer.BufferAllocator;
import org.simpleframework.transport.ByteCursor;

/**
 * The <code>PartSeriesConsumer</code> object is used to consume a list
 * of parts encoded in the multipart format. This is can consume any
 * number of parts from a cursor. Each part consumed is added to an
 * internal part list which can be used to acquire the contents of the
 * upload and inspect the headers provided for each uploaded part. To
 * ensure that only a fixed number of bytes are consumed this uses a
 * content length for an internal buffer.
 * 
 * @author Niall Gallagher
 */
class PartSeriesConsumer implements BodyConsumer {

   /**
    * This is used to consume individual parts from the part list.
    */ 
   private PartEntryConsumer consumer;

   /**
    * This is the factory that is used to create the consumers used.
    */
   private PartEntryFactory factory;
   
   /**
    * This is used to both allocate and buffer the part list body.
    */ 
   private BufferAllocator buffer;

   /**
    * This is used to accumulate all the parts of the upload.
    */ 
   private PartSeries series;
   
   /**
    * Constructor for the <code>PartSeriesConsumer</code> object. This 
    * will create a consumer that is capable of breaking an upload in
    * to individual parts so that they can be accessed and used by
    * the receiver of the HTTP request message.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param boundary this is the boundary used for the upload
    */ 
   public PartSeriesConsumer(Allocator allocator, byte[] boundary) {
      this(allocator, boundary, 8192);
   }

   /**
    * Constructor for the <code>PartSeriesConsumer</code> object. This 
    * will create a consumer that is capable of breaking an upload in
    * to individual parts so that they can be accessed and used by
    * the receiver of the HTTP request message.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param boundary this is the boundary used for the upload
    * @param length this is the number of bytes the upload should be
    */    
   public PartSeriesConsumer(Allocator allocator, byte[] boundary, long length) {
      this(allocator, new PartData(), boundary, length);
   }   

   /**
    * Constructor for the <code>PartSeriesConsumer</code> object. This 
    * will create a consumer that is capable of breaking an upload in
    * to individual parts so that they can be accessed and used by
    * the receiver of the HTTP request message.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param boundary this is the boundary used for the upload
    * @param series this is the part list used to accumulate the parts
    */    
   public PartSeriesConsumer(Allocator allocator, PartSeries series, byte[] boundary) {
      this(allocator, series, boundary, 8192);
   }

   /**
    * Constructor for the <code>PartSeriesConsumer</code> object. This 
    * will create a consumer that is capable of breaking an upload in
    * to individual parts so that they can be accessed and used by
    * the receiver of the HTTP request message.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param series this is the part list used to accumulate the parts   
    * @param boundary this is the boundary used for the upload
    * @param length this is the number of bytes the upload should be   
    */    
   public PartSeriesConsumer(Allocator allocator, PartSeries series, byte[] boundary, long length) {
      this.buffer = new BufferAllocator(allocator, length);
      this.consumer = new PartEntryConsumer(buffer, series, boundary, length);
      this.factory = new PartEntryFactory(buffer, series, boundary, length);
      this.series = series;
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
      return new BufferBody(buffer, series);
   }

   /** 
    * This is used to consume the part list from the cursor. This
    * initially reads the list of parts, which represents the
    * actual content exposed via the <code>PartSeries</code> object,
    * once the content has been consumed the terminal is consumed.
    *
    * @param cursor this is the cursor to consume the list from
    */ 
   public void consume(ByteCursor cursor) throws IOException {
      while(cursor.isReady()) { 
         if(!consumer.isFinished()) {
            consumer.consume(cursor);
         } else {
            if(!consumer.isEnd()) {
               consumer = factory.getInstance();
            } else {
               break;
            }
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
      return consumer.isEnd();
   }   
}
