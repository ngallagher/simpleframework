package org.simpleframework.demo.table.product;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

public class ProductTable implements DepthListener {
   
   private final Map<String, Depth> products;
   private final Set<String> securities;
   private final AtomicLong version;
   
   public ProductTable() {
      this.products = new ConcurrentHashMap<String, Depth>();
      this.securities = new CopyOnWriteArraySet<String>();
      this.version = new AtomicLong();
   }
   
   public List<Product> extract(ProductBuilder subscription) {
      List<Product> list = new LinkedList<Product>();
      
      for(String security : securities) {
         Depth depth = products.get(security);        
         Product product = subscription.getProduct(depth);
             
         list.add(product);
      }
      return list;
   }

   @Override
   public void update(Depth depth) {
      String security = depth.getSecurity();
      
      products.put(security, depth);
      securities.add(security);     
      version.getAndIncrement();
   }
}
