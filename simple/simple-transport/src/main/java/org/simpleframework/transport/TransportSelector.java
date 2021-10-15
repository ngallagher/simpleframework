package org.simpleframework.transport;

import java.nio.ByteBuffer;

import org.simpleframework.transport.probe.TransportProbe;
import org.simpleframework.transport.probe.TransportType;

public class TransportSelector {

   private final TransportAddress address;
   private final Certificate certificate;
   private final TransportProbe probe;
   private final Transport transport;
      
   public TransportSelector(TransportProbe probe, Transport transport, Certificate certificate) {
      this.address = new TransportAddress(probe, transport);
      this.certificate = certificate;
      this.transport = transport;
      this.probe = probe;
   }

   public Transport select(ByteBuffer output, ByteBuffer input, String protocol) {
      TransportType type = probe.probe(input);
   
      if(type == TransportType.SECURE) {
         return new SecureTransport(transport, address, certificate, output, input, protocol);
      }
      return new BufferTransport(transport, address, input, null);
   }
}
