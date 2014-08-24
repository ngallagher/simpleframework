/*
 * SocketConnection.java October 2002
 *
 * Copyright (C) 2002, Niall Gallagher <niallg@users.sf.net>
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
  
package org.simpleframework.transport.connect;

import java.io.IOException;
import java.net.SocketAddress;

import javax.net.ssl.SSLContext;

import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.trace.TraceAnalyzer;

/**
 * The <code>SocketConnection</code>is used to manage connections
 * from a server socket. In order to achieve this it spawns a task
 * to listen for incoming connect requests. When a TCP connection
 * request arrives it hands off the <code>SocketChannel</code> to
 * the <code>SocketProcessor</code> which processes the request.
 * <p>
 * This handles connections from a <code>ServerSocketChannel</code> 
 * object so that features such as SSL can be used by a server that 
 * uses this package. The background acceptor process will terminate 
 * if the connection is closed. 
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.SocketProcessor
 */
public class SocketConnection implements Connection {
   
   /**
    * This is used to maintain the active connection end points.
    */
   private SocketListenerManager manager;
                   
   /** 
    * The processor is used to process connected HTTP pipelines.
    */
   private SocketProcessor processor;
   
   /**
    * This is used to determine if the connection has been closed.
    */
   private boolean closed;
   
   /** 
    * Constructor for the <code>SocketConnection</code> object. This
    * will create a new connection that accepts incoming connections
    * and hands these connections as <code>Socket</code> objects
    * to the specified connector. This in turn will deliver request
    * and response objects to the internal container.
    * 
    * @param processor this is the connector that receives requests
    */    
   public SocketConnection(SocketProcessor processor) throws IOException {
      this(processor, null);
   }

   /** 
    * Constructor for the <code>SocketConnection</code> object. This
    * will create a new connection that accepts incoming connections
    * and hands these connections as <code>Socket</code> objects
    * to the specified processor. This in turn will deliver request
    * and response objects to the internal container.
    * 
    * @param processor this is the connector that receives requests
    * @param analyzer this is used to create a trace for the socket
    */    
   public SocketConnection(SocketProcessor processor, TraceAnalyzer analyzer) throws IOException {
      this.manager = new SocketListenerManager(processor, analyzer);
      this.processor = processor;
   }
   
   /**
    * This creates a new background task that will listen to the 
    * specified <code>ServerAddress</code> for incoming TCP connect
    * requests. When an connection is accepted it is handed to the
    * internal socket connector.
    * 
    * @param address this is the address used to accept connections
    * 
    * @return this returns the actual local address that is used
    */   
   public SocketAddress connect(SocketAddress address) throws IOException {
      if(closed) {
         throw new ConnectionException("Connection is closed");
      }
      return manager.listen(address);  
   }
   
   /**
    * This creates a new background task that will listen to the 
    * specified <code>ServerAddress</code> for incoming TCP connect
    * requests. When an connection is accepted it is handed to the
    * internal socket connector.
    * 
    * @param address this is the address used to accept connections
    * @param context this is used for secure SSL connections
    * 
    * @return this returns the actual local address that is used
    */ 
   public SocketAddress connect(SocketAddress address, SSLContext context) throws IOException {
      if(closed) {
         throw new ConnectionException("Connection is closed");
      }
      return manager.listen(address, context);
   }
   
   /**
    * This is used to close the connection and the server socket
    * used to accept connections. This will perform a close of all
    * connected server sockets that have been created from using
    * the <code>connect</code> method. The connection can be 
    * reused after the existing server sockets have been closed.
    * 
    * @throws IOException thrown if there is a problem closing
    */   
   public void close() throws IOException {
      if(!closed) {
         manager.close();
         processor.stop(); 
      }
      closed = true;
   }
}