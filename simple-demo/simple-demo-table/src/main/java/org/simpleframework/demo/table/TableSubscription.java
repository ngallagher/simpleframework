package org.simpleframework.demo.table;

import java.util.List;

public interface TableSubscription {
  List<Row> next();   
}
