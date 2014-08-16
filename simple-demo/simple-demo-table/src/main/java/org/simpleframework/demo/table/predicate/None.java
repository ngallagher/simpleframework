package org.simpleframework.demo.table.predicate;

public class None implements Predicate {

   @Override
   public boolean accept(Argument argument) {
      return false;
   }

   @Override
   public String toString() {
      return "(false)";
   }
}
