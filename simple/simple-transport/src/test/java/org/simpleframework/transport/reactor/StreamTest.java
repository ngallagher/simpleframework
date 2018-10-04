package org.simpleframework.transport.reactor;

import java.util.function.Function;
import java.util.stream.IntStream;

import junit.framework.TestCase;

public class StreamTest extends TestCase {

   public void testIntegerFunctions() throws Exception {
      IntStream.of(0,1,2,3,4,5,6,7,8,9).limit(4).forEach(x -> System.err.println(x));
      IntStream.iterate(0, i -> i + 2).limit(3).forEach(x -> System.err.println(x)); 
   }
   
   public void testIntStreams() throws Exception {
      Function<String, String> f = s -> s.toUpperCase();
      String str = f.andThen(s -> s + "a").apply("hello");
      System.err.println(str);
   }
   
   public void testFunctions() throws Exception {
      Function<String, String> upper = x -> x.toUpperCase();
      String str = upper
            .andThen(x -> x + "a")
            .andThen(x -> "a" + x)
            .apply("hello");
      
      System.err.println(str);
   }
}
