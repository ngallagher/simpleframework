/*
 * EntityConsumer.java February 2007
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

import static org.simpleframework.http.core.ContainerEvent.BODY_FINISHED;
import static org.simpleframework.http.core.ContainerEvent.HEADER_FINISHED;
import static org.simpleframework.http.core.ContainerEvent.READ_BODY;
import static org.simpleframework.http.core.ContainerEvent.READ_HEADER;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>EntityConsumer</code> object is used to consume data
 * from a cursor and build a request entity. Each constituent part of
 * the entity is consumed from the pipeline and can be acquired from
 * this consumer object. The <code>Header</code> and <code>Body</code>
 * can be used to extract the individual parts of the entity.
 * 
 * @author Niall Gallagher
 */
public class EntityConsumer implements ByteConsumer {   
   
   /**
    * This is used to determine if there a continue is expected.
    */
   protected ContinueDispatcher dispatcher;   
   
   /**
    * This is used to create a body consumer for the entity.
    */
   protected ConsumerFactory factory;
   
   /**
    * This is used to consume the header for the request entity. 
    */
   protected RequestConsumer header;
   
   /**
    * This is used to consume the body for the request entity.
    */
   protected BodyConsumer body;
   
   /**
    * This is used to trace the progress of the request consumption.
    */
   protected Trace trace;
   
   /**
    * Constructor for the <code>EntityConsumer</code> object. This
    * is used to build an entity from the constituent parts. Once
    * all of the parts have been consumed they are available from
    * the exposed methods of this consumed instance.
    * 
    * @param allocator this is used to allocate the memory used
    * @param channel this is the channel used to send a response
    */
   public EntityConsumer(Allocator allocator, Channel channel) {
      this.header = new RequestConsumer();
      this.dispatcher = new ContinueDispatcher(channel);
      this.factory = new ConsumerFactory(allocator, header);
      this.trace = channel.getTrace();
   }
   
   /**
    * This is used to acquire the body for this HTTP entity. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Attachment</code> objects. 
    * Each part can then be read as an individual message.
    *  
    * @return the body provided by the HTTP request message
    */
   public Body getBody() {
      return body.getBody();
   }
   
   /**
    * This provides the HTTP request header for the entity. This is
    * always populated and provides the details sent by the client
    * such as the target URI and the query if specified. Also this
    * can be used to determine the method and protocol version used.
    * 
    * @return the header provided by the HTTP request message
    */
   public Header getHeader() {
      return header;
   }
   
   /**
    * This consumes the header and body from the cursor. The header
    * is consumed first followed by the body if there is any. There
    * is a body of there is a Content-Length or a Transfer-Encoding
    * header present. If there is no body then a substitute body 
    * is given which has an empty input stream.
    * 
    * @param cursor used to consumed the bytes for the entity
    */
   public void consume(ByteCursor cursor) throws IOException {
      while(cursor.isReady()) {
         if(header.isFinished()) {            
            if(body == null) {
               CharSequence sequence = header.getHeader();
               
               trace.trace(HEADER_FINISHED, sequence);
               body = factory.getInstance();    
            }
            trace.trace(READ_BODY);
            body.consume(cursor);            
            
            if(body.isFinished()) {
               trace.trace(BODY_FINISHED);
               break;
            }
         } else {
            trace.trace(READ_HEADER);
            header.consume(cursor);
         }
      }
      if(header.isFinished()) {
         if(body == null) {
            CharSequence sequence = header.getHeader();
            
            trace.trace(HEADER_FINISHED, sequence);
            dispatcher.execute(header);            
            body = factory.getInstance();
         } 
      }
   }   
   
   /**
    * This is determined finished when the body has been consumed.
    * If only the header has been consumed then the body will be
    * created using the header information, the body is then read
    * from the cursor, which may read nothing for an empty body.
    * 
    * @return this returns true if the entity has been built
    */
   public boolean isFinished() {
      if(header.isFinished()) {
         if(body == null) {
            CharSequence sequence = header.getHeader();
            
            trace.trace(HEADER_FINISHED, sequence);
            body = factory.getInstance();
         }
         return body.isFinished();
      } 
      return false;
   
   }
   
   /**
    * This is used to determine if the header has finished. Exposing
    * this method ensures the entity consumer can be used to determine
    * if the header for the entity can be consumed before fully
    * processing the entity body of the request message.
    * 
    * @return determines if the header has been fully consumed
    */
   public boolean isHeaderFinished() {
      return header.isFinished();
   }
}
