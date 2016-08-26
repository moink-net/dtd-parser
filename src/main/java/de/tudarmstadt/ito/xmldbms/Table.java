// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Added rsColumnNumbers array and modified constructor.
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Added rsColumnNumbers array and modified constructor.
// Changes from version 1.01: None

// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Added rsColumnNumbers array and modified constructor.
// Changes from version 1.01: None

/**
 * Describes a table; <A HREF="../readme.html#NotForUse">not for general
 * use</A>.
 *
 * <P>Table contains information about a table. Tables are included in
 * TableMaps and in the array of tables in Map.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Table
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   /** An array of Columns describing the columns in the table. */
   public Column[] columns = null;

   /**
	* The numbers of the columns in the result set from which to
	* retrieve column values.
	*
	* <p>The result set column numbers are stored in this array in
	* ascending order. This is necessary because some databases require
	* columns in result sets to be accessed in ascending order. Although
	* JDBC does not appear to have this requirement, it is likely
	* that some JDBC drivers have it anyway.</p>
	*/

   // 5/29/00, Ronald Bourret
   // Added array. This is needed when the result set has more columns
   // than the table does -- that is, than are mapped. This can occur
   // only when DBMSToDOM.retrieveDocument(ResultSet) is called.

   public int[] rsColumnNumbers = null;

   /** The table name. */
   public String name = null;

   /** The table number. Table numbers are 0-based. */
   public int number = -1;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a Table. */
   public Table()
   {
   }      

   /** Construct a Table with the specified Columns, name, and number. */
   public Table(Column[] columns, String name, int number)
   {
	  this.columns = columns;
	  this.name = name;
	  this.number = number;

	  // 5/29/00, Ronald Bourret
	  // Initialize rsColumnNumbers array. Normally, the number and order
	  // of the columns in the result set corresponds to the number and
	  // order of the columns in the columns array. This is because we
	  // normally build the SELECT statement from the columns array.
	  //
	  // The only exception is when the calling application passes a
	  // result set to DBMSToDOM.retrieveDocument. In this case, the number
	  // and order of the columns in the result set can be different from
	  // the number and order of the columns in the columns array. (The
	  // Columns in the columns array correspond in order and number to the
	  // columsn that were mapped in the mapping document.) In this case,
	  // the values in the rsColumnNumbers array are overridden in
	  // Map.addResultSetMetadata.

	  rsColumnNumbers = new int[columns.length];

	  for (int i = 0; i < rsColumnNumbers.length; i++)
	  {
		 rsColumnNumbers[i] = i + 1;
	  }
   }      

   // ********************************************************************
   // Methods
   // ********************************************************************

   /**
	* Get an array of Column objects for the named columns. Note that this
	* method is not efficient, performing a linear search for each column.
	*
	* @param columnNames Names of the columns to find. If this is null,
	*  getColumns returns a null.
	* @return An array of Column objects for the specified columns.
	* @exception InvalidMapException Thrown if any columns are not found.
	*/
   public Column[] getColumns(String[] columnNames) throws InvalidMapException
   {
	  Column[] columnArray;

	  if (columnNames == null) return null;

	  columnArray = new Column[columnNames.length];
	  for (int i = 0; i < columnNames.length; i++)
	  {
		 columnArray[i] = getColumn(columnNames[i]);
	  }

	  return columnArray;
   }      

   /**
	* Get a Column object for the named column. Note that this method is not
	* efficient, performing a linear search for the column.
	*
	* @param columnName Name of the column to find. If this is null, getColumn
	*  returns a null.
	* @return Column object for the specified column.
	* @exception InvalidMapException Thrown if the column is not found.
	*/
   public Column getColumn(String columnName) throws InvalidMapException
   {
	  Column column = null;
	  if (columnName == null) return null;

	  for (int i = 0; i < columns.length; i++)
	  {
		 if (columns[i].name.equals(columnName))
		 {
			column = columns[i];
			break;
		 }
	  }

	  if (column != null) return column;
	  throw new InvalidMapException("Column " + columnName + " not found in table " + name);
   }               
}