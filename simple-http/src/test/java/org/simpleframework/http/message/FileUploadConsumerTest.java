package org.simpleframework.http.message;

import junit.framework.TestCase;

import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.transport.Cursor;
import org.simpleframework.util.buffer.Allocator;
import org.simpleframework.util.buffer.ArrayAllocator;

public class FileUploadConsumerTest extends TestCase {
   
   private static final String SOURCE =
   "--mxvercagiykxaqsdvrfabfhfpaseejrg\r\n"+
   "Content-Disposition: form-data; name=\"fn\"\r\n"+
   "\r\n"+
   "blah_niall\r\n"+
   "--mxvercagiykxaqsdvrfabfhfpaseejrg\r\n"+
   "Content-Disposition: form-data; name=\"Filename\"\r\n"+
   "\r\n"+
   "content\r\n"+
   "--mxvercagiykxaqsdvrfabfhfpaseejrg\r\n"+
   "Content-Disposition: form-data; name=\"Filedata[]\"; filename=\"content\"\r\n"+
   "Content-Type: application/octet-stream\r\n"+
   "\r\n"+
   "<stage version=\"2.0\" keygen_seq=\"1\"><pageObj print_grid=\"0\" border=\"0\" gr=\"1\" width=\"5000\" highResImage=\"1\" height=\"5000\" drawingHeight=\"379\" print_paper=\"LETTER\" istt=\"false\" guides=\"0\" print_layout=\"0\" print_scale=\"0\" drawingWidth=\"188\" fill=\"16777215\" pb=\"0\"><styles><shapeStyle lineColor=\"global:0x333333\" lineWidth=\"-1\" gradientOn=\"true\" dropShadowOn=\"true\" fillColor=\"global:0xd1d1d1\"/><lineStyle borderLine=\"false\" connType=\"right\" width=\"1\" roundCorners=\"true\" begin=\"0\" color=\"0x000000\" end=\"0\" pattern=\"0\"/><textStyle face=\"Arial\" size=\"12\" color=\"0\" style=\"\"/></styles><objects><object shp_id=\"0\" x=\"158\" order=\"0\" y=\"361.5\" linec=\"3355443\" dsy=\"4\" height=\"75\" symbol_id=\"\" gradon=\"true\" text-vertical-pos=\"middle\" width=\"100\" dshad=\"true\" class=\"rectangle\" dsx=\"4\" linew=\"2\" fill=\"0xd1d1d1\" fixed-aspect=\"false\" rot=\"0\" lock=\"false\" libraryid=\"com.gliffy.symbols.basic\" text-horizontal-pos=\"center\"><text/><connlines/></object></objects></pageObj></stage>\r\n"+
   "--mxvercagiykxaqsdvrfabfhfpaseejrg\r\n"+
   "Content-Disposition: form-data; name=\"Filename\"\r\n"+
   "\r\n"+
   "image\r\n"+
   "--mxvercagiykxaqsdvrfabfhfpaseejrg\r\n"+
   "Content-Disposition: form-data; name=\"Filedata[]\"; filename=\"image\"\r\n"+
   "Content-Type: application/octet-stream\r\n"+
   "\r\n"+
   "PNG"+
   "\r\n"+
   "--mxvercagiykxaqsdvrfabfhfpaseejrg\r\n"+
   "Content-Disposition: form-data; name=\"Upload\"\r\n"+
   "\r\n"+
   "Submit Query\r\n"+
   "--mxvercagiykxaqsdvrfabfhfpaseejrg--";         
   
   public void testNoFinalCRLF() throws Exception {
      byte[] data = SOURCE.getBytes("UTF-8");
      byte[] boundary = "mxvercagiykxaqsdvrfabfhfpaseejrg".getBytes("UTF-8");
      Allocator allocator = new ArrayAllocator();
      FileUploadConsumer consumer = new FileUploadConsumer(allocator, boundary, data.length);      
      Cursor cursor = new StreamCursor(data);
      
      while(!consumer.isFinished()) {
         consumer.consume(cursor);
      }  
      assertEquals(consumer.getBody().getContent(), SOURCE);
      assertEquals(consumer.getBody().getParts().size(), 6); 
   }
   
   public void testNoFinalCRLSWithDribble() throws Exception {
      byte[] data = SOURCE.getBytes("UTF-8");
      byte[] boundary = "mxvercagiykxaqsdvrfabfhfpaseejrg".getBytes("UTF-8");
      Allocator allocator = new ArrayAllocator();
      FileUploadConsumer consumer = new FileUploadConsumer(allocator, boundary, data.length);      
      Cursor cursor = new StreamCursor(data);
      DribbleCursor dribble = new DribbleCursor(cursor, 1);
      
      while(!consumer.isFinished()) {
         consumer.consume(dribble);
      }  
      assertEquals(consumer.getBody().getContent(), SOURCE);
      assertEquals(consumer.getBody().getParts().size(), 6); 
   }
   
   public void testNoFinalCRLSWithDribble3() throws Exception {
      byte[] data = SOURCE.getBytes("UTF-8");
      byte[] boundary = "mxvercagiykxaqsdvrfabfhfpaseejrg".getBytes("UTF-8");
      Allocator allocator = new ArrayAllocator();
      FileUploadConsumer consumer = new FileUploadConsumer(allocator, boundary, data.length);      
      Cursor cursor = new StreamCursor(data);
      DribbleCursor dribble = new DribbleCursor(cursor, 3);
      
      while(!consumer.isFinished()) {
         consumer.consume(dribble);
      }  
      assertEquals(consumer.getBody().getContent(), SOURCE);
      assertEquals(consumer.getBody().getParts().size(), 6); 
   }
}
