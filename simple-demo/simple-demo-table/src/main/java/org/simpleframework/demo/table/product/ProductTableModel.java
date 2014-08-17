package org.simpleframework.demo.table.product;

import org.simpleframework.demo.table.Query;
import org.simpleframework.demo.table.TableModel;
import org.simpleframework.demo.table.TableSubscription;

public class ProductTableModel implements TableModel {
   
   private final ProductTable table;
   
   public ProductTableModel(ProductTable table) {
      this.table = table;
   }
   
   public TableSubscription subscribe(Query client) {
      return new ProductTableSubscription(table, client);
   }
}
