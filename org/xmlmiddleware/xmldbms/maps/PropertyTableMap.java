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
 * Maps a column in a property table to an element type, attribute, or PCDATA; <a 
 * href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>PropertyTableMap inherits from PropertyMapBase. See that class for methods to
 * get the column and to get and set the element type name, order information,
 * and attribute multi-valuedness.</p>
 *
 * <p>PropertyTableMaps are stored in ClassTableMaps and ElementInsertionMaps.</p>
 *
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class PropertyTableMap extends PropertyMapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   // The following variables are inherited from PropertyMapBase

//   private XMLName   xmlName = null;
//   private Column    column = null;
//   private int       type = UNKNOWN;
//   private OrderInfo orderInfo = null;
//   private boolean   multiValued = false;

   // The following variables are new to PropertyTableMap

   private LinkInfo linkInfo = null;
   private Table    table = null;

   ClassTableMap parentClassTableMap = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private PropertyTableMap(Table table)
   {
      this.table = table;
   }

   /**
    * Create a new PropertyTableMap.
    *
    * @param table The table being mapped.
    *
    * @return The PropertyTableMap.
    */
   public static PropertyTableMap create(Table table)
   {
      checkArgNull(table, ARG_TABLE);
      return new PropertyTableMap(table);
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
    * @exception MapException Thrown if the element type, attribute, or PCDATA has
    *    already been mapped.
    */
   public void setXMLName(String uri, String localName, int type)
      throws MapException
   {
      setXMLName(XMLName.create(uri, localName), type);
   }

   /**
    * Set the name and type of the XML construct being mapped.
    *
    * @param xmlName XMLName of the XML construct to which the column is mapped.
    *    Must be null for PCDATA.
    * @param type Type of the XML construct to which the column mapped. One of
    *    ColumnMap.ELEMENTTYPE, ColumnMap.ATTRIBUTE, or ColumnMap.PCDATA.
    * @exception MapException Thrown if the element type, attribute, or PCDATA has
    *    already been mapped.
    */
   public void setXMLName(XMLName xmlName, int type)
      throws MapException
   {
      if (parentClassTableMap != null)
      {
         if (parentClassTableMap.xmlNameInDBPropertyMap(xmlName, type))
            throw new MapException(getXMLObjectName(type) + xmlName.getUniversalName() + " already mapped in the ClassTableMap for " + parentClassTableMap.getTable().getUniversalName()
      }
      super.setXMLName(xmlName, type);
   }

   // ********************************************************************
   // Property table
   // ********************************************************************

   /**
    * Get the property table.
    *
    * @return The column.
    */
   public final Table getTable()
   {
      return table;
   }

   // ********************************************************************
   // Property column
   // ********************************************************************

   /**
    * Set the column to which the property is mapped.
    *
    * @param column The column.
    */
   public void setColumn(Column column)
   {
      super.setColumn(column);
   }

   // ********************************************************************
   // Link information
   // ********************************************************************

   /**
    * Get the information used to link the class table and the property table.
    *
    * @return The LinkInfo.
    */
   public final LinkInfo getLinkInfo()
   {
      return linkInfo;
   }

   /**
    * Set the information used to link the class table and the property table.
    *
    * @param linkInfo The LinkInfo.
    */
   public void setLinkInfo(LinkInfo linkInfo)
   {
      this.linkInfo = linkInfo;
   }

}
