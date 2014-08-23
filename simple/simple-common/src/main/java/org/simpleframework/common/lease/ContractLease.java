/*
 * ContractLease.java May 2004
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

import java.util.concurrent.TimeUnit;

/**
 * The <code>ContractLease</code> is used to maintain contracts by
 * using a controller object. This will invoke the controller with
 * the contract when a lease operation is performed. A lease is
 * renewed by changing the contract duration and passing that to
 * the controller which will reestablish the expiry time for it.
 *
 * @author Niall Gallagher 
 */
class ContractLease<T> implements Lease<T> {

   /**
    * This is the controller object used to handle contracts.
    */
   private final ContractController<T> handler;

   /**
    * This is the contract object representing the lease.
    */
   private final Contract<T> contract;

   /**
    * Constructor for the <code>ContractLease</code> object. This is
    * used to create a lease which will maintain a contract using a
    * controller object. Lease renewals are performed by changing the
    * expiry duration on the contract and notifying the controller.     
    *  
    * @param handler this is used to manage the contract expiration
    * @param contract this is the contract representing the lease
    */
   public ContractLease(ContractController<T> handler, Contract<T> contract) {
      this.handler = handler;
      this.contract = contract;      
   }   

   /**
    * Determines the duration remaining before the lease expires. 
    * The expiry is given as the number of <code>TimeUnit</code>
    * seconds remaining before the lease expires. If this value is 
    * negative it should be assumed that the lease has expired.
    *
    * @param unit this is the time unit used for the duration
    * 
    * @return the duration remaining within this lease instance
    *
    * @exception LeaseException if the lease expiry has passed
    */
   public long getExpiry(TimeUnit unit) throws LeaseException {
      return contract.getDelay(unit);           
   }

   /**
    * This ensures that the leased resource is maintained for the
    * specified number of <code>TimeUnit</code> seconds. Allowing
    * the duration unit to be specified enables the lease system 
    * to maintain a resource with a high degree of accuracy. The
    * accuracy of the leasing system is dependant on how long it
    * takes to clean the resource associated with the lease.
    * 
    * @param duration this is the length of time to renew for
    * @param unit this is the time unit used for the duration
    *
    * @exception LeaseException if the expiry has been passed
    */
   public void renew(long duration, TimeUnit unit) throws LeaseException {
      if(duration >= 0) {
         contract.setDelay(duration, unit);
      }
      handler.renew(contract);      
   }

   /**
    * This will cancel the lease and release the resource. This 
    * has the same effect as the <code>renew</code> method with
    * a zero length duration. Once this has been called the
    * <code>Cleaner</code> used should be notified immediately.
    * If the lease has already expired this throws an exception.
    *
    * @exception LeaseException if the expiry has been passed
    */
   public void cancel() throws LeaseException {      
      handler.cancel(contract);           
   }
   
   /**
    * Provides the key for the resource that this lease represents.
    * This can be used to identify the resource should the need
    * arise. Also, this provides a convenient means of identifying
    * leases when using or storing it as an <code>Object</code>.
    *
    * @return this returns the key for the resource represented
    */
   public T getKey() {
      return contract.getKey();
   }
}

