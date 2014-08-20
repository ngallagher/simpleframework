package org.simpleframework.demo.table.service;

import java.util.List;

import org.simpleframework.demo.table.schema.Column;
import org.simpleframework.demo.table.schema.ColumnStyle;
import org.simpleframework.demo.table.schema.TableSchema;

public class SchemaFormatter {
   
   private final ColumnStyleFormatter formatter;
   private final TableSchema schema;
   
   public SchemaFormatter(TableSchema schema) {
      this.formatter = new ColumnStyleFormatter();
      this.schema = schema;
   }

   public String formatSchema() {
      List<Column> columns = schema.getColumns();
      String table = schema.getTable();
      
      if(!columns.isEmpty()) {
         StringBuilder builder = new StringBuilder();
         
         builder.append(ChangeType.SCHEMA.code);
         builder.append(table);
         
         for(Column column : columns) {
            ColumnStyle style = column.getStyle();
            String text = formatter.formatColumnStyle(style);
            int length = builder.length();
            
            if(length > 0) {
               builder.append("|");
            }
            builder.append(text);
         }
         return builder.toString();         
      }
      return null;
   }
      
}
