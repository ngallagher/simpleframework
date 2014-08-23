/*
 * QueryParser.java December 2002
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
  
package org.simpleframework.http.parse;

import org.simpleframework.common.parse.MapParser;
import org.simpleframework.http.Query;

import java.net.URLEncoder;
import java.util.Set;

/**
 * The <code>ParameterParser</code> is used to parse data encoded in 
 * the <code>application/x-www-form-urlencoded</code> MIME type. It 
 * is also used to parse a query string from a HTTP URL, see RFC 2616.
 * The parsed parameters are available through the various methods of
 * the <code>org.simpleframework.http.net.Query</code> interface. The
 * syntax of the parsed parameters is described below in BNF.
 * <pre>
 *
 *    params  = *(pair [ "&amp;" params])
 *    pair    = name "=" value
 *    name    = *(text | escaped)
 *    value   = *(text | escaped)
 *    escaped = % HEX HEX
 *
 * </pre> 
 * This will consume all data found as a name or value, if the data 
 * is a "+" character then it is replaced with a space character.
 * This regards only "=", "&amp;", and "%" as having special values.
 * The "=" character delimits the name from the value and the "&amp;"
 * delimits the name value pair. The "%" character represents the 
 * start of an escaped sequence, which consists of two hex digits.
 * All escaped sequences are converted to its character value. 
 *
 * @author Niall Gallagher
 */
public class QueryParser extends MapParser<String> implements Query {

   /**
    * Used to accumulate the characters for the parameter name.
    */
   private Token name;
   
   /**
    * Used to accumulate the characters for the parameter value.
    */
   private Token value;

   /**
    * Constructor for the <code>ParameterParser</code>. This creates
    * an instance that can be use to parse HTML form data and URL
    * query strings encoded as application/x-www-form-urlencoded.
    * The parsed parameters are made available through the interface
    * <code>org.simpleframework.util.net.Query</code>.    
    */
   public QueryParser(){
      this.name = new Token();
      this.value = new Token();
   }
   
   /**
    * Constructor for the <code>ParameterParser</code>. This creates
    * an instance that can be use to parse HTML form data and URL
    * query strings encoded as application/x-www-form-urlencoded.
    * The parsed parameters are made available through the interface
    * <code>org.simpleframework.util.net.Query</code>.    
    *
    * @param text this is the text to parse for the parameters
    */
   public QueryParser(String text){
      this();
      parse(text);
   }

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
   public int getInteger(Object name) {
      String value = get(name);

      if(value != null) {
         return Integer.parseInt(value);      
      }
      return 0;
   }

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
   public float getFloat(Object name) {
      String value = get(name);

      if(value != null) {
         return Float.parseFloat(value);      
      }
      return 0.0f;
   }

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
   public boolean getBoolean(Object name) {
      Boolean flag = Boolean.FALSE;           
      String value = get(name);
           
      if(value != null) {         
         flag = Boolean.valueOf(value);
      }
      return flag.booleanValue();
   }

   
   /**
    * This initializes the parser so that it can be used several
    * times. This clears any previous parameters extracted. This
    * ensures that when the next <code>parse(String)</code> is
    * invoked the status of the <code>Query</code> is empty.
    */
   protected void init(){
      all.clear();
      map.clear();
      name.len = 0;
      value.len = 0;
      off = 0;
   }

   /**
    * This performs the actual parsing of the parameter text. The
    * parameters parsed from this are taken as "name=value" pairs.
    * Multiple pairs within the text are separated by an "&amp;".
    * This will parse and insert all parameters into a hashtable.
    */
   protected void parse() {            
      param();
      while(skip("&")){         
         param();
      }
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
      if(name.len > 0){
         insert(name,value);
      }
      name.len = 0;
      value.len = 0;
   }

   /**
    * This will add the given name and value to the parameters map.
    * If any previous value of the given name has been inserted
    * into the map then this will overwrite that value. This is
    * used to ensure that the string value is inserted to the map.
    *
    * @param name this is the name of the value to be inserted
    * @param value this is the value of a that is to be inserted
    */
   private void insert(Token name, Token value){
      put(name.toString(), value.toString());
   }
   
   /** 
    * This is an expression that is defined by RFC 2396 it is used
    * in the definition of a segment expression. This is basically
    * a list of chars with escaped sequences. 
    * <p>
    * This method has to ensure that no escaped chars go unchecked. 
    * This ensures that the read offset does not go out of bounds 
    * and consequently throw an out of bounds exception.  
    */
   private void param() {
      name(); 
      if(skip("=")){ /* in case of error*/
         value();
      }
      insert();      
   }    
   
   /**
    * This extracts the name of the parameter from the character 
    * buffer. The name of a parameter is defined as a set of 
    * chars including escape sequences. This will extract the
    * parameter name and buffer the chars. The name ends when a
    * equals character, "=", is encountered.
    */
   private void name(){  
      int mark = off;
      int pos = off;

      while(off < count){         
         if(buf[off]=='%'){ /* escaped */         
            escape();
         }else if(buf[off]=='=') {
            break;
         }else if(buf[off]=='+'){
            buf[off] = ' ';
         }
         buf[pos++] = buf[off++];         
      }          
      name.len = pos - mark;
      name.off = mark;
   }

   /**
    * This extracts a parameter value from a path segment. The
    * parameter value consists of a sequence of chars and some
    * escape sequences. The parameter value is buffered so that
    * the name and values can be paired. The end of the value 
    * is determined as the end of the buffer or an ampersand.
    */
   private void value(){
      int mark = off;
      int pos = off;

      while(off < count){         
         if(buf[off]=='%'){ /* escaped */         
            escape();
         }else if(buf[off]=='+'){
            buf[off] = ' ';
         }else if(buf[off]=='&'){
            break;
         }
         buf[pos++] = buf[off++];
      }              
      value.len = pos - mark;
      value.off = mark; 
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
         buf[off] =bits(peek);
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
    * sequence. The trailing octets are of the form 10xxxxxx, for
    * each of these octets only the last six bits are valid UCS
    * bits. So a conversion is basically an accumulation of these.
    * <p>
    * If at any point during the accumulation of the UTF-8 bits
    * there is a parsing error, then parsing is aborted an false
    * is returned, as a result the URI is left unchanged.
    *
    * @param peek bytes that have been accumulated fron the URI
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
    * as an integer value of the hexadecimal sequence. This does
    * not make any changes to the buffer it simply checks to see if
    * the characters at the position specified are an escaped set 
    * characters of the form <code>"%" HEX HEX</code>, if so, then
    * it will convert that hexadecimal string  in to an integer 
    * value, or -1 if the expression is not hexadecimal.
    *
    * @param pos this is the position the expression starts from
    *
    * @return the integer value of the hexadecimal expression
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
    * This is used to determine whether a char is a hexadecimal
    * <code>char</code> or not. A hexadecimal character is considered 
    * to be a character within the range of <code>0 - 9</code> and 
    * between <code>a - f</code> and <code>A - F</code>. This will 
    * return <code>true</code> if the character is in this range.
    *
    * @param ch this is the character which is to be determined here
    *
    * @return true if the character given has a hexadecimal value
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
    * This <code>encode</code> method will escape the text that
    * is provided. This is used to that the parameter pairs can
    * be encoded in such a way that it can be transferred over
    * HTTP/1.1 using the ISO-8859-1 character set.
    *
    * @param text this is the text that is to be escaped
    *
    * @return the text with % HEX HEX UTF-8 escape sequences
    */ 
   private String encode(String text) {
      try {           
         return URLEncoder.encode(text, "UTF-8");           
      }catch(Exception e){
         return text;              
      }         
   }

   /**
    * This <code>encode</code> method will escape the name=value
    * pair provided using the UTF-8 character set. This method
    * will ensure that the parameters are encoded in such a way
    * that they can be transferred via HTTP in ISO-8859-1.
    *
    * @param name this is the name of that is to be escaped
    * @param value this is the value that is to be escaped
    *
    * @return the pair with % HEX HEX UTF-8 escape sequences
    */ 
   private String encode(String name, String value) {
      return encode(name) + "=" + encode(value);           
   }
   
   /**
    * This <code>toString</code> method is used to compose an string
    * in the <code>application/x-www-form-urlencoded</code> MIME type.
    * This will encode the tokens specified in the <code>Set</code>.
    * Each name=value pair acquired is converted into a UTF-8 escape
    * sequence so that the parameters can be sent in the IS0-8859-1
    * format required via the HTTP/1.1 specification RFC 2616.
    * 
    * @param set this is the set of parameters to be encoded
    * 
    * @return returns a HTTP parameter encoding for the pairs
    */ 
   public String toString(Set set) {
      Object[] list = set.toArray();
      String text = "";
      
      for(int i = 0; i < list.length; i++){
         String name = list[i].toString();
         String value = get(name);
         
         if(i > 0) {
            text += "&";                 
         }              
         text += encode(name, value);
      }  
      return text;    
   }

   /**
    * This <code>toString</code> method is used to compose an string
    * in the <code>application/x-www-form-urlencoded</code> MIME type.
    * This will iterate over all tokens that have been added to this
    * object, either during parsing, or during use of the instance.
    * Each name=value pair acquired is converted into a UTF-8 escape
    * sequence so that the parameters can be sent in the IS0-8859-1
    * format required via the HTTP/1.1 specification RFC 2616.
    * 
    * @return returns a HTTP parameter encoding for the pairs
    */ 
   public String toString() {
      Set set = map.keySet();
   
      if(map.size() > 0) {      
         return toString(set);
      }
      return "";      
   }
   
   /**
    * This is used to mark regions within the buffer that represent
    * a valid token for either the name of a parameter or its value.
    * This is used as an alternative to the <code>ParseBuffer</code>
    * which requires memory to be allocated for storing the data
    * read from the buffer. This requires only two integer values.
    */
   private class Token {
      
      /**
       * This represents the number of characters in the token.
       */
      public int len;

      /**
       * This represents the start offset within the buffer.
       */  
      public int off;

      /**
       * In order to represent the <code>Token</code> as a value
       * that can be used this converts it to a <code>String</code>.
       * If the length of the token is less than or equal to zero
       * this will return and empty string for the value.
       *
       * @return this returns a value representing the token
       */
      public String toString() {      
         if(len <= 0) {
            return "";
         }
         return new String(buf,off,len);
      }
   }
}
