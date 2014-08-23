package org.simpleframework.common.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.simpleframework.common.thread.ConcurrentExecutor;

public class TransientApplication {

   public static void main(String[] list) throws Exception {
      BlockingQueue queue = new LinkedBlockingQueue();
      ConcurrentExecutor pool = new ConcurrentExecutor(TerminateTask.class, 10);      
      
      for(int i = 0; i < 50; i++) {
         pool.execute(new LongTask(queue, String.valueOf(i)));
      }
      pool.execute(new TerminateTask(pool));      
   }
   
   private static class TerminateTask implements Runnable {
      
      private ConcurrentExecutor pool;
      
      public TerminateTask(ConcurrentExecutor pool) {
         this.pool = pool;
      }
      
      public void run() {
         pool.stop();
      }
   }   
   
   private static class LongTask implements Runnable {
      
      private BlockingQueue queue;
      
      private String name;
      
      public LongTask(BlockingQueue queue, String name) {
         this.queue = queue;
         this.name = name;
      }
      
      public void run() {         
         try {            
            Thread.sleep(1000);
         } catch(Exception e) {
            e.printStackTrace();
         }         
         System.err.println(name);
         queue.offer(name);
      }
   }
}
