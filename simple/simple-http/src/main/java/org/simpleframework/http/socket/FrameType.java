/*
 * FrameType.java February 2014
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
 * The <code>FrameType</code> represents the set of opcodes defined 
 * in RFC 6455. The base framing protocol uses a opcode to define the
 * interpretation of the payload data for the frame.   
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.Frame
 */
public enum FrameType {
   
   /**
    * A continuation frame identifies a fragment from a larger message.
    */
   CONTINUATION(0x00),
   
   /**
    * A text frame identifies a message that contains UTF-8 text data.
    */
   TEXT(0x01),
   
   /**
    * A binary frame identifies a message that contains binary data.
    */
   BINARY(0x02),
   
   /**
    * A close frame identifies a frame used to terminate a connection.
    */
   CLOSE(0x08),
   
   /**
    * A ping frame is a heartbeat used to determine connection health.
    */
   PING(0x09),
   
   /**
    * A pong frame is sent is sent in response to a ping frame.
    */
   PONG(0x0a);
   
   /**
    * This is the integer value for the opcode.
    */
   public final int code;
   
   /**
    * Constructor for the <code>Frame</code> type enumeration. This is
    * given the opcode that is used to identify a specific frame type.
    * 
    * @param code this is the opcode representing the frame type
    */
   private FrameType(int code) {
      this.code = code;
   }
   
   /**
    * This is used to determine if a frame is a text frame. It can be
    * useful to know if a frame is a user based frame as it reduces
    * the need to convert from or to certain character sets.     
    * 
    * @return this returns true if the frame represents a text frame
    */
   public boolean isText() {
      return this == TEXT;
   }
   
   /**
    * This is used to determine if a frame is a close frame. A close
    * frame contains an optional payload, which if present contains
    * an error code in network byte order in the first two bytes, 
    * followed by an optional UTF-8 text reason of the closure.
    * 
    * @return this returns true if the frame represents a close frame
    */
   public boolean isClose() {
      return this == CLOSE;
   }
   
   /**
    * This is used to determine if a frame is a pong frame. A pong 
    * frame is sent in response to a ping and is used to determine if
    * a WebSocket connection is still active and healthy.
    * 
    * @return this returns true if the frame represents a pong frame
    */
   public boolean isPong() {
      return this == PONG;
   }      
   
   /**
    * This is used to determine if a frame is a ping frame. A ping 
    * frame is sent to check if a WebSocket connection is still healthy.
    * A connection is determined healthy if it responds with a pong
    * frame is a reasonable length of time.
    * 
    * @return this returns true if the frame represents a ping frame
    */
   public boolean isPing() {
      return this == PING;
   }
   
   /**
    * This is used to acquire the frame type given an opcode. If no
    * frame type can be determined from the opcode provided then this
    * will return a null value.
    * 
    * @param octet this is the octet representing the opcode
    * 
    * @return this returns the frame type from the opcode
    */
   public static FrameType resolveType(int octet) {
      int value = octet & 0xff;
      
      for(FrameType code : values()) {
         if(code.code == value) {
            return code;
         }
      }
      return null;
   }
}
