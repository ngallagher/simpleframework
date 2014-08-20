package org.simpleframework.http.validate.test;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.simpleframework.http.validate.ThreadDumper;

public class RoundTripTest extends TestCase {

   public static final String REQUEST_ID = "X-ID";
   
   public final void testServer() throws Exception  {
      Class type = getClass();
      Method[] methods = type.getDeclaredMethods();
      
      for(Method method : methods) {
         Scenario scenario = method.getAnnotation(Scenario.class);
         
         if(scenario != null) { 
            Analyser handler = (Analyser) method.invoke(this);
            Runner task = new Runner(handler, scenario);
            boolean threadDump = scenario.threadDump();
            ThreadDumper dumper = new ThreadDumper();
            
            if(threadDump) {
               dumper.start();
            }
            long start = System.currentTimeMillis();
            
            task.start();
            task.execute();
            task.stop();
            
            long duration = System.currentTimeMillis() - start;
            
            if(threadDump) {
               dumper.kill();
            }
            int concurrency = scenario.concurrency();
            int requests = scenario.requests();
            int total = concurrency * requests;
            
            assertEquals(String.format("Expected %s requests but finished %s", total, task.getRequests()), task.getRequests(), total);
            assertEquals(String.format("Got %s failures", task.getFailures()), task.getFailures(), 0);
            
            System.err.println(getClass().getSimpleName() + " was successful with "+
                  task.getRequests()+" requests and "+task.getFailures()+" failures in "+duration+" milliseconds");
         }
      }
   }
}
