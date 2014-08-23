/*
 * ResponseException.java February 2007
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

package org.simpleframework.http.core;

import java.io.IOException;

/**
 * The <code>ResponseException</code> object is used to represent an
 * exception that is thrown when there is a problem producing the
 * response body. This can be used to wrap <code>IOException</code>
 * objects that are thrown from the underlying transport.
 * 
 * @author Niall Gallagher
 */
class ResponseException extends IOException {
   
   /**
    * Constructor for the <code>ResponseException</code> object. This
    * is used to represent an exception that is thrown when producing
    * the response body. The producer exception is an I/O exception
    * and thus exceptions can propagate out of stream methods.
    * 
    * @param message this is the message describing the exception
    */
   public ResponseException(String message) {
      super(message);
   }
   
   /**
    * Constructor for the <code>ResponseException</code> object. This
    * is used to represent an exception that is thrown when producing
    * the response body. The producer exception is an I/O exception
    * and thus exceptions can propagate out of stream methods.
    * 
    * @param message this is the message describing the exception
    * @param cause this is the cause of the producer exception
    */
   public ResponseException(String message, Throwable cause) {
      super(message);
      initCause(cause);
   }
}
