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
// Changes from version 1.0: None
// Changes from version 1.01: None
// Changes from version 1.1:
// * All conversion code moved to conversions package
// * Added new methods and arguments

package org.xmlmiddleware.xmldbms.datahandlers;

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.db.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;

/**
 * Sets parameters in an SQL statement.
 *
 * <p>The methods in this class assume that the arrays of parameter values and
 * Columns are in the same order as the parameters in the SQL statement.</p>
 *
 * <p>Methods that require parameter values to be of the default object type
 * for the column type use the following default object types:</p>
 *
 * <pre>
 * CHAR, VARCHAR, LONGVARCHAR:        String<br />
 * DATE, TIME, TIMESTAMP:             java.sql.Date<br />
 * BIGINT:                            Long<br />
 * INTEGER:                           Integer<br />
 * SMALLINT:                          Short<br />
 * TINYINT:                           Byte<br />
 * DECIMAL, NUMERIC:                  BigDecimal<br />
 * DOUBLE, FLOAT:                     Double<br />
 * REAL:                              Float<br />
 * BINARY, VARBINARY, LONGVARBINARY:  org.xmlmiddleware.conversions.ByteArray<br />
 * BIT:                               Boolean<br />
 * </pre>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class Parameters
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Set parameters from an array of values.
    *
    * <p>For best performance, parameter values should be of the default object type
    * for the parameter type.</p>
    *
    * @param p Prepared SQL statement
    * @param offset Offset into the list of parameters in the prepared statement.
    *   To start with the first parameter, use an offset of 0.
    * @param columns An array of Column objects corresponding to the parameters.
    * @param values Parameter values.
    * @exception SQLException A database error occurred while setting
    *  the parameter.
   */

   public static void setParameters(PreparedStatement p, int offset, Column[] columns, Object[] values)
      throws SQLException
   {
     for (int i = 0; i < values.length; i++)
     {
       setParameter(p, i + offset + 1, columns[i], values[i]);
     }
   }

   /**
    * Set a single parameter.
    *
    * <p>For best performance, parameter values should be of the default object type
    * for the parameter type.</p>
    *
    * @param p Prepared SQL statement
    * @param number The parameter number (1-based)
    * @param column The column corresponding to the parameter
    * @param value The parameter value
    * @exception SQLException A database error occurred while setting
    *  the parameter.
   */
   public static void setParameter(PreparedStatement p, int number, Column column, Object value)
     throws SQLException
   {
      byte[]      b;
      InputStream stream;
      int         type;

      type = column.getType();

      if (value == null)
      {
        p.setNull(number, type);
        return;
      }

      try
      {
         switch (type)
         {
            case Types.BIT:
               p.setBoolean(number, ((Boolean)value).booleanValue());
               break;

            case Types.TINYINT:
               p.setByte(number, ((Byte)value).byteValue());
               break;

            case Types.SMALLINT:
               p.setShort(number, ((Short)value).shortValue());
               break;

            case Types.INTEGER:
               p.setInt(number, ((Integer)value).intValue());
               break;

            case Types.BIGINT:
               p.setLong(number, ((Long)value).longValue());
               break;

            case Types.REAL:
               p.setFloat(number, ((Float)value).floatValue());
               break;

            case Types.FLOAT:
            case Types.DOUBLE:
               p.setDouble(number, ((Double)value).doubleValue());
               break;

            case Types.DECIMAL:
            case Types.NUMERIC:
               p.setBigDecimal(number, (BigDecimal)value);
               break;

            case Types.CHAR:
            case Types.VARCHAR:
               p.setString(number, (String)value);
               break;

            case Types.LONGVARCHAR:
               // First try to pass the value as Unicode. If this fails,
               // assume it is because setUnicodeStream() is not supported.
               // Next, try calling setAsciiStream(). If this also fails,
               // then return the exception.
               try
               {
                  b = ((String)value).getBytes("UTF-16");
                  stream = new ByteArrayInputStream(b);
                  p.setUnicodeStream(number, stream, b.length);
               }
               catch (Exception e)
               {
                  try
                  {
                     b = ((String)value).getBytes("US-ASCII");
                     stream = new ByteArrayInputStream(b);
                     p.setAsciiStream(number, stream, b.length);
                  }
                  catch (UnsupportedEncodingException u)
                  {
                     throw new SQLException("[XML-DBMS] " + u.getMessage());
                  }
               }
               break;

            case Types.BINARY:
            case Types.VARBINARY:
               b = ((ByteArray)value).getBytes();
               p.setBytes(number, b);
               break;

            case Types.LONGVARBINARY:
               b = ((ByteArray)value).getBytes();
               stream = new ByteArrayInputStream(b);
               p.setBinaryStream(number, stream, b.length);
               break;

            case Types.DATE:
               p.setDate(number, (java.sql.Date)value);
               break;

            case Types.TIME:
               p.setTime(number, (Time)value);
               break;

            case Types.TIMESTAMP:
               p.setTimestamp(number, (Timestamp)value);
               break;

            default:
               String name = JDBCTypes.getName(type);
               if (name == null)
               {
                  name = Integer.toString(type);
               }
               throw new SQLException("Unsupported JDBC Type: " + name);
         
         }
      }
      catch (ClassCastException c)
      {
         // If the value is not the expected type, then try to convert it to the
         // correct type. Note that convertAndSetParameter calls back into this
         // method. However, an infinite loop cannot occur because either (a)
         // convertAndSetParameter fails the conversion and an exception is thrown,
         // or (b) convertAndSetParameter succeeds in the conversion and this clause
         // is not reached again.

         convertAndSetParameter(p, number, column, value);
      }
   }

   /**
    * Set parameters from an array of values, converting the object type first
    * if necessary.
    *
    * <p>This method checks the object type of the parameter values and performs
    * any necessary conversions before setting the parameter value. If the parameter
    * values are all known to be of the default type, use setParameters instead,
    * since it is faster.</p>
    *
    * @param p Prepared SQL statement
    * @param offset Offset into the list of parameters in the prepared statement.
    *   To start with the first parameter, use an offset of 0.
    * @param columns An array of Column objects corresponding to the parameters.
    * @param values Parameter values.
    * @exception SQLException A database error occurred while setting
    *  the parameter.
   */
   public static void convertAndSetParameters(PreparedStatement p, int offset, Column[] columns, Object[] values)
      throws SQLException
   {
     for (int i = 0; i < values.length; i++)
     {
       convertAndSetParameter(p, i + offset + 1, columns[i], values[i]);
     }
   }

   /**
    * Set a single parameter, converting the object type first if necessary.
    *
    * <p>This method checks the object type of the parameter value and performs
    * any necessary conversion before setting the parameter value. If the parameter
    * value is known to be of the default type, use setParameters instead,
    * since it is faster.</p>
    *
    * @param p Prepared SQL statement
    * @param number The parameter number (1-based)
    * @param column A Column object describing the parameter.
    * @param value The parameter value.
    * @exception SQLException A database error occurred while setting
    *  the parameter.
    */
   public static void convertAndSetParameter(PreparedStatement p, int number, Column column, Object value)
     throws SQLException
   {
      StringFormatter formatter;
      int             type;
      Object          o;

      type = column.getType();

      if (value == null)
      {
         p.setNull(number, type);
         return;
      }
      try
      {
         formatter = column.getFormatter();
         if (value instanceof String)
         {
            o = formatter.parse((String)value, type);
         }
         else
         {
            o = ConvertObject.convertObject(value, type, formatter);
         }
      }
      catch (ConversionException c)
      {
         throw new SQLException("[XML-DBMS] Conversion exception: " + c.getMessage());
      }
      setParameter(p, number, column, o);
   }   
}
