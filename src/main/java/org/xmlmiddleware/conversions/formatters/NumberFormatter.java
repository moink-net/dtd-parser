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
// Changes from version 1.0: New in version 2.0

package org.xmlmiddleware.conversions.formatters;

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;

import java.math.*;
import java.sql.Types;
import java.text.*;

/**
 * Wraps a NumberFormat in an StringFormatter interface.
 *
 * <p>This can also be used to wrap subclasses of NumberFormat, such as DecimalFormat.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class NumberFormatter implements StringFormatter
{
   // ********************************************************************
   // Class variables
   // ********************************************************************

   private NumberFormat formatter;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   public NumberFormatter(NumberFormat formatter)
   {
      if (formatter == null)
         throw new IllegalArgumentException("formatter argument must be non-null");
      this.formatter = formatter;
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Parse a string according to the parse format used
    * by the underlying NumberFormat object.
    *
    * @param The string to parse.
    * @param A JDBC Types value indicating the type of object to return.
    * @return A number. This is a Long if possible; otherwise, it is a Double.
    * @exception XMLMiddlewareException Thrown if the string can't be parsed.
    */
   public Object parse(String s, int jdbcType) throws XMLMiddlewareException
   {
      Object o;

      try
      {
         o = formatter.parse(s);
      }
      catch (ParseException p)
      {
         throw new XMLMiddlewareException(p);
      }

      if (o instanceof Double)
      {
         Double d = (Double)o;

         switch(jdbcType)
         {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
               throw new XMLMiddlewareException("Conversion to binary types not supported.");

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
               throw new XMLMiddlewareException("Conversion to string types not supported.");

            case Types.DOUBLE:
            case Types.FLOAT:
               return ConvertDouble.toDoubleObject(d);

            case Types.REAL:
               return ConvertDouble.toFloatObject(d);

            case Types.DECIMAL:
            case Types.NUMERIC:
               return ConvertDouble.toBigDecimal(d);

            case Types.BIGINT:
               return ConvertDouble.toLongObject(d);

            case Types.INTEGER:
               return ConvertDouble.toIntegerObject(d);

            case Types.SMALLINT:
               return ConvertDouble.toShortObject(d);

            case Types.TINYINT:
               return ConvertDouble.toByteObject(d);

            case Types.BIT:
               return ConvertDouble.toBooleanObject(d);

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
               throw new XMLMiddlewareException("Conversion to date/time types not supported.");

            default:
               throw new XMLMiddlewareException("Conversion to specified JDBC type not supported.");
         }
      }
      else if (o instanceof Long)
      {
         Long l = (Long)o;

         switch(jdbcType)
         {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
               throw new XMLMiddlewareException("Conversion to binary types not supported.");

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
               throw new XMLMiddlewareException("Conversion to string types not supported.");

            case Types.DOUBLE:
            case Types.FLOAT:
               return ConvertLong.toDoubleObject(l);

            case Types.REAL:
               return ConvertLong.toFloatObject(l);

            case Types.DECIMAL:
            case Types.NUMERIC:
               return ConvertLong.toBigDecimal(l);

            case Types.BIGINT:
               return ConvertLong.toLongObject(l);

            case Types.INTEGER:
               return ConvertLong.toIntegerObject(l);

            case Types.SMALLINT:
               return ConvertLong.toShortObject(l);

            case Types.TINYINT:
               return ConvertLong.toByteObject(l);

            case Types.BIT:
               return ConvertLong.toBooleanObject(l);

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
               throw new XMLMiddlewareException("Conversion to date/time types not supported.");

            default:
               throw new XMLMiddlewareException("Conversion to specified JDBC type not supported.");
         }
      }
      else
         throw new XMLMiddlewareException("Java does not behave as advertised. NumberFormat returned an Object other than a Long or a Double.");
   }

   /**
    * Format an object according to the format used by the
    * underlying NumberFormat object.
    *
    * @param The object to serialize. Must be a BigDecimal, Long, Integer,
    *        Short, Byte, BigDecimal, Double, or Float.
    * @return The string
    * @exception XMLMiddlewareException Thrown if the object is not a Long,
    *            Integer, Short, Byte, BigDecimal, Double, or Float.
    */
   public String format(Object o) throws XMLMiddlewareException
   {
      if ((o instanceof Long) || (o instanceof Integer) ||
          (o instanceof Short) ||(o instanceof Byte))
      {
         return formatter.format(((Number)o).longValue());
      }
      else if (o instanceof BigDecimal)
      {
         try
         {
            return formatter.format(ConvertBigDecimal.toLong((BigDecimal)o));
         }
         catch (XMLMiddlewareException e)
         {
            return formatter.format(ConvertBigDecimal.toDouble((BigDecimal)o));
         }
      }
      else if ((o instanceof Double) || (o instanceof Float))
      {
         return formatter.format(((Number)o).doubleValue());
      }
      else
         throw new XMLMiddlewareException("Object must be a BigDecimal, Long, Integer, Short, Byte, Double, or Float.");
   }

   /**
    * Whether the class can convert to/from a certain type of object.
    *
    * <p>This method returns true for Types.DOUBLE, FLOAT, REAL,
    * DECIMAL, NUMERIC, BIGINT, INTEGER, SMALLINT, TINYINT, and BIT.
    * It returns false for all other types.</p>
    *
    * @param type The JDBC Types value corresponding to the object type.
    * @return Whether the type is supported
    */
   public boolean canConvert(int type)
   {
      return ((type == Types.DOUBLE) ||
              (type == Types.FLOAT) ||
              (type == Types.REAL) ||
              (type == Types.DECIMAL) ||
              (type == Types.NUMERIC) ||
              (type == Types.BIGINT) ||
              (type == Types.INTEGER) ||
              (type == Types.SMALLINT) ||
              (type == Types.TINYINT) ||
              (type == Types.BIT));
   }

   /**
    * Get the underlying NumberFormat object.
    *
    * @return The NumberFormat object.
    */
   public NumberFormat getNumberFormat()
   {
      return formatter;
   }
}
