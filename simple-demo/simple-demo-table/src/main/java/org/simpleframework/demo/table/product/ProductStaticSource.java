package org.simpleframework.demo.table.product;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ProductStaticSource {
   
   private final List<ProductStatic> products;
   private final List<String> names;

   public ProductStaticSource() {
      this.products = new ArrayList<ProductStatic>();
      this.names = new ArrayList<String>();
   }
   
   public List<String> getNames() {
      List<ProductStatic> products = getProducts();
      
      if(!products.isEmpty()) {
         return Collections.unmodifiableList(names);
      }
      return Collections.emptyList();
   }
   
   public List<ProductStatic> getProducts() {
      if(products.isEmpty()) {
         Random random = new SecureRandom();
         
         for(int i = 0; i < 1000; i++) {
            int value = random.nextInt(200);
            String name = null;
            
            if(value % 1 == 0) {
               name = "CGS" + i;
            }
            if(value % 2 == 0) {
               name = "NSW" + i;
            }
            if(value % 3 == 0) {
               name = "QTC" + i;
            }
            if(value % 4 == 0) {
               name = "WAGA" + i;
            }          
            ProductStatic data = new ProductStatic(name);
            
            products.add(data);
            names.add(name);
         }
      }
      return Collections.unmodifiableList(products);
   }
}
