package org.simpleframework.demo.table.reflect;

import java.lang.reflect.Method;

public class PropertyAccessor implements Accessor {

   private final Method method;

   public PropertyAccessor(String name, Class type) {
      this.method = getMethod(name, type);
   }

   public Class getType() {
      return method.getReturnType();
   }

   public <T> T getValue(Object source) {
      try {
         if (!method.isAccessible()) {
            method.setAccessible(true);
         }
         return (T) method.invoke(source);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   protected static Method getMethod(String name, Class type) {
      Method method = getMethod(name, type, Prefix.GET);

      if (method == null) {
         method = getMethod(name, type, Prefix.IS);
      }
      if (method == null) {
         throw new IllegalArgumentException("No property named " + name + " in " + type);
      }
      return method;
   }

   protected static Method getMethod(String name, Class type, Prefix prefix) {
      Method[] methods = type.getDeclaredMethods();
      String property = prefix.getProperty(name);
      Method match = null;

      for (Method method : methods) {
         Class[] parameterTypes = method.getParameterTypes();
         String methodName = method.getName();

         if (parameterTypes.length == 0) {
            if (methodName.equals(property)) {
               return method;
            }
         }
         if (parameterTypes.length == 1) {
            if(methodName.equals(property)) {
               match = method;
            }
         }
      }
      return match;
   }

   private static enum Prefix {
      IS("is"), 
      GET("get");

      private final String prefix;

      private Prefix(String prefix) {
         this.prefix = prefix;
      }

      public String getProperty(String name) {
         char initial = name.charAt(0);
         char upperCase = Character.toUpperCase(initial);
         String end = name.substring(1);

         return String.format("%s%s%s", prefix, upperCase, end);
      }
   }
}
