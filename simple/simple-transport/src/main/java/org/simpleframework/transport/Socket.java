/*
 * Socket.java February 2001
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
 
package org.simpleframework.transport;

import java.nio.channels.SocketChannel;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.trace.Trace;

/**
 * This is a <code>Socket</code> interface that is used to represent 
 * a socket. This has a map that allows attributes to be associated 
 * with the client connection. Attributes such as security details
 * or other transport related details can be exposed by placing them
 * in the socket map. The <code>Processor</code> can then use these
 * attributes as required.
 * <p>
 * This provides the connected <code>SocketChannel</code> that can 
 * be used to read and write data asynchronously. The socket channel 
 * must be selectable and in non-blocking mode. If the socket is not
 * in a non-blocking state the connection will not be processed.
 *
 * @author Niall Gallagher
 */ 
public interface Socket {
   
   /**
    * This is used to acquire the trace object that is associated
    * with the socket. A trace object is used to collection details
    * on what operations are being performed on the socket. For
    * instance it may contain information relating to I/O events
    * or more application specific events such as errors. 
    * 
    * @return this returns the trace associated with this socket
    */
   Trace getTrace();

   /**
    * This is used to acquire the SSL engine used for security. If 
    * the socket is connected to an SSL transport this returns an 
    * SSL engine which can be used to establish the secure connection
    * and send and receive content over that connection. If this is
    * null then the socket represents a normal transport. 
    *  
    * @return the SSL engine used to establish a secure transport
    */
   SSLEngine getEngine();
   
   /**
    * This method is used to acquire the <code>SocketChannel</code>
    * for the connection. This allows the server to acquire the input
    * and output streams with which to communicate. It can also be 
    * used to configure the connection and perform various network 
    * operations that could otherwise not be performed.
    *
    * @return this returns the socket used by this socket
    */         
   SocketChannel getChannel();

   /**
    * This method is used to get the <code>Map</code> of attributes 
    * for this socket. The attributes map is used to maintain details
    * about the connection. Information such as security credentials
    * to client details can be placed within the attribute map.
    *
    * @return this returns the map of attributes for this socket
    */
   Map getAttributes();
}


