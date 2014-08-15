/*
 * Frame.java February 2014
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
 * The <code>Frame</code> interface represents a frame as defined in
 * RFC 6455. A frame is a very lightweight envelope used to send 
 * control information and either text or binary user data. Typically
 * a frame will represent a single message however, it is possible 
 * to fragment a single frame up in to several frames. A fragmented
 * frame has a specific <code>FrameType</code> indicating that it
 * is a continuation frame.   
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.DataFrame
 */
public interface Frame {
   
   /**
    * This is used to determine if the frame is the final frame in
    * a sequence of fragments or a whole frame. If this returns false
    * then the frame is a continuation from from a sequence of 
    * fragments, otherwise it is a whole frame or the last fragment.
    * 
    * @return this returns false if the frame is a fragment
    */
   boolean isFinal();
   
   /**
    * This returns the binary payload that is to be sent with the frame.
    * It contains no headers or other meta data. If the original data
    * was text this converts it to UTF-8.
    * 
    * @return the binary payload to be sent with the frame
    */
   byte[] getBinary();
   
   /**
    * This returns the text payload that is to be sent with the frame. 
    * It contains no header information or meta data. Caution should 
    * be used with this method as binary payloads will encode to
    * garbage when decoded as UTF-8.
    * 
    * @return the text payload to be sent with the frame
    */
   String getText();
   
   /**
    * This method is used to convert from one frame type to another. 
    * Converting a frame type is useful in scenarios such as when a
    * ping needs to respond to a pong or when it is more convenient
    * to send a text frame as binary.
    * 
    * @param type this is the frame type to convert to
    * 
    * @return a new frame using the specified frame type
    */
   Frame getFrame(FrameType type);
   
   /**
    * This is used to determine the type of frame. Interpretation of
    * this type is outlined in RFC 6455 and can be loosely categorised
    * as control frames and either data or binary frames.     
    * 
    * @return this returns the type of frame that this represents
    */
   FrameType getType();
}
