package org.simpleframework.demo.collection;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LeastRecentlyUsedMap<K, V> extends LinkedHashMap<K, V> {

   private final RemovalListener<K, V> removalListener;
   private final int capacity;

   public LeastRecentlyUsedMap() {
      this(null);
   }

   public LeastRecentlyUsedMap(int capacity) {
      this(null, capacity);
   }

   public LeastRecentlyUsedMap(RemovalListener<K, V> removalListener) {
      this(removalListener, 100);
   }

   public LeastRecentlyUsedMap(RemovalListener<K, V> removalListener, int capacity) {
      this.removalListener = removalListener;
      this.capacity = capacity;
   }

   @Override
   protected boolean removeEldestEntry(Entry<K, V> eldest) {
      int size = size();

      if (size <= capacity) {
         return false;
      }
      if (removalListener != null) {
         V value = eldest.getValue();
         K key = eldest.getKey();

         removalListener.notifyRemoved(key, value);
      }
      return true;
   }

   public static interface RemovalListener<K, V> {
      public void notifyRemoved(K key, V value);
   }
}
