package org.simpleframework.demo.predicate;

public class Not implements Predicate {

   private final Predicate predicate;

   public Not(Predicate predicate) {
      this.predicate = predicate;
   }

   @Override
   public boolean accept(Argument argument) {
      return !predicate.accept(argument);
   }

   @Override
   public String toString() {
      return String.format("!(%s)", predicate);
   }
}
