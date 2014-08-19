/*
 * ProcessorServer.java February 2007
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

package org.simpleframework.transport;

import java.io.IOException;

import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.util.thread.Daemon;

/**
 * The <code>ProcessorServer</code> is used to convert pipelines
 * to transports. This simply acts as an adapter to a processor
 * which converts a connected pipeline to a <code>Transport</code>
 * which can be used to read and write data. Conversion of the
 * pipeline is performed only once per connection.
 * 
 * @author Niall Gallagher
 */
public class ProcessorServer implements Server {

   /**
    * This is the factory used to create the required operations.
    */
   private final OperationFactory factory;
   
   /**
    * This is the processor used to process transport objects.
    */
   private final Negotiator negotiator;
   
   /**
    * This is used to terminate the internals of the processor.
    */
   private final Daemon terminator;

   /**
    * Constructor for the <code>ProcessorServer</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    */
   public ProcessorServer(Processor processor) throws IOException {
      this(processor, 8);
   }
   
   /**
    * Constructor for the <code>ProcessorServer</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    * @param threads this is the number of threads this will use
    */
   public ProcessorServer(Processor processor, int threads) throws IOException {
      this(processor, threads, 4096);
   }
      
   /**
    * Constructor for the <code>ProcessorServer</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    * @param threads this is the number of threads this will use
    * @param buffer this is the initial size of the output buffer 
    */
   public ProcessorServer(Processor processor, int threads, int buffer) throws IOException {
      this(processor, threads, buffer, 20480);
   }
   
   /**
    * Constructor for the <code>ProcessorServer</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    * @param threads this is the number of threads this will use
    * @param buffer this is the initial size of the output buffer      
    * @param threshold this is the maximum size of the output buffer
    */
   public ProcessorServer(Processor processor, int threads, int buffer, int threshold) throws IOException {
      this(processor, threads, buffer, threshold, false);
   }
   
   /**
    * Constructor for the <code>ProcessorServer</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    * @param threads this is the number of threads this will use
    * @param buffer this is the initial size of the output buffer      
    * @param threshold this is the maximum size of the output buffer
    * @param client determines if the SSL handshake is for a client
    */
   public ProcessorServer(Processor processor, int threads, int buffer, int threshold, boolean client) throws IOException {
      this.negotiator = new SecureNegotiator(processor, threads);
      this.factory = new OperationFactory(negotiator, buffer, threshold, client);
      this.terminator = new ServerTerminator(processor, negotiator);
   }

   /**
    * Used to process the <code>Pipeline</code> which is a full duplex 
    * communication link that may contain several HTTP requests. This 
    * will be used to read the requests from the <code>Pipeline</code> 
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
      Operation task = factory.getInstance(socket);
      
      if(task != null) {
         negotiator.process(task);
      }
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
    * <p>
    * This is implemented to shut down the server asynchronously. It 
    * will start a process to perform the shutdown. Asynchronous
    * shutdown allows a server resource executed via a HTTP request
    * can stop the server without any danger of killing itself or
    * even worse causing a deadlock.
    */    
   public void stop() throws IOException {
      terminator.start();
   }
 }
