package org.simpleframework.demo.table.reflect;

import static org.simpleframework.demo.table.reflect.PropertyAccessor.getMethod;

import java.lang.reflect.Method;
import java.util.Map;

public class PropertyKeyAccessor implements Accessor {
   
   private final Method method;
   private final Object key;
   private final Class entry;
   private final Class type;
   private final String name;
   
   public PropertyKeyAccessor(String name, Class type, Class entry, Object key) {
      this.method = getMethod(name, type);
      this.entry = entry;
      this.key = key;
      this.type = type;
      this.name = name;
   }   

   @Override
   public Class getType() {
      return entry;
   }    

   @Override
   public <T> T getValue(Object source) {
      try {
         if (!method.isAccessible()) {
            method.setAccessible(true);
         }
         Object value = method.invoke(source);
         Class type = method.getReturnType();
         
         if(value != null) {
            return (T)((Map)value).get(key);
         }         
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      return null;
   }  
}
