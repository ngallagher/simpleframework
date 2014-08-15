package org.simpleframework.http.message;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.simpleframework.http.core.Chunker;
import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.message.ChunkedConsumer;
import org.simpleframework.util.buffer.Allocator;
import org.simpleframework.util.buffer.ArrayAllocator;
import org.simpleframework.util.buffer.ArrayBuffer;
import org.simpleframework.util.buffer.Buffer;

public class ChunkedConsumerTest extends TestCase implements Allocator {
   
   public Buffer buffer;
   
   public void setUp() {
      buffer = new ArrayBuffer();
   }   
   
   public Buffer allocate() {
      return buffer;
   }
   
   public Buffer allocate(long size) {
      return buffer;
   }
	
	public void testChunks() throws Exception {
	   testChunks(64, 1024, 64);
	   testChunks(64, 11, 64);
	   testChunks(1024, 1024, 100000);
      testChunks(1024, 10, 100000);
      testChunks(1024, 11, 100000);
      testChunks(1024, 113, 100000);
      testChunks(1024, 1, 100000);
      testChunks(1024, 2, 50000);
      testChunks(1024, 3, 50000);
      testChunks(10, 1024, 50000);
      testChunks(1, 10, 71234);
      testChunks(2, 11, 123456);
      testChunks(15, 113, 25271);
      testChunks(16, 1, 43265);
      testChunks(64, 2, 63266);
      testChunks(32, 3, 9203);
	}
	
	public void testChunks(int chunkSize, int dribble, int entitySize) throws Exception {	   
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream plain = new ByteArrayOutputStream();
      Chunker encode = new Chunker(out);
      StringBuffer buf = new StringBuffer();
      int fill = 0;
      
      for(int i = 0, line = 0; i < entitySize; i++) {
         String text = "["+String.valueOf(i)+"]";
         
         if(fill >= chunkSize) {
            encode.write(buf.toString().getBytes("UTF-8"));
            plain.write(buf.toString().getBytes("UTF-8"));
            buf.setLength(0);
            fill = 0;            
            line = 0;
         }         
         line += text.length();
         fill += text.length();
         buf.append(text);
         
         if(line >= 48) {
            buf.append("\n");
            fill++;
            line = 0;
         }

      }
      if(buf.length() > 0) {
         encode.write(buf.toString().getBytes("UTF-8"));
         plain.write(buf.toString().getBytes("UTF-8")); 
      }
      buffer = new ArrayAllocator().allocate(); // N.B clear previous buffer
      encode.close();
      byte[] data = out.toByteArray();
      byte[] plainText = plain.toByteArray();
      //System.out.println(">>"+new String(data, 0, data.length, "UTF-8")+"<<");
      //System.out.println("}}"+new String(plainText, 0, plainText.length,"UTF-8")+"{{");
      DribbleCursor cursor = new DribbleCursor(new StreamCursor(new ByteArrayInputStream(data)), dribble);
      ChunkedConsumer test = new ChunkedConsumer(this);
      
      while(!test.isFinished()) {
         test.consume(cursor);
      }
      byte[] result = buffer.encode("UTF-8").getBytes("UTF-8");
      //System.out.println("))"+new String(result, 0, result.length, "UTF-8")+"((");
      
      if(result.length != plainText.length) {
         throw new IOException(String.format("Bad encoding result=[%s] plainText=[%s]", result.length, plainText.length));
      }
      for(int i = 0; i < result.length; i++) {
         if(result[i] != plainText[i]) {
            throw new IOException(String.format("Values do not match for %s, %s, and %s", chunkSize, dribble, entitySize));
         }
      }
	}

   public void close() throws IOException {
      // TODO Auto-generated method stub
      
   }
	

}
