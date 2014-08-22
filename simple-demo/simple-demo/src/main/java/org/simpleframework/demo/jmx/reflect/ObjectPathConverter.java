package org.simpleframework.demo.jmx.reflect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectPathConverter {

   public static final String NAME = "name";
   public static final String TYPE = "type";
   public static final String KEY = "key";
   public static final String ENTRY = "entry";

   public ObjectPath convertPath(String objectPath) {
      List<String> pathSegments = new ArrayList<String>();
      String[] objectSegments = objectPath.split("/");
      String objectName = objectSegments[0];

      if (objectSegments.length > 1) {
         for (int i = 1; i < objectSegments.length; i++) {
            pathSegments.add(objectSegments[i]);
         }
      }
      return new ObjectPathData(objectName, pathSegments);
   }

   public String convertPath(ObjectPath objectPath) {
      StringBuilder builder = new StringBuilder();
      String objectName = objectPath.getObjectName();
      List<ObjectId> objectSegments = objectPath.getObjectPath();

      builder.append(objectName);
      builder.append("/");

      for (ObjectId objectId : objectSegments) {
         Map<String, String> attributes = new LinkedHashMap<String, String>();
         String separatorChar = "";

         attributes.put(NAME, objectId.getObjectName());
         attributes.put(TYPE, objectId.getClassName());
         attributes.put(KEY, objectId.getKeyId());
         attributes.put(ENTRY, objectId.getEntryId());

         for (String name : attributes.keySet()) {
            String value = attributes.get(name);

            if (value != null) {
               builder.append(separatorChar);
               builder.append(name);
               builder.append("=");
               builder.append(value);
               separatorChar = ",";
            }
         }
         builder.append("/");
      }
      return builder.toString();
   }

   private static class ObjectPathData implements ObjectPath {

      private final List<ObjectId> objectIdList;
      private final List<String> objectSegments;
      private final String objectName;

      public ObjectPathData(String objectName, List<String> objectSegments) {
         this.objectIdList = new ArrayList<ObjectId>();
         this.objectSegments = objectSegments;
         this.objectName = objectName;
      }

      public String getObjectName() {
         return objectName;
      }

      public List<ObjectId> getObjectPath() {
         if (objectIdList.isEmpty()) {
            for (String pathSegment : objectSegments) {
               ObjectId objectId = new ObjectId();
               String[] attributes = pathSegment.split(",");

               for (String attribute : attributes) {
                  String[] pair = attribute.split("=");

                  if (pair[0].equals(NAME)) {
                     objectId.setObjectName(pair[1]);
                  } else if (pair[0].equals(TYPE)) {
                     objectId.setClassName(pair[1]);
                  } else if (pair[0].equals(KEY)) {
                     objectId.setKeyId(pair[1]);
                  } else if (pair[0].equals(ENTRY)) {
                     objectId.setEntryId(pair[1]);
                  }
               }
               if (!objectId.isBlankId()) {
                  objectIdList.add(objectId);
               }
            }
         }
         return objectIdList;
      }

      public ObjectPath getRelativePath(ObjectId objectId) {
         List<String> objectSegments = new ArrayList<String>();
         ObjectPathData objectPath = new ObjectPathData(objectName, objectSegments);
         List<ObjectId> relativePath = objectPath.getObjectPath();

         for (ObjectId currentId : objectIdList) {
            relativePath.add(currentId);
         }
         relativePath.add(objectId);
         return objectPath;
      }

   }
}
