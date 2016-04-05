package org.simpleframework.demo.rest;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public interface RequestHandler<T> {
   void handle(Request request, Response response, T message) throws Exception;
}
