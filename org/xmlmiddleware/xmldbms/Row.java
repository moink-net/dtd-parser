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
import org.xmlmiddleware.db.*;

import java.io.*;
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
    * Construct a new Row object.
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
      {
         m_columnValues.put(column, new Null());
      }
      else
      {
         m_columnValues.put(column, value);
      }
   }

   /**
    * Get a column value.
    *
    * @param column Column for which to get the value.
    * @return Returned value. Returns null if the column is not set or
    *     the value is null.
    */
   public Object getColumnValue(Column column)
   {
      Object val = m_columnValues.get(column);

      if(val == null || val instanceof Null)
      {
         return null;
      }
      else
      {
         return val;
      }
   }

   /**
    * Remove a column value.
    *
    * @param column Column to remove value for.
    */
   public void removeColumnValue(Column column)
   {
      m_columnValues.remove(column);
   }

   /**
    * Remove all column values.
    */
   public void removeAllColumnValues()
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
   {
      // Typically, this is used to set a multi-column key value.

      for(int i = 0; i < columns.length; i++)
      {
         setColumnValue(columns[i], values[i]);
      }
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
         else if (o instanceof InputStream)
         {
            // If the returned object is an InputStream, then the underlying column
            // is probably a BLOB (LONGVARCHAR or LONGVARBINARY).

            InputStream in = (InputStream)o;
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Read the byte stream into a buffer.

            int len;
            byte[] buf = new byte[4096];
            try
            {
               while ((len = in.read(buf)) != -1)
               {
                  out.write(buf);
               }
            }
            catch (IOException e)
            {
               throw new SQLException("[XML-DBMS] IOException. " + e.getMessage());
            }

            // Convert the byte stream to a String (character data) or a ByteArray
            // (binary data). If the data type is none of these, throw an exception,
            // since JDBC doesn't define conversions between binary data and other
            // kinds of data.

            int type = rsColumns[i].getType();
            if (JDBCTypes.typeIsChar(type))
            {
               o = out.toString();
            }
            else if (JDBCTypes.typeIsBinary(type))
            {
               o = new ByteArray(out.toByteArray());
            }
            else
               throw new SQLException("[XML-DBMS] The driver returned data for the " + rsColumns[i].getName() + " column as an InputStream. JDBC does not support conversions from stream (byte) data to " + JDBCTypes.getName(type) + ".");
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
      {
         values[i] = getColumnValue(columns[i]);
      }

      return values;
   }

   /**
    * Get a Vector of Column objects for the values in this row that apply
    * to a given table.
    *
    * <p>This method only returns Columns for values (including null values)
    * that have explicitly been set.</p>
    *
    * @param table The table.
    * @return A Vector of Column objects. May be empty.
    */
   public Vector getColumnVectorFor(Table table)
   {
      Vector cols = new Vector();
      Enumeration e = table.getColumns();

      while(e.hasMoreElements())
      {
         Column col = (Column)e.nextElement();
         if(isColumnSet(col))
         {
            cols.addElement(col);
         }
      }

      return cols;
   }

   /**
    * Get an array of Column objects for the values in this row that apply
    * to a given table.
    *
    * <p>This method only returns Columns for values (including null values)
    * that have explicitly been set.</p>
    *
    * @param table The table.
    * @return An array of Column objects. May be zero length.
    */
   public Column[] getColumnArrayFor(Table table)
   {
      Vector   cols = getColumnVectorFor(table);
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
   boolean isColumnSet(Column column)
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
   boolean areColumnsSet(Column[] columns)
   {
      // Generally, this is used with an assumption that if any columns
      // in a key are emtpy, the key has not been set.

      for(int i = 0; i < columns.length; i++)
      {
         if(!isColumnSet(columns[i]))
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
