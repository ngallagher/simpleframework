package org.simpleframework.demo.table.reflect;

public class PropertyPathAccessor implements Accessor {
   
   private final PropertyAccessor[] accessors;

   public PropertyPathAccessor(String path, Class type) {
      this.accessors = getPath(path, type);
   }
   
   @Override
   public Class getType() {
      return accessors[accessors.length - 1].getClass();
   }

   @Override
   public <T> T getValue(Object source) {
      Object value = source;
      
      for(int i = 0; i < accessors.length; i++) {
         if(value != null) {
            value = accessors[i].getValue(value);
         }
      }
      return (T)value;
   }
   
   private  PropertyAccessor[] getPath(String path, Class type) {
      String[] parts = path.split("\\.");
      Class current = type;
      
      if(parts.length > 0) {
         PropertyAccessor[] accessors = new PropertyAccessor[parts.length];
         
         for(int i = 0; i < parts.length; i++) {
            PropertyAccessor accessor = new PropertyAccessor(parts[i], current);
            
            current = accessor.getType();  
            accessors[i] = accessor;       
         }      
         return accessors;
      }
      return new PropertyAccessor[0];
   }
}
