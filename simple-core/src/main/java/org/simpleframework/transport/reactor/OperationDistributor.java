/*
 * Distributor.java February 2007
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

import java.io.IOException;

/**
 * The <code>Distributor</code> object is used to execute operations
 * that have an interested I/O event ready. This acts much like a
 * scheduler would in that it delays the execution of the operations
 * until such time as the associated <code>SelectableChannel</code>
 * has an interested I/O event ready.
 * <p>
 * This distributor has two modes, one mode is used to cancel the
 * channel once an I/O event has occurred. This means that the channel
 * is removed from the <code>Selector</code> so that the selector 
 * does not break when asked to select again. Canceling the channel
 * is useful when the operation execution may not fully read the 
 * payload or when the operation takes a significant amount of time.
 *
 * @see org.simpleframework.transport.reactor.ActionDistributor
 */ 
interface OperationDistributor {
   
   /**
    * This is used to process the <code>Operation</code> object. This
    * will wake up the selector if it is currently blocked selecting
    * and register the operations associated channel. Once the 
    * selector is awake it will acquire the operation from the queue
    * and register the associated <code>SelectableChannel</code> for
    * selection. The operation will then be executed when the channel
    * is ready for the interested I/O events.
    * 
    * @param task this is the task that is scheduled for distribution   
    * @param require this is the bit-mask value for interested events
    */ 
   void process(Operation task, int require) throws IOException;
   
   /**
    * This is used to close the distributor such that it cancels all
    * of the registered channels and closes down the selector. This
    * is used when the distributor is no longer required, after the
    * close further attempts to process operations will fail.
    */ 
   void close() throws IOException;   
}
