package org.simpleframework.demo.table.product;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PriceMerger {

   private final Map<String, Price> prices;
   private final PriceComparator comparator;
   private final Set<Price> sorted;
   
   public PriceMerger() {
      this(20);
   }
   
   public PriceMerger(int capacity) {
      this.comparator = new PriceComparator();
      this.sorted = new TreeSet<Price>(comparator);
      this.prices = new HashMap<String, Price>();
   }
   
   public PriceSeries sort() {
      return new PriceSeries(sorted);
   }
   
   public boolean merge(Price update) {
      String company = update.getCompany();
      Price existing = prices.remove(company);
      
      if(!same(update, existing)) {
         replace(update, existing);
         return true;
      }
      return false;
   }
   
   public void replace(Price update, Price existing) {
      if(existing == null) {
         Iterator<Price> prices = sorted.iterator();
         String key = update.getCompany();
         
         while(prices.hasNext()) {
            Price price = prices.next();
            String company = price.getCompany();
            
            if(company.equals(key)) { 
               prices.remove();
               break;
            }            
         }
      } else {
         sorted.remove(existing);
      }
      sorted.add(update);
   }

   public boolean same(Price update, Price existing) {
      if(!samePrice(update, existing)) {
         return false;
      }
      if(!sameVolume(update, existing)) {
         return false;
      }
      return true;
   }
   
   public boolean samePrice(Price update, Price existing) {      
      if(update != null && existing != null) {
         Double updatePrice = update.getPrice();
         Double existingPrice = existing.getPrice();
         
         return updatePrice.equals(existingPrice);
      }
      if(update != null) {
         return false;
      }
      return true;
   }
   
   public boolean sameVolume(Price update, Price existing) {
      if(update != null && existing != null) {
         Long updateVolume = update.getVolume();
         Long existingVolume = existing.getVolume();
         
         return updateVolume.equals(existingVolume);
      }
      if(update != null) {
         return false;
      }
      return true;
   }   
   
   public void clear() {
      prices.clear();
      sorted.clear();
   }
}
