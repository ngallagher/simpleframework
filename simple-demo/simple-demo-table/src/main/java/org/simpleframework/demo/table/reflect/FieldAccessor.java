package org.simpleframework.demo.table.reflect;

import java.lang.reflect.Field;

public class FieldAccessor implements Accessor {

   private final Field field;

   public FieldAccessor(String name, Class type) {
      this.field = getField(name, type);
   }

   public Class getType() {
      return field.getType();
   }

   public <T> T getValue(Object source) {
      try {
         if (!field.isAccessible()) {
            field.setAccessible(true);
         }
         return (T) field.get(source);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static Field getField(String name, Class type) {
      Field[] fields = type.getDeclaredFields();

      while (type != null) {
         for (Field field : fields) {
            String fieldName = field.getName();

            if (fieldName.equals(name)) {
               return field;
            }
         }
         type = type.getSuperclass();
      }
      return null;
   }
}
