/*
 * ActionDistributor.java February 2007
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
 
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static org.simpleframework.transport.reactor.ReactorEvent.CHANNEL_CLOSED;
import static org.simpleframework.transport.reactor.ReactorEvent.CLOSE_SELECTOR;
import static org.simpleframework.transport.reactor.ReactorEvent.ERROR;
import static org.simpleframework.transport.reactor.ReactorEvent.EXECUTE_ACTION;
import static org.simpleframework.transport.reactor.ReactorEvent.INVALID_KEY;
import static org.simpleframework.transport.reactor.ReactorEvent.READ_INTEREST_READY;
import static org.simpleframework.transport.reactor.ReactorEvent.REGISTER_INTEREST;
import static org.simpleframework.transport.reactor.ReactorEvent.REGISTER_READ_INTEREST;
import static org.simpleframework.transport.reactor.ReactorEvent.REGISTER_WRITE_INTEREST;
import static org.simpleframework.transport.reactor.ReactorEvent.SELECT;
import static org.simpleframework.transport.reactor.ReactorEvent.SELECT_CANCEL;
import static org.simpleframework.transport.reactor.ReactorEvent.SELECT_EXPIRED;
import static org.simpleframework.transport.reactor.ReactorEvent.UPDATE_INTEREST;
import static org.simpleframework.transport.reactor.ReactorEvent.UPDATE_READ_INTEREST;
import static org.simpleframework.transport.reactor.ReactorEvent.UPDATE_WRITE_INTEREST;
import static org.simpleframework.transport.reactor.ReactorEvent.WRITE_INTEREST_READY;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.simpleframework.common.thread.Daemon;
import org.simpleframework.transport.trace.Trace;
 
 /**
  * The <code>ActionDistributor</code> is used to execute operations
  * that have an interested I/O event ready. This acts much like a
  * scheduler would in that it delays the execution of the operations
  * until such time as the associated <code>SelectableChannel</code>
  * has an interested I/O event ready.
  * <p>
  * This distributor has two modes, one mode is used to cancel the
  * channel once an I/O event has occurred. This means that the channel
  * is removed from the <code>Selector</code> so that the selector 
  * does not break when asked to select again. cancelling the channel
  * is useful when the operation execution may not fully read the 
  * payload or when the operation takes a significant amount of time.
  *
  * @see org.simpleframework.transport.reactor.ExecutorReactor
  */ 
class ActionDistributor extends Daemon implements OperationDistributor {
   
   /**
    * This is used to determine the operations that need cancelling.
    */ 
   private Map<Channel, ActionSet> executing;
   
   /**
    * This is used to keep track of actions currently in selection.
    */
   private Map<Channel, ActionSet> selecting;   
   
   /**
    * This is the queue that is used to invalidate channels.
    */ 
   private Queue<Channel> invalid;       
   
   /**
    * This is the queue that is used to provide the operations.
    */ 
   private Queue<Action> pending;  
   
  /**
   * This is the selector used to select for interested events.
   */ 
   private ActionSelector selector;   
 
   /**
    * This is used to execute the operations that are ready.
    */ 
   private Executor executor;        
   
   /**
    * This is used to signal when the distributor has closed.
    */
   private Latch latch;
   
   /**
    * This is the duration in milliseconds the operation expires in.
    */
   private long expiry;
 
   /**
    * This is time in milliseconds when the next expiry will occur.
    */
   private long update;
 
   /**
    * This is used to determine the mode the distributor uses.
    */ 
   private boolean cancel;
   
   /**
    * Constructor for the <code>ActionDistributor</code> object. This 
    * will create a distributor that distributes operations when those
    * operations show that they are ready for a given I/O event. The
    * interested I/O events are provided as a bitmask taken from the
    * actions of the <code>SelectionKey</code>. Distribution of the
    * operations is passed to the provided executor object.
    *
    * @param executor this is the executor used to execute operations
    */   
   public ActionDistributor(Executor executor) throws IOException {
      this(executor, true);
   } 
 
   /**
    * Constructor for the <code>ActionDistributor</code> object. This 
    * will create a distributor that distributes operations when those
    * operations show that they are ready for a given I/O event. The
    * interested I/O events are provided as a bitmask taken from the
    * actions of the <code>SelectionKey</code>. Distribution of the
    * operations is passed to the provided executor object.
    *
    * @param executor this is the executor used to execute operations
    * @param cancel should the channel be removed from selection
    */   
   public ActionDistributor(Executor executor, boolean cancel) throws IOException {
      this(executor, cancel, 120000);
   }
   
   /**
    * Constructor for the <code>ActionDistributor</code> object. This 
    * will create a distributor that distributes operations when those
    * operations show that they are ready for a given I/O event. The
    * interested I/O events are provided as a bitmask taken from the
    * actions of the <code>SelectionKey</code>. Distribution of the
    * operations is passed to the provided executor object.
    *
    * @param executor this is the executor used to execute operations
    * @param cancel should the channel be removed from selection
    * @param expiry this the maximum idle time for an operation
    */   
   public ActionDistributor(Executor executor, boolean cancel, long expiry) throws IOException {
      this.selecting = new LinkedHashMap<Channel, ActionSet>();
      this.executing = new LinkedHashMap<Channel, ActionSet>();
      this.pending = new ConcurrentLinkedQueue<Action>();
      this.invalid = new ConcurrentLinkedQueue<Channel>();      
      this.selector = new ActionSelector();  
      this.latch = new Latch();
      this.executor = executor;    
      this.cancel = cancel;
      this.expiry = expiry;
      this.start(); 
   }   
   
   /**
    * This is used to process the <code>Operation</code> object. This
    * will wake up the selector if it is currently blocked selecting
    * and register the operations associated channel. Once the 
    * selector is awake it will acquire the operation from the queue
    * and register the associated <code>SelectableChannel</code> for
    * selection. The operation will then be executed when the channel
    * is ready for the interested I/O events.
    * 
    * @param task this is the task that is scheduled for distribution   
    * @param require this is the bit-mask value for interested events
    */ 
   public void process(Operation task, int require) throws IOException {
      Action action = new ExecuteAction(task, require, expiry);
      
      if(!isActive())  {
         throw new IOException("Distributor is closed");
      }
      pending.offer(action);
      selector.wake();
   }
   
   /**
    * This is used to close the distributor such that it cancels all
    * of the registered channels and closes down the selector. This
    * is used when the distributor is no longer required, after the
    * close further attempts to process operations will fail.
    */ 
   public void close() throws IOException {  
      stop();  
      selector.wake();
      latch.close();
   }   
   
   /**
    * This returns the number of channels that are currently selecting
    * with this distributor. When busy this can get quite high, however
    * it must return to zero as soon as all tasks have completed.
    * 
    * @return return the number of channels currently selecting
    */
   public int size() {
      return selecting.size();
   }
   
   /**
    * Performs the execution of the distributor. Each distributor runs 
    * on an asynchronous thread to the <code>Reactor</code> which is
    * used to perform the selection on a set of channels. Each  time 
    * there is a new operation to be processed this will take the
    * operation from the ready queue, cancel all outstanding channels,
    * and register the operations associated channel for selection.   
    */ 
   public void run() {
      try {
         execute();
      } finally {
         purge();
      }
   }
   
   /**
    * Performs the execution of the distributor. Each distributor runs 
    * on an asynchronous thread to the <code>Reactor</code> which is
    * used to perform the selection on a set of channels. Each  time 
    * there is a new operation to be processed this will take the
    * operation from the ready queue, cancel all outstanding channels,
    * and register the operations associated channel for selection.   
    */ 
   private void execute() {
      while(isActive()) {
         try {
            register();
            cancel(); 
            expire();
            distribute(); 
            validate();
         } catch(Exception cause) {
            report(cause);           
         }            
      }    
   }
   
   /**
    * This will purge all the actions from the distributor when the
    * distributor ends. If there are any threads waiting on the close
    * to finish they are signalled when all operations are purged.
    * This will allow them to return ensuring no operations linger.
    */
   private void purge() {
      try {
         register();
         cancel();
         clear();
      } catch(Exception cause) {
         report(cause);
      }
   }
   
   /**
    * This method is called to ensure that if there is a global 
    * error that each action will know about it. Such an issue could
    * be file handle exhaustion or an out of memory error. It is
    * also possible that a poorly behaving action could cause an
    * issue which should be know the the entire system.
    * 
    * @param cause this is the exception to report
    */
   private void report(Exception cause) {
      Set<Channel> channels = selecting.keySet();
      
      for(Channel channel : channels) {
         ActionSet set = selecting.get(channel);
         Action[] list = set.list();
            
         for(Action action : list) {         
            Operation operation = action.getOperation();
            Trace trace = operation.getTrace();
            
            try {
               trace.trace(ERROR, cause);
            } catch(Exception e) {
               invalid.offer(channel);
            }
         }
      }
      invalid.clear();
   }   
   
   /**   
    * Here we perform an expire which will take all of the registered
    * sockets and expire it. This ensures that the operations can be
    * executed within the executor and the cancellation of the sockets
    * can be performed. Once this method has finished then all of 
    * the operations will have been scheduled for execution.
    */
   private void clear() throws IOException {
      List<ActionSet> sets = selector.registeredSets();
      
      for(ActionSet set : sets) {
         Action[] list = set.list();
         
         for(Action action : list) {
            Operation task = action.getOperation();
            Trace trace = task.getTrace();
            
            try {
               trace.trace(CLOSE_SELECTOR);         
               expire(set, Long.MAX_VALUE);   
            } catch(Exception cause) {
               trace.trace(ERROR, cause);
            } 
         }
      }
      selector.close();
      latch.signal();
   }
   
   /**
    * This method is used to expire registered operations that remain
    * idle within the selector. Operations specify a time at which 
    * point they wish to be cancelled if the I/O event they wait on
    * has not arisen. This will enables the cancelled operation to be
    * cancelled so that the resources it occupies can be released. 
    */
   private void expire() throws IOException {
      List<ActionSet> sets = selector.registeredSets();
      
      if(cancel) {
         long time = System.currentTimeMillis();
         
         if(update <= time) {
            for(ActionSet set : sets) {          
               expire(set, time);           
            }
            update = time +10000;
         }
      }
   }
   
   /**
    * This method is used to expire registered operations that remain
    * idle within the selector. Operations specify a time at which 
    * point they wish to be  if the I/O event they wait on
    * has not arisen. This will enables the cancelled operation to be
    * cancelled so that the resources it occupies can be released.
    * 
    * @param set this is the selection set check for expired actions
    * @param time this is the time to check the expiry against
    */
   private void expire(ActionSet set, long time) throws IOException {
      Action[] actions = set.list();
      SelectionKey key = set.key();
      
      if(key.isValid()) {
         int mask = key.interestOps();
         
         for(Action action : actions) {
            int interest = action.getInterest();
            long expiry = action.getExpiry();
            
            if(expiry < time) {
               expire(set, action);         
               mask &= ~interest;
            }
         }
         update(set, mask);
      }
   }
   
   /**
    * This is used to update the interested operations of a set of
    * actions. If there are no interested operations the set will be
    * cancelled, otherwise the selection key will be updated with the
    * new operations provided by the bitmask.
    * 
    * @param set this is the action set that is to be updated
    * @param interest this is the bitmask containing the operations
    */
   private void update(ActionSet set, int interest) throws IOException {
      SelectionKey key = set.key();
      
      if(interest == 0) {
         Channel channel = key.channel();
         
         selecting.remove(channel);
         key.cancel();
      } else {
         key.interestOps(interest);
      }
   }
   
   /**
    * This method is used to expire registered operations that remain
    * idle within the selector. Operations specify a time at which 
    * point they wish to be cancelled if the I/O event they wait on
    * has not arisen. This will enables the cancelled operation to be
    * cancelled so that the resources it occupies can be released. 
    * 
    * @param set this is the action set containing the actions
    * @param action this is the actual action to be cancelled        
    */
   private void expire(ActionSet set, Action action) throws IOException {
      Action cancel = new CancelAction(action);
      
      if(set != null) {
         Operation task = action.getOperation();
         Trace trace = task.getTrace();
         int interest = action.getInterest();
         
         try {
            trace.trace(SELECT_EXPIRED, interest);         
            set.remove(interest);
            execute(cancel);
         } catch(Exception cause) {
            trace.trace(ERROR, cause);
         }
      }
   }
   
   /**
    * This method is used to perform simple validation. It ensures 
    * that directly after the processing loop any channels that
    * are registered that have been cancelled or are closed will
    * be removed from the selecting map and rejected.
    */
   private void validate() throws IOException {
      Set<Channel> channels = selecting.keySet();
      
      for(Channel channel : channels) {
         ActionSet set = selecting.get(channel);
         SelectionKey key = set.key();
         
         if(!key.isValid()) {
            invalid.offer(channel);
         }
      }
      for(Channel channel : invalid) {
         invalidate(channel);
      }
      invalid.clear();
   }
   
   /**
    * This method is used to remove the channel from the selecting
    * registry. It is rare that this will every happen, however it
    * is important that tasks are cleared out in this manner as it
    * could lead to a memory leak if left for a long time.
    * 
    * @param channel this is the channel being validated
    */
   private void invalidate(Channel channel) throws IOException {
      ActionSet set = selecting.remove(channel);
      Action[] list = set.list();
         
      for(Action action : list) {
         Operation task = action.getOperation();
         Trace trace = task.getTrace();
               
         try {
            trace.trace(INVALID_KEY);               
            execute(action); // reject
         } catch(Exception cause) {            
            trace.trace(ERROR, cause);            
         }
      }      
   }
 
   /**
    * This is used to cancel any selection keys that have previously
    * been selected with an interested I/O event. Performing a cancel
    * here ensures that on a the next select the associated channel
    * is not considered, this ensures the select does not break.
    */ 
   private void cancel() throws IOException {    
      Collection<ActionSet> list = executing.values();
         
      for(ActionSet set : list) {
         Action[] actions = set.list();
         
         for(Action action : actions) {
            Operation task = action.getOperation();
            Trace trace = task.getTrace();
            
            trace.trace(SELECT_CANCEL);
         }
         set.cancel();
         set.clear();
      }     
      executing.clear();
   }
 
   /**
    * Here all the enqueued <code>Operation</code> objects will be 
    * registered for selection. Each operations channel is used for
    * selection on the interested I/O events. Once the I/O event
    * occurs for the channel the operation is scheduled for execution.   
    */ 
   private void register() throws IOException {
      while(!pending.isEmpty()) {
         Action action = pending.poll();
         
         if(action != null) {
            SelectableChannel channel = action.getChannel();    
            ActionSet set = executing.remove(channel);
            
            if(set == null) {
               set = selecting.get(channel); 
            }  
            if(set != null) {
               update(action, set);
            } else {
               register(action);
            }
         }
      }
   }

   /**
    * Here the specified <code>Operation</code> object is registered
    * with the selector. If the associated channel had previously 
    * been cancelled it is removed from the cancel map to ensure it
    * is not removed from the selector when cancellation is done.
    *
    * @param action this is the operation that is to be registered   
    */
   private void register(Action action) throws IOException {
      SelectableChannel channel = action.getChannel();
      Operation task = action.getOperation();
      Trace trace = task.getTrace();
      
      try {
         if(channel.isOpen()) {
            trace.trace(SELECT);              
            select(action);               
         } else {
            trace.trace(CHANNEL_CLOSED);            
            selecting.remove(channel);
            execute(action); // reject
         }
      }catch(Exception cause) {
         trace.trace(ERROR, cause);
      }
   }   
   
   /**
    * Here the specified <code>Operation</code> object is registered
    * with the selector. If the associated channel had previously 
    * been cancelled it is removed from the cancel map to ensure it
    * is not removed from the selector when cancellation is done.
    *
    * @param action this is the operation that is to be registered
    * @param set this is the action set to register the action with     
    */
   private void update(Action action, ActionSet set) throws IOException {
      Operation task = action.getOperation();
      Trace trace = task.getTrace();
      SelectionKey key = set.key();
      int interest = action.getInterest();
      int current = key.interestOps();
      int updated = current | interest;
     
      try {
         if(OP_READ == (interest & OP_READ)) {
            trace.trace(UPDATE_READ_INTEREST);
         } 
         if(OP_WRITE == (interest & OP_WRITE)) {
            trace.trace(UPDATE_WRITE_INTEREST);
         }       
         trace.trace(UPDATE_INTEREST, updated);
         key.interestOps(updated);
         set.attach(action);
      } catch(Exception cause) {      
         trace.trace(ERROR, cause);      
      }
   }
   
   /**
    * This method is used to perform an actual select on a channel. It
    * will register the channel with the internal selector using the
    * required I/O event bit mask. In order to ensure that selection 
    * is performed correctly the provided channel must be connected.
    * 
    * @param action this is the operation that is to be registered 
    * 
    * @return this returns the selection key used for selection
    */
   private void select(Action action) throws IOException {
      SelectableChannel channel = action.getChannel();
      Operation task = action.getOperation();
      Trace trace = task.getTrace();
      int interest = action.getInterest();
      
      if(interest > 0) {
         ActionSet set = selector.register(channel, interest);  
         
         if(OP_READ == (interest & OP_READ)) {
            trace.trace(REGISTER_READ_INTEREST);
         } 
         if(OP_WRITE == (interest & OP_WRITE)) {
            trace.trace(REGISTER_WRITE_INTEREST);
         }
         trace.trace(REGISTER_INTEREST, interest);         
         set.attach(action);
         selecting.put(channel, set);         
      }
   }
 
   /**
    * This method is used to perform the select and if required queue
    * the operations that are ready for execution. If the selector 
    * is woken up without any ready channels then this will return
    * quietly. If however there are a number of channels ready to be
    * processed then they are handed to the executor object and 
    * marked as ready for cancellation.
    */ 
   private void distribute() throws IOException {      
      if(selector.select(5000) > 0) {
         if(isActive()) {           
            process();            
         }
      }  
   }  
 
   /**
    * This will iterate over the set of selection keys and process each
    * of them. The <code>Operation</code> associated with the selection
    * key is handed to the executor to perform the channel operation.
    * Also, if configured to cancel, this method will add the channel
    * and the associated selection key to the cancellation map.
    */ 
   private void process() throws IOException{  
      List<ActionSet> ready = selector.selectedSets();
      
      for(ActionSet set : ready) {
         process(set);
         remove(set);
      }
   }
 
   /**
    * This will use the specified action set to acquire the channel
    * and <code>Operation</code> associated with it to hand to the
    * executor to perform the channel operation.
    *
    * @param set this is the set of actions that are to be processed
    */ 
   private void process(ActionSet set) throws IOException {
      Action[] actions = set.ready();
      
      for(Action action : actions) {
         Operation task = action.getOperation();
         Trace trace = task.getTrace();
         int interest = action.getInterest();
         
         try {
            if(OP_READ == (interest & OP_READ)) {
               trace.trace(READ_INTEREST_READY, interest);
            } 
            if(OP_WRITE == (interest & OP_WRITE)) {
               trace.trace(WRITE_INTEREST_READY, interest);
            } 
            execute(action);
         } catch(Exception cause) {
            trace.trace(ERROR, cause);
         }
      } 
   }
   
   /**
    * This method ensures that references to the actions and channel
    * are cleared from this instance. To ensure there are no memory
    * leaks it is important to clear out all actions and channels.
    * Also, if configured to cancel executing actions this will
    * register the channel and actions to cancel on the next loop.
    *
    * @param set this is the set of actions that are to be removed
    */ 
   private void remove(ActionSet set) throws IOException {
      Channel channel = set.channel();
      SelectionKey key = set.key();
      
      if(key.isValid()) {
         int interest = set.interest();
         int ready = key.readyOps();
    
         if(cancel) {
            int remaining = interest & ~ready; 
   
            if(remaining == 0) {               
               executing.put(channel, set); 
            } else {       
               key.interestOps(remaining);       
            }
            set.remove(ready);   
         }
      } else {
         selecting.remove(channel);
      }
   }
   
   /**
    * This is where the action is handed off to the executor. Before
    * the action is executed a trace event is generated, this will
    * ensure that the entry and exit points can be tracked. It is
    * also useful in debugging performance issues and memory leaks.
    * 
    * @param action this is the action to execute
    */
   private void execute(Action action) {
      Operation task = action.getOperation();
      Trace trace = task.getTrace();
      int interest = action.getInterest();
      
      try {
         trace.trace(EXECUTE_ACTION, interest);
         executor.execute(action);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
      }
   }
}

  