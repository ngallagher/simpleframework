package org.simpleframework.common.lease;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.simpleframework.common.lease.Contract;
import org.simpleframework.common.lease.ContractQueue;
import org.simpleframework.common.lease.Expiration;

public class ContractQueueTest extends TimeTestCase {
   
   public void testTimeUnits() throws Exception {
      ContractQueue<Long> queue = new ContractQueue<Long>();
      List<String> complete = new ArrayList<String>();
      
      for(long i = 0; i < 10000; i++) {
         long random = (long)(Math.random() * 1000);
         Contract<Long> contract = new Expiration(random, random, TimeUnit.NANOSECONDS);
         
         queue.offer(contract);
      }
      for(int i = 0; i < 10000; i++) {
         Contract<Long> contract = queue.take();
         
         assertGreaterThanOrEqual(contract.getDelay(TimeUnit.NANOSECONDS), contract.getDelay(TimeUnit.NANOSECONDS));
         assertGreaterThanOrEqual(contract.getDelay(TimeUnit.MILLISECONDS), contract.getDelay(TimeUnit.MILLISECONDS));
         assertGreaterThanOrEqual(contract.getDelay(TimeUnit.SECONDS), contract.getDelay(TimeUnit.SECONDS));        
         
         long nanoseconds = contract.getDelay(TimeUnit.NANOSECONDS);
         long milliseconds = contract.getDelay(TimeUnit.MILLISECONDS);
         
         complete.add(String.format("index=[%s] nano=[%s] milli=[%s]", i, nanoseconds, milliseconds));
      }
      for(int i = 0; i < 10000; i++) {
         System.err.println("expiry=[" + complete.get(i)+ "]");         
      }      
   }   
   
   public void testAccuracy() throws Exception {
      ContractQueue<Long> queue = new ContractQueue<Long>();
      
      for(long i = 0; i < 10000; i++) {
         long random = (long)(Math.random() * 1000);
         Contract<Long> contract = new Expiration(random, random, TimeUnit.NANOSECONDS);
         
         queue.offer(contract);
      }
      for(int i = 0; i < 10000; i++) {
         Contract<Long> contract = queue.take();
         
         assertLessThanOrEqual(-2000, contract.getDelay(TimeUnit.MILLISECONDS));
      }      
   }

}
 
