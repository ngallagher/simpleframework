/*
 * ConcurrentExecutor.java February 2007
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

package org.simpleframework.common.thread;

import java.util.concurrent.Executor;

/**
 * The <code>ConcurrentExecutor</code> object is used to execute tasks
 * in a thread pool. This creates a thread pool with an unbounded list
 * of outstanding tasks, which ensures that any system requesting
 * a task to be executed will not block when handing it over.
 * 
 * @author Niall Gallagher
 */
public class ConcurrentExecutor implements Executor {
   
   /**
    * This is the queue used to enqueue the tasks for execution.
    */
   private final ExecutorQueue queue;
   
   /**
    * Constructor for the <code>ConcurrentExecutor</code> object. This 
    * is used to create a pool of threads that can be used to execute
    * arbitrary <code>Runnable</code> tasks. If the threads are
    * busy this will simply enqueue the tasks and return.
    * 
    * @param type this is the type of runnable that this accepts
    */
   public ConcurrentExecutor(Class type) {
      this(type, 10);
   }
   
   /**
    * Constructor for the <code>ConcurrentExecutor</code> object. This 
    * is used to create a pool of threads that can be used to execute
    * arbitrary <code>Runnable</code> tasks. If the threads are
    * busy this will simply enqueue the tasks and return.
    * 
    * @param type this is the type of runnable that this accepts
    * @param size this is the number of threads to use in the pool
    */   
   public ConcurrentExecutor(Class type, int size) {
      this(type, size, size);
   }
   
   /**
    * Constructor for the <code>ConcurrentExecutor</code> object. This 
    * is used to create a pool of threads that can be used to execute
    * arbitrary <code>Runnable</code> tasks. If the threads are
    * busy this will simply enqueue the tasks and return.
    * 
    * @param type this is the type of runnable that this accepts
    * @param rest this is the number of threads to use in the pool    
    * @param active this is the maximum size the pool can grow to 
    */   
   public ConcurrentExecutor(Class type, int rest, int active) {     
      this.queue = new ExecutorQueue(type, rest, active);
   }   
   
   /**
    * The <code>execute</code> method is used to queue the task for
    * execution. If all threads are busy the provided task is queued
    * and waits until all current and outstanding tasks are finished.
    * 
    * @param task this is the task to be queued for execution
    */
   public void execute(Runnable task) {
      queue.execute(task);
   }   
   
   /**
    * This is used to stop the executor by interrupting all running
    * tasks and shutting down the threads within the pool. This will
    * return once it has been stopped, and no further tasks will be 
    * accepted by this pool for execution.
    */   
   public void stop() {
      stop(60000);
   }   
   
   /**
    * This is used to stop the executor by interrupting all running
    * tasks and shutting down the threads within the pool. This will
    * return once it has been stopped, and no further tasks will be 
    * accepted by this pool for execution.
    *
    * @param wait the number of milliseconds to wait for it to stop 
    */   
   public void stop(long wait) {
      queue.stop(wait);
   }
}
