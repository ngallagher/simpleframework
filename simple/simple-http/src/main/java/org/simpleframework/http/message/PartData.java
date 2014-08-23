/*
 * PartData.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.message;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.common.KeyMap;
import org.simpleframework.http.Part;

/**
 * The <code>PartData</code> object represents an ordered list of 
 * parts that were uploaded within a HTTP entity body. This allows 
 * the parts to be iterated over, or if required accessed by name. 
 * In order to access the <code>Part</code> object by name it must 
 * have had a name within the Content-Disposition header.
 * 
 * @author Niall Gallagher
 */
class PartData implements PartSeries {

   /**
    * This is the key map that is used to store the part objects.
    */
   private final KeyMap<Part> map;
   
   /**
    * This is the list of attachments for this part list object.
    */
   private final List<Part> list;

   /**
    * Constructor for the <code>PartData</code> object. This is used
    * to create an order list of parts that is used by the request 
    * to access the individual parts uploaded with a HTTP body.
    */
   public PartData() {
      this.list = new ArrayList<Part>();
      this.map = new KeyMap<Part>();
   }

   /**
    * This is used to acquire the attachments associated with this 
    * list. If no parts have been collected by this list then it
    * will return an empty list. The order of the parts in the list
    * are the insertion order for consistency.
    * 
    * @return this returns the parts collected in iteration order
    */
   public List<Part> getParts() {
      return list;
   }
   
   /**
    * This is used to add a part to the list. The order the parts are 
    * added to the list is the iteration order. If the part has a name
    * that is not null then it is added to an internal map using that 
    * name. This allows it to be accesses by name at a later time.
    * 
    * @param part this is the part that is to be added to the list
    * 
    * @return returns true if the list has changed due to the add
    */
   public boolean addPart(Part part) {
      String name = part.getName();

      if(name != null) {
         map.put(name, part);
      }
      return list.add(part);
   }

   /**
    * This method is used to acquire a <code>Part</code> from the list
    * using a known name for the part. This is a convenient way to 
    * access a part when the name for the part is known.
    * 
    * @param name this is the name of the part to acquire
    * 
    * @return the named part or null if the part does not exist
    */
   public Part getPart(String name) {
      return map.get(name);
   }
}
