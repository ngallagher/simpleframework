package org.simpleframework.http.validate;

import junit.framework.TestCase;

import org.simpleframework.xml.core.Persister;

public class RequestTaskTest extends TestCase {
   
   private static final String SOURCE = 
   "<request method='GET' target='http://www.google.com/'>\r\n" +
   "   <header name='Content-Type'>text/plain</header>\r\n"+
   "   <header name='User-Agent'>Mozilla/1.1</header>\r\n"+
   "   <header name='Content-Length'>10000</header>\r\n"+
   "   <body>0123456789</body>\r\n"+
   "</request>";
   
   private static final String HEADER = 
   "GET http://www.google.com/ HTTP/1.1\r\n"+
   "Content-Type: text/plain\r\n"+
   "User-Agent: Mozilla/1.1\r\n"+
   "Content-Length: 10\r\n"+
   "Connection: keep-alive\r\n"+
   "\r\n"+
   "0123456789";
   
   private static final String PIPELINE = 
   "GET http://www.google.com/ HTTP/1.1\r\n"+
   "Content-Type: text/plain\r\n"+
   "User-Agent: Mozilla/1.1\r\n"+
   "Content-Length: 10\r\n"+
   "Sequence: 1\r\n"+
   "Connection: keep-alive\r\n"+
   "\r\n"+
   "0123456789"+
   "GET http://www.google.com/ HTTP/1.1\r\n"+
   "Content-Type: text/plain\r\n"+
   "User-Agent: Mozilla/1.1\r\n"+
   "Content-Length: 10\r\n"+
   "Sequence: 2\r\n"+
   "Connection: close\r\n"+
   "\r\n"+
   "0123456789";

   public void testRequestTask() throws Exception {
      Persister persister = new Persister();
      RequestTask task = persister.read(RequestTask.class, SOURCE);
      byte[] request = task.getRequest();
      byte[] pipeline = task.getRequest(2);
      
      assertEquals(Method.GET, task.getMethod());
      assertEquals("http://www.google.com/", task.getTarget());
      assertEquals(HEADER, new String(request, "ISO-8859-1"));
      assertEquals(PIPELINE, new String(pipeline, "ISO-8859-1"));     
   }
}
