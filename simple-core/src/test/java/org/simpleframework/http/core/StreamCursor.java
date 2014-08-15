package org.simpleframework.http.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.transport.Cursor;
import org.simpleframework.transport.StreamTransport;
import org.simpleframework.transport.Transport;
import org.simpleframework.transport.TransportCursor;

public class StreamCursor implements Cursor {

   private TransportCursor cursor;
   private Transport transport;
   private byte[] swap;
   
   public StreamCursor(String source) throws IOException {
      this(source.getBytes("UTF-8"));
   }
   
   public StreamCursor(byte[] data) throws IOException {
      this(new ByteArrayInputStream(data));
   }
   
   public StreamCursor(InputStream source) throws IOException {
      this.transport = new StreamTransport(source, new OutputStream() {
         public void write(int octet){}
      });
      this.cursor = new TransportCursor(transport);
      this.swap = new byte[1];
   }

   // TODO investigate this
   public boolean isOpen() throws IOException {
      return true;
   }
   
   public boolean isReady() throws IOException {
      return cursor.isReady();
   }
   
   public int ready() throws IOException {
      return cursor.ready();
   }
   
   public int read() throws IOException {
      if(read(swap) > 0) {
         return swap[0] & 0xff;
      }
      return 0;
   }

   public int read(byte[] data) throws IOException {
      return read(data, 0, data.length);
   }

   public int read(byte[] data, int off, int len) throws IOException {
      return cursor.read(data, off, len);
   }

   public int reset(int len) throws IOException {      
      return cursor.reset(len);
   }

   public void push(byte[] data) throws IOException {
      push(data, 0, data.length);
   }

   public void push(byte[] data, int off, int len) throws IOException {
      cursor.push(data, off, len);
   }
}