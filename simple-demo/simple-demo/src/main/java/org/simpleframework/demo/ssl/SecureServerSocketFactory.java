package org.simpleframework.demo.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

public class SecureServerSocketFactory extends SSLServerSocketFactory {

   private final SSLServerSocketFactory factory;

   public SecureServerSocketFactory(SecureSocketContext context) throws Exception {
      this.factory = context.getServerSocketFactory();
   }

   @Override
   public String[] getDefaultCipherSuites() {
      return factory.getDefaultCipherSuites();
   }

   @Override
   public String[] getSupportedCipherSuites() {
      return factory.getSupportedCipherSuites();
   }

   @Override
   public ServerSocket createServerSocket(int port) throws IOException {
      return factory.createServerSocket(port);
   }

   @Override
   public ServerSocket createServerSocket(int port, int backlog) throws IOException {
      return factory.createServerSocket(port, backlog);
   }

   @Override
   public ServerSocket createServerSocket(int port, int backlog, InetAddress address) throws IOException {
      return factory.createServerSocket(port, backlog, address);
   }
}
