package org.simpleframework.demo.table.message;

import java.util.Arrays;
import java.util.List;

import org.simpleframework.demo.table.extract.Client;
import org.simpleframework.demo.table.telemetry.StatisticsService;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class TableService implements Service {
   
   private final StatisticsService service;
   private final List<TableUpdater> updaters;
   
   public TableService(List<TableUpdater> updaters, StatisticsService service) {
      this.updaters = updaters;
      this.service = service;
   }

   @Override
   public void connect(Session session) {
      WebSocket socket = session.getSocket();
      Request request = session.getRequest();
      String user = request.getParameter("user");
      String company = request.getParameter("company");
      
      if(user != null && company != null) {
         Client client = new Client(user, company, Arrays.asList("DB", "ANZ", "HSBC", "GS", "JPM", "UBS"));
         
         for(TableUpdater updater : updaters) {
            TableListener callbacks = new TableListener(updater, service);
            try {
               socket.register(callbacks);
               updater.subscribe(socket, client);
            }catch(Exception e) {
               e.printStackTrace();
            }
         }      
      }
   }

}
