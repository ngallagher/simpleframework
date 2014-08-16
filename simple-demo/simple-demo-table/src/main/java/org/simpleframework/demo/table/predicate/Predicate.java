package org.simpleframework.demo.table.predicate;

/**
 * A predicate is used to evaluate an expression based on the attributes of the
 * provided argument. Arguments provided should expose attributes as primitive
 * types such as strings, integers, doubles, and any other primitive type.
 * 
 * @author Niall Gallagher
 */
public interface Predicate {
   boolean accept(Argument argument);
}
