package org.simpleframework.http.validate.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.simpleframework.http.ResponseWrapper;
import org.simpleframework.http.Response;

class Interceptor extends ResponseWrapper {

   private OutputStreamCapture out;

   public Interceptor(Response resp) throws Exception {
      super(resp);
      this.out = new OutputStreamCapture(resp);
   }

   public byte[] getBody() {
      return out.getCapture();
   }

   // Get the full response message 
   public String getResponse() throws IOException {
      return response.toString();
   }

   public OutputStream getOutputStream() {
      return out;
   }

   public OutputStream getOutputStream(int size) {
      return out;
   }

   public PrintStream getPrintStream() {
      return new PrintStream(out, false);
   }

   public PrintStream getPrintStream(int size) {
      return new PrintStream(out, false);
   }

   public void reset() throws IOException {
      out.reset();
      response.reset();
   }

   private class OutputStreamCapture extends OutputStream {

      private ByteArrayOutputStream body;
      private OutputStream real;

      public OutputStreamCapture(Response resp) throws Exception {
         this.real = resp.getOutputStream();
         this.body = new ByteArrayOutputStream();
      }

      public void reset() {
         body.reset();
      }

      public byte[] getCapture() {
         return body.toByteArray();
      }

      public void write(int octet) throws IOException {
         body.write(octet);
         real.write(octet);
      }

      public void write(byte[] data, int off, int len) throws IOException {
         body.write(data, off, len);
         real.write(data, off, len);
      }

      public void flush() throws IOException {
         real.flush();
      }

      public void close() throws IOException {
         real.close();
      }
   }
}
