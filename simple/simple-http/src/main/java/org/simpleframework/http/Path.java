/*
 * Path.java February 2001
 *
 * Copyright (C) 2001, Niall Gallagher <niallg@users.sf.net>
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
 
package org.simpleframework.http;

/**
 * The <code>Path</code> represents the path part of a URI. This provides 
 * the various components of the URI path to the user. The normalization
 * of the path is the conversion of the path given into it's actual path by
 * removing the references to the parent directories and to the current dir.
 * <p>
 * If the path that this represents is <code>/usr/bin/../etc/./README</code>
 * then the actual path, normalized, is <code>/usr/etc/README</code>. Once
 * the path has been normalized it is possible to acquire the segments as
 * an array of strings, which allows simple manipulation of the path.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.parse.PathParser
 */
public interface Path {
    
   /**
    * This will return the extension that the file name contains.
    * For example a file name <code>file.en_US.extension</code>
    * will produce an extension of <code>extension</code>. This 
    * will return null if the path contains no file extension.
    *
    * @return this will return the extension this path contains
    */   
   String getExtension();
    
   /**
    * This will return the full name of the file without the path.
    * As regargs the definition of the path in RFC 2396 the name
    * would be considered the last path segment. So if the path 
    * was <code>/usr/README</code> the name is <code>README</code>.
    * Also for directorys the name of the directory in the last
    * path segment is returned. This returns the name without any
    * of the path parameters. As RFC 2396 defines the path to have
    * path parameters after the path segments.
    *
    * @return this will return the name of the file in the path
    */    
   String getName();

   /**
    * This will return the normalized path. The normalized path is
    * the path without any references to its parent or itself. So
    * if the path to be parsed is <code>/usr/../etc/./</code> the
    * path is <code>/etc/</code>. If the path that this represents
    * is a path with an immediate back reference then this will
    * return null. This is the path with all its information even
    * the parameter information if it was defined in the path.
    *
    * @return this returns the normalize path without
    *    <code>../</code> or <code>./</code>
    */
   String getPath();
   
   /**
    * This will return the normalized path from the specified path
    * segment. This allows various path parts to be acquired in an
    * efficient means what does not require copy operations of the
    * use of <code>substring</code> invocations. Of particular
    * interest is the extraction of context based paths. This is
    * the path with all its information even the parameter 
    * information if it was defined in the path.
    *
    * @param from this is the segment offset to get the path for
    *
    * @return this returns the normalize path without
    *    <code>../</code> or <code>./</code>
    */
   String getPath(int from);
   
   /**
    * This will return the normalized path from the specified path
    * segment. This allows various path parts to be acquired in an
    * efficient means what does not require copy operations of the
    * use of <code>substring</code> invocations. Of particular
    * interest is the extraction of context based paths. This is
    * the path with all its information even the parameter 
    * information if it was defined in the path.
    *
    * @param from this is the segment offset to get the path for
    * @param count this is the number of path segments to include
    *
    * @return this returns the normalize path without
    *    <code>../</code> or <code>./</code>
    */
   String getPath(int from, int count);

   /**
    * This method is used to break the path into individual parts
    * called segments, see RFC 2396. This can be used as an easy
    * way to compare paths and to examine the directory tree that
    * the path points to. For example, if an path was broken from
    * the string <code>/usr/bin/../etc</code> then the segments
    * returned would be <code>usr</code> and <code>etc</code> as
    * the path is normalized before the segments are extracted.
    *
    * @return return all the path segments within the directory
    */
   String[] getSegments();

   /**
    * This will return the highest directory that exists within 
    * the path. This is used to that files within the same path
    * can be acquired. An example of that this would do given
    * the path <code>/pub/./bin/README</code> would be to return
    * the highest directory path <code>/pub/bin/</code>. The "/"
    * character will allways be the last character in the path.
    *
    * @return this method will return the highest directory
    */
   String getDirectory();

   /**
    * This will return the path as it is relative to the issued
    * path. This in effect will chop the start of this path if
    * it's start matches the highest directory of the given path
    * as of <code>getDirectory</code>. This is useful if paths 
    * that are relative to a specific location are required. To
    * illustrate what this method will do the following example
    * is provided. If this object represented the path string
    * <code>/usr/share/rfc/rfc2396.txt</code> and the issued
    * path was <code>/usr/share/text.txt</code> then this will
    * return the path string <code>/rfc/rfc2396.txt</code>.
    *
    * @param path the path prefix to acquire a relative path
    *
    * @return returns a path relative to the one it is given
    * otherwize this method will return null 
    */
   String getRelative(String path);

   /**
    * This will return the normalized path. The normalized path is
    * the path without any references to its parent or itself. So
    * if the path to be parsed is <code>/usr/../etc/./</code> the
    * path is <code>/etc/</code>. If the path that this represents
    * is a path with an immediate back reference then this will
    * return null. This is the path with all its information even
    * the parameter information if it was defined in the path.
    *
    * @return this returns the normalize path without
    *    <code>../</code> or <code>./</code>
    */
   String toString();
}    
