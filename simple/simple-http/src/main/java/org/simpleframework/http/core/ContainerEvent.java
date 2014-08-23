/*
 * ContainerEvent.java October 2012
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

package org.simpleframework.http.core;

/**
 * The <code>ContainerEvent</code> enum represents events that occur when
 * processing a HTTP transaction. Here each phase of processing has a 
 * single event to represent it. If a <code>Trace</code> object has been
 * associated with the connection then the server will notify the trace
 * when the connection enters a specific phase of processing.  
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.trace.Trace
 */
public enum ContainerEvent {
   
   /**
    * This event indicates that the server is reading the request header.
    */
   READ_HEADER,
   
   /**
    * This event indicates that the server is reading the request body.
    */
   READ_BODY,
   
   /**
    * This event indicates that the server is writing the response header.
    */
   WRITE_HEADER,
   
   /**
    * This event indicates that the server is writing the response body.
    */
   WRITE_BODY,
   
   /**
    * This indicates that the server has fully read the request header.
    */
   HEADER_FINISHED,
   
   /**
    * This indicates that the server has fully read the request body.
    */
   BODY_FINISHED,
   
   /**
    * This event indicates that the server sent a HTTP continue reply.
    */
   DISPATCH_CONTINUE,
   
   /**
    * This event indicates that the request is ready for processing.
    */   
   REQUEST_READY,
   
   /**
    * This indicates that the request has been dispatched for processing.
    */
   DISPATCH_REQUEST,
   
   /**
    * This indicates that the dispatch thread has completed the dispatch.
    */
   DISPATCH_FINISHED,   
   
   /**
    * This indicates that all the bytes within the response are sent.
    */
   RESPONSE_FINISHED,
   
   /**
    * This indicates that there was some error event with the request.
    */
   ERROR;
}
