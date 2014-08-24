/*
 * Handshake.java February 2007
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

package org.simpleframework.transport;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static org.simpleframework.transport.PhaseType.COMMIT;
import static org.simpleframework.transport.PhaseType.CONSUME;
import static org.simpleframework.transport.PhaseType.PRODUCE;
import static org.simpleframework.transport.TransportEvent.ERROR;
import static org.simpleframework.transport.TransportEvent.HANDSHAKE_BEGIN;
import static org.simpleframework.transport.TransportEvent.HANDSHAKE_DONE;
import static org.simpleframework.transport.TransportEvent.HANDSHAKE_FAILED;
import static org.simpleframework.transport.TransportEvent.READ;
import static org.simpleframework.transport.TransportEvent.WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>Handshake</code> object is used to perform secure SSL
 * negotiations on a pipeline or <code>Transport</code>. This can
 * be used to perform an SSL handshake. To perform the negotiation 
 * this uses an SSL engine provided with the transport to direct 
 * the conversation. The SSL engine tells the negotiation what is
 * expected next, whether this is a response to the client or a 
 * message from it. During the negotiation this may need to wait 
 * for either a write ready event or a read ready event. Event 
 * notification is done using the processor provided.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.transport.TransportProcessor
 */
class Handshake implements Negotiation {   
   
   /**
    * This is the processor used to process the secure transport.
    */
   private final TransportProcessor processor;   
   
   /**
    * This is the certificate associated with this negotiation.
    */
   private final NegotiationState state;
   
   /**
    * This is the socket channel used to read and write data to.
    */
   private final SocketChannel channel;
   
   /**
    * This is the transport dispatched when the negotiation ends.
    */
   private final Transport transport;   
   
   /**
    * This is the reactor used to register for I/O notifications.
    */
   private final Reactor reactor;   
   
   /**
    * This is the output buffer used to generate data to.
    */
   private final ByteBuffer output;
   
   /**
    * This is the input buffer used to read data from the socket.
    */
   private final ByteBuffer input;
   
   /**
    * This is an empty byte buffer used to generate a response.
    */
   private final ByteBuffer empty;
   
   /**
    * This is the SSL engine used to direct the conversation.
    */
   private final SSLEngine engine;
   
   /**
    * This is the trace that is used to monitor handshake events.
    */
   private final Trace trace;
   
   /**
    * This determines if the handshake is from the client side.
    */
   private final boolean client;
   
   /**
    * Constructor for the <code>Handshake</code> object. This is
    * used to create an operation capable of performing negotiations
    * for SSL connections. Typically this is used to perform request
    * response negotiations, such as a handshake or termination.
    *
    * @param processor the processor used to dispatch the transport
    * @param transport the transport to perform the negotiation for     
    * @param reactor this is the reactor used for I/O notifications      
    */
   public Handshake(TransportProcessor processor, Transport transport, Reactor reactor) {
      this(processor, transport, reactor, 20480);           
   }
  
   /**
    * Constructor for the <code>Handshake</code> object. This is
    * used to create an operation capable of performing negotiations
    * for SSL connections. Typically this is used to perform request
    * response negotiations, such as a handshake or termination.
    *
    * @param transport the transport to perform the negotiation for
    * @param processor the processor used to dispatch the transport
    * @param reactor this is the reactor used for I/O notifications     
    * @param size the size of the buffers used for the negotiation
    */
   public Handshake(TransportProcessor processor, Transport transport, Reactor reactor, int size) {
      this(processor, transport, reactor, size, false);
   }
   
   /**
    * Constructor for the <code>Handshake</code> object. This is
    * used to create an operation capable of performing negotiations
    * for SSL connections. Typically this is used to perform request
    * response negotiations, such as a handshake or termination.
    *
    * @param transport the transport to perform the negotiation for
    * @param processor the processor used to dispatch the transport
    * @param reactor this is the reactor used for I/O notifications     
    * @param client determines the side of the SSL handshake
    */
   public Handshake(TransportProcessor processor, Transport transport, Reactor reactor, boolean client) {
      this(processor, transport, reactor, 20480, client);
   }
   
   /**
    * Constructor for the <code>Handshake</code> object. This is
    * used to create an operation capable of performing negotiations
    * for SSL connections. Typically this is used to perform request
    * response negotiations, such as a handshake or termination.
    *
    * @param transport the transport to perform the negotiation for
    * @param processor the processor used to dispatch the transport
    * @param reactor this is the reactor used for I/O notifications  
    * @param size the size of the buffers used for the negotiation
    * @param client determines the side of the SSL handshake
    */
   public Handshake(TransportProcessor processor, Transport transport, Reactor reactor, int size, boolean client) {
      this.state = new NegotiationState(this, transport);
      this.output = ByteBuffer.allocate(size);
      this.input = ByteBuffer.allocate(size);
      this.channel = transport.getChannel();   
      this.engine = transport.getEngine();
      this.trace = transport.getTrace();
      this.empty = ByteBuffer.allocate(0);
      this.processor = processor;
      this.transport = transport;
      this.reactor = reactor;
      this.client = client;
   }
   
   /**
    * This is used to acquire the trace object that is associated
    * with the operation. A trace object is used to collection details
    * on what operations are being performed. For instance it may 
    * contain information relating to I/O events or errors. 
    * 
    * @return this returns the trace associated with this operation
    */  
   public Trace getTrace() {
      return trace;
   }   
   
   /**
    * This returns the socket channel for the connected pipeline. It
    * is this channel that is used to determine if there are bytes
    * that can be read. When closed this is no longer selectable.
    *
    * @return this returns the connected channel for the pipeline
    */
   public SelectableChannel getChannel() {
      return channel;
   }   
   
   /**
    * This is used to start the negotiation. Once started this will
    * send a message to the other side, once sent the negotiation 
    * reads the response. However if the response is not yet ready 
    * this will schedule the negotiation for a selectable operation 
    * ensuring that it can resume execution when ready.
    */
   public void run() {      
      if(engine != null) {
         trace.trace(HANDSHAKE_BEGIN);
         engine.setUseClientMode(client);         
         input.flip();
      }         
      begin();    
   }

   /**
    * This is used to terminate the negotiation. This is excecuted
    * when the negotiation times out. When the negotiation expires it
    * is rejected by the processor and must be canceled. Canceling
    * is basically termination of the connection to free resources.
    */
   public void cancel() {
      try {
         terminate();
      } catch(Exception cause) { 
         trace.trace(ERROR, cause);
      }
   }
   
   /**
    * This is used to start the negotation. Once started this will
    * send a message to the other side, once sent the negotiation 
    * reads the response. However if the response is not yet ready 
    * this will schedule the negotiation for a selectable operation 
    * ensuring that it can resume execution when ready.
    */
   private void begin() {
      try {
         resume();
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         cancel();
      }
   }
   
   /**
    * This is the main point of execution within the negotiation. It
    * is where the negotiation is performed. Negotiations are done
    * by performing a request response flow, governed by the SSL
    * engine associated with the pipeline. Typically the client is
    * the one to initiate the handshake and the server initiates the
    * termination sequence. This may be executed several times 
    * depending on whether reading or writing blocks.
    */
   public void resume() throws IOException {
      Runnable task = process();

      if(task != null) {
         task.run();
      }
   }
   
   /**
    * This is the main point of execution within the negotiation. It
    * is where the negotiation is performed. Negotiations are done
    * by performing a request response flow, governed by the SSL
    * engine associated with the transport. Typically the client is
    * the one to initiate the handshake and the server initiates the
    * termination sequence. This may be executed several times 
    * depending on whether reading or writing blocks.
    * 
    * @return this returns a task used to execute the next phase
    */
   private Runnable process() throws IOException {
      PhaseType require = exchange();
      
      if(require == CONSUME) {
         return new Consumer(this, reactor, trace);
      } 
      if(require == PRODUCE) {
         return new Producer(this, reactor, trace);
      } 
      return new Committer(this, reactor, trace);
   }
   
   /**
    * This is the main point of execution within the negotiation. It
    * is where the negotiation is performed. Negotiations are done
    * by performing a request response flow, governed by the SSL
    * engine associated with the transport. Typically the client is
    * the one to initiate the handshake and the server initiates the
    * termination sequence. This may be executed several times 
    * depending on whether reading or writing blocks.
    * 
    * @return this returns what is expected next in the negotiation
    */
   private PhaseType exchange() throws IOException {
      HandshakeStatus status = engine.getHandshakeStatus();
      
      switch(status){
      case NEED_WRAP:
         return write();
      case NOT_HANDSHAKING:
      case NEED_UNWRAP:
         return read();
      }      
      return COMMIT;
   }
   
   /**
    * This is used to perform the read part of the negotiation. The
    * read part is where the other side sends information where it
    * is consumed and is used to determine what action to take. 
    * Typically it is the SSL engine that determines what action is
    * to be taken depending on the data send from the other side.
    *
    * @return the next action that should be taken by the handshake
    */
   private PhaseType read() throws IOException {
      return read(5);
   }

   /**
    * This is used to perform the read part of the negotiation. The
    * read part is where the other side sends information where it
    * is consumed and is used to determine what action to take. 
    * Typically it is the SSL engine that determines what action is
    * to be taken depending on the data send from the other side.
    *
    * @param count this is the number of times a read can repeat
    *
    * @return the next action that should be taken by the handshake
    */
   private PhaseType read(int count) throws IOException {
      while(count > 0) {
         SSLEngineResult result = engine.unwrap(input, output); 
         HandshakeStatus status = result.getHandshakeStatus();

         switch(status) {
         case NOT_HANDSHAKING:
            return COMMIT;
         case NEED_WRAP:
            return PRODUCE;
         case FINISHED:
         case NEED_UNWRAP:
            return read(count-1);
         case NEED_TASK:
            execute(); 
         }      
      }
      return CONSUME;
   }
   
   /**
    * This is used to perform the write part of the negotiation. The
    * read part is where the this sends information to the other side
    * and the other side interprets the data and determines what action 
    * to take. After a write the negotiation typically completes or
    * waits for the next response from the other side.
    *
    * @return the next action that should be taken by the handshake
    */
   private PhaseType write() throws IOException {   
      return write(5);
   }
   
   /**
    * This is used to perform the write part of the negotiation. The
    * read part is where the this sends information to the other side
    * and the other side interprets the data and determines what action 
    * to take. After a write the negotiation typically completes or
    * waits for the next response from the other side.
    *
    * @param count this is the number of times a read can repeat
    *
    * @return the next action that should be taken by the handshake
    */
   private PhaseType write(int count) throws IOException {
      while(count > 0) {
         SSLEngineResult result = engine.wrap(empty, output);
         HandshakeStatus status = result.getHandshakeStatus();

         switch(status) {
         case NOT_HANDSHAKING:
         case FINISHED:
         case NEED_UNWRAP:
            return PRODUCE;
         case NEED_WRAP:
            return write(count-1);
         case NEED_TASK:
            execute();
         }
      }
      return PRODUCE;
   }  
   
   /**
    * This is used to execute the delegated tasks. These tasks are
    * used to digest the information received from the client in
    * order to generate a response. This may need to execute several
    * tasks from the associated SSL engine.
    */
   private void execute() throws IOException {
      while(true) {
         Runnable task = engine.getDelegatedTask();

         if(task == null) {
            break;
         }
         task.run();
      }
   }
   
   /**
    * This is used to receive data from the client. If at any
    * point during the negotiation a message is required that
    * can not be read immediately this is used to asynchronously
    * read the data when a select operation is signalled.
    *  
    * @return this returns true when the message has been read
    */
   public boolean receive() throws IOException {
      int count = input.capacity();
      
      if(count > 0) {
         input.compact();
      }
      int size = channel.read(input); 

      if(trace != null) {
        trace.trace(READ, size);
      }      
      if(size < 0) {
         throw new TransportException("Client closed connection");      
      }
      if(count > 0) {
         input.flip(); 
      }
      return size > 0;
   }

   /**
    * Here we attempt to send all data within the output buffer. If
    * all of the data is delivered to the other side then this will
    * return true. If however there is content yet to be sent to
    * the other side then this returns false, telling the negotiation
    * that in order to resume it must attempt to send the content
    * again after a write ready operation on the underlying socket.
    * 
    * @return this returns true if all of the content is delivered
    */
   public boolean send() throws IOException {
      int require = output.position();
      int count = 0;
      
      if(require > 0) { 
         output.flip();
      }
      while(count < require) { 
         int size = channel.write(output);

         if(trace != null) {
            trace.trace(WRITE, size);
         }
         if(size <= 0) {
            break;
         }
         count += size;
      }
      if(require > 0) {
         output.compact(); 
      }
      return count == require;
   }   
   
   /**
    * This method is invoked when the negotiation is done and the
    * next phase of the connection is to take place. This will
    * be invoked when the SSL handshake has completed and the new
    * secure transport is to be handed to the processor.
    */
   private void dispatch() throws IOException {
      Transport secure = new SecureTransport(transport, state, output, input);

      if(processor != null) {
         trace.trace(HANDSHAKE_DONE);
         processor.process(secure);
      }
   }  
   
   /**
    * This method is used to terminate the handshake. Termination
    * typically occurs when there has been some error in the handshake
    * or when there is a timeout on some event, such as waiting for 
    * for a read or write operation to occur. As a result the TCP
    * channel is closed and any challenge future is cancelled.
    */
   private void terminate() throws IOException {
      Future<Certificate> future = state.getFuture();
      
      trace.trace(HANDSHAKE_FAILED);
      transport.close();
      future.cancel(true);
   }
   
   /**
    * This is used to execute the completion task after a challenge
    * for the clients X509 certificate. Execution of the completion
    * task in this way allows any challanger to be notified that
    * the handshake has complete.
    */
   private void complete() throws IOException {
      Runnable task = state.getFuture();
      
      if(task != null) {
         task.run();
      }
   }

   /**
    * This method is invoked when the negotiation is done and the
    * next phase of the connection is to take place. If a certificate
    * challenge was issued then the completion task is executed, if
    * this was the handshake for the initial connection a transport
    * is created and handed to the processor.
    */
   public void commit() throws IOException {
      if(!state.isChallenge()) {
         dispatch();
      } else {
         complete();
      }
   }
   
   /**
    * The <code>Committer</code> task is used to transfer the transport
    * created to the processor. This is executed when the SSL
    * handshake is completed. It allows the transporter to use the
    * newly created transport to read and write in plain text and
    * to have the SSL transport encrypt and decrypt transparently.
    */
   private class Committer extends Phase {
      
      /**
       * Constructor for the <code>Committer</code> task. This is used to
       * pass the transport object object to the processor when the
       * SSL handshake has completed. 
       * 
       * @param state this is the underlying negotiation to use
       * @param reactor this is the reactor used for I/O notifications
       * @param trace the trace that is used to monitor the handshake        
       */
      public Committer(Negotiation state, Reactor reactor, Trace trace) {
         super(state, reactor, trace, OP_READ);
      }

      /**
       * This is used to execute the task. It is up to the specific
       * task implementation to decide what to do when executed. If
       * the task needs to read or write data then it can attempt
       * to perform the read or write, if it incomplete the it can
       * be scheduled for execution with the reactor.
       */
      @Override
      public void execute() throws IOException{
         state.commit();
      }
   }
   
   /**
    * The <code>Consumer</code> task is used to schedule the negotiation
    * for a read operation. This allows the negotiation to receive any
    * messages generated by the client asynchronously. Once this has 
    * completed then it will resume the negotiation.
    */
   private class Consumer extends Phase {
      
      /**
       * Constructor for the <code>Consumer</code> task. This is used 
       * to create a task which will schedule a read operation for 
       * the negotiation. When the operation completes this will 
       * resume the negotiation.
       * 
       * @param state this is the negotiation object that is used
       * @param reactor this is the reactor used for I/O notifications        
       * @param trace the trace that is used to monitor the handshake        
       */
      public Consumer(Negotiation state, Reactor reactor, Trace trace) {
         super(state, reactor, trace, OP_READ);
      }
      
      /**
       * This method is used to determine if the task is ready. This 
       * is executed when the select operation is signalled. When this 
       * is true the the task completes. If not then this will 
       * schedule the task again for the specified select operation.
       * 
       * @return this returns true when the task has completed
       */
      @Override
      protected boolean ready() throws IOException {
         return state.receive();
      }
   }
   
   /**
    * The <code>Producer</code> is used to schedule the negotiation
    * for a write operation. This allows the negotiation to send any
    * messages generated during the negotiation asynchronously. Once
    * this has completed then it will resume the negotiation.
    */
   private class Producer extends Phase {
      
      /**
       * Constructor for the <code>Producer</code> task. This is used 
       * to create a task which will schedule a write operation for 
       * the negotiation. When the operation completes this will 
       * resume the negotiation.
       * 
       * @param state this is the negotiation object that is used
       * @param reactor this is the reactor used for I/O notifications        
       * @param trace the trace that is used to monitor the handshake        
       */
      public Producer(Negotiation state, Reactor reactor, Trace trace) {
         super(state, reactor, trace, OP_WRITE);
      }
      
      /**
       * This method is used to determine if the task is ready. This 
       * is executed when the select operation is signalled. When this 
       * is true the the task completes. If not then this will 
       * schedule the task again for the specified select operation.
       * 
       * @return this returns true when the task has completed
       */
      @Override
      protected boolean ready() throws IOException {
         return state.send();
      }
   }
}
