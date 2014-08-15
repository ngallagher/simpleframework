/*
 * Cookie.java February 2001
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * This class is used to represent a generic cookie. This exposes
 * the fields that a cookie can have. By default the version of the 
 * <code>Cookie</code> is set to 1. The version can be configured 
 * using the <code>setVersion</code> method. The domain, path, 
 * security, and expiry of the cookie can also be set using their 
 * respective set methods.
 * <p>
 * The <code>toString</code> method allows the <code>Cookie</code>
 * to be converted back into text form. This text form converts the
 * cookie according to the Set-Cookie header form. This is done so
 * that a created <code>Cookie</code> instance can be converted
 * to a string which can be used as a a HTTP header.
 *
 * @author Niall Gallagher
 */
public class Cookie {

   /**
    * This is used to set the expiry date for the cookie.
    */
   private CookieDate date;
   
   /**
    * The name attribute of this cookie instance.
    */
   private String name;

   /**
    * The value attribute of this cookie instance.
    */
   private String value;

   /**
    * Represents the value of the path for this cookie.
    */
   private String path;

   /**
    * Represents the value of the domain attribute.
    */
   private String domain;

   /**
    * Determines whether the cookie should be secure.
    */
   private boolean secure;

   /**
    * Determines whether the cookie should be protected.
    */
   private boolean protect;
   
   /**
    * This is used to determine the the cookie is new.
    */
   private boolean created;
   
   /**
    * Represents the value of the version attribute.
    */
   private int version;

   /**
    * Represents the duration in seconds of the cookie.
    */
   private int expiry;   

   /**
    * Constructor of the <code>Cookie</code> that uses a default
    * version of 1, which is used by RFC 2109. This contains none
    * of the optional attributes, such as domain and path. These
    * optional attributes can be set using the set methods.
    * <p>
    * The name must conform to RFC 2109, which means that it can
    * contain only ASCII alphanumeric characters and cannot have
    * commas, white space, or semicolon characters.
    *
    * @param name this is the name of this cookie instance
    * @param value this is the value of this cookie instance
    */
   public Cookie(String name, String value) {
      this(name, value, "/");
   }
   
   /**
    * Constructor of the <code>Cookie</code> that uses a default
    * version of 1, which is used by RFC 2109. This contains none
    * of the optional attributes, such as domain and path. These
    * optional attributes can be set using the set methods.
    * <p>
    * The name must conform to RFC 2109, which means that it can
    * contain only ASCII alphanumeric characters and cannot have
    * commas, white space, or semicolon characters.
    *
    * @param name this is the name of this cookie instance
    * @param value this is the value of this cookie instance
    * @param created this determines if the cookie is new 
    */
   public Cookie(String name, String value, boolean created) {
      this(name, value, "/", created);
   }

   /**
    * Constructor of the <code>Cookie</code> that uses a default
    * version of 1, which is used by RFC 2109. This allows the
    * path attribute to be specified for on construction. Other
    * attributes can be set using the set methods provided.
    * <p>
    * The name must conform to RFC 2109, which means that it can
    * contain only ASCII alphanumeric characters and cannot have
    * commas, white space, or semicolon characters.
    *
    * @param name this is the name of this cookie instance
    * @param value this is the value of this cookie instance
    * @param path the path attribute of this cookie instance
    */
   public Cookie(String name, String value, String path) {
      this(name, value, path, false);
   }
   
   /**
    * Constructor of the <code>Cookie</code> that uses a default
    * version of 1, which is used by RFC 2109. This allows the
    * path attribute to be specified for on construction. Other
    * attributes can be set using the set methods provided.
    * <p>
    * The name must conform to RFC 2109, which means that it can
    * contain only ASCII alphanumeric characters and cannot have
    * commas, white space, or semicolon characters.
    *
    * @param name this is the name of this cookie instance
    * @param value this is the value of this cookie instance
    * @param path the path attribute of this cookie instance
    * @param created this determines if the cookie is new 
    */
   public Cookie(String name, String value, String path, boolean created) {
      this.date = new CookieDate();
      this.created = created;
      this.value = value;      
      this.name = name;
      this.path = path;
      this.version = 1;
      this.expiry = -1;
   }
   
   /**
    * This is used to determine if the cookie is new. A cookie is
    * considered new if it has just been created on the server. A
    * cookie is considered not new if it has been received by the
    * client in a request. This allows the server to determine if
    * the cookie needs to be delivered to the client.
    * 
    * @return this returns true if the cookie was just created
    */
   public boolean isNew() {
      return created;
   }

   /**
    * This returns the version for this cookie. The version is
    * not optional and so will always return the version this
    * cookie uses. If no version number is specified this will
    * return a version of 1, to comply with RFC 2109.
    *
    * @return the version value from this cookie instance
    */
   public int getVersion() {
      return version;
   }

   /**
    * This enables the version of the <code>Cookie</code> to be
    * set. By default the version of the <code>Cookie</code> is
    * set to 1. It is not advisable to set the version higher
    * than 1, unless it is known that the client will accept it.
    * <p>
    * Some old browsers can only handle cookie version 0. This
    * can be used to comply with the original Netscape cookie
    * specification. Version 1 complies with RFC 2109.
    *
    * @param version this is the version number for the cookie
    */
   public void setVersion(int version) {
      this.version = version;
   }

   /**
    * This returns the name for this cookie. The name and value
    * attributes of a cookie define what the <code>Cookie</code>
    * is for, these values will always be present. These are
    * mandatory for both the Cookie and Set-Cookie headers.
    * <p>
    * Because the cookie may be stored by name, the cookie name
    * cannot be modified after the creation of the cookie object.
    *
    * @return the name from this cookie instance object
    */
   public String getName() {
      return name;
   }

   /**
    * This returns the value for this cookie. The name and value
    * attributes of a cookie define what the <code>Cookie</code>
    * is for, these values will always be present. These are
    * mandatory for both the Cookie and Set-Cookie headers.
    *
    * @return the value from this cookie instance object
    */
   public String getValue() {
      return value;
   }

   /**
    * This enables the value of the cookie to be changed. This
    * can be set to any value the server wishes to send. Cookie
    * values can contain space characters as they are transmitted
    * in quotes. For example a value of <code>some value</code>
    * is perfectly legal. However for maximum compatibility
    * across the different plaforms such as PHP, JavaScript and
    * others, quotations should be avoided. If quotations are
    * required they must be added to the string. For example a
    * quoted value could be created as <code>"some value"</code>.
    *
    * @param value this is the new value of this cookie object
    */
   public void setValue(String value) {
      this.value = value;
   }

   /**
    * This determines whether the cookie is secure. The cookie
    * is secure if it has the "secure" token set, as defined
    * by RFC 2109. If this token is set then the cookie is only
    * sent over secure channels such as SSL and TLS and ensures
    * that a third party cannot intercept and spoof the cookie.
    *
    * @return this returns true if the "secure" token is set
    */
   public boolean isSecure() {
      return secure;
   }

   /**
    * This is used to determine if the client browser should send
    * this cookie over a secure protocol. If this is true then
    * the client browser should only send the cookie over secure
    * channels such as SSL and TLS. This ensures that the value
    * of the cookie cannot be intercepted by a third party.
    *
    * @param secure if true then the cookie should be secure
    */
   public void setSecure(boolean secure) {
      this.secure = secure;
   }
   
   /**
    * This is used to determine if the cookie is protected against
    * cross site scripting. It sets the <code>HttpOnly</code> value
    * for the cookie. Setting this value ensures that the cookie
    * is not available to some scripting attacks.
    * 
    * @return this returns true if the cookie is protected
    */
   public boolean isProtected() {
      return protect;
   }
   
   /**
    * This is used to protect the cookie from cross site scripting
    * vulnerabilities. If this is set to true the cookie will be
    * protected by setting the <code>HttpOnly</code> value for the
    * cookie. See RFC 6265 for more details on this value.
    * 
    * @param protect this determines if the cookie is protected
    */
   public void setProtected(boolean protect) {
      this.protect = protect;
   }

   /**
    * This returns the number of seconds a cookie lives for. This
    * determines how long the cookie will live on the client side.
    * If the expiry is less than zero the cookie lifetime is the
    * duration of the client browser session, if it is zero then
    * the cookie will be deleted from the client browser.
    *
    * @return returns the duration in seconds the cookie lives
    */
   public int getExpiry() {
      return expiry;
   }

   /**
    * This allows a lifetime to be specified for the cookie. This
    * will make use of the "max-age" token specified by RFC 2109
    * the specifies the number of seconds a browser should keep
    * a cookie for. This is useful if the cookie is to be kept
    * beyond the lifetime of the client session. If the value of
    * this is zero then this will remove the client cookie, if
    * it is less than zero then the "max-age" field is ignored.
    *
    * @param expiry the duration in seconds the cookie lives
    */
   public void setExpiry(int expiry){
      this.expiry = expiry;
   }

   /**
    * This returns the path for this cookie. The path is in both
    * the Cookie and Set-Cookie headers and so may return null
    * if there is no domain value. If the <code>toString</code>
    * or <code>toClientString</code> is invoked the path will
    * not be present if the path attribute is null.
    *
    * @return this returns the path value from this cookie
    */
   public String getPath() {
      return path;
   }

   /**
    * This is used to set the cookie path for this cookie. This
    * is set so that the cookie can specify the directories that
    * the cookie is sent with. For example if the path attribute
    * is set to <code>/pub/bin</code>, then requests for the
    * resource <code>http://hostname:port/pub/bin/README</code>
    * will be issued with this cookie. The cookie is issued for
    * all resources in the path and all subdirectories.
    *
    * @param path this is the path value for this cookie object
    */
   public void setPath(String path) {
      this.path = path;
   }

   /**
    * This returns the domain for this cookie. The domain is in
    * both the Cookie and Set-Cookie headers and so may return
    * null if there is no domain value. If either the
    * <code>toString</code> or <code>toClientString</code> is
    * invoked the domain will not be present if this is null.
    *
    * @return this returns the domain value from this cookie
    */
   public String getDomain() {
      return domain;
   }

   /**
    * This enables the domain for this <code>Cookie</code> to be
    * set. The form of the domain is specified by RFC 2109. The
    * value can begin with a dot, like <code>.host.com</code>.
    * This means that the cookie is visible within a specific
    * DNS zone like <code>www.host.com</code>. By default this
    * value is null which means it is sent back to its origin.
    *
    * @param domain this is the domain value for this cookie
    */
   public void setDomain(String domain) {
      this.domain = domain;
   }

   /**
    * This will give the correct string value of this cookie. This
    * will generate the cookie text with only the values that were
    * given with this cookie. If there are no optional attributes
    * like $Path or $Domain these are left blank. This returns the
    * encoding as it would be for the HTTP Cookie header.
    *
    * @return this returns the Cookie header encoding of this
    */
   public String toClientString(){
      return "$Version="+version+"; "+name+"="+
       value+ (path==null?"":"; $Path="+
      path)+ (domain==null? "":"; $Domain="+
       domain);
   }

   /**
    * The <code>toString</code> method converts the cookie to the
    * Set-Cookie value. This can be used to send the HTTP header
    * to a client browser. This uses a format that has been tested
    * with various browsers. This is required as some browsers
    * do not perform flexible parsing of the Set-Cookie value.
    * <p>
    * Netscape and IE-5.0 can't or wont handle <code>Path</code>
    * it must be <code>path</code> also Netscape can not handle
    * the path in quotations such as <code>"/path"</code> it must
    * be <code>/path</code>. This value is never in quotations.
    * <p>
    * For maximum compatibility cookie values are not transmitted
    * in quotations. This is done to ensure that platforms like
    * PHP, JavaScript and various others that don't comply with
    * RFC 2109 can transparently access the sent cookies.
    * <p>
    * When setting the expiry time for the cookie it is important
    * to set the <code>max-age</code> and <code>expires</code>
    * attributes so that IE-5.0 and up can understand them. Old
    * versions of IE do not understand <code>max-age</code>.
    *
    * @return this returns a Set-Cookie encoding of the cookie
    */
   public String toString(){
      return name+"="+value+"; version="+
      version +(path ==null ?"":"; path="+path)+
        (domain ==null ?"": "; domain="+domain)+
        (expiry< 0?"":"; expires="+date.format(expiry))+
       (expiry < 0 ? "" : "; max-age="+expiry)+
         (secure ? "; secure" : "") +
         (protect ? "; httponly" : "");
         
   }
   
   /**
    * The <code>CookieDate</code> complies with the date format 
    * used by older browsers such as Internet Explorer and 
    * Netscape Navigator. The format of the date is not the same
    * as other HTTP date headers. It takes the form.
    * <pre>
    * 
    *    DAY, DD-MMM-YYYY HH:MM:SS GMT
    * 
    * </pre>
    * Support for this format is required as many browsers do
    * not support <code>max-age</code> and so cookies will not
    * expire for these browsers.
    */
   private static class CookieDate {
      
      /**
       * This is the format that is required for the date.
       */
      private static final String FORMAT = "EEE, dd-MMM-yyyy HH:mm:ss z";
      
      /**
       * The cookie date must be returned in the GMT zone.
       */
      private static final String ZONE = "GMT";
      
      /**
       * This is the date formatter used to build the string.
       */
      private final DateFormat format;
      
      /**
       * This is the GMT time zone which must be used.
       */
      private final TimeZone zone;
      
      /**
       * Constructor for the <code>CookieDate</code> formatter.
       * This creates the time zone and date formatting tools
       * that are need to convert the expiry in seconds to the
       * correct text format for older browsers to understand.
       */
      public CookieDate() {
         this.format = new SimpleDateFormat(FORMAT);
         this.zone = new SimpleTimeZone(0, ZONE);
      }
      
      /**
       * This takes the number of seconds the cookie will live
       * for. In order for this to be respected by older browsers
       * such as IE-5.0 to IE-9.0 this must return a string in
       * the original cookie specification by Netscape.
       * 
       * @param seconds the number of seconds from now
       * 
       * @return a date formatted for used with old browsers
       */
      public String format(int seconds) {
         Calendar calendar = Calendar.getInstance(zone);
         Date date = convert(seconds);
         
         calendar.setTime(date);        
         format.setCalendar(calendar);
         
         return format.format(date);
      }
      
      /**
       * This method is used to convert the provided time to
       * a date that can be formatted. The time returned is the
       * current time plus the number of seconds provided.        
       * 
       * @param seconds the number of seconds from now
       * 
       * @return a date representing some time in the future
       */
      private Date convert(int seconds) {
         long now = System.currentTimeMillis();
         long duration = seconds * 1000L;
         long time = now + duration;
         
         return new Date(time);         
      }          
   }
}
