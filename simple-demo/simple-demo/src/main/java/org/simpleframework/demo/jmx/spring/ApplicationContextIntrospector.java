package org.simpleframework.demo.jmx.spring;

import static org.simpleframework.demo.jmx.reflect.ObjectType.ARRAY;
import static org.simpleframework.demo.jmx.reflect.ObjectType.COLLECTION;
import static org.simpleframework.demo.jmx.reflect.ObjectType.MAP;
import static org.simpleframework.demo.jmx.reflect.ObjectType.NULL;
import static org.simpleframework.demo.jmx.reflect.ObjectType.OBJECT;
import static org.simpleframework.demo.jmx.reflect.ObjectType.PRIMITIVE;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.simpleframework.demo.jmx.reflect.ObjectFieldInfo;
import org.simpleframework.demo.jmx.reflect.ObjectId;
import org.simpleframework.demo.jmx.reflect.ObjectInfo;
import org.simpleframework.demo.jmx.reflect.ObjectIntrospector;
import org.simpleframework.demo.jmx.reflect.ObjectPath;
import org.simpleframework.demo.jmx.reflect.ObjectType;

/**
 * This provides an object introspector that uses the spring application 
 * context to navigate to objects. Each @{link ObjectPath} will extract 
 * a bean from the context and navigate to the fields within that object.
 * 
 * @author Niall Gallagher
 */
public class ApplicationContextIntrospector implements ApplicationContextAware, ObjectIntrospector {

   private ApplicationContext context;

   public void setApplicationContext(ApplicationContext context) throws BeansException {
      this.context = context;
   }

   public ObjectInfo getObjectInfo(ObjectPath objectPath) {
      List<ObjectId> objectIdList = objectPath.getObjectPath();
      String objectName = objectPath.getObjectName();
      Object object = context.getBean(objectName);

      if (object != null) {
         ObjectPath objectPathData = new ObjectPathData(objectName);
         ObjectInfo objectInfo = new ObjectData(object, objectPathData);

         for (ObjectId objectId : objectIdList) {
            objectInfo = objectInfo.getObjectInfo(objectId);
         }
         return objectInfo;
      }
      return null;
   }

   private static class ObjectPathData implements ObjectPath {

      private List<ObjectId> objectPath;
      private String parentName;

      public ObjectPathData(String parentName) {
         this.objectPath = new ArrayList<ObjectId>();
         this.parentName = parentName;
      }

      public ObjectPath getRelativePath(ObjectId objectId) {
         ObjectPathData objectPathData = new ObjectPathData(parentName);

         for (ObjectId currentId : objectPath) {
            objectPathData.getObjectPath().add(currentId);
         }
         objectPathData.getObjectPath().add(objectId);
         return objectPathData;
      }

      public String getObjectName() {
         return parentName;
      }

      public List<ObjectId> getObjectPath() {
         return objectPath;
      }
   }

   private static class ObjectData implements ObjectInfo {

      private final Map<String, Map<String, ObjectFieldInfo>> classFields;
      private final Map<String, ObjectFieldInfo> namedFields;
      private final Collection<ObjectInfo> objectCollection;
      private final Map<ObjectInfo, ObjectInfo> objectMap;
      private final Map<String, Class> classDetails;
      private final List<String> classHierarchy;
      private final ObjectClassifier classifier;
      private final ObjectPath objectPath;
      private final Object object;

      public ObjectData(Object object, ObjectPath objectPath) {
         this.classFields = new HashMap<String, Map<String, ObjectFieldInfo>>();
         this.namedFields = new HashMap<String, ObjectFieldInfo>();
         this.objectMap = new LinkedHashMap<ObjectInfo, ObjectInfo>();
         this.objectCollection = new ArrayList<ObjectInfo>();
         this.classDetails = new HashMap<String, Class>();
         this.classHierarchy = new ArrayList<String>();
         this.classifier = new ObjectClassifier();
         this.objectPath = objectPath;
         this.object = object;
      }

      public String getClassName() {
         return object.getClass().getName();
      }

      public List<String> getClassHierarchy() {
         if (classHierarchy.isEmpty()) {
            Class type = object.getClass();

            while (type != Object.class) {
               String className = type.getName();

               classDetails.put(className, type);
               classHierarchy.add(className);
               type = type.getSuperclass();
            }
         }
         return classHierarchy;
      }

      public ObjectInfo getObjectInfo(ObjectId objectId) {
         ObjectType objectType = getObjectType();

         if (objectType == MAP) {
            return getMapValue(objectId);
         }
         if (objectType == COLLECTION) {
            return getCollectionValue(objectId);
         }
         if (objectType == ARRAY) {
            return getArrayValue(objectId);
         }
         return getObjectValue(objectId);
      }

      public ObjectInfo getArrayValue(ObjectId objectId) {
         ObjectInfo[] objectArray = getObjectArray();
         String entryValue = objectId.getEntryId();

         for (ObjectInfo objectInfo : objectArray) {
            String uniqueId = objectInfo.getUniqueId();

            if (entryValue != null && entryValue.equals(uniqueId)) {
               return objectInfo;
            }
         }
         return getObjectValue(objectId);
      }

      public ObjectInfo getCollectionValue(ObjectId objectId) {
         Collection<ObjectInfo> objectCollection = getObjectCollection();
         String entryValue = objectId.getEntryId();

         for (ObjectInfo objectInfo : objectCollection) {
            String uniqueId = objectInfo.getUniqueId();

            if (entryValue != null && entryValue.equals(uniqueId)) {
               return objectInfo;
            }
         }
         return getObjectValue(objectId);
      }

      public ObjectInfo getMapValue(ObjectId objectId) {
         Map<ObjectInfo, ObjectInfo> objectMap = getObjectMap();
         Set<ObjectInfo> keySet = objectMap.keySet();
         String keyValue = objectId.getKeyId();
         String entryValue = objectId.getEntryId();

         for (ObjectInfo keyObjectInfo : keySet) {
            ObjectInfo valueObjectInfo = objectMap.get(keyObjectInfo);
            String keyUniqueId = keyObjectInfo.getUniqueId();
            String entryUniqueId = valueObjectInfo.getUniqueId();

            if (keyValue != null && keyValue.equals(keyUniqueId)) {
               return keyObjectInfo;
            }
            if (entryValue != null && entryValue.equals(entryUniqueId)) {
               return valueObjectInfo;
            }
         }
         return getObjectValue(objectId);
      }

      public ObjectInfo getObjectValue(ObjectId objectId) {
         String className = objectId.getClassName();
         String fieldName = objectId.getObjectName();

         if (fieldName != null) {
            Map<String, ObjectFieldInfo> allFields = getFields(className);
            ObjectFieldInfo fieldInfo = allFields.get(fieldName);

            return fieldInfo.getObjectInfo();
         }
         return null;
      }

      public Map<String, ObjectFieldInfo> getFields(String className) {
         if (classFields.isEmpty()) {
            Map<String, Map<String, ObjectFieldInfo>> classFields = getClassFields();

            if (!classFields.containsKey(className)) {
               return namedFields;
            }
         }
         return classFields.get(className);
      }

      public Map<String, Map<String, ObjectFieldInfo>> getClassFields() {
         if (classFields.isEmpty()) {
            List<String> classHierarchy = getClassHierarchy();

            for (String className : classHierarchy) {
               Map<String, ObjectFieldInfo> fieldInfoList = new HashMap<String, ObjectFieldInfo>();
               Class currentType = classDetails.get(className);
               Field[] fieldList = currentType.getDeclaredFields();

               for (Field field : fieldList) {
                  ObjectFieldData fieldInfo = new ObjectFieldData(object, objectPath, field);

                  if (!fieldInfo.isStatic()) {
                     fieldInfoList.put(field.getName(), fieldInfo);
                     namedFields.put(field.getName(), fieldInfo);
                  }
               }
               classFields.put(className, fieldInfoList);
            }
         }
         return classFields;
      }

      public ObjectInfo[] getObjectArray() {
         int arrayLength = Array.getLength(object);
         ObjectInfo[] objectArray = new ObjectInfo[arrayLength];
         String className = getClassName();

         for (int index = 0; index < arrayLength; index++) {
            ObjectId objectId = new ObjectId();
            Object value = Array.get(object, index);
            String entryId = getUniqueId(value);

            objectId.setClassName(className);
            objectId.setEntryId(entryId);

            ObjectPath entryPath = objectPath.getRelativePath(objectId);
            ObjectInfo objectInfo = new ObjectData(value, entryPath);

            objectArray[index] = objectInfo;
         }
         return objectArray;
      }

      public Collection<ObjectInfo> getObjectCollection() {
         if (objectCollection.isEmpty()) {
            Collection collection = (Collection) object;
            String className = getClassName();

            for (Object value : collection) {
               ObjectId objectId = new ObjectId();
               String entryId = getUniqueId(value);

               objectId.setClassName(className);
               objectId.setEntryId(entryId);

               ObjectPath entryPath = objectPath.getRelativePath(objectId);
               ObjectInfo objectInfo = new ObjectData(value, entryPath);

               objectCollection.add(objectInfo);
            }
         }
         return objectCollection;
      }

      public Map<ObjectInfo, ObjectInfo> getObjectMap() {
         if (objectMap.isEmpty()) {
            Map map = (Map) object;
            String className = getClassName();

            for (Object key : map.keySet()) {
               ObjectId keyObjectId = new ObjectId();
               ObjectId entryObjectId = new ObjectId();
               Object value = map.get(key);
               String keyId = getUniqueId(key);
               String entryId = getUniqueId(value);

               keyObjectId.setClassName(className);
               keyObjectId.setKeyId(keyId);
               entryObjectId.setClassName(className);
               entryObjectId.setEntryId(entryId);

               ObjectPath keyPath = objectPath.getRelativePath(keyObjectId);
               ObjectPath entryPath = objectPath.getRelativePath(entryObjectId);
               ObjectInfo keyObjectInfo = new ObjectData(key, keyPath);
               ObjectInfo entryObjectInfo = new ObjectData(value, entryPath);

               objectMap.put(keyObjectInfo, entryObjectInfo);
            }
         }
         return objectMap;
      }

      public String getUniqueId(Object value) {
         if (value != null) {
            value = System.identityHashCode(value);
         }
         return String.valueOf(value);
      }

      public ObjectType getObjectType() {
         return classifier.classifyObject(object);
      }

      public String getUniqueId() {
         return getUniqueId(object);
      }

      public Object getObjectValue() {
         return object;
      }

      public ObjectPath getObjectPath() {
         return objectPath;
      }
   }

   private static class ObjectFieldData implements ObjectFieldInfo {

      private int fieldModifiers;
      private ObjectPath objectPath;
      private Object object;
      private Field field;

      public ObjectFieldData(Object object, ObjectPath objectPath, Field field) {
         this.fieldModifiers = field.getModifiers();
         this.objectPath = objectPath;
         this.object = object;
         this.field = field;
      }

      public boolean isStatic() {
         return Modifier.isStatic(fieldModifiers);
      }

      public ObjectInfo getObjectInfo() {
         ObjectId objectId = getObjectId();
         Object object = getFieldValue();
         ObjectPath fieldPath = objectPath.getRelativePath(objectId);

         return new ObjectData(object, fieldPath);
      }

      public ObjectId getObjectId() {
         ObjectId objectId = new ObjectId();
         String fieldName = field.getName();
         Class fieldClass = field.getDeclaringClass();

         objectId.setObjectName(fieldName);
         objectId.setClassName(fieldClass.getName());

         return objectId;
      }

      public String getFieldName() {
         return field.getName();
      }

      public Object getFieldValue() {
         try {
            if (!field.isAccessible()) {
               field.setAccessible(true);
            }
            return field.get(object);
         } catch (Exception e) {
            return null;
         }
      }

      public Class getFieldClass() {
         return field.getType();
      }
   }

   private static class ObjectClassifier {

      public ObjectType classifyObject(Object value) {
         if (value != null) {
            Class objectType = value.getClass();
            return classifyObject(objectType);
         }
         return NULL;
      }

      public ObjectType classifyObject(Class objectType) {
         if (isArray(objectType)) {
            return ARRAY;
         }
         if (isCollection(objectType)) {
            return COLLECTION;
         }
         if (isMap(objectType)) {
            return MAP;
         }
         if (isPrimitive(objectType)) {
            return PRIMITIVE;
         }
         return OBJECT;
      }

      private boolean isArray(Class objectType) {
         return objectType.isArray();
      }

      private boolean isCollection(Class objectType) {
         return Collection.class.isAssignableFrom(objectType);
      }

      private boolean isMap(Class objectType) {
         return Map.class.isAssignableFrom(objectType);
      }

      private boolean isPrimitive(Class objectType) {
         if (objectType == String.class) {
            return true;
         }
         if (objectType == Double.class) {
            return true;
         }
         if (objectType == Integer.class) {
            return true;
         }
         if (objectType == Float.class) {
            return true;
         }
         if (objectType == Byte.class) {
            return true;
         }
         if (objectType == Long.class) {
            return true;
         }
         if (objectType == Short.class) {
            return true;
         }
         if (objectType == Character.class) {
            return true;
         }
         if (objectType == Boolean.class) {
            return true;
         }
         return objectType.isPrimitive() || objectType.isEnum();
      }
   }
}
