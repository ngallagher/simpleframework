package org.simpleframework.demo.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.net.ssl.SSLContext;

import org.simpleframework.demo.ssl.Certificate;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.Analyzer;

public class WebServer {

   private final Certificate certificate;
   private final Connection connection;
   private final SocketAddress address;  
   private final Server server;

   public WebServer(Container container, Certificate certificate, Analyzer analyzer, int port) throws IOException {
      this.server = new ContainerServer(container, 2);
      this.connection = new SocketConnection(server, analyzer);
      this.address = new InetSocketAddress(port);
      this.certificate = certificate;
   }

   public void start() throws IOException {
      if(certificate != null) {
         SSLContext context = certificate.getContext();
         connection.connect(address, context);
      } else {
         connection.connect(address);
      }
   }

   public void stop() throws IOException {
      connection.close();
   }
}
