package org.simpleframework.demo.table;

public class Row {
   
   private final Object value;
   private final int index;
   private final long version;

   public Row(Object value, int index, long version) {
      this.version = version;
      this.index = index;
      this.value = value;
   }
   
   public Object getValue() {
      return value;   
   }
   
   public long getVersion() {
      return version;
   }
   
   public int getIndex() {
      return index;
   }
   
}
