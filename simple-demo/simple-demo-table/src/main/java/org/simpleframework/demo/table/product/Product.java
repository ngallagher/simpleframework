package org.simpleframework.demo.table.product;

import java.util.Map;

public class Product {
   
   private final Map<PriceType, PricePair> best;
   private final Map<PriceType, PricePair> user;
   private final String security;
   private final long version;
   
   public Product(String security, Map<PriceType, PricePair> best, Map<PriceType, PricePair> company, long version) {
      this.version = version;
      this.security = security;
      this.best = best;
      this.user = company;
   }
   
   public long getVersion() {
      return version;
   }
   
   public String getSecurity() {
      return security;
   }

   public PricePair getBestEFP() {
      return best.get(PriceType.EFP);
   }
   
   public PricePair getBestOutright() {
      return best.get(PriceType.OUTRIGHT);
   }   
   
   public PricePair getCompanyEFP() {
      return user.get(PriceType.EFP);
   }
   
   public PricePair getCompanyOutright() {
      return user.get(PriceType.OUTRIGHT);
   }   
}
