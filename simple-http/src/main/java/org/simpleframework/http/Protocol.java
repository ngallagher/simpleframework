/*
 * Protocol.java May 2012
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
 * This represents the HTTP header names defined in RFC 2616. It can be
 * used to set and get headers safely from the <code>Request</code> and 
 * <code>Response</code> objects. This is used internally by the HTTP 
 * server to parse the incoming requests and also to submit response 
 * values for each conversation.
 * <p>
 * In addition to the header names this also contains some common 
 * HTTP header value tokens. These are provided for convenience and
 * can be used to ensure that response values comply with RFC 2616.
 * 
 * @author Niall Gallagher
 */
public interface Protocol {
      
   /**
    * Specifies media types which are acceptable for the response.
    */
   String ACCEPT = "Accept";
   
   /** 
    * Indicates what character sets are acceptable for the response.
    */
   String ACCEPT_CHARSET = "Accept-Charset";
   
   /**
    * Restricts the content codings that are acceptable in the response.
    */
   String ACCEPT_ENCODING = "Accept-Encoding";
   
   /**
    * Restricts the set of languages that are preferred as a response.
    */
   String ACCEPT_LANGUAGE = "Accept-Language";
   
   /**
    * Indicates a servers acceptance of range requests for a resource.
    */
   String ACCEPT_RANGES = "Accept-Ranges";
   
   /**
    * Estimates the amount of time since the response was generated.
    */
   String AGE = "Age";
   
   /**
    * Lists the set of methods supported by the resource identified.
    */
   String ALLOW = "Allow";
   
   /**
    * Sent by a client that wishes to authenticate itself with a server.
    */
   String AUTHORIZATION = "Authorization";
   
   /**
    * Specifies directives that must be obeyed by all caching mechanisms. 
    */
   String CACHE_CONTROL = "Cache-Control";
   
   /**
    * Specifies options that are desired for that particular connection. 
    */
   String CONNECTION = "Connection";
   
   /**
    * Specifies a tag indicating of its desired presentation semantics.
    */
   String CONTENT_DISPOSITION = "Content-Disposition";
   
   /**
    * Indicates additional content codings have been applied to the body.
    */
   String CONTENT_ENCODING = "Content-Encoding";
   
   /**
    * Describes the languages of the intended audience for the body.
    */
   String CONTENT_LANGUAGE = "Content-Language";
   
   /**
    * Indicates the size of the entity body in decimal number of octets.
    */
   String CONTENT_LENGTH = "Content-Length";
   
   /**
    * Used to supply the resource location for the entity enclosed.
    */
   String CONTENT_LOCATION = "Content-Location";
   
   /**
    * An MD5 digest of the body for the purpose of checking integrity.
    */
   String CONTENT_MD5 = "Content-MD5";
   
   /**
    * Specifies where in the full body a partial body should be applied.
    */
   String CONTENT_RANGE = "Content-Range";
   
   /**
    * Indicates the media type of the body sent to the recipient.
    */
   String CONTENT_TYPE = "Content-Type";
   
   /**
    * Represents a cookie that contains some information from the client.
    */
   String COOKIE = "Cookie";
   
   /**
    * Represents the date and time at which the message was originated.
    */
   String DATE = "Date";
   
   /**
    * Provides the value of the entity tag for the requested variant.
    */
   String ETAG = "ETag";
   
   /**
    * Indicate that particular server behaviors are required by the client.
    */
   String EXPECT = "Expect";
   
   /**
    * Gives the time after which the response is considered stale.
    */
   String EXPIRES = "Expires";
   
   /**
    * Address for the human user who controls the requesting user agent.
    */
   String FROM = "From";
   
   /**
    * Specifies the host and port number of the resource being requested.
    */
   String HOST = "Host";
   
   /**
    * Specifies the entity tag for a request to make it conditional.
    */
   String IF_MATCH = "If-Match";
   
   /**
    * If variant has not been modified since the time specified. 
    */
   String IF_MODIFIED_SINCE = "If-Modified-Since";
   
   /**
    * Verify that none of those entities is current by including a list.
    */
   String IF_NONE_MATCH = "If-None-Match";
   
   /**
    * If the entity is unchanged send me the part that I am missing.
    */
   String IF_RANGE = "If-Range";
   
   /**
    * If the requested resource has not been modified since this time. 
    */
   String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
   
   /**
    * Indicates the date and time at which the variant was last modified.
    */
   String LAST_MODIFIED = "Last-Modified";
   
   /**
    * Used to redirect the recipient to a location other than the URI. 
    */
   String LOCATION = "Location";
   
   /**
    * Limit the number of proxies or gateways that can forward the request. 
    */
   String MAX_FORWARDS = "Max-Forwards";
   
   /**
    * Include implementation specific directives that might apply. 
    */
   String PRAGMA = "Pragma";
   
   /**
    * Challenge indicating the authentication applicable to the proxy. 
    */
   String PROXY_AUTHENTICATE = "Proxy-Authenticate";
   
   /**
    * Allows client identification for a proxy requiring authentication.
    */
   String PROXY_AUTHORIZATION = "Proxy-Authorization";
   
   /**
    * Specifies a range of bytes within a resource to be sent by a server.
    */
   String RANGE = "Range";
   
   /**
    * Allows the client to specify the source address to the server. 
    */
   String REFERER = "Referer";
   
   /**
    * Response to indicate how long the service will be unavailable.
    */
   String RETRY_AFTER = "Retry-After";
   
   /**
    * Represents the globally unique identifier sent by the client.
    */
   String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
   
   /**
    * Represents the SHA-1 digest of the clients globally unique identifier.
    */
   String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
   
   /**
    * Specifies the protocol that should be used by the connected parties.
    */
   String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
   
   /**
    * Represents the version of the protocol that should be used.
    */
   String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
   
   /**
    * Contains information about the software used by the origin server. 
    */
   String SERVER = "Server";
   
   /**
    * Represents some value from the server that the client should keep.
    */
   String SET_COOKIE = "Set-Cookie";        
   
   /**
    * Indicates what extension transfer codings it is willing to accept.
    */
   String TE = "TE";
   
   /**
    * Indicates that these header fields is present in the trailer. 
    */
   String TRAILER = "Trailer";
   
   /**
    * Indicates the transformation has been applied to the message body. 
    */
   String TRANSFER_ENCODING = "Transfer-Encoding";
   
   /**
    * Specifies additional communication protocols the client supports. 
    */
   String UPGRADE = "Upgrade";
   
   /**
    * Contains information about the user agent originating the request.
    */
   String USER_AGENT = "User-Agent";
   
   /**
    * Indicates the headers that can make a cached resource stale.
    */
   String VARY = "Vary";
   
   /**
    * Used by gateways and proxies to indicate the intermediate protocols. 
    */
   String VIA = "Via";
   
   /**
    * Used to carry additional information about the status or body.
    */
   String WARNING = "Warning";
   
   /**
    * Uses to challenge a client for authentication for a resource.
    */
   String WWW_AUTHENTICATE = "WWW-Authenticate";
   
   /**
    * Represents a class of data representing an executable application.
    */
   String APPLICATION = "application";
   
   /**
    * Represents the token used to identify a multipart boundary.
    */
   String BOUNDARY = "boundary";
   
   /**
    * Represents the token used to identify the encoding of a message.
    */
   String CHARSET = "charset";
   
   /**
    * Represents the name of a self delimiting transfer encoding.
    */
   String CHUNKED = "chunked";
   
   /**
    * Specifies that the server will terminate the connection.
    */
   String CLOSE = "close";
   
   /**
    * Represents a message type for an image such as a PNG or JPEG.
    */
   String IMAGE = "image";
   
   /**      
    * Specifies that the server wishes to keep the connection open.
    */
   String KEEP_ALIVE = "keep-alive";
   
   /**
    * Represents a message type that contains multiple parts.
    */
   String MULTIPART = "multipart";  
   
   /**
    * Specifies that the message should not be cached by anything.
    */
   String NO_CACHE = "no-cache";
   
   /**
    * Represents the default content type if none is specified.
    */
   String OCTET_STREAM = "octet-stream";
   
   /**
    * Represents a message type containing human readable text.
    */
   String TEXT = "text";   
   
   /**
    * Represents a message type that contains HTML form posted data.
    */
   String URL_ENCODED = "x-www-form-urlencoded";
   
   /**
    * This is the protocol token that is used when upgrading.
    */
   String WEBSOCKET = "websocket";
}
