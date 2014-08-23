/*
 * ExecutorReactor.java February 2007
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
import java.util.concurrent.Executor;

/**
 * The <code>ExecutorReactor</code> is used to schedule operation for
 * execution using an <code>Executor</code> implementation. This can be
 * useful when the operations performed are time intensive. For example
 * if the operations performed a read of the underlying channel and 
 * then had to parse the contents of the payload. Such operations would
 * reduce the performance of the reactor if it could not delegate to
 * some other form of executor, as it would delay their execution.
 *
 * @author Niall Gallagher
 */
public class ExecutorReactor implements Reactor {

  /**
   * This is used to distribute the ready operations for execution.
   */         
  private final OperationDistributor exchange;

  /**
   * This is used to execute the operations that ready to run.
   */ 
  private final Executor executor;
  
  /**
   * Constructor for the <code>ExecutorReactor</code> object. This is
   * used to create a reactor that can delegate to the executor. This
   * also accepts the operations it is interested in, the value is
   * taken from the <code>SelectionKey</code> object. A bit mask can
   * be used to show interest in several operations at once.
   *
   * @param executor this is the executor used to run the operations
   */  
  public ExecutorReactor(Executor executor) throws IOException {
     this(executor, 1);
  }
  
  /**
   * Constructor for the <code>ExecutorReactor</code> object. This is
   * used to create a reactor that can delegate to the executor. This
   * also accepts the operations it is interested in, the value is
   * taken from the <code>SelectionKey</code> object. A bit mask can
   * be used to show interest in several operations at once.
   *
   * @param executor this is the executor used to run the operations
   * @param count this is the number of distributors to be used
   */  
  public ExecutorReactor(Executor executor, int count) throws IOException {    
     this(executor, count, 120000);
  }  

  /**
   * Constructor for the <code>ExecutorReactor</code> object. This is
   * used to create a reactor that can delegate to the executor. This
   * also accepts the operations it is interested in, the value is
   * taken from the <code>SelectionKey</code> object. A bit mask can
   * be used to show interest in several operations at once.
   *
   * @param executor this is the executor used to run the operations
   * @param count this is the number of distributors to be used
   * @param expiry the length of time to maintain and idle operation
   */    
  public ExecutorReactor(Executor executor, int count, long expiry) throws IOException {    
    this.exchange = new PartitionDistributor(executor, count, expiry);    
    this.executor = executor;
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
     executor.execute(task);          
  }
  
  /**        
   * This method is used to execute the provided operation when there
   * is an I/O event that task is interested in. This will used the
   * operations <code>SelectableChannel</code> object to determine 
   * the events that are ready on the channel. If this reactor is
   * interested in any of the ready events then the task is executed.
   *
   * @param task this is the task to execute on interested events
   * @param require this is the bit-mask value for interested events
   */  
  public void process(Operation task, int require) throws IOException {         
     exchange.process(task, require);    
  }        

  /**
   * This is used to stop the reactor so that further requests to
   * execute operations does nothing. This will clean up all of 
   * the reactors resources and unregister any operations that are
   * currently awaiting execution. This should be used to ensure
   * any threads used by the reactor gracefully stop.
   */   
  public void stop() throws IOException {
     exchange.close();
  }
}




