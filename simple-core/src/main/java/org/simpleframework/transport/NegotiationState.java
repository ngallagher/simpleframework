/*
 * NegotiationCertificate.java June 2013
 *
 * Copyright (C) 2013, Niall Gallagher <niallg@users.sf.net>
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

import static org.simpleframework.transport.TransportEvent.CERTIFICATE_CHALLENGE;
import static org.simpleframework.transport.TransportEvent.ERROR;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import org.simpleframework.transport.trace.Trace;

/**
 * The <code>NegotiationState</code> represents the certificate
 * that is sent by a client during a secure HTTPS conversation. This
 * may or may not contain an X509 certificate chain from the client.
 * If it does not a <code>CertificateChallenge</code> may be used to
 * issue a renegotiation of the connection. One completion of the 
 * renegotiation the challenge executes a completion operation.
 * 
 * @author Niall Gallagher
 */
class NegotiationState implements Certificate {
   
   /**
    * This is used to hold the completion task for the challenge.
    */
   private final RunnableFuture<Certificate> future;
   
   /**
    * This is the handshake used to acquire the certificate details.
    */
   private final Negotiation negotiation;   
   
   /**
    * This is the challenge used to request the client certificate.
    */
   private final Challenge challenge;   
   
   /**
    * This is the runnable task that is executed on task completion.
    */
   private final Delegate delegate;
   
   /**
    * This is the socket representing the underlying TCP connection.
    */
   private final Socket socket;

   /**
    * Constructor for the <code>NegotiationCertificate</code> object.
    * This creates an object used to provide certificate details and
    * a means to challenge for certificate details for the connected
    * client if required.
    * 
    * @param negotiation the negotiation associated with this
    * @param socket the underlying TCP connection to the client
    */
   public NegotiationState(Negotiation negotiation, Socket socket) {
      this.delegate = new Delegate(socket);
      this.future = new FutureTask<Certificate>(delegate, this);
      this.challenge = new Challenge(socket);
      this.negotiation = negotiation;
      this.socket = socket;
   }
   
   /**
    * This is used to determine if the state is in challenge mode. 
    * In challenge mode a challenge future will be executed on
    * completion of the challenge. This will the completion task.
    * 
    * @return this returns true if the state is in challenge mode
    */
   public boolean isChallenge() {
      return delegate.isSet();
   }
   
   /**
    * This returns the completion task associated with any challenge
    * made for the client certificate. If this returns null then no
    * challenge has been made for the client certificate.
    * 
    * @return this returns the challenge completion task if any
    */
   public RunnableFuture<Certificate> getFuture() {
      return future;
   }
   
   /**
    * This returns a challenge for the certificate. A challenge is
    * issued by providing a <code>Runnable</code> task which is to 
    * be executed when the challenge has completed. Typically this
    * task should be used to drive completion of an HTTPS request.   
    * 
    * @return this returns a challenge for the client certificate
    */
   public CertificateChallenge getChallenge() throws Exception {
      return challenge;
   }

   /**
    * This will return the X509 certificate chain, if any, that 
    * has been sent by the client. A certificate chain is typically
    * only send when the server explicitly requests the certificate
    * on the initial connection or when it is challenged for.
    * 
    * @return this returns the clients X509 certificate chain
    */
   public X509Certificate[] getChain() throws Exception {
      SSLSession session = getSession();
      
      if(session != null) {
         return session.getPeerCertificateChain();
      }
      return null;
   }

   /**
    * This is used to acquire the SSL session associated with the
    * handshake. The session makes all of the details associated
    * with the handshake available, including the cipher suites 
    * used and the SSL context used to create the session.
    * 
    * @return the SSL session associated with the connection
    */
   public SSLSession getSession() throws Exception{
      SSLEngine engine = socket.getEngine();
      
      if(engine != null) {
         return engine.getSession();
      }
      return null;
   }   
   
   /**
    * This is used to determine if the X509 certificate chain is
    * present for the request. If it is not present then a challenge
    * can be used to request the certificate. 
    * 
    * @return true if the certificate chain is present
    */
   public boolean isChainPresent() {
      try {
         return getChain() != null;
      } catch(Exception e) {
         return false;
      }
   }
   
   /**
    * The <code>Challenge</code> object is used to enable the server
    * to challenge for the client X509 certificate if desired. It 
    * performs the challenge by performing an SSL renegotiation to
    * request that the client sends the 
    */
   private class Challenge implements CertificateChallenge  {
      
      /**
       * This is the SSL engine that is used to begin the handshake.
       */
      private final SSLEngine engine;
      
      /**
       * This is used to trace the certificate challenge request.
       */
      private final Trace trace;
      
      /**
       * Constructor for the <code>Challenge</code> object. This can
       * be used to challenge the client for their X509 certificate. 
       * It does this by performing an SSL renegotiation on the
       * existing TCP connection.
       * 
       * @param socket this is the TCP connection to the client
       */
      public Challenge(Socket socket) {
         this.engine = socket.getEngine();
         this.trace = socket.getTrace();
      }
      
      /**
       * This method will challenge the client for their certificate.
       * It does so by performing an SSL renegotiation. Successful
       * completion of the SSL renegotiation results in the client
       * providing their certificate, and execution of the task. 
       */
      public Future<Certificate> challenge() {
         return challenge(null);
      }
    
      /**
       * This method will challenge the client for their certificate.
       * It does so by performing an SSL renegotiation. Successful
       * completion of the SSL renegotiation results in the client
       * providing their certificate, and execution of the task. 
       * 
       * @param completion task to be run on successful challenge
       */
      public Future<Certificate> challenge(Runnable task) {
         try {
            if(!isChainPresent()) {
               resume(task);
            } else {
               future.run();
            }
         } catch(Exception cause) {
            trace.trace(ERROR, cause);
         }
         return future;
      }
      
      /**
       * This method will challenge the client for their certificate.
       * It does so by performing an SSL renegotiation. Successful
       * completion of the SSL renegotiation results in the client
       * providing their certificate, and execution of the task. 
       * 
       * @param completion task to be run on successful challenge
       */
      private void resume(Runnable task) {
         try {
            trace.trace(CERTIFICATE_CHALLENGE);
            delegate.set(task);
            engine.setNeedClientAuth(true);  
            engine.beginHandshake();         
            negotiation.resume();
         } catch(Exception cause) {
            trace.trace(ERROR, cause);
            negotiation.cancel();
         }
      }
   }
   
   /**
    * The <code>Delegate</code> is basically a settable runnable object.
    * It enables the challenge to set an optional runnable that will
    * be executed when the challenge has completed. If the challenge
    * has not been given a completion task this runs straight through
    * without any state change or action on the certificate.     
    */
   private class Delegate implements Runnable {
      
      /**
       * This is the reference to the runnable that is to be executed.
       */
      private final AtomicReference<Runnable> task;
      
      /**
       * This is used to determine if the challenge is ready to run.
       */
      private final AtomicBoolean ready;
      
      /**
       * This is used to trace any errors when running the task.
       */
      private final Trace trace;
      
      /**
       * Constructor for the <code>Delegate</code> object. This is
       * used to create a wrapper for the completion task so that it
       * can be executed safely and have any errors traced.
       * 
       * @param socket this socket the handshake is associated with
       */
      public Delegate(Socket socket) {
         this.task = new AtomicReference<Runnable>();
         this.ready = new AtomicBoolean();
         this.trace = socket.getTrace();
      }
      
      /**
       * This is used to determine if the delegate is ready to be
       * used. It is ready only after the completion task has been
       * set. When ready a challenge can be executed.        
       * 
       * @return this returns true if a completion task is set
       */
      public boolean isSet() {
         return ready.get();
      }
      
      /**
       * This is used to set the completion task that is to be executed
       * when the challenge has finished. This can be set to null if 
       * no task is to be executed on completion.
       * 
       * @param runnable the task to run when the challenge finishes
       */
      public void set(Runnable runnable) {
         ready.set(true);
         task.set(runnable);
      }      
      
      /**
       * This is used to run the completion task. If no completion 
       * task has been set this will run through without any change to
       * the state of the certificate. All errors thrown by the task
       * will be caught and traced.        
       */
      public void run() {
         try {
            Runnable runnable = task.get();
            
            if(runnable != null) {
               runnable.run();
            }
         } catch(Exception cause) {
            trace.trace(ERROR, cause);
         } finally {
            task.set(null);
         }
      }
   }
}
