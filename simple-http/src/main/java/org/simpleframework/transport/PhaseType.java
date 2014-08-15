/*
 * PhaseType.java February 2007
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

/**
 * The <code>PhaseType</code> enumeration is used to determine what
 * phase of the negotiation the handshake is in. This allows the
 * negotiation to control the selection for read and write ready
 * operations. Also, status signals completion of the handshake.
 * 
 * @author Niall Gallagher
 */
enum PhaseType {
   
   /**
    * Tells the negotiation that a read operations is needed.
    */
   CONSUME,
   
   /**
    * Tells the negotiation that a write operation is required.
    */
   PRODUCE,
   
   /**
    * Tells the negotiation that the the handshake is complete. 
    */
   COMMIT
}