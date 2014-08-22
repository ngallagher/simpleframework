package org.simpleframework.demo.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.springframework.jmx.export.annotation.ManagedResource;

import com.sun.jdmk.comm.AuthInfo;
import com.sun.jdmk.comm.CommunicatorServer;
import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * This provides a means of management for the host service via a web 
 * browser. All controls exposed are JMX attributes or operations which 
 * allows various components to be manipulated for management.
 * 
 * @author Niall Gallagher
 */
@ManagedResource(description="Web administration tool")
public class WebAdministrator {

   private final WebConfiguration config;
   private final WebPageParser parser;

   public WebAdministrator(WebConfiguration config) {
      this(config, null);
   }

   public WebAdministrator(WebConfiguration config, WebObjectIntrospector introspector) {
      this.parser = new WebPageParser(config, introspector);
      this.config = config;
   }

   public void start() {
      try {
         int port = config.getPort();
         String login = config.getLogin();
         String password = config.getPassword();
         MBeanServer platformServer = ManagementFactory.getPlatformMBeanServer();
         HtmlAdaptorServer adapterServer = new HtmlAdaptorServer();
         AuthInfo authInfo = new AuthInfo(login, password);
         ObjectName serverName = new ObjectName("com.yb.fix.manage.jmx:name=WebAdministratorServer,type=HtmlAdaptorServer");
         ObjectName parserName = new ObjectName("com.yb.fix.manage.jmx:name=WebPageParser,type=WebPageParser");
         platformServer.registerMBean(parser, parserName);
         adapterServer.setPort(port);
         adapterServer.addUserAuthenticationInfo(authInfo);
         platformServer.registerMBean(adapterServer, serverName);
         adapterServer.start();
         adapterServer.setParser(parserName);
         adapterServer.waitState(CommunicatorServer.ONLINE, 1000);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
