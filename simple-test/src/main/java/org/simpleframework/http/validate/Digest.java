package org.simpleframework.http.validate; 

import java.io.IOException;   
import java.io.InputStream;   
import java.security.MessageDigest;   
import java.security.NoSuchAlgorithmException;   
  
public class Digest   
{   
    public static final int BUFFER_SIZE = 1024 * 16;   
   
    public static enum Algorithm {
       MD5("MD5"),
       SHA1("SHA1");
      
       private final String name;
       
       private Algorithm(String name) {
          this.name = name;
       }       
       public String getName() {
          return name;
       }
       
    }   
  
    public static String getSignature(Algorithm algo, InputStream ins) throws IOException {   
        return getSignature(algo, ins, 0, 0);   
    }   
  
    public static String getSignature(Algorithm algo, InputStream ins, long offset, long length) throws IOException { 
        if(ins == null) {   
            throw new IllegalArgumentException("ins should not be null");   
        }   
        if(offset < 0) {   
            throw new IllegalArgumentException("offset should not be negative");   
        }  
        MessageDigest md = null;   
        try {   
            md = MessageDigest.getInstance(algo.getName());   
        } catch(NoSuchAlgorithmException e) {   
            e.printStackTrace();   
        }   
  
        ins.skip(offset);   
        int bytesRead = 0;   
  
        // if length is positive read length number of bytes, otherwise read   
        // until the end   
        if(length > 0) {   
            byte[] bytes = new byte[(int)length];   
            bytesRead = ins.read(bytes);
            
            if(bytesRead < length) {   
                byte[] lastBytes = new byte[bytesRead];   
                System.arraycopy(bytes, 0, lastBytes, 0, lastBytes.length);   
                md.update(lastBytes);   
                return toHexString(md.digest());   
            }   
            md.update(bytes);   
        }else {   
            byte[] bytes = new byte[BUFFER_SIZE];   
            while(bytesRead > -1) {   
                bytesRead = ins.read(bytes);   
  
                if(bytesRead < BUFFER_SIZE) {   
                    byte[] lastBytes = new byte[bytesRead];   
                    System.arraycopy(bytes, 0, lastBytes, 0, lastBytes.length);   
                    md.update(lastBytes);   
                    return toHexString(md.digest());   
                } else {   
                    md.update(bytes);   
                }   
            }   
        }     
        return toHexString(md.digest());   
    }   
  
    public static String toHexString(byte[] data) {   
        return toHexString(data, 0, -1);   
    }   
  
    public static String toHexString(byte[] data, int offset, int length) {   
        if(offset < 0 || offset > data.length) {   
            throw new IllegalArgumentException("offset outside of valid range");   
        }   
        if(length > (data.length - offset)) {   
            throw new IllegalArgumentException("invalid length");   
        }     
        int i = 0;   
        int len = (length < 0) ? data.length - offset : length;   
        char[] ch = new char[len * 2];
        
        while(len-- > 0) {   
            // convert the next byte into a hex digit pair   
            //   
            int b = data[offset++] & 0xff;   
            int d = b >> 4;   
  
            d = (d < 0xA) ? d + '0' : d - 0xA + 'a';   
            ch[i++] = (char)d;   
  
            d = b & 0xF;   
            d = (d < 0xA) ? d + '0' : d - 0xA + 'a';   
            ch[i++] = (char)d;   
        } 
        return new String(ch);   
    }   
}  

