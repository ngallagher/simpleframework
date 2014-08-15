package org.simpleframework.demo.http.resource;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public interface ResourceEngine {
   Resource resolve(Request request, Response response) throws Exception;
}
