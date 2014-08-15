/*
 * DataFrame.java February 2014
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
 * The <code>DataFrame</code> object represents a frame as defined in
 * RFC 6455. A frame is a very lightweight envelope used to send 
 * control information and either text or binary user data. Typically
 * a frame will represent a single message however, it is possible 
 * to fragment a single frame up in to several frames. A fragmented
 * frame has a specific <code>FrameType</code> indicating that it
 * is a continuation frame.   
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.Data
 */
public class DataFrame implements Frame {

   /**
    * This is the type used to determine the intent of the frame.
    */
   private final FrameType type;
   
   /**
    * This contains the payload to be sent with the frame.
    */
   private final Data data;
   
   /**
    * This determines if the frame is the last of a sequence.
    */
   private final boolean last;

   /**
    * Constructor for the <code>DataFrame</code> object. This is used
    * to create a frame using the specified data and frame type. A
    * zero payload is created using this constructor and is suitable
    * only for specific control frames such as connection termination.
    * 
    * @param type this is the frame type used for this instance
    */
   public DataFrame(FrameType type) {
      this(type, new byte[0]);
   }
   
   /**
    * Constructor for the <code>DataFrame</code> object. This is used
    * to create a frame using the specified data and frame type. In
    * some cases a control frame may require a zero length payload.
    * 
    * @param type this is the frame type used for this instance
    * @param data this is the payload for this frame
    */
   public DataFrame(FrameType type, byte[] data) {
      this(type, data, true);
   }
   
   /**
    * Constructor for the <code>DataFrame</code> object. This is used
    * to create a frame using the specified data and frame type. In
    * some cases a control frame may require a zero length payload.
    * 
    * @param type this is the frame type used for this instance
    * @param data this is the payload for this frame
    * @param last true if this is not a fragment in a sequence
    */
   public DataFrame(FrameType type, byte[] data, boolean last) {
      this(type, new BinaryData(data), last);
   }   
   
   /**
    * Constructor for the <code>DataFrame</code> object. This is used
    * to create a frame using the specified data and frame type. In
    * some cases a control frame may require a zero length payload.
    * 
    * @param type this is the frame type used for this instance
    * @param data this is the payload for this frame
    */
   public DataFrame(FrameType type, String text) {
      this(type, text, true);
   }
   
   /**
    * Constructor for the <code>DataFrame</code> object. This is used
    * to create a frame using the specified data and frame type. In
    * some cases a control frame may require a zero length payload.
    * 
    * @param type this is the frame type used for this instance
    * @param data this is the payload for this frame
    * @param last true if this is not a fragment in a sequence
    */
   public DataFrame(FrameType type, String text, boolean last) {
      this(type, new TextData(text), last);
   } 
   
   /**
    * Constructor for the <code>DataFrame</code> object. This is used
    * to create a frame using the specified data and frame type. In
    * some cases a control frame may require a zero length payload.
    * 
    * @param type this is the frame type used for this instance
    * @param data this is the payload for this frame
    */
   public DataFrame(FrameType type, Data data) {
      this(type, data, true);
   }
   
   /**
    * Constructor for the <code>DataFrame</code> object. This is used
    * to create a frame using the specified data and frame type. In
    * some cases a control frame may require a zero length payload.
    * 
    * @param type this is the frame type used for this instance
    * @param data this is the payload for this frame
    * @param last true if this is not a fragment in a sequence
    */
   public DataFrame(FrameType type, Data data, boolean last) {
      this.data = data;
      this.type = type;
      this.last = last;
   }   
   
   /**
    * This is used to determine if the frame is the final frame in
    * a sequence of fragments or a whole frame. If this returns false
    * then the frame is a continuation from from a sequence of 
    * fragments, otherwise it is a whole frame or the last fragment.
    * 
    * @return this returns false if the frame is a fragment
    */
   public boolean isFinal() {
      return last;
   }   
   
   /**
    * This returns the binary payload that is to be sent with the frame.
    * It contains no headers or other meta data. If the original data
    * was text this converts it to UTF-8.
    * 
    * @return the binary payload to be sent with the frame
    */
   public byte[] getBinary() {
      return data.getBinary();
   }
   
   /**
    * This returns the text payload that is to be sent with the frame. 
    * It contains no header information or meta data. Caution should 
    * be used with this method as binary payloads will encode to
    * garbage when decoded as UTF-8.
    * 
    * @return the text payload to be sent with the frame
    */
   public String getText(){ 
      return data.getText();
   }
   
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
   public Frame getFrame(FrameType type) {
      return new DataFrame(type, data, last);
   }
   
   /**
    * This is used to determine the type of frame. Interpretation of
    * this type is outlined in RFC 6455 and can be loosely categorised
    * as control frames and either data or binary frames.     
    * 
    * @return this returns the type of frame that this represents
    */
   public FrameType getType(){
      return type;
   }
   
   /**
    * This returns the text payload that is to be sent with the frame. 
    * It contains no header information or meta data. Caution should 
    * be used with this method as binary payloads will encode to
    * garbage when decoded as UTF-8.
    * 
    * @return the text payload to be sent with the frame
    */
   @Override
   public String toString() {
      return getText();
   }
}
