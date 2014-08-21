/*
 * Service.java February 2014
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

import org.simpleframework.http.socket.Session;

/**
 * The <code>Service</code> interface represents a service that can be
 * used to communicate with the WebSocket protocol defined in RFC 6455.
 * Typically a service will implement a sub-protocol negotiated from
 * the initiating HTTP request. The service should be considered a
 * hand off point rather than an place to implement business logic.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.FrameChannel
 */
public interface Service {
   
   /**
    * This method connects a new session with a service implementation.
    * Connecting a session with a service in this way should not block
    * as it could cause starvation of the servicing thread pool.
    * 
    * @param session the new session to connect to the service
    */
   void connect(Session session);
}
