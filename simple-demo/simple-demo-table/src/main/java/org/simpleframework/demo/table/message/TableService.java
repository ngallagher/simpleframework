package org.simpleframework.demo.table.message;

import java.util.Arrays;

import org.simpleframework.demo.table.extract.Client;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class TableService implements Service {
   
   private final TableUpdater updater;
   
   public TableService(TableUpdater updater) {
      this.updater = updater;
   }

   @Override
   public void connect(Session session) {
      WebSocket socket = session.getSocket();
      Client client = new Client("john@hsbc.com", "HSBC", Arrays.asList("DB", "ANZ", "HSBC", "GS", "JPM", "UBS"));
      
      updater.subscribe(socket, client);
      
   }

}
