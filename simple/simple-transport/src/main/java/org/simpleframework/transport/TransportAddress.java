package org.simpleframework.transport;

import java.nio.channels.SocketChannel;

import org.simpleframework.transport.probe.ProxyHeader;
import org.simpleframework.transport.probe.TransportProbe;

final class TransportAddress implements NetworkAddress {

   private final TransportProbe probe;
   private final Transport transport;

   public TransportAddress(TransportProbe probe, Transport transport) {
      this.transport = transport;
      this.probe = probe;
   }

   @Override
   public int getPort() {
      ProxyHeader header = probe.getHeader();
      SocketChannel channel = transport.getChannel();

      if(header != null) {
         return header.getSource().getPort();
      }
      if(channel != null) {
         return channel.socket().getPort();
      }
      return -1;
   }

   @Override
   public String getAddress() {
      ProxyHeader header = probe.getHeader();
      SocketChannel channel = transport.getChannel();

      if(header != null) {
         return header.getSource().getAddress();
      }
      if(channel != null) {
         return channel.socket().getInetAddress().getHostAddress();
      }
      return "0.0.0.0";
   }

   @Override
   public String toString() {
      return getAddress();
   }
}
