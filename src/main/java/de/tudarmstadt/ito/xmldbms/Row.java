// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Changed Column.number - 1 to Column.rowObjectIndex throughout
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Changed Column.number - 1 to Column.rowObjectIndex throughout
// Changes from version 1.01: None

/**
 * Caches data for a single row.
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class Row
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   Object[] columnValues;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a Row of the specified size */
   Row(int size)
   {
	  columnValues = new Object[size];
   }   

   /** Construct a Row that can hold data for one row in the specified Table. */ 
   Row(Table table)
   {
	  columnValues = new Object[table.columns.length];
   }   

   /** Construct a Row from an array of Objects. */
   Row(Object[] columnValues)
   {
	  this.columnValues = columnValues;
   }   

   // ********************************************************************
   // Methods
   // ********************************************************************

   /**
	* Set a column value.
	*
	* @param column Column for which to set the value.
	* @param value Value to set.
	*/
   void setColumnValue(Column column, Object value)
   {
	  // 5/29/00, Ronald Bourret
	  // Change name to Column.rowObjectIndex and adjust for 0-based.

	  columnValues[column.rowObjectIndex] = value;
   }   

   /**
	* Get a column value.
	*
	* @param column Column for which to get the value.
	* @return Returned value.
	*/
   Object getColumnValue(Column column)
   {
	  // 5/29/00, Ronald Bourret
	  // Change name to Column.rowObjectIndex and adjust for 0-based.

	  return columnValues[column.rowObjectIndex];
   }   

   /**
	* Set multiple column values.
	*
	* @param columns Columns for which to set values.
	* @param values Values to set.
	*/
   void setColumnValues(Column[] columns, Object[] values)
   {
	  // Typically, this is used to set a multi-column key value.
	  for (int i = 0; i < columns.length; i++)
	  {
		 setColumnValue(columns[i], values[i]);
	  }
   }   

   /**
	* Get multiple column values.
	*
	* @param columns Columns for which to get values.
	* @return Returned values.
	*/
   Object[] getColumnValues(Column[] columns)
   {
	  // Typically, this is used to get a multi-column key value.

	  Object[] values = new Object[columns.length];

	  for (int i = 0; i < columns.length; i++)
	  {
		 values[i] = getColumnValue(columns[i]);
	  }

	  return values;
   }   

   /**
	* Whether a column is null.
	*
	* @param column Column to test.
	* @return Whether the column is null.
	*/
   boolean isNull(Column column)
   {
	  // 5/29/00, Ronald Bourret
	  // Change name to Column.rowObjectIndex and adjust for 0-based.

	  if (columnValues[column.rowObjectIndex] == null)
	  {
		 return true;
	  }
	  else
	  {
		 return false;
	  }
   }   

   /**
	* Whether any column in a set of columns is null.
	*
	* @param columns Columns to test.
	* @return Whether any column is null.
	*/
   boolean anyNull(Column[] columns)
   {
	  // Generally, this is used with an assumption that if any columns
	  // in a key are null, the key has not been set.
	  for (int i = 0; i < columns.length; i++)
	  {
		 // 5/29/00, Ronald Bourret
		 // Change name to Column.rowObjectIndex and adjust for 0-based.

		 if (columnValues[columns[i].rowObjectIndex] == null) return true;
	  }
	  return false;
   }   
}