/*
 * SocketListenerManager.java February 2007
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

package org.simpleframework.transport.connect;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashSet;

import javax.net.ssl.SSLContext;

import org.simpleframework.transport.Server;
import org.simpleframework.transport.trace.Analyzer;

/**
 * The <code>SocketListenerManager</code> contains all the listeners
 * that have been created for a connection. This set is used to hold
 * and manage the listeners that have been created for a connection.
 * All listeners will be closed if the listener manager is closed.
 * This ensures all resources held by the manager can be released.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.connect.SocketConnection
 */
class SocketListenerManager extends HashSet<SocketListener> implements Closeable {   
   
   /**
    * This is the analyzer used to create a trace for the sockets.
    */
   private final Analyzer analyzer; 
   
   /**
    * This is the server that listeners will dispatch sockets to.
    */
   private final Server server;
   
   /**
    * Constructor for the <code>SocketListenerManager</code> object. 
    * This is used to create a manager that will enable listeners to 
    * be created to listen to specified sockets for incoming TCP
    * connections, which will be converted to socket objects.
    * 
    * @param server this is the server that sockets are handed to
    * @param analyzer this is the agent used to trace socket events
    */
   public SocketListenerManager(Server server, Analyzer analyzer) {
      this.analyzer = new SocketAnalyzer(analyzer);
      this.server = server;
   }
   
   /**
    * This creates a new background task that will listen to the 
    * specified <code>ServerAddress</code> for incoming TCP connect
    * requests. When an connection is accepted it is handed to the
    * internal <code>Server</code> implementation as a pipeline. The
    * background task is a non daemon task to ensure the server is
    * kept active, to terminate the connection this can be closed.
    * 
    * @param address this is the address used to accept connections
    * 
    * @return this returns the actual local address that is used
    */ 
   public SocketAddress listen(SocketAddress address) throws IOException {
      return listen(address, null);
   }
   
   /**
    * This creates a new background task that will listen to the 
    * specified <code>ServerAddress</code> for incoming TCP connect
    * requests. When an connection is accepted it is handed to the
    * internal <code>Server</code> implementation as a pipeline. The
    * background task is a non daemon task to ensure the server is
    * kept active, to terminate the connection this can be closed.
    * 
    * @param address this is the address used to accept connections
    * @param context this is used for secure SSL connections
    * 
    * @return this returns the actual local address that is used
    */ 
   public SocketAddress listen(SocketAddress address, SSLContext context) throws IOException {
      SocketListener listener = new SocketListener(address, context, server, analyzer);
      
      if(server != null) {
         add(listener); 
      }
      return listener.getAddress();   
   }
   
   /**
    * This is used to close all the listeners that have been
    * added to the connection. Closing all the listeners in the
    * set ensures that there are no lingering threads or sockets
    * consumed by the connection after the connection is closed.
    * 
    * @throws IOException thrown if there is an error closing
    */
   public void close() throws IOException {
      for(Closeable listener : this) {
         listener.close();
      }
      if(analyzer != null) {
         analyzer.stop();
      }
      clear();
   }
}