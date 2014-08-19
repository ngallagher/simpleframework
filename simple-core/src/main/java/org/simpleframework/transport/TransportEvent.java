/*
 * TransportEvent.java October 2012
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

/**
 * The <code>TransportEvent</code> enum represents various events that
 * can occur with the transport. Events that are available here are
 * typically those that refer to low level I/O operations within the
 * server. If a <code>Trace</code> has been associated with the socket
 * connection then it will receive these events as they occur.
 * 
 * @author Niall Gallagher
 */
public enum TransportEvent {
   
   /**
    * This event represents a read operation on the underlying socket.
    */
   READ,
   
   /**
    * This event occurs when there is no more data available to read.
    */
   READ_WAIT,
   
   /**
    * This event represents a write operation on the underlying socket.
    */
   WRITE,
   
   /**
    * This event represents a write buffer operation on the underlying socket.
    */
   WRITE_BUFFER,  
   
   /**
    * This event occurs when no more data can be sent over the socket.
    */
   WRITE_WAIT,
   
   /**
    * This event occurs when a thread must wait for a write to finish.
    */
   WRITE_BLOCKING,
   
   /**
    * This event occurs with HTTPS when a new SSL handshake starts.
    */
   HANDSHAKE_BEGIN,   
   
   /**
    * This event occurs with HTTPS when a SSL handshake has finished.
    */
   HANDSHAKE_DONE,
   
   /**
    * This event occurs when a server challenges for an X509 certificate.
    */
   CERTIFICATE_CHALLENGE,
   
   /**
    * This event indicates that the handshake failed in some way.
    */
   HANDSHAKE_FAILED,
   
   /**
    * This event occurs when the underlying connection is terminated.
    */
   CLOSE,
   
   /**
    * This event occurs when there is an error with the transport.
    */
   ERROR
}
