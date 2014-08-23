/*
 * ContainerProcessor.java February 2007
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

package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.transport.Processor;
import org.simpleframework.transport.Transport;
import org.simpleframework.transport.TransportChannel;

/**
 * The <code>ContainerProcessor</code> object is used to create 
 * channels which can be used to consume and process requests. This
 * is basically an adapter to the <code>Selector</code> which will
 * convert the provided transport to a usable channel. Each of the
 * connected pipelines will end up at this object, regardless of
 * whether those connections are SSL or plain data.
 * 
 * @author Niall Gallagher
 */
public class ContainerProcessor implements Processor {   
   
   /**
    * This is the controller used to process the created channels.
    */
   private final Controller controller;   

   /**
    * Constructor for the <code>ContainerProcessor</code> object.
    * This is used to create a processor which will convert the
    * provided transport objects to channels, which can then be
    * processed by the controller and dispatched to the container.
    * 
    * @param container the container to dispatch requests to
    * @param allocator this is the allocator used to buffer data
    * @param count this is the number of threads to be used
    */
   public ContainerProcessor(Container container, Allocator allocator, int count) throws IOException {
     this(container, allocator, count, 1);
   }  
 
   /**
    * Constructor for the <code>ContainerProcessor</code> object.
    * This is used to create a processor which will convert the
    * provided transport objects to channels, which can then be
    * processed by the controller and dispatched to the container.
    * 
    * @param container the container to dispatch requests to
    * @param allocator this is the allocator used to buffer data
    * @param count this is the number of threads to be used
    * @param select this is the number of controller threads to use
    */
   public ContainerProcessor(Container container, Allocator allocator, int count, int select) throws IOException {
     this.controller = new ContainerController(container, allocator, count, select);
   }        

   /**
    * This is used to process the requests from a provided transport
    * and deliver a response to those requests. A transport can be
    * a direct transport or a secure transport providing SSL.  
    * <p>
    * Typical usage of this method is to accept multiple transport 
    * objects, each representing a unique HTTP channel to the client,
    * and process requests from those transports concurrently.  
    *      
    * @param transport the transport to process requests from
    */   
   public void process(Transport transport) throws IOException {
      controller.start(new TransportChannel(transport));
   }
   
   /**
    * This method is used to stop the <code>Processor</code> such 
    * that it will accept no more pipelines. Stopping the processor
    * ensures that all resources occupied will be released. This is 
    * required so that all threads are stopped and released.
    * <p>
    * Typically this method is called once all connections to the
    * server have been stopped. As a final act of shutting down the
    * entire server all threads must be stopped, this allows collection
    * of unused memory and the closing of file and socket resources.
    */    
   public void stop() throws IOException {
      controller.stop();
   }
 }