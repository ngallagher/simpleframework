/*
 * Lease.java May 2004
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
 * The <code>Lease</code> object is used to keep a keyed resource
 * active. This provides a very simple lease that can be used to
 * track the activity of a resource or system. Keeping track of
 * activity allows resources to be maintained until such time
 * that they are no longer required, allowing the server to clean
 * up any allocated memory, file descriptors, or other such data.
 *
 * @author Niall Gallagher 
 */
public interface Lease<T> {   
   
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
    * @exception Exception if the expiry could not be acquired     
    */
   long getExpiry(TimeUnit unit) throws LeaseException;   
   
   /**
    * This ensures that the leased resource is maintained for the
    * specified number of <code>TimeUnit</code> seconds. Allowing
    * the duration unit to be specified enables the lease system 
    * to maintain a resource with a high degree of accuracy. The
    * accuracy of the leasing system is dependent on how long it
    * takes to clean the resource associated with the lease.
    * 
    * @param duration this is the length of time to renew for
    * @param unit this is the time unit used for the duration
    * 
    * @exception Exception if the lease could not be renewed
    */
   void renew(long duration, TimeUnit unit) throws LeaseException;
   
   /**
    * This will cancel the lease and release the resource. This 
    * has the same effect as the <code>renew</code> method with
    * a zero length duration. Once this has been called the
    * <code>Cleaner</code> used should be notified immediately.
    * If the lease has already expired this throws an exception.
    *
    * @exception Exception if the expiry has been passed
    */
   void cancel() throws LeaseException;
   
   /**
    * Provides the key for the resource that this lease represents.
    * This can be used to identify the resource should the need
    * arise. Also, this provides a convenient means of identifying
    * leases when using or storing it as an <code>Object</code>.
    *
    * @return this returns the key for the resource represented
    */
   T getKey();  

}
