package org.simpleframework.transport.probe;

public interface ProxyHeader {
   ProxyAddress getSource();
   ProxyAddress getDestination();
}
