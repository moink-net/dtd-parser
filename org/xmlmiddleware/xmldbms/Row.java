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
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.1: Rewritten in version 2.0

package org.xmlmiddleware.xmldbms;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.conversions.*;

import java.sql.*;
import java.util.*;

/**
 * Caches data for a single row. For internal use.
 */

public class Row
{
    // ********************************************************************
    // Variables
    // ********************************************************************
    private Hashtable m_columnValues;

    // ********************************************************************
    // Constants
    // ********************************************************************

    private static final String EMPTYSTRING = "";

    // ********************************************************************
    // Constructors
    // ********************************************************************

    /** 
     * Construct a Row 
     */
    public Row()
    {
        m_columnValues = new Hashtable();
    }


    // ********************************************************************
    // Methods
    // ********************************************************************

    /**
	 * Set a column value.
	 *
	 * @param column Column for which to set the value.
	 * @param value Value to set (null for a NULL value).
	 */
    public void setColumnValue(Column column, Object value)
//        throws ConversionException
    {
        // Hashtable won't accept null objects, so we create
        // a placeholder for NULL

        if(value == null)
            m_columnValues.put(column, new Null());
        else
	        m_columnValues.put(column, value);
        //  m_columnValues.put(column, ConvertObject.convertObject(value, column.getType()));
        }

    
    /**
	 * Get a column value.
	 *
	 * @param column Column for which to get the value.
	 * @return Returned value. Will return null if column not set or
     *         value is null. @see haveColumn.
	 */
    public Object getColumnValue(Column column)
    {
        Object val = m_columnValues.get(column);

        if(val == null || val instanceof Null)
            return null;
        else
            return val;
    }


    /**
     * Clear a column of a value.
     *
     * @param column Column to clear.
     */
    public void clearColumnValue(Column column)
    {
        m_columnValues.remove(column);
    }


    /**
     * Clear all column values.
     */
    public void clear()
    {
        m_columnValues.clear();
    }


    /**
	 * Set multiple column values.
	 *
	 * @param columns Columns for which to set values.
	 * @param values Values to set.
	 */
    public void setColumnValues(Column[] columns, Object[] values)
//        throws ConversionException
    {
	    // Typically, this is used to set a multi-column key value.
        for(int i = 0; i < columns.length; i++)
		    setColumnValue(columns[i], values[i]);
    }   

    /**
	 * Set column values from a result set
	 *
	 * @param rs The result set.
	 * @param table The Table object describing the rows in the result set
	 */
    public void setColumnValues(ResultSet rs, Table table, boolean emptyStringIsNull)
        throws SQLException
    {
      Column[] rsColumns;
      Object   o;

      // Loop through the columns in the result set and set the corresponding
      // values in the Row. We use Table.getResultSetColumns() since this:
      // (a) Retrieves only the necessary columns (the result set might have more), and
      // (b) Retrieves the columns in ascending order, which is needed for interoperability.

      rsColumns = table.getResultSetColumns();

      for (int i = 0; i < rsColumns.length; i++)
      {
         // Get the next column value.

         o = rs.getObject(rsColumns[i].getResultSetIndex());

         // If the column value is NULL, set it to an EMPTYSTRING or null.

         if (rs.wasNull())
         {
            o = (emptyStringIsNull) ? EMPTYSTRING : null;
         }

         // Set the column value.

         setColumnValue(rsColumns[i], o);
      }
    }   


    /**
	 * Get multiple column values.
	 *
	 * @param columns Columns for which to get values.
	 * @return Returned values.
	 */
    public Object[] getColumnValues(Column[] columns)
    {
	    // Typically, this is used to get a multi-column key value.
	    Object[] values = new Object[columns.length];

	    for(int i = 0; i < columns.length; i++)
		    values[i] = getColumnValue(columns[i]);

	    return values;
    }

    
    /**
     * Returns the columns in this row that apply to a given
     * table. Only returns columns that have values (including
     * null).
     *
     * @param table The table.
     */
    public Vector getColumnVectorFor(Table table)
    {
        Vector cols = new Vector();
        Enumeration e = table.getColumns();
        
        while(e.hasMoreElements())
        {
            Column col = (Column)e.nextElement();
            if(haveColumn(col))
                cols.addElement(col);
        }

        return cols;
    }

    /**
     * Returns the columns in this row that apply to a given
     * table. Only returns columns that have values (including
     * null).
     *
     * @param table The table.
     */
    public Column[] getColumnsFor(Table table)
    {
        Vector cols = getColumnVectorFor(table);
        Column[] array = new Column[cols.size()];
        cols.copyInto(array);
        return array;
    }


    /**
	 * Whether a column has a value (including null).
	 *
	 * @param column Column to test.
	 * @return Whether the column has a value.
	 */
    boolean haveColumn(Column column)
    {
        return m_columnValues.get(column) != null;
    }   


    /**
	 * Whether all of a set of columns have values
     * (including null).
	 *
	 * @param columns Columns to test.
	 * @return Whether all column have values.
	 */
    boolean haveColumns(Column[] columns)
    {
	    // Generally, this is used with an assumption that if any columns
	    // in a key are emtpy, the key has not been set.
	    for(int i = 0; i < columns.length; i++)
        {
            if(!haveColumn(columns[i]))
                return false;
        }

        return true;
    }  
 
    // ********************************************************************
    // Variables
    // ********************************************************************

    /**
     * Denotes a null value in the row.
     */
    class Null
    {

    };

}
