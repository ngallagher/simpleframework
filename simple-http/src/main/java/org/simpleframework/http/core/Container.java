/*
 * Container.java February 2001
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

package org.simpleframework.http.core;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * The <code>Container</code> object is used to process HTTP requests
 * and compose HTTP responses. The <code>Request</code> objects that
 * are handed to this container contain all information relating to 
 * the received message. The responsibility of the container is to
 * interpret the request and compose a suitable response.
 * <p>
 * All implementations must ensure that the container is thread safe
 * as it will receive multiple HTTP transactions concurrently. Also
 * it should be known that the <code>Response</code> object used to
 * deliver the HTTP response will only commit and send once it has 
 * its <code>OutputStream</code> closed.
 * <p>
 * The <code>Container</code> is entirely responsible for the HTTP 
 * message headers and body. It is up to the implementation to ensure
 * that it complies to RFC 2616 or any previous specification. All 
 * headers and the status line can be modified by this object.
 * 
 * @author Niall Gallagher
 */
public interface Container {

   /**
    * Used to pass the <code>Request</code> and <code>Response</code>
    * to the container for processing. Any implementation of this 
    * must ensure that this is thread safe, as it will receive many
    * concurrent invocations each with a unique HTTP request.
    * <p>
    * The request and response objects are used to interact with the
    * connected pipeline, in such a way that requests and response
    * objects can be delivered in sequence and without interference.
    * The next request from a HTTP pipeline is only processed once
    * the <code>Response</code> object has been closed and committed.
    *
    * @param req the request that contains the client HTTP message
    * @param resp the response used to deliver the server response
    */
   void handle(Request req, Response resp);
}
