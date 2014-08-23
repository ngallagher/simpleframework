package org.simpleframework.http.socket;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.WebSocketCertificate.KeyStoreReader;
import org.simpleframework.http.socket.service.Service;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class WebSocketChatRoom extends Thread implements Service {
   
   private final WebSocketChatRoomListener listener;
   private final Map<String, FrameChannel> sockets;
   private final Set<String> users;
   
   public WebSocketChatRoom() {
      this.listener = new WebSocketChatRoomListener(this);
      this.sockets = new ConcurrentHashMap<String, FrameChannel>();
      this.users = new CopyOnWriteArraySet<String>();
   }  
  
   public void connect(Session connection) {
      FrameChannel socket = connection.getChannel();
      Request req = connection.getRequest();      
      Cookie user = req.getCookie("user");
      
      if(user == null) {
         user = new Cookie("user", "anonymous");
      }
      String name = user.getValue();
      
      try {
         socket.register(listener);
         join(name, socket);
      } catch(Exception e) {
         e.printStackTrace();
      }
      
   }
   
   public void join(String user, FrameChannel operation) {
      sockets.put(user, operation);
      users.add(user);
   }
   
   public void leave(String user, FrameChannel operation){
      sockets.put(user, operation);
      users.add(user);
   }
   
   public void distribute(Frame frame) {
      try {         
         for(String user : users) {
            FrameChannel operation = sockets.get(user);
            
            try {
               
               operation.send(frame);
            } catch(Exception e){   
               sockets.remove(user);
               users.remove(user);
               e.printStackTrace();
               operation.close();
            }
         }
      } catch(Exception e) {
         e.printStackTrace();
      }
   }
   
   public static void main(String[] list) throws Exception {
      TraceAnalyzer agent = new WebSocketAnalyzer();
      WebSocketChatRoom application = new WebSocketChatRoom();
      File file = new File("C:\\work\\development\\async_http\\proxy\\yieldbroker-proxy-site\\certificate\\www.yieldbroker.com.pfx");
      KeyStoreReader reader = new KeyStoreReader(WebSocketCertificate.KeyStoreType.PKCS12, file, "p", "p");
      WebSocketCertificate certificate = new WebSocketCertificate(reader, WebSocketCertificate.SecureProtocol.TLS);
      WebSocketChatApplication container = new WebSocketChatApplication(application, certificate, agent, 6060);
      application.start();
      container.connect();
   }
}
