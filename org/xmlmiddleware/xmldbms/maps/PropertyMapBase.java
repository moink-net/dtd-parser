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
 * Base class for PropertyMap and ColumnMap;
 * <a href="../readme.htm#NotForUse">not for general use</a>.
 *
 * <p>PropertyMapBase provides accessors and mutators for XML construct name
 * and type and for order information. It is the base class for PropertyMap
 * and ColumnMap.</p>
 *
 * <p>PropertyMapBase is never used directly. It is only used through
 * PropertyMap and ColumnMap.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 * @see PropertyMap
 * @see ColumnMap
 */

public class PropertyMapBase extends MapBase
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Unknown mapping. */
   public static final int UNKNOWN = 0;

   /** Mapping between a column and an element type. */
   public static final int ELEMENTTYPE = 1;

   /** Mapping between a column and an attribute. */
   public static final int ATTRIBUTE = 2;

   /** Mapping between a column and PCDATA. */
   public static final int PCDATA = 3;

   // ********************************************************************
   // Private variables
   // ********************************************************************

   private XMLName   xmlName = null;
   private Column    column = null;
   private int       type = UNKNOWN;
   private OrderInfo orderInfo = null;
   private boolean   multiValued = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   PropertyMapBase()
   {
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // XML name and type
   // ********************************************************************

   /**
    * Get the name of the element type or attribute being mapped.
    *
    * @return The element type or attribute name. Null if PCDATA is mapped.
    */
   public final XMLName getXMLName()
   {
      return xmlName;
   }

   /**
    * Get the type of the XML construct being mapped: ELEMENTTYPE, ATTRIBUTE, or PCDATA.
    *
    * @return The type.
    */
   public final int getType()
   {
      return type;
   }

   // PROTECTED! Called by PropertyMap.create() and ColumnMap.setXMLName().

   void setXMLName(String uri, String localName, int type)
   {
      XMLName xmlName;

      if ((type == PCDATA) && ((uri != null) || (localName != null)))
         throw new IllegalArgumentException("URI and local name must be null when type is PCDATA.");

      xmlName = (type == PCDATA) ? null : XMLName.create(uri, localName);
      setXMLName(xmlName, type);
   }

   void setXMLName(XMLName xmlName, int type)
   {
      if ((type == ELEMENTTYPE) || (type == ATTRIBUTE))
      {
         checkArgNull(xmlName, ARG_XMLNAME);
      }
      else if (type == PCDATA)
      {
         if (xmlName != null)
            throw new IllegalArgumentException("xmlName name must be null when type is PCDATA.");
      }
      else
         throw new IllegalArgumentException("Type must be ELEMENTTYPE, ATTRIBUTE, or PCDATA.");
      this.xmlName = xmlName;
      this.type = type;
   }

   // ********************************************************************
   // Property column
   // ********************************************************************

   // In PropertyMap and PropertyTableMap, the column is set with the
   // setColumn() method. In ColumnMap it is set with the create() method.

   /**
    * Get the column to which the property is mapped.
    *
    * @return The column.
    */
   public final Column getColumn()
   {
      return column;
   }

   // PROTECTED! Called by ColumnMap.create() and PropertyMap.setColumn().

   void setColumn(Column column)
   {
      checkArgNull(column, ARG_COLUMN);
      this.column = column;
   }

   // ********************************************************************
   // Order information
   // ********************************************************************

   /**
    * Get the information used to order the element or PCDATA in its parent.
    *
    * <p>Applies only to element types and PCDATA. The result of calling
    * this method for attributes is undefined.</p>
    *
    * @return The order information. Null if the property is not ordered.
    */
   public final OrderInfo getOrderInfo()
   {
      return orderInfo;
   }

   /**
    * Set the information used to order the element or PCDATA in its parent.
    *
    * <p>Applies only to element types and PCDATA.</p>
    *
    * @param orderInfo The order information. Null if the property is not ordered.
    */
   public void setOrderInfo(OrderInfo orderInfo)
   {
      if (type == ATTRIBUTE)
         throw new IllegalStateException("Cannot call setOrderInfo(OrderInfo) on PropertyMaps for attributes.");
      this.orderInfo = orderInfo;
   }

   // ********************************************************************
   // Attribute multi-valuedness
   // ********************************************************************

   /**
    * Is an attribute multi-valued or not?
    *
    * <p>Applies only to attributes. The result of calling this method for
    * element types or PCDATA is undefined.</p>
    *
    * @return Whether an attribute is multi-valued.
    */
   public final boolean attributeIsMultiValued()
   {
      return multiValued;
   }

   /**
    * Sets whether an attribute multi-valued or not.
    *
    * <p>Applies only to attributes.</p>
    *
    * @param multiValued Whether an attribute is multi-valued.
    */
   public void setAttributeIsMultiValued(boolean multiValued)
   {
      if (type != ATTRIBUTE)
         throw new IllegalStateException("Cannot call setAttributeIsMultiValued(boolean) on PropertyMaps for element types or PCDATA.");
      this.multiValued = multiValued;
   }
}
