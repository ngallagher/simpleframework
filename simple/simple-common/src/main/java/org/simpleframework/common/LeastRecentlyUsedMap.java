/*
 * LeastRecentlyUsedMap.java May 2007
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

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * The <code>LeastRecentlyUsedMap</code> is a hash map that keeps only
 * those entries most recently used. This acts much like a hot spot 
 * cache for specific keys that are used frequently. It also allows
 * for algorithms to keep hot spot values available in the cache 
 * without the risk of running out of memory.
 * 
 * @author Niall Gallagher
 */
public class LeastRecentlyUsedMap<K, V> extends LinkedHashMap<K, V> {

   /**
    * This is the listener that is called when an entry is removed.
    */
   private final RemovalListener<K, V> listener;

   /**
    * This is the number of items to keep within the cache.
    */
   private final int capacity;

   /** 
    * Constructor for the <code>LeastRecentlyUsedMap</code> object. This
    * creates a hash container that keeps only those entries that have
    * been recently added or accessed available within the collection.
    */
   public LeastRecentlyUsedMap() {
      this(null);
   }

   /** 
    * Constructor for the <code>LeastRecentlyUsedMap</code> object. This
    * creates a hash container that keeps only those entries that have
    * been recently added or accessed available within the collection.
    * 
    * @param capacity this is the capacity of the hash container
    */
   public LeastRecentlyUsedMap(int capacity) {
      this(null, capacity);
   }

   /** 
    * Constructor for the <code>LeastRecentlyUsedMap</code> object. This
    * creates a hash container that keeps only those entries that have
    * been recently added or accessed available within the collection.
    * 
    * @param listener this listens for entries that are removed
    */
   public LeastRecentlyUsedMap(RemovalListener<K, V> listener) {
      this(listener, 100);
   }

   /** 
    * Constructor for the <code>LeastRecentlyUsedMap</code> object. This
    * creates a hash container that keeps only those entries that have
    * been recently added or accessed available within the collection.
    * 
    * @param listener this listens for entries that are removed
    * @param capacity this is the capacity of the hash container
    */
   public LeastRecentlyUsedMap(RemovalListener<K, V> listener, int capacity) {
      this.listener = listener;
      this.capacity = capacity;
   }

   /**
    * This is used to determine if an entry should be removed from the 
    * cache. If the cache has reached its capacity then the listener,
    * if one was specified is given a callback to tell any other 
    * participating objects the entry has been removed.
    * 
    * @param eldest this is the candidate for removal
    */
   @Override
   protected boolean removeEldestEntry(Entry<K, V> eldest) {
      int size = size();

      if (size <= capacity) {
         return false;
      }
      if (listener != null) {
         V value = eldest.getValue();
         K key = eldest.getKey();

         listener.notifyRemoved(key, value);
      }
      return true;
   }

   /**
    * The <code>RemovalListener</code> is used with the least recently
    * used hash map to listen for removals. A callback is issued if
    * an entry has been removed from the container because it was
    * the least recently used entry.
    */
   public static interface RemovalListener<K, V> {
      
      /**
       * This method is called when the entry has been removed due
       * to the capacity having been reached. On removal any
       * implementation can take action using the key or value.
       * 
       * @param key this is the key of the removed entry
       * @param value this is the value of the removed entry
       */
      public void notifyRemoved(K key, V value);
   }
}
