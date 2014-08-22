package org.simpleframework.demo.http.socket;

import java.util.Map;

import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.service.Service;

public class ServiceRouter implements Service {
   
   private final Map<String, Service> services;
   
   public ServiceRouter(Map<String, Service> services) {
      this.services = services;
   }

   @Override
   public void connect(Session session) {
      try {
         Request request = session.getRequest();
         Path path = request.getPath();
         String normal = path.getPath();
         Service service = services.get(normal);
         FrameChannel socket = session.getChannel();
         
         if(service != null) {
            service.connect(session);
         } else {
            socket.close();
         }
      } catch(Exception e) {
         e.printStackTrace();
      }
   }

}
