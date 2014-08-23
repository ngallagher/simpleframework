/*
 * RequestCertificate.java June 2013
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

package org.simpleframework.http.core;

import java.io.IOException;
import java.util.concurrent.Future;

import javax.security.cert.X509Certificate;

import org.simpleframework.http.message.Entity;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.CertificateChallenge;
import org.simpleframework.transport.Channel;

/**
 * The <code>RequestCertificate</code> represents a certificate for
 * an HTTP request. It basically wraps the raw SSL certificate that
 * comes with the <code>Channel</code>. Wrapping the raw certificate
 * allows us to enforce the HTTPS workflow for SSL renegotiation,
 * which requires some rather weird behaviour. Most importantly
 * we only allow a challenge when the response has not been sent.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.CertificateChallenge
 */
class RequestCertificate implements Certificate {
   
   /**
    * This is used to challenge the client for an X509 certificate.
    */
   private final CertificateChallenge challenge;
   
   /**
    * This is the raw underlying certificate for the SSL channel.
    */
   private final Certificate certificate;
   
   /**
    * This is the channel representing the client connection.
    */
   private final Channel channel;
   
   /**
    * Constructor for the <code>RequestCertificate</code>. This is
    * used to create a wrapper for the raw SSL certificate that
    * is provided by the underlying SSL session. 
    * 
    * @param observer the observer used to observe the transaction     
    * @param entity the request entity containing the data
    */
   public RequestCertificate(BodyObserver observer, Entity entity) {
      this.challenge = new Challenge(observer, entity);
      this.channel = entity.getChannel();
      this.certificate = channel.getCertificate();
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
      return certificate.getChain();
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
    * This is used to determine if the X509 certificate chain is
    * present for the request. If it is not present then a challenge
    * can be used to request the certificate. 
    * 
    * @return true if the certificate chain is present
    */
   public boolean isChainPresent() throws Exception {
      return certificate.isChainPresent();
   }   

   /**
    * The <code>Challenge</code> provides a basic wrapper around the
    * challenge provided by the SSL connection. It is used to enforce
    * the workflow required by HTTP, this workflow requires that the
    * SSL renegotiation be issued before the response is sent. This
    * will also throw an exception if a challenge is issued for 
    * a request that already has a client certificate.
    */
   private static class Challenge implements CertificateChallenge {      
      
      /**
       * This is the observer used to keep track of the HTTP transaction.
       */
      private final BodyObserver observer;      
      
      /**
       * This is the certificate associated with the SSL connection. 
       */
      private final Certificate certificate;
      
      /**
       * This is the channel representing the underlying TCP stream.
       */
      private final Channel channel;
      
      /**
       * Constructor for the <code>Challenge</code> object. This is
       * basically a wrapper for the raw certificate challenge that 
       * will enforce some of the workflow required by HTTPS.
       * 
       * @param observer this observer used to track the transaction        
       * @param entity this entity containing the request data
       */
      public Challenge(BodyObserver observer, Entity entity) {
         this.channel = entity.getChannel();
         this.certificate = channel.getCertificate();
         this.observer = observer;
      }
      
      /**
       * This method will challenge the client for their certificate.
       * It does so by performing an SSL renegotiation. Successful
       * completion of the SSL renegotiation results in the client
       * providing their certificate, and execution of the task. 
       * 
       * @param completion task to be run on successful challenge
       */
      public Future<Certificate> challenge() throws Exception {
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
      public Future<Certificate> challenge(Runnable completion) throws Exception {
         if(certificate == null) {
            throw new IOException("Challenging must be done on a secure connection");            
         }
         CertificateChallenge challenge = certificate.getChallenge();
         
         if(certificate.isChainPresent()) {
            throw new IOException("Certificate is already present");
         }
         if(observer.isCommitted()) {
            throw new IOException("Response has already been committed");         
         }
         return challenge.challenge(completion);
      }
   }
}
