package org.simpleframework.demo.table.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PriceMerger {

   private final Map<String, Price> prices;
   private final PriceComparator comparator;
   private final Set<Price> sorted;
   
   public PriceMerger(int capacity) {
      this.comparator = new PriceComparator();
      this.sorted = new TreeSet<Price>(comparator);
      this.prices = new HashMap<String, Price>();
   }
   
   public List<Price> sort() {
      return new ArrayList<Price>(sorted);
   }
   
   public boolean merge(Price update) {
      String company = update.getCompany();
      Price existing = prices.remove(company);
      
      if(!same(update, existing)) {
         if(existing != null) {
            sorted.remove(existing);
         }
         prices.put(company, update);    
         sorted.add(update);
         return true;
      }
      return false;
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
