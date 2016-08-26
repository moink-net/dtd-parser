// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import java.util.Hashtable;

/**
 * Temporary version of de.tudarmstadt.ito.xmldbms.Table; <B>used
 * only by map factories</B>.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class TempTable
{
   //***********************************************************************
   // Variables
   //***********************************************************************

   Hashtable columns = new Hashtable();
   Hashtable mappedColumns = new Hashtable();
   String    name;

   //***********************************************************************
   // Constructors
   //***********************************************************************

   TempTable()
   {
   }   

   TempTable(String name)
   {
	  this.name = name;
   }   

   //***********************************************************************
   // Methods
   //***********************************************************************

   TempColumn getColumn(String columnName)
   {
	  TempColumn column;

	  if ((column = (TempColumn)columns.get(columnName)) == null)
	  {
		 column = new TempColumn(columnName);
		 columns.put(columnName, column);
	  }
	  return column;
   }   

   TempColumn mapPropertyColumn(String columnName)
	  throws InvalidMapException
   {
	  // Check if a property has already been mapped to the column. This
	  // is different from whether the column exists: the column could have
	  // been created as a candidate key column, foreign key column, or order
	  // column. If nothing has been mapped to the column, store a dummy
	  // Object using the column name as a key to mark it as unavailable.

	  if (mappedColumns.get(columnName) != null)
		 throw new InvalidMapException("More than one property mapped to the column " + columnName + " in the table " + name);
	  mappedColumns.put(columnName, new Object());
	  return getColumn(columnName);
   }   
}