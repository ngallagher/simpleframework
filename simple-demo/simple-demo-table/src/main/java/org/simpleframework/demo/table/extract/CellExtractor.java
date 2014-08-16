package org.simpleframework.demo.table.extract;

public interface CellExtractor<T> {
   Object extract(T value);
}
