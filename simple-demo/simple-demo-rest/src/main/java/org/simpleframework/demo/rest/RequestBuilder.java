package org.simpleframework.demo.rest;

import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY;
import static org.apache.http.params.CoreConnectionPNames.CONNECTION_TIMEOUT;
import static org.apache.http.params.CoreConnectionPNames.SO_TIMEOUT;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class RequestBuilder {

   private final AtomicReference<String> override;
   private final AtomicReference<String> body;
   private final Map<String, String> parameters;
   private final Map<String, String> headers;
   private final Map<String, String> cookies;
   private final ClientConnectionManager manager;
   private final ConnectionReuseStrategy strategy;
   private final ResponseConverter converter;
   private final AtomicBoolean post;
   private final DefaultHttpClient client;
   private final String address;
   private final URI template;

   public RequestBuilder(String address) throws Exception {
      this.parameters = new HashMap<String, String>();
      this.headers = new HashMap<String, String>();
      this.cookies = new HashMap<String, String>();
      this.override = new AtomicReference<String>();
      this.body = new AtomicReference<String>();
      this.manager = new BasicClientConnectionManager();
      this.strategy = new NoConnectionReuseStrategy();
      this.converter = new ResponseConverter();
      this.client = new DefaultHttpClient(manager);
      this.template = new URI(address);
      this.post = new AtomicBoolean();
      this.address = address;
   }

   public String getAddress() {
      return address;
   }
   
   public RequestBuilder setBody(String value) {
      if (body != null) {
         body.set(value);
      }
      return this;
   }

   public RequestBuilder setPath(String path) {
      if (path != null) {
         override.set(path);
      }
      return this;
   }

   public RequestBuilder addCookie(String name, String value) {
      if (value != null) {
         cookies.put(name, value);
      }
      return this;
   }

   public RequestBuilder addHeader(String name, String value) {
      if (value != null) {
         headers.put(name, value);
      }
      return this;
   }

   public RequestBuilder addParameter(String name, String value) {
      if (value != null) {
         parameters.put(name, value);
      }
      return this;
   }

   public RequestBuilder setMethod(String method) {
      if (method.equals(HttpPost.METHOD_NAME)) {
         post.set(true);
         return this;
      }
      if (method.equals(HttpGet.METHOD_NAME)) {
         post.set(false);
         return this;
      }
      throw new IllegalStateException("Method '" + method + "' is not supported");
   }

   public RequestBuilder setAuthorization(String name, String password) {
      Credentials credentials = createCredentials(name, password);
      CredentialsProvider provider = client.getCredentialsProvider();

      if (provider != null) {
         provider.setCredentials(AuthScope.ANY, credentials);
      }
      return this;
   }

   public RequestBuilder setConnectTimeout(int duration) throws Exception {
      HttpParams parameters = client.getParams();

      parameters.setParameter(CONNECTION_TIMEOUT, duration);

      return this;
   }

   public RequestBuilder setReadTimeout(int duration) throws Exception {
      HttpParams parameters = client.getParams();

      parameters.setParameter(SO_TIMEOUT, duration);

      return this;
   }

   public RequestBuilder setProxy(String address) throws Exception {
      HttpHost proxy = createHost(address);
      HttpParams parameters = client.getParams();

      parameters.setParameter(DEFAULT_PROXY, proxy);

      return this;
   }

   public <T> T execute(Class<T> type) throws Exception {
      try {
         if (post.get()) {
            return executePost(type);
         }
         return executeGet(type);
      } finally {
         manager.shutdown();
      }
   }

   private <T> T executeGet(Class<T> type) throws Exception {
      HttpResponse response = executeGet();

      if (converter.accept(type)) {
         return convertResponse(response, type);
      }
      return createResponse(response, type);
   }

   private <T> T executePost(Class<T> type) throws Exception {
      HttpResponse response = executePost();

      if (converter.accept(type)) {
         return convertResponse(response, type);
      }
      return createResponse(response, type);
   }

   private <T> T createResponse(HttpResponse response, Class type) throws Exception {
      HttpEntity entity = response.getEntity();

      if (type == HttpResponse.class) {
         return (T) response;
      }
      if (type == byte[].class) {
         return (T) EntityUtils.toByteArray(entity);
      }
      if (type == InputStream.class) {
         return (T) entity.getContent();
      }
      throw new IllegalStateException("Cannot convert response to " + type);
   }

   private <T> T convertResponse(HttpResponse response, Class type) throws Exception {
      HttpEntity entity = response.getEntity();
      String body = EntityUtils.toString(entity);

      if (body != null) {
         return (T) converter.convert(type, body);
      }
      return null;
   }

   private HttpResponse executePost() throws Exception {
      HttpPost message = createPost();
      HttpEntity entity = createEntity();
      URI target = createURI(message);

      client.setReuseStrategy(strategy);
      message.setURI(target);
      message.setEntity(entity);

      return client.execute(message);
   }

   private HttpResponse executeGet() throws Exception {
      HttpGet message = createGet();
      URI target = createURI(message);

      client.setReuseStrategy(strategy);
      message.setURI(target);

      return client.execute(message);
   }

   private HttpGet createGet() throws Exception {
      HttpGet get = new HttpGet(address);

      if (!headers.isEmpty()) {
         Set<String> names = headers.keySet();

         for (String name : names) {
            String value = headers.get(name);
            get.addHeader(name, value);
         }
      }
      if (!cookies.isEmpty()) {
         Set<String> names = cookies.keySet();

         for (String name : names) {
            String value = cookies.get(name);
            String cookie = String.format("%s=%s", name, value);
            
            get.addHeader("Cookie", cookie);
         }
      }
      return get;
   }

   private HttpPost createPost() throws Exception {
      HttpPost post = new HttpPost(address);

      if (!headers.isEmpty()) {
         Set<String> names = headers.keySet();

         for (String name : names) {
            String value = headers.get(name);
            post.addHeader(name, value);
         }
      }
      if (!cookies.isEmpty()) {
         Set<String> names = cookies.keySet();

         for (String name : names) {
            String value = cookies.get(name);
            String cookie = String.format("%s=%s", name, value);
            
            post.addHeader("Cookie", cookie);
         }
      }
      return post;
   }

   private HttpHost createHost(String address) throws Exception {
      URI target = URI.create(address);
      String scheme = target.getScheme();
      String host = target.getHost();
      int port = target.getPort();

      return new HttpHost(host, port, scheme);
   }

   private URI createURI(HttpPost message) throws Exception {
      URI target = message.getURI();
      String query = target.getQuery();

      return createURI(message, query);
   }

   private URI createURI(HttpGet message) throws Exception {
      String query = createQuery(message);

      return createURI(message, query);
   }

   private URI createURI(HttpUriRequest message, String query) throws Exception {
      URI target = message.getURI();
      String fragment = target.getFragment();
      String info = target.getUserInfo();
      String path = extractPath(target);
      String scheme = extractScheme(target);
      String host = extractHost(target);
      int port = extractPort(target);

      return new URI(scheme, info, host, port, path, query, fragment);
   }

   private String createQuery(HttpGet message) throws Exception {
      List<NameValuePair> parameters = createParameters();
      URI target = message.getURI();
      String query = target.getQuery();

      if (!parameters.isEmpty()) {
         if (query != null) {
            query += "&";
         } else {
            query = "";
         }
         for (NameValuePair parameter : parameters) {
            String name = parameter.getName();
            String value = parameter.getValue();

            query += String.format("%s=%s&", name, value);
         }
      }
      return query;
   }

   private String extractPath(URI target) throws Exception {
      String pathOverride = override.get();

      if (pathOverride == null) {
         String targetPath = target.getPath();

         if (targetPath != null) {
            return targetPath;
         }
         return template.getPath();
      }
      return pathOverride;
   }

   private int extractPort(URI target) throws Exception {
      String targetHost = target.getHost();
      int targetPort = target.getPort();

      if (targetHost != null) {
         return targetPort;
      }
      return template.getPort();
   }

   private String extractHost(URI target) throws Exception {
      String targetHost = target.getHost();

      if (targetHost != null) {
         return targetHost;
      }
      return template.getHost();
   }

   private String extractScheme(URI target) throws Exception {
      String targetScheme = target.getScheme();

      if (targetScheme != null) {
         return targetScheme;
      }
      return template.getScheme();
   }

   private HttpEntity createEntity() throws Exception {
      List<NameValuePair> parameters = createParameters();

      if(parameters.isEmpty()) {
         String content = body.get();
         return new StringEntity(content, "UTF-8");
      }
      return new UrlEncodedFormEntity(parameters, "UTF-8");
   }

   private List<NameValuePair> createParameters() throws Exception {
      List<NameValuePair> list = new LinkedList<NameValuePair>();

      if (!parameters.isEmpty()) {
         Set<String> names = parameters.keySet();

         for (String name : names) {
            String value = parameters.get(name);
            NameValuePair pair = createAttribute(name, value);

            list.add(pair);
         }
      }
      return list;
   }

   private NameValuePair createAttribute(String name, String value) {
      return new BasicNameValuePair(name, value);
   }

   private Credentials createCredentials(String user, String password) {
      return new UsernamePasswordCredentials(user, password);
   }
}
