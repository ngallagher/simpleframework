/*
 * MapParser.java February 2005
 *
 * Copyright (C) 2005, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.common.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The <code>MapParser</code> object represents a parser for name
 * value pairs. Any parser extending this will typically be parsing
 * name=value tokens or the like, and inserting these pairs into 
 * the internal map. This type of parser is useful as it exposes all
 * pairs extracted using the <code>java.util.Map</code> interface
 * and as such can be used with the Java collections framework. The
 * internal map used by this is a <code>Hashtable</code>, however
 * subclasses are free to assign a different type to the map used.
 * 
 * @author Niall Gallagher
 */ 
public abstract class MapParser<T> extends Parser implements Map<T, T> {

   /**
    * Represents all values inserted to the map as a list of values.
    */
   protected Map<T, List<T>> all;
   
   /**
    * Represents the last value inserted into this map instance.
    */
   protected Map<T, T> map;

   /**
    * Constructor for the <code>MapParser</code> object. This is 
    * used to create a new parser that makes use of a thread safe
    * map implementation. The <code>HashMap</code> is used so
    * that the resulting parser can be accessed in a concurrent
    * environment with the fear of data corruption.
    */ 
   protected MapParser(){
      this.all = new HashMap<T, List<T>>();
      this.map = new HashMap<T, T>();                
   }

   /**
    * This is used to determine whether a token representing the
    * name of a pair has been inserted into the internal map. The
    * object passed into this method should be a string, as all
    * tokens stored within the map will be stored as strings.
    *  
    * @param name this is the name of a pair within the map
    *
    * @return this returns true if the pair of that name exists
    */
   public boolean containsKey(Object name) {
      return map.containsKey(name);
   }
   
   /**
    * This method is used to determine whether any pair that has
    * been inserted into the internal map had the presented value.
    * If one or more pairs within the collected tokens contains
    * the value provided then this method will return true.
    * 
    * @param value this is the value that is to be searched for
    *
    * @return this returns true if any value is equal to this
    */
   public boolean containsValue(Object value) {
      return map.containsValue(value);
   }
   
   /**
    * This method is used to acquire the name and value pairs that
    * have currently been collected by this parser. This is used
    * to determine which tokens have been extracted from the 
    * source. It is useful when the tokens have to be gathered.
    *
    * @return this set of token pairs that have been extracted
    */
   public Set<Map.Entry<T, T>> entrySet() {
      return map.entrySet();
   }
   
   /**
    * The <code>get</code> method is used to acquire the value for
    * a named pair. So if a pair of name=value has been parsed and
    * inserted into the collection of tokens this will return the
    * value given the name. The value returned will be a string.
    *
    * @param name this is a string used to search for the value
    *
    * @return this is the value, as a string, that has been found 
    */
   public T get(Object name) {
      return map.get(name);
   }
   
   /**
    * This method is used to acquire a <code>List</code> for all of
    * the values that have been put in to the map. The list allows
    * all values associated with the specified key. This enables a
    * parser to collect a number of associated tokens.
    * 
    * @param key this is the key used to search for the value
    * 
    * @return this is the list of values associated with the key
    */
   public List<T> getAll(Object key) {
      return all.get(key);
   }
   
   /**
    * This method is used to determine whether the parser has any
    * tokens available. If the <code>size</code> is zero then the
    * parser is empty and this returns true. The is acts as a
    * proxy the the <code>isEmpty</code> of the internal map.
    * 
    * @return this is true if there are no available tokens
    */
   public boolean isEmpty() {
      return map.isEmpty();
   }
   
   /**
    * This is used to acquire the names for all the tokens that 
    * have currently been collected by this parser. This is used
    * to determine which tokens have been extracted from the 
    * source. It is useful when the tokens have to be gathered.
    *
    * @return the set of name tokens that have been extracted
    */
   public Set<T> keySet() {
      return map.keySet();
   }
   
   /**
    * The <code>put</code> method is used to insert the name and
    * value provided into the collection of tokens. Although it is
    * up to the parser to decide what values will be inserted it
    * is generally the case that the inserted tokens will be text.
    *
    * @param name this is the name token from a name=value pair
    * @param value this is the value token from a name=value pair
    *
    * @return this returns the previous value if there was any
    */
   public T put(T name, T value) {
      List<T> list = all.get(name);
      T first = map.get(name);
      
      if(list == null) {
         list = new ArrayList<T>();
         all.put(name, list);
      }      
      list.add(value);
      
      if(first == null) {
         return map.put(name, value);
      }
      return null;
   }
   
   /**
    * This method is used to insert a collection of tokens into 
    * the parsers map. This is used when another source of tokens
    * is required to populate the connection currently maintained
    * within this parsers internal map. Any tokens that currently
    * exist with similar names will be overwritten by this.
    * 
    * @param data this is the collection of tokens to be added
    */
   public void putAll(Map<? extends T, ? extends T> data) {
      Set<? extends T> keySet = data.keySet();
      
      for(T key : keySet) {
         T value = data.get(key);
         
         if(value != null) {
            put(key, value);
         }         
      }                 
   }
   
   /**
    * The <code>remove</code> method is used to remove the named
    * token pair from the collection of tokens. This acts like a
    * take, in that it will get the token value and remove if 
    * from the collection of tokens the parser has stored.
    *
    * @param name this is a string used to search for the value
    *
    * @return this is the value, as a string, that is removed
    */
   public T remove(Object name) {
      return map.remove(name);
   }
   
   /**
    * This obviously enough provides the number of tokens that
    * have been inserted into the internal map. This acts as
    * a proxy method for the internal map <code>size</code>.
    *
    * @return this returns the number of tokens are available
    */
   public int size() {
      return map.size();
   }
   
   /**
    * This method is used to acquire the value for all tokens that
    * have currently been collected by this parser. This is used
    * to determine which tokens have been extracted from the
    * source. It is useful when the tokens have to be gathered.
    *
    * @return the list of value tokens that have been extracted
    */
   public Collection<T> values() {
      return map.values();
   }   

   /**
    * The <code>clear</code> method is used to wipe out all the
    * currently existing tokens from the collection. This is used
    * to recycle the parser so that it can be used to parse some
    * other source of tokens without any lingering state.
    */
   public void clear() {
      all.clear();
      map.clear();      
   }
}
