package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.transport.Channel;

public class MockController implements Controller {
   
   private boolean ready;
   private boolean sleep;
   private boolean start;
   private boolean initiated;
   private boolean stop;
   
   public void start(Channel channel) throws IOException {
      initiated = true;
   }

   public void ready(Collector collector) throws IOException {
      ready = true;
   }

   public void select(Collector collector) throws IOException {
      sleep = true;
   }  

   public void start(Collector collector) throws IOException {
      start = true;
   }
   
   public void stop() throws IOException {
      stop = true;
   }
   
   public boolean isStopped() {
      return stop;
   }
   
   public boolean isInitiated() {
      return initiated;
   }
   
   public boolean isReady() {
      return ready;
   }
   
   public boolean isSleep() {
      return sleep;
   }
   
   public boolean isStart() {
      return start;
   }
      
}
