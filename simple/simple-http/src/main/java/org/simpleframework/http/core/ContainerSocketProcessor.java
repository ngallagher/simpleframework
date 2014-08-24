/*
 * ContainerSocketProcessor.java February 2001
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

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.FileAllocator;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.Socket;

/**
 * The <code>ContainerSocketProcessor</code> object is a connector
 * that dispatch requests from a connected pipeline. SSL connections
 * and plain connections can be processed by this implementation. It
 * collects data from the connected pipelines and constructs the
 * requests and responses used to dispatch to the container.
 * <p>
 * In order to process the requests this uses two thread pools. One 
 * is used to collect data from the pipelines and create the requests.
 * The other is used to service those requests. Such an architecture
 * ensures that the serving thread does not have to deal with I/O
 * operations. All data is consumed before it is serviced.
 * 
 * @author Niall Gallagher
 */
public class ContainerSocketProcessor implements SocketProcessor {

   /**
    * This is the transporter used to process the connections.
    */
   private final TransportProcessor processor;
   
   /**
    * This is used to deliver pipelines to the container.
    */
   private final SocketProcessor adapter;  

   /**
    * Constructor for the <code>ContainerSocketProcessor</code> object. 
    * The connector created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    */
   public ContainerSocketProcessor(Container container) throws IOException {
      this(container, 8);
   }
   
   /**
    * Constructor for the <code>ContainerSocketProcessor</code> object. 
    * The connector created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param count this is the number of threads used for each pool
    */
   public ContainerSocketProcessor(Container container, int count) throws IOException {
      this(container, count, 1);
   }
   
   /**
    * Constructor for the <code>ContainerSocketProcessor</code> object. The
    * connector created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param count this is the number of threads used for each pool
    * @param select this is the number of selector threads to use
    */
   public ContainerSocketProcessor(Container container, int count, int select) throws IOException {
      this(container, new FileAllocator(), count, select);
   }
   
   /**
    * Constructor for the <code>ContainerSocketProcessor</code> object. 
    * The connector created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param allocator this is the allocator used to create buffers
    */   
   public ContainerSocketProcessor(Container container, Allocator allocator) throws IOException {
      this(container, allocator, 8);
   } 
   
   /**
    * Constructor for the <code>ContainerSocketProcessor</code> object. 
    * The connector created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param allocator this is the allocator used to create buffers
    * @param count this is the number of threads used for each pool
    */   
   public ContainerSocketProcessor(Container container, Allocator allocator, int count) throws IOException {
      this(container, allocator, count, 1);
   }   
   
   /**
    * Constructor for the <code>ContainerSocketProcessor</code> object. 
    * The connector created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param allocator this is the allocator used to create buffers
    * @param count this is the number of threads used for each pool
    * @param select this is the number of selector threads to use
    */   
   public ContainerSocketProcessor(Container container, Allocator allocator, int count, int select) throws IOException {
     this.processor = new ContainerTransportProcessor(container, allocator, count, select);
     this.adapter = new TransportSocketProcessor(processor, count); 
   }  

   /**
    * This is used to consume HTTP messages that arrive on the socket
    * and dispatch them to the internal container. Depending on whether
    * the socket contains an <code>SSLEngine</code> an SSL handshake may
    * be performed before any HTTP messages are consumed. This can be
    * called from multiple threads and does not block. 
    *
    * @param socket this is the connected HTTP pipeline to process
    */    
   public void process(Socket socket) throws IOException {
     adapter.process(socket);
   }  
   
   /**
    * This method is used to stop the connector in such a way that it
    * will not accept and process any further messages. If there are
    * resources to clean up they may be cleaned up asynchronously
    * so that this method can return without blocking.
    */     
   public void stop() throws IOException {
      adapter.stop();
   }
 }
