package org.simpleframework.demo.table.product;

import java.util.Map;

public class Depth {
   
   private final Map<PriceType, PriceSeries> offer;
   private final Map<PriceType, PriceSeries> bid;
   private final String security;
   private final long version;
   
   public Depth(String security, Map<PriceType, PriceSeries> bid, Map<PriceType, PriceSeries> offer, long version) {
      this.security = security;
      this.bid = bid;
      this.offer = offer;
      this.version = version;
   }
   
   public long getVersion() {
      return version;
   }   
   
   public String getSecurity() {
      return security;
   }

   public PriceSeries getBid(PriceType type) {
      return bid.get(type);
   }

   public PriceSeries getOffer(PriceType type) {
      return offer.get(type);
   }
}
