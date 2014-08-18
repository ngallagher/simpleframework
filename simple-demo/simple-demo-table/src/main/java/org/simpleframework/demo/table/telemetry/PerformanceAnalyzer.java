package org.simpleframework.demo.table.telemetry;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class PerformanceAnalyzer implements Service {

   private static final Logger LOG = Logger.getLogger(PerformanceAnalyzer.class);

   private final SampleAverager averageRoundTrip;
   private final SampleAverager averagePaint;
   private final Set<Session> sessions;

   public PerformanceAnalyzer() {
      this.sessions = new CopyOnWriteArraySet<Session>();
      this.averagePaint = new SampleAverager();
      this.averageRoundTrip = new SampleAverager();
   }
   
   public void update(String user, long paintTime, long roundTripTime, long rowChanges) {
      long time = System.currentTimeMillis();
      long totalPaint = averagePaint.count();
      long totalRoundTrip = averageRoundTrip.count();
      long maxMemory = Runtime.getRuntime().maxMemory();
      long freeMemory = Runtime.getRuntime().freeMemory();
      long usedMemory = maxMemory - freeMemory;
      long averagePaintTime = averagePaint.average();
      long averageRoundTripTime = averageRoundTrip.average();
      long averageNetworkTime = averageRoundTripTime - averagePaintTime;
      long networkTime = roundTripTime - paintTime;
      
      if(totalPaint > 1000) {
         averagePaint.reset();         
         averagePaint.sample(averagePaintTime);
      }
      if(totalRoundTrip > 1000) {
         averageRoundTrip.reset();
         averageRoundTrip.sample(averageRoundTripTime);
      }
      averagePaint.sample(paintTime);
      averageRoundTrip.sample(roundTripTime);
      
      for (Session session : sessions) {
         Request request = session.getRequest();
         String query = request.getParameter("user");
         WebSocket socket = session.getSocket();
         
         if(query != null && query.equals(user)) {
            try {
               socket.send("usedMemory:"+time+","+usedMemory);
               socket.send("freeMemory:"+time+","+freeMemory);                    
               socket.send("averagePaintTime:"+time+","+averagePaintTime);
               socket.send("averageNetworkTime:"+time+","+averageNetworkTime);               
               socket.send("paintTime:"+time+","+paintTime);
               socket.send("roundTripTime:"+time+","+roundTripTime);
               socket.send("networkTime:"+time+","+networkTime);               
               socket.send("rowsChanged:"+time+","+rowChanges);                             
            } catch (Exception e) {
               sessions.remove(session);
               LOG.info("Problem sending point", e);
            } 
         }
      }
   }

   public void connect(Session session) {
      try {
         sessions.add(session);
      } catch (Exception e) {
         LOG.info("Problem joining chat room", e);
      }
   }
}
