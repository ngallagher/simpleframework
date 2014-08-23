/*
 * Negotiation.java February 2007
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

import org.simpleframework.transport.reactor.Operation;

/**
 * The <code>Negotiation</code> interface is used to represent an
 * SSL negotiation. When an operation can not be completed this
 * will allow a task to perform asynchronous operations and resume
 * the negotiation when those operations can be fulfilled.
 * 
 * @author Niall Gallagher
 */
interface Negotiation extends Operation  {
   
   /**
    * This is used to resume the negotiation when an operation
    * has completed. This will continue the decrypt and encrypt
    * sequence of messages required to fulfil the negotiation.
    */
   void resume() throws IOException;

   /**
    * This method is invoked when the negotiation is done and
    * the next phase of the connection is to take place. This
    * will typically be invoked when an SSL handshake or
    * termination exchange has completed successfully. 
    */
   void commit() throws IOException;     
   
   /**
    * This is used to send any messages the negotiation may have.
    * If the negotiation can not send the information during its
    * execution then this method will be executed when a select
    * operation is signaled.
    * 
    * @return this returns true when the message has been sent
    */
   boolean send() throws IOException;   
   
   /**
    * This is used to receive data from the other side. If at any
    * point during the negotiation a message is required that
    * can not be read immediately this is used to asynchronously
    * read the data when a select operation is signaled.
    *  
    * @return this returns true when the message has been read
    */
   boolean receive() throws IOException; 
}
