package org.simpleframework.http.parse;

import java.util.PriorityQueue;

import junit.framework.TestCase;

public class PriorityQueueTest extends TestCase {
   
   
   private static class Entry implements Comparable<Entry> {
      
      private final String text;
      private final int priority;
      private final int start;
      
      public Entry(String text, int priority, int start) {
         this.priority = priority;
         this.start = start;
         this.text = text;
      }
      
      public int compareTo(Entry entry) {
         int value = entry.priority - priority;
         
         if(value == 0) {
            return entry.start - start;
         }
         return value;
      }
   }
   public void testPriorityQueue() {
      PriorityQueue<Entry> queue = new PriorityQueue<Entry>();
      int start = 10000;
      
      queue.offer(new Entry("a", 10, start--));
      queue.offer(new Entry("b", 10, start--));
      queue.offer(new Entry("c", 10, start--));
      queue.offer(new Entry("d", 10, start--));
      queue.offer(new Entry("e", 20, start--));
      queue.offer(new Entry("f", 30, start--));
      queue.offer(new Entry("g", 20, start--));
      
      while(!queue.isEmpty()) {
         System.err.println(queue.remove().text);
      }
   }

}
