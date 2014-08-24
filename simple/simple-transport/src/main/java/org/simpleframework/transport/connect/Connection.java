/*
 * Connection.java October 2002
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

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

import javax.net.ssl.SSLContext;

/**
 * The <code>Connection</code> object is used to manage connections
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
public interface Connection extends Closeable {
   
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
   SocketAddress connect(SocketAddress address) throws IOException;
   
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
   SocketAddress connect(SocketAddress address, SSLContext context) throws IOException;
}
