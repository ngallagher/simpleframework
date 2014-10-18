/*
 * Scheduler.java October 2014
 *
 * Copyright (C) 2014, Niall Gallagher <niallg@users.sf.net>
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
import java.util.concurrent.TimeUnit;

/**
 * The <code>Scheduler</code> interface represents a means to execute
 * a task immediately or after a specified delay. This queues the 
 * task for the requested period of time before it is executed, if a
 * delay is specified. How the task is executed is dependent on the
 * implementation, however it will normally use a thread pool.
 * 
 * @author Niall Gallagher
 */
public interface Scheduler extends Executor {
   
   /**
    * This will execute the task within the executor after the time
    * specified has expired. If the time specified is zero then it
    * will be executed immediately. Once the scheduler has been
    * stopped then this method will no longer accept runnable tasks.
    * 
    * @param task this is the task to schedule for execution
    * @param delay the time in milliseconds to wait for execution
    */   
   void execute(Runnable task, long delay);
   
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
   void execute(Runnable task, long delay, TimeUnit unit);
}
