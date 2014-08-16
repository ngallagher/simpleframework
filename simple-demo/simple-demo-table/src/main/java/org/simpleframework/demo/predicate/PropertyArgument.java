package org.simpleframework.demo.predicate;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.demo.table.reflect.PropertyBinder;

public class PropertyArgument implements Argument {
   
   private final Map<String, Object> cache;
   private final PropertyBinder binder;
   private final Object source;
   
   public PropertyArgument(PropertyBinder binder, Object source) {
      this.cache = new HashMap<String, Object>();
      this.binder = binder;
      this.source = source;
   }

   @Override
   public Object getAttribute(String name) {
      if(cache.containsKey(name)) {
         return cache.get(name);
      }
      Object value = binder.getValue(name, source);
      
      if(name != null) {
         cache.put(name, value);
      }
      return value;
   }

}
