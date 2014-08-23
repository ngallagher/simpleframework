/*
 * ContinueDispatcher.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.simpleframework.http.message;

import static org.simpleframework.http.core.ContainerEvent.DISPATCH_CONTINUE;

import java.io.IOException;

import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>ContinueDispatcher</code> object is used to send the HTTP
 * 100 continue status if required. This is delivered to the client 
 * to tell the client that the server is willing to accept the request 
 * body. Once this is sent the transport will likely wait until there 
 * is a read ready event.
 * 
 * @author Niall Gallagher
 */
class ContinueDispatcher {

   /**
    * This is the status code that is sent to prompt the client. 
    */
   private static final byte[] STATUS = { 'H', 'T','T', 'P', '/','1','.', '1',' ', '1','0','0',' '};
   
   /**
    * This is the optional description for the expect status code.
    */
   private static final byte[] MESSAGE = {'C','o','n','t','i','n','u','e', '\r','\n','\r','\n'};
   
   /**
    * This is the writer that is used to deliver the continue.
    */
   private final ByteWriter writer;
   
   /**
    * This is the trace used to capture a continue response if any.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>ContinueDispatcher</code> object. This 
    * will create an object that will deliver the continue status code.
    * Because the transport performs an asynchronous write this will
    * not block the execution of this method and delay execution.
    * 
    * @param channel this is the channel used to deliver the prompt
    */
   public ContinueDispatcher(Channel channel) {
      this.writer = channel.getWriter();
      this.trace = channel.getTrace();
   }
   
   /**
    * This will execute the continue if the header contains the 
    * expectation header. If there is no expectation then this will
    * return without sending anything back to the connected client.
    * 
    * @param header this is the header read from the channel
    */
   public void execute(Header header) throws IOException {
      if(header.isExpectContinue()) {
         trace.trace(DISPATCH_CONTINUE);
         writer.write(STATUS);
         writer.write(MESSAGE);
         writer.flush();
      }
   }
}
