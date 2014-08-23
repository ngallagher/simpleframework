/*
 * FrameHeader.java February 2014
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

import org.simpleframework.http.socket.FrameType;

/**
 * The <code>FrameHeader</code> represents the variable length header
 * used for a WebSocket frame. It is used to determine the number of
 * bytes that need to be consumed to successfully process a frame 
 * from the connected client.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.FrameConsumer
 */
interface FrameHeader {
   
   /**
    * This is used to determine the type of frame. Interpretation of
    * this type is outlined in RFC 6455 and can be loosely categorised
    * as control frames and either data or binary frames.     
    * 
    * @return this returns the type of frame that this represents
    */
   FrameType getType();
   
   /**
    * This provides the client mask send with the request. The mask is 
    * a 32 bit value that is used as an XOR bitmask of the client
    * payload. Masking applies only in the client to server direction. 
    * 
    * @return this returns the 32 bit mask used for this frame
    */
   byte[] getMask();
   
   /**
    * This provides the length of the payload within the frame. It 
    * is used to determine how much data to consume from the underlying
    * TCP stream in order to recreate the frame to dispatch.     
    * 
    * @return the number of bytes used in the frame
    */
   int getLength();
   
   /**
    * This is used to determine if the frame is masked. All client 
    * frames should be masked according to RFC 6455. If masked the 
    * payload will have its contents bitmasked with a 32 bit value.
    * 
    * @return this returns true if the payload has been masked
    */
   boolean isMasked();
   
   /**
    * This is used to determine if the frame is the final frame in
    * a sequence of fragments or a whole frame. If this returns false
    * then the frame is a continuation from from a sequence of 
    * fragments, otherwise it is a whole frame or the last fragment.
    * 
    * @return this returns false if the frame is a fragment
    */
   boolean isFinal();
}
