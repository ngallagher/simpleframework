/*
 * Acceptor.java October 2002
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

import static org.simpleframework.transport.connect.ConnectionEvent.ACCEPT;
import static org.simpleframework.transport.connect.ConnectionEvent.ERROR;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.Socket;
import org.simpleframework.transport.SocketWrapper;
import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.transport.trace.TraceAnalyzer;

/**
 * The <code>SocketAcceptor</code> object is used to accept incoming 
 * TCP connections from a specified socket address. This is used by 
 * the <code>Connection</code> object as a background process to 
 * accept the connections and hand them to a socket connector.
 * <p>
 * This is capable of processing SSL connections created by the
 * internal server socket. All SSL connections are forced to finish
 * the SSL handshake before being dispatched to the server. This
 * ensures that there are no problems with reading the request.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.connect.SocketConnection
 */
class SocketAcceptor implements Operation {

   /**
    * This is the server socket channel used to accept connections.
    */
   private final ServerSocketChannel listener;
   
   /** 
    * The handler that manages the incoming TCP connections.
    */
   private final SocketProcessor processor; 

   /**
    * This is the server socket to bind the socket address to.
    */
   private final ServerSocket socket;

   /**
    * If provided the SSL context is used to create SSL engines.
    */
   private final SSLContext context;

   /**
    * This is the tracing analyzer used to trace accepted sockets.
    */
   private final TraceAnalyzer analyzer;
   
   /**
    * This is the local address to bind the listen socket to.
    */
   private final SocketAddress address;
   
   /**
    * This is used to collect trace events with the acceptor.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>SocketAcceptor</code> object. This 
    * accepts new TCP connections from the specified server socket. 
    * Each of the connections that is accepted is configured for 
    * performance for the application.
    *
    * @param address this is the address to accept connections from
    * @param processor this is used to initiate the HTTP processing
    * @param analyzer this is the tracing analyzer to be used
    */
   public SocketAcceptor(SocketAddress address, SocketProcessor processor, TraceAnalyzer analyzer) throws IOException {
      this(address, processor, analyzer, null);
   }

   /**
    * Constructor for the <code>SocketAcceptor</code> object. This 
    * accepts new TCP connections from the specified server socket. 
    * Each of the connections that is accepted is configured for 
    * performance for the applications.
    *
    * @param address this is the address to accept connections from
    * @param processor this is used to initiate the HTTP processing
    * @param analyzer this is the tracing analyzer to be used
    * @param context this is the SSL context used for secure HTTPS 
    */
   public SocketAcceptor(SocketAddress address, SocketProcessor processor, TraceAnalyzer analyzer, SSLContext context) throws IOException {
      this.listener = ServerSocketChannel.open();
      this.trace = analyzer.attach(listener);
      this.socket = listener.socket();
      this.context = context;
      this.analyzer = analyzer;
      this.processor = processor;
      this.address = address;
   }

   /**
    * This is used to acquire the local socket address that this is
    * listening to. This required in case the socket address that
    * is specified is an emphemeral address, that is an address that
    * is assigned dynamically when a port of 0 is specified.
    * 
    * @return this returns the address for the listening address
    */
   public SocketAddress getAddress() {
      return socket.getLocalSocketAddress();
   }
   
   /**
    * This is used to acquire the trace object that is associated
    * with the operation. A trace object is used to collection details
    * on what operations are being performed. For instance it may 
    * contain information relating to I/O events or errors. 
    * 
    * @return this returns the trace associated with this operation
    */     
   public Trace getTrace() {
      return trace;
   }  
   
   /**
    * This is the <code>SelectableChannel</code> which is used to 
    * determine if the operation should be executed. If the channel   
    * is ready for a given I/O event it can be run. For instance if
    * the operation is used to perform some form of read operation
    * it can be executed when ready to read data from the channel.
    *
    * @return this returns the channel used to govern execution
    */
   public SelectableChannel getChannel() {
      return listener;
   }   

   /**
    * This is used to configure the server socket for non-blocking
    * mode. It will also bind the server socket to the socket port
    * specified in the <code>SocketAddress</code> object. Once done
    * the acceptor is ready to accept newly arriving connections.
    * 
    * @param address this is the server socket address to bind to
    */
   public void bind() throws IOException {
      listener.configureBlocking(false);
      socket.setReuseAddress(true);
      socket.bind(address, 100);
   }   

   /**
    * This is used to accept a new TCP connections. When the socket
    * is ready to accept a connection this method is invoked. It will
    * then create a HTTP pipeline object using the accepted socket
    * and if provided with an <code>SSLContext</code> it will also
    * provide an <code>SSLEngine</code> which is handed to the
    * processor to handle the HTTP requests. 
    */
   public void run() {
      try {
         accept();
      } catch(Exception cause) {       
         pause();
      }
   }
   
   /**
    * This is used to throttle the acceptor when there is an error
    * such as exhaustion of file descriptors. This will prevent the
    * CPU from being hogged by the acceptor on such occasions. If
    * the thread can not be put to sleep then this will freeze.
    */
   private void pause() {
      try {
         Thread.sleep(10);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
      }
   }

   /**
    * This is used to cancel the operation if the reactor decides to
    * reject it for some reason. Typically this method will never be
    * invoked as this operation never times out. However, should the
    * reactor cancel the operation this will close the socket.     
    */
   public void cancel() {
      try {
         close();
      } catch(Throwable cause) {
         trace.trace(ERROR, cause);
      }
   }

   /**
    * The main processing done by this object is done using a thread
    * calling the <code>run</code> method. Here the TCP connections 
    * are accepted from the <code>ServerSocketChannel</code> which 
    * creates the socket objects. Each socket is then encapsulated in
    * to a pipeline and dispatched to the processor for processing. 
    * 
    * @throws IOException if there is a problem accepting the socket
    */
   private void accept() throws IOException {
      SocketChannel channel = listener.accept();
      
      while(channel != null) {      
         Trace trace = analyzer.attach(channel);
         
         configure(channel);

         if(context == null) {
            process(channel, trace, null);
         } else {
            process(channel, trace);
         }       
         channel = listener.accept();
      }
   }
   
   /**
    * This method is used to configure the accepted channel. This 
    * will disable Nagles algorithm to improve the performance of the
    * channel, also this will ensure the accepted channel disables
    * blocking to ensure that it works within the processor object.
    * 
    * @param channel this is the channel that is to be configured
    */
   private void configure(SocketChannel channel) throws IOException {
      channel.socket().setTcpNoDelay(true);   
      channel.configureBlocking(false);
   }

   /**
    * This method is used to dispatch the socket for processing. The 
    * socket will be configured and connected to the client, this 
    * will hand processing to the <code>Server</code> which will
    * create the pipeline instance used to wrap the socket object.
    *
    * @param channel this is the connected socket to be processed
    * @param trace this is the trace to associate with the socket        
    */
   private void process(SocketChannel channel, Trace trace) throws IOException {
      SSLEngine engine = context.createSSLEngine();

      try {
         process(channel, trace, engine);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();
      }
   }
   
   /**
    * This method is used to dispatch the socket for processing. The 
    * socket will be configured and connected to the client, this 
    * will hand processing to the <code>Server</code> which will
    * create the pipeline instance used to wrap the socket object.
    *
    * @param channel this is the connected socket to be processed
    * @param trace this is the trace to associate with the socket   
    * @param engine this is the SSL engine used for secure HTTPS  
    */
   private void process(SocketChannel channel, Trace trace, SSLEngine engine) throws IOException {
      Socket socket = new SocketWrapper(channel, trace, engine);
      
      try {
         trace.trace(ACCEPT);
         processor.process(socket);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();
      }
   }

   /**
    * This is used to close the server socket channel so that the
    * port that it is bound to is released. This allows the acceptor
    * to close off the interface to the server. Ensuring the socket
    * is closed allows it to be recreated at a later point.
    * 
    * @throws IOException thrown if the socket can not be closed
    */
   public void close() throws IOException {
      listener.close();
   }
}
