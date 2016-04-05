package org.simpleframework.demo.rest;

public interface RequestRouter {
   <T> void register(RequestHandler<T> handler, Class<T> type, String prefix);
}
