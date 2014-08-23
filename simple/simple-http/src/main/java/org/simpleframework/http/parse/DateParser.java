/*
 * DateParser.java February 2001
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

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

import java.util.Calendar;
import java.util.TimeZone;

import org.simpleframework.common.parse.Parser;

/** 
 * This is used to create a <code>Parser</code> for the HTTP date format. 
 * This will parse the 3 formats that are acceptable for the HTTP/1.1 date. 
 * The three formats that are acceptable for the HTTP-date are  
 * <pre>
 * Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
 * Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
 * Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
 * </pre>
 * <p>
 * This can also parse the date in ms as retrived from the <code>System</code>'s 
 * <code>System.currentTimeMillis</code> method. This has a parse method for a 
 * <code>long</code> which will do the same as the <code>parse(String)</code>. 
 * Once the date has been parsed there are two methods that allow the date 
 * to be represented, the <code>toLong</code> method converts the date to a 
 * <code>long</code> and the <code>toString</code> method will convert the date 
 * into a <code>String</code>.
 * <p>
 * This produces the same string as the <code>SimpleDateFormat.format</code> 
 * using the pattern <code>"EEE, dd MMM yyyy hh:mm:ss 'GMT'"</code>. This will
 * however do the job faster as it does not take arbitrary inputs.
 *
 * @author Niall Gallagher
 */
public class DateParser extends Parser {

   /**
    * Ensure that the time zone for dates if set to GMT. 
    */
   private static final TimeZone ZONE = TimeZone.getTimeZone("GMT");

   /** 
    * Contains the possible days of the week for RFC 1123.
    */
   private static final String WKDAYS[] = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

   /** 
    * Contains the possible days of the week for RFC 850.
    */
   private static final String WEEKDAYS[] = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

   /** 
    * Contains the possible months in the year for HTTP-date.
    */
   private static final String MONTHS[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
  
   /** 
    * Used as an index into the months array to get the month.
    */
   private int month;
      
   /** 
    * Represents the decimal value of the date such as 1977.
    */
   private int year;
   
   /** 
    * Represents the decimal value of the date such as 18.
    */
   private int day;
   
   /** 
    * Used as an index into the weekdays array to get the weekday.
    */
   private int weekday;
   
   /** 
    * Represents the decimal value of the hour such as 24.
    */
   private int hour;

   /** 
    * Represents the decimal value of the minute.
    */
   private int mins;
   
   /** 
    * Represents the decimal value for the second.
    */
   private int secs;

   /** 
    * The default constructor will create a parser that can parse 
    * <code>String</code>s that contain dates in the form of RFC 1123, 
    * RFC 850 or asctime. If the dates that are to be parsed are not in 
    * the form of one of these date encodings the results of this 
    * parser will be random. 
    */
   public DateParser(){
      this.init();         
   }  
 
   /** 
    * This constructor will conveniently parse the <code>long</code> argument 
    * in the constructor. This can also  be done by first calling the no-arg 
    * constructor and then using the parse method. 
    * <p>
    * This will then set this object to one that uses the RFC 1123 format 
    * for a date. 
    *
    * @param date the date to be parsed
    */ 
   public DateParser(long date){
      this();
      parse(date);
   }

   /** This constructor will conveniently parse the <code>String</code> 
   * argument in the constructor. This can also be done by first calling 
   * the no-arg constructor and then using the parse method. 
    * <p> 
    * This will then set this object to one that uses the RFC 1123 format
    * for a date.
    * 
    * @param date the date to be parsed
    */   
   public DateParser(String date)  {      
      this();
      parse(date);
   } 

   /** 
    * This is used to extract the date from a <code>long</code>. If this
    * method is given the value of the date as a <code>long</code> it will
    * construct the RFC 1123 date as required by RFC 2616 sec 3.3.
    * <p>
    * This saves time on parsing a <code>String</code> that is encoded in 
    * the HTTP-date format. The date given must be positive, if the date 
    * given is not a positive '<code>long</code>' then the results
    * of this method is random/unknown.
    * 
    * @param date the date to be parsed
    */   
   public void parse(long date){
      Calendar calendar = Calendar.getInstance(ZONE);
      calendar.setTimeInMillis(date); 
      
      weekday = calendar.get(DAY_OF_WEEK);
      year = calendar.get(YEAR);
      month = calendar.get(MONTH);
      day = calendar.get(DAY_OF_MONTH);
      hour = calendar.get(HOUR_OF_DAY);
      mins = calendar.get(MINUTE);
      secs = calendar.get(SECOND);
      month = month > 11 ? 11: month;
      weekday = (weekday+5) % 7;
   }
   
   /**
    * Convenience method used to convert the specified HTTP date in to a 
    * long representing the time. This is used when a single method is
    * required to convert a HTTP date format to a usable long value for
    * use in creating <code>Date</code> objects.
    * 
    * @param date the date specified in on of the HTTP date formats
    * 
    * @return the date value as a long value in milliseconds
    */
   public long convert(String date) {
      parse(date);
      return toLong();
      
   }
   
   /**
    * Convenience method used to convert the specified long date in to a
    * HTTP date format. This is used when a single method is required to
    * convert a long data value in milliseconds to a HTTP date value. 
    * 
    * @param date the date specified as a long of milliseconds
    * 
    * @return the date represented in the HTTP date format RFC 1123
    */
   public String convert(long date) {
      parse(date);
      return toString();
   }

   /** 
    * This is used to reset the date and the buffer variables 
    * for this <code>DateParser</code>. Every in is set to the 
    * value of 0.
    */
   protected void init() {
      month = year = day = 
      weekday = hour = mins = 
      secs = off = 0;
   }

   /** 
    * This is used to parse the contents of the <code>buf</code>. This 
    * checks the fourth char of the buffer to see what it contains. Invariably 
    * a date format belonging to RFC 1123 will have a ',' character in position 4, 
    * a date format belonging to asctime will have a ' ' character in position 4 
    * and if neither of these characters are found at position 4 then it is 
    * assumed that the date is in the RFC 850 fromat, however it may not be.
    */
   protected void parse(){
      if(buf.length<4)return;
      if(buf[3]==','){
         rfc1123();
      }else if(buf[3]==' '){
         asctime();
      }else{
         rfc850();
      } 
   }

   /** 
    * This will parse a date that is in the form of an RFC 1123 date. This 
    * date format is the date format that is to be used with all applications 
    * that are HTTP/1.1 compliant. The RFC 1123 date format is  
    * <pre>
    * rfc1123 = 'wkday "," SP date1 SP time SP GMT'. 
    * date1 = '2DIGIT SP month SP 4DIGIT' and finally
    * time = '2DIGIT ":" 2DIGIT ":" 2DIGIT'. 
    * </pre>
    */
   private void rfc1123(){
      wkday();
      off+=2;
      date1();
      off++;
      time();      
   }  
 
   /** 
    * This will parse a date that is in the form of an RFC 850 date. This date 
    * format is the date format that is to be used with some applications that 
    * are HTTP/1.0 compliant. The RFC 1123 date format is  
    * <pre>
    * rfc850 = 'weekday "," SP date2 SP time SP GMT'. 
    * date2 = '2DIGIT "-" month "-" 2DIGIT' and finally
    * time = '2DIGIT ":" 2DIGIT ":" 2DIGIT'. 
    * </pre>
    */
   private void rfc850() {
      weekday();
      off+=2;
      date2();
      off++;
      time();
   }

   /** 
    * This will parse a date that is in the form of an asctime date. This date 
    * format is the date format that is to be used with some applications that 
    * are HTTP/1.0 compliant. The RFC 1123 date format is  
    * <pre>
    * asctime = 'weekday SP date3 SP time SP 4DIGIT'. 
    * date3 = 'month SP (2DIGIT | (SP 1DIGIT))' and 
    * time = '2DIGIT ":" 2DIGIT ":" 2DIGIT'. 
    * </pre>
    */
   private void asctime(){
      wkday();
      off++;
      date3();
      off++;
      time();
      off++;
      year4();
   }

   /** 
    * This is the date1 format of a date that is used by the RFC 1123
    * date format. This date is
    * <pre>
    * date1 = '2DIGIT SP month SP 4DIGIT'.
    * example '02 Jun 1982'.
    * </pre>
    */
   private void date1(){
      day();
      off++;
      month();
      off++;
      year4();
   }

   /** 
    * This is the date2 format of a date that is used by the RFC 850 
    * date format. This date is
    * <pre>
    * date2 = '2DIGIT "-" month "-" 2DIGIT'
    * example '02-Jun-82'.
    * </pre>
    */   
   private void date2(){
      day();
      off++;
      month();
      off++;
      year2();
   }

   /** 
    * This is the date3 format of a date that is used by the asctime 
    * date format. This date is
    * <pre>
    * date3 = 'month SP (2DIGIT | (SP 1DIGIT))' 
    * example 'Jun  2'.
    * <pre>
    */
   private void date3(){
      month();
      off++;
      day();
   }

   /** 
    * This is used to parse a consecutive set of digit characters to create 
    * the day of the week. This will tolerate a space on front of the digits 
    * thiswill allow all date formats including asctime to use this to get 
    * the day. This may parse more than 2 digits, however if there are more 
    * than 2 digits the date format is incorrect anyway.
    */
   private void day(){
      if(space(buf[off])){
         off++;
      }
      while(off < count){
         if(!digit(buf[off])){
            break;
         }
         day *= 10;
         day += buf[off];
         day -= '0';
         off++;
      }    
   }

   /** 
    * This is used to get the year from a set of digit characters. This is 
    * used to parse years that are of the form of 2 digits (e.g 82) however 
    * this will assume that any dates that are in 2 digit format are dates 
    * for the 2000 th milleneum so 01 will be 2001.
    * <p>
    * This may parse more than 2 digits but if there are more than 2 digits
    *  in a row then the date format is incorrect anyway.
    */
   private void year2(){      
      int mill = 2000;  /* milleneum */
      int cent = 0;   /* century */
      while(off < count){
         if(!digit(buf[off])){            
            break;
         }
         cent *= 10;
         cent += buf[off];
         cent -= '0';
         off++;
      }  
      year= mill+cent; /* result 4 digits*/
   }

   /** 
    * This is used to get the year from a set of digit characters. This 
    * is used to parse years that are of the form of 4 digits (e.g 1982).
    * <p>
    * This may parse more than 4 digits but if there are more than 2  
    * digits in a row then the date format is incorrect anyway.
    */
   private void year4() {
      while(off < count){
         if(!digit(buf[off])){
            break;
         }
         year *= 10;
         year += buf[off];
         year -= '0';
         off++;
      }      
   }

   /** 
    * This is used to parse the time for a HTTP-date. The time for a 
    * HTTP-date is in the form <code>00:00:00</code> that is
    * <pre>
    * time = '2DIGIT ":" 2DIGIT ":" 2DIGIT' so this will
    * read only a time of that form, although this will
    * parse time = '2DIGIT CHAR 2DIGIT CHAR 2DIGIT'.
    * </pre>
    */
   private void time(){
      hours();
      off++;
      mins();
      off++;
      secs();
   }

   /** 
    * This is used to initialize the hour. This will read a consecutive 
    * sequence of digit characters and convert them into a decimal number 
    * to represent the hour that this date represents. 
    * <p>
    * This may parse more than 2 digits but if there are more than 2 
    * digits the date is already incorrect.
    */
   private void hours(){
      while(off < count){
         if(!digit(buf[off])){
            break;
         }
         hour *= 10;
         hour += buf[off];
         hour -= '0';
         off++;
      }
   }

   /** 
    * This is used to initialize the mins. This will read a consecutive 
    * sequence of digit characters and convert them into a decimal number 
    * to represent the mins that  this date represents. 
    * <p>
    * This may parse more than 2 digits but if there are more than 2 
    * digits the date is already incorrect.
    */
   private void mins(){
      while(off < count){
         if(!digit(buf[off])){
            break;
         }
         mins *= 10;
         mins += buf[off];
         mins -= '0';
         off++;
      }
   }

   /** 
    * This is used to initialize the secs. This will read a consecutive 
    * sequence of digit characters and convert them into a decimal 
    * number to represent the secs that this date represents. 
    * <p>
    * This may parse more than 2 digits but if there are more than 2 
    * digits the date is already incorrect
    */
   private void secs(){
      while(off < count){
         if(!digit(buf[off])){
            break;
         }
         secs *= 10;
         secs += buf[off];
         secs -= '0';
         off++;
      }
   }

   /** 
    * This is used to read the week day of HTTP-date. The shorthand day 
    * (e.g Mon for Monday) is used by the RFC 1123 and asctime date formats. 
    * This will simply try to read each day from the buffer, when the day
    * is read successfully then the index of that day is saved.
    */    
   private void wkday(){
      for(int i =0; i < WKDAYS.length;i++){
         if(skip(WKDAYS[i])){
            weekday = i;
            return;
         }      
      }
   }

   /** 
    * This is used to read the week day of HTTP-date. This format is used 
    * by the RFC 850 date format. This will simply try to read each day from 
    * the buffer, when the day is read successfully then the index of that 
    * day is saved.
    */ 
   private void weekday(){
      for(int i =0; i < WKDAYS.length;i++){
         if(skip(WEEKDAYS[i])){
            weekday = i;
            return;
         }      
      }     
   }

   /** 
    * This is used to read the month of HTTP-date. This will simply 
    * try to read each month from the buffer, when the month is read 
    * successfully then the index of that month is saved.
    */
   private void month(){
      for(int i =0; i < MONTHS.length;i++){
         if(skip(MONTHS[i])){
            month = i;
            return;
         }      
      } 
   }
   
   /**
    * This is used to append the date in RFC 1123 format to the given
    * string builder. This will append the date and a trailing space
    * character to the buffer. Dates like the following are appended. 
    * <pre>
    * Tue, 02 Jun 1982 
    * </pre>. 
    * For performance reasons a string builder is used. This avoids 
    * an unneeded synchronization caused by the string buffers.
    * 
    * @param builder this is the builder to append the date to
    */
   private void date(StringBuilder builder) {
      builder.append(WKDAYS[weekday]);
      builder.append(", ");
      
      if(day <= 9) {
         builder.append('0');
      }
      builder.append(day);
      builder.append(' ');
      builder.append(MONTHS[month]);
      builder.append(' ');
      builder.append(year);
      builder.append(' ');
   }
   
   /**
    * This is used to append the time in RFC 1123 format to the given
    * string builder. This will append the time and a trailing space
    * character to the buffer. Times like the following are appended. 
    * <pre>
    * 23:59:59
    * </pre>. 
    * For performance reasons a string builder is used. This avoids 
    * an unneeded synchronization caused by the string buffers. 
    *
    * @param builder this is the builder to write the time to
    */
   private void time(StringBuilder builder) {
      if(hour <= 9) {
         builder.append('0');
      }
      builder.append(hour);
      builder.append(':');
      
      if(mins <= 9) {
         builder.append('0');
      }
      builder.append(mins);
      builder.append(':');
      
      if(secs <= 9) {
         builder.append('0');
      }
      builder.append(secs);
      builder.append(' ');
   }
   
   /**
    * This is used to append the time zone to the provided appender.
    * For HTTP the dates should always be in GMT format. So this will
    * simply append the "GMT" string to the end of the builder.
    * 
    * @param builder this builder to append the time zone to
    */
   private void zone(StringBuilder builder) {
      builder.append("GMT");
   }

   /** 
    * This returns the date in as a <code>long</code>, given the exact 
    * time this will use the <code>java.util.Date</code> to parse this date 
    * into a <code>long</code>. The <code>GregorianCalendar</code> uses 
    * the method <code>getTime</code> which produces the <code>Date</code>
    * object from this the <code>getTime</code> returns the <code>long</code>
    *
    * @return the date parsed as a <code>long</code>
    */
   public long toLong() {
      Calendar calendar = Calendar.getInstance(ZONE); /* GMT*/
      calendar.set(year,month, day, hour, mins, secs);
      calendar.set(MILLISECOND, 0);

      return calendar.getTime().getTime();   
   }

   /** 
    * This prints the date in the format of a RFC 1123 date. Example
    * <pre>
    * Tue, 02 Jun 1982 23:59:59 GMT
    * </pre>. 
    * This uses a <code>StringBuffer</code> to accumulate the various 
    * <code>String</code>s/<code>int</code>s to form the resulting date 
    * value. The resulting date value is the one required by RFC 2616. 
    * <p>
    * The HTTP date must be in the form of RFC 1123. The hours, minutes 
    * and seconds are appended with the 0 character if they are less than 
    * 9 i.e. if they do not have two digits.
    *
    * @return the date in RFC 1123 format
    */
   public String toString(){
      StringBuilder builder = new StringBuilder(30);
      
      date(builder);
      time(builder);
      zone(builder);
      
      return builder.toString();
   }
}
