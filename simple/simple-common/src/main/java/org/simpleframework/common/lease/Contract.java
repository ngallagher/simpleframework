/*
 * Contract.java May 2004
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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A <code>Contract</code> is used to represent the contract a
 * lease has been issued. This contains all relevant information
 * regarding the lease, such as the keyed resource that has been 
 * leased and the duration of the lease. Delays for the contract
 * can be measured in any <code>TimeUnit</code> for convinienct.
 *
 * @author Niall Gallagher
 */
interface Contract<T> extends Delayed {
   
   /**
    * This returns the key for the resource this represents. 
    * This is used when the contract has expired to clean resources
    * associated with the lease. It is passed in to the cleaner as
    * an parameter to the callback. The cleaner is then responsible
    * for cleaning any resources associated with the lease.    
    *
    * @return returns the resource key that this represents
    */
   T getKey();
   
   /**
    * This method will return the number of <code>TimeUnit</code>
    * seconds that remain in the contract. If the value returned is 
    * less than or equal to zero then it should be assumed that the 
    * lease has expired, if greater than zero the lease is active.
    *
    * @return returns the duration in time unit remaining
    */
   long getDelay(TimeUnit unit);  
   
   /**
    * This method is used to set the number of <code>TimeUnit</code>
    * seconds that should remain within the contract. This is used
    * when the contract is to be reissued. Once a new duration has
    * been set the contract for the lease has been changed and the
    * previous expiry time is ignores, so only one clean is called.
    * 
    * @param delay this is the delay to be used for this contract
    * @param unit this is the time unit measurment for the delay
    */
   void setDelay(long delay, TimeUnit unit);  
   
   /**
    * This is used to provide a description of the contract that the
    * instance represents. A description well contain the key owned
    * by the contract as well as the expiry time expected for it.
    * This is used to provide descriptive messages in the exceptions.
    * 
    * @return a descriptive message describing the contract object
    */
   String toString();
}
