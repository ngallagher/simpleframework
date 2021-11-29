package org.simpleframework.http.socket;

import junit.framework.TestCase;

public class CloseCodeTest extends TestCase {

   public void testClose() throws Exception {
      for (CloseCode code : CloseCode.values()) {
         short value = code.code;
         int high = (byte)(value >>> 8);
         int low = (byte)(value & 0xff);

         assertEquals(high, code.getData()[0]);
         assertEquals(low, code.getData()[1]);
         assertEquals(CloseCode.resolveCode(high, low), code);
      }
   }
}
