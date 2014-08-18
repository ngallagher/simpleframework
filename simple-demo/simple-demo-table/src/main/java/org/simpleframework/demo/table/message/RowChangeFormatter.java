package org.simpleframework.demo.table.message;

import java.util.List;
import java.util.Map;

import org.simpleframework.demo.table.extract.RowChange;
import org.simpleframework.demo.table.extract.ValueEncoder;
import org.simpleframework.demo.table.schema.Column;
import org.simpleframework.demo.table.schema.TableSchema;

public class RowChangeFormatter {

   private final CellChangeFormatter formatter;
   private final ValueEncoder encoder;
   private final TableSchema schema;
   
   public RowChangeFormatter(TableSchema schema) {
      this.formatter = new CellChangeFormatter();
      this.encoder = new ValueEncoder();
      this.schema = schema;
   }
   
   public String formatChange(RowChange change) {
      Map<String, String> changes = change.getChanges();
      List<Column> columns = schema.getColumns();
      Integer index = change.getIndex();
      int width = columns.size();
      int count = 0;
      
      if(!changes.isEmpty()) {
         StringBuilder builder = new StringBuilder();
         
         builder.append(index);
         builder.append(":");
         
         for(int i = 0; i < width; i++) {
            Column column = columns.get(i);
            String name = column.getName();
            
            if(changes.containsKey(name)) {
               Object value = changes.get(name);
               String text = formatter.formatCell(value);
               String encoded = encoder.encode(text);
               
               if(count++ > 0) {
                  builder.append(",");
               }
               builder.append(i);
               builder.append("=");
               builder.append(encoded);
            }
         }
         return builder.toString();
      }
      return null;
   }
}
