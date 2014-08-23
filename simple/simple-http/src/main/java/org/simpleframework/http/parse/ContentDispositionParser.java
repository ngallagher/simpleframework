/*
 * ContentDispositionParser.java February 2007
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

package org.simpleframework.http.parse;

import org.simpleframework.common.parse.ParseBuffer;
import org.simpleframework.common.parse.Parser;
import org.simpleframework.http.ContentDisposition;

/**
 * The <code>ContentDispositionParser</code> object is used to represent
 * a parser used to parse the Content-Disposition header. Its used when
 * there is a multipart form upload to the server and allows the
 * server to determine the individual part types.
 * 
 * @author Niall Gallagher
 */
public class ContentDispositionParser extends Parser implements ContentDisposition {
   
   /**
    * This is the buffer used to acquire values from the header.
    */
   private ParseBuffer skip;
   
   /**
    * This is used to capture the name of the file if it is provided.
    */
   private ParseBuffer file;
   
   /**
    * This is used to capture the name of the part if it is provided.
    */
   private ParseBuffer name;
   
   /**
    * This is used to determine if the disposition is a file or form.
    */
   private boolean form;
   
   /**
    * Constructor for the <code>ContentDispositionParser</code> object. 
    * This is used to create a parser that can parse a disposition 
    * header which is typically sent as part of a multipart upload. It 
    * can be used to determine the type of the upload.
    */
   public ContentDispositionParser() {
      this.file = new ParseBuffer();
      this.name = new ParseBuffer();
      this.skip = new ParseBuffer();
   }
   
   /**
    * Constructor for the <code>ContentDispositionParser</code> object. 
    * This is used to create a parser that can parse a disposition header
    * which is typically sent as part of a multipart upload. It can
    * be used to determine the type of the upload.
    * 
    * @param text this is the header value that is to be parsed
    */   
   public ContentDispositionParser(String text) {
      this();
      parse(text);
   }
   
   /**
    * This method is used to acquire the file name of the part. This
    * is used when the part represents a text parameter rather than 
    * a file. However, this can also be used with a file part.
    *
    * @return this returns the file name of the associated part
    */   
   public String getFileName() {
      return file.toString();
   }
   
   /**
    * This method is used to acquire the name of the part. Typically
    * this is used when the part represents a text parameter rather
    * than a file. However, this can also be used with a file part.
    * 
    * @return this returns the name of the associated part
    */   
   public String getName() {
      return name.toString();
   }
   
   /**
    * This method is used to determine the type of a part. Typically
    * a part is either a text parameter or a file. If this is true
    * then the content represented by the associated part is a file.
    *
    * @return this returns true if the associated part is a file
    */   
   public boolean isFile() {
      return !form || file.length() > 0;
   }
   
   /** 
    * This will initialize the <code>Parser</code> when it is ready 
    * to parse a new <code>String</code>. This will reset the 
    * parser to a ready state. This method is invoked by the parser
    * before the parse method is invoked, it is used to pack the
    * contents of the header and clear any previous tokens used.
    */   
   protected void init() {
      if(count > 0) {
         pack();
      }
      clear();
   }
   /**
    * This is used to clear all previously collected tokens. This 
    * allows the parser to be reused when there are multiple source
    * strings to be parsed. Clearing of the tokens is performed 
    * when the parser is initialized.
    */
   protected void clear() {
      file.clear();
      name.clear();
      form = false;
      off = 0;
   }

   /** 
    * This is the method that should be implemented to read the 
    * buffer. This method will extract the type from the header and
    * the tries to extract the optional parameters if they are in
    * the header. The optional parts are the file name and name.
    */   
   protected void parse() {
      type();
      parameters();
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
      char old = buf[0];
      int len = count;
      int seek = 0;
      int pos = 0;

      while(seek < len){
         char ch = buf[seek++];
         
         if(ch == '"' && old != '\\'){  /* qd-text*/
            buf[pos++] = ch;
            
            while(seek < len){
               old = buf[seek-1];
               ch = buf[seek++];  
               buf[pos++] = ch;
               
               if(ch =='"'&& old!='\\'){  /*qd-text*/
                  break;
               }
            }
         }else if(!space(ch)){            
            old = buf[seek - 1];  
            buf[pos++] = old;                   
         }         
      }
      count = pos;
   }   
   
   /**
    * This is used to determine the type of the disposition header. This
    * will allow the parser to determine it the header represents form 
    * data or a file upload. Once it determines the type of the upload 
    * header it sets an internal flag which can be used.
    */
   private void type() {
      if(skip("form-data")) {
         form = true;
      } else if(skip("file")) {
         form = false;
      }
   }

   /** 
    * This will read the parameters from the header value. This will search 
    * for the <code>filename</code> parameter within the set of parameters 
    * which are given to the type. The <code>filename</code> param and the 
    * the <code>name</code> are tokenized by this method. 
    */   
   private void parameters(){   
      while(skip(";")){
         if(skip("filename=")){
            value(file);            
         } else {
            if(skip("name=")) {
               value(name);           
            } else {
               parameter();
            }
         }
      }
   }
   
   /** 
    * This will read the parameters from the header value. This will search 
    * for the <code>filename</code> parameter within the set of parameters 
    * which are given to the type. The <code>filename</code> param and the 
    * the <code>name</code> are tokenized by this method. 
    */    
   private void parameter() {
      name();
      off++;
      value(skip);
   }
   
   /** 
    * This will simply read all characters from the buffer before the first '=' 
    * character. This represents a parameter name (see RFC 2616 for token). The 
    * parameter name is not buffered it is simply read from the buffer. This will
    * not cause an <code>IndexOutOfBoundsException</code> as each offset
    * is checked before it is acccessed.
    */   
   private void name(){
      while(off < count){
         if(buf[off] =='='){
            break;
         }
         off++;
      }   
   }
   
   /** 
    * This is used to read a parameters value from the buf. This will read all 
    * <code>char</code>'s upto but excluding the first terminal <code>char</code> 
    * encountered from the off within the buf, or if the value is a literal 
    * it will read a literal from the buffer (literal is any data between 
    * quotes except if the quote is prefixed with a backward slash character). 
    * 
    * @param value this is the parse buffer to append the value to
    */   
   private void value(ParseBuffer value) {
      if(quote(buf[off])) {
         char quote = buf[off];

         for(off++; off < count;) {
            if(quote == buf[off]) {
               if(buf[++off - 2] != '\\') {
                  break;
               }
            }

            value.append(buf[off++]);
         }
      } else {
         while(off < count) {
            if(buf[off] == ';') {
               break;
            }

            value.append(buf[off]);
            off++;
         }
      }
   }  
   
   /**
    * This method is used to determine if the specified character is a quote
    * character. The quote character is typically used as a boundary for the
    * values within the header. This accepts a single or double quote.
    * 
    * @param ch the character to determine if it is a quotation
    * 
    * @return true if the character provided is a quotation character
    */   
   private boolean quote(char ch) {
      return ch == '\'' || ch == '"';
   }
}
