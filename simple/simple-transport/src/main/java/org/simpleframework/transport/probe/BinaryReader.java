package org.simpleframework.transport.probe;

final class BinaryReader implements ProxyHeader {

   private static final int COMMAND = 0; // 13th
   private static final int PROTOCOL = 1; // 14th
   private static final int LENGTH_HIGH = 2; // 15th
   private static final int LENGTH_LOW = 3; // 16th

   private final TransportAddress destination;
   private final TransportAddress source;
   private byte[] header;
   private int count;
   private int offset;

   public BinaryReader() {
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
      if(length < LENGTH_LOW) {
         throw new IllegalStateException("Invalid proxy header");
      }
      if(start + length > header.length) {
         throw new IllegalStateException("Invalid proxy header");
      }
      byte command = header[start + COMMAND];
      byte high = header[start + LENGTH_HIGH];
      byte low = header[start + LENGTH_LOW];
      int octets = ((high << 8) | low) + 4;

      if(length < octets) {
         throw new IllegalStateException("Invalid proxy header");
      }
      this.header = header;
      this.offset = start;
      this.count = start + length;

      if(command == 0x21) {
         parse();
      }
      return octets;
   }

   private void parse() {
      ProtocolType protocol = protocol();

      offset += 4;
      source.type = protocol;
      destination.type = protocol;

      switch(protocol) {
         case TCP4:
         case UDP4:
            source.address = offset;
            destination.address = offset + 4;
            source.port = offset + 4 + 4;
            destination.port = offset + 4 + 4 + 2;
            break;
         case TCP6:
         case UDP6:
            source.address = offset;
            destination.address = offset + 16;
            source.port = offset + 16 + 16;
            destination.port = offset + 16 + 16 + 2;
            break;
      }
   }

   private ProtocolType protocol() {
      byte hint = header[offset + PROTOCOL];

      switch(hint) {
         case 0x00:
            return ProtocolType.UNKNOWN;
         case 0x11:
            return ProtocolType.TCP4;
         case 0x21:
            return ProtocolType.TCP6;
         case 0x12:
            return ProtocolType.UDP4;
         case 0x22:
            return ProtocolType.UDP6;
      }
      return ProtocolType.UNKNOWN;
   }

   private class TransportAddress implements ProxyAddress {

      private ProtocolType type;
      private String cache;
      private int port;
      private int address;

      public TransportAddress() {
         this.type = ProtocolType.UNKNOWN;
      }

      @Override
      public int getPort() {
         return (header[port] << 8) | header[port + 1];
      }

      @Override
      public ProtocolType getType() {
         return type;
      }

      @Override
      public String getAddress() {
         if(cache == null) {
            switch(type) {
               case TCP6:
               case UDP6:
                  return cache = new StringBuilder(type.length)
                       .append(hexadecimal(address))
                       .append(hexadecimal(address + 1))
                       .append(":")
                       .append(hexadecimal(address + 2))
                       .append(hexadecimal(address + 3))
                       .append(":")
                       .append(hexadecimal(address + 4))
                       .append(hexadecimal(address + 5))
                       .append(":")
                       .append(hexadecimal(address + 6))
                       .append(hexadecimal(address + 7))
                       .append(":")
                       .append(hexadecimal(address + 8))
                       .append(hexadecimal(address + 9))
                       .append(":")
                       .append(hexadecimal(address + 10))
                       .append(hexadecimal(address + 11))
                       .append(":")
                       .append(hexadecimal(address + 12))
                       .append(hexadecimal(address + 13))
                       .append(":")
                       .append(hexadecimal(address + 14))
                       .append(hexadecimal(address + 15))
                       .toString();
               case TCP4:
               case UDP4:
                  return cache = new StringBuilder(type.length)
                       .append(header[address])
                       .append(".")
                       .append(header[address + 1])
                       .append(".")
                       .append(header[address + 2])
                       .append(".")
                       .append(header[address + 3])
                       .toString();
            }
         }
         return cache;
      }

      private char hexadecimal(int offset) {
         byte nibble = header[offset];

         if (nibble <= 9) {
            return (char)('0' + nibble);
         } else {
            return (char)('a' + (nibble - 10));
         }
      }

      @Override
      public String toString() {
         return getAddress();
      }
   }
}
