/*
 * LeaseException.java May 2004
 *
 * Copyright (C) 2004, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.common.lease;

/**
 * The <code>LeaseException</code> is used to indicate that some
 * operation failed when using the lease after the lease duration 
 * has expired. Typically this will be thrown when the lease is 
 * renewed after the expiry period has passed.
 *
 * @author Niall Gallagher
 */
public class LeaseException extends RuntimeException {
   
   /**
    * This constructor is used if there is a description of the 
    * event that caused the exception required. This can be given
    * a message used to describe the situation for the exception.
    * 
    * @param message this is a description of the exception
    */
   public LeaseException(String template) {
      super(template);      
   }

   /**
    * This constructor is used if there is a description of the 
    * event that caused the exception required. This can be given
    * a message used to describe the situation for the exception.
    * 
    * @param message this is a description of the exception
    */
   public LeaseException(String template, Throwable cause) {
      super(template, cause);
   }
}
