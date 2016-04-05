package org.simpleframework.demo.rest;

import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.service.Service;

public class ChatRequestHandler implements Service { // forwards on the chat messages from the WebSocket!!
   
   private final ChatRequestDistributor distributor;
   
   public ChatRequestHandler(MessagePublisher publisher) {
      this.distributor = new ChatRequestDistributor(publisher);
   }

   @Override
   public void connect(Session connection) {
      FrameChannel socket = connection.getChannel();     
      
      try {
         socket.register(distributor);
      } catch(Exception e) {
         e.printStackTrace();
      }
   }

}