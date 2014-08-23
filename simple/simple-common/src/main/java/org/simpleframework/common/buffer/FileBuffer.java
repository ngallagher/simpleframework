/*
 * FileBuffer.java February 2008
 *
 * Copyright (C) 2008, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.simpleframework.common.buffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The <code>FileBuffer</code> object is used to create a buffer
 * which will write the appended data to an underlying file. This
 * is typically used for buffers that are too large for to allocate
 * in memory. Data appended to the buffer can be retrieved at a 
 * later stage by acquiring the <code>InputStream</code> for the
 * underlying file. To ensure that excessive file system space is
 * not occupied the buffer files are cleaned every five minutes.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.common.buffer.FileAllocator
 */
class FileBuffer implements Buffer {
   
   /**
    * This is the file output stream used for this buffer object.
    */
   private OutputStream buffer;
   
   /**
    * This represents the last file segment that has been created.
    */
   private Segment segment;
   
   /**
    * This is the path for the file that this buffer appends to.
    */
   private File file;
   
   /**
    * This is the number of bytes currently appended to the buffer.
    */
   private long count;
   
   /**
    * This is used to determine if this buffer has been closed.
    */
   private boolean closed;
   
   /**
    * Constructor for the <code>FileBuffer</code> object. This will
    * create a buffer using the provided file. All data appended to
    * this buffer will effectively written to the underlying file. 
    * If the appended data needs to be retrieved at a later stage
    * then it can be acquired using the buffers input stream.
    * 
    * @param file this is the file used for the file buffer
    */
   public FileBuffer(File file) throws IOException {
      this.buffer  = new FileOutputStream(file);
      this.file = file;
   }

   /**
    * This is used to allocate a segment within this buffer. If the
    * buffer is closed this will throw an exception, if however the
    * buffer is still open then a segment is created which will 
    * write all appended data to this buffer. However it can be
    * treated as an independent source of data.
    * 
    * @return this returns a buffer which is a segment of this 
    */
   public Buffer allocate() throws IOException {
      if(closed) {
         throw new BufferException("Buffer has been closed");
      }
      if(segment != null) {
         segment.close();
      }
      if(!closed) {
         segment = new Segment(this, count);
      }
      return segment;
   }

   /**
    * This is used to append the specified data to the underlying 
    * file. All bytes appended to the file can be consumed at a
    * later stage by acquiring the <code>InputStream</code> from
    * this buffer. Also if require the data can be encoded as a
    * string object in a required character set.
    * 
    * @param array this is the array to write the the file
    * 
    * @return this returns this buffer for further operations
    */
   public Buffer append(byte[] array) throws IOException {
      return append(array, 0, array.length);
   }

   /**
    * This is used to append the specified data to the underlying 
    * file. All bytes appended to the file can be consumed at a
    * later stage by acquiring the <code>InputStream</code> from
    * this buffer. Also if require the data can be encoded as a
    * string object in a required character set.
    * 
    * @param array this is the array to write the the file
    * @param off this is the offset within the array to write
    * @param size this is the number of bytes to be appended
    * 
    * @return this returns this buffer for further operations
    */
   public Buffer append(byte[] array, int off, int size) throws IOException {
      if(closed) {
         throw new BufferException("Buffer has been closed");
      }
      if(size > 0) {
         buffer.write(array, off, size);
         count += size;
      }
      return this;
   }

   /**
    * This method is used to acquire the buffered bytes as a string.
    * This is useful if the contents need to be manipulated as a
    * string or transferred into another encoding. If the UTF-8
    * content encoding is not supported the platform default is 
    * used, however this is unlikely as UTF-8 should be supported.
    *
    * @return this returns a UTF-8 encoding of the buffer contents
    */ 
   public String encode() throws IOException {
      return encode("UTF-8");
   }

   /**
    * This method is used to acquire the buffered bytes as a string.
    * This is useful if the contents need to be manipulated as a
    * string or transferred into another encoding. This will convert
    * the bytes using the specified character encoding format.
    * 
    * @param charset this is the charset to encode the data with
    *
    * @return this returns the encoding of the buffer contents
    */   
   public String encode(String charset) throws IOException {
      InputStream source = open();
      int size = (int)count;
      
      if(count <= 0) {
         return new String();
      }
      return convert(source, charset, size); 
   }
   
   /**
    * This method is used to acquire the buffered bytes as a string.
    * This is useful if the contents need to be manipulated as a
    * string or transferred into another encoding. This will convert
    * the bytes using the specified character encoding format.
    * 
    * @param source this is the source stream that is to be encoded
    * @param charset this is the charset to encode the data with
    * @param count this is the number of bytes to be encoded
    *
    * @return this returns the encoding of the buffer contents
    */   
   private String convert(InputStream source, String charset, int count) throws IOException {
      byte[] buffer = new byte[count];
      int left = count;
      
      while(left > 0) {
         int size = source.read(buffer, 0, left);
         
         if(size == -1) {
            throw new BufferException("Could not read buffer");
         }
         left -= count;
      }
      return new String(buffer, charset);
   }

   /**
    * This method is used so that a buffer can be represented as a
    * stream of bytes. This provides a quick means to access the data
    * that has been written to the buffer. It wraps the buffer within
    * an input stream so that it can be read directly.
    *
    * @return a stream that can be used to read the buffered bytes
    */ 
   public InputStream open() throws IOException {
      if(!closed) {
         close();
      }
      return open(file);
   }
   
   /**
    * This method is used so that a buffer can be represented as a
    * stream of bytes. This provides a quick means to access the data
    * that has been written to the buffer. It wraps the buffer within
    * an input stream so that it can be read directly.
    *
    * @param file this is the file used to create the input stream
    *
    * @return a stream that can be used to read the buffered bytes
    */ 
   private InputStream open(File file) throws IOException {
      InputStream source = new FileInputStream(file);
      
      if(count <= 0) {
         source.close(); // release file descriptor
      }
      return new Range(source, count);
   }

   /**
    * This will clear all data from the buffer. This simply sets the
    * count to be zero, it will not clear the memory occupied by the
    * instance as the internal buffer will remain. This allows the
    * memory occupied to be reused as many times as is required.
    */   
   public void clear() throws IOException {
      if(closed) {
         throw new BufferException("Buffer has been closed");
      }
   }

   /**
    * This method is used to ensure the buffer can be closed. Once
    * the buffer is closed it is an immutable collection of bytes and
    * can not longer be modified. This ensures that it can be passed
    * by value without the risk of modification of the bytes.
    */   
   public void close() throws IOException {
      if(!closed) {
         buffer.close();
         closed = true;
      }
      if(segment != null) {
         segment.close();
      }
   }
   
   /**
    * This is used to provide the number of bytes that have been
    * written to the buffer. This increases as bytes are appended
    * to the buffer. if the buffer is cleared this resets to zero.
    *  
    * @return this returns the number of bytes within the buffer
    */
   public long length() {
      return count;
   }

   /**
    * The <code>Segment</code> object is used to create a segment of
    * the parent buffer. The segment will write to the parent however
    * if can be read as a unique range of bytes starting with the 
    * first sequence of bytes appended to the segment. A segment can
    * be used to create a collection of buffers backed by the same
    * underlying file, as is require with multipart uploads.
    */
   private class Segment implements Buffer {
        
      /**
       * This is an internal segment created from this buffer object.
       */
      private Segment segment;
      
      /**
       * This is the parent buffer that bytes are to be appended to.
       */
      private Buffer parent;
      
      /**
       * This is the offset of the first byte within the sequence.
       */
      private long first;
      
      /**
       * This is the last byte within the segment for this segment.
       */
      private long last;
      
      /**
       * This determines if the segment is currently open or closed.
       */
      private boolean closed;
      
      /**
       * Constructor for the <code>Segment</code> object. This is used
       * to create a segment from a parent buffer. A segment is a part
       * of the parent buffer and appends its bytes to the parent. It
       * can however be treated as an independent source of bytes.
       * 
       * @param parent this is the parent buffer to be appended to
       * @param first this is the offset for the first byte in this
       */
      public Segment(Buffer parent, long first) {
         this.parent = parent;
         this.first = first;
         this.last = first;
      }

      /**
       * This is used to allocate a segment within this buffer. If the
       * buffer is closed this will throw an exception, if however the
       * buffer is still open then a segment is created which will 
       * write all appended data to this buffer. However it can be
       * treated as an independent source of data.
       * 
       * @return this returns a buffer which is a segment of this 
       */
      public Buffer allocate() throws IOException {
         if(closed) {
            throw new BufferException("Buffer has been closed");
         }
         if(segment != null) {
            segment.close();
         }
         if(!closed) {
            segment = new Segment(this, last);
         }
         return segment;
      }

      /**
       * This is used to append the specified data to the underlying 
       * file. All bytes appended to the file can be consumed at a
       * later stage by acquiring the <code>InputStream</code> from
       * this buffer. Also if require the data can be encoded as a
       * string object in a required character set.
       * 
       * @param array this is the array to write the the file
       * 
       * @return this returns this buffer for further operations
       */
      public Buffer append(byte[] array) throws IOException {
         return append(array, 0, array.length);
      }

      /**
       * This is used to append the specified data to the underlying 
       * file. All bytes appended to the file can be consumed at a
       * later stage by acquiring the <code>InputStream</code> from
       * this buffer. Also if require the data can be encoded as a
       * string object in a required character set.
       * 
       * @param array this is the array to write the the file
       * @param off this is the offset within the array to write
       * @param size this is the number of bytes to be appended
       * 
       * @return this returns this buffer for further operations
       */
      public Buffer append(byte[] array, int off, int size) throws IOException {
         if(closed) {
            throw new BufferException("Buffer has been closed");
         }
         if(size > 0) {
            parent.append(array, off, size);
            last += size;
         }
         return this;
      }

      /**
       * This method is used to acquire the buffered bytes as a string.
       * This is useful if the contents need to be manipulated as a
       * string or transferred into another encoding. If the UTF-8
       * content encoding is not supported the platform default is 
       * used, however this is unlikely as UTF-8 should be supported.
       *
       * @return this returns a UTF-8 encoding of the buffer contents
       */ 
      public String encode() throws IOException {
         return encode("UTF-8");
      }

      /**
       * This method is used to acquire the buffered bytes as a string.
       * This is useful if the contents need to be manipulated as a
       * string or transferred into another encoding. This will convert
       * the bytes using the specified character encoding format.
       * 
       * @param charset this is the charset to encode the data with
       *
       * @return this returns the encoding of the buffer contents
       */  
      public String encode(String charset) throws IOException {
         InputStream source = open();
         long count = last - first;
         int size = (int)count;
         
         if(count <= 0) {
            return new String();
         }
         return convert(source, charset, size);    
      }

      /**
       * This method is used so that a buffer can be represented as a
       * stream of bytes. This provides a quick means to access the data
       * that has been written to the buffer. It wraps the buffer within
       * an input stream so that it can be read directly.
       *
       * @return a stream that can be used to read the buffered bytes
       */ 
      public InputStream open() throws IOException {
         InputStream source = new FileInputStream(file);
         long length = last - first;
         
         if(first > 0) {
            source.skip(first);
         }
         return new Range(source, length);
      }

      /**
       * This will clear all data from the buffer. This simply sets the
       * count to be zero, it will not clear the memory occupied by the
       * instance as the internal buffer will remain. This allows the
       * memory occupied to be reused as many times as is required.
       */  
      public void clear() throws IOException {
         if(closed) {
            throw new BufferException("Buffer is closed");
         }
      }

      /**
       * This method is used to ensure the buffer can be closed. Once
       * the buffer is closed it is an immutable collection of bytes and
       * can not longer be modified. This ensures that it can be passed
       * by value without the risk of modification of the bytes.
       */   
      public void close() throws IOException {
         if(!closed) {
            closed = true;
         }
         if(segment != null) {
            segment.close();
         }
      }

      /**
       * This determines how much space is left in the buffer. If there
       * is no limit to the buffer size this will return the maximum
       * long value. Typically this is the capacity minus the length.
       *
       * @return this is the space that is available within the buffer
       */
      public long space() {
         return Long.MAX_VALUE;
      }
      
      /**
       * This is used to provide the number of bytes that have been
       * written to the buffer. This increases as bytes are appended
       * to the buffer. if the buffer is cleared this resets to zero.
       *  
       * @return this returns the number of bytes within the buffer
       */
      public long length() {
         return last - first;
      }
      
   }
   
   /**
    * The <code>Range</code> object is used to provide a stream that
    * can read a range of bytes from a provided input stream. This
    * allows buffer segments to be allocated from the main buffer.
    * Providing a range in this manner ensures that only one backing
    * file is needed for the primary buffer allocated. 
    */
   private class Range extends FilterInputStream {
        
      /**
       * This is the length of the bytes that exist in the range.
       */
      private long length;
      
      /**
       * This is used to close the stream once it has been read.
       */
      private boolean closed;
      
      /**
       * Constructor for the <code>Range</code> object. This ensures
       * that only a limited number of bytes can be consumed from a
       * backing input stream giving the impression of an independent
       * stream of bytes for a segmented region of the parent buffer.
       * 
       * @param source this is the input stream used to read data
       * @param length this is the number of bytes that can be read
       */ 
      public Range(InputStream source, long length) {
         super(source);
         this.length = length;
      }
    
      /**
       * This will read data from the underlying stream up to the 
       * number of bytes this range is allowed to read. When all of
       * the bytes are exhausted within the stream this returns -1.
       * 
       * @return this returns the octet from the underlying stream
       */
      @Override
      public int read() throws IOException {
         if(length-- > 0) {
            return in.read();
         }
         if(length <= 0) {
            close();
         }
         return -1;
      }
      
      /**
       * This will read data from the underlying stream up to the 
       * number of bytes this range is allowed to read. When all of
       * the bytes are exhausted within the stream this returns -1.
       * 
       * @param array this is the array to read the bytes in to
       * @param off this is the start offset to append the bytes to
       * @param size this is the number of bytes that are required
       * 
       * @return this returns the number of bytes that were read
       */
      @Override
      public int read(byte[] array, int off, int size) throws IOException {
         int left = (int)Math.min(length, size);
         
         if(left > 0) {
            int count = in.read(array, off, left);
            
            if(count > 0){
               length -= count;
            }
            if(length <= 0) {
               close();
            }
            return count;
         }
         return -1;
      }
      
      /**
       * This returns the number of bytes that can be read from the
       * range. This will be the actual number of bytes the range
       * contains as the underlying file will not block reading.
       * 
       * @return this returns the number of bytes within the range
       */
      @Override
      public int available() throws IOException {
         return (int)length;
      }
      
      /**
       * This is the number of bytes to skip from the buffer. This 
       * will allow up to the number of remaining bytes within the
       * range to be read. When all the bytes have been read this
       * will return zero indicating no bytes were skipped.
       * 
       * @param size this returns the number of bytes to skip
       * 
       * @return this returns the number of bytes that were skipped
       */
      @Override
      public long skip(long size) throws IOException {
         long left = Math.min(length, size);
         long skip = in.skip(left);
         
         if(skip > 0) {
            length -= skip;
         } 
         if(length <= 0) {
            close();
         }
         return skip;
      }
      
      /**
       * This is used to close the range once all of the content has
       * been fully read. The <code>Range</code> object forces the
       * close of the stream once all the content has been consumed 
       * to ensure that excessive file descriptors are used. Also
       * this will ensure that the files can be deleted.
       */
      @Override
      public void close() throws IOException {
         if(!closed) {
            in.close();
            closed =true;
         }
      }
   }
}
