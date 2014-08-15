package org.simpleframework.demo.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class WebServer {

   private final Connection connection;
   private final SocketAddress address;
   private final Server server;

   public WebServer(Container container, int port) throws IOException {
      this.server = new ContainerServer(container, 2);
      this.connection = new SocketConnection(server);
      this.address = new InetSocketAddress(port);
   }

   public void start() throws IOException {
      connection.connect(address);
   }

   public void stop() throws IOException {
      connection.close();
   }
}
