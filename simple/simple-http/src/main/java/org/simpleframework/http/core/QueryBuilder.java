/*
 * QueryBuilder.java October 2002
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

package org.simpleframework.http.core;

import static org.simpleframework.http.Protocol.APPLICATION;
import static org.simpleframework.http.Protocol.URL_ENCODED;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.message.Entity;
import org.simpleframework.http.message.Header;

/**
 * The <code>QueryBuilder</code> object is used to create the query. 
 * It is created using the request URI query and a form post body if
 * sent. The application/x-www-form-urlencoded conent type identifies
 * the body as contain form data. If there are duplicates then they
 * both are available from the query that is built.
 * 
 * @author Niall Gallagher
 */
class QueryBuilder {
   
   /**
    * This is the request that is used to acquire the data.
    */
   private final Request request;
   
   /**
    * This is the header that is used to acquire the data.
    */
   private final Header header;

   /**
    * Constructor for the <code>QueryBuilder</code> object. This will
    * create an object that can be used to construct a single query
    * from the multiple sources of data within the request entity.
    * 
    * @param request this is the request to build a query for
    * @param entity this is the entity that contains the data
    */
   public QueryBuilder(Request request, Entity entity) {
      this.header = entity.getHeader();
      this.request = request;     
   } 
   
   /**
    * This method is used to acquire the query part from the HTTP 
    * request URI target and a form post if it exists. Both the 
    * query and the form post are merge together in a single query.
    * 
    * @return the query associated with the HTTP target URI
    */   
   public Query build() {;
      Query query = header.getQuery();
      
      if(!isFormPost()) {
         return query;
      }
      return getQuery(query);
   }
   
   /**
    * This method is used to acquire the query part from the HTTP 
    * request URI target and a form post if it exists. Both the 
    * query and the form post are merge together in a single query.
    * 
    * @param query this is the URI query string to be used
    * 
    * @return the query associated with the HTTP target URI
    */   
   private Query getQuery(Query query) {
      String body = getContent(); 
      
      if(body == null) {
         return query;
      }
      return new QueryCombiner(query, body);
   }
   
   /**
    * This method attempts to acquire the content of the  request
    * body. If there is an <code>IOException</code> acquiring the
    * content of the body then this will simply return a null
    * value without reporting the exception.
    * 
    * @return the content of the body, or null on error
    */
   private String getContent() {
      try {
         return request.getContent();
      } catch(Exception e) {
         return null;
      }
   }
   
   /**
    * This is used to determine if the content type is a form POST
    * of type application/x-www-form-urlencoded. Such a type is
    * used when a HTML form is used to post data to the server.
    * 
    * @return this returns true if content type is a form post
    */   
   private boolean isFormPost() {
      ContentType type = request.getContentType();
      
      if(type == null) {
         return false;
      }
      return isFormPost(type);
   }
   
   /**
    * This is used to determine if the content type is a form POST
    * of type application/x-www-form-urlencoded. Such a type is
    * used when a HTML form is used to post data to the server.  
    * 
    * @param type the type to determine if its a form post
    * 
    * @return this returns true if content type is a form post
    */
   private boolean isFormPost(ContentType type) {   
      String primary = type.getPrimary();
      String secondary = type.getSecondary();
      
      if(!primary.equals(APPLICATION)) {
         return false;
      }
      return secondary.equals(URL_ENCODED);
   }
}
