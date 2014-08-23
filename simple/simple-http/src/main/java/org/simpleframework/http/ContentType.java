/*
 * ContentType.java February 2001
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
 
package org.simpleframework.http;

/** 
 * This provides access to the MIME type parts, that is the primary 
 * type, the secondary type and an optional character set parameter.
 * The <code>charset</code> parameter is one of many parameters that 
 * can be associated with a MIME type. This however this exposes this
 * parameter with a typed method.
 * <p>
 * The <code>getCharset</code> will return the character encoding the
 * content type is encoded within. This allows the user of the content
 * to decode it correctly. Other parameters can be acquired from this
 * by simply providing the name of the parameter. 
 *
 * @author Niall Gallagher
 */
public interface ContentType {
   
   /**
    * This method is used to get the primary and secondary parts 
    * joined together with a "/". This is typically how a content
    * type is examined. Here convenience is most important, we can
    * easily compare content types without any parameters.
    * 
    * @return this returns the primary and secondary types
    */
   String getType();

   /** 
    * This sets the primary type to whatever value is in the string 
    * provided is. If the string is null then this will contain a 
    * null string for the primary type of the parameter, which is 
    * likely invalid in most cases.
    * 
    * @param type the type to set for the primary type of this
    */ 
   void setPrimary(String type);
   
   /** 
    * This is used to retrieve the primary type of this MIME type. The 
    * primary type part within the MIME type defines the generic type.
    * For example <code>text/plain; charset=UTF-8</code>. This will 
    * return the text value. If there is no primary type then this 
    * will return <code>null</code> otherwise the string value.
    *
    * @return the primary type part of this MIME type
    */   
   String getPrimary();  
   
   /** 
    * This sets the secondary type to whatever value is in the string 
    * provided is. If the string is null then this will contain a 
    * null string for the secondary type of the parameter, which is 
    * likely invalid in most cases.
    * 
    * @param type the type to set for the primary type of this
    */ 
   void setSecondary(String type);

   /** 
    * This is used to retrieve the secondary type of this MIME type. 
    * The secondary type part within the MIME type defines the generic 
    * type. For example <code>text/html; charset=UTF-8</code>. This 
    * will return the HTML value. If there is no secondary type then 
    * this will return <code>null</code> otherwise the string value.
    *
    * @return the primary type part of this MIME type
    */   
   String getSecondary();

   /** 
    * This will set the <code>charset</code> to whatever value the
    * string contains. If the string is null then this will not set 
    * the parameter to any value and the <code>toString</code> method 
    * will not contain any details of the parameter.
    *
    * @param charset parameter value to add to the MIME type
    */ 
   void setCharset(String charset);

   /** 
    * This is used to retrieve the <code>charset</code> of this MIME 
    * type. This is a special parameter associated with the type, if
    * the parameter is not contained within the type then this will
    * return null, which typically means the default of ISO-8859-1.
    *
    * @return the value that this parameter contains
    */     
   String getCharset();
   
   /**
    * This is used to retrieve an arbitrary parameter from the MIME
    * type header. This ensures that values for <code>boundary</code>
    * or other such parameters are not lost when the header is parsed.
    * This will return the value, unquoted if required, as a string. 
    * 
    * @param name this is the name of the parameter to be retrieved
    * 
    * @return this is the value for the parameter, or null if empty
    */
   String getParameter(String name);
      
   /**
    * This will add a named parameter to the content type header. If
    * a parameter of the specified name has already been added to the
    * header then that value will be replaced by the new value given.
    * Parameters such as the <code>boundary</code> as well as other
    * common parameters can be set with this method.
    * 
    * @param name this is the name of the parameter to be added     
    * @param value this is the value to associate with the name
    */
   void setParameter(String name, String value);

   /** 
    * This will return the value of the MIME type as a string. This
    * will concatenate the primary and secondary type values and 
    * add the <code>charset</code> parameter to the type which will
    * recreate the content type.
    * 
    * @return this returns the string representation of the type
    */
   String toString();
}
