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
// Changes from version 1.0: None
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;

/**
 * Maps an element type, attribute, or PCDATA to a column in the class
 * table or a separate property table; <a href="../readme.htm#NotForUse">not
 * for general use</a>.
 *
 * <p>PropertyMap inherits from PropertyMapBase. See that class for methods to
 * get the property name and type, and to get and set the column, order
 * information, and attribute multi-valuedness.</p>
 *
 * <p>PropertyMaps are stored in ClassMaps and InlineClassMaps.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 * @see PropertyMapBase
 */

public class PropertyMap extends PropertyMapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   // The following variables are inherited from PropertyMapBase

//   private XMLName   xmlName = null;
//   private Column    column = null;
//   private int       type = UNKNOWN;
//   private OrderInfo orderInfo = null;
//   private boolean   multiValued = false;

   // The following variables are new to PropertyMap

   private Table     table = null;
   private LinkInfo  linkInfo = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private PropertyMap()
   {
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new PropertyMap.
    *
    * @param uri Namespace URI of the element type or attribute being mapped.
    *    May be null for element types or attribues. Must be null for PCDATA.
    * @param localName Local name of the element type or attribute being mapped.
    *    Must be null for PCDATA.
    * @param type Type of the property being mapped. One of PropertyMap.ELEMENTTYPE,
    *    PropertyMap.ATTRIBUTE, or PropertyMap.PCDATA.
    *
    * @return The PropertyMap.
    */
   public static PropertyMap create(String uri, String localName, int type)
   {
      PropertyMap propMap = new PropertyMap();
      propMap.setXMLName(uri, localName, type);
      return propMap;
   }

   /**
    * Create a new PropertyMap.
    *
    * @param xmlName XMLName of the element type or attribute being mapped.
    *    Must be null for PCDATA.
    * @param type Type of the property being mapped. One of PropertyMap.ELEMENTTYPE,
    *    PropertyMap.ATTRIBUTE, or PropertyMap.PCDATA.
    *
    * @return The PropertyMap.
    */
   public static PropertyMap create(XMLName xmlName, int type)
   {
      PropertyMap propMap = new PropertyMap();
      propMap.setXMLName(xmlName, type);
      return propMap;
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Property table
   // ********************************************************************

   /**
    * Get the property table to which the property is mapped, if any.
    *
    * @return The table. Null if the property is mapped to a column in the class table.
    */
   public final Table getTable()
   {
      return table;
   }

   /**
    * Get the information used to link the class table to the property table, if any.
    *
    * @return The LinkInfo. Null if the property is mapped to a column in the class table.
    */
   public final LinkInfo getLinkInfo()
   {
      return linkInfo;
   }

   /**
    * Map the property to a column in a property table.
    *
    * <p>The arguments must either both be null or both be non-null.</p>
    *
    * @param table The property table. Null to unset the property table.
    * @param linkInfo The link information. Null to unset the link information.
    */
   public void setTable(Table table, LinkInfo linkInfo)
   {
      if (((table != null) && (linkInfo == null)) ||
          ((table == null) && (linkInfo != null)))
         throw new IllegalArgumentException("table and linkInfo must either both be null or both be non-null.");
      this.table = table;
      this.linkInfo = linkInfo;
   }

   // ********************************************************************
   // Property column
   // ********************************************************************

   /**
    * Set the column to which the property is mapped.
    *
    * <p>This column is in the class table or the property table, depending on whether
    * getTable() returns null or not.</p>
    *
    * @param column The column.
    */
   public void setColumn(Column column)
   {
      super.setColumn(column);
   }
}
