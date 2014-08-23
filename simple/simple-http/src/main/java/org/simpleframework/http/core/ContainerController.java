/*
 * ContainerController.java February 2001
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

import static java.nio.channels.SelectionKey.OP_READ;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.TransportException;
import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>ContainerController</code> object is essentially the core
 * processing engine for the server. This is used to collect requests
 * from the connected channels and dispatch those requests to the
 * provided <code>Container</code> object. This contains two thread
 * pools. The first is used to collect data from the channels and
 * create request entities. The second is used to take the created
 * entities and service them with the provided container.
 * 
 * @author Niall Gallagher
 */
class ContainerController implements Controller {  
   
   /**
    * This is the thread pool used for servicing the requests.
    */
   private final ConcurrentExecutor executor;
   
   /**
    * This is the thread pool used for collecting the requests.
    */
   private final ConcurrentExecutor collect;

   /**
    * This is the allocator used to create the buffers needed.
    */
   private final Allocator allocator; 
   
   /**
    * This is the container used to service the requests.
    */
   private final Container container;
   
   /**
    * This is the reactor used to schedule the collectors.
    */
   private final Reactor reactor; 

   /**
    * Constructor for the <code>ContainerController</code> object. This
    * is used to create a controller which will collect and dispatch
    * requests using two thread pools. The first is used to collect
    * the requests, the second is used to service those requests.
    * 
    * @param container this is the container used to service requests
    * @param allocator this is used to allocate any buffers needed
    * @param count this is the number of threads per thread pool
    * @param select this is the number of controller threads to use
    */
   public ContainerController(Container container, Allocator allocator, int count, int select) throws IOException {
      this.executor = new ConcurrentExecutor(RequestDispatcher.class, count); 
      this.collect = new ConcurrentExecutor(RequestReader.class, count);
      this.reactor = new ExecutorReactor(collect, select);     
      this.allocator = allocator;
      this.container = container;
   }

   /**
    * This is used to initiate the processing of the channel. Once
    * the channel is passed in to the initiator any bytes ready on
    * the HTTP pipeline will be processed and parsed in to a HTTP
    * request. When the request has been built a callback is made
    * to the <code>Container</code> to process the request. Also
    * when the request is completed the channel is passed back in
    * to the initiator so that the next request can be dealt with.
    * 
    * @param channel the channel to process the request from
    */   
   public void start(Channel channel) throws IOException {
      start(new RequestCollector(allocator, channel));    
   }

   /**
    * The start event is used to immediately consume bytes form the
    * underlying transport, it does not require a select to check
    * if the socket is read ready which improves performance. Also,
    * when a response has been delivered the next request from the 
    * pipeline is consumed immediately.     
    * 
    * @param collector this is the collector used to collect data
    */   
   public void start(Collector collector) throws IOException {
      reactor.process(new RequestReader(this, collector));   
   }
   
   /**
    * The select event is used to register the connected socket with 
    * a Java NIO selector which can efficiently determine when there 
    * are bytes ready to read from the socket. 
    *      
    * @param collector this is the collector used to collect data
    */   
   public void select(Collector collector) throws IOException {
      reactor.process(new RequestReader(this, collector), OP_READ);      
   }
   
   /**
    * The ready event is used when a full HTTP entity has been 
    * collected from the underlying transport. On such an event the 
    * request and response can be handled by a container.
    * 
    * @param collector this is the collector used to collect data
    */   
   public void ready(Collector collector) throws IOException {
      executor.execute(new RequestDispatcher(container, this, collector));
   }   
   
   /**
    * This method is used to stop the <code>Selector</code> so that
    * all resources are released. As well as freeing occupied memory
    * this will also stop all threads, which means that is can no
    * longer be used to collect data from the pipelines. 
    * <p>
    * Here we stop the <code>Reactor</code> first, this ensures 
    * that there are no further selects performed if a given socket
    * does not have enough data to fulfil a request. From there we
    * stop the main dispatch <code>Executor</code> so that all of
    * the currently executing tasks complete. The final stage of
    * termination requires the collector thread pool to be stopped.
    */   
   public void stop() throws IOException {
     try {
        reactor.stop();
        executor.stop();
        collect.stop();
     } catch(Exception cause) {
        throw new TransportException("Error stopping", cause);
     }
   }
}
