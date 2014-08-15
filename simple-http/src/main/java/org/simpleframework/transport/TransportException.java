/*
 * TransportException.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

import java.io.IOException;

/**
 * The <code>TransportException</code> object is thrown when there 
 * is a problem with the transport. Typically this is done thrown if
 * there is a problem reading or writing to the transport.
 * 
 * @author Niall Gallagher
 */
public class TransportException extends IOException {
   
   /**
    * Constructor for the <code>TransportException</code> object. If
    * there is a problem sending or reading from a transport then it
    * will throw a transport exception to report the error.
    * 
    * @param message this is the message associated with the error
    */
   public TransportException(String message) {
      super(message);
   }
   
   /**
    * Constructor for the <code>TransportException</code> object. If
    * there is a problem sending or reading from a transport then it
    * will throw a transport exception to report the error.
    * 
    * @param message this is the message associated with the error
    * @param cause this is the cause of the producer exception
    */
   public TransportException(String message, Throwable cause) {
      super(message);
      initCause(cause);
   }
}
