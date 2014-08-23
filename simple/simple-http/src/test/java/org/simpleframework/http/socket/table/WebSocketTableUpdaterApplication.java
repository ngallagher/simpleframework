package org.simpleframework.http.socket.table;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerTransportConnector;
import org.simpleframework.http.socket.service.Router;
import org.simpleframework.http.socket.service.RouterContainer;
import org.simpleframework.http.socket.service.SingletonRouter;
import org.simpleframework.transport.TransportConnector;
import org.simpleframework.transport.TransportSocketConnector;
import org.simpleframework.transport.SocketConnector;
import org.simpleframework.transport.Transport;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class WebSocketTableUpdaterApplication implements Container, TransportConnector {   

   //private final String ROOT_PATH = "/Users/niallg/Work/development/simpleframework/simple/src/test/java/org/simpleframework/http/socket/";
   private final String ROOT_PATH = "C:\\Work\\development\\simpleframework\\simple\\src\\test\\java\\org\\simpleframework\\http\\socket\\table\\";
   //private final String ROOT_PATH = "C:\\Work\\development\\async_http\\simple\\src\\test\\java\\org\\simpleframework\\http\\socket\\table\\";   
   
   private final Router negotiator;
   private final RouterContainer container;
   private final SocketAddress address;
   private final Connection connection;
   private final TransportConnector processor;
   private final Allocator allocator;
   private final SocketConnector server;
   
   public WebSocketTableUpdaterApplication(WebSocketTableUpdater handler, TraceAnalyzer agent, int port) throws Exception {
      this.negotiator = new SingletonRouter(handler);
      this.container = new RouterContainer(this, negotiator, 10);
      this.allocator = new ArrayAllocator();
      this.processor = new ContainerTransportConnector(container, allocator, 1);
      this.server = new TransportSocketConnector(this);
      this.connection = new SocketConnection(server, agent);
      this.address = new InetSocketAddress(port);
   }
   
   public void connect() throws IOException {
      connection.connect(address);
   }

   public void handle(Request req, Response resp) {
      Path path = req.getPath();
      String normal = path.getPath();
      
      System.err.println(req);

      if(req.getTarget().equals("/login")) {
         String user = req.getParameter("user");
         long time = System.currentTimeMillis();
         
         try {
            resp.setStatus(Status.FOUND);
            resp.setValue("Location", "/table");
            resp.setCookie("user", user);
            resp.setDate("Date", time);
            resp.setValue("Server", "WebSocketTableApplication/1.0");
            resp.setContentType("text/html");
            resp.close();
         }catch(Exception e) {
            e.printStackTrace();
         }      
      } else if(req.getTarget().equals("/update")){         
         long time = System.currentTimeMillis();
         try {
            container.handle(req, resp);
         } catch(Exception e) {
            e.printStackTrace();
         }
      } else {
         long time = System.currentTimeMillis();
         
         try {
            byte[] page = loadPage(normal);
            
            resp.setDate("Date", time);
            resp.setValue("Server", "WebSocketTableApplication/1.0");
            
            if(normal.endsWith(".html")) {
               resp.setContentType("text/html");
            } else if(normal.endsWith(".css")) {
               resp.setContentType("text/css");
            } else if(normal.endsWith(".js")) {
               resp.setContentType("text/javascript");
            } else if(normal.endsWith(".png")) {
               resp.setContentType("image/png");           
            } else {
               resp.setContentType("text/plain");
            }            
            OutputStream out = resp.getOutputStream();
            out.write(page);
            out.close();
         }catch(Exception e) {
            e.printStackTrace();
            
            try {               
               resp.setCode(404);
               resp.setDescription("Not Found");
               resp.setDate("Date", time);
               resp.setValue("Server", "WebSocketTableApplication/1.0");
               resp.setContentType("text/plain");
               
               PrintStream out = resp.getPrintStream();
               
               e.printStackTrace(out);
               out.close();
            } catch(Exception ex) {
               ex.printStackTrace();
            }
         }
         
      }
   }
   
   public byte[] loadPage(String name) throws IOException {
      InputStream loginPage = new FileInputStream(new File(ROOT_PATH, name));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] chunk = new byte[1024];
      int count = 0;
      
      while((count = loginPage.read(chunk)) != -1) {
         out.write(chunk, 0, count);
      }            
      out.close();
      return out.toByteArray();
   }

   public void connect(Transport transport) throws IOException {
      Map map = transport.getAttributes();
      map.put(Transport.class, transport);
      processor.connect(transport);
   }

   public void stop() throws IOException {
      processor.stop();
   }      
}  
