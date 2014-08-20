package org.simpleframework.http.validate.test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public enum Protocol {
   HTTP("http", 9000),
   HTTPS("https", 9001);
   
   private final String scheme;
   private final int port;
   
   private Protocol(String scheme, int port) {
      this.scheme = scheme;
      this.port = port;
   }
   public String getScheme(){
      return scheme;
   }
   public SocketAddress getAddress() {
      return new InetSocketAddress(port);
   }
   public int getPort() {
      return port;
   }
   public String getTarget() {
      return scheme +"://localhost:"+port+"/";
   }
}
