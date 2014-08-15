package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.transport.Sender;


public class MockObserver implements Observer {
   
   private boolean close;
   
   private boolean error;
   
   private boolean ready;
   
   private boolean commit;
   
   public MockObserver() {
      super();
   }
   
   public void close(Sender sender) {
      close = true;
   }
   
   public boolean isClose() {
      return close;
   }
   
   public boolean isError() {
      return error;
   }
   
   public void ready(Sender sender) {
      ready = true;
   }
   
   public boolean isReady() {
      return ready;
   }

   public void error(Sender sender) {
      error = true;      
   }

   public boolean isClosed() {
      return close || error;
   }

   public long getTime() {
      return 0;
   }

   public void commit(Sender sender) {
      this.commit = commit;
   }

   public boolean isCommitted() {
      return commit;
   }

}
