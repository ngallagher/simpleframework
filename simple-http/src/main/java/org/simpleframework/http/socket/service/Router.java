/*
 * Router.java February 2014
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

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * The <code>Router</code> interface represents a means of routing 
 * a session initiating request to the correct service. Typically
 * a service is chosen based on the sub-protocol provided in the 
 * initiating request, however it can be chosen on any criteria
 * available in the request. An initiating request must contain
 * a <code>Connection</code> header with the <code>websocket</code>
 * token according to RFC 6455 section 4.2.1. If the request does 
 * not contain this token it is treated as a normal request and
 * a <code>Service</code> will not be resolved.
 * <p>
 * If a service has been successfully chosen from the initiating
 * request the the value of <code>Sec-WebSocket-Protocol</code> will
 * contain either the chosen protocol if a match was made with the
 * initiating request or null to indicate a default choice.   
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.RouterContainer
 */
public interface Router {
   
   /**
    * This is used to route an incoming request to a service if 
    * the request represents a WebSocket handshake as defined by
    * RFC 6455. If the request is not a session initiating handshake
    * then this must return a null value to allow it to be processed
    * by some other part of the server.
    * 
    * @param request this is the request to use for routing
    * @param response this is the response to establish the session
    * 
    * @return a service that can be used to process the session
    */
   Service route(Request request, Response response);
}
