
package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.util.*;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * Caches data for a single row.
 */

class Row
{
    class Null
    {

    };

    // ********************************************************************
    // Variables
    // ********************************************************************
    private Hashtable m_columnValues;

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
    {
        // Hashtable won't accept null objects, so we create
        // a placeholder for NULL

        if(value == null)
            m_columnValues.put(column, new Null());
        else
	        m_columnValues.put(column, value);
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
	 * Set multiple column values.
	 *
	 * @param columns Columns for which to set values.
	 * @param values Values to set.
	 */
    public void setColumnValues(Column[] columns, Object[] values)
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
    public Object[] getColumnValues(Column[] columns)
    {
	    // Typically, this is used to get a multi-column key value.
	    Object[] values = new Object[columns.length];

	    for(int i = 0; i < columns.length; i++)
		    values[i] = getColumnValue(columns[i]);

	    return values;
    }

    

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

    public Column[] getColumnsFor(Table table)
    {
        Vector cols = getColumnVectorFor(table);
        Column[] array = new Column[cols.size()];
        cols.copyInto(array);
        return array;
    }

    /**
	 * Whether a column is null.
	 *
	 * @param column Column to test.
	 * @return Whether the column is null.
	 */
    boolean haveColumn(Column column)
    {
        return m_columnValues.get(column) != null;
    }   


    /**
	 * Whether any column in a set of columns is null.
	 *
	 * @param columns Columns to test.
	 * @return Whether any column is null.
	 */
    boolean haveColumns(Column[] columns)
    {
	    // Generally, this is used with an assumption that if any columns
	    // in a key are null, the key has not been set.
	    for(int i = 0; i < columns.length; i++)
        {
            if(!haveColumn(columns[i]))
                return false;
        }

        return true;
    }  
    
}