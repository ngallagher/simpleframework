/*
 * RequestReader.java February 2001
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

import static org.simpleframework.http.core.ContainerEvent.ERROR;

import java.nio.channels.SocketChannel;

import org.simpleframework.transport.Channel;
import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>RequestReader</code> object is used to read the bytes 
 * that form the request entity. In order to execute a read operation 
 * the socket must be read ready. This is determined using the socket 
 * object, which is registered with a controller. If at any point the 
 * reading results in an error the operation is cancelled and the 
 * collector is closed, which shuts down the connection.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.reactor.Reactor
 */ 
class RequestReader implements Operation {   

   /**
    * This is the selector used to process the collection events.
    */
   private final Controller controller;   

   /**
    * This is the collector used to consume the entity bytes.
    */
   private final Collector collector;   
   
   /**
    * This is the channel object associated with the collector.
    */
   private final Channel channel;
   
   /**
    * This is used to collect any trace information.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>RequestReader</code> object. This is
    * used to collect the data required to compose a HTTP request.
    * Once all the data has been read by this it is dispatched. 
    * 
    * @param controller the controller object used to process events
    * @param collector this is the task used to collect the entity
    */
   public RequestReader(Controller controller, Collector collector){
      this.channel = collector.getChannel();
      this.trace = channel.getTrace();
      this.collector = collector;        
      this.controller = controller;    
   }   
   
   /**
    * This is used to acquire the trace object that is associated
    * with the operation. A trace object is used to collection details
    * on what operations are being performed. For instance it may 
    * contain information relating to I/O events or errors. 
    * 
    * @return this returns the trace associated with this operation
    */    
   public Trace getTrace() {
      return trace;
   }   
   
   /**
    * This is the <code>SocketChannel</code> used to determine if the
    * connection has some bytes that can be read. If it contains any
    * data then that data is read from and is used to compose the 
    * request entity, which consists of a HTTP header and body.
    * 
    * @return this returns the socket for the connected pipeline
    */
   public SocketChannel getChannel() {
      return channel.getSocket();
   }

   /**
    * This <code>run</code> method is used to collect the bytes from
    * the connected channel. If a sufficient amount of data is read
    * from the socket to form a HTTP entity then the collector uses
    * the <code>Selector</code> object to dispatch the request. This
    * is sequence of events that occur for each transaction.
    */
   public void run() {
      try {
         collector.collect(controller);
      }catch(Throwable cause){
         trace.trace(ERROR, cause);
         channel.close();
      } 
   }
   
   /**
    * This is used to cancel the operation if it has timed out. If 
    * the retry is waiting too long to read content from the socket
    * then the retry is cancelled and the underlying transport is 
    * closed. This helps to clean up occupied resources.     
    */       
   public void cancel() {
      try {
         channel.close();
      } catch(Throwable cause) {
         trace.trace(ERROR, cause);
      }
   }  
}
