package org.simpleframework.http.validate.test;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

class Adapter implements Container {
   
   private final Analyser handler;
   private final Scenario scenario;
   private final Connection connection;
   private final SocketProcessor server;
   private final SecurityManager manager;
   private final AtomicInteger requests;
   private final AtomicInteger failures;
   private final Set<String> requestIds;
   private final boolean debug;
   
   public Adapter(Analyser handler, Scenario scenario) throws Exception {
      this.requestIds = Collections.synchronizedSet(new HashSet<String>());
      this.server = new ContainerSocketProcessor(this, 10);
      this.connection = new SocketConnection(server);
      this.manager = new SecurityManager();
      this.requests = new AtomicInteger();
      this.failures = new AtomicInteger();
      this.debug = scenario.debug();
      this.handler = handler;
      this.scenario = scenario;
   }
   
   public int getRequests() {
      return requests.get();
   }
   
   public int getFailures() {
      return failures.get();
   }
   
   public void start() {
      try{
         Protocol protocol = scenario.protocol();
         SocketAddress address = protocol.getAddress();
         
         if(protocol == Protocol.HTTP) {
            connection.connect(address);
         }else {
            connection.connect(address, manager.getContext());
         }
      }catch(Exception e) {
         e.printStackTrace();
      }
   } 

   public void handle(Request req, Response resp) {
      requests.getAndIncrement();
      
      try {
         String requestId = req.getValue(RoundTripTest.REQUEST_ID);
         
         if(requestId == null || requestId.equals("")) {
            throw new IllegalStateException("The request id was not set for "+ requests.get());
         }
         if(!requestIds.add(requestId)) {
            throw new IllegalStateException("The request id '" + requestId + "' has already been set " + requests.get());
         }
         if(debug) {
            System.err.println(Encoder.encode(req));
         }
         handler.handle(req, resp);
      } catch(Throwable e) {
         failures.getAndIncrement();
         e.printStackTrace();
      }finally {
         try {
            resp.close();
            
            if(debug) {
               System.err.println(Encoder.encode(resp));
            }
         }catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public void stop() {
      try {
         connection.close();
         server.stop();
      }catch(Exception e) {
         e.printStackTrace();
      }
   }

}
