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
 * Maps a column to an element type, attribute, or PCDATA; <A 
 * HREF="../readme.html#NotForUse">not for general use</A>.
 *
 * <P>ColumnMaps are stored in TableMaps.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class ColumnMap
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Map the column to an element type. */
   public static final int TYPE_TOELEMENTTYPE = 1;

   /** Map the column to an attribute. */
   public static final int TYPE_TOATTRIBUTE = 2;

   /** Map the column to PCDATA. */
   public static final int TYPE_TOPCDATA = 3;

   // ********************************************************************
   // Variables
   // ********************************************************************


   /**
	* What the column is mapped to: TYPE_TOELEMENTTYPE, TYPE_TOATTRIBUTE,
	* or TYPE_TOPCDATA.
	*/
   public int type = 0;

   /** Column object of the column being mapped. */
   public Column column = null;

   /**
	* Column object of the column that contains order information for the
	* mapped column. Null if the there is no order information associated
	* with the mapped column.
	*/
   public Column orderColumn = null;

   /**
	* Unprefixed name of the element type or attribute to which the column
	* is mapped. Null if the column is mapped to PCDATA. In the future,
	* this and prefixedProperty should be replaced with an NSName.
	*
	* @see DBMSToDOM#usePrefixes(boolean)
	*/
   public String property = null;

   /**
	* Prefixed name of the element type or attribute to which the column
	* is mapped. Null if the column is mapped to PCDATA. In the future,
	* this and property should be replaced with an NSName.
	*
	* @see DBMSToDOM#usePrefixes(boolean)
	*/
   public String prefixedProperty = null;

   /**
	* True if the column is mapped to an attribute and that attribute can
	* be multi-valued (IDREFS, NMTOKENS, or ENTITIES). Otherwise false.
	*/
   public boolean multiValued = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /**
	* Construct a ColumnMap with default values.
	*/
   public ColumnMap()
   {
   }   

   /**
	* Construct a ColumnMap with the specified values.
	*/
   public ColumnMap(int type, Column column, Column orderColumn, String property, String prefixedProperty, boolean multiValued)
   {
	  this.type = type;
	  this.column = column;
	  this.orderColumn = orderColumn;
	  this.property = property;
	  this.prefixedProperty = prefixedProperty;
	  this.multiValued = multiValued;
   }   

//   String  passThroughType;
//   This tells us which pass-through type to use. Note that this is different
//   from the pass-through type in TableMap. The pass-through type in TableMap
//   reconstructs the chains of pass-through elements. This pass-through type
//   allows us to choose from those chains. Questions:
//   1) Should this be an array?
//   2) Is the above really correct? If the column has no value, should we
//      reconstruct the chain?
//   String  passThroughType;
//   This tells us which pass-through type to use. Note that this is different
//   from the pass-through type in TableMap. The pass-through type in TableMap
//   reconstructs the chains of pass-through elements. This pass-through type
//   allows us to choose from those chains. Questions:
//   1) Should this be an array?
//   2) Is the above really correct? If the column has no value, should we
//      reconstruct the chain?
}