/*
 * DirectRouter.java February 2014
 *
 * Copyright (C) 2014, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.socket.service;

import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_PROTOCOL;
import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_VERSION;
import static org.simpleframework.http.Protocol.UPGRADE;
import static org.simpleframework.http.Protocol.WEBSOCKET;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * The <code>DirectRouter</code> object is used to create a router
 * that uses a single service. Typically this is used by simpler
 * servers that wish to expose a single sub-protocol to clients.
 * 
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.socket.service.RouterContainer
 */
public class DirectRouter implements Router {

   /**
    * The service used by this router instance.
    */
   private final Service service;
   
   /**
    * The protocol used or null if none was specified.
    */
   private final String protocol;

   /**
    * Constructor for the <code>DirectRouter</code> object. This 
    * is used to create an object that will select a single service.
    * Creating an instance with this constructor means that the 
    * protocol header will not be set.
    * 
    * @param service this is the service used by this instance
    * @param protocol the protocol used by this router or null
    */
   public DirectRouter(Service service) {
      this(service, null);
   }
   
   /**
    * Constructor for the <code>DirectRouter</code> object. This 
    * is used to create an object that will select a single service.
    * If the protocol specified is null then the response to the 
    * session initiation will contain null for the protocol header.
    * 
    * @param service this is the service used by this instance
    * @param protocol the protocol used by this router or null
    */
   public DirectRouter(Service service, String protocol) {
      this.protocol = protocol;
      this.service = service;
   }
   
   /**
    * This is used to route an incoming request to a service if 
    * the request represents a WebSocket handshake as defined by
    * RFC 6455. If the request is not a session initiating handshake
    * then this will return a null value to allow it to be processed
    * by some other part of the server.
    * 
    * @param request this is the request to use for routing
    * @param response this is the response to establish the session
    * 
    * @return a service that can be used to process the session
    */
   public Service route(Request request, Response response) {
      String token = request.getValue(UPGRADE);
      
      if(token != null) {
         if(token.equalsIgnoreCase(WEBSOCKET)) {
            String version = request.getValue(SEC_WEBSOCKET_VERSION);
            
            if(version != null) {
               response.setValue(SEC_WEBSOCKET_VERSION, version);
            }
            if(protocol != null) {
               response.setValue(SEC_WEBSOCKET_PROTOCOL, protocol);               
            }
            return service;
         }
      }
      return null;
   }
}
