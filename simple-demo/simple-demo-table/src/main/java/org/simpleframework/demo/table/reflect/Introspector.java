package org.simpleframework.demo.table.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Introspector {

   public static Class getDependent(Class type) {
      Class[] types = getDependents(type);

      if (types != null) {
         return types[0];
      }
      return null;
   }

   public static Class[] getDependents(Class type) {
      ParameterizedType paramType = (ParameterizedType) type.getGenericSuperclass();

      if (paramType != null) {
         return getClasses(paramType);
      }
      return null;
   }

   public static Class getDependent(Field field) {
      ParameterizedType type = getType(field);

      if (type != null) {
         return getClass(type);
      }
      return null;
   }

   public static Class[] getDependents(Field field) {
      ParameterizedType type = getType(field);

      if (type != null) {
         return getClasses(type);
      }
      return new Class[] {};
   }

   private static ParameterizedType getType(Field field) {
      Type type = field.getGenericType();

      if (type instanceof ParameterizedType) {
         return (ParameterizedType) type;
      }
      return null;
   }

   public static Class getReturnDependent(Method method) {
      ParameterizedType type = getReturnType(method);

      if (type != null) {
         return getClass(type);
      }
      return null;
   }

   public static Class[] getReturnDependents(Method method) {
      ParameterizedType type = getReturnType(method);

      if (type != null) {
         return getClasses(type);
      }
      return new Class[] {};
   }

   private static ParameterizedType getReturnType(Method method) {
      Type type = method.getGenericReturnType();

      if (type instanceof ParameterizedType) {
         return (ParameterizedType) type;
      }
      return null;
   }

   public static Class getParameterDependent(Method method, int index) {
      ParameterizedType type = getParameterType(method, index);

      if (type != null) {
         return getClass(type);
      }
      return null;
   }

   public static Class[] getParameterDependents(Method method, int index) {
      ParameterizedType type = getParameterType(method, index);

      if (type != null) {
         return getClasses(type);
      }
      return new Class[] {};
   }

   public static Class getParameterDependent(Constructor factory, int index) {
      ParameterizedType type = getParameterType(factory, index);

      if (type != null) {
         return getClass(type);
      }
      return null;
   }

   public static Class[] getParameterDependents(Constructor factory, int index) {
      ParameterizedType type = getParameterType(factory, index);

      if (type != null) {
         return getClasses(type);
      }
      return new Class[] {};
   }

   private static ParameterizedType getParameterType(Method method, int index) {
      Type[] list = method.getGenericParameterTypes();

      if (list.length > index) {
         Type type = list[index];

         if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
         }
      }
      return null;
   }

   private static ParameterizedType getParameterType(Constructor factory, int index) {
      Type[] list = factory.getGenericParameterTypes();

      if (list.length > index) {
         Type type = list[index];

         if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
         }
      }
      return null;
   }

   private static Class getClass(ParameterizedType type) {
      Type[] list = type.getActualTypeArguments();

      if (list.length > 0) {
         return getClass(list[0]);
      }
      return null;
   }

   private static Class[] getClasses(ParameterizedType type) {
      Type[] list = type.getActualTypeArguments();
      Class[] types = new Class[list.length];

      for (int i = 0; i < list.length; i++) {
         types[i] = getClass(list[i]);
      }
      return types;
   }

   private static Class getClass(Type type) {
      if (type instanceof Class) {
         return (Class) type;
      }
      return getGenericClass(type);
   }

   private static Class getGenericClass(Type type) {
      if (type instanceof GenericArrayType) {
         return getArrayClass(type);
      }
      return Object.class;
   }

   private static Class getArrayClass(Type type) {
      GenericArrayType generic = (GenericArrayType) type;
      Type array = generic.getGenericComponentType();
      Class entry = getClass(array);

      if (entry != null) {
         return Array.newInstance(entry, 0).getClass();
      }
      return null;
   }

   public static String getName(String name) {
      int length = name.length();

      if (length > 0) {
         char[] array = name.toCharArray();
         char first = array[0];

         if (!isAcronym(array)) {
            array[0] = toLowerCase(first);
         }
         return new String(array);
      }
      return name;
   }

   private static boolean isAcronym(char[] array) {
      if (array.length < 2) {
         return false;
      }
      if (!isUpperCase(array[0])) {
         return false;
      }
      return isUpperCase(array[1]);
   }

   private static char toLowerCase(char value) {
      return Character.toLowerCase(value);
   }

   private static boolean isUpperCase(char value) {
      return Character.isUpperCase(value);
   }
}
