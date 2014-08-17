package org.simpleframework.demo.table.product;

public class Product {
   
   private final Depth companyDepth;
   private final Depth marketDepth;
   private final String security;
   private final long version;
   
   public Product(String security, Depth marketDepth, Depth companyDepth, long version) {
      this.companyDepth = companyDepth;
      this.marketDepth = marketDepth;
      this.version = version;
      this.security = security;
   }
   
   public long getVersion() {
      return version;
   }
   
   public String getSecurity() {
      return security;
   }

   public Depth getMarketDepth() {
      return marketDepth;
   }
   
   public Depth getCompanyDepth() {
      return companyDepth;
   }  
}
