package org.simpleframework.transport.probe;

import java.nio.charset.StandardCharsets;

final class TextReader implements ProxyHeader {

   private static final byte[] TERMINAL = {'\r', '\n'};

   private final TransportAddress destination;
   private final TransportAddress source;
   private byte[] header;
   private int count;
   private int offset;

   public TextReader() {
      this.source = new TransportAddress();
      this.destination = new TransportAddress();
   }

   @Override
   public ProxyAddress getSource() {
      return source;
   }

   @Override
   public ProxyAddress getDestination() {
      return destination;
   }

   public int read(byte[] header, int start, int length) {
      if(start + length > header.length) {
         throw new IllegalStateException("Invalid proxy header");
      }
      this.header = header;
      this.offset = start;
      this.count = start + length;

      if(length < 5) {
         finish();
      } else {
         byte peek = header[offset++];

         if(peek == ' ') {
            parse();
         } else {
            finish();
         }
      }
      return offset - start;
   }

   private void parse() {
      ProtocolType protocol = protocol();

      if(protocol != ProtocolType.UNKNOWN) {
         address(source, protocol);
         address(destination, protocol);
         port(source);
         port(destination);
      }
      finish();
   }

   private void address(TransportAddress address, ProtocolType type) {
      if(offset + 1 < count) {
          byte peek = header[offset++];

          if(peek == ' ') {
             address.type = type;
             address.offset = offset;
             address.length = 0;

             while(offset < count) {
                peek = header[offset];

                if(peek == ' ' || peek == '\r' || peek == '\n') {
                  break;
                }
                address.length++;
                offset++;
             }
          }
      }
   }

   private void port(TransportAddress address) {
      if(offset + 1 < count) {
         byte peek = header[offset++];

         if(peek == ' ') {
            address.port = 0;

            while(offset < count) {
               peek = header[offset];

               if(peek >= '0' && peek <= '9') {
                  address.port *= 10;
                  address.port += (peek - '0');
               } else {
                  break;
               }
               offset++;
            }
         }
      }
   }

   private ProtocolType protocol() {
      if(offset + 3 < count) {
         byte hint = header[offset + 3];

         switch(hint) {
            case 0x34:
               offset += 4;
               return ProtocolType.TCP4;
            case 0x36:
               offset += 4;
               return ProtocolType.TCP6;
            case 0x4E:
               offset += 7;
               return ProtocolType.UNKNOWN;
         }
      }
      return ProtocolType.UNKNOWN;
   }

   private void finish() {
      int seek = 0;

      while(offset < count) {
         if(header[offset++] != TERMINAL[seek++]) {
            seek = 0;
         }
         if(seek == TERMINAL.length) {
            break;
         }
      }
   }

   private class TransportAddress implements ProxyAddress {

      private ProtocolType type;
      private String cache;
      private int length;
      private int offset;
      private int port;

      public TransportAddress() {
         this.type = ProtocolType.UNKNOWN;
      }

      @Override
      public int getPort() {
         return port;
      }

      @Override
      public ProtocolType getType() {
         return type;
      }

      @Override
      public String getAddress() {
         if(cache == null) {
            cache = new String(header, offset, length, StandardCharsets.UTF_8);
         }
         return cache;
      }

      @Override
      public String toString() {
         return getAddress();
      }
   }
}
