package org.simpleframework.transport;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

public class BufferTransportTest extends TestCase {
   
   public void testTransport() throws Exception {
      byte[] firstMessage = "this line should come first\n".getBytes();
      byte[] secondMessage = "this is the second line\n".getBytes();
      ByteBuffer firstBuffer = ByteBuffer.wrap(firstMessage);
      ByteArrayInputStream stream = new ByteArrayInputStream(secondMessage);
      StreamTransport streamTransport = new StreamTransport(stream, System.out);
      BufferTransport bufferTransport = new BufferTransport(streamTransport, firstBuffer, null);
      TransportCursor cursor = new TransportCursor(bufferTransport);
      byte[] readBuffer = new byte[1024];
      
      while(cursor.ready() != -1) {
         int count = cursor.read(readBuffer);
         System.out.write(readBuffer, 0, count);
      }
   }

}
