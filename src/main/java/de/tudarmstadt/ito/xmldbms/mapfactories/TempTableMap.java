// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.TableMap; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempTableMap
{
   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempTable  table = null;
   int        type = 0;
   String     elementType = null;
   String     prefixedElementType = null;

   Hashtable  elementTypeColumnMaps = new Hashtable();
   Hashtable  propertyColumnMaps = new Hashtable();

   Vector     relatedTables = new Vector(); // Contains TempTableMaps
   Vector     parentKeyIsCandidate = new Vector();
   Vector     parentKeys = new Vector();    // Contains Vectors of TempColumns
   Vector     childKeys = new Vector();     // Contains Vectors of TempColumns
   Vector     orderColumns = new Vector();

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempTableMap()
   {
   }   

   TempTableMap (TempTable table)
   {
	  this.table = table;
   }   

   //***********************************************************************
   // Methods
   //***********************************************************************

   TempColumnMap addElementTypeColumnMap(TempColumn column)
	  throws InvalidMapException
   {
	  TempColumnMap columnMap;

	  if ((elementTypeColumnMaps.get(column) != null) ||
		  (propertyColumnMaps.get(column) != null))
		 throw new InvalidMapException("More than one property mapped to the " + column.name + " column in the " + table.name + " table.");

	  columnMap = new TempColumnMap(column);
	  elementTypeColumnMaps.put(column, columnMap);
	  return columnMap;
   }   

   TempColumnMap addPropertyColumnMap(TempColumn column)
	  throws InvalidMapException
   {
	  TempColumnMap columnMap;

	  if ((elementTypeColumnMaps.get(column) != null) ||
		  (propertyColumnMaps.get(column) != null))
		 throw new InvalidMapException("More than one property mapped to the " + column.name + " column in the " + table.name + " table.");

	  columnMap = new TempColumnMap(column);
	  propertyColumnMaps.put(column, columnMap);
	  return columnMap;
   }   
}