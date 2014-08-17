package org.simpleframework.demo.table.reflect;

import static org.simpleframework.demo.table.reflect.PropertyAccessor.getMethod;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;

public class PropertyIndexAccessor implements Accessor {
   
   private final Method method;
   private final Integer index;
   private final Class entry;
   private final Class type;
   private final String name;
   
   public PropertyIndexAccessor(String name, Class type, Class entry, Integer index) {
      this.method = getMethod(name, type);
      this.entry = entry;
      this.index = index;
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
            if(type.isArray()) {
               return (T)Array.get(value, index);
            }
            return (T)((List)value).get(index);
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      return null;
   }  
}
