package org.simpleframework.demo.table.extract;

import java.util.Map;

public class RowChange {

   private final Map<String, String> changes;
   private final long version;
   private final int index;
   
   public RowChange(Map<String, String> changes, int index, long version) {
      this.changes = changes;
      this.version = version;
      this.index = index;
   }
   
   public Map<String, String> getChanges() {
      return changes;
   }
   
   public long getVersion() {
      return version;
   }
   
   public int getIndex() {
      return index;
   }
}
