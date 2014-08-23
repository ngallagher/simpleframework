/*
 * Expiration.java May 2004
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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A <code>Expiration</code> is used to represent the expiration
 * for a lease. This contains all relevant information for the
 * the lease, such as the keyed resource that has been leased and 
 * the duration of the lease. Durations for the contract can be 
 * measured in any <code>TimeUnit</code> for convenience.
 *
 * @author Niall Gallagher
 */
class Expiration<T> implements Contract<T> {

   /**
    * This is the expiration time in nanoseconds for this.
    */
   private volatile long time;
   
   /**
    * This is the key representing the resource being lease.
    */   
   private T key;
   
   /**
    * Constructor for the <code>Expiration</code> object. This is used
    * to create a contract with an initial expiry period. Once this
    * is created the time is taken and the contract can be issued.    
    * 
    * @param key this is the key that this contract represents
    * @param lease this is the initial lease duration to be used
    * @param scale this is the time unit scale that is to be used
    */   
   public Expiration(T key, long lease, TimeUnit scale) {
      this.time = getTime() + scale.toNanos(lease);
      this.key = key;
   }

   /**
    * This returns the key for the resource this represents. 
    * This is used when the contract has expired to clean resources
    * associated with the lease. It is passed in to the cleaner as
    * an parameter to the callback. The cleaner is then responsible
    * for cleaning any resources associated with the lease.    
    *
    * @return returns the resource key that this represents
    */   
   public T getKey() {
      return key;
   }

   /**
    * This method will return the number of <code>TimeUnit</code>
    * seconds that remain in the contract. If the value returned is 
    * less than or equal to zero then it should be assumed that the 
    * lease has expired, if greater than zero the lease is active.
    *
    * @return returns the duration in the time unit remaining
    */   
   public long getDelay(TimeUnit unit) {
      return unit.convert(time - getTime(), NANOSECONDS);
   }   

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
   public void setDelay(long delay, TimeUnit unit) {
      this.time = getTime() + unit.toNanos(delay);
   }

   /**
    * This method returns the current time in nanoseconds. This is
    * used to allow the duration of the lease to be calculated with
    * any given time unit which allows flexibility in setting and 
    * getting the current delay for the contract.
    *
    * @return returns the current time in nanoseconds remaining
    */   
   private long getTime() {
      return System.nanoTime();
   }
   
   /**
    * This is used to compare the specified delay to this delay. The
    * result of this operation is used to prioritize contracts in 
    * order of first to expire. Contracts that expire first reach
    * the top of the contract queue and are taken off for cleaning.
    * 
    * @param other this is the delay to be compared with this
    * 
    * @return this returns zero if equal otherwise the difference
    */   
   public int compareTo(Delayed other) {
      Expiration value = (Expiration) other;  
      
      if(other == this) {
         return 0;
      }
      return compareTo(value);
   }
   
   /**
    * This is used to compare the specified delay to this delay. The
    * result of this operation is used to prioritize contracts in 
    * order of first to expire. Contracts that expire first reach
    * the top of the contract queue and are taken off for cleaning.
    * 
    * @param value this is the expiration to be compared with this
    * 
    * @return this returns zero if equal otherwise the difference
    */   
   private int compareTo(Expiration value) {
      long diff = time - value.time;
      
      if(diff < 0) {
         return -1;
      } else if(diff > 0) {
         return 1;
      }
      return 0;
   }
   
   /**
    * This is used to provide a description of the contract that the
    * instance represents. A description well contain the key owned
    * by the contract as well as the expiry time expected for it.
    * This is used to provide descriptive messages in the exceptions.
    * 
    * @return a descriptive message describing the contract object
    */
   public String toString() {
      return String.format("contract %s", key);
   }
}
