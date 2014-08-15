package org.simpleframework.util.buffer.queue;

import java.io.IOException;

public interface ByteQueue {
   void write(byte[] array) throws IOException;
   void write(byte[] array, int off, int size) throws IOException;
   int read(byte[] array) throws IOException;
   int read(byte[] array, int off, int size) throws IOException;
   int available() throws IOException;
   void reset() throws IOException;
   void close() throws IOException;
}
