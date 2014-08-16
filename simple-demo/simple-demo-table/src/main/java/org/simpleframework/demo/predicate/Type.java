package org.simpleframework.demo.predicate;

import java.math.BigDecimal;

public enum Type {
   ARGUMENT {
      public Comparable convert(String value) {
         return value;
      }
   },   
   STRING {
      public Comparable convert(String value) {
         return value;
      }
   },
   NUMBER {
      public Comparable convert(String value) {
         return new BigDecimal(value);
      }
   },
   BOOLEAN {
      public Comparable convert(String value) {
         return new Boolean(value);
      }
   };

   public abstract Comparable convert(String value);
}
