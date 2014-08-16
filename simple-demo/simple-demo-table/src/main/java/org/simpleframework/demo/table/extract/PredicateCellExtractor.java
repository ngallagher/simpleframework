package org.simpleframework.demo.table.extract;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.demo.table.predicate.Predicate;
import org.simpleframework.demo.table.predicate.PredicateParser;
import org.simpleframework.demo.table.predicate.PropertyArgument;
import org.simpleframework.demo.table.reflect.PropertyBinder;

public class PredicateCellExtractor implements CellExtractor {
   
   private final Map<Predicate, Object> predicates;
   private final Map<String, Object> expressions;
   private final PropertyBinder binder;
   private final Object failure;

   public PredicateCellExtractor(String expression, Object success, Object failure) {
      this(Collections.singletonMap(expression, success), failure);
   }
   
   public PredicateCellExtractor(Map<String, Object> expressions, Object failure) {
      this.predicates = new ConcurrentHashMap<Predicate, Object>();
      this.binder = new PropertyBinder();      
      this.expressions = expressions;
      this.failure = failure;
   }

   @Override
   public Object extract(Object object) {
      if(predicates.isEmpty()) {
         Set<String> values = expressions.keySet();
         
         for(String value : values) {
            Predicate predicate = new PredicateParser(value);
               
            if(value != null) {
               Object result = expressions.get(value);
               
               if(result != null) {
                  predicates.put(predicate, result);
               }
            }     
         }
      }
      return evaluate(object);
   }
   
   private Object evaluate(Object object) {
      PropertyArgument argument = new PropertyArgument(binder, object);
      
      if(!predicates.isEmpty()) {
         Set<Predicate> values = predicates.keySet();
         
         for(Predicate value : values) {
            Object result = predicates.get(value);
            
            if(value.accept(argument)) {
               return result;
            }
         }
      }
      return failure;
   }

}
