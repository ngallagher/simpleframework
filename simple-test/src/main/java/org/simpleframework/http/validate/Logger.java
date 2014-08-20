package org.simpleframework.http.validate;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Logger extends Thread implements Measurement {

   private static final long KILOBYTE = 1024L;

   private final StatisticsLogger logger;
   private final Averager latency;
   private final ThreadMXBean tmbean;
   private final AtomicInteger concurrency;
   private final AtomicLong errors;
   private final AtomicLong received;
   private final AtomicLong sent;
   private final AtomicLong waiting;   
   private final AtomicLong connections;
   private final AtomicLong bytes;
   private final AtomicLong sample;
   private final long sampleDuration;
   private final long start;
   
   private volatile boolean dead;
   
   private long previousBytesSent;
   private long previousResponseCount;
   private long maxResponseCount;
   private long minResponseCount;
   
   public Logger(StatisticsLogger logger, AtomicInteger concurrency) {
      this(logger, concurrency, 0); // don't sample periodically
   }
   
   public Logger(StatisticsLogger logger, AtomicInteger concurrency, long sampleDuration) {
      this.tmbean = ManagementFactory.getThreadMXBean();
      this.latency = new Averager();
      this.errors = new AtomicLong();
      this.received = new AtomicLong();
      this.sent = new AtomicLong();
      this.waiting = new AtomicLong();
      this.connections = new AtomicLong();
      this.bytes = new AtomicLong();
      this.sample = new AtomicLong();      
      this.concurrency = concurrency;
      this.start = System.currentTimeMillis();
      this.sampleDuration = sampleDuration;
      this.logger = logger;
   }
   
   public long bytesToKilo(long bytes) {
      return bytes / KILOBYTE;
   }
   
   public void responseDuration(int count, long duration) {
      latency.sample(duration / count);
   }
   
   public void errorOccured() {     
      errors.getAndIncrement();
   }

   public void receivedResponse(int count) {
      received.addAndGet(count);      
   }

   public void sentRequest(int count) {
      sent.addAndGet(count);
   }

   public void threadRunning() {
      waiting.getAndDecrement();
   }

   public void threadWaiting() {
      waiting.getAndIncrement();
   }
   
   public void connectionEstablished() {
      connections.getAndIncrement();
   }
   
   public void connectionTerminated() {
      connections.getAndDecrement();
   }
   
   public void bytesTransferred(long byteCount) {
      bytes.getAndAdd(byteCount);      
   }
   
   public void kill() {
      try {
        dead = true;
        join();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
   
   public void run() {
      while(!dead && sampleDuration > 0) {
        try {
          Thread.sleep(sampleDuration); // print out performance periodically
          
          if(!dead) { // dont print out when dead as its not accurate
             sample();
          }
        }catch(Exception e) {
           e.printStackTrace();
        }
      }
   }
   
   public void sample() throws Exception {
      long responseCount = received.get();
      long requestsPerSample = responseCount - previousResponseCount;
      
      // track the bytes sent
      long bytesSent = bytes.get();
      long bytesSentPerSecond = bytesSent - previousBytesSent;

      if (requestsPerSample > maxResponseCount) {
        maxResponseCount = requestsPerSample;
      }
      if (previousResponseCount == 0) {
        minResponseCount = requestsPerSample;
      } else if (requestsPerSample < minResponseCount) { // clock min when we have had valid results
        minResponseCount = requestsPerSample;
      }
      logger.log(new LogEvent(
                   sample.getAndIncrement(),
                   System.currentTimeMillis() - start, // what time was the sample done at
                   requestsPerSample,
                   received.get(),
                   connections.get(),
                   latency.getAverage(),
                   latency.getMax(),
                   latency.getMin(),
                   concurrency.get(),
                   waiting.get(),
                   tmbean.getThreadCount(),
                   bytesToKilo(Runtime.getRuntime().freeMemory()),
                   bytesToKilo(Runtime.getRuntime().totalMemory()),
                   bytesSentPerSecond,
                   bytes.get()));            
      
      // remember previous count
      previousResponseCount = responseCount;
      previousBytesSent = bytesSent;
      latency.clear();
    }

}
