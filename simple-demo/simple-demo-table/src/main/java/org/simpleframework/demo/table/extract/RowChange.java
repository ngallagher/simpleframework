package org.simpleframework.demo.table.extract;

import java.util.Map;

public class RowChange {

   private final Map<String, Object> changes;
   private final long version;
   
   public RowChange(Map<String, Object> changes, long version) {
      this.changes = changes;
      this.version = version;
   }
   
   public Map<String, Object> getChanges() {
      return changes;
   }
   
   public long getVersion() {
      return version;
   }
}
