/*
 * ContractMaintainer.java May 2004
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The <code>ContractMaintainer</code> is used provide a controller
 * uses a cleaner. This simple delegates to the cleaner queue when 
 * a renewal is required. Renewals are performed by revoking the 
 * contract and then reissuing it. This will ensure that the delay
 * for expiry of the contract is reestablished within the queue.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.common.lease.LeaseCleaner
 */
class ContractMaintainer<T> implements ContractController<T> {       

   /**
    * The queue that is used to issue and revoke contracts.
    */
   private final LeaseCleaner<T> queue;

   /**
    * Constructor for the <code>ContractMaintainer</code> object. This 
    * is used to create a controller for contracts which will ensure
    * that the lease expiry durations are met. All notifications of
    * expiry will be delivered to the provided cleaner instance.
    * 
    * @param cleaner this is used to receive expiry notifications
    */
   public ContractMaintainer(Cleaner<T> cleaner) {
      this.queue = new LeaseCleaner<T>(cleaner);
   }
   
   /**
    * This method will establish a contract for the given duration.
    * If the contract duration expires before it is renewed then a
    * notification is sent, typically to a <code>Cleaner</code> to
    * to signify that the resource should be released. The contract
    * can also be cancelled by providing a zero length duration.
    * 
    * @param contract a contract representing a leased resource
    */  
   public synchronized void issue(Contract<T> contract) {
      queue.issue(contract);
   }

   /**
    * This ensures that the contract is renewed for the duration on
    * the contract, which may have changed since it was issued or
    * last renewed. If the duration on the contract has changed this
    * will insure the previous contract duration is revoked and the 
    * new duration is used to maintain the leased resource.
    *
    * @param contract a contract representing a leased resource
    */
   public synchronized void renew(Contract<T> contract) {
      boolean active = queue.revoke(contract);              

      if(!active) {
         throw new LeaseException("Lease has expired for " + contract);           
      }
      queue.issue(contract); 
   }
   
   /**
    * This will cancel the lease and release the resource. This 
    * has the same effect as the <code>renew</code> method with
    * a zero length duration. Once this has been called the
    * <code>Cleaner</code> used should be notified immediately.
    * If the lease has already expired this throws an exception.
    *
    * @param contract a contract representing a leased resource
    */
   public synchronized void cancel(Contract<T> contract) {
      boolean active = queue.revoke(contract);              
 
      if(!active) {
         throw new LeaseException("Lease has expired for " + contract);           
      }
      contract.setDelay(0, MILLISECONDS);
      queue.issue(contract); 
   }
   
   /**
    * This method is used to cancel all outstanding leases and to
    * close the controller. Closing the controller ensures that it
    * can no longer be used to issue or renew leases. All resources
    * occupied by the controller are released, including threads,
    * memory, and all leased resources occupied by the instance.   
    * 
    * @throws LeaseException if the controller can not be closed
    */
   public synchronized void close() {
      queue.close();
   }
}
