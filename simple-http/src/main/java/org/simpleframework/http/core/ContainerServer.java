/*
 * ContainerServer.java February 2001
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

import org.simpleframework.transport.Processor;
import org.simpleframework.transport.ProcessorServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.Socket;
import org.simpleframework.util.buffer.Allocator;
import org.simpleframework.util.buffer.FileAllocator;

/**
 * The <code>ContainerServer</code> object provides a processor
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
public class ContainerServer implements Server {

   /**
    * This is the transporter used to process the connections.
    */
   private final Processor processor;
   
   /**
    * This is used to deliver pipelines to the container.
    */
   private final Server server;  

   /**
    * Constructor for the <code>ContainerServer</code> object. The
    * processor created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    */
   public ContainerServer(Container container) throws IOException {
      this(container, 8);
   }
   
   /**
    * Constructor for the <code>ContainerServer</code> object. The
    * processor created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param count this is the number of threads used for each pool
    */
   public ContainerServer(Container container, int count) throws IOException {
      this(container, count, 1);
   }
   
   /**
    * Constructor for the <code>ContainerServer</code> object. The
    * processor created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param count this is the number of threads used for each pool
    * @param select this is the number of selector threads to use
    */
   public ContainerServer(Container container, int count, int select) throws IOException {
      this(container, new FileAllocator(), count, select);
   }
   
   /**
    * Constructor for the <code>ContainerServer</code> object. The
    * processor created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param allocator this is the allocator used to create buffers
    */   
   public ContainerServer(Container container, Allocator allocator) throws IOException {
      this(container, allocator, 8);
   } 
   
   /**
    * Constructor for the <code>ContainerServer</code> object. The
    * processor created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param allocator this is the allocator used to create buffers
    * @param count this is the number of threads used for each pool
    */   
   public ContainerServer(Container container, Allocator allocator, int count) throws IOException {
      this(container, allocator, count, 1);
   }   
   
   /**
    * Constructor for the <code>ContainerServer</code> object. The
    * processor created will collect HTTP requests from the pipelines
    * provided and dispatch those requests to the provided container.
    * 
    * @param container this is the container used to service requests
    * @param allocator this is the allocator used to create buffers
    * @param count this is the number of threads used for each pool
    * @param select this is the number of selector threads to use
    */   
   public ContainerServer(Container container, Allocator allocator, int count, int select) throws IOException {
     this.processor = new ContainerProcessor(container, allocator, count, select);
     this.server = new ProcessorServer(processor, count); 
   }  

   /**
    * Used to process the <code>Socket</code> which is a full duplex 
    * communication link that may contain several HTTP requests. This 
    * will be used to read the requests from the <code>Socket</code> 
    * and to pass these requests to a <code>Container</code> for 
    * processing.
    * <p>
    * Typical usage of this method is to accept multiple pipeline 
    * objects, each representing a unique HTTP channel to the client,
    * and process requests from those pipelines concurrently.  
    *
    * @param socket this is the connected HTTP pipeline to process
    */    
   public void process(Socket socket) throws IOException {
     server.process(socket);
   }  
   
   /**
    * This method is used to stop the <code>Processor</code> such that
    * it will accept no more pipelines. Stopping the processor ensures
    * that all resources occupied will be released. This is required
    * so that all threads are stopped, and all memory is released.
    * <p>
    * Typically this method is called once all connections to the
    * server have been stopped. As a final act of shutting down the
    * entire server all threads must be stopped, this allows collection
    * of unused memory and the closing of file and socket resources.
    */    
   public void stop() throws IOException {
      server.stop();
   }
 }
