/*
 * SocketWrapper.java February 2001
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
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.trace.Trace;

/**
 * This is a <code>SocketWrapper</code> objects that represents a TCP 
 * socket connections. This contains a map that allows attributes to be
 * associated with the client connection. Attributes such as security
 * certificates or other transport related details can be exposed to
 * the <code>Request</code> using the socket attribute map.
 * <p>
 * This provides the connected <code>SocketChannel</code> that can be
 * used to receive and response to HTTP requests. The socket channel 
 * must be selectable and in non-blocking mode. If the socket is not
 * in a non-blocking state the connection will not be processed.
 *
 * @author Niall Gallagher
 */ 
public class SocketWrapper implements Socket {
   
   /**
    * This is the socket that provides the input and output.
    */
   private final SocketChannel channel;

   /**
    * This is used to encrypt content for secure connections.
    */
   private final SSLEngine engine;
   
   /**
    * This can be used to trace specific events for the socket.
    */
   private final Trace trace;
   
   /**
    * This is used to store the attributes for the socket.
    */
   private final Map map;

   /**
    * This creates a <code>SocketWrapper</code> from a socket channel. 
    * Any implementations of the object may use this constructor to 
    * ensure that all the data is initialized. 
    *
    * @param channel the socket channel that is used as the transport
    * @param trace used to trace specific events for the socket
    */    
   public SocketWrapper(SocketChannel channel, Trace trace) {
      this(channel, trace, null);
   }
   
   /**
    * This creates a <code>SecureSocket</code> from a socket channel. 
    * Any implementations of the object may use this constructor to 
    * ensure that all the data is initialized.  
    *
    * @param channel the socket channel that is used as the transport
    * @param trace used to trace specific events for the socket
    * @param engine this is the SSL engine used for secure transport
    */   
   public SocketWrapper(SocketChannel channel, Trace trace, SSLEngine engine) {
      this.map = new HashMap();
      this.channel = channel;
      this.engine = engine;
      this.trace = trace;
   } 
   
   /**
    * This is used to acquire the trace object that is associated
    * with the socket. A trace object is used to collection details
    * on what operations are being performed on the socket. For
    * instance it may contain information relating to I/O events
    * or more application specific events such as errors. 
    * 
    * @return this returns the trace associated with this socket
    */
   public Trace getTrace() {
      return trace;
   }
   
   /**
    * This is used to acquire the SSL engine used for HTTPS. If the
    * socket is connected to an SSL transport this returns an SSL
    * engine which can be used to establish the secure connection
    * and send and receive content over that connection. If this is
    * null then the socket represents a normal transport. 
    *  
    * @return the SSL engine used to establish a secure transport
    */   
   public SSLEngine getEngine() {
      return engine;
   }
   
   /**
    * This method is used to acquire the <code>SocketChannel</code>
    * for the connection. This allows the server to acquire the input
    * and output streams with which to communicate. It can also be 
    * used to configure the connection and perform various network 
    * operations that could otherwise not be performed.
    *
    * @return this returns the socket used by this HTTP socket
    */    
   public SocketChannel getChannel() {
      return channel;
   }
  
   /**
    * This method is used to get the <code>Map</code> of attributes 
    * by this socket. The attributes map is used to maintain details
    * about the connection. Information such as security credentials
    * to client details can be placed within the attribute map.
    *
    * @return this returns the map of attributes for this socket
    */   
   public Map getAttributes() {
      return map;           
   }
}

