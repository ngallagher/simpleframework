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
 * @see org.simpleframework.http.socket.WebSocket
 */
public interface Session {
   
   /**
    * Provides a <code>WebSocket</code> that can be used to communicate
    * with the connected client. Communication is full duplex and also
    * asynchronous through the use of a <code>FrameListener</code> that
    * can be registered with the socket. 
    * 
    * @return a web socket for full duplex communication
    */
   WebSocket getSocket();
   
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
   
   /**
    * Provides the protocol used to establish the session. If there are
    * multiple protocols requested this is the protocol that was sent
    * back to the client as the accepted protocol. Exposing this is
    * for convenience only as it can also be acquired from the response.  
    * 
    * @return returns the protocol chosen for this session
    */
   String getProtocol();
   
   /**
    * Provides the key used to establish the session. The key is a small
    * random token sent by the client. In normal operation the key must
    * not be used to identify the session as it is possible two clients
    * may choose the same key, it is however useful for audit purposes.
    * 
    * @return the random key provided by the connected client
    */
   String getKey();
}
