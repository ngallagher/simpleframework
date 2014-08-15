package org.simpleframework.demo.http.resource;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public interface Resource {
   void handle(Request request, Response response) throws Throwable;

}
