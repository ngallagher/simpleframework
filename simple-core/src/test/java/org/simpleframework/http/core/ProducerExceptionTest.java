package org.simpleframework.http.core;

import java.io.IOException;

import junit.framework.TestCase;

public class ProducerExceptionTest extends TestCase {
   
   public void testException() {
      try {
         throw new IOException("Error");
      }catch(Exception main) {
         try {
            throw new ProducerException("Wrapper", main);
         }catch(Exception cause) {
            cause.printStackTrace();
            
            assertEquals(cause.getCause(), main);
         }
      }
   }

}
