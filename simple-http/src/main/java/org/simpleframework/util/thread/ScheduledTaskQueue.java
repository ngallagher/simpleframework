/*
 * ScheduledTaskQueue.java February 2007
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

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * The <code>ScheduledTaskQueue</code> object is used to schedule tasks 
 * for execution. This queues the task for the requested period of 
 * time before it is executed. It ensures that the delay is adhered 
 * to such that tasks can be timed for execution in an accurate way.
 * 
 * @author Niall Gallagher
 */
class ScheduledTaskQueue extends ScheduledThreadPoolExecutor {
   
   /**
    * Constructor for the <code>ScheduledTaskQueue</code> object. This 
    * will create a scheduler with a fixed number of threads to use
    * before execution. Depending on the types of task that are
    * to be executed this should be increased for accuracy.
    * 
    * @param size this is the number of threads for the scheduler
    */   
   public ScheduledTaskQueue(int size) {
      super(size);
   }
   
   /**
    * This is used to stop the executor by interrupting all running
    * tasks and shutting down the threads within the pool. This will
    * return immediately once it has been stopped, and not further
    * tasks will be accepted by this pool for execution.
    */    
   public void stop() {
      shutdown();
   }
}
