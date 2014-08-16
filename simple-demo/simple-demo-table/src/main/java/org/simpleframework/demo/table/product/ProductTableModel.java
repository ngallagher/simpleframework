package org.simpleframework.demo.table.product;

import org.simpleframework.demo.table.extract.Client;
import org.simpleframework.demo.table.extract.TableModel;
import org.simpleframework.demo.table.extract.TableSubscription;

public class ProductTableModel implements TableModel {
   
   private final ProductTable table;
   
   public ProductTableModel(ProductTable table) {
      this.table = table;
   }
   
   public TableSubscription subscribe(Client client) {
      return new ProductTableSubscription(table, client);
   }
}
