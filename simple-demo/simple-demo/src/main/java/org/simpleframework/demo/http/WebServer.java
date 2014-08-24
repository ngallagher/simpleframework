package org.simpleframework.demo.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.net.ssl.SSLContext;

import org.simpleframework.demo.ssl.Certificate;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class WebServer {

   private final Certificate certificate;
   private final Connection connection;
   private final SocketAddress address;  
   private final SocketProcessor server;
   
   public WebServer(Container container, TraceAnalyzer analyzer, int port) throws IOException {
      this(container, null, analyzer, port, 10);
   }
   
   public WebServer(Container container,TraceAnalyzer analyzer, int port, int threads) throws IOException {
      this(container, null, analyzer, port, threads);
   }

   public WebServer(Container container, Certificate certificate, TraceAnalyzer analyzer, int port) throws IOException {
      this(container, certificate, analyzer, port, 10);
   }
   
   public WebServer(Container container, Certificate certificate, TraceAnalyzer analyzer, int port, int threads) throws IOException {
      this.server = new ContainerSocketProcessor(container, threads);
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
