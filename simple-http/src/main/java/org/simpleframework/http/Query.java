/*
 * Query.java February 2001
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

import java.util.List;
import java.util.Map;

/**
 * The <code>Query</code> object is used to represent HTTP query
 * parameters. Parameters are acquired by name and can be either a
 * string, float, int, or boolean value. This ensures that data can
 * be conveniently extracted in the correct type. This stores the
 * parameters in a map of key value pairs. Each parameter can be
 * acquired using the name of the parameter, if the parameter is
 * named twice then all values can be acquired.
 *
 * @author Niall Gallagher
 */
public interface Query extends Map<String, String> {
   
   /**
    * This method is used to acquire a <code>List</code> for all of
    * the parameter values associated with the specified name. Using
    * this method allows the query to expose many values taken from
    * the query or HTTP form posting. Typically the first value in
    * the list is the value from the <code>get(String)</code> method
    * as this is the primary value from the ordered list of values. 
    * 
    * @param name this is the name used to search for the value
    * 
    * @return this is the list of values associated with the key
    */
   List<String> getAll(Object name);
   
   /**
    * This extracts an integer parameter for the named value. If the 
    * named parameter does not exist this will return a zero value. 
    * If however the parameter exists but is not in the format of a 
    * decimal integer value then this will throw an exception.
    *
    * @param name the name of the parameter value to retrieve
    *
    * @return this returns the named parameter value as an integer   
    */
   int getInteger(Object name);

   /**
    * This extracts a float parameter for the named value. If the 
    * named parameter does not exist this will return a zero value. 
    * If however the parameter exists but is not in the format of a 
    * floating point number then this will throw an exception.
    *
    * @param name the name of the parameter value to retrieve
    *
    * @return this returns the named parameter value as a float   
    */
   float getFloat(Object name);

   /**
    * This extracts a boolean parameter for the named value. If the
    * named parameter does not exist this will return false otherwise
    * the value is evaluated. If it is either <code>true</code> or 
    * <code>false</code> then those boolean values are returned.
    * 
    * @param name the name of the parameter value to retrieve
    *
    * @return this returns the named parameter value as an float
    */
   boolean getBoolean(Object name);   

   /**
    * This will return all parameters represented using the HTTP
    * URL query format. The <code>x-www-form-urlencoded</code>
    * format is used to encode the attributes, see RFC 2616. 
    * <p>
    * This will also encode any special characters that appear
    * within the name and value pairs as an escaped sequence.
    * If there are no parameters an empty string is returned.
    *
    * @return returns an empty string if the is no parameters
    */ 
   String toString();
}
