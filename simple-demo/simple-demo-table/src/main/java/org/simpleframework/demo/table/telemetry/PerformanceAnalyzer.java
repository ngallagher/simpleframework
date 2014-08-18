package org.simpleframework.demo.table.telemetry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class PerformanceAnalyzer extends Thread implements Service {

   private static final Logger LOG = Logger.getLogger(PerformanceAnalyzer.class);

   private final Set<Session> sessions;

   public PerformanceAnalyzer() {
      this.sessions = new CopyOnWriteArraySet<Session>();
   }
   
   public void update(String user, long paintTime, long roundTripTime, long rowChanges) {
      for (Session session : sessions) {
         Request request = session.getRequest();
         String query = request.getParameter("user");
         
         if(query != null && query.equals(user)) {
            try {
               Map attributes = session.getAttributes();
               SampleAverager roundTripAverager = (SampleAverager)attributes.get("roundTripAverager");
               SampleAverager paintAverager = (SampleAverager)attributes.get("paintAverager");
               SampleAverager rowChangesAverager = (SampleAverager)attributes.get("rowChangesAverager");               
               SampleAverager networkTimeAverager = (SampleAverager)attributes.get("networkTimeAverager");  

               if(networkTimeAverager == null) {
                  networkTimeAverager = new SampleAverager();
                  attributes.put("networkTimeAverager", networkTimeAverager);
               }
               if(roundTripAverager == null) {
                  roundTripAverager = new SampleAverager();
                  attributes.put("roundTripAverager", roundTripAverager);
               }
               if(paintAverager == null) {
                  paintAverager = new SampleAverager();
                  attributes.put("paintAverager", paintAverager);
               }
               if(rowChangesAverager == null) {
                  rowChangesAverager = new SampleAverager();
                  attributes.put("rowChangesAverager", rowChangesAverager);
               }        
               networkTimeAverager.sample(roundTripTime - paintTime);
               rowChangesAverager.sample(rowChanges);
               roundTripAverager.sample(roundTripTime);
               paintAverager.sample(paintTime);                           
            } catch (Exception e) {
               sessions.remove(session);
               LOG.info("Problem sending point", e);
            } 
         }
      }
   }
   
   public void run() {
      while(true) {
         try {
            Thread.sleep(1000);
            
            for (Session session : sessions) {
               Request request = session.getRequest();
               WebSocket socket = session.getSocket();
            
               try {
                  Map attributes = session.getAttributes();
                  SampleAverager roundTripAverager = (SampleAverager)attributes.get("roundTripAverager");
                  SampleAverager paintAverager = (SampleAverager)attributes.get("paintAverager");
                  SampleAverager networkTimeAverager = (SampleAverager)attributes.get("networkTimeAverager");                  
                  SampleAverager rowChangesAverager = (SampleAverager)attributes.get("rowChangesAverager"); 
                  
                  if(roundTripAverager != null && paintAverager != null && rowChangesAverager != null && networkTimeAverager != null) {
                     long totalMemory = Runtime.getRuntime().totalMemory();
                     long freeMemory = Runtime.getRuntime().freeMemory();
                     long usedMemory = totalMemory - freeMemory;
                     long maximumPaintTime = paintAverager.maximum();
                     long maximumRoundTripTime = roundTripAverager.maximum();
                     long maximumNetworkTime = networkTimeAverager.maximum();                     
                     long averageRowChanges = rowChangesAverager.average();
                     long averagePaintTime = roundTripAverager.average();
                     long averageRoundTripTime = paintAverager.average();                     
                     long averageNetworkTime = networkTimeAverager.average();
                     long rowChangesSum = rowChangesAverager.sum();
                     long time = System.currentTimeMillis();
                     
                     socket.send("usedMemory:"+time+","+usedMemory);
                     socket.send("freeMemory:"+time+","+freeMemory);                    
                     
                     if(maximumPaintTime > 0) {
                        socket.send("maximumPaintTime:"+time+","+maximumPaintTime);
                     }
                     if(maximumRoundTripTime > 9) {
                        socket.send("maximumRoundTripTime:"+time+","+maximumRoundTripTime);
                     }
                     if(maximumNetworkTime > 9) {
                        socket.send("maximumNetworkTime:"+time+","+maximumNetworkTime);
                     }                     
                     socket.send("paintTime:"+time+","+averagePaintTime);
                     socket.send("roundTripTime:"+time+","+averageRoundTripTime);
                     socket.send("networkTime:"+time+","+averageNetworkTime);               
                     socket.send("rowsChanged:"+time+","+averageRowChanges);
                     socket.send("rowsChangedSum:"+time+","+rowChangesSum);                       
                     
                     networkTimeAverager.reset();
                     paintAverager.reset();
                     roundTripAverager.reset();
                     rowChangesAverager.reset();
                  }                          
               } catch (Exception e) {
                  sessions.remove(session);
                  LOG.info("Problem sending point", e);
               } 
            }
         }catch(Exception e) {
            e.printStackTrace();
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
