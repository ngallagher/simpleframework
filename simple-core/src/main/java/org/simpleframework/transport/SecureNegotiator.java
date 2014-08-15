/*
 * SecureNegotiator.java February 2007
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

import java.io.IOException;

import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.util.thread.ConcurrentExecutor;

/**
 * The <code>SecureNegotiator</code> object is used to negotiations
 * to complete SSL handshakes for secure connections. Negotiations 
 * are performed on <code>Pipeline</code> object before a transport
 * is created from the pipeline. Performing the SSL handshake is
 * required to ensure that only HTTP specific data is read and
 * written to the underlying transport.
 * 
 * @author Niall Gallagher
 */
class SecureNegotiator implements Negotiator {
   
   /**
    * This is the executor used to execute the negotiations.
    */
   private final ConcurrentExecutor executor;
   
   /**
    * This is the transport used to process complete transports.
    */
   private final Processor processor;
   
   /**
    * This is the reactor which is used to schedule I/O events.
    */
   private final Reactor reactor;

   /**
    * Constructor for the <code>SecureNegotiator</code> object. This
    * is used to create a negotiator that will perform SSL handshakes
    * on provided pipelines so that the data read from an written to
    * the underlying transport is complete and ready to use.
    * 
    * @param processor this is used to process the transports
    * @param count this is the number of threads used by this
    */
   public SecureNegotiator(Processor processor, int count) throws IOException {          
     this.executor = new ConcurrentExecutor(Notifier.class, count);          
     this.reactor = new ExecutorReactor(executor);            
     this.processor = processor;                  
   }  

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
   public void process(Operation task) throws IOException {
     reactor.process(task);          
   }

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
   public void process(Operation task, int require) throws IOException {
     reactor.process(task, require);
   }

   /**
    * Once the negotiation has completed this is used to perform
    * processing of the provided transport. Processing of the
    * transport is done only after the negotiation has completed.
    * The given transport is used to read and write to the socket.
    * 
    * @param transport this is the transport for the pipeline
    */   
   public void ready(Transport transport) throws IOException {
     processor.process(transport); 
   }
   
   /**
    * This is used to stop the reactor so that further requests to
    * execute operations does nothing. This will clean up all of 
    * the reactors resources and unregister any operations that are
    * currently awaiting execution. This should be used to ensure
    * any threads used by the reactor gracefully stop.
    */    
   public void stop() throws IOException {
      executor.stop();
      reactor.stop();
   }
 }