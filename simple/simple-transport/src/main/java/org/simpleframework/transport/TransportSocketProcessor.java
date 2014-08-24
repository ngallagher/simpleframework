/*
 * TransportSocketProcessor.java February 2007
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

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.common.thread.Daemon;
import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>TransportSocketProcessor</code> is used to convert sockets
 * to transports. This acts as an adapter to a transport processor 
 * which converts a connected socket to a <code>Transport</code> that
 * can be used to read and write data. Depending on whether there is
 * an <code>SSLEngine</code> associated with the socket or not, there
 * could be an SSL handshake performed.
 * 
 * @author Niall Gallagher
 */
public class TransportSocketProcessor implements SocketProcessor { 
   
   /**
    * This is the executor used to execute the I/O operations.
    */
   private final ConcurrentExecutor executor;   

   /**
    * This is the factory used to create the required operations.
    */
   private final OperationFactory factory;
   
   /**
    * This is the processor used to process transport objects.
    */
   private final Reactor reactor;
   
   /**
    * This is used to clean the internals of the processor.
    */
   private final Daemon cleaner;   

   /**
    * Constructor for the <code>TransportSocketProcessor</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    */
   public TransportSocketProcessor(TransportProcessor processor) throws IOException {
      this(processor, 8);
   }
   
   /**
    * Constructor for the <code>TransportSocketProcessor</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    * @param threads this is the number of threads this will use
    */
   public TransportSocketProcessor(TransportProcessor processor, int threads) throws IOException {
      this(processor, threads, 4096);
   }
      
   /**
    * Constructor for the <code>TransportSocketProcessor</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    * @param threads this is the number of threads this will use
    * @param buffer this is the initial size of the output buffer 
    */
   public TransportSocketProcessor(TransportProcessor processor, int threads, int buffer) throws IOException {
      this(processor, threads, buffer, 20480);
   }
   
   /**
    * Constructor for the <code>TransportSocketProcessor</code> object. 
    * The transport processor is used to process plain connections
    * and wrap those connections in a <code>Transport</code> that
    * can be used to send and receive data to and from.
    * 
    * @param processor this is used to process transports
    * @param threads this is the number of threads this will use
    * @param buffer this is the initial size of the output buffer      
    * @param threshold this is the maximum size of the output buffer
    */
   public TransportSocketProcessor(TransportProcessor processor, int threads, int buffer, int threshold) throws IOException {
      this(processor, threads, buffer, threshold, false);
   }
   
   /**
    * Constructor for the <code>TransportSocketProcessor</code> object. 
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
   public TransportSocketProcessor(TransportProcessor processor, int threads, int buffer, int threshold, boolean client) throws IOException {
      this.executor = new ConcurrentExecutor(Operation.class, threads);     
      this.reactor = new ExecutorReactor(executor);
      this.factory = new OperationFactory(processor, reactor, buffer, threshold, client);
      this.cleaner = new ServerCleaner(processor, executor, reactor);
   }

   /**
    * Used to connect the <code>Socket</code> which is a full duplex 
    * TCP connection to a higher layer the application. It is this
    * layer that is responsible for interpreting a protocol or handling
    * messages in some manner. In the case of HTTP this will initiate
    * the consumption of a HTTP request after any SSL handshake is 
    * finished if the connection is secure.    
    *
    * @param socket this is the connected HTTP pipeline to process
    */    
   public void process(Socket socket) throws IOException {
      Operation task = factory.getInstance(socket);
      
      if(task != null) {
         reactor.process(task);
      }
   }
   
   /**
    * This is implemented to shut down the server asynchronously. It 
    * will start a process to perform the shutdown. Asynchronous
    * shutdown allows a server resource executed via a HTTP request
    * can stop the server without any danger of killing itself or
    * even worse causing a deadlock.
    */    
   public void stop() throws IOException {
      cleaner.start();
      executor.stop();
   }
 }
