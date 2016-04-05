package org.simpleframework.demo.rest;

public interface MessagePublisher {
   void publish(Object value) throws Exception;
}
