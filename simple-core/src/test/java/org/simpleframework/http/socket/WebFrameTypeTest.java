package org.simpleframework.http.socket;

import junit.framework.TestCase;

public class WebFrameTypeTest extends TestCase {
   
   public void testFrameType() throws Exception {
      System.err.println(Integer.toBinaryString(129)); // TEXT FRAME
      System.err.println(Integer.toBinaryString(128)); 
      System.err.println(Integer.toBinaryString(129 >>> 4));
      System.err.println(Integer.toBinaryString(0x01)); // TEXT
      System.err.println(Integer.toBinaryString(0x02)); // BINARY
      System.err.println(Integer.toBinaryString(0x03));
      System.err.println(Integer.toBinaryString(0x01 % 0x80)); // TEXT
      System.err.println(Integer.toBinaryString(0x02 % 0x80)); // BINARY
      System.err.println(Integer.toBinaryString(0x03 % 0x80)); 
      System.err.println(Integer.toBinaryString(0x80)); // FIN
      
      int b0 = 0;
      if (true) {
          b0 |= 1 << 7;
      }
      b0 |= 0x02 % 128;
     
      
      System.err.println(Integer.toBinaryString(b0)); 
   }

}
