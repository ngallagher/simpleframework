/*
 * Latch.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.transport.reactor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * The <code>Latch</code> is used to provide a simple latch that will
 * allow a thread to block until it is signaled that it is ready.
 * The latch will block on the <code>close</code> method and when the
 * latch is signaled the close method will release all threads.
 * 
 * @author Niall Gallagher
 */
class Latch extends CountDownLatch {
   
   /**
    * Constructor for the <code>Latch</code> object. This will 
    * create a count down latch that will block when it is
    * closed. Any blocked threads will be released when the
    * latch is signaled that it is ready.
    */
   public Latch() {
      super(1);
   }
   
   /**
    * This is used to signal that the latch is ready. Invoking
    * this method will release all threads that are blocking on
    * the close method. This method is used when the distributor
    * is closed and all operations have been purged.
    */
   public void signal() throws IOException {
      try {
         countDown();
      } catch(Exception e) {
         throw new IOException("Thread interrupted");
      }
   }
   
   /**
    * This will block all threads attempting to close the latch.
    * All threads will be release when the latch is signaled. This
    * is used to ensure the distributor blocks until it has fully
    * purged all registered operations that are registered.
    */
   public void close() throws IOException {
      try {
         await();
      } catch(Exception e){
         throw new IOException("Thread interrupted");
      }
   }
}