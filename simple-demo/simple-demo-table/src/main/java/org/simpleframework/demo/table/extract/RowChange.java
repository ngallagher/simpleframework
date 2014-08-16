package org.simpleframework.demo.table.extract;

import java.util.Map;

public class RowChange {

   private final Map<String, Object> changes;
   private final long version;
   private final int index;
   
   public RowChange(Map<String, Object> changes, int index, long version) {
      this.changes = changes;
      this.version = version;
      this.index = index;
   }
   
   public Map<String, Object> getChanges() {
      return changes;
   }
   
   public long getVersion() {
      return version;
   }
   
   public int getIndex() {
      return index;
   }
}
