// This software is in the public static domain.
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

import java.math.BigDecimal;

/**
 * Converts from longs to other data types.
 *
 * <p>The following rules from the ODBC specification are followed; the
 * JDBC specification does not address these in spite of allowing the
 * conversions they apply to:</p>
 *
 * <ul>
 * <li>Methods throw an exception when a narrowing conversion exceeds
 * the range of the target type.</li>
 * <li>When converting from integer to floating point, loss of precision
 * is allowed.</li>
 * <li>When converting to boolean, 1 is true, 0 is false, and all other
 * values throw an exception.</li>
 * </ul>
 *
 * <p>To convert to/from String, use an implementation of the
 * StringFormatter interface.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ConvertLong
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(long l)
      throws ConversionException
   {
      return (double)l;
   }

   public static double toDouble(Long l)
      throws ConversionException
   {
      return l.doubleValue();
   }

   public static Double toDoubleObject(long l)
      throws ConversionException
   {
      return new Double((double)l);
   }

   public static Double toDoubleObject(Long l)
      throws ConversionException
   {
      return toDoubleObject(l.longValue());
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(long l)
      throws ConversionException
   {
      return (float)l;
   }

   public static float toFloat(Long l)
      throws ConversionException
   {
      return l.floatValue();
   }

   public static Float toFloatObject(long l)
      throws ConversionException
   {
      return new Float((float)l);
   }

   public static Float toFloatObject(Long l)
      throws ConversionException
   {
      return toFloatObject(l.longValue());
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(long l)
      throws ConversionException
   {
      return new BigDecimal((double)l);
   }

   public static BigDecimal toBigDecimal(Long l)
      throws ConversionException
   {
      return toBigDecimal(l.longValue());
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(long l)
      throws ConversionException
   {
      return l;
   }

   public static long toLong(Long l)
      throws ConversionException
   {
      return l.longValue();
   }

   public static Long toLongObject(long l)
      throws ConversionException
   {
      return new Long(l);
   }

   public static Long toLongObject(Long l)
      throws ConversionException
   {
      return l;
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(long l)
      throws ConversionException
   {
      if ((l > Integer.MAX_VALUE) || (l < Integer.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (int)l;
   }

   public static int toInteger(Long l)
      throws ConversionException
   {
      return toInteger(l.longValue());
   }

   public static Integer toIntegerObject(long l)
      throws ConversionException
   {
      return new Integer(toInteger(l));
   }

   public static Integer toIntegerObject(Long l)
      throws ConversionException
   {
      return toIntegerObject(l.longValue());
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(long l)
      throws ConversionException
   {
      if ((l > Short.MAX_VALUE) || (l < Short.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (short)l;
   }

   public static short toShort(Long l)
      throws ConversionException
   {
      return toShort(l.longValue());
   }

   public static Short toShortObject(long l)
      throws ConversionException
   {
      return new Short(toShort(l));
   }

   public static Short toShortObject(Long l)
      throws ConversionException
   {
      return toShortObject(l.longValue());
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(long l)
      throws ConversionException
   {
      if ((l > Byte.MAX_VALUE) || (l < Byte.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (byte)l;
   }

   public static byte toByte(Long l)
      throws ConversionException
   {
      return toByte(l.longValue());
   }

   public static Byte toByteObject(long l)
      throws ConversionException
   {
      return new Byte(toByte(l));
   }

   public static Byte toByteObject(Long l)
      throws ConversionException
   {
      return toByteObject(l.longValue());
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(long l)
      throws ConversionException
   {
      if (l == 0)
      {
         return false;
      }
      else if (l == 1)
      {
         return true;
      }
      else
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
   }

   public static boolean toBoolean(Long l)
      throws ConversionException
   {
      return toBoolean(l.longValue());
   }

   public static Boolean toBooleanObject(long l)
      throws ConversionException
   {
      return new Boolean(toBoolean(l));
   }

   public static Boolean toBooleanObject(Long l)
      throws ConversionException
   {
      return toBooleanObject(l.longValue());
   }
}