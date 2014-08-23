package org.simpleframework.http;

import junit.framework.TestCase;

public class StatusTest extends TestCase {
   
   private static final int ITERATIONS = 100000;
   
   public void testStatus() {
      testStatus(200, "OK");
      testStatus(404, "Not Found");
   }
   
   public void testStatus(int code, String expect) {
      for(int i = 0; i < ITERATIONS; i++) {
         assertEquals(expect, Status.getDescription(code));
      }
   }

}
