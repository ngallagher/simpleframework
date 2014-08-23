/*
 * SynchronousExecutor.java February 2007
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
 * The <code>SynchronousExecutor</code> object is used for synchronous
 * execution of tasks. This simple acts as an adapter for running
 * a <code>Runnable</code> implementation and can be used wherever
 * the executor interface is required.
 * 
 * @author Niall Gallagher
 */
public class SynchronousExecutor implements Executor {
   
   /**
    * This will execute the provided <code>Runnable</code> within
    * the current thread. This implementation will simple invoke
    * the run method of the task and wait for it to complete.
    * 
    * @param task this is the task that is to be executed
    */
   public void execute(Runnable task) {
      task.run();
   }
}