/*
 * Action.java February 2007
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

import java.nio.channels.SelectableChannel;

/**
 * The <code>Action</code> object is used to represent an action that
 * the distributor is to process. This contains the operation and 
 * the required I/O events as an integer bit mask. When an operation
 * is considered ready it will be handed to an executor to execute.
 * 
 * @author Niall Gallagher
 */
interface Action extends Runnable {

   /**
    * This is used to get the expiry for the operation. The expiry
    * represents some static time in the future when the action will
    * expire if it does not become ready. This is used to cancel the
    * operation so that it does not remain in the distributor.
    *
    * @return the remaining time this operation will wait for
    */         
   long getExpiry();  
   
   /**
    * This returns the I/O operations that the action is interested
    * in as an integer bit mask. When any of these operations are
    * ready the distributor will execute the provided operation. 
    * 
    * @return the integer bit mask of interested I/O operations
    */
   int getInterest();

   /**
    * This is the <code>SelectableChannel</code> which is used to 
    * determine if the operation should be executed. If the channel   
    * is ready for a given I/O event it can be run. For instance if
    * the operation is used to perform some form of read operation
    * it can be executed when ready to read data from the channel.
    *
    * @return this returns the channel used to govern execution
    */    
   SelectableChannel getChannel();

   /**
    * This is used to acquire the <code>Operation</code> that is to
    * be executed when the required operations are ready. It is the
    * responsibility of the distributor to invoke the operation.
    * 
    * @return the operation to be executed when it is ready
    */
   Operation getOperation();
}
