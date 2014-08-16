package org.simpleframework.demo.table.reflect;

/**
 * Provides access to a specific resource within an object. This is typically
 * used to reflectively access a field or getter method.
 * 
 * @author Niall Gallagher
 */
public interface Accessor {
   <T> T getValue(Object source);
   Class getType();
}
