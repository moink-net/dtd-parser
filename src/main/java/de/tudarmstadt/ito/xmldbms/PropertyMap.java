// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

import de.tudarmstadt.ito.utils.NSName;

/**
 * Maps an element type, attribute, or PCDATA as a property; <A
 * HREF="../readme.html#NotForUse">not for general use</A>.
 *
 * <P>PropertyMap contains information about an element type-as-property,
 * attribute, or PCDATA: how it is mapped, what table/column it is mapped
 * to, etc. Note that the name of the mapped element type or attribute is
 * not actually stored in the class; PropertyMaps are stored in hash tables
 * in ClassMaps that are keyed by element type or attribute name.</p>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class PropertyMap
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Map the property to a column in the class table. */
   public static final int TYPE_TOCOLUMN = 1;

   /** Map the property to a column in a separate property table. */
   public static final int TYPE_TOPROPERTYTABLE = 2;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /**
	* Name of the property. This is the element or attribute name; it is
	* ignored for PCDATA. It includes full namespace information.
	*/
   public NSName name = null;

   /** The map type: TYPE_TOCOLUMN or TYPE_TOPROPERTYTABLE. */
   public int type = 0;

   /**
	* A Table object describing the property table. This is null if the
	* property is mapped to a column in the class table.
	*/
   public Table table = null;

   /**
	* A LinkInfo object containing the information needed to link the
	* class table to the property table. Null if the property is mapped to
	* a column in the class table.
	*/
   public LinkInfo linkInfo = null;

   /**
	* A Column object describing the column to which the property is mapped.
	* Depending on the type variable, this column is either in the class
	* table or a separate property table.
	*/
   public Column column = null;

   /**
	* An OrderInfo object describing the column containing the order
	* information for the property. If there is no order information, this
	* object still exists, but has its orderColumn member set to null.
	*/
   public OrderInfo orderInfo = new OrderInfo();

   /**
	* True if the property is a multi-valued attribute (IDREFS, NMTOKENS, or
	* ENTITIES); otherwise false.
	*/
   public boolean multiValued = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   public PropertyMap()
   {
   }   
}