package org.simpleframework.transport;

import java.nio.ByteBuffer;

public class TransportSelector {
   
   private final Certificate certificate;
   private final TransportProbe probe;
   private final Transport transport;
      
   public TransportSelector(TransportProbe probe, Transport transport, Certificate certificate) {
      this.certificate = certificate;
      this.transport = transport;
      this.probe = probe;
   }

   public Transport select(ByteBuffer output, ByteBuffer input, String protocol) {
      TransportType type = probe.update(input);
   
      if(type == TransportType.SECURE) {
         return new SecureTransport(transport, certificate, output, input, protocol);
      }
      return new BufferTransport(transport, input, null);
   }
}
