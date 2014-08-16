package org.simpleframework.demo.table.product;

import java.util.List;
import java.util.Map;

public class Depth {
   
   private final Map<PriceType, List<Price>> offer;
   private final Map<PriceType, List<Price>> bid;
   private final String security;
   private final long version;
   
   public Depth(String security, Map<PriceType, List<Price>> bid, Map<PriceType, List<Price>> offer, long version) {
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

   public List<Price> getBid(PriceType type) {
      return bid.get(type);
   }

   public List<Price> getOffer(PriceType type) {
      return offer.get(type);
   }
}
