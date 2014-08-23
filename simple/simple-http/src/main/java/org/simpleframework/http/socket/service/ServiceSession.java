/*
 * ServiceSession.java February 2014
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

import java.util.Map;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.http.socket.Session;

/**
 * The <code>ServiceSession</code> represents a simple WebSocket session
 * that contains the connection handshake details and the actual socket.
 * In order to determine how the session should be interacted with the
 * protocol is conveniently exposed, however all attributes of the
 * original HTTP request are available.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.FrameChannel
 */
class ServiceSession implements Session {
   
   /**
    * The WebSocket used for asynchronous full duplex communication.
    */
   private final FrameChannel channel;
   
   /**
    * This is the initiating response associated with the session.
    */
   private final Response response;
   
   /**
    * This is the initiating request associated with the session.
    */
   private final Request request;
   
   /**
    * This is the bag of attributes used by this session.
    */
   private final Map attributes;
   
   /**
    * Constructor for the <code>ServiceSession</code> object. This is used 
    * to create the session that will be used by a <code>Service</code> to
    * send and receive WebSocket frames.
    * 
    * @param channel this is the actual WebSocket for the session
    * @param request this is the session initiating request
    * @param response this is the session initiating response
    */
   public ServiceSession(FrameChannel channel, Request request, Response response) {
      this.channel = new ServiceChannel(channel);
      this.attributes = request.getAttributes();
      this.response = response;
      this.request = request;
   }
   
   /**
    * This can be used to retrieve the response attributes. These can
    * be used to keep state with the response when it is passed to
    * other systems for processing. Attributes act as a convenient
    * model for storing objects associated with the response. This 
    * also inherits attributes associated with the client connection.
    *
    * @return the attributes of that have been set on the request
    */ 
   public Map getAttributes() {
      return attributes;
   }

   /**
    * This is used as a shortcut for acquiring attributes for the
    * response. This avoids acquiring the attribute <code>Map</code>
    * in order to retrieve the attribute directly from that object.
    * The attributes contain data specific to the response.
    * 
    * @param key this is the key of the attribute to acquire
    * 
    * @return this returns the attribute for the specified name
    */ 
   public Object getAttribute(Object key) {
      return attributes.get(key);
   }

   /**
    * Provides a <code>WebSocket</code> that can be used to communicate
    * with the connected client. Communication is full duplex and also
    * asynchronous through the use of a <code>FrameListener</code> that
    * can be registered with the socket. 
    * 
    * @return a web socket for full duplex communication
    */
   public FrameChannel getChannel() {
      return channel;
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
}
