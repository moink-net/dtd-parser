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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.conversions;

import org.xmlmiddleware.conversions.formatters.*;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Converts from Objects to other data types.
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ConvertObject
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   public static ByteArray toByteArray(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof ByteArray)
         return (ByteArray)o;
      else if (o instanceof String)
         return (ByteArray)formatter.parse((String)o, Types.BINARY);
      else
         throw new ConversionException("Conversion to ByteArray not supported: " + o.getClass().getName());
   }

   public static double toDouble(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toDouble((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toDouble((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toDouble((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toDouble((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toDouble((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toDouble((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toDouble((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toDouble((Boolean)o);
      else if (o instanceof String)
         return ((Double)formatter.parse((String)o, Types.DOUBLE)).doubleValue();
      else
         throw new ConversionException("Conversion to double not supported: " + o.getClass().getName());
   }

   public static Double toDoubleObject(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toDoubleObject((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toDoubleObject((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toDoubleObject((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toDoubleObject((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toDoubleObject((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toDoubleObject((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toDoubleObject((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toDoubleObject((Boolean)o);
      else if (o instanceof String)
         return (Double)formatter.parse((String)o, Types.DOUBLE);
      else
         throw new ConversionException("Conversion to Double not supported: " + o.getClass().getName());
   }

   public static float toFloat(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toFloat((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toFloat((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toFloat((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toFloat((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toFloat((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toFloat((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toFloat((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toFloat((Boolean)o);
      else if (o instanceof String)
         return ((Float)formatter.parse((String)o, Types.REAL)).floatValue();
      else
         throw new ConversionException("Conversion to float not supported: " + o.getClass().getName());
   }

   public static Float toFloatObject(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toFloatObject((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toFloatObject((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toFloatObject((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toFloatObject((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toFloatObject((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toFloatObject((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toFloatObject((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toFloatObject((Boolean)o);
      else if (o instanceof String)
         return (Float)formatter.parse((String)o, Types.REAL);
      else
         throw new ConversionException("Conversion to Float not supported: " + o.getClass().getName());
   }

   public static BigDecimal toBigDecimal(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toBigDecimal((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toBigDecimal((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toBigDecimal((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toBigDecimal((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toBigDecimal((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toBigDecimal((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toBigDecimal((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toBigDecimal((Boolean)o);
      else if (o instanceof String)
         return (BigDecimal)formatter.parse((String)o, Types.NUMERIC);
      else
         throw new ConversionException("Conversion to BigDecimal not supported: " + o.getClass().getName());
   }

   public static long toLong(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toLong((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toLong((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toLong((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toLong((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toLong((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toLong((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toLong((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toLong((Boolean)o);
      else if (o instanceof String)
         return ((Long)formatter.parse((String)o, Types.BIGINT)).longValue();
      else
         throw new ConversionException("Conversion to long not supported: " + o.getClass().getName());
   }

   public static Long toLongObject(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toLongObject((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toLongObject((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toLongObject((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toLongObject((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toLongObject((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toLongObject((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toLongObject((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toLongObject((Boolean)o);
      else if (o instanceof String)
         return (Long)formatter.parse((String)o, Types.BIGINT);
      else
         throw new ConversionException("Conversion to Long not supported: " + o.getClass().getName());
   }

   public static int toInteger(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toInteger((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toInteger((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toInteger((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toInteger((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toInteger((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toInteger((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toInteger((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toInteger((Boolean)o);
      else if (o instanceof String)
         return ((Integer)formatter.parse((String)o, Types.INTEGER)).intValue();
      else
         throw new ConversionException("Conversion to int not supported: " + o.getClass().getName());
   }

   public static Integer toIntegerObject(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toIntegerObject((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toIntegerObject((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toIntegerObject((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toIntegerObject((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toIntegerObject((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toIntegerObject((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toIntegerObject((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toIntegerObject((Boolean)o);
      else if (o instanceof String)
         return (Integer)formatter.parse((String)o, Types.INTEGER);
      else
         throw new ConversionException("Conversion to Integer not supported: " + o.getClass().getName());
   }

   public static short toShort(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toShort((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toShort((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toShort((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toShort((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toShort((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toShort((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toShort((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toShort((Boolean)o);
      else if (o instanceof String)
         return ((Short)formatter.parse((String)o, Types.SMALLINT)).shortValue();
      else
         throw new ConversionException("Conversion to short not supported: " + o.getClass().getName());
   }

   public static Short toShortObject(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toShortObject((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toShortObject((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toShortObject((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toShortObject((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toShortObject((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toShortObject((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toShortObject((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toShortObject((Boolean)o);
      else if (o instanceof String)
         return (Short)formatter.parse((String)o, Types.SMALLINT);
      else
         throw new ConversionException("Conversion to Short not supported: " + o.getClass().getName());
   }

   public static byte toByte(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toByte((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toByte((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toByte((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toByte((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toByte((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toByte((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toByte((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toByte((Boolean)o);
      else if (o instanceof String)
         return ((Byte)formatter.parse((String)o, Types.TINYINT)).byteValue();
      else
         throw new ConversionException("Conversion to byte not supported: " + o.getClass().getName());
   }

   public static Byte toByteObject(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toByteObject((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toByteObject((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toByteObject((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toByteObject((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toByteObject((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toByteObject((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toByteObject((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toByteObject((Boolean)o);
      else if (o instanceof String)
         return (Byte)formatter.parse((String)o, Types.TINYINT);
      else
         throw new ConversionException("Conversion to Byte not supported: " + o.getClass().getName());
   }

   public static boolean toBoolean(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toBoolean((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toBoolean((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toBoolean((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toBoolean((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toBoolean((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toBoolean((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toBoolean((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toBoolean((Boolean)o);
      else if (o instanceof String)
         return ((Boolean)formatter.parse((String)o, Types.BIT)).booleanValue();
      else
         throw new ConversionException("Conversion to boolean not supported: " + o.getClass().getName());
   }

   public static Boolean toBooleanObject(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Double)
         return ConvertDouble.toBooleanObject((Double)o);
      else if (o instanceof Float)
         return ConvertFloat.toBooleanObject((Float)o);
      else if (o instanceof BigDecimal)
         return ConvertBigDecimal.toBooleanObject((BigDecimal)o);
      else if (o instanceof Long)
         return ConvertLong.toBooleanObject((Long)o);
      else if (o instanceof Integer)
         return ConvertInteger.toBooleanObject((Integer)o);
      else if (o instanceof Short)
         return ConvertShort.toBooleanObject((Short)o);
      else if (o instanceof Byte)
         return ConvertByte.toBooleanObject((Byte)o);
      else if (o instanceof Boolean)
         return ConvertBoolean.toBooleanObject((Boolean)o);
      else if (o instanceof String)
         return (Boolean)formatter.parse((String)o, Types.BIT);
      else
         throw new ConversionException("Conversion to Boolean not supported: " + o.getClass().getName());
   }

   public static Date toDate(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Timestamp)
         return ConvertTimestamp.toDate((Timestamp)o);
      else if (o instanceof Date)
         return ConvertDate.toDate((Date)o);
      else if (o instanceof String)
         return (Date)formatter.parse((String)o, Types.DATE);
      else
         throw new ConversionException("Conversion to Date not supported: " + o.getClass().getName());
   }

   public static Time toTime(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Timestamp)
         return ConvertTimestamp.toTime((Timestamp)o);
      else if (o instanceof Time)
         return ConvertTime.toTime((Time)o);
      else if (o instanceof String)
         return (Time)formatter.parse((String)o, Types.TIME);
      else
         throw new ConversionException("Conversion to Time not supported: " + o.getClass().getName());
   }

   public static Timestamp toTimestamp(Object o, StringFormatter formatter)
      throws ConversionException
   {
      if (o instanceof Date)
         return ConvertDate.toTimestamp((Date)o);
      else if (o instanceof Timestamp)
         return ConvertTimestamp.toTimestamp((Timestamp)o);
      else if (o instanceof String)
         return (Timestamp)formatter.parse((String)o, Types.TIMESTAMP);
      else
         throw new ConversionException("Conversion to Timestamp not supported: " + o.getClass().getName());
   }

   /**
    * Converts an Object to the specified type.
    *
    * @param o The object to convert.
    * @param destType A JDBC Types value. The Object is converted to the default
    *    object type for the specified type.
    * @param formatter An object that implements StringFormatter. If o is a String or
    *    destType is CHAR, VARCHAR, or LONGVARCHAR, this must be non-null.
    * @return The converted object
    */
   public static Object convertObject(Object o, int destType, StringFormatter formatter)
      throws ConversionException
   {
      switch(destType)
      {
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
            return toByteArray(o, formatter);

         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            return formatter.format(o);

         case Types.DOUBLE:
         case Types.FLOAT:
            return toDoubleObject(o, formatter);

         case Types.REAL:
            return toFloatObject(o, formatter);

         case Types.DECIMAL:
         case Types.NUMERIC:
            return toBigDecimal(o, formatter);

         case Types.BIGINT:
            return toLongObject(o, formatter);

         case Types.INTEGER:
            return toIntegerObject(o, formatter);

         case Types.SMALLINT:
            return toShortObject(o, formatter);

         case Types.TINYINT:
            return toByteObject(o, formatter);

         case Types.BIT:
            return toBooleanObject(o, formatter);

         case Types.DATE:
            return toDate(o, formatter);

         case Types.TIME:
            return toTime(o, formatter);

         case Types.TIMESTAMP:
            return toTimestamp(o, formatter);

         default:
            throw new ConversionException("Conversion to specified JDBC type not supported.");
      }
   }
}
