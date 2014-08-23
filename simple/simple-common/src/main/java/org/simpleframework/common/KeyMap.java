/*
 * KeyMap.java May 2007
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

package org.simpleframework.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * The <code>KeyMap</code> object is used to represent a map of values
 * keyed using a known string. This also ensures that the keys and
 * the values added to this hash map can be acquired in an independent
 * list of values, ensuring that modifications to the map do not have
 * an impact on the lists provided, and vice versa. The key map can
 * also be used in a fore each look using the string keys.
 * 
 * @author Niall Gallagher
 */
public class KeyMap<T> extends LinkedHashMap<String, T> implements Iterable<String> {
   
   /**
    * Constructor for the <code>KeyMap</code> object. This creates
    * a hash map that can expose the keys and values of the map as
    * an independent <code>List</code> containing the values. This
    * can also be used within a for loop for convenience.
    */
   public KeyMap() {
     super();            
   }          
   
   /**
    * This is used to produce an <code>Iterator</code> of values 
    * that can be used to acquire the contents of the key map within
    * a for each loop. The key map can be modified while it is been
    * iterated as the iterator is an independent list of values.
    * 
    * @return this returns an iterator of the keys in the map
    */
   public Iterator<String> iterator() {
      return getKeys().iterator();
   }

   /**
    * This is used to produce a <code>List</code> of the keys in
    * the map. The list produced is a copy of the internal keys and
    * so can be modified and used without affecting this map object.
    * 
    * @return this returns an independent list of the key values
    */
   public List<String> getKeys() {
     Set<String> keys = keySet();
     
     if(keys == null) {
        return new ArrayList<String>();
     }
     return new ArrayList<String>(keys);      
   }
   
   /**
    * This is used to produce a <code>List</code> of the values in
    * the map. The list produced is a copy of the internal values and
    * so can be modified and used without affecting this map object.
    * 
    * @return this returns an independent list of the values
    */   
   public List<T> getValues() {
      Collection<T> values = values();
      
      if(values == null) {
         return new ArrayList<T>();          
      }
      return new ArrayList<T>(values);
   }
 }