package org.simpleframework.demo.table.predicate;

import java.util.LinkedList;

public class Stack<T> {

   private LinkedList<T> stack;

   public Stack() {
      this.stack = new LinkedList<T>();
   }

   public void clear() {
      stack.clear();
   }

   public boolean isEmpty() {
      return stack.isEmpty();
   }

   public void push(T value) {
      stack.addFirst(value);
   }

   public T pop() {
      return stack.removeFirst();
   }

   @Override
   public String toString() {
      return stack.toString();
   }
}