package org.simpleframework.demo.table.extract;

import java.util.List;

public interface TableSubscription {
  List<Row> next();   
}
