package org.simpleframework.demo.table.product;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public class DepthGenerator extends Thread {
   
   private final ProductStaticSource source;
   private final DepthProcessor processor;
   private final List<String> companies;
   private final long frequency;
   
   public DepthGenerator(DepthProcessor processor, ProductStaticSource source, List<String> companies, long frequency) throws Exception {
      this.frequency = frequency;
      this.companies = companies;
      this.processor = processor;
      this.source = source;
   }
   
   public void run() {
      Random random = new SecureRandom();

      while(true) {
         try {     
            List<ProductStatic> products = source.getProducts();
            int productCount = products.size();
            int companyCount = companies.size();
            int randomCompany = random.nextInt(companyCount);
            int randomProduct = random.nextInt(productCount);
            int randomMid = random.nextInt(50) + 50;            
            int randomVolume = (random.nextInt(5) + 1) * 10;
            double randomSpread = random.nextInt(10) / 10.0d;
            
            if(randomProduct != 0) {
               ProductStatic product = products.get(randomProduct);
               String name = product.getName();
               String company = companies.get(randomCompany);
               
               createDepth(name, company, randomMid, randomSpread, randomVolume);               
            }            
            Thread.sleep(100);
         } catch(Exception e) {
            e.printStackTrace();          
         }
      }
   }
   
   public void createDepth(String product, String company, double mid, double spread, long volume) {
      processor.update(new Price(product, PriceType.EFP, Side.BID, company, mid + spread, volume));
      processor.update(new Price(product, PriceType.EFP, Side.OFFER, company, mid - spread, volume));      
      processor.update(new Price(product, PriceType.OUTRIGHT, Side.BID, company, mid / 10.0 + spread, volume));
      processor.update(new Price(product, PriceType.OUTRIGHT, Side.OFFER, company, mid / 10.0 - spread, volume));    
   }

}
