package org.simpleframework.demo.predicate;

public enum Condition {
   AND("&&") {
      public Predicate combine(Stack<Predicate> predicates) {
         Predicate top = predicates.pop();
         Predicate bottom = predicates.pop();

         return new And(bottom, top);
      }
   },
   OR("||") {
      public Predicate combine(Stack<Predicate> predicates) {
         Predicate top = predicates.pop();
         Predicate bottom = predicates.pop();

         return new Or(bottom, top);
      }
   },
   NOT("!") {
      public Predicate combine(Stack<Predicate> predicates) {
         Predicate predicate = predicates.pop();

         return new Not(predicate);
      }
   };

   public final String condition;

   private Condition(String condition) {
      this.condition = condition;
   }

   public abstract Predicate combine(Stack<Predicate> predicates);

   public static Condition resolveCondition(String token) {
      for (Condition condition : values()) {
         if (condition.condition.equals(token)) {
            return condition;
         }
      }
      throw new IllegalStateException("No such condition '" + token + "'");
   }
}
