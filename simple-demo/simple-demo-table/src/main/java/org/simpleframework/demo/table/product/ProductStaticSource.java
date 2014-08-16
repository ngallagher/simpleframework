package org.simpleframework.demo.table.product;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProductStaticSource {
   
   private final List<ProductStatic> products;

   public ProductStaticSource() {
      this.products = new ArrayList<ProductStatic>();
   }
   
   public List<ProductStatic> getProducts() {
      if(products.isEmpty()) {
         Random random = new SecureRandom();
         
         for(int i = 0; i < 100; i++) {
            int value = random.nextInt(200);
            
            if(value % 1 == 0) {
               products.add(new ProductStatic("CGS" + i));
            }
            if(value % 2 == 0) {
               products.add(new ProductStatic("NSW" + i));
            }
            if(value % 3 == 0) {
               products.add(new ProductStatic("QTC" + i));
            }
            if(value % 4 == 0) {
               products.add(new ProductStatic("WAGA" + i));
            }          
         }
      }
      return products;
   }
}
