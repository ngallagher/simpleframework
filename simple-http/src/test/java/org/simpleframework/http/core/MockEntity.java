
package org.simpleframework.http.core;

import org.simpleframework.http.message.Body;
import org.simpleframework.http.message.Entity;
import org.simpleframework.http.message.Header;
import org.simpleframework.transport.Channel;


public class MockEntity implements Entity {

  private Body body;
  private Header header;
   
  public MockEntity() {
    super();          
  }  
  
  public MockEntity(Body body) {
     this.body = body;
  }
  
  public MockEntity(Body body, Header header) {
     this.body = body;
     this.header = header;
  }
  
  public long getTime() {
     return 0;
  }

  public Body getBody() {
    return body;
  }

  public Header getHeader() {
    return header;
  }

  public Channel getChannel() {
    return null;
  }

  public void close() {}

  public long getStart() {
    return 0;
  }
}
