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
// This software was originally developed at the Technical University
// of Darmstadt, Germany.

// Version 2.0
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;

/**
 * Inserts an element between the class table element and a child element;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>ElementInsertionMap contains information about an element that is to be
 * inserted between a class table element and an element for a column, related
 * class table, or related property table. It is used to reconstruct elements
 * that are "inlined" with InlineClassMap. Note that it is essentially the
 * opposite of InlineClassMap, rather than providing parallel functionality in
 * the database-centric view of a map. That is, it reconstructs elements, rather
 * than inlining child tables.</p>
 *
 * <p>ElementInsertionMap inherits from ClassTableMapBase. See that class for methods to
 * get the element type name, and to manipulate child maps (column maps, related
 * class table maps, related property table maps, and ElementInsertionMaps).
 * ElementInsertionMap provides methods for getting and setting order information.</p>
 *
 * <p>ElementInsertionMaps are stored in ClassTableMaps and ElementInsertionMaps.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class ElementInsertionMap extends ClassTableMapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   // The following variables are inherited from ClassTableMapBase

//   private XMLName   elementTypeName = null;
//   private Hashtable columnMaps = new Hashtable();
//   private Hashtable relatedClassTableMaps = new Hashtable();
//   private Hashtable relatedPropTableMaps = new Hashtable();
//   private Hashtable elementInsertionMaps = new Hashtable();

   // The following variables are new to ElementInsertionMap

   private OrderInfo orderInfo = null;

   // ********************************************************************
   // Constructor
   // ********************************************************************

   private ElementInsertionMap()
   {
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
      ElementInsertionMap elementInsertionMap = new ElementInsertionMap();
      elementInsertionMap.setElementTypeName(uri, localName);
      return elementInsertionMap;
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
      ElementInsertionMap elementInsertionMap = new ElementInsertionMap();
      elementInsertionMap.setElementTypeName(elementTypeName);
      return elementInsertionMap;
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Order information
   // ********************************************************************

   /**
    * Get the information used to order the inserted element in its parent.
    *
    * @return The order information. Null if the inserted element is not ordered.
    */
   public final OrderInfo getOrderInfo()
   {
      return orderInfo;
   }

   /**
    * Set the information used to order the inserted element in its parent.
    *
    * @param orderInfo The order information. Null if the inserted elementfs is not ordered.
    */
   public void setOrderInfo(OrderInfo orderInfo)
   {
      this.orderInfo = orderInfo;
   }
}
