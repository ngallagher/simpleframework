package org.simpleframework.transport.probe;

final class ProxyHeaderReader {

   private final BinaryReader binary;
   private final TextReader text;
   private ProxyHeader header;

   public ProxyHeaderReader() {
      this.binary = new BinaryReader();
      this.text = new TextReader();
   }

   public ProxyHeader getHeader() {
      return header;
   }

   public int read(ProxyVersion version, byte[] data, int start, int length) {
      int offset = 0;

      if(version == ProxyVersion.V1) {
         offset = text.read(data, start, length);
         header = text;
      } else if(version == ProxyVersion.V2) {
         offset = binary.read(data, start, length);
         header = binary;
      }
      return offset;
   }
}
