/*
 * FrameListener.java February 2014
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

package org.simpleframework.http.socket;

/**
 * The <code>FrameListener</code> is used to listen for incoming frames
 * on a <code>WebSocket</code>. Any number of listeners can listen on 
 * a single web socket and it will receive all incoming events. For 
 * consistency this interface is modelled on the WebSocket API as
 * defined by W3C Candidate Recommendation as of 20 September 2012.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.FrameChannel
 */
public interface FrameListener {
   
   /**
    * This is called when a new frame arrives on the WebSocket. It
    * will receive control frames as well as binary and text user
    * frames. Control frames should not be acted on or responded
    * to as they are provided for informational purposes only.
    * 
    * @param session this is the associated session
    * @param frame this is the frame that has been received
    */
   void onFrame(Session session, Frame frame);
   
   /**
    * This is called when an error occurs on the WebSocket. After
    * an error the connection it is closed with an opcode indicating
    * an internal server error. 
    * 
    * @param session this is the associated session
    * @param frame this is the exception that has been thrown
    */
   void onError(Session session, Exception cause);
   
   /**
    * This is called when the connection is closed from the other
    * side. Typically a frame with an opcode of close is sent 
    * before the close callback is issued.
    * 
    * @param session this is the associated session
    * @param reason this is the reason the connection was closed
    */
   void onClose(Session session, Reason reason);
}
