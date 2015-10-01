package org.simpleframework.http.message;

import junit.framework.TestCase;

import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.ByteCursor;

public class RequestConsumerPerformanceTest extends TestCase {
   
   private static final int ITERATIONS = 1000000;
   
   private static final byte[] SOURCE_1 =
   ("POST /index.html HTTP/1.0\r\n"+
   "Content-Type: application/x-www-form-urlencoded\r\n"+
   "Content-Length: 42\r\n"+
   "Transfer-Encoding: chunked\r\n"+
   "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
   "   \t\t   image/png;\t\r\n\t"+
   "   q=1.0,*;q=0.1\r\n"+
   "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
   "Host:   some.host.com    \r\n"+
   "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
   "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
   "\r\n").getBytes();
   
   private static final byte[] SOURCE_2 =
   ("GET /tmp/amazon_files/21lP7I1XB5L.jpg HTTP/1.1\r\n"+
   "Accept-Encoding: gzip, deflate\r\n"+
   "Connection: keep-alive\r\n"+
   "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
   "Cache-Control: max-age=0\r\n"+      
   "Host: localhost:9090\r\n"+
   "Accept-Language: en-US\r\n"+
   "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
   "Accept: */*\r\n" +
   "\r\n").getBytes();
   
   private static final byte[] SOURCE_3 = 
   ("GET /tmp/amazon_files/in-your-city-blue-large._V256095983_.gif HTTP/1.1Accept-Encoding: gzip, deflate\r\n"+
   "Connection: keep-alive\r\n"+
   "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
   "Cache-Control: max-age=0\r\n"+
   "Host: localhost:9090\r\n"+
   "Accept-Language: en-US\r\n"+
   "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
   "Accept: */*\r\n"+
   "\r\n").getBytes();

   private static final byte[] SOURCE_4 = 
   ("GET /tmp/amazon_files/narrowtimer_transparent._V47062518_.gif HTTP/1.1\r\n"+
   "Accept-Encoding: gzip, deflate\r\n"+
   "Connection: keep-alive\r\n"+
   "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
   "Cache-Control: max-age=0\r\n"+
   "Host: localhost:9090\r\n"+
   "Accept-Language: en-US\r\n"+
   "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
   "Accept: */*\r\n"+
   "\r\n").getBytes();

   public void testPerformance() throws Exception {
      testPerformance(SOURCE_1, "/index.html");
      testPerformance(SOURCE_2, "/tmp/amazon_files/21lP7I1XB5L.jpg");
      testPerformance(SOURCE_3, "/tmp/amazon_files/in-your-city-blue-large._V256095983_.gif");
      testPerformance(SOURCE_4, "/tmp/amazon_files/narrowtimer_transparent._V47062518_.gif");
   }
   
   public void testPerformance(byte[] request, String path) throws Exception {
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < ITERATIONS; i++) {
         RequestConsumer header = new RequestConsumer();
         ByteCursor cursor = new StreamCursor(request);
      
         while(!header.isFinished()) {
            header.consume(cursor);
         }
         assertEquals(cursor.ready(), -1);
         assertEquals(header.getPath().getPath(), path);     
      }
      long finish = System.currentTimeMillis();
      long duration = finish - start;
      
      System.err.printf("time=%s performance=%s (per/ms) performance=%s (per/sec) path=%s%n", duration, ITERATIONS/duration, ITERATIONS/(duration/1000), path);
   }
}
