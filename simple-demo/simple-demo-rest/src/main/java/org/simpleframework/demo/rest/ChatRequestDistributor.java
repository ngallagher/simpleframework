package org.simpleframework.demo.rest;

import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;

import com.google.gson.Gson;

public class ChatRequestDistributor implements FrameListener {

   private final MessagePublisher publisher;
   private final Gson gson;
   
   public ChatRequestDistributor(MessagePublisher publisher) {
      this.gson = new Gson();
      this.publisher = publisher;
   }


   @Override
   public void onFrame(Session session, Frame frame) {
      String text = frame.getText();
      Object value = gson.fromJson(text, ChatRequest.class);
      
      try {
         publisher.publish(value);
      } catch(Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void onError(Session session, Exception cause) {
      cause.printStackTrace();
   }

   @Override
   public void onClose(Session session, Reason reason) {
      String message = reason.getText();
      System.err.println(message);
   }

}
