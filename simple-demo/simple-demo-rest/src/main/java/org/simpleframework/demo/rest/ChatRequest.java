package org.simpleframework.demo.rest;

public class ChatRequest {

   private final String message;
   private final String user;
   private final String time;
   
   public ChatRequest(String message, String user, String time) {
      this.message = message;
      this.user = user;
      this.time = time;
   }

   public String getMessage() {
      return message;
   }

   public String getUser() {
      return user;
   }

   public String getTime() {
      return time;
   }
}
