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

package org.xmlmiddleware.conversions.helpers;

import org.xmlmiddleware.conversions.StringFormatter;
import org.xmlmiddleware.conversions.ConversionException;
import org.xmlmiddleware.conversions.ConvertDouble;
import org.xmlmiddleware.conversions.ConvertLong;

import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;

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
    * @exception ConversionException Thrown if the string can't be parsed.
    */
   public Object parse(String s, int jdbcType) throws ConversionException
   {
      Object o;

      try
      {
         o = formatter.parse(s);
      }
      catch (ParseException p)
      {
         throw new ConversionException(p);
      }

      if (o instanceof Double)
      {
         Double d = (Double)o;

         switch(jdbcType)
         {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
               throw new ConversionException("Conversion to binary types not supported.");

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
               throw new ConversionException("Use an implementation of StringFormatter to convert to strings.");

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
               throw new ConversionException("Conversion to date/time types not supported.");

            default:
               throw new ConversionException("Conversion to specified JDBC type not supported.");
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
               throw new ConversionException("Conversion to binary types not supported.");

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
               throw new ConversionException("Use an implementation of StringFormatter to convert to strings.");

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
               throw new ConversionException("Conversion to date/time types not supported.");

            default:
               throw new ConversionException("Conversion to specified JDBC type not supported.");
         }
      }
      else
         throw new ConversionException("Java does not behave as advertised. NumberFormat returned an Object other than a Long or a Double.");
   }

   /**
    * Format an object according to the format used by the
    * underlying NumberFormat object.
    *
    * @param The object to serialize. Must be a Long, Integer,
    *        Short, Byte, Double, or Float.
    * @return The string
    * @exception ConversionException Thrown if the object is not a Long,
    *            Integer, Short, Byte, Double, or Float.
    */
   public String format(Object o) throws ConversionException
   {
      if ((o instanceof Long) || (o instanceof Integer) ||
          (o instanceof Short) ||(o instanceof Byte))
      {
         return formatter.format(((Number)o).longValue());
      }
      else if ((o instanceof Double) || (o instanceof Float))
      {
         return formatter.format(((Number)o).doubleValue());
      }
      else
         throw new ConversionException("Object must be a Long, Integer, Short, Byte, Double, or Float.");
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