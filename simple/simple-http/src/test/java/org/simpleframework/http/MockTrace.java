package org.simpleframework.http;

import org.simpleframework.transport.trace.Trace;

public class MockTrace implements Trace{
   public void trace(Object event) {}
   public void trace(Object event, Object value) {}
}
