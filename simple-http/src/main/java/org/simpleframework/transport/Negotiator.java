/*
 * Negotiator.java February 2007
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

import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>Negotiator</code> interface represents a special kind
 * of reactor which is used to perform negotiations. Negotiations
 * are performed on <code>Pipeline</code> objects as a means to
 * exchange data between the client and server that does not form
 * part of a HTTP request entity.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.Negotiation
 */
interface Negotiator extends Reactor {
   
   /**
    * Once the negotiation has completed this is used to perform
    * processing of the provided transport. Processing of the
    * transport is done only after the negotiation has completed.
    * The given transport is used to read and write to the socket.
    * 
    * @param transport this is the transport for the pipeline
    */
   void ready(Transport transport) throws IOException;         
}
