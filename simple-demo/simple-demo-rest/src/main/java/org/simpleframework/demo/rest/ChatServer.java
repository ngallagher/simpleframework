package org.simpleframework.demo.rest;

public class ChatServer {
   
   public static final int PORT = 8991;

   public static void main(String[] list) throws Exception {
      MessageServer server = new MessageServer();
      MessagePublisher publisher = server.create(PORT);
      ChatRequestDistributor distributor = new ChatRequestDistributor(publisher);
      
   }
}
