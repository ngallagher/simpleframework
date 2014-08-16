package org.simpleframework.demo.table.extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TableSchema {

   private final List<ColumnStyle> styles;
   private final List<Column> columns;
   private final String table;
   
   public TableSchema(String table, List<ColumnStyle> styles) {
      this.columns = new ArrayList<Column>();
      this.table = table;
      this.styles = styles;
   }
   
   public String getTable() {
      return table;
   }
   
   public List<Column> getColumns() {
      if(columns.isEmpty()) {         
         Set<String> done = new HashSet<String>();
         int index = 0;
         
         for(ColumnStyle style : styles) {
            String name = style.getName();
            
            if(done.add(name)) {
               Column column = new Column(style, name, index);            

               columns.add(column);
               index++;
            }
         }
      }
      return Collections.unmodifiableList(columns);
   }
}
