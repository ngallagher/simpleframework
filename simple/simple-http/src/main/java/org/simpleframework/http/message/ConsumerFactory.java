/*
 * ConsumerFactory.java February 2007
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

import static org.simpleframework.http.Protocol.BOUNDARY;
import static org.simpleframework.http.Protocol.CHUNKED;
import static org.simpleframework.http.Protocol.MULTIPART;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.http.ContentType;

/**
 * The <code>ConsumerFactory</code> object is used to create a factory
 * for creating consumers. This allows the request to determine the
 * type of content sent and allows consumption of the request body in
 * a the manner specified by the HTTP header. This will allow multipart
 * and chunked content to be consumed from the pipeline.
 * 
 * @author Niall Gallagher
 */
class ConsumerFactory {
   
   /**
    * This is used to allocate the memory associated with the body.
    */
   protected Allocator allocator;
   
   /**
    * This is the header associated with the request body consumed.
    */
   protected Segment segment;
   
   /**
    * Constructor for the <code>ConsumerFactory</code> object. This
    * will create a factory that makes use of the HTTP header in order
    * to determine the type of the body that is to be consumed.
    * 
    * @param allocator this is the allocator used to allocate memory
    * @param segment this is the HTTP header used to determine type
    */
   public ConsumerFactory(Allocator allocator, Segment segment) {
      this.allocator = allocator;
      this.segment = segment;
   }
   
   /**
    * This method is used to create a body consumer to read the body
    * from the pipeline. This will examine the HTTP header associated
    * with the body to determine how to consume the data. This will 
    * provide an empty consumer if no specific delimiter was provided.
    * 
    * @return this returns the consumer used to consume the body
    */
   public BodyConsumer getInstance() {
      long length = getContentLength();
      
      if(length < 0) { 
         return getInstance(8192);
      }
      return getInstance(length);
   }
   
   /**
    * This method is used to create a body consumer to read the body
    * from the pipeline. This will examine the HTTP header associated
    * with the body to determine how to consume the data. This will 
    * provide an empty consumer if no specific delimiter was provided.
    * 
    * @param length this is the length of the body to be consumed
    * 
    * @return this returns the consumer used to consume the body
    */   
   public BodyConsumer getInstance(long length) {      
      byte[] boundary = getBoundary(segment);
      
      if(isUpload(segment)) { 
         return new FileUploadConsumer(allocator, boundary, length);
      }
      if(isChunked(segment)) {
         return new ChunkedConsumer(allocator);
      }
      if(isFixed(segment)) {
         return new FixedLengthConsumer(allocator, length);
      }
      return new EmptyConsumer();
   }
   
   /**
    * This is used to extract information from the HTTP header that
    * can be used to determine the type of the body. This will look
    * at the HTTP headers provided to find a specific token which
    * enables it to determine how to consume the body. 
    * 
    * @param header this is the header associated with the body
    * 
    * @return the boundary for a multipart upload body
    */
   protected byte[] getBoundary(Segment header) {
      ContentType type = header.getContentType();
      
      if(type != null) {
         String token = type.getParameter(BOUNDARY);
         
         if(token != null) {
            return token.getBytes();
         }
      }
      return null;
   }
   
   /**
    * This is used to extract information from the HTTP header that
    * can be used to determine the type of the body. This will look
    * at the HTTP headers provided to find a specific token which
    * enables it to determine how to consume the body. 
    * 
    * @param segment this is the header associated with the body
    * 
    * @return true if the content type is that of a multipart body
    */   
   protected boolean isUpload(Segment segment) {
      ContentType type = segment.getContentType();      
      
      if(type != null) {
         String token = type.getPrimary();
      
         if(token.equals(MULTIPART)) {
            return true;
         }
      }
      return false;
   }
   
   /**
    * This is used to extract information from the HTTP header that
    * can be used to determine the type of the body. This will look
    * at the HTTP headers provided to find a specific token which
    * enables it to determine how to consume the body. 
    * 
    * @param segment this is the header associated with the body
    * 
    * @return true if the body is to be consumed as a chunked body
    */   
   protected boolean isChunked(Segment segment) {
      String encoding = segment.getTransferEncoding();
      
      if(encoding != null) {
         if(encoding.equals(CHUNKED)) {
            return true;
         }
      }
      return false;
   }

   /**
    * This is used to extract information from the HTTP header that
    * can be used to determine the type of the body. This will look
    * at the HTTP headers provided to find a specific token which
    * enables it to determine how to consume the body. 
    * 
    * @param segment this is the header associated with the body
    * 
    * @return true if there was a content length in the header
    */   
   protected boolean isFixed(Segment segment) {
      long length = segment.getContentLength();
      
      if(length > 0) {
         return true;
      }
      return false;
   }
   
   /**
    * This is a convenience method that can be used to determine
    * the length of the message body. This will determine if there
    * is a <code>Content-Length</code> header, if it does then the
    * length can be determined, if not then this returns -1.
    *
    * @return the content length, or -1 if it cannot be determined
    */
   protected long getContentLength() {
      return segment.getContentLength();
   }
}
