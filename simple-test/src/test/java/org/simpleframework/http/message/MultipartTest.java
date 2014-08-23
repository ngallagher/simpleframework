package org.simpleframework.http.message;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.Part;
import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;

public class MultipartTest extends TestCase {
   
   public void testMiltipart() throws Exception {
      for(int i = 1; i < 40; i++) {  
         List<String> list = new ArrayList<String>();
         
         for(int j = 1; j < i + 1; j++) {
            list.add("This is another file to be added "+i);
         }
         for(int j = 1; j < 512; j++) {
            testMultipart(list, j);
         }
         System.err.println("Multipart chunks: " + i);
         		
      }
   }
   
   public void testMultipart(List<String> partList, int dribble) throws Exception {
      //System.err.println("dribble: "+dribble);
      MimeMultipart multipart = new MimeMultipart();
      int index = 0;
      
      for(String part : partList) {
         MimeBodyPart bodyPart = new MimeBodyPart();
         
         bodyPart.addHeader("Content-Type", "text/plain");
         bodyPart.addHeader("Content-Disposition", "form-data; name='file"+index+".txt'; filename='C:\\Inetpub\\wwwroot\\Upload\\file"+index+".txt'");
         bodyPart.setText(part);
         multipart.addBodyPart(bodyPart);
      }
      ByteArrayOutputStream result = new ByteArrayOutputStream();

      multipart.writeTo(result);
      
      String text = result.toString();
      String line = text.split("\\s+")[0];
      String boundary = line.replaceAll("^--", "");
      
      assertTrue(text.startsWith(line));
      
      Allocator allocator = new ArrayAllocator();
      PartSeriesConsumer consumer = new PartSeriesConsumer(allocator, boundary.getBytes(), result.toByteArray().length);
      StreamCursor stream = new StreamCursor(text);
      DribbleCursor cursor = new DribbleCursor(stream, dribble);
      
      while(!consumer.isFinished()) {
         consumer.consume(cursor);
      }
      List<Part> list = consumer.getBody().getParts();
      
      for(int i = 0; i < list.size(); i++) {
         String content = list.get(i).getContent();
         String body = partList.get(i);
         
         assertEquals(content, body);
      }
    }

}
