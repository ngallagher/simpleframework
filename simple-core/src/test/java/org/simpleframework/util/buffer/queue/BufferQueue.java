package org.simpleframework.util.buffer.queue;

import java.io.IOException;
import java.io.InputStream;

import org.simpleframework.util.buffer.ArrayBuffer;
import org.simpleframework.util.buffer.Buffer;

public class BufferQueue implements Buffer {
   
   private final ByteQueue queue;
   private final Buffer buffer;
   
   public BufferQueue(ByteQueue queue) {
      this.buffer = new ArrayBuffer();
      this.queue = queue;
   }

   public InputStream open() throws IOException {
      return new ByteQueueStream(queue);
   }

   public Buffer allocate() throws IOException {     
      return new BufferQueue(queue);
   }

   public String encode() throws IOException {
      return encode("UTF-8");
   }

   public String encode(String charset) throws IOException {
      InputStream source = open();      
      byte[] chunk = new byte[512];      
      int count = 0;
      
      while((count = source.read(chunk)) != -1) {
         buffer.append(chunk, 0, count);
      }
      return buffer.encode(charset);
   }

   public Buffer append(byte[] array) throws IOException {
      if(array.length > 0) {
         queue.write(array);
      }
      return this;
   }

   public Buffer append(byte[] array, int off, int len) throws IOException {
      if(len > 0) {
         queue.write(array, off, len);
      }
      return this;
   }

   public void clear() throws IOException {
      queue.reset();
   }

   public void close() throws IOException {
      queue.close();
   }
   
   public long length() {
      return buffer.length();
   }
}
