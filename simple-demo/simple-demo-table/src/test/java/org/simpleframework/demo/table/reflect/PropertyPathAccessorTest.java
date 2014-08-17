package org.simpleframework.demo.table.reflect;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

public class PropertyPathAccessorTest extends TestCase {
   
   private static enum PriceType{
      YIELD,
      SPREAD;
   }
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
      public String getBest(String product) {
         return "best-" + product;
      }
      public Integer[] getPrices() {
         return new Integer[]{10,20,30,40,50};
      }
      public Map<PriceType, Double> getDepth(){
         return Collections.singletonMap(PriceType.SPREAD, 10.99);
      }
      public ProductStatic[] getReferences() {
         return new ProductStatic[]{ 
               new ProductStatic("A"),
               new ProductStatic("B")
         };
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
   
   public void testComplextPath() throws Exception {
      Product product = new Product("XXY");
      PropertyPathAccessor accessor = new PropertyPathAccessor("data.references[1].depth[YIELD]", Product.class);
      
      assertEquals(product.getData().getReferences()[1].getDepth().get(PriceType.YIELD), accessor.getValue(product));
      
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < 1000000; i++) {
         accessor.getValue(product);
      }
      long end = System.currentTimeMillis();
      long duration = end - start;
      
      System.err.println("duration for 1000000 was " + duration);
   }   
   
   public void testPathArrayIndexAccessor() throws Exception {
      Product product = new Product("XXY");
      PropertyPathAccessor accessor = new PropertyPathAccessor("data.prices[2]", Product.class);
      
      assertEquals(product.getData().getPrices()[2], accessor.getValue(product));
      
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < 1000000; i++) {
         accessor.getValue(product);
      }
      long end = System.currentTimeMillis();
      long duration = end - start;
      
      System.err.println("duration for 1000000 was " + duration);
   }
   
   public void testPathIndexAccessor() throws Exception {
      Product product = new Product("XXY");
      PropertyPathAccessor accessor = new PropertyPathAccessor("data.best[X]", Product.class);
      
      assertEquals(product.getData().getBest("X"), accessor.getValue(product));
      
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < 1000000; i++) {
         accessor.getValue(product);
      }
      long end = System.currentTimeMillis();
      long duration = end - start;
      
      System.err.println("duration for 1000000 was " + duration);
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
