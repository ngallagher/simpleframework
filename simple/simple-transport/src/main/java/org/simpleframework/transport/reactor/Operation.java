/*
 * Operation.java February 2007
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

package org.simpleframework.transport.reactor;

import java.nio.channels.SelectableChannel;

import org.simpleframework.transport.trace.Trace;

/**
 * The <code>Operation</code> interface is used to describe a task
 * that can be executed when the associated channel is ready for some
 * operation. Typically the <code>SelectableChannel</code> is used to
 * register with a selector with a set of given interested operations
 * when those operations can be performed this is executed.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.transport.reactor.Reactor
 */ 
public interface Operation extends Runnable {   
   
   /**
    * This is used to acquire the trace object that is associated
    * with the operation. A trace object is used to collection details
    * on what operations are being performed. For instance it may 
    * contain information relating to I/O events or errors. 
    * 
    * @return this returns the trace associated with this operation
    */   
   Trace getTrace();   

  /**
   * This is the <code>SelectableChannel</code> which is used to 
   * determine if the operation should be executed. If the channel   
   * is ready for a given I/O event it can be run. For instance if
   * the operation is used to perform some form of read operation
   * it can be executed when ready to read data from the channel.
   *
   * @return this returns the channel used to govern execution
   */ 
  SelectableChannel getChannel();       

  /**
   * This is used to cancel the operation if it has timed out. This
   * is typically invoked when it has been waiting in a selector for
   * an extended duration of time without any active operations on
   * it. In such a case the reactor must purge the operation to free
   * the memory and open channels associated with the operation.
   */ 
  void cancel();
}


