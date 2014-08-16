package org.simpleframework.demo.table.product;

import java.util.Comparator;

public class PriceComparator implements Comparator<Price> {

   @Override
   public int compare(Price left, Price right) {
      Double leftPrice = left.getPrice();
      Double rightPrice = right.getPrice();
      Side side = left.getSide();
      
      if(side == Side.BID) {
         return rightPrice.compareTo(leftPrice);         
      }
      return leftPrice.compareTo(rightPrice);
   }

}
