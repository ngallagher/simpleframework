/*
 * Certificate.java June 2013
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

import javax.security.cert.X509Certificate;

/**
 * The <code>Certificate</code> interface represents the certificate
 * that is sent by a client during a secure HTTPS conversation. This
 * may or may not contain an X509 certificate chain from the client.
 * If it does not a <code>CertificateChallenge</code> may be used to
 * issue a renegotiation of the connection. One completion of the 
 * renegotiation the challenge executes a completion operation.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.CertificateChallenge
 */
public interface Certificate {

   /**
    * This will return the X509 certificate chain, if any, that 
    * has been sent by the client. A certificate chain is typically
    * only send when the server explicitly requests the certificate
    * on the initial connection or when it is challenged for.
    * 
    * @return this returns the clients X509 certificate chain
    */
   X509Certificate[] getChain() throws Exception; 
   
   /**
    * This returns a challenge for the certificate. A challenge is
    * issued by providing a <code>Runnable</code> task which is to 
    * be executed when the challenge has completed. Typically this
    * task should be used to drive completion of an HTTPS request.   
    * 
    * @return this returns a challenge for the client certificate
    */
   CertificateChallenge getChallenge() throws Exception;
      
   /**
    * This is used to determine if the X509 certificate chain is
    * present for the request. If it is not present then a challenge
    * can be used to request the certificate. 
    * 
    * @return true if the certificate chain is present
    */
   boolean isChainPresent() throws Exception;
}
