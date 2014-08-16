package org.simpleframework.demo.table.product;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.demo.table.extract.Client;

public class ProductBuilder {

   private final Map<String, Product> cache;
   private final ProductFilter builder;
   
   public ProductBuilder(Client client) {
      this.cache = new ConcurrentHashMap<String, Product>();
      this.builder = new ProductFilter(client);
   }   
   
   public Product getProduct(Depth depth) {
      String security = depth.getSecurity();
      Product previous = cache.get(security);
      
      if(previous != null) {
         long previousVersion = previous.getVersion();      
         long currentVersion = depth.getVersion();
         
         if(previousVersion == currentVersion) {
            return previous;
         }
      }      
      Product current = builder.filter(depth);
         
      if(current != null) {
         cache.put(security, current);
      } else {
         cache.remove(security);
      }
      return current;   
   }   
}
