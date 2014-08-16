package org.simpleframework.demo.table.extract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class RowMerger {

   private final Map<String, Object> cache;
   private final TableSchema schema;
   private final AtomicLong revision;
   private final int index;
   
   public RowMerger(TableSchema schema, int index) {
      this.cache = new HashMap<String, Object>();
      this.revision = new AtomicLong(-1);
      this.schema = schema;
      this.index = index;
   }
   
   public synchronized RowChange merge(Map<String, Object> row, long version) {
      List<Column> columns = schema.getColumns();
      int require = columns.size();
      int actual = row.size();
      
      if(require != actual) {
         throw new IllegalArgumentException("Row does not match schema " + columns + " require " + require + " but got " + actual + " " + row);
      }
      long state = revision.getAndSet(version);
      
      if(state > version) {
         throw new IllegalStateException("Merging version " + version + " but already higher at " + state);
      }
      if(state == version) {
         throw new IllegalStateException("Merging version " + version + " but already at " + state);         
      }
      Map<String, Object> difference = new HashMap<String, Object>();
      
      for(Column column : columns) {
         String name = column.getName();
         Object current = row.get(name);
         Object previous = cache.get(name);
         
         if (current != null) {
            if (!current.equals(previous)) {            
               difference.put(name, current);
               cache.put(name, current);               
            }
         } else {
            if(previous != null) {
               difference.put(name, null);
               cache.put(name, null);               
            }
         }
      }
      if(!difference.isEmpty()) {
         return new RowChange(difference, index, version);
      }
      return null;
   }
}
