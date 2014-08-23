package org.simpleframework.http.validate;

import java.io.File;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Persister;


/**
 * <test debug="false" validate="false" repeat='10' timeout='10000'>
 *    <connection host='localhost' port='8080'>
 *       <request method='GET' target='/path.html'>
 *          <header name='Content-Type'>text/plain</header>
 *          <header name='Content-Length'>10</header>
 *          <body>name=value&a=b</body>
 *       </request>        
 *    </connection>
 * </test>
 * 
 */
@Root
public class Test {
   
   @Element
   private ConnectionTask connection;
   
   @Element
   private Analyser analyser;
   
   @Attribute
   private int timeout;
   
   @Attribute
   private int startConcurrency;
   
   @Attribute
   private int finishConcurrency;
   
   @Attribute
   private int sampleDuration;
   
   @Attribute
   private File logFile;
   
   @Attribute(required=false)
   private boolean validate;
   
   @Attribute(required=false)
   private boolean debug;
   
   @Transient
   private int repeat;
   
   @Commit
   private void commit() throws Exception {
      if(startConcurrency < 1) {
         throw new IllegalArgumentException("Start concurrency must be greater than '"+startConcurrency+"'");
      }
      if(startConcurrency > finishConcurrency) {
          throw new IllegalArgumentException("Start concurrency '"+startConcurrency+"' must be less than finish concurrency than '"+finishConcurrency+"'");
       }
      repeat = connection.getRepeat();
   }
   
   public void execute(File configDir) throws Exception {
      ExecutorService executor = Executors.newFixedThreadPool((finishConcurrency + 1) * 2);
      StatisticsFileLogger logWriter = new StatisticsFileLogger(logFile);
      AtomicInteger activeClients = new AtomicInteger(startConcurrency);
      Logger logger = new Logger(logWriter, activeClients, sampleDuration);
      
      try {
		  for(int concurrency = startConcurrency; concurrency <= finishConcurrency; concurrency++) {
			  List<Buffer> list = execute(executor, configDir, logger, concurrency);
			  
			  if(validate) {
			     List<Report> report = validate(list, configDir, concurrency);
			  
		        dump(report, configDir, debug); 
		     }
			  activeClients.getAndIncrement();
		  }
      }finally {
          executor.shutdown();     
          executor.awaitTermination(5000L, TimeUnit.MILLISECONDS);
      }
   }
   
   public void investigate(Buffer buffer, File configDir) throws Exception {
	   if(debug) {
		   System.out.println("Investigating an issue");
	   }
	   List<Buffer> list = Collections.singletonList(buffer);
	   List<Report> report = validate(list, configDir, 1);
	   
	   dump(report, configDir, debug);
   }

   private void dump(List<Report> report, File configDir, boolean debug) throws Exception {
		Persister persister = new Persister();
		int count = report.size();
	  
	  for(int i = 0; i < count; i++) {
	     Report entry = report.get(i);
	     
	     if(entry.isError() || debug) {
	        String output = String.format("report-%s.xml", i);
	        File file = new File(configDir, output);
	        
	        persister.write(entry, file);
	     } else if(debug){
	        System.out.printf("PASS: %s of %s - %s%n", i, count, entry.getStatusLine());
	     }
	  }
   }
   
   private List<Buffer> execute(ExecutorService executor, File configDir, Logger logger, int activeClients) throws Exception {
      Client client = new Client(executor, logger, configDir, this, activeClients, timeout, repeat, debug);
	    List<Buffer> list = new Vector<Buffer>();
	    CountDownLatch start = new CountDownLatch(activeClients);
	    CountDownLatch finish = new CountDownLatch(activeClients);
	    ExecutionTask task = new ExecutionTask(start, finish, client, list);
	    
	    for(int i = 0; i < activeClients; i++) {
	      executor.execute(task);
	    }
	    finish.await(); 
	    logger.sample();
	    return list;
   }
   
   private List<Report> validate(List<Buffer> list, File configDir, int activeClients) throws Exception {
      List<Report> reportList = new ArrayList<Report>();
      Extractor extractor = new Extractor(debug);
      int repeat = connection.getRepeat();
      int total = list.size();
      int pipeline = 0;
      
      if(debug) {
    	  System.out.println("Validating '"+total+"' responses for '"+activeClients+"'");
      }
      if(total != activeClients) {
         System.out.println("Received only '"+total+"' responses when expected '"+activeClients+"'");
         System.exit(0);
      }
      for(Buffer buffer : list) {
         InputStream stream = buffer.open();
         PushbackInputStream pushback = new PushbackInputStream(stream, 1024);
         List<Report> pipelineList = new ArrayList<Report>();
         pipeline++;
         
         for(int i = 0; i < repeat; i++) {
            Result response = extractor.extractResponse(pushback);
            
            if(response != null) {
               if(debug) {
                  System.out.printf("VALIDATE: pipeline %s of %s request %s - %s remaining [%s]%n", pipeline, total, i, response.getStatusLine(), stream.available());
               }
               Buffer nextLine = extractor.extractLine(pushback);
               String nextStatus = null;
               
               if(nextLine != null) {
                  nextStatus = nextLine.encode("ISO-8859-1").trim(); 
               }
               Report report = new Report(response, nextStatus);
            
               try {
                  String unread = nextStatus + "\r\n";
                  byte[] data = unread.getBytes("ISO-8859-1");
                  
                  analyser.analyse(response, debug);
                  
                  if(nextStatus != null) {
                	  pushback.unread(data);
                  }
               } catch(Exception e) {
                  e.printStackTrace();
                  report.addException(e);
                  pipelineList.add(report);
                  dump(pipelineList, configDir, true);
                  System.exit(0);
               }
               pipelineList.add(report);
            }
         }
         reportList.addAll(pipelineList);
         int size = pushback.available();
         
         if(size > 0) {
        	   System.out.println("Excess information sent in response of size '"+size+"' after reading '"+pipelineList.size()+"' requests of expected '"+repeat+"' was");
        	   System.out.println(extractor.extractAll(pushback).encode("ISO-8859-1"));
        	   dump(pipelineList, configDir, true);
        	   System.exit(0);
         }
         if(reportList.size() != repeat * pipeline) {
        	   System.out.println("Only recieved '"+reportList.size()+"' from a pipeline of '"+(repeat * pipeline)+"'");
        	   dump(pipelineList, configDir, true);
        	   System.exit(0);
         }
      }
      System.err.println("Validated '"+activeClients+"' clients and '"+(pipeline * repeat)+"' requests");
      return reportList;
   }
   
   private class ExecutionTask implements Runnable {
      
      private final List<Buffer> list;
      private final CountDownLatch start;      
      private final CountDownLatch finish;      
      private final Client client;      
      
      public ExecutionTask(CountDownLatch start, CountDownLatch finish, Client client, List<Buffer> list) {
         this.start = start;
         this.finish = finish;
         this.client = client;
         this.list = list;
      }
      
      public void run() {
         try {
            start.countDown();
            start.await();
            execute();         
         }catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
         } finally {
            finish.countDown();  
         }
      }
      
      private void execute() throws Exception {
         Buffer result = connection.execute(client);
         
         if(result == null) {
        	 System.err.println("Returned a null buffer");
        	 System.exit(0);
         }
         list.add(result);
      }
   }
   

}
