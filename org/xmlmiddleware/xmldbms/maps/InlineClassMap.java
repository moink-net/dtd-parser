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
 * Inlines an element type that might otherwise be mapped as a class;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>InlineClassMap is a cross between a ClassMap and a
 * PropertyMap. It is similar to a ClassMap in that it contains lists of
 * PropertyMaps, RelatedClassMaps, and InlineClassMaps for child elements,
 * PCDATA, and attributes. It differs in that it doesn't have a table or base
 * class property. Instead, its children are mapped to columns in the table
 * of its parent class. This effectively "inlines" the element, treating it
 * as if it didn't exist and its child elements, PCDATA, and attributes
 * belonged to its parent.</p>
 *
 * <p>An InlineClassMap is similar to a PropertyMap in that it contains order
 * information. This allows inlined elements to be placed in the correct
 * order in their parent.</p>
 *
 * <p>For example, consider the following Book element:
 *
 * <pre>
 *    &lt;Book>
 *       &lt;Title>Programming with XML-DBMS&lt;/Title>
 *       &lt;Author>
 *          &lt;LastName>Bourret&lt;/LastName>
 *          &lt;FirstName>Ronald&lt;/FirstName>
 *       &lt;/Author>
 *    &lt;/Book>
 * </pre>
 *
 * <p>Normally, the Author element type would be mapped to its own table. If
 * the Author element type is inlined, the references to the LastName and
 * FirstName element types are mapped to columns in the Books table, as if
 * the Author element did not exist.</p>
 *
 * <p>Inlined element types can be nested arbitrarily deep. That is, an
 * inlined element type may have inlined child elements. However, there can
 * be at most one inlined element of a given type in any parent. If more
 * than one inlined element of the same type were allowed in a given parent,
 * extra information (which XML-DBMS does not store) would need to be stored
 * in the database to reconstruct the inlined elements. (A single inlined
 * element can be reconstructed without such information.) This restriction
 * is unlikely to cause problems for most data-centric documents.</p>
 *
 * <p>InlineClassMap inherits from ClassMapBase, which provides accessors
 * element type name and accessors and mutators for attribute, PCDATA, and
 * child element type maps. It adds accessors and mutators for order information.</p>
 *
 * <p>InlineClassMaps are stored in ClassMaps and InlineClassMaps.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class InlineClassMap extends ClassMapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   // The following variables are inherited from ClassMapBase

//   private XMLName     elementTypeName = null;
//   private Hashtable   attributeMaps = new Hashtable();
//   private PropertyMap pcdataMap = null;
//   private Hashtable   childMaps = new Hashtable(); // For child element types

   // The following variables are new to InlineClassMap

   private OrderInfo orderInfo = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private InlineClassMap(XMLName elementTypeName)
   {
      super(elementTypeName);
   }

   /**
    * Create a new InlineClassMap.
    *
    * @param uri Namespace URI of the element type being mapped. May be null.
    * @param localName Local name of the element type being mapped.
    *
    * @return The InlineClassMap.
    */
   public static InlineClassMap create(String uri, String localName)
   {
      return new InlineClassMap(XMLName.create(uri, localName, null));
   }

   /**
    * Create a new InlineClassMap.
    *
    * @param elementTypeName The element type name.
    *
    * @return The InlineClassMap.
    */
   public static InlineClassMap create(XMLName elementTypeName)
   {
      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      return new InlineClassMap(elementTypeName);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Order information
   // ********************************************************************

   /**
    * Get the information used to order the inlined element in its parent.
    *
    * @return The order information. Null if the inlined element is not ordered.
    */
   public final OrderInfo getOrderInfo()
   {
      return orderInfo;
   }

   /**
    * Set the information used to order the inlined element in its parent.
    *
    * @param orderInfo The order information. Null if the inlined element is not ordered.
    */
   public void setOrderInfo(OrderInfo orderInfo)
   {
      this.orderInfo = orderInfo;
   }
}

