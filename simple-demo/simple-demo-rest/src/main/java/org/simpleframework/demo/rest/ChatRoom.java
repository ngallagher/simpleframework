package org.simpleframework.demo.rest;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.http.socket.service.Service;

public class ChatRoom implements Service {
   
   private static final Logger LOG = Logger.getLogger(ChatRoom.class);
   
   private final ChatRoomListener listener;
   private final Map<String, FrameChannel> sockets;
   private final Set<String> users;
   
   public ChatRoom() {
      this.listener = new ChatRoomListener(this);
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
         LOG.info("Problem joining chat room", e);
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
   
   public void distribute(String from, Frame frame) {
      try {          
         for(String user : users) {
            FrameChannel operation = sockets.get(user);
            
            try {               
               if(!from.equals(user)) {
                  operation.send(frame);
               }
            } catch(Exception e){   
               sockets.remove(user);
               users.remove(user);
               operation.close();
               LOG.info("Problem sending message", e);
            }
         }
      } catch(Exception e) {
         LOG.info("Problem distributing message", e);
      }
   }
}
