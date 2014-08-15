/*
 * ServiceDispatcher.java February 2014
 *
 * Copyright (C) 2014, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.socket.service;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.util.thread.ConcurrentExecutor;

/**
 * The <code>ServiceDispatcher</code> object is used to perform the
 * opening handshake for a WebSocket session. Once the session has been
 * established it is connected to a <code>Service</code> where frames
 * can be sent and received. If for any reason the handshake fails
 * this will terminated the connection with a HTTP 400 response.
 * 
 * @author Niall Gallagher
 */
class ServiceDispatcher {
   
   /**
    * This is used to check the session using ping control frames.
    */
   private final SessionChecker checker;
   
   /**
    * This is used asynchronously read frames from the TCP channel.
    */
   private final Executor executor;
   
   /**
    * This is used to notify of read events on the TCP channel.
    */
   private final Reactor reactor;
   
   /**
    * This is used to select and route to a specific service.
    */
   private final Router router;

   /**
    * Constructor for the <code>ServiceDispatcher</code> object. The
    * dispatcher created will dispatch WebSocket sessions to a service
    * using the provided <code>Router</code> instance. 
    * 
    * @param router this is the router used to select a service
    * @param threads this is the number of threads to use
    */
   public ServiceDispatcher(Router router, int threads) throws IOException {
      this(router, threads, 5000);
   }
   
   /**
    * Constructor for the <code>ServiceDispatcher</code> object. The
    * dispatcher created will dispatch WebSocket sessions to a service
    * using the provided <code>Router</code> instance. 
    * 
    * @param router this is the router used to select a service
    * @param threads this is the number of threads to use
    * @param ping this is the frequency used to send ping frames
    */
   public ServiceDispatcher(Router router, int threads, long ping) throws IOException {
      this(router, threads, ping, 20000);
   }
   
   /**
    * Constructor for the <code>ServiceDispatcher</code> object. The
    * dispatcher created will dispatch WebSocket sessions to a service
    * using the provided <code>Router</code> instance. 
    * 
    * @param router this is the router used to select a service
    * @param threads this is the number of threads to use
    * @param ping this is the frequency used to send ping frames
    * @param expiry this is the expiry for the session    
    */
   public ServiceDispatcher(Router router, int threads, long ping, long expiry) throws IOException {
      this.executor = new ConcurrentExecutor(FrameCollector.class, threads);
      this.checker = new SessionChecker(ping, expiry);
      this.reactor = new ExecutorReactor(executor);
      this.router = router;
   }

   /**
    * This method is used to create a dispatch a <code>Session</code> to
    * a specific service selected by a router. If the session initiating
    * handshake fails for any reason this will close the underlying TCP
    * connection and send a HTTP 400 response back to the client. 
    * 
    * @param request this is the session initiating request
    * @param response this is the session initiating response
    */
   public void dispatch(Request request, Response response) {
      SessionBuilder connector = new SessionBuilder(request, response, reactor);
      SessionDispatcher dispatcher = new SessionDispatcher(connector, checker, router);      
      
      dispatcher.dispatch(request, response);
   }
   
   /**
    * This is used to initiating session management by pinging all
    * connected WebSocket channels. If after a specific number of 
    * pings the WebSocket does not respond then the WebSocket is
    * closed using a control frame.
    */
   public void start() {
      checker.start();
   }
   
   /**
    * This is used to stop session management. Stopping the session
    * manger means connected WebSocket channels will not receive
    * any ping messages, they will however still receive pong frames
    * if a ping is sent to it. Session management can be started 
    * and stopped at will.
    */
   public void stop() {
      checker.stop();
   }
}
