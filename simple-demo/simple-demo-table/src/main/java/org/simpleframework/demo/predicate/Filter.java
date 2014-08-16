package org.simpleframework.demo.predicate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Filter {

   private final Map<String, Predicate> predicates;
   private final Map<String, String> expressions;

   public Filter(Map<String, String> expressions) {
      this.predicates = new ConcurrentHashMap<String, Predicate>();
      this.expressions = expressions;
   }

   public Predicate getPredicate(String type) {
      Predicate predicate = predicates.get(type);

      if (predicate == null) {
         String expression = expressions.get(type);

         if (expression == null) {
            return null;
         }
         predicate = new PredicateParser(expression);
         predicates.put(type, predicate);
      }
      return predicate;
   }
}
