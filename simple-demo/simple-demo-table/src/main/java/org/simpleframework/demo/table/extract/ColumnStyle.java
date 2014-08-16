package org.simpleframework.demo.table.extract;

public interface ColumnStyle {
   String getName();
   String getTemplate();
   String getStyle();
   String getTitle();
   boolean isResizable();
   boolean isSortable();
   boolean isHidden();
}
