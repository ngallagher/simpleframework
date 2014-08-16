package org.simpleframework.demo.table.product;

public class PricePair {
   
   private final String security;
   private final PriceType type;
   private final Price bid;
   private final Price offer;   
   
   public PricePair(String security, PriceType type, Price bid, Price offer) {
      this.security = security;
      this.type = type;
      this.bid = bid;
      this.offer = offer;
   }
   
   public PriceType getType() {
      return type;
   }   
   
   public String getSecurity() {
      return security;
   }   
   
   public Price getBid() {
      return bid;
   }
   
   public Price getOffer() {
      return offer;
   }
}
