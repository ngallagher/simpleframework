/*
 * Processor.java February 2007
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
 * This is the <code>Processor</code> used to process messages from
 * a connected transport. This will process each request from a
 * provided transport and pass those requests to a container. The
 * transport provided can be either a direct transport or provide 
 * some form of secure encoding such as SSL.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.Transport
 */
public interface Processor {

   /**
    * This is used to process the requests from a provided transport
    * and deliver a response to those requests. A transport can be
    * a direct transport or a secure transport providing SSL.  
    * <p>
    * Typical usage of this method is to accept multiple transport 
    * objects, each representing a unique TCP channel to the client,
    * and process requests from those transports concurrently.  
    *      
    * @param transport the transport to process requests from
    */
   void process(Transport transport) throws IOException;
   
   /**
    * This method is used to stop the <code>Processor</code> such 
    * that it will accept no more pipelines. Stopping the processor
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
