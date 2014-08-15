/*
 * Method.java May 2012
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http;

/**
 * The <code>Method</code> interface contains the common HTTP methods
 * that are sent with a request. This only contains those methods
 * that have been defined within the RFC 2616 specification. These
 * are defined here for convenience and informational purposes.
 * 
 * @author Niall Gallagher
 */
public interface Method {
   
   /**
    * For use with a proxy that can dynamically switch to being a tunnel. 
    */
   String CONNECT = "CONNECT";
   
   /**
    * Requests that the origin server delete the resource identified. 
    */
   String DELETE = "DELETE";
   
   /**
    * Retrieve whatever information is identified by the request. 
    */
   String GET = "GET";
   
   /**
    * Retrieve only the headers for the resource that is requested. 
    */
   String HEAD = "HEAD";
   
   /**
    * Represents a request for the communication options available.
    */
   String OPTIONS = "OPTIONS";
   
   /**
    * Request that the origin server accept the entity in the request. 
    */
   String POST = "POST";
   
   /**
    * Requests that the entity be stored as the resource specified
    */
   String PUT = "PUT";
   
   /**
    * Invoke a remote application layer loop back of the request.
    */
   String TRACE = "TRACE";
}