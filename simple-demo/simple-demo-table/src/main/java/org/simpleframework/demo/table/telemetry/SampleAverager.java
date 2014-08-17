package org.simpleframework.demo.table.telemetry;

public class SampleAverager {

   private long maximum;
   private long minimum;
   private long count;
   private long sum;

   public SampleAverager() {
      this.minimum = Long.MAX_VALUE;
      this.maximum = Long.MIN_VALUE;
   }

   public synchronized long sum() {
      return sum;
   }

   public synchronized long count() {
      return count;
   }

   public synchronized long maximum() {
      return maximum;
   }

   public synchronized long minimum() {
      return minimum;
   }

   public synchronized long average() {
      if (count > 0) {
         return sum / count;
      }
      return 0;
   }

   public synchronized void reset() {
      minimum = Long.MAX_VALUE;
      maximum = Long.MIN_VALUE;
      count = 0;
      sum = 0;
   }

   public synchronized void sample(long sample) {
      if (sample > maximum) {
         maximum = sample;
      }
      if (sample < minimum) {
         minimum = sample;
      }
      sum += sample;
      count++;
   }
}
