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
 * Converts from BigDecimals to other data types.
 *
 * <p>The following rules from the ODBC specification are followed; the
 * JDBC specification does not address these in spite of allowing the
 * conversions they apply to:</p>
 *
 * <ul>
 * <li>Methods throw an exception when a narrowing conversion exceeds
 * the range of the target type.</li>
 * <li>When converting from integer to decimal, loss of precision
 * is allowed.</li>
 * <li>When converting from decimal to integer, truncation of 
 * fractional digits is allowed.</li>
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

public class ConvertBigDecimal
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private static final BigDecimal MAX_DOUBLE = new BigDecimal(Double.MAX_VALUE),
                                   MIN_DOUBLE = new BigDecimal(Double.MIN_VALUE),
                                   MAX_FLOAT = new BigDecimal(Float.MAX_VALUE),
                                   MIN_FLOAT = new BigDecimal(Float.MIN_VALUE),
                                   MAX_LONG = new BigDecimal(Long.MAX_VALUE),
                                   MIN_LONG = new BigDecimal(Long.MIN_VALUE),
                                   MAX_INTEGER = new BigDecimal(Integer.MAX_VALUE),
                                   MIN_INTEGER = new BigDecimal(Integer.MIN_VALUE),
                                   MAX_SHORT = new BigDecimal(Short.MAX_VALUE),
                                   MIN_SHORT = new BigDecimal(Short.MIN_VALUE),
                                   MAX_BYTE = new BigDecimal(Byte.MAX_VALUE),
                                   MIN_BYTE = new BigDecimal(Byte.MIN_VALUE),
                                   ZERO = new BigDecimal(0.0),
                                   ONE = new BigDecimal(1.0);

   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(BigDecimal b)
      throws ConversionException
   {
      if ((b.compareTo(MAX_DOUBLE) == 1) || (b.compareTo(MIN_DOUBLE) == -1))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return b.doubleValue();
   }

   public static Double toDoubleObject(BigDecimal b)
      throws ConversionException
   {
      return new Double(toDouble(b));
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(BigDecimal b)
      throws ConversionException
   {
      if ((b.compareTo(MAX_FLOAT) == 1) || (b.compareTo(MIN_FLOAT) == -1))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return b.floatValue();
   }

   public static Float toFloatObject(BigDecimal b)
      throws ConversionException
   {
      return new Float(toFloat(b));
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(BigDecimal b)
      throws ConversionException
   {
      return b;
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(BigDecimal b)
      throws ConversionException
   {
      if ((b.compareTo(MAX_LONG) == 1) || (b.compareTo(MIN_LONG) == -1))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return b.longValue();
   }

   public static Long toLongObject(BigDecimal b)
      throws ConversionException
   {
      return new Long(toLong(b));
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(BigDecimal b)
      throws ConversionException
   {
      if ((b.compareTo(MAX_INTEGER) == 1) || (b.compareTo(MIN_INTEGER) == -1))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return b.intValue();
   }

   public static Integer toIntegerObject(BigDecimal b)
      throws ConversionException
   {
      return new Integer(toInteger(b));
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(BigDecimal b)
      throws ConversionException
   {
      if ((b.compareTo(MAX_SHORT) == 1) || (b.compareTo(MIN_SHORT) == -1))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return ConvertLong.toShort(b.longValue());
   }

   public static Short toShortObject(BigDecimal b)
      throws ConversionException
   {
      return new Short(toShort(b));
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(BigDecimal b)
      throws ConversionException
   {
      if ((b.compareTo(MAX_BYTE) == 1) || (b.compareTo(MIN_BYTE) == -1))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return ConvertLong.toByte(b.longValue());
   }

   public static Byte toByteObject(BigDecimal b)
      throws ConversionException
   {
      return new Byte(toByte(b));
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(BigDecimal b)
      throws ConversionException
   {
      if (b.compareTo(ZERO) == 0)
      {
         return false;
      }
      else if (b.compareTo(ONE) == 0)
      {
         return true;
      }
      else
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
   }

   public static Boolean toBooleanObject(BigDecimal b)
      throws ConversionException
   {
      return new Boolean(toBoolean(b));
   }
}
