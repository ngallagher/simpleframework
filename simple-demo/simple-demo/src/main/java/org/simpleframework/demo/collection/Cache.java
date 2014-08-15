package org.simpleframework.demo.collection;

import java.util.Set;

public interface Cache<K, V> {
   Set<K> keySet();
   V take(K key);
   V fetch(K key);
   boolean isEmpty();
   boolean contains(K key);
   void cache(K key, V value);
   void clear();
   int size();
}
