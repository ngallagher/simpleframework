/*
 * LeaseMap.java May 2004
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

import java.util.concurrent.ConcurrentHashMap;

/**
 * The <code>LeaseMap</code> object is used to map lease keys to the
 * lease objects managing those objects. This allows components that
 * are using the leasing framework to associate an object with its
 * lease and vice versa. Such a capability enables lease renewals to
 * be performed without the need for a direct handle on the lease.
 * 
 * @author Niall Gallagher
 */
public class LeaseMap<T> extends ConcurrentHashMap<T, Lease<T>> {
   
   /**
    * Constructor for the <code>LeaseMap</code> object. This will
    * create a map for mapping leased resource keys to the leases
    * that manage them. Having such a map allows leases to be 
    * maintained without having a direct handle on the lease.
    */
   public LeaseMap() {
      super();
   }
   
   /**
    * Constructor for the <code>LeaseMap</code> object. This will
    * create a map for mapping leased resource keys to the leases
    * that manage them. Having such a map allows leases to be 
    * maintained without having a direct handle on the lease.
    * 
    * @param capacity this is the initial capacity of the map
    */
   public LeaseMap(int capacity) {
      super(capacity);
   }
   
   /**
    * This is used to acquire the <code>Lease</code> object that is
    * mapped to the specified key. Overriding this method ensures
    * that even without generic parameters a type safe method for
    * acquiring the registered lease objects can be used.
    * 
    * @param key this is the key used to acquire the lease object
    * 
    * @return this is the lease that is associated with the key
    */
   public Lease<T> get(Object key) {
      return super.get(key);
   }
   
   /**
    * This is used to remove the <code>Lease</code> object that is
    * mapped to the specified key. Overriding this method ensures
    * that even without generic parameters a type safe method for
    * removing the registered lease objects can be used.
    * 
    * @param key this is the key used to remove the lease object
    * 
    * @return this is the lease that is associated with the key
    */
   public Lease<T> remove(Object key) {
      return super.remove(key);
   }
}
