package org.simpleframework.demo.table.extract;

import java.util.Collections;
import java.util.Set;

public class TableSchema {

   private final Set<String> columns;
   
   public TableSchema(Set<String> columns) {
      this.columns = columns;
   }
   
   public Set<String> getColumns() {
      return Collections.unmodifiableSet(columns);
   }
}
