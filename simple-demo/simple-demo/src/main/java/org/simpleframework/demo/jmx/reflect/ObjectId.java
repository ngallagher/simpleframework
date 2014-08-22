package org.simpleframework.demo.jmx.reflect;

public class ObjectId {

   private String objectName;
   private String className;
   private String keyId;
   private String entryId;

   public String getObjectName() {
      return objectName;
   }

   public void setObjectName(String objectName) {
      this.objectName = objectName;
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getKeyId() {
      return keyId;
   }

   public void setKeyId(String keyId) {
      this.keyId = keyId;
   }

   public String getEntryId() {
      return entryId;
   }

   public void setEntryId(String entryId) {
      this.entryId = entryId;
   }

   public boolean isBlankId() {
      if (objectName != null) {
         return false;
      }
      if (entryId != null) {
         return false;
      }
      return keyId == null;
   }
}
