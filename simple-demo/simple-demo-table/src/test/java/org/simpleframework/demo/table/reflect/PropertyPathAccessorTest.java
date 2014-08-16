package org.simpleframework.demo.table.reflect;

import java.util.Date;

import junit.framework.TestCase;

public class PropertyPathAccessorTest extends TestCase {
   
   
   public static class ProductStatic {
      private  final Date maturity;
      private final String name;
      public ProductStatic(String name){
         this.maturity = new Date();
         this.name = name;
      }
      public String getName(){
         return name;
      }
      public Date getDate(){
         return maturity;
      }
   }
   
   public static class Product {
      private final ProductStatic data;
      public Product(String name) {
         this.data = new ProductStatic(name);
      }
      public ProductStatic getData(){
         return data;
      }
   }
   
   public void testPathAccessor() throws Exception {
      Product product = new Product("XXY");
      PropertyPathAccessor accessor = new PropertyPathAccessor("data.date.time", Product.class);
      
      assertEquals(product.getData().getDate().getTime(), accessor.getValue(product));
      
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < 1000000; i++) {
         accessor.getValue(product);
      }
      long end = System.currentTimeMillis();
      long duration = end - start;
      
      System.err.println("duration for 1000000 was " + duration);
   }

}
