/*
 * AddressParser.java February 2001
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

package org.simpleframework.http.parse;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.parse.Parser;
import org.simpleframework.http.Address;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;

/**
 * This parser is used to parse uniform resource identifiers.
 * The uniform resource identifier syntax is given in RFC 2396.
 * This parser can parse relative and absolute URI's. The
 * uniform resource identifier syntax that this parser will
 * parse are based on the generic web based URL similar to
 * the syntax represented in RFC 2616 section 3.2.2. The syntax
 * used to parse this URI is a modified version of RFC 2396
 * <pre>
 *
 *    URI         = (absoluteURI | relativeURI)
 *    absoluteURI = scheme ":" ("//" netpath | relativeURI)
 *    relativeURI = path ["?" querypart]
 *    netpath     = domain [":" port] relativeURI
 *    path        = *("/" segment)
 *    segment     = *pchar *( ";" param )
 *
 * </pre>
 * This implements the <code>Address</code> interface and provides
 * methods that access the various parts of the URI. The parameters
 * in the path segments of the uniform resource identifier are
 * stored in name value pairs. If parameter names are not unique
 * across the path segments then only the deepest parameter will be
 * stored from the path segment. For example if the URI represented
 * was <code>http://domain/path1;x=y/path2;x=z</code> the value for
 * the parameter named <code>x</code> would be <code>z</code>.
 * <p>
 * This will normalize the path part of the uniform resource
 * identifier. A normalized path is one that contains no back
 * references like "./" and "../". The normalized path will not
 * contain the path parameters.
 * <p>
 * The <code>setPath</code> method is used to reset the path this
 * uniform resource identifier has, it also resets the parameters.
 * The parameters are extracted from the new path given.
 *
 * @author Niall Gallagher
 */
public class AddressParser extends Parser implements Address {

   /**
    * Parameters are stored so that the can be viewed.
    */
   private ParameterMap param;
   
   /**
    * This is the path used to represent the address path.
    */
   private Path normal;
   
   /**
    * This contains the query parameters for the address.
    */
   private Query data;

   /**
    * Used to track the characters that form the path.
    */
   private Token path;

   /**
    * Used to track the characters that form the domain.
    */
   private Token domain;

   /**
    * Used to track the characters that form the query.
    */
   private Token query;

   /**
    * Used to track the name characters of a parameter.
    */
   private Token name;

   /**
    * Used to track the value characters of a parameter.
    */
   private Token value;

   /**
    * References the scheme that this URI contains.
    */
   private Token scheme;

   /**
    * Contains the port number if it was specified.
    */
   private int port;

   /**
    * Default constructor will create a <code>AddressParser</code>
    * that contains no specifics. The instance will return
    * <code>null</code> for all the get methods. The parsers
    * get methods are populated by using the <code>parse</code>
    * method.
    */
   public AddressParser(){
      this.param = new ParameterMap();
      this.path = new Token();
      this.domain = new Token();
      this.query = new Token();
      this.scheme = new Token();
      this.name = new Token();
      this.value = new Token();
   }

   /**
    * This is primarily a convenience constructor. This will parse
    * the <code>String</code> given to extract the specifics. This
    * could be achieved by calling the default no-arg constructor
    * and then using the instance to invoke the <code>parse</code>
    * method on that <code>String</code> to extract the parts.
    *
    * @param text a <code>String</code> containing a URI value
    */
   public AddressParser(String text){
      this();
      parse(text);
   }

   /**
    * This allows the scheme of the URL given to be returned.
    * If the URI does not contain a scheme then this will
    * return null. The scheme of the URI is the part that
    * specifies the type of protocol that the URI is used
    * for, an example <code>gopher://domain/path</code> is
    * a URI that is intended for the gopher protocol. The
    * scheme is the string <code>gopher</code>.
    *
    * @return this returns the scheme tag for the URI if
    * there is one specified for it
    */
   public String getScheme(){
      return scheme.toString();
   }

   /**
    * This is used to retrieve the domain of this URI. The
    * domain part in the URI is an optional part, an example
    * <code>http://domain/path?querypart</code>. This will
    * return the value of the domain part. If there is no
    * domain part then this will return null otherwise the
    * domain value found in the uniform resource identifier.
    *
    * @return the domain part of this uniform resource
    * identifier this represents
    */
   public String getDomain(){
      return domain.toString();
   }

   /**
    * This is used to retrieve the path of this URI. The path part
    * is the most fundamental part of the URI. This will return
    * the value of the path. If there is no path part then this
    * will return <code>/</code> to indicate the root.
    * <p>
    * The <code>Path</code> object returned by this will contain
    * no path parameters. The path parameters are available using
    * the <code>Address</code> methods. The reason that this does not
    * contain any of the path parameters is so that if the path is
    * needed to be converted into an OS specific path then the path
    * parameters will not need to be separately parsed out.
    *
    * @return the path that this URI contains, this value will not
    * contain any back references such as "./" and "../" or any
    * path parameters
    */
   public Path getPath(){
      if(normal == null) {
         String text = path.toString();
         
         if(text == null) {
            normal = new PathParser("/");
         }
         if(normal == null){
            normal = new PathParser(text);
         }
      }
      return normal;
   }

   /**
    * This is used to retrieve the query of this URI. The query part
    * in the URI is an optional part. This will return the value
    * of the query part. If there is no query part then this will
    * return an empty <code>Query</code> object. The query is
    * an optional member of a URI and comes after the path part, it
    * is preceded by a question mark, <code>?</code> character.
    * For example the following URI contains <code>query</code> for
    * its query part, <code>http://host:port/path?query</code>.
    * <p>
    * This returns a <code>org.simpleframework.http.Query</code> 
    * object that can be used to interact directly with the query 
    * values. The <code>Query</code> object is a read-only interface
    * to the query parameters, and so will not affect the URI.
    *
    * @return a <code>Query</code> object for the query part
    */
   public Query getQuery(){
      if(data == null) {
         String text = query.toString();      
         
         if(text == null) {
            data = new QueryParser();
         }
         if(data == null){
            data = new QueryParser(text);
         }
      }
      return data;
   }

   /**
    * This is used to retrieve the port of the uniform resource
    * identifier. The port part in this is an optional part, an
    * example <code>http://host:port/path?querypart</code>. This
    * will return the value of the port. If there is no port then
    * this will return <code>-1</code> because this represents
    * an impossible uniform resource identifier port. The port
    * is an optional part.
    *
    * @return this returns the port of the uniform resource
    * identifier
    */
   public int getPort(){
      return port <= 0? -1 : port;
   }

   /**
    * This extracts the parameter values from the uniform resource
    * identifier represented by this object. The parameters that a
    * uniform resource identifier contains are embedded in the path
    * part of the URI. If the path contains no parameters then this
    * will return an empty <code>Map</code> instance.
    * <p>
    * This will produce unique name and value parameters. Thus if the
    * URI contains several path segments with similar parameter names
    * this will return the deepest parameter. For example if the URI
    * represented was <code>http://domain/path1;x=y/path2;x=z</code>
    * the value for the parameter named <code>x</code> would be
    * <code>z</code>.
    *
    * @return this will return the parameter names found in the URI
    */
   public KeyMap<String> getParameters(){
      return param;
   }

   /**
    * This allows the scheme for the URI to be specified.
    * If the URI does not contain a scheme then this will
    * attach the scheme and the <code>://</code> identifier
    * to ensure that the <code>Address.toString</code> will
    * produce the correct syntax.
    * <p>
    * Caution must be taken to ensure that the port and
    * the scheme are consistent. So if the original URI
    * was <code>http://domain:80/path</code> and the scheme
    * was changed to <code>ftp</code> the port number that
    * remains is the standard HTTP port not the FTP port.
    *
    * @param value this specifies the protocol this URI
    * is intended for
    */
   public void setScheme(String value){
      scheme.value = value;
   }

   /**
    * This will set the domain to whatever value is in the
    * string parameter. If the string is null then this URI
    * objects <code>toString</code> method will not contain
    * the domain. The result of the <code>toString</code>
    * method will be <code>/path/path?query</code>. If the
    * path is non-null this URI will contain the path.
    *
    * @param value this will be the new domain of this
    * uniform resource identifier, if it is not null
    */
   public void setDomain(String value){
      path.toString();      
      query.toString();
      scheme.toString();
      domain.clear();
      parseDomain(value); 
   }
   
   /**
    * This will set the domain to whatever value is in the
    * string parameter. If the string is null then this URI
    * objects <code>toString</code> method will not contain
    * the domain. The result of the <code>toString</code>
    * method will be <code>/path/path?query</code>. If the
    * path is non-null this URI will contain the path.
    *
    * @param value this will be the new domain of this
    * uniform resource identifier, if it is not null
    */
   private void parseDomain(String value){
      count = value.length();
      ensureCapacity(count);
      value.getChars(0, count, buf, 0);
      normal = null;
      off = 0;
      hostPort();
   }

   /**
    * This will set the port to whatever value it is given. If
    * the value is 0 or less then the <code>toString</code> will
    * will not contain the optional port. If port number is above
    * 0 then the <code>toString</code> method will produce a URI
    * like <code>http://host:123/path</code> but only if there is
    * a valid domain.
    *
    * @param port the port value that this URI is to have
    */
   public void setPort(int port) {
      this.port = port;
   }

   /**
    * This will set the path to whatever value it is given. If the
    * value is null then this <code>Address.toString</code> method will
    * not contain the path, that is if path is null then it will be
    * interpreted as <code>/</code>.
    * <p>
    * This will reset the parameters this URI has. If the value
    * given to this method has embedded parameters these will form
    * the parameters of this URI. The value given may not be the
    * same value that the <code>getPath</code> produces. The path
    * will have all back references and parameters stripped.
    *
    * @param text the path that this URI is to be set with
    */
   public void setPath(String text) {
      if(!text.startsWith("/")){
         text = "/" + text;
      }
      domain.toString();
      query.toString();
      scheme.toString();
      param.clear();
      path.clear();
      parsePath(text); /*extract params*/
   }

   /**
    * This will set the path to whatever value it is given. If the
    * value is null then this <code>Address.toString</code> method
    * will not contain the path, that is if path is null then it will 
    * be interpreted as <code>/</code>.
    * <p>
    * This will reset the parameters this URI has. If the value
    * given to this method has embedded parameters these will form
    * the parameters of this URI. The value given may not be the
    * same value that the <code>getPath</code> produces. The path
    * will have all back references and parameters stripped.
    *
    * @param path the path that this URI is to be set with
    */
   public void setPath(Path path) {
      if(path != null){
         normal = path;
      }else {
         setPath("/");
      }
   }

   /**
    * This is used to parse the path given with the <code>setPath</code>
    * method. The path contains name and value pairs. These parameters
    * are embedded into the path segments using a semicolon character,
    * ';'. Since the parameters to not form part of the actual path
    * mapping they are removed from the path and stored. Each parameter
    * can then be extracted from this parser using the methods provided
    * by the <code>Address</code> interface.
    *
    * @param path this is the path that is to be parsed and have the
    * parameter values extracted
    */
   private void parsePath(String path){
      count = path.length();
      ensureCapacity(count);
      path.getChars(0, count, buf, 0);
      normal = null;
      off = 0;
      path();
   }

   /**
    * This will set the query to whatever value it is given. If the
    * value is null then this <code>Address.toString</code> method 
    * will not contain the query. If the query was <code>abc</code> 
    * then the <code>toString</code> method would produce a string 
    * like <code>http://host:port/path?abc</code>. If the query is 
    * null this URI would have no query part. The query must not 
    * contain the <code>?</code> character.
    *
    * @param value the query that this uniform resource identifier
    * is to be set to if it is non-null
    */
   public void setQuery(String value) {
      query.value = value;
      data = null;
   }

   /**
    * This will set the query to whatever value it is given. If the
    * value is null then this <code>Address.toString</code> method 
    * will not contain the query. If the <code>Query.toString</code>
    * returns null then the query will be empty. This is basically
    * the <code>setQuery(String)</code> method with the string value
    * from the issued <code>Query.toString</code> method.
    *
    * @param query a <code>Query</code> object that contains
    * the name value parameters for the query
    */
   public void setQuery(Query query) {
      if(value != null) {
         data = query;
      }else {
         setQuery("");
      }
   }

   /**
    * This will check to see what type of URI this is if it is an
    * <code>absoluteURI</code> or a <code>relativeURI</code>. To
    * see the definition of a URI see RFC 2616 for the definition
    * of a URL and for more specifics see RFC 2396 for the
    * expressions.
    */
   protected void parse(){
      if(count > 0){
         if(buf[0] == '/'){
            relativeURI();
         }else{
            absoluteURI();
         }
      }
   }

   /**
    * This will empty each tokens cache. A tokens cache is used
    * to represent a token once the token's <code>toString</code>
    * method has been called. Thus when the <code>toString</code>
    * method is called then the token depends on the value of the
    * cache alone in further calls to <code>toString</code>.
    * However if a URI has just been parsed and that method has
    * not been invoked then the cache is created from the buf if
    * its length is greater than zero.
    */
   protected void init(){
      param.clear();
      domain.clear();
      path.clear();
      query.clear();
      scheme.clear();
      off =port = 0;
      normal = null;
      data = null;
   }

   /**
    * This is a specific definition of a type of URI. An absolute
    * URI is a URI that contains a host and port. It is the most
    * frequently used type of URI. This will define the host and
    * the optional port part. As well as the relative URI part.
    * This uses a simpler syntax than the one specified in RFC 2396
    * <code><pre>
    *
    *    absoluteURI = scheme ":" ("//" netpath | relativeURI)
    *    relativeURI = path ["?" querypart]
    *    netpath     = domain [":" port] relativeURI
    *    path        = *("/" segment)
    *    segment     = *pchar *( ";" param )
    *
    * </pre></code>
    * This syntax is sufficient to handle HTTP style URI's as well
    * as GOPHER and FTP and various other 'simple' schemes. See
    * RFC 2396 for the syntax of an <code>absoluteURI</code>.
    */
   private void absoluteURI(){
      scheme();
      netPath();
   }

   /**
    * This will check to see if there is a scheme in the URI. If
    * there is a scheme found in the URI this returns true and
    * removes that scheme tag of the form "ftp:" or "http:"
    * or whatever the protocol scheme tag may be for the URI.
    * <p>
    * The syntax for the scheme is given in RFC 2396 as follows
    * <code><pre>
    *
    *    scheme = alpha *( alpha | digit | "+" | "-" | "." )
    *
    * </pre></code>
    * This will however also skips the "://" from the tag
    * so of the URI was <code>gopher://domain/path</code> then
    * the URI would be <code>domain/path</code> afterwards.
    */
   private void scheme(){
      int mark = off;
      int pos = off;

      if(alpha(buf[off])){
         while(off < count){
            char next = buf[off++];

            if(schemeChar(next)){
               pos++;
            }else if(next == ':'){
               if(!skip("//")) {
                  off = mark;
                  pos = mark;
               }
               break;
            }else{
               off = mark;
               pos = mark;
               break;
            }
         }
         scheme.len = pos - mark;
         scheme.off = mark;
      }
   }

   /**
    * This method is used to assist the scheme method. This will
    * check to see if the type of the character is the same as
    * those described in RFC 2396 for a scheme character. The
    * scheme tag can contain an alphanumeric of the following
    * <code>"+", "-", "."</code>.
    *
    * @param c this is the character that is being checked
    *
    * @return this returns true if the character is a valid
    * scheme character
    */
   private boolean schemeChar(char c){
      switch(c){
      case '+': case '-':
      case '.':
         return true;
      default:
         return alphanum(c);
      }
   }

   /**
    * The network path is the path that contains the network
    * address of the host that this URI is targeted at. This
    * will parse the domain name of the host and also a port
    * number before parsing a relativeURI
    * <code><pre>
    *
    *    netpath     = domain [":" port] relativeURI
    *
    * </pre></code>
    * This syntax is modified from the URI specification on
    * RFC 2396.
    */
   private void netPath(){
      hostPort();
      relativeURI();
   }
   
   /**
    * This is used to extract the host and port combination.
    * Typically a URI will not explicitly specify a port, however
    * if there is a semicolon at the end of the domain it should
    * be interpreted as the port part of the URI.
    */
   private void hostPort() {
      domain();
      if(skip(":")){
         port();
      }
   }

   /**
    * This is a specific definition of a type of URI. A relative
    * URI is a URI that contains no host or port. It is basically
    * the resource within the host. This will extract the path and
    * the optional query part of the URI. Rfc2396 has the proper
    * definition of a <code>relativeURI</code>.
    */
   private void relativeURI(){
      path();
      if(skip("?")){
         query();
      }
   }

   /**
    * This is used to extract the optional port from a given URI.
    * This will read a sequence of digit characters and convert
    * the <code>String</code> of digit characters into a decimal
    * number. The digits will be added to the port variable. If
    * there is no port number this will not update the read offset.
    */
   private void port() {
      while(off < count){
         if(!digit(buf[off])){
            break;
         }
         port *= 10;
         port += buf[off];
         port -= '0';
         off++;
      }
   }

   /**
    * This is used to extract the domain from the given URI. This
    * will  firstly initialize the token object that represents the
    * domain. This allows the token's <code>toString</code> method to
    * return the extracted value of the token rather than getting
    * confused with previous values set by a previous parse method.
    * <p>
    * This uses the following delimiters to determine the end of the
    * domain <code>?</code>,<code>:</code> and <code>/<code>. This
    * ensures that the read offset does not go out of bounds and
    * consequently throw an <code>IndexOutOfBoundsException</code>.
    */
   private void domain(){
      int mark = off;

      loop: while(off < count){
         switch(buf[off]){
         case '/': case ':':
         case '?':
            break loop;
         default:
            off++;
         }
      }
      domain.len = off - mark;
      domain.off = mark;
   }

   /**
    * This is used to extract the segments from the given URI. This
    * will firstly initialize the token object that represents the
    * path. This allows the token's <code>toString</code> method to
    * return the extracted value of the token rather than getting
    * confused with previous values set by a previous parse method.
    * <p>
    * This is slightly different from RFC 2396 in that it defines a
    * pchar as the RFC 2396 definition of a pchar without the escaped
    * chars. So this method has to ensure that no escaped chars go
    * unchecked. This ensures that the read offset does not go out
    * of bounds and throw an <code>IndexOutOfBoundsException</code>.
    */
   private void path(){
      int mark = off;
      int pos = off;

      while(skip("/")) {
         buf[pos++] = '/';

         while(off < count){
            if(buf[off]==';'){
               while(skip(";")){
                  param();
                  insert();
               }
               break;
            }
            if(buf[off]=='%'){
               escape();
            }else if(!pchar(buf[off])){
               break;
            }
            buf[pos++]=buf[off++];
         }
      }
      path.len = pos -mark;
      path.off = mark;
   }

   /**
    * This is used to extract the query from the given URI. This
    * will firstly initialize the token object that represents the
    * query. This allows the token's <code>toString</code> method
    * to return the extracted value of the token rather than getting
    * confused with previous values set by a previous parse method.
    * The calculation of the query part of a URI is basically the
    * end of the URI.
    */
   private void query() {
      query.len = count - off;
      query.off = off;
   }

   /**
    * This is an expression that is defined by RFC 2396 it is used
    * in the definition of a segment expression. This is basically
    * a list of pchars.
    * <p>
    * This method has to ensure that no escaped chars go unchecked.
    * This ensures that the read offset does not goe out of bounds
    * and consequently throw an out of bounds exception.
    */
   private void param() {
      name();
      if(skip("=")){ /* in case of error*/
         value();
      }
   }

   /**
    * This extracts the name of the parameter from the character
    * buffer. The name of a parameter is defined as a set of
    * pchars including escape sequences. This will extract the
    * parameter name and buffer the chars. The name ends when a
    * equals character, "=", is encountered or in the case of a
    * malformed parameter when the next character is not a pchar.
    */
   private void name(){
      int mark = off;
      int pos = off;

      while(off < count){
         if(buf[off]=='%'){ /* escaped */
            escape();
         }else if(buf[off]=='=') {
            break;
         }else if(!pchar(buf[off])){
            break;
         }
         buf[pos++] = buf[off++];
      }
      name.len = pos - mark;
      name.off = mark;
   }

   /**
    * This extracts a parameter value from a path segment. The
    * parameter value consists of a sequence of pchars and some
    * escape sequences. The parameter value is buffered so that
    * the name and values can be paired. The end of the value
    * is determined as the end of the buffer or the last pchar.
    */
   private void value(){
      int mark = off;
      int pos = off;

      while(off < count){
         if(buf[off]=='%'){ /* escaped */
            escape();
         }else if(!pchar(buf[off])) {
            break;
         }
         buf[pos++] = buf[off++];
      }
      value.len = pos - mark;
      value.off = mark;
   }

   /**
    * This method adds the name and value to a map so that the next
    * name and value can be collected. The name and value are added
    * to the map as string objects. Once added to the map the
    * <code>Token</code> objects are set to have zero length so they
    * can be reused to collect further values. This will add the
    * values to the map as an array of type string. This is done so
    * that if there are multiple values that they can be stored.
    */
   private void insert(){
      if(value.length() > 0){
         if(name.length() > 0)
            insert(name,value);
      }
      name.clear();
      value.clear();
   }

   /**
    * This will add the given name and value to the parameters map.
    * This will only store a single value per parameter name, so
    * only the parameter that was latest encountered will be saved.
    * The <code>getQuery</code> method can be used to collect
    * the parameter values using the parameter name.
    *
    * @param name this is the name of the value to be inserted
    * @param value this is the value of a that is to be inserted
    */
   private void insert(Token name, Token value){
      insert(name.toString(), value.toString());
   }

   /**
    * This will add the given name and value to the parameters map.
    * This will only store a single value per parameter name, so
    * only the parameter that was latest encountered will be saved.
    * The <code>getQuery</code> method can be used to collect
    * the parameter values using the parameter name.
    *
    * @param name this is the name of the value to be inserted
    * @param value this is the value of a that is to be inserted
    */
   private void insert(String name, String value) {
      param.put(name, value);
   }

   /**
    * This converts an encountered escaped sequence, that is all
    * embedded hexidecimal characters into a native UCS character
    * value. This does not take any characters from the stream it
    * just prepares the buffer with the correct byte. The escaped
    * sequence within the URI will be interpreded as UTF-8.
    * <p>
    * This will leave the next character to read from the buffer
    * as the character encoded from the URI. If there is a fully
    * valid escaped sequence, that is <code>"%" HEX HEX</code>.
    * This decodes the escaped sequence using UTF-8 encoding, all
    * encoded sequences should be in UCS-2 to fit in a Java char.
    */
   private void escape() {
      int peek = peek(off);

      if(!unicode(peek)) {
         binary(peek);
      }
   }

   /**
    * This method determines, using a peek character, whether the
    * sequence of escaped characters within the URI is binary data.
    * If the data within the escaped sequence is binary then this
    * will ensure that the next character read from the URI is the
    * binary octet. This is used strictly for backward compatible
    * parsing of URI strings, binary data should never appear.
    *
    * @param peek this is the first escaped character from the URI
    *
    * @return currently this implementation always returns true
    */
   private boolean binary(int peek) {
      if(off + 2 < count) {
         off += 2;
         buf[off]= bits(peek);
      }
      return true;
   }

   /**
    * This method determines, using a peek character, whether the
    * sequence of escaped characters within the URI is in UTF-8. If
    * a UTF-8 character can be successfully decoded from the URI it
    * will be the next character read from the buffer. This can
    * check for both UCS-2 and UCS-4 characters. However, because
    * the Java <code>char</code> can only hold UCS-2, the UCS-4
    * characters will have only the low order octets stored.
    * <p>
    * The WWW Consortium provides a reference implementation of a
    * UTF-8 decoding for Java, in this the low order octets in the
    * UCS-4 sequence are used for the character. So, in the
    * absence of a defined behaviour, the W3C behaviour is assumed.
    *
    * @param peek this is the first escaped character from the URI
    *
    * @return this returns true if a UTF-8 character is decoded
    */
   private boolean unicode(int peek) {
      if((peek & 0x80) == 0x00){
         return unicode(peek, 0);
      }
      if((peek & 0xe0) == 0xc0){
         return unicode(peek & 0x1f, 1);
      }
      if((peek & 0xf0) == 0xe0){
         return unicode(peek & 0x0f, 2);
      }
      if((peek & 0xf8) == 0xf0){
         return unicode(peek & 0x07, 3);
      }
      if((peek & 0xfc) == 0xf8){
         return unicode(peek & 0x03, 4);
      }
      if((peek & 0xfe) == 0xfc){
         return unicode(peek & 0x01, 5);
      }
      return false;
   }

   /**
    * This method will decode the specified amount of escaped
    * characters from the URI and convert them into a single Java
    * UCS-2 character. If there are not enough characters within
    * the URI then this will return false and leave the URI alone.
    * <p>
    * The number of characters left is determined from the first
    * UTF-8 octet, as specified in RFC 2279, and because this is
    * a URI there must that number of <code>"%" HEX HEX</code>
    * sequences left. If successful the next character read is
    * the UTF-8 sequence decoded into a native UCS-2 character.
    *
    * @param peek contains the bits read from the first UTF octet
    * @param more this specifies the number of UTF octets left
    *
    * @return this returns true if a UTF-8 character is decoded
    */
   private boolean unicode(int peek, int more) {
      if(off + more * 3 >= count) {
         return false;
      }
      return unicode(peek,more,off);
   }

   /**
    * This will decode the specified amount of trailing UTF-8 bits
    * from the URI. The trailing bits are those following the first
    * UTF-8 octet, which specifies the length, in octets, of the
    * sequence. The trailing octets are if the form 10xxxxxx, for
    * each of these octets only the last six bits are valid UCS
    * bits. So a conversion is basically an accumulation of these.
    * <p>
    * If at any point during the accumulation of the UTF-8 bits
    * there is a parsing error, then parsing is aborted an false
    * is returned, as a result the URI is left unchanged.
    *
    * @param peek bytes that have been accumulated from the URI
    * @param more this specifies the number of UTF octets left
    * @param pos this specifies the position the parsing begins
    *
    * @return this returns true if a UTF-8 character is decoded
    */
   private boolean unicode(int peek, int more, int pos) {
      while(more-- > 0) {
         if(buf[pos] == '%'){
            int next = pos + 3;
            int hex = peek(next);

            if((hex & 0xc0) == 0x80){
               peek = (peek<<6)|(hex&0x3f);
               pos = next;
               continue;
            }
         }
         return false;
      }
      if(pos + 2 < count) {
         off = pos + 2;
         buf[off]= bits(peek);
      }
      return true;
   }

   /**
    * Defines behaviour for UCS-2 versus UCS-4 conversion from four
    * octets. The UTF-8 encoding scheme enables UCS-4 characters to
    * be encoded and decodeded. However, Java supports the 16-bit
    * UCS-2 character set, and so the 32-bit UCS-4 character set is
    * not compatable. This basically decides what to do with UCS-4.
    *
    * @param data up to four octets to be converted to UCS-2 format
    *
    * @return this returns a native UCS-2 character from the int
    */
   private char bits(int data) {
      return (char)data;
   }

   /**
    * This will return the escape expression specified from the URI
    * as an integer value of the hexidecimal sequence. This does
    * not make any changes to the buffer it simply checks to see if
    * the characters at the position specified are an escaped set
    * characters of the form <code>"%" HEX HEX</code>, if so, then
    * it will convert that hexidecimal string  in to an integer
    * value, or -1 if the expression is not hexidecimal.
    *
    * @param pos this is the position the expression starts from
    *
    * @return the integer value of the hexidecimal expression
    */
   private int peek(int pos) {
      if(buf[pos] == '%'){
         if(count <= pos + 2) {
            return -1;
         }
         char high = buf[pos + 1];
         char low = buf[pos + 2];

         return convert(high, low);
      }
      return -1;
   }

   /**
    * This will convert the two hexidecimal characters to a real
    * integer value, which is returned. This requires characters
    * within the range of 'A' to 'F' and 'a' to 'f', and also
    * the digits '0' to '9'. The characters encoded using the
    * ISO-8859-1 encoding scheme, if the characters are not with
    * in the range specified then this returns -1.
    *
    * @param high this is the high four bits within the integer
    * @param low this is the low four bits within the integer
    *
    * @return this returns the indeger value of the conversion
    */
   private int convert(char high, char low) {
      int hex = 0x00;

      if(hex(high) && hex(low)){
         if('A' <= high && high <= 'F'){
            high -= 'A' - 'a';
         }
         if(high >= 'a') {
            hex ^= (high-'a')+10;
         } else {
            hex ^= high -'0';
         }
         hex <<= 4;

         if('A' <= low && low <= 'F') {
            low -= 'A' - 'a';
         }
         if(low >= 'a') {
            hex ^= (low-'a')+10;
         } else {
            hex ^= low-'0';
         }
         return hex;
      }
      return -1;
   }

   /**
    * This is used to determine wheather a char is a hexidecimal
    * <code>char</code> or not. A hexidecimal character is consdered
    * to be a character within the range of <code>0 - 9</code> and
    * between <code>a - f</code> and <code>A - F</code>. This will
    * return <code>true</code> if the character is in this range.
    *
    * @param ch this is the character which is to be determined here
    *
    * @return true if the character given has a hexidecimal value
    */
   private boolean hex(char ch) {
      if(ch >= '0' && ch <= '9') {
         return true;
      } else if(ch >='a' && ch <= 'f') {
         return true;
      } else if(ch >= 'A' && ch <= 'F') {
         return true;
      }
      return false;
   }

   /**
    * This is a character set defined by RFC 2396 it is used to
    * determine the valididity of certain <code>chars</code>
    * within a Uniform Resource Identifier. RFC 2396 defines
    * an unreserved char as <code>alphanum | mark</code>.
    *
    * @param c the character value that is being checked
    *
    * @return true if the character has an unreserved value
    */
   private boolean unreserved(char c){
      return mark(c) || alphanum(c);
   }

   /**
    * This is used to determine wheather or not a given unicode
    * character is an alphabetic character or a digit character.
    * That is withing the range <code>0 - 9</code> and between
    * <code>a - z</code> it uses <code>iso-8859-1</code> to
    * compare the character.
    *
    * @param c the character value that is being checked
    *
    * @return true if the character has an alphanumeric value
    */
   private boolean alphanum(char c){
      return digit(c) || alpha(c);
   }

   /**
    * This is used to determine wheather or not a given unicode
    * character is an alphabetic character. This uses encoding
    * <code>iso-8859-1</code> to compare the characters.
    *
    * @param c the character value that is being checked
    *
    * @return true if the character has an alphabetic value
    */
   private boolean alpha(char c){
      return (c <= 'z' && 'a' <= c) ||
       (c <= 'Z' && 'A' <= c);
   }

   /**
    * This is a character set defined by RFC 2396 it checks
    * the valididity of cetain chars within a uniform resource
    * identifier. The RFC 2396 defines a mark char as <code>"-",
    * "_", ".", "!", "~", "*", "'", "(", ")"</code>.
    *
    * @param c the character value that is being checked
    *
    * @return true if the character is a mark character
    */
   private boolean mark(char c){
      switch(c){
      case '-': case '_': case '.':
      case '!': case '~': case '*':
      case '\'': case '(': case ')':
         return true;
      default:
         return false;
      }
   }

   /**
    * This is a character set defined by RFC 2396 it is used to check
    * the valididity of cetain chars within a generic uniform resource
    * identifier. The RFC 2396 defines a pchar char as unreserved or
    * escaped or one of the following characters <code>":", "@", "=",
    * "&amp;", "+", "$", ","</code> this will not check to see if the
    * char is an escaped char, that is <code>% HEX HEX</code>. Because
    * this takes 3 chars.
    *
    * @param c the character value that is being checked
    *
    * @return true if the character is a pchar character
    */
   private boolean pchar(char c){
      switch(c){
      case '@': case '&': case '=':
      case '+': case '$': case ',':
      case ':':
         return true;
      default:
         return unreserved(c);
      }
   }

   /**
    * This is a character set defined by RFC 2396, it checks the
    * valididity of certain chars in a uniform resource identifier.
    * The RFC 2396 defines a reserved char as <code>";", "/", "?",
    * ":", "@", "&amp;", "=", "+", "$", ","</code>.
    *
    * @param c the character value that is being checked
    *
    * @return true if the character is a reserved character
    */   
   private boolean reserved(char c){
      switch(c){
      case ';': case '/': case '?':
      case '@': case '&': case ':':
      case '=': case '+': case '$':
      case ',':
         return true;
      default:
         return false;
      }
   }

   /**
    * This is used to convert this URI object into a <code>String</code>
    * object. This will only convert the parts of the URI that exist, so
    * the URI may not contain the domain or the query part and it will
    * not contain the path parameters. If the URI contains all these
    * parts then it will return somthing like
    * <pre>
    * scheme://host:port/path/path?querypart
    * </pre>
    * <p>
    * It can return <code>/path/path?querypart</code> style relative
    * URI's. If any of the parts are set to null then that part will be
    * missing, for example if <code>setDomain</code> method is invoked
    * with a null parameter then the domain and port will be missing
    * from the resulting URI. If the path part is set to null using the
    * <code>setPath</code> then the path will be <code>/</code>. An
    * example URI with the path part of null would be
    * <pre>
    * scheme://host:port/?querypart
    * </pre>
    *
    * @return the URI with only the path part and the non-null optional
    * parts of the uniform resource identifier
    */
   public String toString() {
      return (scheme.length() > 0 ? scheme +"://": "") +
      (domain.length() > 0 ? domain +
       (port > 0 ? ":"+port : "") : "")+ getPath() +
         (param.size() > 0 ? param  : "")+ 
         (query.length()>0?"?"+query :"");
   }
   
   /**
    * The <code>ParameterMap</code> is uses to store the parameters 
    * that are to be encoded in to the address. This will append all
    * of the parameters to the end of the path. These can later be
    * extracted by parsing the address.
    * 
    * @author Niall Gallagher
    */
   private class ParameterMap extends KeyMap<String> {
      
      /** 
       * This will return the parameters encoded in such a way that
       * it can be appended to the end of the path. These parameters
       * can be added to the address such that they do not form a
       * query parameter. Values such as session identifiers are 
       * often added as the path parameters to the address.
       * 
       * @return this returns the representation of the parameters
       */
      private String encode() {
         StringBuilder text = new StringBuilder();
         
         for(String name : param) {
            String value = param.get(name);
            
            text.append(";");
            text.append(name);
            
            if(value != null) {
               text.append("=");
               text.append(value);;
            }
         }
         return text.toString();
      }
      
      /** 
       * This will return the parameters encoded in such a way that
       * it can be appended to the end of the path. These parameters
       * can be added to the address such that they do not form a
       * query parameter. Values such as session identifiers are 
       * often added as the path parameters to the address.
       * 
       * @return this returns the representation of the parameters
       */
      public String toString() {
         return encode();
      }
   }

   /**
    * This is used as an alternative to the <code>ParseBuffer</code>
    * for extracting tokens from the URI without allocating memory.
    * This will basically mark out regions within the buffer which are
    * used to represent the token. When the token value is required
    * the region is used to create a <code>String</code> object.
    */
   private class Token {

      /**
       * This can be used to override the value for this token.
       */
      public String value;

      /**
       * This represents the start offset within the buffer.
       */
      public int off;

      /**
       * This represents the number of charters in the token.
       */
      public int len;

      /**
       * If the <code>Token</code> is to be reused this will clear
       * all previous data. Clearing the buffer allows it to be
       * reused if there is a new URI to be parsed. This ensures
       * that a null is returned if the token length is zero.
       */
      public void clear() {
         value = null;
         len = 0;
      }

      /**
       * This is used to determine the number of characters this
       * token contains. This is used rather than accessing the
       * length directly so that the value the token represents
       * can be overridden easily without upsetting the token.
       *
       * @return this returns the number of characters this uses
       */
      public int length() {
         if(value == null){
            return len;
         }
         return value.length();
      }

      /**
       * This method will convert the <code>Token</code> into it's
       * <code>String</code> equivelant. This will firstly check
       * to see if there is a value, for the string representation,
       * if there is the value is returned, otherwise the region
       * is converted into a <code>String</code> and returned.
       *
       * @return this returns a value representing the token
       */
      public String toString() {
         if(value != null) {
            return value;
         }
         if(len > 0) {
            value = new String(buf,off,len);
         }
         return value;
      }
   }
}
