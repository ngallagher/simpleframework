/*
 * ActionMap.java February 2007
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

import java.nio.channels.Channel;
import java.util.LinkedHashMap;

/**
 * The <code>ActionMap</code> object is used to store action sets
 * using a given channel. This is used to determine which of
 * the registered operations has been executed, and thus should be
 * removed from the selector so that it does not break on further
 * selections of the interested operations.
 * 
 * @author Niall Gallagher
 */ 
class ActionMap extends LinkedHashMap<Channel, ActionSet> {
 
   /**
    * Constructor for the <code>ActionMap</code> object. This is
    * used to create a map for channels to action sets. This will
    * allow the action sets that need to be canceled quickly to
    * be retrieved using the associated channel object.
    */
   public ActionMap() {
      super();
   }
}
