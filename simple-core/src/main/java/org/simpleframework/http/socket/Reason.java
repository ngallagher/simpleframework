/*
 * Reason.java February 2014
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
 * The <code>Reason</code> object is used to hold a textual reason
 * for connection closure and an RFC 6455 defined code. When a 
 * connection is to be closed a control frame with an opcode of
 * close is sent with the text reason, if one is provided.
 * 
 * @author Niall Gallagher
 */
public class Reason {

   /**
    * This is the close code to be sent with a control frame.
    */
   private final CloseCode code;
   
   /**
    * This is the textual description of the close reason.
    */
   private final String text;

   /**
    * Constructor for the <code>Reason</code> object. This is used
    * to create a reason and a textual description of that reason
    * to be delivered as a control frame. 
    * 
    * @param code this is the code to be sent with the frame
    */
   public Reason(CloseCode code) {
      this(code, null);
   }
   
   /**
    * Constructor for the <code>Reason</code> object. This is used
    * to create a reason and a textual description of that reason
    * to be delivered as a control frame. 
    * 
    * @param code this is the code to be sent with the frame
    * @param text this is textual description of the close reason 
    */
   public Reason(CloseCode code, String text) {
      this.code = code;
      this.text = text;
   }
   
   /**
    * This is used to get the RFC 6455 code describing the type
    * of close event. It is the code that should be used by
    * applications to determine why the connection was terminated.
    * 
    * @return returns the close code for the connection
    */
   public CloseCode getCode() {
      return code;
   }
   
   /**
    * This is used to get the textual description for the closure.
    * In many scenarios there will be no textual reason as it is
    * an optional attribute.
    * 
    * @return this returns the description for the closure
    */
   public String getText() {
      return text;
   }
   
   /**
    * This is used to provide a textual representation of the reason.
    * For consistency this will only return the enumerated value for
    * the close code, or if none exists a "null" text string.
    * 
    * @return this returns a string representation of the reason
    */
   public String toString() {
      return String.valueOf(code);
   }
}
