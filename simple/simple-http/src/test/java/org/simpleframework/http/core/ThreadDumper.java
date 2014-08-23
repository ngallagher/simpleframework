package org.simpleframework.http.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;


public class ThreadDumper extends Thread {

   private static String INDENT = "    ";
   private CountDownLatch latch;
   private volatile boolean dead;
   private int wait;

   public ThreadDumper() {
      this(10000);
   }
   
   public ThreadDumper(int wait) {
      this.latch = new CountDownLatch(1);
      this.wait = wait;
  }
   
   public void waitUntilStarted() throws InterruptedException{
      latch.await();
   }
   
   public void kill(){        
      try {
         Thread.sleep(1000);
         dead = true;
         dumpThreadInfo();            
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   public void run() {
      while(!dead) {
         try{
            latch.countDown();
            dumpThreadInfo();
            findDeadlock();
            Thread.sleep(wait);
         }catch(Exception e){
            e.printStackTrace();
         }
      }
   }
   public String dumpThreads() {
      Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
      return generateDump(stackTraces);
   }

   public static String dumpCurrentThread() {
      Thread currentThread = Thread.currentThread();
      StackTraceElement[] stackTrace = currentThread.getStackTrace();
      Map<Thread, StackTraceElement[]> stackTraces = Collections.singletonMap(currentThread, stackTrace);
      return generateDump(stackTraces);

   }

   private static String generateDump(Map<Thread, StackTraceElement[]> stackTraces) {
      StringBuilder builder = new StringBuilder();

      builder.append("<pre>");
      builder.append("<b>Full Java thread dump</b>");
      builder.append("\n");

      Set<Thread> threads = stackTraces.keySet();

      for (Thread thread : threads) {
         StackTraceElement[] stackElements = stackTraces.get(thread);

         generateDescription(thread, builder);
         generateStackFrames(stackElements, builder);
      }
      builder.append("</pre>");
      return builder.toString();
   }

   private static void generateStackFrames(StackTraceElement[] stackElements, StringBuilder builder) {
      for (StackTraceElement stackTraceElement : stackElements) {
         builder.append("    at ");
         builder.append(stackTraceElement);
         builder.append("\n");
      }
   }

   private static void generateDescription(Thread thread, StringBuilder builder) {
      Thread.State threadState = thread.getState();
      String threadName = thread.getName();
      long threadId = thread.getId();

      builder.append("\n");
      builder.append("<b>");
      builder.append(threadName);
      builder.append("</b> Id=");
      builder.append(threadId);
      builder.append(" in ");
      builder.append(threadState);
      builder.append("\n");
   }

  /**
   * Prints the thread dump information to System.out.
   */
  public static void dumpThreadInfo(){
     System.out.println(getThreadInfo());
  }
  
  public static String getThreadInfo() {
     ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
     long[] tids = tmbean.getAllThreadIds();
     ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
     StringWriter str = new StringWriter();
     PrintWriter log = new PrintWriter(str);
     log.println("Full Java thread dump");
     
     for (ThreadInfo ti : tinfos) {
         printThreadInfo(ti, log);
     }
     log.flush();
     return str.toString();
  }
  
  private static void printThreadInfo(ThreadInfo ti, PrintWriter log) {
     if(ti != null) {
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" +
                                             " Id=" + ti.getThreadId() +
                                             " in " + ti.getThreadState());
        if (ti.getLockName() != null) {
            sb.append(" on lock=" + ti.getLockName()); 
        }
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        log.println(sb.toString());
        if (ti.getLockOwnerName() != null) {
             log.println(INDENT + " owned by " + ti.getLockOwnerName() +
                                " Id=" + ti.getLockOwnerId());
        }
        for (StackTraceElement ste : ti.getStackTrace()) {
            log.println(INDENT + "at " + ste.toString());
        }
        log.println();
     }
  }

  /**
   * Checks if any threads are deadlocked. If any, print
   * the thread dump information.
   */
  public static boolean findDeadlock() {
     ThreadMXBean tmbean = ManagementFactory.getThreadMXBean();
     long[] tids = tmbean.findMonitorDeadlockedThreads();
     if (tids == null) { 
         return false;
     } else {
        StringWriter str = new StringWriter();
        PrintWriter log = new PrintWriter(str);
        
         tids = tmbean.getAllThreadIds();
         System.out.println("Deadlock found :-");
         ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
         for (ThreadInfo ti : tinfos) {
             printThreadInfo(ti, log);
         }
         log.flush();
         System.out.println(str.toString());
         return true;
     }
  }

}
