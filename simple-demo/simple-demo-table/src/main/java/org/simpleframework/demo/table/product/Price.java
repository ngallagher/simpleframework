package org.simpleframework.demo.table.product;

public class Price {
   
   private final PriceType type;
   private final String security;
   private final String company;
   private final Double price;
   private final Long volume;
   private final Side side;
   
   public Price(String security, PriceType type, Side side, String company, Double price, Long volume) {
      this.security = security;
      this.company = company;
      this.price = price;
      this.volume = volume;
      this.type = type;
      this.side = side;
   }
   
   public Side getSide() {
      return side;
   }
   
   public String getSecurity() {
      return security;
   }

   public PriceType getType() {
      return type;
   }

   public String getCompany() {
      return company;
   }

   public Double getPrice() {
      return price;
   }

   public Long getVolume() {
      return volume;
   }
   
   public String toString() {
      return String.format("security=%s,type=%s,side=%s,company=%s,price=%s,volume=%s", security, type, side, company, price, volume);
   }
}
