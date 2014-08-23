/*
 * ContractController.java May 2004
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

import org.simpleframework.common.lease.LeaseException;

/**
 * The <code>ContractController</code> forms the interface to the 
 * lease management system. There are two actions permitted for 
 * leased resources, these are lease issue and lease renewal. When 
 * the lease is first issued it is scheduled for the contract
 * duration. Once issued the lease can be renewed with another
 * duration, which can be less than the previous duration used.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.common.lease.ContractMaintainer
 */
interface ContractController<T> {
   
   /**
    * This method will establish a contract for the given duration.
    * If the contract duration expires before it is renewed then a
    * notification is sent, typically to a <code>Cleaner</code> to
    * to signify that the resource should be released. The contract
    * can also be cancelled by providing a zero length duration.
    * 
    * @param contract a contract representing a leased resource
    *
    * @exception Exception if the lease could not be done
    */   
   void issue(Contract<T> contract) throws LeaseException;
   
   /**
    * This ensures that the contract is renewed for the duration on
    * the contract, which may have changed since it was issued or
    * last renewed. If the duration on the contract has changed this
    * will insure the previous contract duration is revoked and the 
    * new duration is used to maintain the leased resource.
    *
    * @param contract a contract representing a leased resource
    *
    * @exception Exception if the lease could not be done
    */
   void renew(Contract<T> contract) throws LeaseException;   
   
   /**
    * This will cancel the lease and release the resource. This 
    * has the same effect as the <code>renew</code> method with
    * a zero length duration. Once this has been called the
    * <code>Cleaner</code> used should be notified immediately.
    * If the lease has already expired this throws an exception.
    *
    * @param contract a contract representing a leased resource
    *
    * @exception Exception if the expiry has been passed
    */
   void cancel(Contract<T> contract) throws LeaseException;
   
   /**
    * This method is used to cancel all outstanding leases and to
    * close the controller. Closing the controller ensures that it
    * can no longer be used to issue or renew leases. All resources
    * occupied by the controller are released, including threads,
    * memory, and all leased resources occupied by the instance.  
    */
   void close();
}
