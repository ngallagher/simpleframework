package org.simpleframework.demo.table.predicate;

import static org.simpleframework.demo.table.predicate.Type.ARGUMENT;

/**
 * An evaluation is used to provide a predicate that can leverage a set of
 * predefined operators. This allows complex statements to be created that
 * contain several nested predicates. All expressions will evaluate attributes
 * with {@link String#valueOf} as this will take care of null attributes and
 * simplify text based statements.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.demo.table.predicate.Operator
 */
public class Evaluation implements Predicate {

   private final Operator operator;
   private final String key;
   private final String value;
   private final Type type;

   public Evaluation(String key, String value, String operator, Type type) {
      this.operator = Operator.resolveOperator(operator);
      this.value = value;
      this.type = type;
      this.key = key;
   }

   @Override
   public boolean accept(Argument argument) {
      Object attribute = argument.getAttribute(key);      
      String left = String.valueOf(attribute);
      String right = value;

      try {
         if(type == ARGUMENT) {
            attribute = argument.getAttribute(value);      
            right = String.valueOf(attribute);
         }
         return operator.evaluate(left, right, type);
      } catch(Exception e) {                                             
         throw new IllegalArgumentException("Could not evaluate " + this + " as type " + type + " with '" + left + "'", e);
      }
   }

   @Override
   public String toString() {
      return String.format("(%s %s %s)", key, operator.operator, value);
   }
}
