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
 * Converts from shorts to other data types.
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

public class ConvertShort
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(short s)
      throws ConversionException
   {
      return (double)s;
   }

   public static double toDouble(Short s)
      throws ConversionException
   {
      return s.doubleValue();
   }

   public static Double toDoubleObject(short s)
      throws ConversionException
   {
      return new Double((double)s);
   }

   public static Double toDoubleObject(Short s)
      throws ConversionException
   {
      return new Double(s.doubleValue());
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(short s)
      throws ConversionException
   {
      return (float)s;
   }

   public static float toFloat(Short s)
      throws ConversionException
   {
      return s.floatValue();
   }

   public static Float toFloatObject(short s)
      throws ConversionException
   {
      return new Float((float)s);
   }

   public static Float toFloatObject(Short s)
      throws ConversionException
   {
      return new Float(s.floatValue());
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(short s)
      throws ConversionException
   {
      return new BigDecimal((double)s);
   }

   public static BigDecimal toBigDecimal(Short s)
      throws ConversionException
   {
      return new BigDecimal(s.doubleValue());
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(short s)
      throws ConversionException
   {
      return (long)s;
   }

   public static long toLong(Short s)
      throws ConversionException
   {
      return s.longValue();
   }

   public static Long toLongObject(short s)
      throws ConversionException
   {
      return new Long((long)s);
   }

   public static Long toLongObject(Short s)
      throws ConversionException
   {
      return new Long(s.longValue());
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(short s)
      throws ConversionException
   {
      return (int)s;
   }

   public static int toInteger(Short s)
      throws ConversionException
   {
      return s.intValue();
   }

   public static Integer toIntegerObject(short s)
      throws ConversionException
   {
      return new Integer((int)s);
   }

   public static Integer toIntegerObject(Short s)
      throws ConversionException
   {
      return new Integer(s.intValue());
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(short s)
      throws ConversionException
   {
      return s;
   }

   public static short toShort(Short s)
      throws ConversionException
   {
      return s.shortValue();
   }

   public static Short toShortObject(short s)
      throws ConversionException
   {
      return new Short(s);
   }

   public static Short toShortObject(Short s)
      throws ConversionException
   {
      return s;
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(short s)
      throws ConversionException
   {
      if ((s > Byte.MAX_VALUE) || (s < Byte.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (byte)s;
   }

   public static byte toByte(Short s)
      throws ConversionException
   {
      return toByte(s.shortValue());
   }

   public static Byte toByteObject(short s)
      throws ConversionException
   {
      return new Byte(toByte(s));
   }

   public static Byte toByteObject(Short s)
      throws ConversionException
   {
      return toByteObject(s.shortValue());
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(short s)
      throws ConversionException
   {
      if (s == 0)
      {
         return false;
      }
      else if (s == 1)
      {
         return true;
      }
      else
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
   }

   public static boolean toBoolean(Short s)
      throws ConversionException
   {
      return toBoolean(s.shortValue());
   }

   public static Boolean toBooleanObject(short s)
      throws ConversionException
   {
      return new Boolean(toBoolean(s));
   }

   public static Boolean toBooleanObject(Short s)
      throws ConversionException
   {
      return toBooleanObject(s.shortValue());
   }
}