package org.simpleframework.transport.probe;

import org.simpleframework.transport.NetworkAddress;

public interface ProxyAddress extends NetworkAddress {
   ProtocolType getType();
}
