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
 * Converts from doubles to other data types.
 *
 * <p>The following rules from the ODBC specification are followed; the
 * JDBC specification does not address these in spite of allowing the
 * conversions they apply to:</p>
 *
 * <ul>
 * <li>Methods throw an exception when a narrowing conversion exceeds
 * the range of the target type.</li>
 * <li>When converting from floating point to integer, truncation of 
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

public class ConvertDouble
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(double d)
      throws ConversionException
   {
      return d;
   }

   public static double toDouble(Double d)
      throws ConversionException
   {
      return d.doubleValue();
   }

   public static Double toDoubleObject(double d)
      throws ConversionException
   {
      return new Double(d);
   }

   public static Double toDoubleObject(Double d)
      throws ConversionException
   {
      return d;
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(double d)
      throws ConversionException
   {
      if ((d > Float.MAX_VALUE) || (d < Float.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (float)d;
   }

   public static float toFloat(Double d)
      throws ConversionException
   {
      return toFloat(d.doubleValue());
   }

   public static Float toFloatObject(double d)
      throws ConversionException
   {
      return new Float(toFloat(d));
   }

   public static Float toFloatObject(Double d)
      throws ConversionException
   {
      return toFloatObject(d.doubleValue());
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(double d)
      throws ConversionException
   {
      return new BigDecimal(d);
   }

   public static BigDecimal toBigDecimal(Double d)
      throws ConversionException
   {
      return new BigDecimal(d.doubleValue());
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(double d)
      throws ConversionException
   {
      if ((d > Long.MAX_VALUE) || (d < Long.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (long)d;
   }

   public static long toLong(Double d)
      throws ConversionException
   {
      return toLong(d.doubleValue());
   }

   public static Long toLongObject(double d)
      throws ConversionException
   {
      return new Long(toLong(d));
   }

   public static Long toLongObject(Double d)
      throws ConversionException
   {
      return toLongObject(d.doubleValue());
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(double d)
      throws ConversionException
   {
      if ((d > Integer.MAX_VALUE) || (d < Integer.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (int)d;
   }

   public static int toInteger(Double d)
      throws ConversionException
   {
      return toInteger(d.doubleValue());
   }

   public static Integer toIntegerObject(double d)
      throws ConversionException
   {
      return new Integer(toInteger(d));
   }

   public static Integer toIntegerObject(Double d)
      throws ConversionException
   {
      return toIntegerObject(d.doubleValue());
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(double d)
      throws ConversionException
   {
      if ((d > Short.MAX_VALUE) || (d < Short.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (short)d;
   }

   public static short toShort(Double d)
      throws ConversionException
   {
      return toShort(d.doubleValue());
   }

   public static Short toShortObject(double d)
      throws ConversionException
   {
      return new Short(toShort(d));
   }

   public static Short toShortObject(Double d)
      throws ConversionException
   {
      return toShortObject(d.doubleValue());
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(double d)
      throws ConversionException
   {
      if ((d > Byte.MAX_VALUE) || (d < Byte.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (byte)d;
   }

   public static byte toByte(Double d)
      throws ConversionException
   {
      return toByte(d.doubleValue());
   }

   public static Byte toByteObject(double d)
      throws ConversionException
   {
      return new Byte(toByte(d));
   }

   public static Byte toByteObject(Double d)
      throws ConversionException
   {
      return toByteObject(d.doubleValue());
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(double d)
      throws ConversionException
   {
      if (d == 0.0)
      {
         return false;
      }
      else if (d == 1.0)
      {
         return true;
      }
      else
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
   }

   public static boolean toBoolean(Double d)
      throws ConversionException
   {
      return toBoolean(d.doubleValue());
   }

   public static Boolean toBooleanObject(double d)
      throws ConversionException
   {
      return new Boolean(toBoolean(d));
   }

   public static Boolean toBooleanObject(Double d)
      throws ConversionException
   {
      return toBooleanObject(d.doubleValue());
   }
}
