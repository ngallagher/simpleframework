package org.simpleframework.demo.jmx.spring;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description="Contains application details")
public class ApplicationAgent {

   @ManagedAttribute(description="Current working directory")
   public String getWorkingDirectory() {
      return new File(".").getAbsolutePath();
   }

   @ManagedAttribute(description="Owner of the process")
   public String getProcessOwner() {
      return System.getProperty("user.name");
   }

   @ManagedAttribute(description="Name of host")
   public String getHostName() {
      try {
         return InetAddress.getLocalHost().getHostName();
      } catch (Exception e) {
         return "unknown";
      }
   }

   @ManagedAttribute(description="Percentage or memory used")
   public String getMemoryPercentageUsed() {
      double memoryLimit = Runtime.getRuntime().maxMemory();
      double memoryFree = Runtime.getRuntime().freeMemory();
      double memoryUsed = memoryLimit - memoryFree;
      double percentageUsed = (memoryUsed / memoryLimit) * 100f;

      return Math.round(percentageUsed) + "%";
   }

   @ManagedOperation(description="Shows how much memory is used")
   public String showMemoryUsage() {
      List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
      DecimalFormat format = new DecimalFormat("#,###,###,###");
      StringBuilder builder = new StringBuilder();

      builder.append("<table border='1'>");
      builder.append("<th>name</th>");
      builder.append("<th>size</th>");
      builder.append("<th>used</th>");
      builder.append("<th>free</th>");
      builder.append("<th>usage</th>");

      for (MemoryPoolMXBean memoryPool : memoryPools) {
         String name = memoryPool.getName();
         MemoryUsage usage = memoryPool.getUsage();
         double defaultMax = Runtime.getRuntime().maxMemory();
         double explicitMax = usage.getMax();
         double memoryUsed = usage.getUsed();
         double memoryMax = explicitMax > 0 ? explicitMax : defaultMax;
         double memoryFree = memoryMax - memoryUsed;
         double widthMax = 700;
         double widthUsed = widthMax * (memoryUsed / memoryMax);
         double widthFree = widthMax - widthUsed;
         String formattedMax = format.format(memoryMax / 1024);
         String formattedUsed = format.format(memoryUsed / 1024);
         String formattedFree = format.format(memoryFree / 1024);
         String sizeUsed = format.format(widthUsed);
         String sizeFree = format.format(widthFree);

         builder.append("<tr>");
         builder.append("<td>").append(name).append("</td>");
         builder.append("<td>").append(formattedMax).append("k</td>");
         builder.append("<td>").append(formattedUsed).append("k</td>");
         builder.append("<td>").append(formattedFree).append("k</td>");
         builder.append("<td>\n");
         builder.append("<table cellpadding='0' cellspacing='0'>\n");
         builder.append("<tr>\n");
         builder.append("<td bgcolor='#00ff00' height='20' width='").append(sizeUsed).append("'></td>\n");
         builder.append("<td bgcolor='#ff0000' height='20' width='").append(sizeFree).append("'></td>\n");
         builder.append("</table>\n");
         builder.append("</td>\n");
         builder.append("</tr>");
      }
      builder.append("</table>");
      return builder.toString();
   }

   @ManagedOperation(description="Show system properties")
   public String showSystemProperties() {
      Properties properties = System.getProperties();
      Set<String> propertyKeys = properties.stringPropertyNames();
      Set<String> sortedKeys = new TreeSet<String>(propertyKeys);
      StringBuilder builder = new StringBuilder();

      builder.append("<table border='1'>");
      builder.append("<th>property</th>");
      builder.append("<th>value</th>");

      for (String propertyKey : sortedKeys) {
         String propertyValue = properties.getProperty(propertyKey);

         builder.append("<tr>");
         builder.append("<td>").append(propertyKey).append("</td>");
         builder.append("<td>").append(propertyValue).append("</td>");
         builder.append("</tr>");
      }
      builder.append("</table>");
      return builder.toString();
   }
}
