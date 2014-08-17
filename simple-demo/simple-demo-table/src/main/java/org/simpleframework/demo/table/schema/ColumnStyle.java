package org.simpleframework.demo.table.schema;

public interface ColumnStyle {
   String getName();
   String getTemplate();
   String getStyle();
   String getTitle();
   boolean isResizable();
   boolean isSortable();
   boolean isHidden();
}
