package org.simpleframework.http.validate;

public class LogEvent {

   private final long throughput;
   private final long totalRequests;
   private final long connectionCount;
   private final long responseLatency;
   private final long maxLatency;
   private final long minLatency;
   private final long concurrency;
   private final long waitingThreads;
   private final long totalThreads;
   private final long freeMemory;
   private final long totalMemory;
   private final long bytesSent;
   private final long totalBytes;
   private final long sample;
   private final long sampleTime;


public LogEvent( long sample,
                             long sampleTime,
                             long throughput, 
                             long totalRequests,
                             long connectionCount, 
                             long responseLatency,
                             long maxLatency,
                             long minLatency,
                             long concurrency,
                             long waitingThreads,
                             long totalThreads,
                             long freeMemory,
                             long totalMemory,
                             long bytesSent,
                             long totalBytes) 
  {
      this.sample = sample;
      this.sampleTime = sampleTime;
      this.throughput = throughput;
      this.totalRequests = totalRequests;
      this.connectionCount = connectionCount;
      this.responseLatency = responseLatency;
      this.maxLatency = maxLatency;
      this.minLatency = minLatency;
      this.concurrency = concurrency;
      this.waitingThreads = waitingThreads;
      this.totalThreads = totalThreads;
      this.freeMemory = freeMemory;
      this.totalMemory = totalMemory;
      this.bytesSent = bytesSent;
      this.totalBytes = totalBytes;
  }

   public long getMaxLatency() {
   return maxLatency;
}

public long getMinLatency() {
   return minLatency;
}

   public long getSample() {
      return sample;
   }
   
   public long getSampleTime() {
      return sampleTime;
   }

   public long getTotalBytes() {
      return totalBytes;
   }
   
   public long getTotalRequests() {
      return totalRequests;
   }

   public long getBytesSent() {
      return bytesSent;
   }
   
   public long getConcurrency() {
      return concurrency;
   }
   
   public long getConnectionCount() {
      return connectionCount;
   }
   
   public long getFreeMemory() {
      return freeMemory;
   }
   
   public long getResponseLatency() {
      return responseLatency;
   }
   
   public long getThroughput() {
      return throughput;
   }
   
   public long getTotalMemory() {
      return totalMemory;
   }
   
   public long getTotalThreads() {
      return totalThreads;
   }
   
   public long getWaitingThreads() {
      return waitingThreads;
   }
}
