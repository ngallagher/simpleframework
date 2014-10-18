package org.simpleframework.http.socket.service;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.simpleframework.http.core.MockRequest;
import org.simpleframework.http.core.MockResponse;
import org.simpleframework.http.socket.Session;

public class PathRouterTest extends TestCase {
   
   public static class A implements Service {
      public void connect(Session session) {}
   }
   
   public static class B implements Service {
      public void connect(Session session) {}
   }
   
   public static class C implements Service {
      public void connect(Session session) {}
   }
   
   public void testRouter() throws Exception{
      Map<String, Service> services = new HashMap<String, Service>();
      
      services.put("/a", new A());
      services.put("/b", new B());
      
      PathRouter router = new PathRouter(services, new C());
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      
      request.setTarget("/a");
      
      Service service = router.route(request, response);
      
      assertNull(service);
      
      request.setValue("Sec-WebSocket-Version", "13");
      request.setValue("connection", "upgrade");
      request.setValue("upgrade", "WebSocket");
      
      service = router.route(request, response);
      
      assertNotNull(service);
      assertEquals(service.getClass(), A.class);
      assertEquals(response.getValue("Sec-WebSocket-Version"), "13");
      
      request.setTarget("/c");
      
      service = router.route(request, response);
      
      assertNotNull(service);
      assertEquals(service.getClass(), C.class);
      assertEquals(response.getValue("Sec-WebSocket-Version"), "13");
      
   }

}
