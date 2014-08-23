/*
 * ActionSelector.java February 2013
 *
 * Copyright (C) 2013, Niall Gallagher <niallg@users.sf.net>
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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The <code>ActionSelector</code> object is used to perform socket
 * based selection with the help of the <code>ActionSet</code> object.
 * All registered channels have an associated action set. The action
 * set contains a set of actions that should be executed when the
 * selector selects the channel.
 * 
 * @author Niall Gallagher
 */
class ActionSelector {
   
   /**
    * This is the selector used to perform the select operations.
    */
   private final Selector selector;
   
   /**
    * Constructor for the <code>ActionSelector</code> object. This is
    * used to create a selector that can register socket channels
    * with an associated <code>ActionSet</code>. The set can then be
    * used to store actions to be executed upon selection.
    */
   public ActionSelector() throws IOException {
      this.selector = Selector.open();
   }  
   
   /**
    * This is used to perform a select on the selector. This returns
    * the number of channels that have been selected. If this returns
    * a number greater than zero the <code>ready</code> method can
    * be used to acquire the actions that are ready for execution.
    * 
    * @param timeout this is the timeout associated with the select
    * 
    * @return this returns the number of channels that are ready
    */
   public int select(long timeout) throws IOException {
      return selector.select(timeout);
   }
   
   /**
    * This performs the actual registration of the channel for selection
    * based on the provided interest bitmask. When the channel has been
    * registered for selection this returns an <code>ActionSet</code> 
    * for the selection key. The set can then be used to register the
    * actions that should be executed when selection succeeds. 
    * 
    * @param channel this is the channel to register for selection
    * @param interest this is the interested operations bitmask
    * 
    * @return this is the action set associated with the registration
    */
   public ActionSet register(SelectableChannel channel, int interest) throws IOException {
      SelectionKey key = channel.register(selector, interest);
      Object value = key.attachment();
      
      if(value == null) {
         value = new ActionSet(key);
         key.attach(value);
      }
      return (ActionSet)value;
   }
   
   /**
    * This is used to acquire all the action sets that are associated
    * with this selector. Only action sets that have a valid selection
    * key are returned. Modification of the list will not affect the
    * associated selector instance.
    * 
    * @return this returns all the associated action sets for this
    */
   public List<ActionSet> registeredSets() {
      Set<SelectionKey> keys = selector.keys();
      Iterator<SelectionKey> ready = keys.iterator();
     
      return registeredSets(ready);
   }
   
   /**
    * This is used to acquire all the action sets that are associated
    * with this selector. Only action sets that have a valid selection
    * key are returned. Modification of the list will not affect the
    * associated selector instance.
    * 
    * @param keys the selection keys to get the associated sets from
    * 
    * @return this returns all the associated action sets for this
    */
   private List<ActionSet> registeredSets(Iterator<SelectionKey> keys) {
      List<ActionSet> sets = new LinkedList<ActionSet>();
      
      while(keys.hasNext()) {
         SelectionKey key = keys.next();
         ActionSet actions = (ActionSet)key.attachment();
 
         if(!key.isValid()) {
            key.cancel();
         } else {
            sets.add(actions);
         }
      }  
      return sets;
   }
   
   /**
    * This is used to acquire all the action sets that are selected
    * by this selector. All action sets returned are unregistered from
    * the selector and must be registered again to hear about further
    * I/O events that occur on the associated channel.
    * 
    * @return this returns all the selected action sets for this
    */
   public List<ActionSet> selectedSets() throws IOException {
      Set<SelectionKey> keys = selector.selectedKeys();
      Iterator<SelectionKey> ready = keys.iterator();
     
      return selectedSets(ready);
   }
   
   /**
    * This is used to acquire all the action sets that are selected
    * by this selector. All action sets returned are unregistered from
    * the selector and must be registered again to hear about further
    * I/O events that occur on the associated channel.
    * 
    * @param keys the selection keys to get the associated sets from
    * 
    * @return this returns all the selected action sets for this
    */
   private List<ActionSet> selectedSets(Iterator<SelectionKey> keys) {
      List<ActionSet> ready = new LinkedList<ActionSet>();
      
      while(keys.hasNext()) {
         SelectionKey key = keys.next();
         ActionSet actions = (ActionSet)key.attachment();
 
         if(key != null) {
            keys.remove();
         }
         if(key != null) {
            ready.add(actions);
         }
      }  
      return ready;
   }
   
   /**
    * This is used to wake the selector if it is in the middle of a 
    * select operation. Waking up the selector in this manner is 
    * useful if further actions are to be registered with it.
    */
   public void wake() throws IOException {
      selector.wakeup();
   }
   
   /**
    * This is used to close the associated selector. Further attempts
    * to register a channel with the selector will fail. All actions
    * should be cancelled before closing the selector in this way.
    */
   public void close() throws IOException {
      selector.close();
   }
}
