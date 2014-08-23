/*
 * CancelAction.java February 2007
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
 * The <code>CancelAction</code> object is used to represent a task
 * that can be executed to cancel an operation. This is used in the
 * place of a normal <code>Operation</code> to pass for execution
 * when the operation has expired before the I/O event is was
 * interested in occurred. Before this is executed the operation is
 * removed from selection.
 * 
 * @author Niall Gallagher
 */      
class CancelAction implements Action {
   
   /**
    * This is the operation that is to be canceled by this action.
    */
   private final Operation task;
   
   /**
    * This is the operation object that is to be canceled.
    */
   private final Action action;
   
   /**
    * Constructor for the <code>Cancellation</code> object. This is
    * used to create a runnable task that delegates to the cancel
    * method of the operation. This will be executed asynchronously
    * by the executor after being removed from selection. 
    * 
    * @param action this is the task that is to be canceled by this
    */
   public CancelAction(Action action) {
      this.task = action.getOperation();
      this.action = action;
   }
   
   /**
    * This method is executed by the <code>Executor</code> object 
    * if the operation expires before the required I/O event(s)
    * have occurred. It is typically used to shutdown the socket
    * and release any resources associated with the operation.
    */
   public void run() {
      task.cancel();
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
      return 0;
   }
   
   /**
    * This returns the I/O operations that the action is interested
    * in as an integer bit mask. When any of these operations are
    * ready the distributor will execute the provided operation. 
    * 
    * @return the integer bit mask of interested I/O operations
    */
   public int getInterest() {
      return action.getInterest();
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
      return action.getChannel();
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
}