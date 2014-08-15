package org.simpleframework.http.core;

public class Ticket {
   
   public static final Class KEY = Ticket.class;
  
   private final String ticket;
   private final int port;
   public Ticket(int port) {
      this.ticket = String.valueOf(port);
      this.port = port;
   }   
   
   public int getPort() {
      return port;
   }
   
   public String getTicket() {
      return ticket;
   }

}
