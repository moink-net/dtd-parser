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
 * Maps a column to an element type, attribute, or PCDATA; <a 
 * href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>ColumnMap inherits from PropertyMapBase. See that class for methods to
 * get the column and to get and set the element type name, order information,
 * and attribute multi-valuedness.</p>
 *
 * <p>ColumnMaps are stored in ClassTableMaps and ElementInsertionMaps.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 * @see PropertyMapBase
 */

public class ColumnMap extends PropertyMapBase
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

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private ColumnMap()
   {
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new ColumnMap.
    *
    * @param column The column being mapped.
    *
    * @return The ColumnMap.
    */
   public static ColumnMap create(Column column)
   {
      ColumnMap columnMap = new ColumnMap();
      columnMap.setColumn(column);
      return columnMap;
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // XML name and type
   // ********************************************************************

   /**
    * Set the name and type of the XML construct being mapped.
    *
    * @param uri Namespace URI of the XML construct to which the column is mapped.
    *    May be null for element types or attributes. Must be null for PCDATA.
    * @param localName Local name of the XML construct to which the column mapped.
    *    Must be null for PCDATA.
    * @param type Type of the XML construct to which the column mapped. One of
    *    ColumnMap.ELEMENTTYPE, ColumnMap.ATTRIBUTE, or ColumnMap.PCDATA.
    */
   public void setXMLName(String uri, String localName, int type)
   {
      super.setXMLName(uri, localName, type);
   }

   /**
    * Set the name and type of the XML construct being mapped.
    *
    * @param xmlName XMLName of the XML construct to which the column is mapped.
    *    Must be null for PCDATA.
    * @param type Type of the XML construct to which the column mapped. One of
    *    ColumnMap.ELEMENTTYPE, ColumnMap.ATTRIBUTE, or ColumnMap.PCDATA.
    */
   public void setXMLName(XMLName xmlName, int type)
   {
      super.setXMLName(xmlName, type);
   }
}
