package org.simpleframework.demo.table.reflect;

public class StringConverter {

   public static Object convert(Class type, String value) throws Exception {
      Class actual = convert(type);
      
      if (actual == String.class) {
         return value;
      }
      if (actual == Integer.class) {
         return Integer.parseInt(value);
      }
      if (actual == Double.class) {
         return Double.parseDouble(value);
      }
      if (actual == Float.class) {
         return Float.parseFloat(value);
      }
      if (actual == Boolean.class) {
         return Boolean.parseBoolean(value);
      }
      if (actual == Byte.class) {
         return Byte.parseByte(value);
      }
      if (actual == Short.class) {
         return Short.parseShort(value);
      }
      if (actual == Long.class) {
         return Long.parseLong(value);
      }
      if (actual == Character.class) {
         return value.charAt(0);
      }
      if(actual == Class.class) {
         return Class.forName(value);
      }
      if (Enum.class.isAssignableFrom(type)) {
         return Enum.valueOf(type, value);
      }
      return value;
   }
   
   private static Class convert(Class type) throws Exception {
      if (type == int.class) {
         return Integer.class;
      }
      if (type == double.class) {
         return Double.class;
      }
      if (type == float.class) {
         return Float.class;
      }
      if (type == boolean.class) {
         return Boolean.class;
      }
      if (type == byte.class) {
         return Byte.class;
      }
      if (type == short.class) {
         return Short.class;
      }
      if (type == long.class) {
         return Long.class;
      }
      if (type == char.class) {
         return Character.class;
      }
      return type;
   }
}
