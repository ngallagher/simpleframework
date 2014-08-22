package org.simpleframework.demo.jmx.reflect;

import java.util.List;

public interface ObjectPath {
   String getObjectName();
   List<ObjectId> getObjectPath();
   ObjectPath getRelativePath(ObjectId objectId);
}
