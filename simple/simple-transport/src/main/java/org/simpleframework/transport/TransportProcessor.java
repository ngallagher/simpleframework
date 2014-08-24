/*
 * TransportProcessor.java February 2007
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
 * This is the <code>TransportProcessor</code> used to process the
 * provided transport in a higher layer. It is the responsibility of
 * the delegate to handle protocols and message processing. In the
 * case of HTTP this will process requests for a container. The
 * transport provided can be either a direct transport or provide 
 * some form of secure encoding such as SSL.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.Transport
 */
public interface TransportProcessor {

   /**
    * This is used to process a <code>Transport</code> instance in
    * a higher layer that can handle a protocol. A transport can be
    * a direct transport or a secure transport providing SSL. At this
    * point any SSL handshake will have already completed.
    * <p>
    * Typical usage of this method is to accept multiple transport 
    * objects, each representing a unique TCP channel to the client,
    * and process requests from those transports concurrently.  
    *      
    * @param transport the transport to process requests from
    */
   void process(Transport transport) throws IOException;
   
   /**
    * This method is used to stop the <code>TransportProcessor</code> 
    * such that it will accept no more pipelines. Stopping the connector
    * ensures that all resources occupied will be released. This is 
    * required so that all threads are stopped and released.
    * <p>
    * Typically this method is called once all connections to the
    * server have been stopped. As a final act of shutting down the
    * entire server all threads must be stopped, this allows collection
    * of unused memory and the closing of file and socket resources.
    */ 
   void stop() throws IOException;
}
