package org.simpleframework.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.trace.MockTrace;
import org.simpleframework.transport.trace.Trace;

public class StreamTransport implements Transport {
   
   private final WritableByteChannel write;
   private final ReadableByteChannel read;
   private final OutputStream out;
   
   public StreamTransport(InputStream in, OutputStream out) {
      this.write = Channels.newChannel(out);
      this.read = Channels.newChannel(in);
      this.out = out;
   }

   public void close() throws IOException {
      write.close();
      read.close();
   }

   public void flush() throws IOException {
      out.flush();
   }

   public int read(ByteBuffer buffer) throws IOException {
      return read.read(buffer);
   }

   public void write(ByteBuffer buffer) throws IOException {
      write.write(buffer);
   }

   public Map getAttributes() {
      return null;
   }

   public SocketChannel getChannel() {
      return null;
   }   

   public SSLEngine getEngine() {
      return null;
   }

   public Certificate getCertificate() {
      return null;
   }

   public Trace getTrace() {
      return new MockTrace();
   }
}
