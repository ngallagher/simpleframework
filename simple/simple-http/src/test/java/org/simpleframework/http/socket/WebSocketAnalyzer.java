package org.simpleframework.http.socket;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.SelectableChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.simpleframework.transport.trace.TraceAnalyzer;
import org.simpleframework.transport.trace.Trace;

public class WebSocketAnalyzer implements TraceAnalyzer {
   
   private final Map<SelectableChannel, Integer> map;
   private final AtomicInteger count;
   private final boolean debug;

   public WebSocketAnalyzer() {
      this(true);
   }
   
   public WebSocketAnalyzer(boolean debug) {
      this.map = new ConcurrentHashMap<SelectableChannel, Integer>();
      this.count = new AtomicInteger();
      this.debug = debug;
   }

   public Trace attach(SelectableChannel channel) {
      if(map.containsKey(channel)) {
         throw new IllegalStateException("Can't attach twice");
      }
      final int counter = count.getAndIncrement();
      map.put(channel, counter);
      
      return new Trace() {
         
         public void trace(Object event) {
            if(debug) {
               trace(event, "");
            }
         }
         
         public void trace(Object event, Object value) {
            if(debug) {
               if(value instanceof Throwable) {
                  StringWriter writer = new StringWriter();
                  PrintWriter out = new PrintWriter(writer);
                  ((Exception)value).printStackTrace(out);
                  out.flush();
                  value = writer.toString();
               }
               if(value != null && !String.valueOf(value).isEmpty()) {
                  System.err.printf("(%s) [%s] %s: %s%n", Thread.currentThread().getName(), counter, event, value);
               } else {
                  System.err.printf("(%s) [%s] %s%n", Thread.currentThread().getName(), counter, event);
               }          
            }
         }            
      };
   }

   public void stop() {
      System.err.println("Stop agent");
   }
}
