package org.simpleframework.demo.thread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description="A JMX tool for building a thread dump")
public class ThreadDumper {

   @ManagedOperation(description="Provides a dump of the threads")
   public String dumpThreads() {
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      long[] threadIds = threadBean.getAllThreadIds();
      ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadIds, Integer.MAX_VALUE);

      return generateDump(threadInfos);
   }

   public String dumpCurrentThread() {
      ThreadMXBean bean = ManagementFactory.getThreadMXBean();
      Thread thread = Thread.currentThread();
      long threadId = thread.getId();
      ThreadInfo threadInfo = bean.getThreadInfo(threadId, Integer.MAX_VALUE);

      return generateDump(threadInfo);
   }

   public static String generateDump(ThreadInfo threadInfo) {
      StringBuilder builder = new StringBuilder();

      builder.append("<pre>");
      builder.append("\n");

      generateThreadDetail(threadInfo, builder);

      builder.append("</pre>");
      return builder.toString();
   }

   public static String generateDump(ThreadInfo[] threadInfos) {
      StringBuilder builder = new StringBuilder();

      builder.append("<pre>");
      builder.append("<b>Full Java thread dump</b>");
      builder.append("\n");

      for (ThreadInfo threadInfo : threadInfos) {
         generateThreadDetail(threadInfo, builder);
      }
      builder.append("</pre>");
      return builder.toString();
   }

   public static void generateThreadDetail(ThreadInfo threadInfo, StringBuilder builder) {
      generateDescription(threadInfo, builder);
      generateLockDetails(threadInfo, builder);
      generateStackFrames(threadInfo, builder);
   }

   public static void generateStackFrames(ThreadInfo threadInfo, StringBuilder builder) {
      StackTraceElement[] stackTrace = threadInfo.getStackTrace();

      for (StackTraceElement stackTraceElement : stackTrace) {
         builder.append("    at ");
         builder.append(stackTraceElement);
         builder.append("\n");
      }
   }

   public static void generateLockDetails(ThreadInfo threadInfo, StringBuilder builder) {
      String lockOwnerName = threadInfo.getLockOwnerName();
      long lockOwnerId = threadInfo.getLockOwnerId();

      if (lockOwnerName != null) {
         builder.append("    owned by ");
         builder.append(lockOwnerName);
         builder.append(" Id=");
         builder.append(lockOwnerId);
         builder.append("\n");
      }
   }

   public static void generateDescription(ThreadInfo threadInfo, StringBuilder builder) {
      Thread.State threadState = threadInfo.getThreadState();
      String threadName = threadInfo.getThreadName();
      String lockName = threadInfo.getLockName();
      long threadId = threadInfo.getThreadId();

      builder.append("\n");
      builder.append("<b>");
      builder.append(threadName);
      builder.append("</b> Id=");
      builder.append(threadId);
      builder.append(" in ");
      builder.append(threadState);

      if (lockName != null) {
         builder.append(" on lock=");
         builder.append(lockName);
      }
      if (threadInfo.isSuspended()) {
         builder.append(" (suspended)");
      }
      if (threadInfo.isInNative()) {
         builder.append(" (running in native)");
      }
      builder.append("\n");
   }
}
