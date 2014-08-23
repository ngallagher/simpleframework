/*
 * Trace.java October 2012
 *
 * Copyright (C) 2002, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.transport.trace;

/**
 * The <code>Trace</code> interface represents an trace log for various
 * connection events. A trace is not limited to low level I/O events
 * it can also gather event data that relates to protocol specific 
 * events. Using a trace in this manner enables problems to be solved
 * with connections as they arise.
 * <p>
 * When implementing a <code>Trace</code> there should be special 
 * attention paid to its affect on the performance of the server. The
 * trace is used deep within the core and any delays experienced in
 * the trace will be reflected in the performance of the server. 
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.trace.TraceAnalyzer
 */
public interface Trace {
   
   /**
    * This method is used to accept an event that occurred on the socket
    * associated with this trace. Typically the event is a symbolic
    * description of the event such as an enum or a string. 
    * 
    * @param event this is the event that occurred on the socket
    */
   void trace(Object event);
   
   /**
    * This method is used to accept an event that occurred on the socket
    * associated with this trace. Typically the event is a symbolic
    * description of the event such as an enum or a string. 
    * 
    * @param event this is the event that occurred on the socket
    * @param value provides additional information such as an exception
    */
   void trace(Object event, Object value);
}