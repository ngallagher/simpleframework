/*
 * Session.java February 2014
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

package org.simpleframework.http.socket;

import java.util.Map;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * The <code>Session</code> object represents a simple WebSocket session
 * that contains the connection handshake details and the actual socket.
 * In order to determine how the session should be interacted with the
 * protocol is conveniently exposed, however all attributes of the
 * original HTTP request are available.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.FrameChannel
 */
public interface Session {
   
   /**
    * This can be used to retrieve the response attributes. These can
    * be used to keep state with the response when it is passed to
    * other systems for processing. Attributes act as a convenient
    * model for storing objects associated with the response. This 
    * also inherits attributes associated with the client connection.
    *
    * @return the attributes of that have been set on the request
    */ 
   Map getAttributes();

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
   Object getAttribute(Object key);
   
   /**
    * Provides a <code>FrameChannel</code> that can be used to communicate
    * with the connected client. Communication is full duplex and also
    * asynchronous through the use of a <code>FrameListener</code> that
    * can be registered with the channel. 
    * 
    * @return a web socket for full duplex communication
    */
   FrameChannel getChannel();
   
   /**
    * Provides the <code>Request</code> used to initiate the session.
    * This is useful in establishing the identity of the user, acquiring
    * an security information and also for determining the request path
    * that was used, which be used to establish context.
    * 
    * @return the request used to initiate the session
    */
   Request getRequest();
   
   /**
    * Provides the <code>Response</code> used to establish the session 
    * with the remote client. This is useful in establishing the protocol
    * used to create the session and also for determining various other 
    * useful contextual information.
    * 
    * @return the response used to establish the session
    */
   Response getResponse();
}
