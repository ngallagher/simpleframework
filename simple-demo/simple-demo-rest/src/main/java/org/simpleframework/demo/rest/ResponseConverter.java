package org.simpleframework.demo.rest;

public class ResponseConverter {

   public ResponseConverter() {
      super();
   }

   public boolean accept(Class type) {
      Class actual = convert(type);

      if (actual == String.class) {
         return true;
      }
      if (actual == Integer.class) {
         return true;
      }
      if (actual == Double.class) {
         return true;
      }
      if (actual == Float.class) {
         return true;
      }
      if (actual == Boolean.class) {
         return true;
      }
      if (actual == Byte.class) {
         return true;
      }
      if (actual == Short.class) {
         return true;
      }
      if (actual == Long.class) {
         return true;
      }
      if (actual == Character.class) {
         return true;
      }
      return false;
   }

   public Object convert(Class type, String value) {
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
      return value;
   }

   private Class convert(Class type) {
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
