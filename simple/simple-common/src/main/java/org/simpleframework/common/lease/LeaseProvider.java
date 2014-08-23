/*
 * LeaseProvider.java May 2004
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
 * The <code>LeaseProvider</code> is used to issue a lease for a
 * named resource. This is effectively used to issue a request
 * for a keyed resource to be released when a lease has expired.
 * The use of a <code>Lease</code> simplifies the interface to
 * the notification and also enables other objects to manage the
 * lease without any knowledge of the resource it represents.
 *
 * @author Niall Gallagher
 */
public interface LeaseProvider<T> {
   
   /**
    * This method will issue a <code>Lease</code> object that
    * can be used to manage the release of a keyed resource. If
    * the lease duration expires before it is renewed then the 
    * notification is sent, typically to a <code>Cleaner</code>
    * implementation, to signify that the resource should be
    * recovered. The issued lease can also be canceled.
    * 
    * @param key this is the key for the leased resource 
    * @param duration this is the duration of the issued lease
    * @param unit this is the time unit to issue the lease with
    *
    * @return a lease that can be used to manage notification
    */
   Lease<T> lease(T key, long duration, TimeUnit unit);

   /**
    * This is used to close the lease provider such that all of
    * the outstanding leases are canceled. This also ensures the
    * provider can no longer be used to issue new leases, such 
    * that further invocations of the <code>lease</code> method
    * will result in null leases. Once the provider has been 
    * closes all threads and other such resources are released.
    */ 
   void close();
}
