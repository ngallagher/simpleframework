/*
 * RequestSession.java February 2014
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

import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_KEY;
import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_PROTOCOL;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;

/**
 * The <code>RequestSession</code> represents a simple WebSocket session
 * that contains the connection handshake details and the actual socket.
 * In order to determine how the session should be interacted with the
 * protocol is conveniently exposed, however all attributes of the
 * original HTTP request are available.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.WebSocket
 */
class RequestSession implements Session {
   
   /**
    * The WebSocket used for asynchronous full duplex communication.
    */
   private final WebSocket socket;
   
   /**
    * This is the initiating response associated with the session.
    */
   private final Response response;
   
   /**
    * This is the initiating request associated with the session.
    */
   private final Request request;
   
   /**
    * This is the protocol associated with the session.
    */
   private final String protocol;
   
   /**
    * This is the key associated with this session.
    */
   private final String key;   
   
   /**
    * Constructor for the <code>Request</code> object. This is used to
    * create the session that will be used by a <code>Service</code> to
    * send and receive WebSocket frames.
    * 
    * @param socket this is the actual WebSocket for the session
    * @param request this is the session initiating request
    * @param response this is the session initiating response
    */
   public RequestSession(WebSocket socket, Request request, Response response) {
      this.protocol = response.getValue(SEC_WEBSOCKET_PROTOCOL);
      this.key = request.getValue(SEC_WEBSOCKET_KEY);
      this.response = response;
      this.request = request;
      this.socket = socket;
   }

   /**
    * Provides a <code>WebSocket</code> that can be used to communicate
    * with the connected client. Communication is full duplex and also
    * asynchronous through the use of a <code>FrameListener</code> that
    * can be registered with the socket. 
    * 
    * @return a web socket for full duplex communication
    */
   public WebSocket getSocket() {
      return socket;
   }

   /**
    * Provides the <code>Request</code> used to initiate the session.
    * This is useful in establishing the identity of the user, acquiring
    * an security information and also for determining the request path
    * that was used, which be used to establish context.
    * 
    * @return the request used to initiate the session
    */
   public Request getRequest() {
      return request;
   }

   /**
    * Provides the <code>Response</code> used to establish the session 
    * with the remote client. This is useful in establishing the protocol
    * used to create the session and also for determining various other 
    * useful contextual information.
    * 
    * @return the response used to establish the session
    */
   public Response getResponse() {
      return response;
   }   
   
   /**
    * Provides the protocol used to establish the session. If there are
    * multiple protocols requested this is the protocol that was sent
    * back to the client as the accepted protocol. Exposing this is
    * for convenience only as it can also be acquired from the response.  
    * 
    * @return returns the protocol chosen for this session
    */
   public String getProtocol(){
      return protocol;
   }
   
   /**
    * Provides the key used to establish the session. The key is a small
    * random token sent by the client. In normal operation the key must
    * not be used to identify the session as it is possible two clients
    * may choose the same key, it is however useful for audit purposes.
    * 
    * @return the random key provided by the connected client
    */
   public String getKey() {
      return key;
   }
}
