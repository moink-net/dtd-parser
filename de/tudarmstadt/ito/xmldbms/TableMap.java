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
 * Maps a table to a class or property; <A HREF="../readme.html#NotForUse">
 * not for general use</A>.
 *
 * <P>TableMaps contain all the information necessary to map a table to
 * a class or property: table information, class (element type) name,
 * column maps, information about related tables, and so on. Note that
 * the arrays containing information about related tables (relatedTables,
 * parentKeyIsCandidate, parentKeys, childKeys, and orderColumns) are read
 * in parallel and must contain the same number of entries in the same
 * order.</P>
 *
 * <P>TableMaps are stored in an array in the Map class and in the
 * relatedTableMaps hash table in TableMap, which is keyed by table name.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class TableMap
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Map the table to a class. */
   public static final int TYPE_CLASSTABLE    = 1;

   /** Map the table to a property. */
   public static final int TYPE_PROPERTYTABLE = 2;

   // ********************************************************************
   // Variables
   // ********************************************************************

   /** Table object of the mapped table. */
   public Table table = null;

   /**
	* What the table is mapped to: TYPE_CLASSTABLE or TYPE_PROPERTYTABLE.
	*/
   public int type = 0;

   /**
	* Unprefixed name of the element type to which a class table is
	* mapped. Null if the table is mapped to a property. In the future,
	* this and prefixedElementType should probably be replaced by an NSName.
	*
	* @see DBMSToDOM#usePrefixes(boolean)
	*/
   public String elementType = null;

   /**
	* Prefixed name of the element type to which a class table is
	* mapped. Null if the table is mapped to a property. In the future,
	* this and elementType should probably be replaced by an NSName.
	*
	* @see DBMSToDOM#usePrefixes(boolean)
	*/
   public String prefixedElementType = null;

   /**
	* ColumnMaps for each column in the table.
	*
	* <P>These must be ordered such that all columns mapped to element types
	* occur before any columns mapped to attributes of these elements or
	* PCDATA. This guarantees that, when retrieving data from the database,
	* the desired element will be available when the attribute column is
	* processed.</P>
	*
	* <P>A table mapped to a property can contain only one ColumnMap -- that of
	* the column containing the property. In the future, if we allow attributes
	* of element types-as-properties to be stored, then one column map of the
	* property table would map to an element type and all other column maps
	* would map to attributes of that element type.</P>
	*/
   public ColumnMap[] columnMaps = null;

   // Currently, a property table cannot have any related tables. In the future,
   // this will be possible if we allow the attributes of element types mapped
   // as properties to also be stored. In this case, the related tables of a
   // property table would contain attribute values, such as those of a
   // multi-valued attribute.
   //
   // orderColumn provides the order of the elements created for each row in a
   // class table. This will never be used for tables related to a property
   // table, since property order information is stored in ColumnMap on a per-
   // property basis.

   /**
	* An array containing a TableMap for each related property table or
	* class table.
	*/
   public TableMap[] relatedTables = null;

   /**
	* An array stating whether the parent table or the child table contains
	* the candidate key. The parent table is the table described in this
	* TableMap; the child table is the table described in the TableMap in
	* relatedTables. The array must match relatedTables in size and order.
	*/
   public boolean[] parentKeyIsCandidate = null;

   /**
	* An array containing the Columns in each parent key used to join the
	* parent table to each child table (see parentKeyIsCandidate). The
	* outer array must match relatedTables in size and order.
	*/
   public Column[][] parentKeys = null;

   /**
	* An array containing the Columns in each child key used to join the
	* parent table to the child table (see parentKeyIsCandidate). The
	* outer array must match relatedTables in size and order.
	*/
   public Column[][] childKeys = null;

   /**
	* An array containing order Columns and/or nulls. For related class
	* tables, an entry can be a Column that describes the order in which
	* the class elements are to be created in the parent element or null
	* if there is no such column. For related property tables, an entry
	* is always null, as property order information is stored in the
	* ColumnMap for the column corresponding to the property.
	*/

   public Column[] orderColumns = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a TableMap. */
   public TableMap()
   {
   }   

   /** Construct and initialize a TableMap. */
   public TableMap(Table table, int type, String elementType, String prefixedElementType, ColumnMap[] columnMaps, int numRelatedTables)
   {
	  this.table = table;
	  this.type = type;
	  this.elementType = elementType;
	  this.prefixedElementType = prefixedElementType;
	  this.columnMaps = columnMaps;
	  this.relatedTables = new TableMap[numRelatedTables];
	  this.parentKeyIsCandidate = new boolean[numRelatedTables];
	  this.parentKeys = new Column[numRelatedTables][];
	  this.childKeys = new Column[numRelatedTables][];
	  this.orderColumns = new Column[numRelatedTables];
   }   

//   String[][]     passThroughTypes = null;
//
// passThroughTypes allows us to reconstruct empty passthrough elements.
// The double array is needed because there can be an arbitrary number of
// pass-through elements directly beneath the parent (first array), each of
// which is a linear array of pass-through elements. That is, the following
// structure could be flattened to a single table A with one column E:
//
// <A>
//    <B>
//       <C>
//          <D>
//             <E>foo</E>
//          </D>
//       </C>
//    </B>
// </A>
//
// The only limitation is that there be one element of each pass-through type
// per parent. We need this limitation to be able to reconstruct the chain of
// ancestors. Otherwise, we couldn't tell the difference between the following
// two structures, in which <B> is passed through. Note that we need to check
// this condition both at map compile time and at run time.
//
// <A>                <A>
//    <B>                <B>
//       <C>foo</C>         <C>foo</C>
//       <D>bar</D>      </B>
//    </B>               <B>
// </A>                     <D>bar</D>
//                       </B>
//                    </A>

//   String[][]     passThroughTypes = null;
//
// passThroughTypes allows us to reconstruct empty passthrough elements.
// The double array is needed because there can be an arbitrary number of
// pass-through elements directly beneath the parent (first array), each of
// which is a linear array of pass-through elements. That is, the following
// structure could be flattened to a single table A with one column E:
//
// <A>
//    <B>
//       <C>
//          <D>
//             <E>foo</E>
//          </D>
//       </C>
//    </B>
// </A>
//
// The only limitation is that there be one element of each pass-through type
// per parent. We need this limitation to be able to reconstruct the chain of
// ancestors. Otherwise, we couldn't tell the difference between the following
// two structures, in which <B> is passed through. Note that we need to check
// this condition both at map compile time and at run time.
//
// <A>                <A>
//    <B>                <B>
//       <C>foo</C>         <C>foo</C>
//       <D>bar</D>      </B>
//    </B>               <B>
// </A>                     <D>bar</D>
//                       </B>
//                    </A>

}