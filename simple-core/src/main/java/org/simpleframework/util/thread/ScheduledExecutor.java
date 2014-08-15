/*
 * ScheduledExecutor.java February 2007
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

package org.simpleframework.util.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * The <code>ScheduledExecutor</code> object is used to schedule tasks 
 * for execution. This queues the task for the requested period of 
 * time before it is executed. It ensures that the delay is adhered
 * to such that tasks can be timed for execution in an accurate way.
 * 
 * @author Niall Gallagher
 */
public class ScheduledExecutor implements Executor {
   
   /**
    * This is the scheduler queue used to enque tasks to execute.
    */
   private final ScheduledTaskQueue engine;
   
   /**
    * Constructor for the <code>ScheduledExecutor</code> object. This 
    * will create a scheduler with a fixed number of threads to use
    * before execution. Depending on the types of task that are
    * to be executed this should be increased for accuracy.
    * 
    * @param size this is the number of threads for the scheduler
    */
   public ScheduledExecutor(int size) {
      this.engine = new ScheduledTaskQueue(size);
   }

   /**
    * This will execute the task within the executor immediately 
    * as it uses a delay duration of zero milliseconds. This can
    * be used if the scheduler is to be used as a thread pool.
    * 
    * @param task this is the task to schedule for execution
    */
   public void execute(Runnable task) {
     execute(task, 0);           
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
      engine.schedule(task, delay, unit);
   }

   /**
    * This is used to stop the executor by interrupting all running
    * tasks and shutting down the threads within the pool. This will
    * return immediately once it has been stopped, and not further
    * tasks will be accepted by this pool for execution.
    */   
   public void stop() {
      engine.stop();           
   }
}
