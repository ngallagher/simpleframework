/*
 * Phase.java February 2007
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

package org.simpleframework.transport;

import static org.simpleframework.transport.TransportEvent.ERROR;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>Phase</code> object represents an asynchronous phase
 * within the negotiation. This is typically used to either schedule
 * an asynchronous read or write when it can not be performed 
 * directly. It ensures that the negotiation does not block the 
 * thread so that execution can be optimized of high concurrency.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.Handshake
 */
abstract class Phase implements Operation {
   
   /**
    * This is the negotiation that this task will operate on.
    */
   protected final Negotiation state;
   
   /**
    * This is the reactor that is used to schedule execution.
    */
   protected final Reactor reactor;
   
   /**
    * This is the trace used to monitor the handshake socket.
    */
   protected final Trace trace;
   
   /**
    * This is the required operation for the task to complete.
    */
   protected final int require;

   /**
    * Constructor for the <code>Phase</code> object. This is used to
    * create an operation that performs some phase of a negotiation.
    * It allows the negotiation to schedule the read and write 
    * operations asynchronously.
    * 
    * @param state this is the negotiation this task works on
    * @param reactor this is the reactor used to schedule the task
    * @param trace the trace that is used to monitor the handshake
    * @param require this is the required operation for the task
    */
   public Phase(Negotiation state, Reactor reactor, Trace trace, int require) {
      this.reactor = reactor;
      this.require = require;
      this.state = state;
      this.trace = trace;
   }    
   
   /**
    * This is used to acquire the trace object that is associated
    * with the operation. A trace object is used to collection details
    * on what operations are being performed. For instance it may 
    * contain information relating to I/O events or errors. 
    * 
    * @return this returns the trace associated with this operation
    */    
   public Trace getTrace() {
      return trace;
   }   

   /**
    * This is the <code>SelectableChannel</code> which is used to 
    * determine if the operation should be executed. If the channel   
    * is ready for a given I/O event it can be run. For instance if
    * the operation is used to perform some form of read operation
    * it can be executed when ready to read data from the channel.
    *
    * @return this returns the channel used to govern execution
    */ 
   public SelectableChannel getChannel() {
      return state.getChannel();
   }     

   /**
    * This is used to execute the task. It is up to the specific
    * task implementation to decide what to do when executed. If
    * the task needs to read or write data then it can attempt
    * to perform the read or write, if it incomplete the it can
    * be scheduled for execution with the reactor.
    */
   public void run() {
      try {
         execute();
      }catch(Exception cause) {
         trace.trace(ERROR, cause);
         cancel();
      }
   }

   /**
    * This is used to cancel the operation if it has timed out. This
    * is typically invoked when it has been waiting in a selector for
    * an extended duration of time without any active operations on
    * it. In such a case the reactor must purge the operation to free
    * the memory and open channels associated with the operation.
    */ 
   public void cancel() {
      try {
         state.cancel();
      }catch(Exception cause) {
         trace.trace(ERROR, cause);         
      }
   }

   /**
    * This is used to execute the task. It is up to the specific
    * task implementation to decide what to do when executed. If
    * the task needs to read or write data then it can attempt
    * to perform the read or write, if it incomplete the it can
    * be scheduled for execution with the reactor.
    */
   protected void execute() throws IOException {
      boolean done = ready();
      
      if(!done) {
         reactor.process(this, require);
      } else {
         state.resume();
      }
   }

   /**
    * This method is used to determine if the task is ready. This is
    * executed when the select operation is signaled. When this is
    * true the the task completes. If not then this will schedule
    * the task again for the specified select operation.
    * 
    * @return this returns true when the task has completed
    */
   protected boolean ready() throws IOException {
      return true;
   }
}
