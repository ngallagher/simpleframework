
package org.simpleframework.http.core;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Chunker extends FilterOutputStream { 


   private byte[] size = {'0', '0', '0', '0', '0', 
                          '0', '0', '0',  13, 10};


   private byte[] index = {'0', '1', '2', '3', '4', '5','6', '7',
                          '8', '9', 'a', 'b', 'c', 'd','e', 'f'};


   private byte[] zero = {'0', 13, 10, 13, 10};


   public Chunker(OutputStream out){
      super(out);
   }
   
   public void write(int octet) throws IOException {
	   byte[] swap = new byte[1];
	   swap[0] = (byte)octet;
	   write(swap);
   }
   

   public void write(byte[] buf, int off, int len) throws IOException {
      int pos = 7;

      if(len > 0) {
         for(int num = len; num > 0; num >>>= 4){      
            size[pos--] = index[num & 0xf];
         }
         String text = String.format("%s; %s\r\n", Integer.toHexString(len), len);
         
         out.write(text.getBytes("ISO-8859-1"));   
         out.write(buf, off, len);
         out.write(size, 8, 2);
      }
   }
   
   public void close() throws IOException {
	   out.write(zero);
	   out.close();
   }
}
