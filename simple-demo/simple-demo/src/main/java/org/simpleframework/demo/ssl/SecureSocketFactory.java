package org.simpleframework.demo.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

public class SecureSocketFactory extends SSLSocketFactory {

   private final SSLSocketFactory factory;

   public SecureSocketFactory(SecureSocketContext context) throws Exception {
      this.factory = context.getSocketFactory();
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
   public Socket createSocket(String host, int port) throws IOException {
      return factory.createSocket(host, port);
   }

   @Override
   public Socket createSocket(InetAddress host, int port) throws IOException {
      return factory.createSocket(host, port);
   }

   @Override
   public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
      return factory.createSocket(host, port, localHost, localPort);
   }

   @Override
   public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
      return factory.createSocket(address, port, localAddress, localPort);
   }

   @Override
   public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
      return factory.createSocket(socket, host, port, autoClose);
   }
}
