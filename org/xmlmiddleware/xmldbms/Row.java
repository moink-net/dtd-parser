
package org.xmlmiddleware.xmldbms;

import java.lang.*;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * Caches data for a single row.
 */

class Row
{
    class Empty
    {
        Empty() 
            {  }
    };

    // ********************************************************************
    // Variables
    // ********************************************************************

    Object[] columnValues;

    // ********************************************************************
    // Constructors
    // ********************************************************************

    /** 
     * Construct a Row 
     *
     * @param size Width of row.
     */
    Row(int size)
    {
        init(size);
    }


    /** 
     * Construct a Row that can hold data for one row in the specified Table. 
     *
     * @param table The table.
     */ 
    Row(Table table)
    {
        init(table.getNumberofColumns());
    }


    /** 
     * Construct a Row from an array of Objects. 
     *
     * @param columnValues The objects.
     */
    Row(Object[] vals)
    {
	    columnValues = vals;
    }

    
    /**
     * Helper construction method.
     *
     * @param size Width of row
     */
    void init(int size)
    {
        columnValues = new Object[size];
        while(size-- > 0)
            columnValues[size] = new Empty();
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
	    columnValues[column.getRowIndex()] = value;
    }   

    /**
	 * Get a column value.
	 *
	 * @param column Column for which to get the value.
	 * @return Returned value.
	 */
    Object getColumnValue(Column column)
    {
	    return columnValues[column.getRowIndex()];
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
        for(int i = 0; i < columns.length; i++)
		    setColumnValue(columns[i], values[i]);
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

	    for(int i = 0; i < columns.length; i++)
		    values[i] = getColumnValue(columns[i]);

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
        return columnValues[column.getRowIndex()] == null;
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
	    for(int i = 0; i < columns.length; i++)
	    {
		    if(columnValues[columns[i].getRowIndex()] == null) 
                return true;
	    }
	  
        return false;
    }   


    /**
     * Checks if a column is empty (ie: hasn't been set)
     *
     * @param column Column to test.
     * @return Whether the column has been set or not.
     */
    boolean isEmpty(Column column)
    {
        return columnValues[column.getRowIndex()] instanceof Empty;
    }

    /**
     * Set a column to empty
     *
     * @param column Column to set.
     */
    void setEmpty(Column column)
    {
        columnValues[column.getRowIndex()] = new Empty();
    }

    /**
     * Checks whether any of the columns are not set
     *
	 * @param columns Columns to test.
	 * @return Whether any column are empty.
     */
    boolean anyEmpty(Column[] columns)
    {
	    for(int i = 0; i < columns.length; i++)
	    {
		    if(columnValues[columns[i].getRowIndex()] instanceof Empty) 
                return true;
	    }
	  
        return false;
    }
}