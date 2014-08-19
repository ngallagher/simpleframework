/*
 * OperationFactory.java February 2007
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

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.reactor.Operation;

/**
 * The <code>OperationFactory</code> is used to create operations
 * for the transport processor. Depending on the configuration of the
 * pipeline object this will create different operations. Typically
 * this will create an SSL handshake operation if the pipeline has 
 * an <code>SSLEngine</code> instance. This allows the transport
 * processor to complete the handshake before handing the transport
 * to the transporter for processing.
 *  
 * @author Niall Gallagher
 */
class OperationFactory {
   
   /**
    * This is the negotiator used to process the created transport.
    */
   private final Negotiator negotiator;
   
   /**
    * This is the threshold for the asynchronous buffers to use.
    */
   private final int threshold;
   
   /**
    * This is the size of the buffers to be used by the transport.
    */
   private final int buffer;
   
   /**
    * This determines if the SSL handshake is for the client side.
    */
   private final boolean client;
   
   /**
    * Constructor for the <code>OperationFactory</code> object. This
    * uses the negotiator provided to hand off the created transport
    * when it has been created. All operations created typically
    * execute in an asynchronous thread.
    * 
    * @param negotiator the negotiator used to process transports 
    * @param buffer this is the initial size of the buffer to use   
    */
   public OperationFactory(Negotiator negotiator, int buffer) {
      this(negotiator, buffer, 20480);
   }
   
   /**
    * Constructor for the <code>OperationFactory</code> object. This
    * uses the negotiator provided to hand off the created transport
    * when it has been created. All operations created typically
    * execute in an asynchronous thread.
    * 
    * @param negotiator the negotiator used to process transports 
    * @param buffer this is the initial size of the buffer to use       
    * @param threshold maximum size of the output buffer to use
    */
   public OperationFactory(Negotiator negotiator, int buffer, int threshold) {
      this(negotiator, buffer, threshold, false);
   }
   
   /**
    * Constructor for the <code>OperationFactory</code> object. This
    * uses the negotiator provided to hand off the created transport
    * when it has been created. All operations created typically
    * execute in an asynchronous thread.
    * 
    * @param negotiator the negotiator used to process transports 
    * @param buffer this is the initial size of the buffer to use       
    * @param threshold maximum size of the output buffer to use
    * @param client determines if the SSL handshake is for a client
    */
   public OperationFactory(Negotiator negotiator, int buffer, int threshold, boolean client) {
      this.negotiator = negotiator;
      this.threshold = threshold;
      this.buffer = buffer;
      this.client = client;
   }
   
   /**
    * This method is used to create <code>Operation</code> object to
    * process the next phase of the negotiation. The operations that
    * are created using this factory ensure the processing can be
    * done asynchronously, which reduces the overhead the connection
    * thread has when handing the pipelines over for processing.
    * 
    * @param socket this is the pipeline that is to be processed
    * 
    * @return this returns the operation used for processing
    */
   public Operation getInstance(Socket socket) throws IOException {
      return getInstance(socket, socket.getEngine());
   }
   
   /**
    * This method is used to create <code>Operation</code> object to
    * process the next phase of the negotiation. The operations that
    * are created using this factory ensure the processing can be
    * done asynchronously, which reduces the overhead the connection
    * thread has when handing the pipelines over for processing.
    * 
    * @param socket this is the pipeline that is to be processed
    * @param engine this is the engine used for SSL negotiations 
    * 
    * @return this returns the operation used for processing
    */
   private Operation getInstance(Socket socket, SSLEngine engine) throws IOException {
      Transport transport = new SocketTransport(socket, negotiator, threshold, buffer);
   
      if(engine != null) {
         return new Handshake(transport, negotiator, client);
      } 
      return new TransportDispatcher(transport, negotiator);
   }
}
