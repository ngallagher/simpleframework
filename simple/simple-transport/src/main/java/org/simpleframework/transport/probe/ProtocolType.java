package org.simpleframework.transport.probe;

public enum ProtocolType {
   UNKNOWN(0x00, 0),
   TCP4(0x11, 16),
   TCP6(0x21, 40),
   UDP4(0x12, 16),
   UDP6(0x22, 40);

   public final byte header;
   public final int length;

   private ProtocolType(int header, int length) {
      this.header = (byte)header;
      this.length = length;
   }
}
