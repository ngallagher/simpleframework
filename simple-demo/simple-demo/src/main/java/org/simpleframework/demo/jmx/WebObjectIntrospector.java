package org.simpleframework.demo.jmx;

import static org.simpleframework.demo.jmx.reflect.ObjectType.ARRAY;
import static org.simpleframework.demo.jmx.reflect.ObjectType.COLLECTION;
import static org.simpleframework.demo.jmx.reflect.ObjectType.MAP;
import static org.simpleframework.demo.jmx.reflect.ObjectType.NULL;
import static org.simpleframework.demo.jmx.reflect.ObjectType.OBJECT;
import static org.simpleframework.demo.jmx.reflect.ObjectType.PRIMITIVE;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import org.simpleframework.demo.jmx.reflect.ObjectFieldInfo;
import org.simpleframework.demo.jmx.reflect.ObjectId;
import org.simpleframework.demo.jmx.reflect.ObjectInfo;
import org.simpleframework.demo.jmx.reflect.ObjectIntrospector;
import org.simpleframework.demo.jmx.reflect.ObjectPath;
import org.simpleframework.demo.jmx.reflect.ObjectPathConverter;
import org.simpleframework.demo.jmx.reflect.ObjectType;

@ManagedResource(description="Web based introspection tool")
public class WebObjectIntrospector implements BeanNameAware {

   private ObjectPathConverter objectPathConverter;
   private ObjectIntrospector objectIntrospector;
   private String beanName;

   public WebObjectIntrospector(ObjectIntrospector objectIntrospector) {
      this.objectPathConverter = new ObjectPathConverter();
      this.objectIntrospector = objectIntrospector;
   }

   public void setBeanName(String beanName) {
      this.beanName = beanName;
   }

   @ManagedOperation(description="Get object information as HTML")
   @ManagedOperationParameters({ 
      @ManagedOperationParameter(name="objectPath", description="Path to the object") 
   })
   public String getObjectInfo(String objectPath) throws UnsupportedEncodingException {
      StringBuilder builder = new StringBuilder();

      if (objectPath != null) {
         ObjectPath objectPathData = objectPathConverter.convertPath(objectPath);
         ObjectInfo objectInfo = objectIntrospector.getObjectInfo(objectPathData);
         ObjectType objectType = objectInfo.getObjectType();

         buildObjectPath(objectInfo, builder);

         if (objectType == OBJECT) {
            buildObjectDetails(objectInfo, builder);
         }
         if (objectType == MAP) {
            buildMapDetails(objectInfo, builder);
         }
         if (objectType == COLLECTION) {
            buildCollectionDetails(objectInfo, builder);
         }
         if (objectType == ARRAY) {
            buildArrayDetails(objectInfo, builder);
         }
         if (objectType == PRIMITIVE) {
            buildObjectValue(objectInfo, builder);
         }
      }
      return builder.toString();
   }

   @ManagedOperation(description="Get link for for a named object")
   @ManagedOperationParameters({ 
      @ManagedOperationParameter(name="objectName", description="Name of the object") 
   })
   public String getObjectLink(String objectName) throws UnsupportedEncodingException {
      StringBuilder builder = new StringBuilder();

      if (objectName != null) {
         ObjectPath objectPathData = objectPathConverter.convertPath(objectName);
         ObjectInfo objectInfo = objectIntrospector.getObjectInfo(objectPathData);

         if (objectInfo == null) {
            return null;
         }
         buildObjectLink(objectInfo, builder);
      }
      return builder.toString();
   }

   private void buildObjectDetails(ObjectInfo objectInfo, StringBuilder builder) throws UnsupportedEncodingException {
      List<String> classHierarchy = objectInfo.getClassHierarchy();

      for (String type : classHierarchy) {
         Map<String, ObjectFieldInfo> fieldInfo = objectInfo.getFields(type);

         builder.append("<b>");
         builder.append(type);
         builder.append("</b><br><br>");

         for (ObjectFieldInfo field : fieldInfo.values()) {
            ObjectInfo fieldObjectInfo = field.getObjectInfo();

            builder.append("<b>");
            builder.append(field.getFieldName());
            builder.append(": </b>");
            buildObjectValue(fieldObjectInfo, builder);
            builder.append("<br>");
         }
         builder.append("<hr>");
      }
   }

   private void buildMapDetails(ObjectInfo objectInfo, StringBuilder builder) throws UnsupportedEncodingException {
      Map<ObjectInfo, ObjectInfo> objectMap = objectInfo.getObjectMap();
      Set<ObjectInfo> keySet = objectMap.keySet();
      String className = objectInfo.getClassName();

      builder.append("<b>");
      builder.append(className);
      builder.append("</b><br><br><table border='1'>");
      builder.append("<th>key</th><th>value</th>");

      for (ObjectInfo keyObjectInfo : keySet) {
         ObjectInfo valueObjectInfo = objectMap.get(keyObjectInfo);

         builder.append("<tr><td>");
         buildObjectValue(keyObjectInfo, builder);
         builder.append("</td><td>");
         buildObjectValue(valueObjectInfo, builder);
         builder.append("</td></tr>");
      }
      builder.append("</table>");
   }

   private void buildCollectionDetails(ObjectInfo objectInfo, StringBuilder builder) throws UnsupportedEncodingException {
      Collection<ObjectInfo> objectCollection = objectInfo.getObjectCollection();
      String className = objectInfo.getClassName();

      builder.append("<b>");
      builder.append(className);
      builder.append("</b><br><br><table border='1'>");
      builder.append("<th>entry</th>");

      for (ObjectInfo keyObjectInfo : objectCollection) {
         builder.append("<tr><td>");
         buildObjectValue(keyObjectInfo, builder);
         ;
         builder.append("</td></tr>");
      }
      builder.append("</table>");
   }

   private void buildArrayDetails(ObjectInfo objectInfo, StringBuilder builder) throws UnsupportedEncodingException {
      ObjectInfo[] objectArray = objectInfo.getObjectArray();

      builder.append("<table border='1'>");
      builder.append("<th>index</th>");
      builder.append("<th>entry</th>");

      for (int i = 0; i < objectArray.length; i++) {
         builder.append("<tr><td>");
         builder.append(i);
         builder.append("</td><td>");
         buildObjectValue(objectArray[i], builder);
         ;
         builder.append("</td></tr>");
      }
      builder.append("</table>");
   }

   private void buildObjectValue(ObjectInfo objectInfo, StringBuilder builder) throws UnsupportedEncodingException {
      ObjectType objectType = objectInfo.getObjectType();
      Object objectValue = objectInfo.getObjectValue();

      if (objectType == PRIMITIVE) {
         builder.append(objectValue);
      } else if (objectType == NULL) {
         builder.append(objectValue);
      } else {
         builder.append("<a href='");
         buildObjectLink(objectInfo, builder);
         builder.append("'>");
         buildObjectName(objectInfo, builder);
         builder.append("</a>");
      }
   }

   private void buildObjectLink(ObjectInfo objectInfo, StringBuilder builder) throws UnsupportedEncodingException {
      ObjectPath fieldObjectPath = objectInfo.getObjectPath();
      String fieldPath = objectPathConverter.convertPath(fieldObjectPath);
      String encodedPath = URLEncoder.encode(fieldPath, "UTF-8");

      buildObjectLink(encodedPath, builder);
   }

   private void buildObjectPath(ObjectInfo objectInfo, StringBuilder builder) {
      ObjectPath objectPath = objectInfo.getObjectPath();
      String objectName = objectPath.getObjectName();
      List<ObjectId> objectIds = objectPath.getObjectPath();

      buildObjectReference(objectName, builder);

      for (int index = 0; index < objectIds.size(); index++) {
         ObjectId objectId = objectIds.get(index);
         String fieldName = objectId.getObjectName();
         String className = objectId.getClassName();

         builder.append("<span style='padding-left: ");
         builder.append(index * 25);
         builder.append("'/>+ ");

         if (fieldName != null) {
            builder.append("<b>");
            builder.append(fieldName);
            builder.append("</b> - ");
         }
         builder.append(className);
         builder.append("<br>");
      }
      builder.append("<hr>");
   }

   private void buildObjectReference(String fieldPath, StringBuilder builder) {
      builder.append("<a href='");
      buildObjectLink(fieldPath, builder);
      builder.append("'>");
      builder.append(fieldPath);
      builder.append("</a><br>");
   }

   private void buildObjectLink(String fieldPath, StringBuilder builder) {
      Class beanClass = getClass();
      String beanType = beanClass.getSimpleName();
      Package beanPackage = beanClass.getPackage();
      String packageName = beanPackage.getName();

      builder.append("/InvokeAction//");
      builder.append(packageName);
      builder.append("%3Aname%3D");
      builder.append(beanName);
      builder.append("%2Ctype%3D");
      builder.append(beanType);
      builder.append("/action=getObjectInfo?action=getObjectInfo&objectPath%2Bjava.lang.String=");
      builder.append(fieldPath);
   }

   private void buildObjectName(ObjectInfo objectInfo, StringBuilder builder) {
      String className = objectInfo.getClassName();
      String uniqueId = objectInfo.getUniqueId();

      builder.append(className);
      builder.append("@");
      builder.append(uniqueId);
   }
}
