package org.simpleframework.demo.table.product;

public class Product {
   
   private final Depth filteredDepth;
   private final Depth companyDepth;
   private final Depth actualDepth;
   private final String security;
   private final long version;
   
   public Product(String security, Depth actualDepth, Depth filteredDepth, Depth companyDepth, long version) {
      this.filteredDepth = filteredDepth;
      this.companyDepth = companyDepth;
      this.actualDepth = actualDepth;
      this.version = version;
      this.security = security;
   }
   
   public long getVersion() {
      return version;
   }
   
   public String getSecurity() {
      return security;
   }

   public Depth getActualDepth() {
      return actualDepth;
   }
   
   public Depth getFilteredDepth() {
      return filteredDepth;
   }
   
   public Depth getCompanyDepth() {
      return companyDepth;
   }  
}
