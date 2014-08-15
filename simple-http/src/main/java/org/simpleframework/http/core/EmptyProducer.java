/*
 * EmptyProducer.java February 2007
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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.simpleframework.transport.Sender;

/**
 * The <code>EmptyProducer</code> object is a producer used if there
 * is not response body to be delivered. Typically this is used when
 * the HTTP request method is HEAD or if there is some status code
 * sent to the client that does not require a response body.
 *
 * @author Niall Gallagher
 */  
class EmptyProducer implements Producer {
   
   /**
    * This is the observer that is used to process the pipeline.
    */
   private final Observer observer;
   
   /**
    * This is the sender that is passed to the monitor when ready.
    */
   private final Sender sender;
   
   /**
    * Constructor for the <code>EmptyProducer</code> object. Once 
    * created this producer will signal the kernel the the next
    * request is ready to read from the HTTP pipeline as there is
    * no content to be delivered with this producer object.
    *
    * @param sender this is used to send to the underlying transport
    * @param observer this is used to deliver signals to the kernel   
    */         
   public EmptyProducer(Sender sender, Observer observer) {    
      this.observer = observer;
      this.sender = sender;
   }
  
   /**
    * This method performs no operation. Because this producer is
    * not required to generate a response body this will ignore all
    * data that is provided to sent to the underlying transport.
    *
    * @param array this is the array of bytes to send to the client
    */  
   public void produce(byte[] array) throws IOException {
      return;
   }
  
   /**
    * This method performs no operation. Because this producer is
    * not required to generate a response body this will ignore all
    * data that is provided to sent to the underlying transport.
    *
    * @param array this is the array of bytes to send to the client
    * @param off this is the offset within the array to send from
    * @param size this is the number of bytes that are to be sent    
    */     
   public void produce(byte[] array, int off, int size) throws IOException {
      return;
   }
   
   /**
    * This method is used to encode the provided buffer of bytes in
    * a HTTP/1.1 compliant format and sent it to the client. Once
    * the data has been encoded it is handed to the transport layer
    * within the server, which may choose to buffer the data if the
    * content is too small to send efficiently or if the socket is
    * not write ready.
    *
    * @param buffer this is the buffer of bytes to send to the client
    */         
   public void produce(ByteBuffer buffer) throws IOException {
      return;
   }

   /**
    * This method is used to encode the provided buffer of bytes in
    * a HTTP/1.1 compliant format and sent it to the client. Once
    * the data has been encoded it is handed to the transport layer
    * within the server, which may choose to buffer the data if the
    * content is too small to send efficiently or if the socket is
    * not write ready.
    *
    * @param buffer this is the buffer of bytes to send to the client
    * @param off this is the offset within the buffer to send from
    * @param size this is the number of bytes that are to be sent
    */          
   public void produce(ByteBuffer buffer, int off, int size) throws IOException {
      return;
   }
   
   /**
    * This method performs no operation. Because this producer is
    * not required to generate a response body this will ignore all
    * data that is provided to sent to the underlying transport.
    */   
   public void flush() throws IOException {
      return;
   }
 
   /**
    * This method performs no operation. Because this producer is
    * not required to generate a response body this will ignore all
    * data that is provided to sent to the underlying transport.
    */   
   public void close() throws IOException {
      observer.ready(sender);
   }
}

