package org.simpleframework.demo.rest;

public interface MessageListener<T> {
   void onMessage(T message) throws Exception;
}
