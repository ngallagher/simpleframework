package org.simpleframework.demo.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.SelectableChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.transport.trace.Analyzer;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.util.thread.Daemon;

public class ConsoleAnalyzer extends Daemon implements Analyzer {
   
   private final Queue<TraceRecord> queue;
   private final AtomicLong count;
   private final String filter;
   
   public ConsoleAnalyzer() {
      this(null);
   }
   
   public ConsoleAnalyzer(String filter) {
      this.queue = new ConcurrentLinkedQueue<TraceRecord>();
      this.count = new AtomicLong();
      this.filter = filter;
   }   

   @Override
   public Trace attach(SelectableChannel channel) {     
      return new TraceFeeder(channel);
   }
   
   public void run() {
      try {
         while(isActive()) {
            Thread.sleep(1000);
            
            while(!queue.isEmpty()) {
               TraceRecord record = queue.poll();
               
               if(filter != null) {
                  Object event = record.event;
                  Class type = event.getClass();
                  String name = type.getName();
                  
                  if(name.contains(filter)) {
                     System.err.println(record);
                  }
               } else {               
                  System.err.println(record);
               }
            }        
         }
      } catch(Exception e) {
         e.printStackTrace();
      }
      
   }
   
   private class TraceFeeder implements Trace {
      
      private final SelectableChannel channel;
      private final long sequence;
      
      public TraceFeeder(SelectableChannel channel) {
         this.sequence = count.getAndIncrement();
         this.channel = channel;
      }

      @Override
      public void trace(Object event) {
         trace(event, null);
      }

      @Override
      public void trace(Object event, Object value) {
         TraceRecord record = new TraceRecord(channel, event, value, sequence);
         
         if(isActive()) {
            queue.offer(record);
         }
      }
      
   }
   
   private class TraceRecord {
      
      private final SelectableChannel channel;
      private final String thread;
      private final Object event;
      private final Object value;
      private final long sequence;
      
      public TraceRecord(SelectableChannel channel, Object event, Object value, long sequence) {
         this.thread = Thread.currentThread().getName();
         this.sequence = sequence;
         this.channel = channel;
         this.event = event;
         this.value = value;
      }
      
      public String toString() {
         StringWriter builder = new StringWriter();
         PrintWriter writer = new PrintWriter(builder);
         
         writer.print(sequence);         
         writer.print(" ["); 
         writer.print(channel);
         writer.print("]");
         writer.print(" ");
         writer.print(thread);
         writer.print(": ");
         writer.print(event);
         
         if(value != null) {
            if(value instanceof Throwable) {
               ((Throwable)value).printStackTrace(writer);
            } else {
               writer.print(" -> ");
               writer.print(value);
            }
         }
         writer.close();
         return builder.toString();
      }
   }

}
