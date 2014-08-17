package org.simpleframework.demo.table.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.demo.table.Query;
import org.simpleframework.demo.table.Row;
import org.simpleframework.demo.table.TableSubscription;

public class ProductTableSubscription implements TableSubscription {
   
   private final Map<String, Integer> indexes;
   private final ProductQuery builder;
   private final ProductTable table;
   
   public ProductTableSubscription(ProductTable table, Query client) {
      this.indexes = new ConcurrentHashMap<String, Integer>();
      this.builder = new ProductQuery(client);
      this.table = table;
   }
   
   public List<Row> next() {
      List<Product> products = table.query(builder);
      
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
