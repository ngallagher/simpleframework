package org.simpleframework.http.validate;

import java.util.concurrent.atomic.AtomicLong;

public class Averager {

   private final AtomicLong count;
   private final AtomicLong total;
   private volatile long max;
   private volatile long min;
   
   public Averager() {
      this.count = new AtomicLong(1);
      this.total = new AtomicLong(1);
      this.clear();
   }

   public void clear() {
     min = 0;
     max = 0;
   }
   
   public void  sample(long time) {
      if(time > max) {
        max = time;              
      }            
      if(time < min) {
        min = time;                       
      }
      count.getAndIncrement();
      total.getAndAdd(time);
   }

   public long getMin() {
      return min;           
   }

   public long getMax() {
      return max;           
   }

   public long getCount() {
      return count.get();           
   }
   
   public long getAverage() {
      return total.get() / count.get();
   }
}
