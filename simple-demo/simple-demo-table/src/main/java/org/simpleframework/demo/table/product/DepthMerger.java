package org.simpleframework.demo.table.product;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DepthMerger {
   
   private final Map<PriceType, PriceSorter> bid;
   private final Map<PriceType, PriceSorter> offer;
   private final AtomicLong counter;
   private final String security;
   private final int capacity;

   public DepthMerger(String security) {
      this(security, 20);
   }
   
   public DepthMerger(String security, int capacity) {     
      this.bid = new HashMap<PriceType, PriceSorter>();
      this.offer = new HashMap<PriceType, PriceSorter>();
      this.counter = new AtomicLong();
      this.security = security;
      this.capacity = capacity;
   }

   public synchronized Depth merge(Price price) {      
      Side side = price.getSide();
      PriceType type = price.getType();
      PriceSorter sorter = sort(side, type);
      
      if(sorter.update(price)) {  
         Map<PriceType, List<Price>> bid = extract(Side.BID);
         Map<PriceType, List<Price>> offer = extract(Side.OFFER);
         long version = counter.getAndIncrement();
         
         return new Depth(security, bid, offer, version);
      }
      return null;
   }
   
   private synchronized Map<PriceType, List<Price>> extract(Side side) {
      Map<PriceType, List<Price>> prices = new HashMap<PriceType, List<Price>>();
      
      for(PriceType type : PriceType.values()) {
         List<Price> values = extract(side, type);
         
         if(values == null) {
            values = Collections.emptyList();
         }
         prices.put(type, values);         
      }
      return prices;
   }
   
   private synchronized List<Price> extract(Side side, PriceType type) {
      PriceSorter sorter = sort(side, type);
      List<Price> list = sorter.sort();
      
      if(list.isEmpty()) {
         return Collections.emptyList();
      }
      return list;
   }
   
   private synchronized PriceSorter sort(Side side, PriceType type) {      
      if(side == Side.BID) {
         PriceSorter series = bid.get(type);
         
         if(series == null) {
            series = new PriceSorter(capacity);
            bid.put(type, series);
         }
         return series;
      }
      PriceSorter series = offer.get(type);
      
      if(series == null) {
         series = new PriceSorter(capacity);
         offer.put(type, series);
      }
      return series;
   }

}
