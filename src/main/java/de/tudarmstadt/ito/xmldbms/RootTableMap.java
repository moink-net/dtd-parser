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
 * Provides information needed to retrieve data from root tables beyond
 * that found in TableMap; <A HREF="../readme.html#NotForUse">not for general
 * use</A>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class RootTableMap
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** TableMap for the root table. */
   public TableMap tableMap = null;

   /**
	* The unprefixed name of the ignored root element type to be constructed
	* as a parent of the root table's element type. In the future, this and
	* prefixedIgnoredRootType should probably be replaced by an NSName.
	*
	* @see DBMSToDOM#usePrefixes(boolean)
	*/
   public String ignoredRootType = null;

   /**
	* The prefixed name of the ignored root element type to be constructed
	* as a parent of the root table's element type. In the future, this and
	* ignoredRootType should probably be replaced by an NSName.
	*
	* @see DBMSToDOM#usePrefixes(boolean)
	*/
   public String prefixedIgnoredRootType = null;

   /** The key used to retrieve data from the table. */
   public Column[] candidateKey = null;

   /**
	* The column used to order column retrieved from the table. Null if
	* there is no order column.
	*/
   public Column orderColumn = null;

   // ********************************************************************
   // Constants
   // ********************************************************************

   /** Construct a RootTableMap with null initial values. */
   public RootTableMap()
   {
   }   

   /** Construct a RootTableMap with non-null initial values. */
   public RootTableMap(TableMap tableMap, String ignoredRootType, String prefixedIgnoredRootType, Column[] candidateKey, Column orderColumn)
   {
	  this.tableMap = tableMap;
	  this.ignoredRootType = ignoredRootType;
	  this.prefixedIgnoredRootType = prefixedIgnoredRootType;
	  this.candidateKey = candidateKey;
	  this.orderColumn = orderColumn;
   }   
}