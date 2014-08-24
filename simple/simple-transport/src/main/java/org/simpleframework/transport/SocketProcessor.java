/*
 * SocketProcessor.java February 2001
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
 * The <code>SocketProcessor</code> interface represents a processor that 
 * is used to accept <code>Socket</code> objects. Implementations of
 * this object will typically hand the socket over for processing either
 * by some form of protocol handler or message processor. If the socket
 * contains an <code>SSLEngine</code> an SSL hand shake may be performed
 * before any messages on the socket are interpreted.
 *
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.connect.SocketConnection
 */ 
public interface SocketProcessor {
   
   /**
    * Used to process the <code>Socket</code> which is a full duplex 
    * TCP connection to a higher layer the application. It is this
    * layer that is responsible for interpreting a protocol or handling
    * messages in some manner. In the case of HTTP this will initiate
    * the consumption of a HTTP request after any SSL handshake is 
    * finished if the connection is secure.
    *
    * @param socket this is the connected HTTP socket to process
    */ 
   void process(Socket socket) throws IOException;

   /**
    * This method is used to stop the <code>SocketProcessor</code> such 
    * that it will accept no more sockets. Stopping the server ensures
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
