package org.simpleframework.demo.rest;

import org.apache.log4j.Logger;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;

public class ChatRoomListener implements FrameListener {
   
   private static final Logger LOG = Logger.getLogger(ChatRoomListener.class);   
   
   private final ChatRoom room;
   
   public ChatRoomListener(ChatRoom room) {
      this.room = room;
   }

   public void onFrame(Session socket, Frame frame) {
      FrameType type = frame.getType();
      String text = frame.getText();
      Request request = socket.getRequest();
      Cookie user = request.getCookie("user");
      String name = user.getValue();
      
      if(type == FrameType.TEXT){
         Frame replay = new DataFrame(type, "(" + name + ") " +text);
         
         room.distribute(name, replay);
      } 
      LOG.info("onFrame(" + type + ")");
   }

   public void onError(Session socket, Exception cause) {
      LOG.info("onError(" + cause + ")", cause);
   }

   public void onClose(Session session, Reason reason) {
      LOG.info("onClose(" + reason + ")");
   }
}
