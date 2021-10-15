package org.simpleframework.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.trace.Trace;

public class BufferTransport implements Transport {

   private final NetworkAddress address;
   private final Transport transport;
   private final ByteBuffer extra;
   private final SSLEngine engine;
   private final byte[] data;

   public BufferTransport(Transport transport, NetworkAddress address, ByteBuffer extra, SSLEngine engine) {
      this(transport, address, extra, engine, 512);
   }
   
   public BufferTransport(Transport transport, NetworkAddress address, ByteBuffer extra, SSLEngine engine, int size) {
      this.data = new byte[size];
      this.transport = transport;
      this.address = address;
      this.engine = engine;
      this.extra = extra;
   }
  
   @Override
   public String getProtocol() {
      return transport.getProtocol();
   }

   @Override
   public Trace getTrace() {
      return transport.getTrace();
   }

   @Override
   public SSLEngine getEngine() {
      return engine;
   }

   @Override
   public SocketChannel getChannel() {
      return transport.getChannel();
   }

   @Override
   public Map getAttributes() {
      return transport.getAttributes();
   }

   @Override
   public NetworkAddress getAddress() throws IOException {
      return address;
   }

   @Override
   public Certificate getCertificate() throws IOException {
      return transport.getCertificate();
   }

   @Override
   public int read(ByteBuffer buffer) throws IOException {
      int available = extra.remaining();
      
      if(available > 0) {
         int limit = buffer.limit();
         int position = extra.position();
         int ready = Math.min(available, data.length);
         int size = Math.min(limit, ready);
         
         extra.get(data, 0, size);
         extra.position(position + size);
         buffer.put(data, 0, size);
         
         return size;
      }
      return transport.read(buffer);
   }

   @Override
   public void write(ByteBuffer buffer) throws IOException {
      transport.write(buffer);
   }

   @Override
   public void flush() throws IOException {
      transport.flush();
   }

   @Override
   public void close() throws IOException {
      transport.close();
   }

}
