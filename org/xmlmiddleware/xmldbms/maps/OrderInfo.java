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

/**
 * Contains information about ordering child elements, PCDATA, or multiple
 * attribute values; <a href="../readme.htm#NotForUse"> not for general use</a>.
 *
 * <p>Order occurs in two places in an XML document: the order of child
 * elements and PCDATA in a parent element, and the order of values in
 * multi-valued attributes (IDREFS, NMTOKENS, and ENTITIES).</p>
 *
 * <p>There are three possible ways to order a given item:</p>
 *
 * <ul>
 * <li>Use an OrderInfo object with an order column. This stores the order in a special
 * column in the database and guarantees that the document can be round-tripped
 * at the elements / attributes / PCDATA level. This method is usually used by
 * document-centric documents.</li>
 *
 * <li>Use an OrderInfo object with a fixed order value. This is useful
 * only for ordering child elements and cannot be used with all content
 * models, but is usually sufficient for data-centric documents that need to
 * be validated.</li>
 *
 * <li>Do not use an OrderInfo object. In this case, items are not
 * ordered. This is usually used by data-centric documents that do not need to
 * be validated.</li>
 * </ul>
 *
 * <p>OrderInfo objects are stored in PropertyMaps, RelatedClassMaps,
 * InlineClassMaps, RelatedTableMaps, and ElementInsertionMaps.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 */

public class OrderInfo extends MapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   private boolean ascending = true;
   private Column  orderColumn = null;
   private boolean generateOrder = false;
   private int     orderValue = 0;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private OrderInfo()
   {
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new OrderInfo object.
    *
    * @return The OrderInfo object.
    */
   public static OrderInfo create()
   {
      return new OrderInfo();
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Order direction
   // ********************************************************************

   /** Get whether the order is ascending.
    *
    * @return Whether the order is ascending.
   **/
   public final boolean isAscending()
   {
      return ascending;
   }

   /** Set whether the order is ascending.
    *
    * @param ascending Whether the order is ascending.
   **/
   public void setIsAscending(boolean ascending)
   {
      this.ascending = ascending;
   }

   // ********************************************************************
   // Fixed order
   // ********************************************************************

   /**
    * Whether a fixed order value or an order column is used.
    *
    * @return Whether a fixed order value is used.
    */
   public final boolean orderValueIsFixed()
   {
      return (orderColumn == null);
   }

   /**
    * Get the fixed order value.
    *
    * <p>This method should be called only if a fixed order value is used -- that
    * is, if orderValueIsFixed() returns true. If orderValueIsFixed() returns false,
    * the returned value is undefined.</p>
    *
    * @return The fixed order value. This is 0 by default.
    */
   public final int getFixedOrderValue()
   {
      return orderValue;
   }

   /**
    * Set the fixed order value.
    *
    * <p>This method sets the order column to null and whether to generate
    * order to false.</p>
    *
    * @param orderValue The fixed order value.
    */
   public void setFixedOrderValue(int orderValue)
   {
      this.orderValue = orderValue;
      this.orderColumn = null;
      this.generateOrder = false;
   }

   // ********************************************************************
   // Order column
   // ********************************************************************

   /**
    * Get the order column.
    *
    * <p>This method should be called only if an order column is used -- that
    * is, if orderValueIsFixed() returns false. If orderValueIsFixed() returns true,
    * the returned value is undefined.</p>
    *
    * @return The order column.
    */
   public final Column getOrderColumn()
   {
      return orderColumn;
   }

   /**
    * Set the order column.
    *
    * <p>This method sets the fixed order value to 0.</p>
    *
    * @param orderColumn The order column.
    */
   public void setOrderColumn(Column orderColumn)
   {
      checkArgNull(orderColumn, ARG_ORDERCOLUMN);
      this.orderColumn = orderColumn;
      this.orderValue = 0;
   }

   // ********************************************************************
   // Order generation
   // ********************************************************************

   /**
    * Get whether the order column value is generated.
    *
    * <p>This method should be called only if an order column is used -- that
    * is, if orderValueIsFixed() returns false. If orderValueIsFixed() returns true,
    * the returned value is undefined.</p>
    *
    * @return Whether the order column value is generated.
    */
   public boolean generateOrder()
   {
      return generateOrder;
   }

   /**
    * Set whether to generate the order column value.
    *
    * <p>This method can be called only if an order column is used -- that
    * is, if orderValueIsFixed() returns false.</p>
    *
    * @param generateOrder Whether to generate the order column value. If the
    *    order value is not generated, the order column must also be mapped as
    *    a data column with a PropertyMap or ColumnMap.
    */
   public void setGenerateOrder(boolean generateOrder)
   {
      if (orderValueIsFixed())
         throw new IllegalStateException("setGenerateOrder() can be called only when an order column is used. Call orderValueIsFixed() to determine if an order column is used.");
      this.generateOrder = generateOrder;
   }
}
