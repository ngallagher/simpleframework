/*
 * ParseBuffer.java February 2001
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
 
package org.simpleframework.common.parse;

/** 
 * This is primarily used to replace the <code>StringBuffer</code> 
 * class, as a way for the <code>Parser</code> to store the char's
 * for a specific region within the parse data that constitutes a 
 * desired value. The methods are not synchronized so it enables 
 * the <code>char</code>'s to be taken quicker than the 
 * <code>StringBuffer</code> class.
 *
 * @author Niall Gallagher
 */
public class ParseBuffer {      

   /** 
    * This is used to quicken <code>toString</code>.
    */
   protected String cache;

   /** 
    * The <code>char</code>'s this buffer accumulated.
    */
   protected char[] buf;

   /** 
    * This is the number of <code>char</code>'s stored.
    */
   protected int count;
   
   /** 
    * Constructor for <code>ParseBuffer</code>. The default 
    * <code>ParseBuffer</code> stores 16 <code>char</code>'s 
    * before a <code>resize</code> is needed to accommodate
    * extra characters. 
    */
   public ParseBuffer(){
      this(16);
   }
   
   /** 
    * This creates a <code>ParseBuffer</code> with a specific 
    * default size. The buffer will be created the with the 
    * length specified. The <code>ParseBuffer</code> can grow 
    * to accommodate a collection of <code>char</code>'s larger 
    * the the size specified.
    *
    * @param size initial size of this <code>ParseBuffer</code>
    */
   public ParseBuffer(int size){
      this.buf = new char[size];
   }
   
   /** 
    * This will add a <code>char</code> to the end of the buffer.
    * The buffer will not overflow with repeated uses of the 
    * <code>append</code>, it uses an <code>ensureCapacity</code>
    * method which will allow the buffer to dynamically grow in 
    * size to accommodate more <code>char</code>'s.
    *
    * @param c the <code>char</code> to be appended
    */
   public void append(char c){
      ensureCapacity(count+ 1);
      buf[count++] = c;
   }

   /** 
    * This will add a <code>String</code> to the end of the buffer.
    * The buffer will not overflow with repeated uses of the 
    * <code>append</code>, it uses an <code>ensureCapacity</code> 
    * method which will allow the buffer to dynamically grow in 
    * size to accommodate large <code>String</code> objects.
    *
    * @param text the <code>String</code> to be appended to this
    */  
   public void append(String text){
      ensureCapacity(count+ text.length());
      text.getChars(0,text.length(),buf,count);
      count += text.length();
   }
   
   /**
    * This will reset the buffer in such a way that the buffer is
    * cleared of all contents and then has the given string appended.
    * This is used when a value is to be set into the buffer value.
    * See the <code>append(String)</code> method for reference.
    * 
    * @param text this is the text that is to be appended to this
    */
   public void reset(String text) {      
      clear();      
      append(text);
   }

   /** 
    * This will add a <code>ParseBuffer</code> to the end of this.
    * The buffer will not overflow with repeated uses of the 
    * <code>append</code>, it uses an <code>ensureCapacity</code> 
    * method which will allow the buffer to dynamically grow in 
    * size to accommodate large <code>ParseBuffer</code> objects.
    *
    * @param text the <code>ParseBuffer</code> to be appended 
    */  
   public void append(ParseBuffer text){
      append(text.buf, 0, text.count);           
   }
   
   /**
    * This will reset the buffer in such a way that the buffer is
    * cleared of all contents and then has the given string appended.
    * This is used when a value is to be set into the buffer value.
    * See the <code>append(ParseBuffer)</code> method for reference.
    * 
    * @param text this is the text that is to be appended to this
    */
   public void reset(ParseBuffer text) {      
      clear();      
      append(text);
   }
   /** 
    * This will add a <code>char</code> to the end of the buffer.
    * The buffer will not overflow with repeated uses of the 
    * <code>append</code>, it uses an <code>ensureCapacity</code> 
    * method which will allow the buffer to dynamically grow in 
    * size to accommodate large <code>char</code> arrays.
    *
    * @param c the <code>char</code> array to be appended to this
    * @param off the read offset for the array    
    * @param len the number of <code>char</code>'s to add
    */   
   public void append(char[] c, int off, int len){
      ensureCapacity(count+ len);
      System.arraycopy(c,off,buf,count,len);
      count+=len;
   }
   
   /** 
    * This will add a <code>String</code> to the end of the buffer.
    * The buffer will not overflow with repeated uses of the 
    * <code>append</code>, it uses an <code>ensureCapacity</code>
    * method which will allow the buffer to dynamically grow in 
    * size to accommodate large <code>String</code> objects.
    *
    * @param str the <code>String</code> to be appended to this
    * @param off the read offset for the <code>String</code>
    * @param len the number of <code>char</code>'s to add
    */   
   public void append(String str, int off, int len){
      ensureCapacity(count+ len);
      str.getChars(off,len,buf,count);  
      count += len;
   }


   /** 
    * This will add a <code>ParseBuffer</code> to the end of this.
    * The buffer will not overflow with repeated uses of the 
    * <code>append</code>, it uses an <code>ensureCapacity</code> 
    * method which will allow the buffer to dynamically grow in 
    * size to accommodate large <code>ParseBuffer</code> objects.
    *
    * @param text the <code>ParseBuffer</code> to be appended 
    * @param off the read offset for the <code>ParseBuffer</code>
    * @param len the number of <code>char</code>'s to add
    */  
   public void append(ParseBuffer text, int off, int len){
      append(text.buf, off, len);           
   }   
   
   /** 
    * This ensure that there is enough space in the buffer to 
    * allow for more <code>char</code>'s to be added. If
    * the buffer is already larger than min then the buffer 
    * will not be expanded at all.
    *
    * @param min the minimum size needed
    */     
   protected void ensureCapacity(int min) {
      if(buf.length < min) {
         int size = buf.length * 2;
         int max = Math.max(min, size);
         char[] temp = new char[max];         
         System.arraycopy(buf, 0, temp, 0, count); 
         buf = temp;
      }
   }  
   
   /** 
    * This will empty the <code>ParseBuffer</code> so that the
    * <code>toString</code> parameter will return <code>null</code>. 
    * This is used so that the same <code>ParseBuffer</code> can be 
    * recycled for different tokens.
    */
   public void clear(){
      cache = null;
      count = 0;
   }
  
   /** 
    * This will return the number of bytes that have been appended 
    * to the <code>ParseBuffer</code>. This will return zero after 
    * the clear method has been invoked.
    *
    * @return the number of <code>char</code>'s within the buffer
    */
   public int length(){
      return count;
   }

   /** 
    * This will return the characters that have been appended to the 
    * <code>ParseBuffer</code> as a <code>String</code> object.
    * If the <code>String</code> object has been created before then
    * a cached <code>String</code> object will be returned. This
    * method will return <code>null</code> after clear is invoked.
    *
    * @return the <code>char</code>'s appended as a <code>String</code>
    */
   public String toString(){
      if(count <= 0) {
         return null;
      }
      if(cache != null) {
         return cache;
      }
      cache = new String(buf,0,count);
      return cache;
   }
}   
