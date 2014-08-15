/*
 * ContentDisposition.java February 2007
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

package org.simpleframework.http;

/**
 * The <code>ContentDisposition</code> object represents the HTTP 
 * Content-Disposition header of a request. A content disposition
 * contains the name of the part and whether that part contains 
 * the contents of a file. If the part represents a parameter then
 * the <code>getName</code> can be used to determine the name, if 
 * it represents a file then <code>getFileName</code> is preferred.  
 * 
 * @author Niall Gallagher
 */
public interface ContentDisposition {

   /**
    * This method is used to acquire the name of the part. Typically
    * this is used when the part represents a text parameter rather
    * than a file. However, this can also be used with a file part.
    * 
    * @return this returns the name of the associated part
    */
   String getName();
   
   /**
    * This method is used to acquire the file name of the part. This
    * is used when the part represents a text parameter rather than 
    * a file. However, this can also be used with a file part.
    *
    * @return this returns the file name of the associated part
    */
   String getFileName();
   
   /**
    * This method is used to determine the type of a part. Typically
    * a part is either a text parameter or a file. If this is true
    * then the content represented by the associated part is a file.
    *
    * @return this returns true if the associated part is a file
    */
   boolean isFile();
}
