/*
 * TerminateException.java February 2007
 *
 * Copyright (C) 2001, Niall Gallagher <niallg@users.sf.net>
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
 * The <code>TerminateException</code> object is used to represent an
 * exception that is thrown when there is a problem terminating the
 * server. Typically this is thrown by any service that has had a
 * problem responding to a request to terminate the server.
 * 
 * @author Niall Gallagher
 */
public class TerminateException extends IOException {
   
   /**
    * Constructor for the <code>TerminateException</code> object. This
    * is used to represent an exception that is thrown when attempting
    * to terminate the server fails for some reason.
    * 
    * @param message this is the message describing the exception
    */
   public TerminateException(String message) {
      super(message);
   }
   
   /**
    * Constructor for the <code>TerminateException</code> object. This
    * is used to represent an exception that is thrown when attempting
    * to terminate the server fails for some reason.
    * 
    * @param message this is the message describing the exception
    * @param cause this is the cause of the termination exception
    */
   public TerminateException(String message, Throwable cause) {
      super(message);
      initCause(cause);
   }
}