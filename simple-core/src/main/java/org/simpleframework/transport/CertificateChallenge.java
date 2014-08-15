/*
 * CertificateChallenge.java June 2013
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

import java.util.concurrent.Future;

/**
 * The <code>CertificateChallenge</code> object is used to challenge 
 * a client for their x509 certificate. Notification of a successful
 * challenge for the certificate is done using a completion task.
 * The task is executed when the SSL renegotiation completes with
 * a client certificate. 
 * <p>
 * For HTTPS the SSL renegotiation workflow used to challenge the
 * client for their X509 certificate is rather bizzare. It starts 
 * with an initial challenge, where an SSL handshake is performed.
 * This initial handshake typically completes but results in the
 * TCP connection being closed by the client. Then a second
 * handshake is performed by the client on a new TCP connection,
 * this second handshake does not contain the certificate either. 
 * When the handshake is finished on this new connection the client 
 * will resubmit the original HTTP request. Again the server will 
 * have to challenge for the certificate, which should succeed and 
 * result in execution of the task provided.
 * <p>
 * An important point to note here, is that if the client closes
 * the TCP connection on the first challenge, the completion task
 * will not be executed, it will be ignored. Only a successful
 * completion of a HTTPS renegotiation will result in execution
 * of the provided task. 
 * 
 * @author Niall Gallagher
 */
public interface CertificateChallenge {
   
   /**
    * This method will challenge the client for their certificate.
    * It does so by performing an SSL renegotiation. Successful
    * completion of the SSL renegotiation results in the client
    * providing their certificate, and execution of the task. 
    * 
    * @return this future containing the original certificate
    */
   Future<Certificate> challenge() throws Exception;   
   
   /**
    * This method will challenge the client for their certificate.
    * It does so by performing an SSL renegotiation. Successful
    * completion of the SSL renegotiation results in the client
    * providing their certificate, and execution of the task. 
    * 
    * @param completion task to be run on successful challenge
    * 
    * @return this future containing the original certificate    
    */
   Future<Certificate> challenge(Runnable completion) throws Exception;
}
