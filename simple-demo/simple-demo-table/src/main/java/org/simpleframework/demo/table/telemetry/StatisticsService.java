package org.simpleframework.demo.table.telemetry;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class StatisticsService implements Service {

   private static final Logger LOG = Logger.getLogger(StatisticsService.class);

   private final SampleAverager averageRoundTrip;
   private final SampleAverager averagePaint;
   private final Set<Session> sessions;

   public StatisticsService() {
      this.sessions = new CopyOnWriteArraySet<Session>();
      this.averagePaint = new SampleAverager();
      this.averageRoundTrip = new SampleAverager();
   }
   
   public void update(String user, long paintTime, long roundTripTime, long rowChanges) {
      long time = System.currentTimeMillis();
      long totalPaint = averagePaint.count();
      long totalRoundTrip = averageRoundTrip.count();
      
      if(totalPaint > 1000) {
         long lastAverage = averagePaint.average();
         
         averagePaint.reset();         
         averagePaint.sample(lastAverage);
      }
      if(totalRoundTrip > 1000) {
         long lastAverage = averageRoundTrip.average();
         
         averageRoundTrip.reset();
         averageRoundTrip.sample(lastAverage);
      }
      averagePaint.sample(paintTime);
      averageRoundTrip.sample(roundTripTime);
      
      for (Session session : sessions) {
         Request request = session.getRequest();
         String query = request.getParameter("user");
         WebSocket socket = session.getSocket();
         
         if(query != null && query.equals(user)) {
            try {
               socket.send("averagePaintTime:"+time+","+averagePaint.average());
               socket.send("averageRoundTripTime:"+time+","+averageRoundTrip.average());               
               socket.send("paintTime:"+time+","+paintTime);
               socket.send("roundTripTime:"+time+","+roundTripTime);
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
