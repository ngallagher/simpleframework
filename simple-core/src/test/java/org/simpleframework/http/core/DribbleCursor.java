package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.transport.Cursor;

public class DribbleCursor implements Cursor {
   
   private Cursor cursor;
   private byte[] swap;
   private int dribble;
   
   public DribbleCursor(Cursor cursor, int dribble) {
      this.cursor = cursor;
      this.dribble = dribble;
      this.swap = new byte[1];
   }

   public boolean isOpen() throws IOException {
      return true;
   }
   
   public boolean isReady() throws IOException {
      return cursor.isReady();
   }

   public int ready() throws IOException {
      int ready = cursor.ready();
      
      return Math.min(ready, dribble);
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
      int size = Math.min(len, dribble);
      
      return cursor.read(data, off, size); 
   }

   public int reset(int len) throws IOException {
      return cursor.reset(len);
   }

   public void push(byte[] data) throws IOException {
      cursor.push(data);
   }

   public void push(byte[] data, int off, int len) throws IOException {
      cursor.push(data, off, len);
   }
}
