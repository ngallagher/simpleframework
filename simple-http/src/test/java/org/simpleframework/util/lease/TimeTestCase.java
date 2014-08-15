package org.simpleframework.util.lease;

import junit.framework.TestCase;

public class TimeTestCase extends TestCase {

   public void testTime() {
   }
   
   public static void assertLessThan(long a, long b) {      
      assertTrue(String.format("Value %s is not less than %s", a, b), a < b);
   }
   
   public static void assertLessThanOrEqual(long a, long b) {      
      assertTrue(String.format("Value %s is not less than or equal to %s", a, b), a <= b);
   }
   
   public static void assertGreaterThan(long a, long b) {
      assertTrue(String.format("Value %s is not greater than %s", a, b), a > b);      
   }
   
   public static void assertGreaterThanOrEqual(long a, long b) {
      assertTrue(String.format("Value %s is not greater than or equal to %s", a, b), a >= b);      
   }
}
