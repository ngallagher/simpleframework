package org.simpleframework.http.validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class StatisticsFileLogger implements StatisticsLogger{
   
   private final PrintStream logFile;
   
   public StatisticsFileLogger(File file) throws FileNotFoundException {
      this.logFile = new PrintStream(file);
      this.logFile.println("Sample,Sample Time,Throughput,Total Requests,Connection Count,Response Latency,Maximum Latency,Minimum Latency,Concurrency,Waiting Threads,Total Threads,Free Memory,Total Memory,Bytes Received,Total Bytes");
   }

   public void log(LogEvent event) throws Exception {
      logFile.println(
            event.getSample()+","+
            event.getSampleTime()+","+
            event.getThroughput()+","+
            event.getTotalRequests()+","+
            event.getConnectionCount()+","+
            event.getResponseLatency()+","+
            event.getMaxLatency()+","+
            event.getMinLatency()+","+
            event.getConcurrency()+","+
            event.getWaitingThreads()+","+
            event.getTotalThreads()+","+
            event.getFreeMemory()+","+
            event.getTotalMemory()+","+
            event.getBytesSent()+","+
            event.getTotalBytes());
   }

}
