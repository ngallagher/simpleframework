package org.simpleframework.demo.jmx.reflect;

public interface ObjectFieldInfo {
   ObjectInfo getObjectInfo();
   Object getFieldValue();
   Class getFieldClass();
   String getFieldName();
}
