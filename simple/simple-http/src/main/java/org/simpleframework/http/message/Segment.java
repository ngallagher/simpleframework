/*
 * Segment.java February 2007
 *
 * Copyright (C) 2001, Niall Gallagher <niallg@users.sf.net>
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

import org.simpleframework.http.ContentDisposition;
import org.simpleframework.http.ContentType;

import java.util.List;

/**
 * The <code>Segment</code> object represents a collection of header
 * values that is followed by a body. This is used to represent the
 * header of a multipart upload part. The raw value of each header
 * for the part can be acquired using this interface, also the type 
 * and the disposition of the body can be determined from this.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.Part
 */ 
public interface Segment {    

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
    * This can be used to get the value of the first message header
    * that has the specified name. The value provided from this will
    * be trimmed so there is no need to modify the value, also if 
    * the header name specified refers to a comma separated list of
    * values the value returned is the first value in that list.  
    * This returns null if there is no HTTP message header.
    *
    * @param name the HTTP message header to get the value from
    *
    * @return this returns the value that the HTTP message header
    */   
   String getValue(String name);
   
   /**
    * This can be used to get the value of the first message header
    * that has the specified name. The value provided from this will
    * be trimmed so there is no need to modify the value, also if 
    * the header name specified refers to a comma separated list of
    * values the value returned is the first value in that list.  
    * This returns null if there is no HTTP message header.
    *
    * @param name the HTTP message header to get the value from
    * @param index acquires a specific header value from multiple
    *
    * @return this returns the value that the HTTP message header
    */   
   String getValue(String name, int index);
   
   /**
    * This can be used to get the values of HTTP message headers
    * that have the specified name. This is a convenience method that 
    * will present that values as tokens extracted from the header.
    * This has obvious performance benefits as it avoids having to 
    * deal with <code>substring</code> and <code>trim</code> calls.
    * <p>
    * The tokens returned by this method are ordered according to
    * there HTTP quality values, or "q" values, see RFC 2616 section
    * 3.9. This also strips out the quality parameter from tokens
    * returned. So "image/html; q=0.9" results in "image/html". If
    * there are no "q" values present then order is by appearance.
    * <p> 
    * The result from this is either the trimmed header value, that
    * is, the header value with no leading or trailing whitespace
    * or an array of trimmed tokens ordered with the most preferred
    * in the lower indexes, so index 0 is has highest preference.
    *
    * @param name the name of the headers that are to be retrieved
    *
    * @return ordered array of tokens extracted from the header(s)
    */
   List<String> getValues(String name);
   
   /**
    * This is a convenience method that can be used to determine the 
    * content type of the message body. This will determine whether
    * there is a <code>Content-Type</code> header, if there is then
    * this will parse that header and represent it as a typed object
    * which will expose the various parts of the HTTP header.
    *
    * @return this returns the content type value if it exists
    */   
   ContentType getContentType();
   
   /**
    * This is a convenience method that can be used to determine the 
    * content type of the message body. This will determine whether
    * there is a <code>Content-Disposition</code> header, if there is
    * this will parse that header and represent it as a typed object
    * which will expose the various parts of the HTTP header.
    *
    * @return this returns the content disposition value if it exists
    */      
   ContentDisposition getDisposition();   
   
   /**
    * This is a convenience method that can be used to determine the 
    * content type of the message body. This will determine whether
    * there is a <code>Transfer-Encoding</code> header, if there is 
    * then this will parse that header and return the first token in
    * the comma separated list of values, which is the primary value.
    *
    * @return this returns the transfer encoding value if it exists
    */   
   String getTransferEncoding();
   
   /**
    * This is a convenience method that can be used to determine
    * the length of the message body. This will determine if there
    * is a <code>Content-Length</code> header, if it does then the
    * length can be determined, if not then this returns -1.
    *
    * @return the content length, or -1 if it cannot be determined
    */
   long getContentLength();
}


