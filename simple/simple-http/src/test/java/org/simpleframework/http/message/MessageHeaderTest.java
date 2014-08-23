package org.simpleframework.http.message;

import junit.framework.TestCase;

public class MessageHeaderTest extends TestCase {
   
   public void testMessage() {
      MessageHeader header = new MessageHeader();
      header.addValue("A", "a");
      header.addValue("A", "b");
      header.addValue("A", "c");
      
      assertEquals(header.getValue("A"), "a");
      assertEquals(header.getValue("A", 0), "a");
      assertEquals(header.getValue("A", 1), "b");
      assertEquals(header.getValue("A", 2), "c");
      
      header.setValue("A", null);
      
      assertEquals(header.getValue("A"), null);
      assertEquals(header.getValue("A", 0), null);
      assertEquals(header.getValue("A", 1), null);
      assertEquals(header.getValue("A", 2), null);
      assertEquals(header.getValue("A", 3), null);
      assertEquals(header.getValue("A", 4), null);
      assertEquals(header.getValue("A", 5), null);
      
      header.setValue("A", "X");
      
      assertEquals(header.getValue("A"), "X");
      assertEquals(header.getValue("A", 0), "X");
      assertEquals(header.getValue("A", 1), null);
      
      header.addInteger("A", 1);
      
      assertEquals(header.getValue("A"), "X");
      assertEquals(header.getValue("A", 0), "X");
      assertEquals(header.getValue("A", 1), "1");
      assertEquals(header.getValue("A", 2), null);
      
      header.addValue("A", null);
      
      assertEquals(header.getValue("A"), "X");
      assertEquals(header.getValue("A", 0), "X");
      assertEquals(header.getValue("A", 1), "1");
      assertEquals(header.getValue("A", 2), null);
   }
}
