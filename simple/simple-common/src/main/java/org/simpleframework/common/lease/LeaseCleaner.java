/*
 * LeaseCleaner.java May 2004
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

import org.simpleframework.common.thread.Daemon;

/**
 * The <code>LeaseCleaner</code> provides a means of providing
 * callbacks to clean a leased resource once the contract duration
 * has expired. This will acquire contracts from the queue and
 * invoke the <code>Cleaner</code> notification method. This will
 * wait until the current clean operation has completed before it
 * attempts to clean the next contract.
 *
 * @author Niall Gallagher
 */
class LeaseCleaner<T> extends Daemon {

   /**
    * This is used to queue contracts that are to be cleaned.
    */
   private final ContractQueue<T> queue;        

   /**
    * This is the cleaner that is invoked to clean contracts.
    */
   private final Cleaner<T> cleaner;        

   /**
    * Constructor for the <code>LeaseCleaner</code> object. This 
    * can be used to issue, update, and expire leases. When a lease
    * expires notification is sent to the <code>Cleaner</code>
    * object provided. This allows an implementation independent
    * means to clean up once a specific lease has expired.
    *
    * @param cleaner this will receive expiration notifications
    */   
   public LeaseCleaner(Cleaner<T> cleaner) {
      this.queue = new ContractQueue<T>();
      this.cleaner = cleaner;          
      this.start();
   }
   
   /**
    * This revokes a contract that has previously been issued. This
    * is used when the contract duration has changed so that it can
    * be reissued again with a new duration. This returns true if 
    * the contract was still active and false if it did not exist.
    *
    * @param contract this is the contract that contains details
    */
   public boolean revoke(Contract<T> contract) throws LeaseException {
      if(!isActive()) {
         throw new LeaseException("Lease can not be revoked");
      }
      return queue.remove(contract);
   }
   
   /**
    * This method will establish a contract for a given resource.     
    * If the contract duration expires before it is renewed then 
    * a notification is sent, to the issued <code>Cleaner</code>
    * implementation, to signify that the resource has expired.
    * 
    * @param contract this is the contract that contains details
    */   
   public boolean issue(Contract<T> contract) throws LeaseException {
      if(!isActive()) {
         throw new LeaseException("Lease can not be issued");
      }
      return queue.offer(contract);
   }

   /**
    * This acquires expired lease contracts from the queue once the
    * expiry duration has passed. This will deliver notification to
    * the <code>Cleaner</code> object once the contract has been 
    * taken from the queue. This allows the cleaner to clean up any
    * resources associated with the lease before the next expiration.
    */
   public void run() {
      while(isActive()) {
         try {                         
            clean();            
         } catch(Throwable e) {
            continue;                 
         }
      }
      purge();
   }   
   
   /**
    * This method is used to take the lease from the queue and give
    * it to the cleaner for expiry. This effectively waits until the
    * next contract expiry has passed, once it has passed the key
    * for that contract is given to the cleaner to clean up resources.   
    */
   private void clean() throws Exception {      
      Contract<T> next = queue.take();
      T key = next.getKey();

      if(key != null) {
         cleaner.clean(key);
      }
   } 
   
   /**
    * Here all of the existing contracts are purged when the invoker
    * is closed. This ensures that each leased resource has a chance
    * to clean up after the lease manager has been closed. All of the
    * contracts are given a zero delay and cleaned immediately such
    * that once this method has finished the queue will be empty.    
    */
   private void purge() {
      for(Contract<T> next : queue) {
         T key = next.getKey();
         
         try {
            next.setDelay(0L, NANOSECONDS);         
            cleaner.clean(key);
         } catch(Throwable e) {
            continue;
         }
      }      
   }
   
   /**
    * Here we shutdown the lease maintainer so that the thread will
    * die. Shutting down the maintainer is done by interrupting the
    * thread and setting the dead flag to true. Once this is invoked
    * then the thread will no longer be running for this object.
    */
   public void close() {
      stop();     
      interrupt();            
   }       
}
