/*
 * ServiceEvent.java February 2014
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

/**
 * The <code>ServiceEvent</code> enumeration contains the events that
 * are dispatched processing a WebSocket. To see how a WebSocket is
 * behaving and to gather performance statistics the service events 
 * can be intercepted using a custom <code>TraceAnalyzer</code> object.  
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.trace.TraceAnalyzer
 */
public enum ServiceEvent {
   
   /**
    * This event is dispatched when a WebSocket is connected.
    */
   OPEN_SOCKET,
   
   /**
    * This event is dispatched when a WebSocket is dispatched.
    */
   DISPATCH_SOCKET,
   
   /**
    * This event is dispatched when a WebSocket channel is closed.
    */
   TERMINATE_SOCKET,
   
   /**
    * This event is dispatched when the response handshake is sent.
    */
   WRITE_HEADER,
   
   /**
    * This event is dispatched when the WebSocket receives a ping.
    */
   READ_PING,
   
   /**
    * This event is dispatched when a ping is sent over a WebSocket.
    */
   WRITE_PING,
   
   /**
    * This event is dispatched when the WebSocket receives a pong.
    */
   READ_PONG,
   
   /**
    * This event is dispatched when a pong is sent over a WebSocket.
    */
   WRITE_PONG,
   
   /**
    * This event is dispatched when a frame is read from a WebSocket.
    */
   READ_FRAME,
   
   /**
    * This event is dispatched when a frame is sent over a WebSocket.
    */
   WRITE_FRAME,   
   
   /**
    * This indicates that there has been no response to a ping.
    */
   PING_EXPIRED,
   
   /**
    * This indicates that there has been no response to a ping.
    */
   PONG_RECEIVED,   
   
   /**
    * This event is dispatched when an error occurs with a WebSocket.
    */
   ERROR;
}
