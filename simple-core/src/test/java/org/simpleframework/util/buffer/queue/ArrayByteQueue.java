package org.simpleframework.util.buffer.queue;

import java.io.IOException;

import org.simpleframework.util.buffer.BufferException;

public class ArrayByteQueue implements ByteQueue {

   private byte[] buffer;
   private int limit;
   private int count;
   private int seek;
   private boolean closed;

   public ArrayByteQueue(int limit) {
      this.buffer = new byte[16];
      this.limit = limit;
   }
   
   public synchronized void write(byte[] array) throws IOException {
      write(array, 0, array.length);
   }

   public synchronized void write(byte[] array, int off, int size) throws IOException {
      if(closed) {
         throw new BufferException("Queue has been closed");
      }
      if (size + count > buffer.length) {
         expand(count + size);
      }
      int fragment = buffer.length - seek; // from read pos to end
      int space = fragment - count; // space at end

      if(space >= size) {
         System.arraycopy(array, off, buffer, seek + count, size);         
      } else {
         int chunk = Math.min(fragment, count);
         
         System.arraycopy(buffer, seek, buffer, 0, chunk); // adjust downward         
         System.arraycopy(array, off, buffer, chunk, size);
         seek = 0;
      }
      notify();
      count += size;
   }
   
   public synchronized int read(byte[] array) throws IOException {
      return read(array, 0, array.length);
   }

   public synchronized int read(byte[] array, int off, int size) throws IOException {
      while(count == 0) {
         try {
            if(closed) {
               return -1;
            }
            wait();
         } catch(Exception e) {
            throw new BufferException("Thread interrupted", e);
         }
      }
      int chunk = Math.min(size, count);
      
      if(chunk > 0) {
         System.arraycopy(buffer, seek, array, off, chunk);
         seek += chunk;
         count -= chunk;
      }
      return chunk;
   }   
   
   private synchronized void expand(int capacity) throws IOException {
      if (capacity > limit) {
         throw new BufferException("Capacity limit %s exceeded", limit);
      }
      int resize = buffer.length * 2;
      int size = Math.max(capacity, resize);
      byte[] temp = new byte[size];

      System.arraycopy(buffer, seek, temp, 0, count);
      buffer = temp;
      seek = 0;
   }
   
   public synchronized void reset() throws IOException {
      if(closed) {
         throw new BufferException("Queue has been closed");
      }
      seek = 0;
      count = 0;
   }
   
   public synchronized int available() {
      return count;
   }
   
   public synchronized void close() {
      closed = true;
   }
}
