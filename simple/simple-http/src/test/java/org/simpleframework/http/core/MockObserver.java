package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.transport.ByteWriter;


public class MockObserver implements BodyObserver {
   
   private boolean close;
   
   private boolean error;
   
   private boolean ready;
   
   private boolean commit;
   
   public MockObserver() {
      super();
   }
   
   public void close(ByteWriter sender) {
      close = true;
   }
   
   public boolean isClose() {
      return close;
   }
   
   public boolean isError() {
      return error;
   }
   
   public void ready(ByteWriter sender) {
      ready = true;
   }
   
   public boolean isReady() {
      return ready;
   }

   public void error(ByteWriter sender) {
      error = true;      
   }

   public boolean isClosed() {
      return close || error;
   }

   public long getTime() {
      return 0;
   }

   public void commit(ByteWriter sender) {
      this.commit = commit;
   }

   public boolean isCommitted() {
      return commit;
   }

}
