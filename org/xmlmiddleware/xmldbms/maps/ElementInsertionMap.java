// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.xmlutils.*;

/**
 * Describes a wrapper element that needs to be inserted;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>ElementInsertionMap contains information about an element that is to be
 * inserted between a class table element and an element, attribute, or PCDATA
 * for a column, related class table, or property table. When retrieving data
 * from the database, it is used to reconstruct elements that were eliminated
 * with WrapperClassMap.</p>
 *
 * <p>Note that an ElementInsertionMap is closer to being the opposite of a
 * WrapperClassMap than it is to providing parallel functionality in
 * the database-centric view of a map. That is, it reconstructs elements, rather
 * than treating child tables as "wrapper" tables.</p>
 *
 * <p>ElementInsertionMaps are stored in ElementInsertionLists.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class ElementInsertionMap extends MapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private XMLName   elementTypeName = null;
   private OrderInfo orderInfo = null;

   // ********************************************************************
   // Constructor
   // ********************************************************************

   private ElementInsertionMap(XMLName elementTypeName)
   {
      this.elementTypeName = elementTypeName;
   }

   /**
    * Create a new ElementInsertionMap.
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    *
    * @return The ElementInsertionMap.
    */
   public static ElementInsertionMap create(String uri, String localName)
   {
      return new ElementInsertionMap(XMLName.create(uri, localName));
   }

   /**
    * Create a new ElementInsertionMap.
    *
    * @param elementTypeName The element type name.
    *
    * @return The ElementInsertionMap.
    */
   public static ElementInsertionMap create(XMLName elementTypeName)
   {
      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      return new ElementInsertionMap(elementTypeName);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Element type name
   // ********************************************************************

   /**
    * Get the name of the element type or attribute being mapped.
    *
    * @return The element type or attribute name. Null if PCDATA is mapped.
    */
   public final XMLName getElementTypeName()
   {
      return elementTypeName;
   }

   // ********************************************************************
   // Order information
   // ********************************************************************

   /**
    * Get the information used to order the wrapper element in its parent.
    *
    * @return The order information. Null if the wrapper element is not ordered.
    */
   public final OrderInfo getOrderInfo()
   {
      return orderInfo;
   }

   /**
    * Set the information used to order the wrapper element in its parent.
    *
    * @param orderInfo The order information. Null if the wrapper element is not ordered.
    */
   public void setOrderInfo(OrderInfo orderInfo)
   {
      this.orderInfo = orderInfo;
   }
}
