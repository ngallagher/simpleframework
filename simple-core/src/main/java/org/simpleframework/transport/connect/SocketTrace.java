/*
 * SocketTrace.java February 2012
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

package org.simpleframework.transport.connect;

import org.simpleframework.transport.trace.Trace;

/**
 * The <code>SocketTrace</code> is used to wrap an trace for safety. 
 * Wrapping an trace in this way ensures that even if the trace is
 * badly written there is little chance that it will affect the
 * operation of the server. 
 * 
 * @author Niall Gallagher
 */
class SocketTrace implements Trace {
   
   /**
    * This is the actual trace that is being wrapped by this.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>SocketTrace</code> object. This will
    * create a trace object that wraps the one provided. If the
    * provided trace is null then this will simply ignore all events.
    * 
    * @param trace this is the trace that is to be wrapped by this
    */
   public SocketTrace(Trace trace) {
      this.trace = trace;
   }

   /**
    * This method is used to accept an event that occurred on the socket
    * associated with this trace. Typically the event is a symbolic
    * description of the event such as an enum or a string. 
    * 
    * @param event this is the event that occurred on the socket
    */
   public void trace(Object event) {
      if(trace != null) {
         trace.trace(event);
      }
   }

   /**
    * This method is used to accept an event that occurred on the socket
    * associated with this trace. Typically the event is a symbolic
    * description of the event such as an enum or a string. 
    * 
    * @param event this is the event that occurred on the socket
    * @param value provides additional information such as an exception
    */
   public void trace(Object event, Object value) {     
      if(trace != null) {
         trace.trace(event, value);
      }
   }      
}