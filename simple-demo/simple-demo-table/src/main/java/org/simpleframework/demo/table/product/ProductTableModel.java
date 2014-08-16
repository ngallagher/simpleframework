package org.simpleframework.demo.table.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.demo.table.extract.Row;
import org.simpleframework.demo.table.extract.TableModel;

public class ProductTableModel implements TableModel {
   
   private final Map<String, Integer> indexes;
   private final ProductSubscription cursor;
   private final ProductTable table;
   
   public ProductTableModel(ProductTable table, ProductSubscription cursor) {
      this.indexes = new ConcurrentHashMap<String, Integer>();
      this.table = table;
      this.cursor = cursor;
   }
   
   public List<Row> build() {
      List<Product> products = table.extract(cursor);
      
      if(!products.isEmpty()) {
         List<Row> rows = new ArrayList<Row>();
         
         for(Product product : products) {
            String security = product.getSecurity();
            Integer index = indexes.get(security);
            long version = product.getVersion();
            int size = indexes.size();
            
            if(index == null) {
               indexes.put(security, size);
               index = size;
            }
            Row row = new Row(product, index, version);
            
            if(product != null) {
               rows.add(row);
            }           
         }
         return rows;
      }
      return Collections.emptyList();
   }

}
