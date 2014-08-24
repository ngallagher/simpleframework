/*
 * ServerCleaner.java February 2009
 *
 * Copyright (C) 2009, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.transport;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.common.thread.Daemon;
import org.simpleframework.transport.reactor.Reactor;

/**
 * The <code>ServerCleaner</code> object allows for the termination
 * and resource recovery to be done asynchronously. This ensures that
 * should a HTTP request be used to terminate the processor that it 
 * does not block waiting for the servicing thread pool to terminate 
 * causing a deadlock.
 * 
 * @author Niall Gallagher
 */
class ServerCleaner extends Daemon {
   
   /**
    * This is the internal processor that is to be terminated.
    */
   private final TransportProcessor processor;
   
   /**
    * This is the thread pool implementation used by the server.
    */
   private final ConcurrentExecutor executor;
   
   /**
    * This is the internal write reactor that is terminated.
    */
   private final Reactor reactor;
   
   /**
    * Constructor for the <code>ServerCleaner</code> object. For an 
    * orderly termination of the processor, the processor and reactor
    * provided to the constructor will be stopped asynchronously.
    *
    * @param processor this is the processor that is to be stopped
    * @param executor this is the executor used by the server
    * @param reactor this is the reactor that is to be closed
    */
   public ServerCleaner(TransportProcessor processor, ConcurrentExecutor executor, Reactor reactor) {
      this.processor = processor;  
      this.executor = executor;
      this.reactor = reactor;      
   }
   
   /**
    * When this method runs it will firstly stop the processor in 
    * a synchronous fashion. Once the <code>TransportProcessor</code> 
    * has stopped it will stop the <code>Reactor</code> ensuring that
    * all threads will be released. 
    * <p>
    * It is important to note that stopping the processor before
    * stopping the reactor is required. This ensures that if there
    * are any threads executing within the processor that require
    * the reactor threads, they can complete without a problem.
    */
   public void run() {
      try {
         processor.stop();
         executor.stop();
         reactor.stop();
      } catch(Exception e) {
         return;
      }
   }
}