package org.simpleframework.http.validate.test;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.StatusLine;

class Client implements Runnable {
   
   private final AtomicBoolean hasExecuted;
   private final HttpClient client;
   private final CountDownLatch latch;
   private final Scenario scenario;
   private final Analyser handler;
   private final StringBuilder target;
   private final KeyMap<String> header;
   private final Buffer body;
   private final int id;
   
   public Client(CountDownLatch latch, Analyser handler, Scenario scenario, StringBuilder target, KeyMap<String> header, Buffer body, int id) throws Exception {
      this.hasExecuted = new AtomicBoolean(false);
      this.client = new HttpClient();
      this.scenario = scenario;
      this.handler = handler;
      this.target = target;
      this.latch = latch;
      this.header = header;
      this.body = body;
      this.id = id;
   }
   
   public void run() {
      try {
         if(hasExecuted.get()) {
            throw new IllegalStateException("Client of id '" +id+ "' has already executed");
         }
         execute();
      } catch(Exception e) {
         e.printStackTrace();
      } finally {
         hasExecuted.set(true);
      }
   }
   
   private void execute() throws Exception {
      Method type = scenario.method();
      int requests = scenario.requests();

      for(int i = 0; i < requests; i++) {
         HttpMethod method = getMethod(type);
      
         try {
            method.addRequestHeader(RoundTripTest.REQUEST_ID, String.format("id-%s-%s", id, i));
            client.executeMethod(method);
            analyse(method);
         } catch(Throwable e) {
            e.printStackTrace();
         }finally {
            method.releaseConnection();
         }
      }
      latch.countDown();
   }
   
   private HttpMethod getMethod(Method method) throws Exception {   
      Protocol protocol = scenario.protocol();
      
      if(method == Method.GET) {
         return getGetMethod(protocol);
      }
      if(method == Method.POST) {
         return getPostMethod(protocol);
      }
      if(method == Method.HEAD) {
         return getHeadMethod(protocol);
      }
      return getGetMethod(protocol);
   }
   
   private HttpMethod getHeadMethod(Protocol protocol) throws Exception {
      HeadMethod head = new HeadMethod(protocol.getTarget());
      head.setPath(target.toString());
      
      for(String name : header) {
         head.addRequestHeader(name, header.get(name));
      }   
      return head;
   }
   
   private HttpMethod getGetMethod(Protocol protocol) throws Exception {
      GetMethod get = new GetMethod(protocol.getTarget());
      get.setPath(target.toString());
      
      for(String name : header) {
         get.addRequestHeader(name, header.get(name));
      }   
      return get;
   }
   
   private HttpMethod getPostMethod(Protocol protocol) throws Exception {   
      PostMethod post = new PostMethod(protocol.getTarget());
      post.setPath(target.toString());
      post.setUseExpectHeader(true);
      
      for(String name : header) {
         post.addRequestHeader(name, header.get(name));
      }
      InputStream requestBody = body.open();
      
      post.setRequestBody(requestBody);     
      return post;
   }
   
   private void analyse(HttpMethod method) throws Exception {
      KeyMap<String> message = new KeyMap<String>();
      StatusLine status = new ResponseStatus();
      int code = method.getStatusCode();
      String text = method.getStatusText();
       Header[] reply = method.getResponseHeaders();
       boolean debug = scenario.debug();
       
       for(Header header : reply) {
          String name = header.getName();
          String value = header.getValue();
          
          message.put(name, value);
       }
       InputStream responseStream = method.getResponseBodyAsStream();
       Buffer responseBody = new ArrayBuffer(1048576);
       byte[] chunk = new byte[1024];
       int count = 0;
       
       if(responseStream != null) {
          while((count = responseStream.read(chunk)) != -1) {
             responseBody.append(chunk, 0, count);
          }
       }
       if(debug) {
          System.err.println(responseBody.encode());
       }
       status.setCode(code);
       status.setDescription(text);
       status.setMajor(1);
       status.setMinor(1);
       
       handler.analyse(status, message, responseBody);
   }
}
