/*
 * ContractQueue.java May 2004
 *
 * Copyright (C) 2004, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.common.lease;

import java.util.concurrent.DelayQueue;

/**
 * The <code>ContraceQueue</code> object is used to queue contracts
 * between two asynchronous threads of execution. This allows the
 * controller to schedule the lease contract for expiry. Taking the
 * contracts from the queue is delayed for the contract duration.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.common.lease.Contract
 */
class ContractQueue<T> extends DelayQueue<Contract<T>> {
   
   /**
    * Constructor for the <code>ContractQueue</code> object. This
    * is used to create a queue for passing contracts between two
    * asynchronous threads of execution. This is used by the 
    * lease controller to schedule the lease contract for expiry.    
    */
    public  ContractQueue() {
      super();
    }
}
