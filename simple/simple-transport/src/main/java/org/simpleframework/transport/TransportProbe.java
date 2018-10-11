package org.simpleframework.transport;

import static org.simpleframework.transport.TransportType.PLAIN;
import static org.simpleframework.transport.TransportType.SECURE;
import static org.simpleframework.transport.TransportType.UNKNOWN;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class TransportProbe {
   
   private AtomicReference<TransportType> reference;
   private byte[] data;
   private int count;

   public TransportProbe(boolean client) {
      this(client, 64);
   }
   
   public TransportProbe(boolean client, int capacity) {
      this.reference = new AtomicReference<TransportType>(client ? SECURE : UNKNOWN);
      this.data = new byte[capacity];
   }
   
   public TransportType update(ByteBuffer buffer) {
      TransportType type = reference.get();
      
      if(type == UNKNOWN) {
         int limit = buffer.limit();
         int position = buffer.position();
         int size = Math.min(limit, data.length - count);
         
         if(limit > 0) {
            buffer.get(data, count, size);
            buffer.position(position);
            count += size;
         }
         if(count > 5) {
            byte first = data[0];
            byte third = data[2];
            byte sixth = data[5];
            
            if((first & 0x80) == 0x80 && third == 0x01) { // SSL 2
               reference.set(SECURE);
            } else if(first == 0x16 && sixth == 0x01) { // SSL 3.0 or TLS 1.0, 1.1 and 1.2L
               reference.set(SECURE);
            } else {
               reference.set(PLAIN);
            }
         }
      }
      return reference.get();
   }
}
