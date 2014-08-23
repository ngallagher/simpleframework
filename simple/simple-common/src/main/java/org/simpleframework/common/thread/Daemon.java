/*
 * Daemon.java February 2009
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

package org.simpleframework.common.thread;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The <code>Daemon</code> object provides a named thread which will
 * execute the <code>run</code> method when started. This offers 
 * some convenience in that it hides the normal thread methods and 
 * also allows the object extending this to provide the name of the 
 * internal thread, which is given an incrementing sequence number 
 * appended to the name provided.
 * 
 * @author Niall Gallagher
 */
public abstract class Daemon implements Runnable {
   
   /**
    * This is the current thread executing this service.
    */
   private final AtomicReference<Thread> reference;
   
   /**
    * This is the internal thread used by this daemon instance.
    */
   private final DaemonFactory factory;
   
   /**
    * This is used to determine if the daemon is active.
    */
   private final AtomicBoolean active;
   
   /**
    * This is the internal thread that is executed by this.
    */
   private final Runnable delegate;
   
   /**
    * Constructor for the <code>Daemon</code> object. This will 
    * create the internal thread and ensure it is a daemon. When it
    * is started the name of the internal thread is set using the
    * name of the instance as taken from <code>getName</code>. If
    * the name provided is null then no name is set for the thread.
    */
   protected Daemon() {
      this.reference = new AtomicReference<Thread>();
      this.delegate = new RunnableDelegate(this);
      this.factory = new DaemonFactory();
      this.active = new AtomicBoolean();
   }   
   
   /**
    * This is used to determine if the runner is active. If it is not
    * active then it is assumed that no thread is executing. Also, if
    * this is extended then any executing thread to stop as soon as
    * this method returns false.
    * 
    * @return this returns true if the runner is active
    */
   public boolean isActive() {
      return active.get();
   }
   
   /**
    * This is used to start the internal thread. Once started the
    * internal thread will execute the <code>run</code> method of
    * this instance. Aside from starting the thread this will also
    * ensure the internal thread has a unique name.
    */
   public void start() {
      Class type = getClass();
      
      if (!active.get()) {
         Thread thread = factory.newThread(delegate, type);
         
         reference.set(thread);
         active.set(true);
         thread.start();
      }
   }
   
   /**
    * This is used to interrupt the internal thread. This is used
    * when there is a need to wake the thread from a sleeping or
    * waiting state so that some other operation can be performed.
    * Typically this is required when killing the thread.
    */
   public void interrupt() {
      Thread thread = reference.get();
      
      if(thread != null) {
         thread.interrupt();
      }
   }   
   
   /**
    * This method is used to stop the thread without forcing it to
    * stop. It acts as a means to deactivate it. It is up to the
    * implementor to ensure that the <code>isActive</code> method
    * is checked to determine whether it should continue to run.
    */
   public void stop() {
      active.set(false);
   }
   
   /**
    * The <code>RunnableDelegate</code> object is used to actually
    * invoke the <code>run</code> method. A delegate is used to ensure
    * that once the task has finished it is inactive so that it can
    * be started again with a new thread.    
    */
   private class RunnableDelegate implements Runnable {      
      
      /**
       * This is the runnable that is to be executed. 
       */
      private final Runnable task;
      
      /**
       * Constructor for the <code>RunnableDelegate</code> object. The
       * delegate requires the actual runnable that is to be executed.
       * As soon as the task has finished the runner becomes inactive.
       * 
       * @param task this is the task to be executed
       */
      public RunnableDelegate(Runnable task) {
         this.task = task;
      }
      
      /**
       * This is used to execute the task. Once the task has finished
       * the runner becomes inactive and any reference to the internal
       * thread is set to null. This ensures the runner can be started
       * again at a later time if desired.
       */
      public void run() {
         try {
            task.run();
         } finally {
            reference.set(null);
            active.set(false);
         }
      }
      
   }
}
