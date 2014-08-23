/*
 * Principal.java November 2002
 *
 * Copyright (C) 2002, Niall Gallagher <niallg@users.sf.net>
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
 
package org.simpleframework.http;

/**
 * The <code>Principal</code> interface is used to describe a 
 * user that has a name and password. This should not be 
 * confused with the <code>java.security.Principal</code> 
 * interface which does not provide <code>getPassword</code>.
 *
 * @author Niall Gallagher
 */
public interface Principal {

   /**
    * The <code>getPassword</code> method is used to retrieve 
    * the password of the principal. This is the password 
    * tag in the RFC 2616 Authorization credentials expression.
    *
    * @return this returns the password for this principal
    */
   String getPassword();
   
   /**
    * The <code>getName</code> method is used to retreive 
    * the name of the principal. This is the name tag in 
    * the RFC 2616 Authorization credentials expression.
    *
    * @return this returns the name of this principal
    */
   String getName();
}
