package org.simpleframework.demo.predicate;

public class Origin {

   private final String name;
   private final String host;
   private final int port;

   public Origin(String name) {
      this(name, null, 0);
   }

   public Origin(String name, String host, int port) {
      this.host = host;
      this.name = name;
      this.port = port;
   }

   public String getName() {
      return name;
   }

   public String getHost() {
      return host;
   }

   public int getPort() {
      return port;
   }

   @Override
   public String toString() {
      return String.format("origin '%s'", name);
   }
}
