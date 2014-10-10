/*
 * ProtocolRouter.java February 2014
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * The <code>ProtocolRouter</code> is used when there are multiple 
 * services that can be used. Each service is selected based on the
 * protocol sent in the initiating request. If a match cannot be
 * made based on the request then a default service us chosen.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.RouterContainer
 */
public class ProtocolRouter implements Router {

   /**
    * This is the set of services that can be selected.
    */
   private final Map<String, Service> registry;
   
   /**
    * This is the default service chosen if there is no match.
    */
   private final Service primary;
   
   /**
    * Constructor for the <code>ProtocolRouter</code> object. This is
    * used to create a router using a selection of services that can
    * be selected using the <code>Sec-WebSocket-Protocol</code> header
    * sent in the initiating request by the client.
    * 
    * @param registry this is the registry of available services
    * @param primary this is the default service to use
    */
   public ProtocolRouter(Map<String, Service> registry, Service primary) throws IOException {
      this.registry = registry;
      this.primary = primary;
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
            List<String> protocols = request.getValues(SEC_WEBSOCKET_PROTOCOL);      
            String version = request.getValue(SEC_WEBSOCKET_VERSION);
            
            if(version != null) {
               response.setValue(SEC_WEBSOCKET_VERSION, version);
            }
            for(String protocol : protocols) {
               Service service = registry.get(protocol);
               
               if(service != null) {
                  response.setValue(SEC_WEBSOCKET_PROTOCOL, protocol);
                  return service;
               }
            }
            return primary;
         }
      }
      return null;
   }
}
