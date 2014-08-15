/*
 * Part.java February 2007
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

import java.io.IOException;
import java.io.InputStream;

/**
 * The <code>Part</code> object is used to represent a part within
 * a request message. Typically a part represents either a text
 * parameter or a file, with associated headers. The contents of
 * the part can be acquire as an <code>InputStream</code> or as a
 * string encoded in the default HTTP encoding ISO-8859-1 or in
 * the encoding specified with the Content-Type header.
 * 
 * @author Niall Gallagher
 */
public interface Part {
   
   /**
    * This method is used to determine the type of a part. Typically
    * a part is either a text parameter or a file. If this is true
    * then the content represented by the associated part is a file.
    *
    * @return this returns true if the associated part is a file
    */
   boolean isFile();
   
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
    * This is used to acquire the header value for the specified 
    * header name. Providing the header values through this method
    * ensures any special processing for a know content type can be
    * handled by an application.
    * 
    * @param name the name of the header to get the value for
    * 
    * @return value of the header mapped to the specified name
    */
   String getHeader(String name);
   
   /**
    * This is used to acquire the content of the part as a string.
    * The encoding of the string is taken from the content type. 
    * If no content type is sent the content is decoded in the
    * standard default of ISO-8859-1.
    * 
    * @return this returns a string representing the content
    * 
    * @throws IOException thrown if the content can not be created
    */
   String getContent() throws IOException;

   /**
    * This is used to acquire an <code>InputStream</code> for the
    * part. Acquiring the stream allows the content of the part to
    * be consumed by reading the stream. Each invocation of this
    * method will produce a new stream starting from the first byte.
    * 
    * @return this returns the stream for this part object
    * 
    * @throws IOException thrown if the stream can not be created
    */
   InputStream getInputStream() throws IOException;
     
   /**
    * This is used to acquire the content type for this part. This
    * is typically the type of content for a file part, as provided
    * by a MIME type from the HTTP "Content-Type" header.
    * 
    * @return this returns the content type for the part object
    */
   ContentType getContentType();  
}