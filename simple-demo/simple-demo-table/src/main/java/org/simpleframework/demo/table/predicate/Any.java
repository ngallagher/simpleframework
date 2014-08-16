package org.simpleframework.demo.table.predicate;

public class Any implements Predicate {

   @Override
   public boolean accept(Argument argument) {
      return true;
   }

   @Override
   public String toString() {
      return "(*)";
   }
}
