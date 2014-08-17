package org.simpleframework.demo.table.extract;

import org.simpleframework.demo.table.reflect.PropertyPathAccessor;

public class PropertyCellExtractor implements CellExtractor {

   private final PropertyPathAccessor accessor;
   
   public PropertyCellExtractor(String path, Class type) throws Exception {
      this.accessor = new PropertyPathAccessor(path, type);
   }

   @Override
   public Object extract(Object value) {
      return accessor.getValue(value);
   }
}
