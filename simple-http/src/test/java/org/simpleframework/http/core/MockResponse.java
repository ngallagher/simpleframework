package org.simpleframework.http.core;

import static org.simpleframework.http.Protocol.CLOSE;
import static org.simpleframework.http.Protocol.CONNECTION;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

import org.simpleframework.http.Response;
import org.simpleframework.http.core.ResponseMessage;

public class MockResponse extends ResponseMessage implements Response {

   private boolean committed;
   
   public MockResponse() {
      super();
   }
   
   public OutputStream getOutputStream() {
      return System.out;
   }
   
   public boolean isKeepAlive() {
      String value = getValue(CONNECTION);
      
      if(value != null) {
         return value.equalsIgnoreCase(CLOSE);
      }
      return true;
   }
   
   public boolean isCommitted() {
      return committed;
   }
   
   public void commit() {
      committed = true;
   }
   
   public void reset() {
      return;
   }
   
   public void close() {
      return;
   }

   public Object getAttribute(String name) {
      return null;
   }

   public Map getAttributes() {
      return null;
   }

   public OutputStream getOutputStream(int size) throws IOException {
      return null;
   }

   public PrintStream getPrintStream() throws IOException {
      return null;
   }

   public PrintStream getPrintStream(int size) throws IOException {
      return null;
   }

   public void setContentLength(long length) {
      setValue("Content-Length", String.valueOf(length));
   }

   public WritableByteChannel getByteChannel() throws IOException {
      return null;
   }

   public WritableByteChannel getByteChannel(int size) throws IOException {
      return null;
   }

   public boolean isEmpty() {
      return false;
   }

   public long getResponseTime() {
      return 0;
   }

   public void setContentType(String type) {
      setValue("Content-Type", type);
   }
}
