/*
 * Reactor.java February 2007
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

import java.io.IOException;

/**
 * The <code>Reactor</code> interface is used to describe an object 
 * that is used to schedule asynchronous I/O operations. An operation
 * is performed by handing it to the reactor, which will determine
 * if an interested event has occurred. This allows the operation to
 * perform the task in a manner that does not block.
 * <p>
 * Implementing an <code>Operation</code> object requires that the
 * operation itself is aware of the I/O task it is performing. For
 * example, if the operation is concerned with reading data from the
 * underlying channel then the operation should perform the read, if
 * there is more data required then that operation to register with
 * the reactor again to receive further notifications. 
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.transport.reactor.Operation
 */ 
public interface Reactor { 

  /**
   * This method is used to execute the provided operation without
   * the need to specifically check for I/O events. This is used if
   * the operation knows that the <code>SelectableChannel</code> is
   * ready, or if the I/O operation can be performed without knowing
   * if the channel is ready. Typically this is an efficient means
   * to perform a poll rather than a select on the channel.
   *
   * @param task this is the task to execute immediately
   */ 
  void process(Operation task) throws IOException;

  /**        
   * This method is used to execute the provided operation when there
   * is an I/O event that task is interested in. This will used the
   * operations <code>SelectableChannel</code> object to determine 
   * the events that are ready on the channel. If this reactor is
   * interested in any of the ready events then the task is executed.
   *
   * @param task this is the task to execute on interested events    
   * @param require this is the bitmask value for interested events
   */
  void process(Operation task, int require) throws IOException;

  /**
   * This is used to stop the reactor so that further requests to
   * execute operations does nothing. This will clean up all of 
   * the reactors resources and unregister any operations that are
   * currently awaiting execution. This should be used to ensure
   * any threads used by the reactor gracefully stop.
   */ 
  void stop() throws IOException;
}




