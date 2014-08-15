/*
 * Controller.java February 2007
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

import org.simpleframework.transport.Channel;

/**
 * The <code>Controller</code> interface represents an object which 
 * is used to process collection events. The sequence of events that
 * typically take place is for the collection to start, if not all
 * of the bytes can be consumed it selects, and finally when all of
 * the bytes within the entity have been consumed it is ready.
 * <p>
 * The start event is used to immediately consume bytes form the
 * underlying transport, it does not require a select to determine
 * if the socket is read ready which provides an initial performance
 * enhancement. Also when a response has been delivered the next
 * request from the pipeline is consumed immediately.
 * <p>
 * The select event is used to register the connected socket with a
 * Java NIO selector which can efficiently determine when there are
 * bytes ready to read from the socket. Finally, the ready event
 * is used when a full HTTP entity has been collected from the 
 * underlying transport. On such an event the request and response
 * can be handled by a container.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.core.Collector
 */
interface Controller {
   
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
  void start(Channel channel) throws IOException;
  
   /**
    * The start event is used to immediately consume bytes form the
    * underlying transport, it does not require a select to check
    * if the socket is read ready which improves performance. Also,
    * when a response has been delivered the next request from the 
    * pipeline is consumed immediately.     
    * 
    * @param collector this is the collector used to collect data
    */
   void start(Collector collector) throws IOException;
   
   /**
    * The select event is used to register the connected socket with 
    * a Java NIO selector which can efficiently determine when there 
    * are bytes ready to read from the socket. 
    *      
    * @param collector this is the collector used to collect data
    */
   void select(Collector collector) throws IOException;
   
   /**
    * The ready event is used when a full HTTP entity has been 
    * collected from the underlying transport. On such an event the 
    * request and response can be handled by a container.
    * 
    * @param collector this is the collector used to collect data
    */
   void ready(Collector collector) throws IOException;
   
   /**
    * This method is used to stop the <code>Selector</code> so that
    * all resources are released. As well as freeing occupied memory
    * this will also stop all threads, which means that is can no
    * longer be used to collect data from the pipelines.    
    */
   void stop() throws IOException;
}
