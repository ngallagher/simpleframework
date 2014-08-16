package org.simpleframework.demo.table.reflect;

import org.simpleframework.demo.collection.Cache;
import org.simpleframework.demo.collection.LeastRecentlyUsedCache;

public class PropertyBinder {
   
   private final Cache<Class, PropertyExtractor> extractors;
   private final int capacity;

   public PropertyBinder() {
      this(100);
   }
   
   public PropertyBinder(int capacity) {
      this.extractors = new LeastRecentlyUsedCache<Class, PropertyExtractor>(capacity);
      this.capacity = capacity;
   }
   
   public Object getValue(String property, Object source) {
      Class type = source.getClass(); 
      PropertyExtractor extractor = extractors.fetch(type);
      
      if(extractor == null) {
         extractor = new PropertyExtractor(type, capacity);
         extractors.cache(type, extractor);
      }
      return extractor.getValue(property, source);
   }

   
   private static class PropertyExtractor {
      
      private final Cache<String, Accessor> accessors;
      private final Class type;
      
      public PropertyExtractor(Class type, int capacity) {
         this.accessors = new LeastRecentlyUsedCache<String, Accessor>(capacity);
         this.type = type;
      }
      
      public Object getValue(String property, Object source) {
         Accessor accessor = accessors.fetch(property);     
         
         if(accessor == null) {
            accessor = new PropertyPathAccessor(property, type);
            accessors.cache(property, accessor);
         }
         return accessor.getValue(source);
      }
               
   }
}
