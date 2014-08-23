/*
 * CloseCode.java February 2014
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
 * The <code>CloseCode</code> enumerates the closure codes specified in
 * RFC 6455. When closing an established connection an endpoint may
 * indicate a reason for closure. The interpretation of this reason by
 * an endpoint, and the action an endpoint should take given this reason,
 * are left undefined by RFC 6455. The specification defines a set of 
 * status codes and specifies which ranges may be used by extensions, 
 * frameworks, and end applications. The status code and any associated 
 * textual message are optional components of a Close frame.
 * 
 * @author niall.gallagher
 */
public enum CloseCode {
   
   /**
    * Indicates the purpose for the connection has been fulfilled.
    */
   NORMAL_CLOSURE(1000),
   
   /**
    * Indicates that the server is going down or the client browsed away.
    */
   GOING_AWAY(1001),
   
   /**
    * Indicates the connection is terminating due to a protocol error.
    */
   PROTOCOL_ERROR(1002),
   
   /**
    * Indicates the connection received a data type it cannot accept.
    */
   UNSUPPORTED_DATA(1003),
   
   /**
    * According to RFC 6455 this has been reserved for future use. 
    */
   RESERVED(1004),
   
   /**
    * Indicates that no status code was present and should not be used.
    */
   NO_STATUS_CODE(1005),
   
   /**
    * Indicates an abnormal closure and should not be used.
    */
   ABNORMAL_CLOSURE(1006),
   
   /**
    * Indicates that a payload was not consistent with the message type. 
    */
   INVALID_FRAME_DATA(1007),
   
   /**
    * Indicates an endpoint received a message that violates its policy.
    */
   POLICY_VIOLATION(1008),
   
   /**
    * Indicates that a payload is too big to be processed.
    */
   TOO_BIG(1009),
   
   /**
    * Indicates that the server did not negotiate an extension properly.
    */
   NO_EXTENSION(1010),
   
   /**
    * Indicates an unexpected error within the server.
    */  
   INTERNAL_SERVER_ERROR(1011),
   
   /**
    * Indicates a validation failure for TLS and should not be used.
    */
   TLS_HANDSHAKE_FAILURE(1015);  
   
   /**
    * This is the actual integer value representing the code.
    */
   public final int code;
   
   /**
    * This is the high order byte for the closure code.
    */
   public final int high;
   
   /**
    * This is the low order byte for the closure code.
    */
   public final int low;
   
   /**
    * Constructor for the <code>CloseCode</code> object. This is used
    * to create a closure code using one of the pre-defined values
    * within RFC 6455.
    * 
    * @param code this is the code that is to be used
    */
   private CloseCode(int code) {
      this.high = code & 0x0f;
      this.low = code & 0xf0;
      this.code = code;
   }
   
   /**
    * This is the data that represents the closure code. The array 
    * contains the high order byte and the low order byte as taken
    * from the pre-defined closure code.
    * 
    * @return a byte array representing the closure code
    */
   public byte[] getData() {
      return new byte[] { (byte)high, (byte)low };
   }
   
   
   public static CloseCode resolveCode(int high, int low) {     
      for(CloseCode code : values()) {
         if(code.high == high) {
            if(code.low == low) {
               return code;
            }
         }
      }
      return NO_STATUS_CODE;
   }
}
