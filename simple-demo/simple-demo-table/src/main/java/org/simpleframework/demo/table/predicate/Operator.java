package org.simpleframework.demo.table.predicate;

public enum Operator {
   EQUAL("==") {
      public boolean evaluate(String left, String right, Type type) {
         Comparable first = type.convert(left);
         Comparable second = type.convert(right);

         return first.equals(second);
      }
   },
   NOT_EQUAL("!=") {
      public boolean evaluate(String left, String right, Type type) {
         Comparable first = type.convert(left);
         Comparable second = type.convert(right);

         return !first.equals(second);
      }
   },
   GREATER(">") {
      public boolean evaluate(String left, String right, Type type) {
         Comparable first = type.convert(left);
         Comparable second = type.convert(right);

         return first.compareTo(second) > 0;
      }
   },
   GREATER_OR_EQUAL(">=") {
      public boolean evaluate(String left, String right, Type type) {
         Comparable first = type.convert(left);
         Comparable second = type.convert(right);

         return first.compareTo(second) >= 0;
      }
   },
   LESS("<") {
      public boolean evaluate(String left, String right, Type type) {
         Comparable first = type.convert(left);
         Comparable second = type.convert(right);

         return first.compareTo(second) < 0;
      }
   },
   LESS_OR_EQUAL("<=") {
      public boolean evaluate(String left, String right, Type type) {
         Comparable first = type.convert(left);
         Comparable second = type.convert(right);

         return first.compareTo(second) <= 0;
      }
   },
   LIKE("=~") {
      public boolean evaluate(String left, String right, Type type) {
         return left.matches(right);
      }
   };

   public final String operator;

   private Operator(String operator) {
      this.operator = operator;
   }

   public abstract boolean evaluate(String left, String right, Type type);

   public static Operator resolveOperator(String token) {
      for (Operator operator : values()) {
         if (operator.operator.equals(token)) {
            return operator;
         }
      }
      throw new IllegalStateException("No such operator '" + token + "'");
   }
}
