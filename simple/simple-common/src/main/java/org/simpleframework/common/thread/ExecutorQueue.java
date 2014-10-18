/*
 * ExecutorQueue.java February 2007
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The <code>ExecutorQueue</code> object is used to queue tasks in 
 * a thread pool. This creates a thread pool with no limit to the 
 * number of tasks that can be enqueued, which ensures that any 
 * system requesting a task to be executed will not block when 
 * handing it over, it also means the user must use caution.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.common.thread.ConcurrentExecutor
 */
class ExecutorQueue {
   
   /**
    * This is the task queue that contains tasks due to execute.
    */
   private final BlockingQueue<Runnable> queue;
   
   /**
    * This is the actual thread pool implementation used.
    */
   private final ThreadPoolExecutor executor;
   
   /**
    * This is used to create the pool worker threads.
    */
   private final ThreadFactory factory;
   
   /**
    * Constructor for the <code>ExecutorQueue</code> object. This is
    * used to create a pool of threads that can be used to execute
    * arbitrary <code>Runnable</code> tasks. If the threads are
    * busy this will simply enqueue the tasks and return.
    * 
    * @param type this is the type of runnable that this accepts
    * @param rest this is the number of threads to use in the pool    
    * @param active this is the maximum size the pool can grow to 
    */    
   public ExecutorQueue(Class type, int rest, int active) {
      this(type, rest, active, 120, TimeUnit.SECONDS);
   }
  
   /**
    * Constructor for the <code>ExecutorQueue</code> object. This is
    * used to create a pool of threads that can be used to execute
    * arbitrary <code>Runnable</code> tasks. If the threads are
    * busy this will simply enqueue the tasks and return.
    *
    * @param type this is the type of runnable that this accepts
    * @param rest this is the number of threads to use in the pool    
    * @param active this is the maximum size the pool can grow to
    * @param duration the duration active threads remain idle for
    * @param unit this is the time unit used for the duration 
    */    
   public ExecutorQueue(Class type, int rest, int active, long duration, TimeUnit unit) {
      this.queue = new LinkedBlockingQueue<Runnable>();
      this.factory = new DaemonFactory(type);
      this.executor = new ThreadPoolExecutor(rest, active, duration, unit, queue, factory);
   }
   
   /**
    * The <code>execute</code> method is used to queue the task for
    * execution. If all threads are busy the provided task is queued
    * and waits until all current and outstanding tasks are finished.
    * 
    * @param task this is the task to be queued for execution
    */
   public void execute(Runnable task) {
      executor.execute(task);
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
      if(!executor.isTerminated()) {
         try {
            executor.shutdown();
            executor.awaitTermination(wait, MILLISECONDS);
         } catch(Exception e) {
            throw new IllegalStateException("Could not stop pool", e);
         }         
      }
   }
}
