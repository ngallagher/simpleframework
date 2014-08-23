/*
 * Entity.java February 2007
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

package org.simpleframework.http.message;

import org.simpleframework.transport.Channel;

/**
 * The <code>Entity</code> object is used to represent the HTTP entity
 * received from the client. The entity contains a header and body as
 * well as the underlying <code>Channel</code> for the connection. If
 * there is no body with the entity this will provide an empty body
 * object which provides a zero length sequence of bytes.
 * 
 * @author Niall Gallagher
 */
public interface Entity {
   
   /**
    * This is the time in milliseconds when the request was first
    * read from the underlying channel. The time represented here
    * represents the time collection of this request began. This 
    * does not necessarily represent the time the bytes arrived on
    * the receive buffers as some data may have been buffered.
    * 
    * @return this represents the time the request was ready at
    */
   long getTime();
   
   /**
    * This is used to acquire the body for this HTTP entity. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Part</code> objects. Each
    * part can then be read as an individual message.
    *  
    * @return the body provided by the HTTP request message
    */
   Body getBody();   
   
   /**
    * This provides the HTTP request header for the entity. This is
    * always populated and provides the details sent by the client
    * such as the target URI and the query if specified. Also this
    * can be used to determine the method and protocol version used.
    * 
    * @return the header provided by the HTTP request message
    */
   Header getHeader();

   /**
    * This provides the connected channel for the client. This is
    * used to send and receive bytes to and from an transport layer.
    * Each channel provided with an entity contains an attribute 
    * map which contains information about the connection.
    * 
    * @return the connected channel for this HTTP entity
    */
   Channel getChannel();
}
