/*
 * PacketException.java February 2008
 *
 * Copyright (C) 2008, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.transport;

/**
 * The <code>PacketException</code> represents an exception that 
 * is thrown when there is a problem with a packet operation. This
 * is typically thrown when the packet is closed and an attempt
 * is made to either append or write the packets contents.
 * 
 * @author Niall Gallagher
 */
class PacketException extends TransportException {
   
   /**
    * Constructor for the <code>PacketException</code> object. This
    * creates an exception that takes a template string an a list
    * of arguments to pass in to the message template.
    * 
    * @param message this is the message template string to use
    */
   public PacketException(String message) {
      super(message);
   }
   
   /**
    * Constructor for the <code>PacketException</code> object. This
    * creates an exception that takes a template string an a list
    * of arguments to pass in to the message template.
    * 
    * @param message this is the message template string to use
    * @param cause this is the root cause of the exception
    */
   public PacketException(String message, Throwable cause) {
      super(message, cause);
   }
}
