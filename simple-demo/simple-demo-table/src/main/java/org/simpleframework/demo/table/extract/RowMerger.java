package org.simpleframework.demo.table.extract;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class RowMerger {

   private final Map<String, Object> cache;
   private final TableSchema schema;
   private final AtomicLong revision;
   
   public RowMerger(TableSchema schema) {
      this.cache = new HashMap<String, Object>();
      this.revision = new AtomicLong(-1);
      this.schema = schema;
   }
   
   public synchronized RowChange merge(Map<String, Object> row, long version) {
      Set<String> columns = schema.getColumns();
      int expect = columns.size();
      int actual = row.size();
      
      if(expect != actual) {
         throw new IllegalArgumentException("Row does not match schema " + columns);
      }
      long state = revision.getAndSet(version);
      
      if(state > version) {
         throw new IllegalStateException("Merging version " + version + " but already higher at " + state);
      }
      if(state == version) {
         throw new IllegalStateException("Merging version " + version + " but already at " + state);         
      }
      Map<String, Object> difference = new HashMap<String, Object>();
      
      for(String column : columns) {
         Object current = row.get(column);
         Object previous = cache.get(column);
         
         if (current != null) {
            if (!current.equals(previous)) {            
               difference.put(column, current);
               cache.put(column, current);               
            }
         } else {
            if(previous != null) {
               difference.put(column, null);
               cache.put(column, null);               
            }
         }
      }
      if(!difference.isEmpty()) {
         return new RowChange(difference, version);
      }
      return null;
   }
}
