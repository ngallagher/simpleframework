/*
 * ExecuteAction.java February 2007
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
 * The <code>ExecuteAction</code> object is represents an action 
 * that the distributor is to process. This contains the operation 
 * and the required I/O events as an integer bit mask as well as the
 * selectable channel used to register for selection. In order to
 * ensure that the action does not remain within the distributor for
 * too long the action has an expiry time.
 * 
 * @author Niall Gallagher
 */
class ExecuteAction implements Action {

   /**
    * The task to execute when the required operations is ready.
    */
   private final Operation task;

   /**
    * This is the bit mask of required operations to be executed.
    */
   private final int require;
   
   /**
    * This is the time in the future that the event will expire in.
    */
   private final long expiry;

   /**
    * Constructor for the <code>Event</code> object. The actions are
    * used to encapsulate the task to execute and the operations
    * to listen to when some action is to be performed.
    * 
    * @param task this is the task to be executed when it is ready
    * @param require this is the required operations to listen to
    */
   public ExecuteAction(Operation task, int require, long expiry) {
      this.expiry = System.currentTimeMillis() + expiry;
      this.require = require;
      this.task = task;
   }  
   
   /**
    * This is used to execute the operation for the action. This will
    * be executed when the interested I/O event is ready for the
    * associated <code>SelectableChannel</code> object. If the action
    * expires before the interested I/O operation is ready this will
    * not be executed, instead the operation is canceled. 
    */
   public void run() {
      task.run();
   }

   /**
    * This is used to get the expiry for the operation. The expiry
    * represents some static time in the future when the action will
    * expire if it does not become ready. This is used to cancel the
    * operation so that it does not remain in the distributor.
    *
    * @return the remaining time this operation will wait for
    */        
   public long getExpiry() {
      return expiry;
   }

   /**
    * This is the <code>SelectableChannel</code> which is used to 
    * determine if the operation should be executed. If the channel   
    * is ready for a given I/O event it can be run. For instance if
    * the operation is used to perform some form of read operation
    * it can be executed when ready to read data from the channel.
    *
    * @return this returns the channel used to govern execution
    */    
   public SelectableChannel getChannel() {
      return task.getChannel();
   }
   
   /**
    * This is used to acquire the <code>Operation</code> that is to
    * be executed when the required operations are ready. It is the
    * responsibility of the distributor to invoke the operation.
    * 
    * @return the operation to be executed when it is ready
    */
   public Operation getOperation() {
      return task;
   }

   /**
    * This returns the I/O operations that the action is interested
    * in as an integer bit mask. When any of these operations are
    * ready the distributor will execute the provided operation. 
    * 
    * @return the integer bit mask of interested I/O operations
    */
   public int getInterest() {
      return require;
   }
}