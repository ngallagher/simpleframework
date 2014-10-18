/*
 * SessionBuilder.java February 2014
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

import org.simpleframework.common.thread.Scheduler;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.socket.Session;
import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>SessionBuilder</code> object is used to create sessions
 * for connected WebSockets. Before the session is created a response
 * is sent back to the connected client. If for some reason the session
 * is not valid or does not conform to the requirements of RFC 6455
 * then a HTTP 400 response code is sent and the TCP channel is closed.
 * 
 * @author Niall Gallagher
 */
class SessionBuilder {
   
   /**
    * This is the scheduler that is used to ping WebSocket sessions.
    */
   private final Scheduler scheduler;
   
   /**
    * This is the reactor used to register for I/O notifications.
    */
   private final Reactor reactor;
   
   /**
    * This is the frequency the server should send out ping frames.
    */
   private final long ping;
   
   /**
    * Constructor for the <code>SessionBuilder</code> object. This is
    * used to create sessions using the request and response associated
    * with the WebSocket opening handshake. 
    * 
    * @param scheduler this is the shared thread pool used for pinging
    * @param reactor this is used to check for I/O notifications
    * @param ping this is the frequency to send out ping frames
    */
   public SessionBuilder(Scheduler scheduler, Reactor reactor, long ping) {
      this.scheduler = scheduler;
      this.reactor = reactor;
      this.ping = ping;
   }
   
   /**
    * This is used to create a WebSocket session. If at any point there 
    * is an error creating the session the underlying TCP connection is
    * closed and a <code>Session</code> is returned regardless. 
    * 
    * @param request this is the request associated with this session
    * @param response this is the response associated with this session
    * 
    * @return this returns the session associated with the WebSocket
    */
   public Session create(Request request, Response response) throws Exception {
      FrameConnection connection = new FrameConnection(request, response, reactor);
      ResponseBuilder builder = new ResponseBuilder(request, response);
      StatusChecker checker = new StatusChecker(connection, request, scheduler, ping);

      try {
         builder.commit();
         checker.start();
      } catch(Exception e) {
         throw new IOException("Could not send response", e);
      }
      return connection.open();
   }
}
