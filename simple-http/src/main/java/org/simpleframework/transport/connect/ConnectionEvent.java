/*
 * ConnectionEvent.java October 2012
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

/**
 * The <code>ConnectionEvent</code> enum represents various events that
 * can occur with a new connection. When a new connection is accepted
 * then the accept event is dispatched to a <code>Trace</code> object
 * if one has been associated with the connection. 
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.trace.Trace
 */
public enum ConnectionEvent {
   
   /**
    * This event occurs when the server accepts a new connection.
    */
   ACCEPT,
   
   /**
    * This event occurs when there is an error with the connection.
    */
   ERROR
}
