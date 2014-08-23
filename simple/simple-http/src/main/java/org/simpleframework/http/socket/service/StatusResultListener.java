/*
 * StatusResultListener.java February 2014
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

import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;

/**
 * The <code>StatusResultListener</code> is used to listen for responses 
 * to ping frames sent out by the server. A response to the ping frame 
 * is a pong frame. When a pong is received it allows the session to
 * be scheduled to receive another ping.
 * 
 * @author Niall Gallagher
 */
class StatusResultListener implements FrameListener {
   
   /**
    * This is used to ping sessions to check for health.
    */
   private final StatusChecker checker;
   
   /**
    * Constructor for the <code>StatusResultListener</code> object. 
    * This requires the session health checker that performs the pings 
    * so that it can reschedule the session for multiple pings if
    * the connection responds with a pong.
    * 
    * @param checker this is the session health checker
    */
   public StatusResultListener(StatusChecker checker) {
      this.checker = checker;
   }

   /**
    * This is called when a new frame arrives on the WebSocket. If
    * the frame is a pong then this will reschedule the the session
    * to receive another ping frame.
    * 
    * @param session this is the associated session
    * @param frame this is the frame that has been received
    */   
   public void onFrame(Session session, Frame frame) {
      FrameType type = frame.getType();
      
      if(type.isPong()) {         
         checker.refresh(); 
      }
   }

   /**
    * This is called when there is an error with the connection.
    * When called the session is removed from the checker and no
    * more ping frames are sent.
    * 
    * @param session this is the associated session
    * @param cause this is the cause of the error
    */
   public void onError(Session session, Exception cause) {
      checker.failure();     
   }

   /**
    * This is called when the connection is closed from the other
    * side. When called the session is removed from the checker
    * and no more ping frames are sent.
    * 
    * @param session this is the associated session
    * @param reason this is the reason the connection was closed
    */
   public void onClose(Session session, Reason reason) {
      checker.close();
   }
}  