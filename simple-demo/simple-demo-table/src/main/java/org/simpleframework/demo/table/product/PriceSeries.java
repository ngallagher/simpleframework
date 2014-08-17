package org.simpleframework.demo.table.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PriceSeries {

   private final List<Price> prices;
   
   public PriceSeries(){
      this(Collections.EMPTY_LIST);
   }
   
   public PriceSeries(Collection<Price> prices) {
      this.prices = new ArrayList<Price>(prices);
   }   
   
   public Price getAt(int index) {
      int size = prices.size();
      
      if(size > index) {
         return prices.get(index);
      }
      return null;
   }   
   
   public Price getTop() {
      int size = prices.size();
      
      if(size > 0) {
         return prices.get(0);
      }
      return null;
   }
   
   public Price getBottom() {
      int size = prices.size();
      
      if(size > 0) {
         return prices.get(size -1);
      }
      return null;
   }
   
   public boolean isEmpty(){
      return prices.isEmpty();
   }
   
   public int size() {
      return prices.size();
   }
   
   @Override
   public String toString() {
      return String.valueOf(prices);
   }
}
