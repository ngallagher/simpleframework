/*
 * SchedulerQueue.java February 2007
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

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * The <code>SchedulerQueue</code> object is used to schedule tasks 
 * for execution. This queues the task for the requested period of 
 * time before it is executed. It ensures that the delay is adhered 
 * to such that tasks can be timed for execution in an accurate way.
 * 
 * @author Niall Gallagher
 */
class SchedulerQueue {
   
   /**
    * This is the actual scheduler used to schedule the tasks.
    */
   private final ScheduledThreadPoolExecutor executor;
   
   /**
    * This is the factory used to create the worker threads.
    */
   private final ThreadFactory factory;
   
   /**
    * Constructor for the <code>SchedulerQueue</code> object. This 
    * will create a scheduler with a fixed number of threads to use
    * before execution. Depending on the types of task that are
    * to be executed this should be increased for accuracy.
    * 
    * @param type this is the type of task to execute
    * @param size this is the number of threads for the scheduler
    */   
   public SchedulerQueue(Class type, int size) {
      this.factory = new DaemonFactory(type);
      this.executor = new ScheduledThreadPoolExecutor(size, factory);
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
    * This will execute the task within the executor after the time
    * specified has expired. If the time specified is zero then it
    * will be executed immediately. Once the scheduler has been
    * stopped then this method will no longer accept runnable tasks.
    * 
    * @param task this is the task to schedule for execution
    * @param delay the time in milliseconds to wait for execution
    */   
   public void execute(Runnable task, long delay) {
      execute(task, delay, TimeUnit.MILLISECONDS);
   }
   
   /**
    * This will execute the task within the executor after the time
    * specified has expired. If the time specified is zero then it
    * will be executed immediately. Once the scheduler has been
    * stopped then this method will no longer accept runnable tasks.
    * 
    * @param task this is the task to schedule for execution
    * @param delay this is the delay to wait before execution
    * @param unit this is the duration time unit to wait for
    */   
   public void execute(Runnable task, long delay, TimeUnit unit) {
      executor.schedule(task, delay, unit);
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
