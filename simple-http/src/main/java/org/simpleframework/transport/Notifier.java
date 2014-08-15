/*
 * Notifier.java February 2009
 *
 * Copyright (C) 2009, Niall Gallagher <niallg@users.sf.net>
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

import org.simpleframework.transport.reactor.Operation;

/**
 * The <code>Notifier</code> object is a special type of operation
 * that is used to notify the kernel that there is an initialized
 * connection ready for processing. Typically an initialized socket
 * is one that has undergone the SSL handshake or is a raw byte
 * stream that does not require the SSL handshake.
 * 
 * @author Niall Gallagher
 */
interface Notifier extends Operation {
   
   /**
    * This method is executed when the operation is in a state
    * that is ready for execution. Typically for a notifier this 
    * is executed when there is data ready to read from the socket.
    */
   void run();
}
