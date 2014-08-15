/*
 * Timer.java November 2012
 *
 * Copyright (C) 2012, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.core;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;

/**
 * The <code>Timer<code> object is used to set the time a specific
 * event occurred at. The time can be set only once from that point 
 * on all attempts to set the time are ignored. This makes this 
 * timer useful when there is a desire to record when a certain
 * scenario was first encountered, for example when a request is
 * first read from the underlying transport.  
 * 
 * @author Niall Gallagher
 */
class Timer {

   /**
    * This is the time unit that this timer provides the time in.
    */
   private TimeUnit unit;
   
   /**
    * This is the time in milliseconds used to record the event.
    */
   private volatile long time;
   
   /**
    * Constructor for the <code>Timer</code> object. This is used
    * to record when a specific event occurs. The provided time 
    * unit is used to determine how the time is retrieved.
    * 
    * @param unit this time unit this timer will be using
    */
   public Timer(TimeUnit unit) {
      this.unit = unit;
      this.time = -1L;
   }
   
   /**
    * This is used to determine if the timer has been set. If 
    * the <code>set</code> method has been called on this instance
    * before then this will return true, otherwise false.
    * 
    * @return this returns true if the timer has been set
    */
   public boolean isSet() {
      return time > 0;
   }
 
   /**
    * This is used to set the time for a specific event. Invoking
    * this method multiple times will have no effect as the time
    * is set for the first invocation only. Setting the time in 
    * this manner enables start times to be recorded effectively.     
    */
   public void set() {
      if(time < 0) {
         time = currentTimeMillis();
      }
   }
   
   /**
    * This is used to get the time for a specific event. The time
    * returned by this method is given in the time unit specified
    * on construction of the instance.
    * 
    * @return this returns the time recorded by the timer
    */
   public long get() {
      return unit.convert(time, MILLISECONDS);
   }
}
 