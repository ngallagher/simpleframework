package org.simpleframework.demo.predicate;

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
