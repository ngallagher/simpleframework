package org.simpleframework.transport.probe;

import static org.simpleframework.transport.probe.TransportType.PLAIN;
import static org.simpleframework.transport.probe.TransportType.SECURE;
import static org.simpleframework.transport.probe.TransportType.UNKNOWN;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class TransportProbe {

   private static final byte[] PROXY_V1 = {0x50, 0x52, 0x4F, 0x58, 0x59};
   private static final byte[] PROXY_V2 = {0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D, 0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A};
   private static final int MIN_LENGTH = 5; // enough for 'PROXY' or 'GET /' or SSLv3

   private final AtomicReference<TransportType> reference;
   private final ProxyHeaderReader reader;
   private byte[] data;
   private int count;

   public TransportProbe(boolean client) {
      this(client, 107);
   }

   public TransportProbe(boolean client, int capacity) {
      this.reference = new AtomicReference<>(client ? SECURE : UNKNOWN);
      this.reader = new ProxyHeaderReader();
      this.data = new byte[capacity];
   }

   public ProxyHeader getHeader() {
      return reader.getHeader();
   }

   public TransportType probe(ByteBuffer buffer) {
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
         if(count >= MIN_LENGTH) {
            ProxyVersion version = version(data, 0, count);
            int offset = 0;

            if(version == ProxyVersion.UNKNOWN) {
               return reference.get();
            } else if(version == ProxyVersion.V1) {
               offset = PROXY_V1.length + reader.read(version, data, 0 + PROXY_V1.length, count);
            } else if(version == ProxyVersion.V2) {
               offset = PROXY_V2.length + reader.read(version, data, 0 + PROXY_V2.length, count);
            }
            if(count >= offset + MIN_LENGTH) {
               byte first = data[offset + 0];
               byte third = data[offset + 2];
               byte sixth = data[offset + 5];

               if((first & 0x80) == 0x80 && third == 0x01) { // SSL 2
                  reference.set(SECURE);
               } else if(first == 0x16 && sixth == 0x01) { // SSL 3.0 or TLS 1.0, 1.1 and 1.2
                  reference.set(SECURE);
               } else {
                  reference.set(PLAIN);
               }
               if(offset > 0) {
                  buffer.position(position + offset); // how much should you skip
               }
            }
         }
      }
      return reference.get();
   }

   private ProxyVersion version(byte[] header, int offset, int length) {
      if(length >= PROXY_V1.length) {
         int seek = 0;

         while(seek < header.length) {
            if(header[offset + seek] != PROXY_V1[seek++]) {
               seek = 0;
               break;
            }
            if(seek == PROXY_V1.length) {
               return ProxyVersion.V1;
            }
         }
         if(length >= PROXY_V2.length) {
            while(seek < header.length) {
               if(header[offset + seek] != PROXY_V2[seek++]) {
                  return ProxyVersion.NONE;
               }
               if(seek == PROXY_V2.length) {
                  return ProxyVersion.V2;
               }
            }
            return ProxyVersion.UNKNOWN;
         }
         return ProxyVersion.NONE; // if we don't get a full v2 header we reject
      }
      return ProxyVersion.UNKNOWN;
   }
}