package org.simpleframework.demo.table.extract;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableCursor {

   private final Map<Integer, RowMerger> mergers;
   private final Map<Integer, Long> versions;
   private final RowExtractor extractor;
   private final TableSchema schema;
   private final TableModel model;
   
   public TableCursor(TableModel model, TableSchema schema, RowExtractor extractor) {
      this.mergers = new ConcurrentHashMap<Integer, RowMerger>();
      this.versions = new ConcurrentHashMap<Integer, Long>();
      this.extractor = extractor;
      this.schema = schema;
      this.model = model;
   }
   
   public List<RowChange> update() {
      List<Row> rows = model.build();
      
      if(!rows.isEmpty()) {
         List<RowChange> changes = new LinkedList<RowChange>();
      
         for(Row row : rows) {
            Integer index = row.getIndex();
            Long previous = versions.get(index);
            long current = row.getVersion();            
            
            if(previous == null || previous < current) {
               RowMerger merger = merge(row);
               Object value = row.getValue();
               Map<String, Object> attributes = extractor.extract(value);
               RowChange change = merger.merge(attributes, current);
               
               if(change != null) {
                  changes.add(change);               
               }
               versions.put(index, current);
            }
         }      
         return changes;
      }
      return Collections.emptyList();      
   }
   
   private RowMerger merge(Row row) {
      Integer index = row.getIndex();
      RowMerger merger = mergers.get(index);
      
      if(merger == null) {
         merger = new RowMerger(schema);
         mergers.put(index, merger);
      }
      return merger;
   }
   
   public void clear() {
      mergers.clear();
   }
  
}
