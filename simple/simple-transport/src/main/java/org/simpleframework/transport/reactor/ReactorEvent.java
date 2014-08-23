/*
 * ReactorEvent.java February 2007
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

package org.simpleframework.transport.reactor;

/**
 * The <code>ReactorEvent</code> enumeration is used for tracing the
 * operations that occur within the reactor. This is useful when the
 * performance of the system needs to be monitored or when there is a
 * resource or memory consumption issue that needs to be debugged.
 * 
 * @author Niall Gallagher
 */
public enum ReactorEvent {
   
   /**
    * This event indicates the registration of an I/O interest. 
    */
   SELECT,
   
   /**
    * This indicates that the selected I/O interest has not occurred.
    */
   SELECT_EXPIRED,
   
   /**
    * This occurs when a selection key is cancelled for all interests.
    */
   SELECT_CANCEL,
   
   /**
    * This is used to indicate the channel is already selecting.
    */
   ALREADY_SELECTING,
   
   /**
    * This occurs rarely however it indicates an invalid registration.
    */
   INVALID_KEY,
   
   /**
    * This occurs upon the initial registration of an I/O interest.
    */
   REGISTER_INTEREST,
   
   /**
    * This occurs upon the initial registration of a read I/O interest.
    */
   REGISTER_READ_INTEREST,
   
   /**
    * This occurs upon the initial registration of a write I/O interest.
    */
   REGISTER_WRITE_INTEREST,  
   
   /**
    * This is used to indicate the operation interest changed.
    */
   UPDATE_INTEREST,
   
   /**
    * This occurs upon the initial registration of a read I/O interest.
    */
   UPDATE_READ_INTEREST,
   
   /**
    * This occurs upon the initial registration of a write I/O interest.
    */
   UPDATE_WRITE_INTEREST,     
   
   /**
    * This indicates that the I/O interest has been satisfied.
    */
   INTEREST_READY,
   
   /**
    * This indicates that the I/O read interest has been satisfied.
    */
   READ_INTEREST_READY,   
   
   /**
    * This indicates that the I/O write interest has been satisfied.
    */
   WRITE_INTEREST_READY,      
   
   /**
    * This is the final action of executing the action.
    */
   EXECUTE_ACTION,   
   
   /**
    * This occurs on an attempt to register an closed channel.
    */
   CHANNEL_CLOSED,
   
   /**
    * This occurs when the selector has been shutdown globally.
    */
   CLOSE_SELECTOR,      
   
   /**
    * This occurs if there is an error with the selection.
    */
   ERROR,   
}
