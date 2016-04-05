package org.simpleframework.demo.rest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.http.Path;
import org.simpleframework.http.Protocol;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.google.gson.Gson;

public class RequestProcessor  {
   
   private final Map<String, RequestRegistration> registrations;
   private final SocketProcessor processor;
   private final SocketAddress address;
   private final Container container;
   private final Connection connection;
   private final Gson gson;
   
   public RequestProcessor(int port) throws Exception {
      this.registrations = new ConcurrentHashMap<String, RequestRegistration>();
      this.address = new InetSocketAddress(port);
      this.container = new RequestRouter();
      this.processor = new ContainerSocketProcessor(container);
      this.connection = new SocketConnection(processor);
      this.gson = new Gson();
   }
   
   public void register(RequestRegistration registration) throws Exception {
      String prefix = registration.getPrefix();
      RequestRegistration previous = registrations.put(prefix, registration);
      
      if(previous != null) {
         throw new IllegalArgumentException("Prefix '" + prefix + "' has already been used");
      }
   }
   
   public void start() throws Exception {
      connection.connect(address);
   }
   
   public void stop() throws Exception {
      connection.close();
   }
   
   private class RequestRouter implements Container {

      
      @Override
      public void handle(Request request, Response response) {
         try {
            Path path = request.getPath();
            String normal = path.getPath();
            RequestRegistration registration = registrations.get(normal);
            RequestHandler handler = registration.getHandler();
            Class type = registration.getType();
            String content = request.getContent();
            Object message = gson.fromJson(content, type);
            long time = System.currentTimeMillis();
            
            response.setDate(Protocol.DATE, time);
            handler.handle(request, response, message);
            response.close();
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
      
   }

}
