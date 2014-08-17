package org.simpleframework.demo.table.product;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.demo.table.Query;

public class ProductQuery {

   private final Map<String, Product> cache;
   private final ProductFilter builder;
   
   public ProductQuery(Query client) {
      this.cache = new ConcurrentHashMap<String, Product>();
      this.builder = new ProductFilter(client);
   }   
   
   public Product query(Depth depth) {
      String security = depth.getSecurity();
      Product product = cache.get(security);
      
      if(product != null) {
         long version = product.getVersion();      
         long update = depth.getVersion();
         
         if(version == update) {
            return product;
         }
      }      
      Product current = builder.filterDepth(depth);
         
      if(current != null) {
         cache.put(security, current);
      } else {
         cache.remove(security);
      }
      return current;   
   }   
}
