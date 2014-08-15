package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.simpleframework.transport.Cursor;
import org.simpleframework.transport.Sender;
import org.simpleframework.util.buffer.ArrayBuffer;
import org.simpleframework.util.buffer.Buffer;

public class MockSender implements Sender {
   
   private Buffer buffer;
   
   public MockSender() {
      this(1024);
   }
   
   public MockSender(int size) {
      this.buffer = new ArrayBuffer(size);
   }
   
   public Buffer getBuffer() {
      return buffer;
   }
   
   public Cursor getCursor() throws IOException {
      return new StreamCursor(buffer.encode("UTF-8"));
   }
   
   public void send(byte[] array) throws IOException {
      buffer.append(array);
   }
   
   public void send(byte[] array, int off, int len) throws IOException {
      buffer.append(array, off, len);
   }
   
   public void flush() throws IOException {
      return;
   }
   
   public void close() throws IOException {
      return;
   }   
   
   public String toString() {
      return buffer.toString();
   }

   public boolean isOpen() throws Exception {
      return true;
   }

   public void send(ByteBuffer source) throws IOException {
      int mark = source.position();
      int limit = source.limit();
      
      byte[] array = new byte[limit - mark];
      source.get(array, 0, array.length);
      buffer.append(array);
   }

   public void send(ByteBuffer source, int off, int len) throws IOException {
      int mark = source.position();
      int limit = source.limit();
      
      if(limit - mark < len) {
         len = limit - mark;
      }
      byte[] array = new byte[len];
      source.get(array, 0, len);
      buffer.append(array);
   }
}
