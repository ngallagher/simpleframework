package org.simpleframework.util.lease;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaseManagerTest extends TimeTestCase {

   private static int ITERATIONS = 1000;
   private static int MAXIMUM = 20000;

   static {
      String value = System.getProperty("iterations");

      if (value != null) {
         ITERATIONS = Integer.parseInt(value);
      }
   }
   
   public void testClock() {
      List<Long> timeList = new ArrayList<Long>();
      
      for(int i = 0; i < ITERATIONS; i++) {
         long time = System.nanoTime();
         long milliseconds = TimeUnit.MILLISECONDS.convert(time, TimeUnit.MILLISECONDS);
         
         timeList.add(milliseconds);
      }
      for(int i = 1; i < ITERATIONS; i++) {
         assertLessThanOrEqual(timeList.get(i - 1), timeList.get(i));         
      }
   }
   
   public void testRandom() {
      for(int i = 0; i < ITERATIONS; i++) {
         long randomTime = getRandomTime(MAXIMUM);
         
         assertGreaterThanOrEqual(MAXIMUM, randomTime);
         assertGreaterThanOrEqual(randomTime, 0);
      }      
   }
   
   public void testOrder() throws Exception {
      final BlockingQueue<Integer> clean = new LinkedBlockingQueue<Integer>();
      final ConcurrentHashMap<Integer, Long> record = new ConcurrentHashMap<Integer, Long>();
      
      Cleaner<Integer> cleaner = new Cleaner<Integer>() {
         
         long start = System.currentTimeMillis();
         
         public void clean(Integer key) {
            record.put(key, start - System.currentTimeMillis());
            clean.offer(key);
            
         }
      };
      LeaseManager<Integer> manager = new LeaseManager<Integer>(cleaner);
      List<Lease<Integer>> list = new ArrayList<Lease<Integer>>();
      
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < ITERATIONS; i++) {
         long randomTime = getRandomTime(MAXIMUM) + MAXIMUM + i * 50;
         
         System.err.printf("leasing [%s] for [%s] @ %s%n", i, randomTime, System.currentTimeMillis() - start);
         
         Lease<Integer> lease = manager.lease(i, randomTime, TimeUnit.MILLISECONDS);
         
         list.add(lease);
      }
      start = System.currentTimeMillis();
      
      for(int i = 0; i < ITERATIONS; i++) {
         try {
            System.err.printf("renewing [%s] for [%s] expires [%s] @ %s expired [%s] %n", i, i, list.get(i).getExpiry(TimeUnit.MILLISECONDS), System.currentTimeMillis() - start, record.get(i));
            list.get(i).renew(i, TimeUnit.MILLISECONDS);
         }catch(Exception e) {
            System.err.printf("Lease %s in error: ", i);
            e.printStackTrace(System.err);
         }
      }     
      int variation = 20;
      int cleaned = 0;
      
      for(int i = 0; i < ITERATIONS; i++) {
         int value = clean.take();
         cleaned++;
         
         System.err.printf("index=[%s] clean=[%s] expiry[%s]=%s expiry[%s]=%s%n ", i, value, i, record.get(i), value, record.get(value));
         assertLessThanOrEqual(i - variation, value);
      }      
      assertEquals(cleaned, ITERATIONS);
   }

   public void testLease() throws Exception {
      final BlockingQueue<Expectation> clean = new LinkedBlockingQueue<Expectation>();
      
      Cleaner<Expectation> cleaner = new Cleaner<Expectation>() {
         public void clean(Expectation key) {
            clean.offer(key);
         }
      };           
      final BlockingQueue<Lease<Expectation>> renewalQueue = new LinkedBlockingQueue<Lease<Expectation>>();      
      final BlockingQueue<Lease<Expectation>> expiryQueue = new LinkedBlockingQueue<Lease<Expectation>>();
      final CountDownLatch ready = new CountDownLatch(21);
      final AtomicInteger renewCount = new AtomicInteger(ITERATIONS);
      
      for(int i = 0; i < 20; i++) {
         new Thread(new Runnable() {
            public void run() {              
               while(renewCount.getAndDecrement() > 0) {                
                  long randomTime = getRandomTime(MAXIMUM);
                  
                  try {
                     ready.countDown();  
                     ready.await();                 
    
                     Lease<Expectation> lease = renewalQueue.take();

                     try {
                        lease.renew(randomTime, TimeUnit.MILLISECONDS);
                        lease.getKey().setExpectation(randomTime, TimeUnit.MILLISECONDS);

                        assertGreaterThanOrEqual(randomTime, 0);
                        assertGreaterThanOrEqual(randomTime, lease.getExpiry(TimeUnit.MILLISECONDS));
                     } catch(Exception e) {
                        expiryQueue.offer(lease);
                     }
                  } catch(Exception e) {
                     e.printStackTrace();
                  }
               }
            }
         }).start();
      }         
      final LeaseManager<Expectation> manager = new LeaseManager<Expectation>(cleaner);
      final CountDownLatch latch = new CountDownLatch(21);
      final AtomicInteger leaseCount = new AtomicInteger(ITERATIONS);
      
      for(int i = 0; i < 20; i++) {
         new Thread(new Runnable() {
            public void run() {
               while(leaseCount.getAndDecrement() > 0) {
                  long randomTime = getRandomTime(MAXIMUM);
                  Expectation expectation = new Expectation(randomTime, TimeUnit.MILLISECONDS);
                  
                  try {
                     latch.countDown();
                     latch.await();
                  } catch(InterruptedException e) {
                     e.printStackTrace();
                  }
                  assertGreaterThanOrEqual(randomTime, 0);

                  Lease<Expectation> lease = manager.lease(expectation, randomTime, TimeUnit.MILLISECONDS);                  
                  renewalQueue.offer(lease);                  
               }
            }
         }).start();
      }
      ready.countDown();
      latch.countDown();      

      for (int i = 0; i < ITERATIONS; i++) {
         Expectation expectation = clean.poll(MAXIMUM, TimeUnit.MILLISECONDS);
         
         if(expectation != null) {           
            long accuracy = System.nanoTime() - expectation.getExpectation(TimeUnit.NANOSECONDS);
            long milliseconds = TimeUnit.MILLISECONDS.convert(accuracy, TimeUnit.NANOSECONDS);         
         
            System.err.printf("index=[%s] accuracy=[%s] queue=[%s]%n", i, milliseconds, clean.size());
         } else {
            System.err.printf("index=[%s] queue=[%s]%n", i, clean.size());
         }
         
      }
      System.err.printf("waiting=[%s]%n", clean.size());
   }

   
   public static class Expectation {
      
      private long time;
   
      public Expectation(long duration, TimeUnit unit) {
         setExpectation(duration, unit);
      }
      
      public void setExpectation(long duration, TimeUnit unit) {
         long nano = TimeUnit.NANOSECONDS.convert(duration, unit);
         long expect = nano + System.nanoTime();
         
         this.time = expect;
      }
      
      public long getExpectation(TimeUnit unit) {
         return unit.convert(time, TimeUnit.NANOSECONDS); 
      }
   }   

   
   public static long getRandomTime(long maximum) {
      long random = new Random().nextLong() % maximum;

      if(random < 0) {
        random *= -1;              
      }
      return random;
   }

   public static void main(String[] list) throws Exception {
      new LeaseManagerTest().testClock();
      new LeaseManagerTest().testRandom();
      new LeaseManagerTest().testOrder();
      new LeaseManagerTest().testLease();
   }
}
