// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

/**
 * Contains information about an order column;
 * <A HREF="../readme.html#NotForUse"> not for general use</A>.
 *
 * <P>Order occurs in two places in an XML document: the order of child
 * elements and PCDATA in a parent element, and the order of values in
 * multi-valued attributes (IDREFS, NMTOKENS, and ENTITIES). XML-DBMS can
 * save information about this in a column, which is then used when
 * constructing an XML document. Whether this order information is used
 * depends on the mapping: for many documents, especially data-centric
 * documents, order is unimportant and there is no order column.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class OrderInfo
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** Column object for the order column. */
   public Column orderColumn = null;

   /** Whether to generate the order information. */
   public boolean generateOrder = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   public OrderInfo()
   {
   }   
}