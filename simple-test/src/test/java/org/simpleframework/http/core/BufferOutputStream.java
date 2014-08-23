package org.simpleframework.http.core;

import java.io.IOException;
import java.io.OutputStream;

import org.simpleframework.common.buffer.Buffer;

public class BufferOutputStream extends OutputStream {

   private final Buffer buffer;
   
   public BufferOutputStream(Buffer buffer) {
      this.buffer = buffer;
   }
   
   @Override
   public void write(int b) throws IOException {
      write(new byte[]{(byte)b});
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      buffer.append(b, off, len);
   }
   
   @Override
   public void close() throws IOException {
      buffer.close();
   }
}
