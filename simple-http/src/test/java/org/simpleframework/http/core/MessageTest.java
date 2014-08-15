package org.simpleframework.http.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.simpleframework.http.message.MessageHeader;

import junit.framework.TestCase;

public class MessageTest extends TestCase {
   
   public void testMessage() {
      MessageHeader message = new MessageHeader();
      
      message.addValue("Content-Length", "10");
      message.addValue("Connection", "keep-alive");
      message.addValue("Accept", "image/gif, image/jpeg, */*");
      message.addValue("Set-Cookie", "a=b");
      message.addValue("Set-Cookie", "b=c");
      
      assertEquals(message.getValue("CONTENT-LENGTH"), "10");      
      assertEquals(message.getValue("Content-Length"), "10");
      assertEquals(message.getValue("CONTENT-length"), "10");
      assertEquals(message.getValue("connection"), "keep-alive");
      assertEquals(message.getValue("CONNECTION"), "keep-alive");
      
      assertTrue(message.getValues("CONNECTION") != null);
      assertEquals(message.getValues("connection").size(), 1);
      
      assertTrue(message.getValues("set-cookie") != null);
      assertEquals(message.getValues("set-cookie").size(), 2);
      assertTrue(message.getValues("SET-COOKIE").contains("a=b"));
      assertTrue(message.getValues("SET-COOKIE").contains("b=c"));

      assertTrue(message.getNames().contains("Content-Length"));
      assertFalse(message.getNames().contains("CONTENT-LENGTH"));
      assertTrue(message.getNames().contains("Connection"));
      assertFalse(message.getNames().contains("CONNECTION"));
      assertTrue(message.getNames().contains("Set-Cookie"));
      assertFalse(message.getNames().contains("SET-COOKIE"));   
      
      message.setValue("Set-Cookie", "d=e");
      
      assertTrue(message.getValues("set-cookie") != null);
      assertEquals(message.getValues("set-cookie").size(), 1);
      assertFalse(message.getValues("SET-COOKIE").contains("a=b"));
      assertFalse(message.getValues("SET-COOKIE").contains("b=c"));
      assertTrue(message.getValues("SET-COOKIE").contains("d=e"));  
   }
   
   public void testDates() {
      MessageHeader message = new MessageHeader();
      DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
      TimeZone zone = TimeZone.getTimeZone("GMT");
      long time = System.currentTimeMillis();
      Date date = new Date(time);
      
      format.setTimeZone(zone);
      message.setValue("Date", format.format(date));
    
      assertEquals(format.format(date), message.getValue("date"));
      assertEquals(new Date(message.getDate("DATE")).toString(), date.toString());
      
      message.setDate("Date", time);
      
      assertEquals(format.format(date), message.getValue("date"));
      assertEquals(new Date(message.getDate("DATE")).toString(), date.toString());      
   }

}
