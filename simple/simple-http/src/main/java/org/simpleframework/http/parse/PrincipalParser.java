/*
 * PrincipalParser.java February 2001
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

import org.simpleframework.common.parse.ParseBuffer;
import org.simpleframework.common.parse.Parser;
import org.simpleframework.http.Principal;

/** 
 * PrincipalParser is a parser class for the HTTP basic authorization 
 * header. It decodes the <code>base64</code> encoding of the user and 
 * password pair. 
 * <p>
 * This follows the parsing tree of RFC 2617. The goal of this parser
 * is to decode the <code>base64</code> encoding of the user name and 
 * password. After the string has been decoded then the user name and 
 * password are extracted. This will only parse headers that are from 
 * the <code>Basic</code> authorization scheme. The format of the basic 
 * scheme can be found in RFC 2617 and is of the form 
 * <pre>
 *  Basic SP base64-encoding.
 * </pre>
 *
 * @author Niall Gallagher
 */
public class PrincipalParser extends Parser implements Principal {
   
   /** 
    * Keeps the characters consumed for the password token.
    */
   private ParseBuffer password;

   /** 
    * Keeps the characters consumed for the user name token.
    */ 
   private ParseBuffer user;

   /** 
    * Keeps the <code>bytes</code> used for decoding base64.
    */
   private byte[] four;

   /** 
    * Tracks the write offset for the buffer.
    */
   private int write;

   /** 
    * Tracks the ready offset for the four buffer.
    */   
   private int ready;
   
   /** 
    * Tracks the read offset for the buffer.
    */
   private int read;

   /** 
    * Creates a <code>Parser</code> for the basic authorization 
    * scheme. This allows headers that are of this scheme to be 
    * broken into its component parts i.e. user name and password.
    */
   public PrincipalParser() {
      this.password = new ParseBuffer();
      this.user = new ParseBuffer();
      this.four = new byte[4];
   }

   /** 
    * Creates a <code>Parser</code> for the basic authorization 
    * scheme.  This allows headers that are of this scheme to be 
    * broken into its component parts i.e. user name and password.
    * This constructor will parse the <code>String</code> given as 
    * the header.
    *
    * @param header this is a header value from the basic scheme
    */
   public PrincipalParser(String header){
      this();
      parse(header);
   }

   /** 
    * Gets the users password parsed from the Authorization
    * header value. If there was not password parsed from the
    * base64 value of the header this returns <code>null</code>
    *
    * @return the password for the user or <code>null</code>
    */ 
   public String getPassword(){
      if(password.length() == 0){
         return null;
      }
      return password.toString();  
   }

   /** 
    * Gets the users name from the Authorization header value.
    * This will return <code>null</code> if there is no user 
    * name extracted from the base64 header value.
    * 
    * @return this returns the name of the user
    */ 
   public String getName(){
      if(user.length() == 0){
         return null;
      }
      return user.toString();      
   }

   /** 
    * Used to parse the actual header data. This will attempt to 
    * read the "Basic" token from the set of characters given, if 
    * this is successful then the username and password is 
    * extracted.
    */
   protected void parse(){
      if(skip("Basic ")){
         decode(); 
         userpass();
      }
   }

   /** 
    * This will initialize the <code>Parser</code> when it is ready 
    * to parse a new <code>String</code>. This will reset the 
    * <code>Parser</code> to a ready state. The <code>init</code> method 
    * is invoked by the <code>Parser</code> when the <code>parse</code> 
    * method is invoked.
    */
   protected void init() {
      password.clear();
      user.clear();
      write = ready = 
      read = off = 0;
      pack();
   }

   /** 
    * This is used to remove all whitespace characters from the 
    * <code>String</code> excluding the whitespace within literals. 
    * The definition of a literal can be found in RFC 2616. 
    * <p>
    * The definition of a literal for RFC 2616 is anything between 2 
    * quotes but excuding quotes that are prefixed with the backward 
    * slash character.
    */
   private void pack() {
      int len = count;   
      int seek = 0;     /* read */
      int pos = 0;     /* write */
      char ch = 0;
  
      while(seek <len){ /* trim start*/
         if(!space(buf[seek])){
            break;
         }
         seek++;
      }
      while(seek < len){
         ch = buf[seek++];  
         if(space(ch)){         
            while(seek < len){ /* skip spaces */
               if(!space(buf[seek])){ 
                  break;
               }
               seek++;
            }        
         }
         buf[pos++] = ch;
      }
      if(space(ch)){  /* trim end */
         pos--;
      }
      count = pos;
   }

   /** 
    * Extracts the name and password of the user from the 
    * <code>name : password</code> pair that was given. This 
    * will take all data up to the first occurence of a 
    * ':' character as the users name and all data after the 
    * colon as the users password.
    */
   private void userpass(){
      userid();
      off++;
      password();
   }
   
   /** 
    * Extracts the user name from the buffer. This will read up to 
    * the first occurence of a colon, ':', character as the user 
    * name. For the BNF syntax of this see RFC 2617.
    */
   private void userid(){
      while(off < count){
         char ch = buf[off];
         if(!text(ch) || ch ==':'){
            break;
         }
         user.append(ch);
         off++;
      }

   }

   /** 
    * Extracts the password from the buffer. This will all characters 
    * from the current offset to the first non text character as the 
    * password. For the BNF syntax of this see RFC 2617.
    */
   private void password() {
      while(off < count){
         char ch = buf[off];
         if(!text(ch)){
            break;
         }
         password.append(ch);
         off++;
      }
   }
   
   /** 
    * This is used to remove decode the <code>base64</code> encoding of 
    * the user name and password. This uses a standart <code>base64</code> 
    * decoding scheme.
    * <p>
    * For information on the decoding scheme used for <code>base64</code> 
    * see the RFC 2045 on MIME, Multipurpose Internet Mail Extensions.
    */
   private void decode() {  
      for(write = read = off; read + 3 < count;) { 
         while(ready < 4) {
            int ch = translate(buf[read++]);
            if(ch >= 0) {
               four[ready++] = (byte)ch;
            }
         }
         if(four[2] == 65) {
            buf[write++] = first(four);
            break;
         } else if(four[3] == 65) {
            buf[write++] = first(four);
            buf[write++] = second(four);
            break;
         } else {
            buf[write++] = first(four);
            buf[write++] = second(four);
            buf[write++] = third(four);
         }
         ready = 0;              
      }  
      count = write;
   }

   /** 
    * This uses a basic translation from the <code>byte</code> character to the
    * <code>byte</code> number. 
    * <p>
    * The table for translation the data can be found  in RFC 2045 on 
    * MIME, Multipurpose Internet Mail Extensions.
    *
    * @param octet this is the octet ttat is to be translated
    * 
    * @return this returns the translated octet
    */
   private int translate(int octet) {     
      if(octet >= 'A' && octet <= 'Z') {
         octet = octet - 'A';
      } else if(octet >= 'a' && octet <= 'z') {
         octet = (octet - 'a') + 26;
      } else if(octet >= '0' && octet <= '9') {
         octet = (octet - '0') + 52;
      } else if(octet == '+') {
         octet = 62;
      } else if(octet == '/') {
         octet = 63;
      } else if(octet == '=') {
         octet = 65;
      } else {
         octet = -1;
      }
      return octet;
   }

   /** 
    * This is used to extract the <code>byte</code> from the set of four 
    * <code>bytes</code> given. This method is used to isolate the correct 
    * bits that corrospond to an actual character withing the 
    * <code>base64</code> data.
    *
    * @param four this is the four <code>bytes</code> that the character
    *    is to be extracted from
    *
    * @return this returns the character extracted
    */
   private char first(byte[] four) {
      return (char)(((four[0] & 0x3f) << 2) | ((four[1] & 0x30) >>> 4));      
   }

   /** 
    * This is used to extract the <code>byte</code> from the set of four 
    * <code>bytes</code> given. This method is used to isolate the correct 
    * bits that corrospond to an actual character withing the 
    * <code>base64</code> data.
    *
    * @param four this is the four <code>bytes</code> that the character
    *    is to be extracted from
    *
    * @return this returns the character extracted

    */
   private char second(byte[] four) {
      return (char)(((four[1] & 0x0f) << 4) | ((four[2] &0x3c) >>> 2));      
   }

   /** 
    * This is used to extract the <code>byte</code> from the set of four 
    * <code>bytes</code> given. This method is used to isolate the correct 
    * bits that corrospond to an actual character withing the 
    * <code>base64</code> data.
    *
    * @param four this is the four <code>bytes</code> that the character
    *    is to be extracted from
    *
    * @return this returns the character extracted
    */
   private char third(byte[] four) {
      return (char)(((four[2] & 0x03) << 6) | (four[3] & 0x3f));      
   }

   /** 
    * This is used to determine wheather or not a character is a 
    * <code>TEXT</code> character according to the HTTP specification, 
    * that is RFC 2616 specifies a <code>TEXT</code> character as one 
    * that is any octet except those less than 32 and not 127.
    *
    * @param c this is the character that is to be determined
    * 
    * @return this returns true if the character is a <code>TEXT</code>
    */
   private boolean text(char c){
      return c > 31 && c != 127 && c <= 0xffff;
   }
}
