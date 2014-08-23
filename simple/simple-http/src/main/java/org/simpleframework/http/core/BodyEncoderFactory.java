/*
 * BodyEncoderFactory.java February 2007
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

package org.simpleframework.http.core;

import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteWriter;

/**
 * The <code>BodyEncoderFactory</code> is used to create a producer to
 * match the HTTP header sent with the response. This interprets the
 * headers within the response and composes a producer that will
 * match those. Producers can be created to send in chunked encoding
 * format, as well as fixed length and connection close for HTTP/1.0.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.core.ResponseEncoder
 */
class BodyEncoderFactory {

   /**
    * This is used to determine the semantics of the HTTP pipeline.
    */         
   private final Conversation support;   

   /**
    * This is the monitor used to notify the initiator of events.
    */ 
   private final BodyObserver observer;   

   /**
    * This is the underlying sender used to deliver the raw data.
    */ 
   private final ByteWriter writer;   
   
   /**
    * Constructor for the <code>BodyEncoderFactory</code> object. 
    * This is used to create producers that can encode data in a HTTP
    * compliant format. Each producer created will produce its data
    * and deliver it to the specified sender, should an I/O events
    * occur such as an error, or completion of the response then 
    * the monitor is notified and the server kernel takes action.
    *
    * @param observer this is used to deliver signals to the kernel  
    * @param support this contains details regarding the semantics
    * @param writer this is used to send to the underlying transport  
    */ 
   public BodyEncoderFactory(BodyObserver observer, Conversation support, Channel channel) {
      this.writer = channel.getWriter();
      this.observer = observer;
      this.support = support;
   }
  
   /**
    * This is used to create an a <code>BodyEncoder</code> object 
    * that can be used to encode content according to the HTTP header.
    * If the request was from a HTTP/1.0 client that did not ask 
    * for keep alive connection semantics a simple close producer
    * is created. Otherwise the content is chunked encoded or sent
    * according the the Content-Length.
    *
    * @return this returns the producer used to send the response
    */  
   public BodyEncoder getInstance() {
      boolean keepAlive = support.isKeepAlive();
      boolean chunkable = support.isChunkedEncoded();
      boolean tunnel = support.isTunnel();
      
      if(!keepAlive || tunnel) {
         return new CloseEncoder(observer, writer);
      }
      return getInstance(chunkable);
   }
   
   /**
    * This is used to create an a <code>BodyEncoder</code> object 
    * that can be used to encode content according to the HTTP header.
    * If the request was from a HTTP/1.0 client that did not ask 
    * for keep alive connection semantics a simple close producer
    * is created. Otherwise the content is chunked encoded or sent
    * according the the Content-Length.
    *
    * @param chunkable does the connected client support chunked
    *
    * @return this returns the producer used to send the response
    */     
   private BodyEncoder getInstance(boolean chunkable) {      
      long length = support.getContentLength();
      
      if(!support.isHead()) { 
         if(length > 0) {
            return new FixedLengthEncoder(observer, writer, length);
         }
         if(chunkable) {
            return new ChunkedEncoder(observer, writer);
         }
      } 
      return new EmptyEncoder(observer, writer);
   }
}


