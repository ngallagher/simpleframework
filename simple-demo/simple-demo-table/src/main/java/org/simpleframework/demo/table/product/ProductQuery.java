package org.simpleframework.demo.table.product;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProductQuery {

   private final Map<String, Product> cache;
   private final ProductFilter builder;
   private final String company;
   private final String user;
   
   public ProductQuery(String user, String company, List<String> include) {
      this.cache = new ConcurrentHashMap<String, Product>();
      this.builder = new ProductFilter(company, include);
      this.company = company;    
      this.user = user;
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
   
   public String getUser() {
      return user;
   }
   
   public String getCompany() {
      return company;
   }
}
