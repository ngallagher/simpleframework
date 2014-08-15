/*
 * Server.java February 2001
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
 
package org.simpleframework.transport;

import java.io.IOException;

/**
 * The <code>Server</code> interface represents a handler that is
 * used to process <code>Socket</code> objects. Implementations of
 * this object will read HTTP requests for the provided sockets and
 * dispatch the requests for processing by the core protocol handler.
 * <p>
 * The intended use of a <code>Server</code> is that it be used in
 * conjunction with a <code>Container</code> object, which acts as the
 * primary protocol handler for a server. Typically the server will
 * deliver callbacks to a container with both <code>Request</code> and
 * <code>Response</code> objects encapsulating the transaction.
 * <p>
 * Core responsibilities of the server are to manage connections, 
 * to ensure that all HTTP requests are collected, and to dispatch the
 * collected requests to an appropriate handler. It is also required
 * to manage multiplexing such that many connections can be processed
 * concurrently without a high latency period.
 *
 * @author Niall Gallagher
 */ 
public interface Server {
   
   /**
    * Used to process the <code>Socket</code> which is a full duplex 
    * communication link that may contain several HTTP requests. This 
    * will be used to read the requests from the <code>Socket</code> 
    * and to pass these requests to a <code>Container</code> for 
    * processing.
    * <p>
    * Typical usage of this method is to accept multiple sockets
    * objects, each representing a unique HTTP channel to the client,
    * and process requests from those sockets concurrently.  
    *
    * @param socket this is the connected HTTP socket to process
    */ 
   void process(Socket socket) throws IOException;

   /**
    * This method is used to stop the <code>Server</code> such that
    * it will accept no more sockets. Stopping the server ensures
    * that all resources occupied will be released. This is required
    * so that all threads are stopped, and all memory is released.
    * <p>
    * Typically this method is called once all connections to the
    * server have been stopped. As a final act of shutting down the
    * entire server all threads must be stopped, this allows collection
    * of unused memory and the closing of file and socket resources.
    */ 
   void stop() throws IOException;
}
