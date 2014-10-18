package org.simpleframework.common.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.simpleframework.common.thread.ConcurrentScheduler;

import junit.framework.TestCase;

public class SchedulerTest extends TestCase {
   
   private static final int ITERATIONS = 10000;
   
   public void testScheduler() throws Exception {
      ConcurrentScheduler queue = new ConcurrentScheduler(Runnable.class, 10);
      LinkedBlockingQueue<Timer> list = new LinkedBlockingQueue<Timer>();
      
      for(int i = 0; i < ITERATIONS; i++) {
         queue.execute(new Task(list, new Timer(i)), i, TimeUnit.MILLISECONDS);
      }      
      for(Timer timer = list.take(); timer.getValue() < ITERATIONS - 10; timer = list.take()) {
         System.err.println("value=["+timer.getValue()+"] delay=["+timer.getDelay()+"] expect=["+timer.getExpectation()+"]");
      }      
   }
   
   public class Timer {
      
      private Integer value;
      
      private long time;
      
      public Timer(Integer value) {
         this.time = System.currentTimeMillis();
         this.value = value;
      }
      
      public Integer getValue() {
         return value;
      }
      
      public long getDelay() {
         return System.currentTimeMillis() - time;
      }
      
      public int getExpectation() {
         return value.intValue();
      }
   }
   
   public class Task implements Runnable {
      
      private LinkedBlockingQueue<Timer> queue;
      
      private Timer timer;
      
      public Task(LinkedBlockingQueue<Timer> queue, Timer timer) {
         this.queue = queue;
         this.timer = timer;
      }
      
      public void run() {
         queue.offer(timer);
      }     
   }
}
