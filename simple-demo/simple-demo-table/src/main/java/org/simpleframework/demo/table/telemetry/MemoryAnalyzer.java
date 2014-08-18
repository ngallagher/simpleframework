package org.simpleframework.demo.table.telemetry;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class MemoryAnalyzer extends Thread implements Service {

   private static final Logger LOG = Logger.getLogger(MemoryAnalyzer.class);

   private final Set<WebSocket> sockets;

   public MemoryAnalyzer() {
      this.sockets = new CopyOnWriteArraySet<WebSocket>();
   }

   public void connect(Session connection) {
      WebSocket socket = connection.getSocket();

      try {
         sockets.add(socket);
      } catch (Exception e) {
         LOG.info("Problem joining chat room", e);
      }
   }

   public void run() {
      while (true) {
         try {
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threads = ManagementFactory.getThreadMXBean();            
            MemoryUsage usage = memory.getHeapMemoryUsage();
            long time = System.currentTimeMillis();
            double count = threads.getThreadCount();
            double max = usage.getMax();
            double used = usage.getUsed();
            double free = max - used;
            String heapUsedPoint = String.format("heapUsed:%s,%s", time, used);
            String heapFreePoint = String.format("heapFree:%s,%s", time, free);
            String threadCountPoint = String.format("threadCount:%s,%s", time, count);            
            
            for (WebSocket socket : sockets) {
               try {
                  socket.send(heapUsedPoint);
                  socket.send(heapFreePoint);
                  socket.send(threadCountPoint);                   
               } catch (Exception e) {
                  sockets.remove(socket);
                  LOG.info("Problem sending point", e);
               } 
            }
            Thread.sleep(50);
         } catch (Exception e) {
            LOG.info("Could not send point", e);
         }
      }
   }
}