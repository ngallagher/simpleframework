/*
 * Body.java February 2007
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

package org.simpleframework.http.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.simpleframework.http.Part;

/**
 * The <code>Body</code> interface is used to represent the body of
 * a HTTP entity. It contains the information that is delivered with
 * the request. The body is represented by a stream of bytes. In
 * order to access the entity body this interface provides a stream
 * which can be used to read it. Also, should the message be encoded
 * as a multipart message the individual parts can be read using the
 * <code>Attachment</code> instance for it.
 *
 * @author Niall Gallagher 
 */ 
public interface Body {           
   
   /**
    * This will acquire the contents of the body in UTF-8. If there
    * is no content encoding and the user of the request wants to
    * deal with the body as a string then this method can be used.
    * It will simply create a UTF-8 string using the body bytes.
    *
    * @return returns a UTF-8 string representation of the body
    */ 
   String getContent() throws IOException;
   
   /**
    * This will acquire the contents of the body in the specified
    * charset. Typically this will be given the charset as taken 
    * from the HTTP Content-Type header. Although any encoding can
    * be specified to convert the body to a string representation.
    *
    * @return returns an encoded string representation of the body
    */ 
   String getContent(String charset) throws IOException;   

   /**
    * This is used to acquire the contents of the body as a stream.
    * Each time this method is invoked a new stream is created that
    * will read the contents of the body from the first byte. This
    * ensures that the stream can be acquired several times without
    * any issues arising from previous reads.
    *
    * @return this returns a new string used to read the body
    */    
   InputStream getInputStream() throws IOException;   
 
   /**
    * This method is used to acquire a <code>Part</code> from the
    * HTTP request using a known name for the part. This is typically 
    * used  when there is a file upload with a multipart POST request.
    * All parts that are not files can be acquired as string values
    * from the attachment object.
    * 
    * @param name this is the name of the part object to acquire
    * 
    * @return the named part or null if the part does not exist
    */ 
   Part getPart(String name);   
   
   /**
    * This method is used to get all <code>Part</code> objects that
    * are associated with the request. Each attachment contains the 
    * body and headers associated with it. If the request is not a 
    * multipart POST request then this will return an empty list.
    * 
    * @return the list of parts associated with this request
    */     
   List<Part> getParts();
}


