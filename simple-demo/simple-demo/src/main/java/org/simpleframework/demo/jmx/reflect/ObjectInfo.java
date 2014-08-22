package org.simpleframework.demo.jmx.reflect;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is used to represent an object instance within the virtual 
 * machine. Convenience methods are provided to extract the fields 
 * as well as other information used to identify the object.
 * 
 * @author Niall Gallagher
 */
public interface ObjectInfo {
   List<String> getClassHierarchy();
   ObjectPath getObjectPath();
   ObjectInfo getObjectInfo(ObjectId objectId);
   Map<String, ObjectFieldInfo> getFields(String type);
   Object getObjectValue();
   ObjectInfo[] getObjectArray();
   Collection<ObjectInfo> getObjectCollection();
   Map<ObjectInfo, ObjectInfo> getObjectMap();
   ObjectType getObjectType();
   String getClassName();
   String getUniqueId();
}
