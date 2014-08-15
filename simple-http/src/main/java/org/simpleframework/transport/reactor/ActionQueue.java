/*
 * ActionQueue.java February 2007
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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The <code>ActionQueue</code> object is used to queue actions for
 * selection. This is used by the reactor to queue actions that are 
 * to be executed on a given I/O event. It allows actions to be
 * queued in such a way that the caller does not block.
 *
 * @author Niall Gallagher
 */ 
class ActionQueue extends ConcurrentLinkedQueue<Action> {

   /**
    * Constructor for the <code>ActionQueue</code> object. This is 
    * used to create a non-blocking queue to schedule actions for
    * execution. This allows any number of actions to be inserted
    * for selection so the associated channels can be registered.
    */
   public ActionQueue() {
      super();          
   }        
}


