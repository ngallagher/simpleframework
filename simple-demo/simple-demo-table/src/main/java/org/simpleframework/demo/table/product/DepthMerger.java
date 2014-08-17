package org.simpleframework.demo.table.product;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DepthMerger {
   
   private final Map<PriceType, PriceMerger> bid;
   private final Map<PriceType, PriceMerger> offer;
   private final AtomicLong counter;
   private final String security;
   private final int capacity;

   public DepthMerger(String security) {
      this(security, 20);
   }
   
   public DepthMerger(String security, int capacity) {     
      this.bid = new HashMap<PriceType, PriceMerger>();
      this.offer = new HashMap<PriceType, PriceMerger>();
      this.counter = new AtomicLong();
      this.security = security;
      this.capacity = capacity;
   }

   public synchronized Depth merge(Price price) {      
      Side side = price.getSide();
      PriceType type = price.getType();
      PriceMerger merger = merge(side, type);
      
      if(merger.merge(price)) {  
         Map<PriceType, PriceSeries> bid = extract(Side.BID);
         Map<PriceType, PriceSeries> offer = extract(Side.OFFER);
         long version = counter.getAndIncrement();
         
         return new Depth(security, bid, offer, version);
      }
      return null;
   }
   
   private synchronized Map<PriceType, PriceSeries> extract(Side side) {
      Map<PriceType, PriceSeries> prices = new HashMap<PriceType, PriceSeries>();
      
      for(PriceType type : PriceType.values()) {
         PriceMerger merger = merge(side, type);
         PriceSeries series = merger.sort();
         
         prices.put(type, series);
      }
      return prices;
   }
   
   private synchronized PriceMerger merge(Side side, PriceType type) { 
      if(side == Side.BID) {
         PriceMerger merger = bid.get(type);
         
         if(merger == null) {
            merger = new PriceMerger(capacity);
            bid.put(type, merger);
         }
         return merger;
      }
      if(side == Side.OFFER) {
         PriceMerger merger = offer.get(type);
         
         if(merger == null) {
            merger = new PriceMerger(capacity);
            offer.put(type, merger);
         }
         return merger;
      }
      return null;
   }

}
