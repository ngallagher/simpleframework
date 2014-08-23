package org.simpleframework.http.validate.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;

class Runner {
   private final SecurityManager manager;
   private final Analyser handler;
   private final StringBuilder target;
   private final KeyMap<String> header;
   private final Adapter server;
   private final Scenario scenario;
   private final Buffer body;
   
   public Runner(Analyser handler, Scenario scenario) throws Exception {
      this.server = new Adapter(handler, scenario);
      this.manager = new SecurityManager();
      this.target = new StringBuilder();
      this.header = new KeyMap<String>();
      this.body = new ArrayBuffer(1048576);
      this.scenario = scenario;
      this.handler = handler;
   }
   
   public int getRequests() {
      return server.getRequests();
   }
   
   public int getFailures() {
      return server.getFailures();
   }
   
   public void start() throws Exception {
      Method method = scenario.method();
      
      if(method == Method.GET && body.open().available() > 0) {
         throw new IllegalStateException("Get methods can not contain a body");
      }
      server.start();
      handler.compose(target, header, body);
   }
   
   public void execute() throws Exception {
      Protocol protocol = scenario.protocol();
      int concurrency = scenario.concurrency();
      String scheme = protocol.getScheme();
      int port = protocol.getPort();
      
      if(protocol == Protocol.HTTPS) {
         manager.registerProtocol(scheme, port);
      }
      execute(concurrency);
   }
   
   private void execute(int concurrency) throws Exception {
      CountDownLatch latch = new CountDownLatch(concurrency);
      ExecutorService executor = Executors.newFixedThreadPool(concurrency);
      
      for(int i = 0; i < concurrency; i++) {
         Client task = new Client(latch, handler, scenario, target, header, body, i);
         executor.execute(task);
      }
      for(int i = 0; i < 15; i++) {
         boolean done = latch.await(60000, TimeUnit.MILLISECONDS);
         
         if(done) {
            break;
         }
         System.err.println("Waiting for client to finish remaining=["+latch.getCount()+"] required=["+concurrency+"]");
      }
      executor.shutdown();
   }
   
   public void stop() throws Exception {
      server.stop();
   }

}
