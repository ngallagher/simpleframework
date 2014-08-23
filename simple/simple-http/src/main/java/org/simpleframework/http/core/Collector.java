/*
 * Collector.java October 2002
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

package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.http.message.Entity;

/**
 * The <code>Collector</code> object is used to collect all of the
 * data used to form a request entity. This will collect the data
 * fragment by fragment from the underlying transport. When all
 * of the data is consumed and the entity is created and then it
 * is sent to the <code>Controller</code> object for processing. 
 * If the request has completed the next request can be collected
 * from the underlying transport using a new collector object.  
 * 
 * @author Niall Gallagher
 */
interface Collector extends Entity {

   /**
    * This is used to collect the data from a <code>Channel</code>
    * which is used to compose the entity. If at any stage there
    * are no ready bytes on the socket the controller provided can be
    * used to queue the collector until such time as the socket is
    * ready to read. Also, should the entity have completed reading
    * all required content it is handed to the controller as ready,
    * which processes the entity as a new client HTTP request.
    * 
    * @param controller this is the controller used to queue this
    */
   void collect(Controller controller) throws IOException;
}