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
 * Converts from integers to other data types.
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

public class ConvertInteger
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(int i)
      throws ConversionException
   {
      return (double)i;
   }

   public static double toDouble(Integer i)
      throws ConversionException
   {
      return i.doubleValue();
   }

   public static Double toDoubleObject(int i)
      throws ConversionException
   {
      return new Double((double)i);
   }

   public static Double toDoubleObject(Integer i)
      throws ConversionException
   {
      return new Double(i.doubleValue());
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(int i)
      throws ConversionException
   {
      return (float)i;
   }

   public static float toFloat(Integer i)
      throws ConversionException
   {
      return i.floatValue();
   }

   public static Float toFloatObject(int i)
      throws ConversionException
   {
      return new Float((float)i);
   }

   public static Float toFloatObject(Integer i)
      throws ConversionException
   {
      return new Float(i.floatValue());
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(int i)
      throws ConversionException
   {
      return new BigDecimal((double)i);
   }

   public static BigDecimal toBigDecimal(Integer i)
      throws ConversionException
   {
      return new BigDecimal(i.doubleValue());
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(int i)
      throws ConversionException
   {
      return (long)i;
   }

   public static long toLong(Integer i)
      throws ConversionException
   {
      return i.longValue();
   }

   public static Long toLongObject(int i)
      throws ConversionException
   {
      return new Long((long)i);
   }

   public static Long toLongObject(Integer i)
      throws ConversionException
   {
      return new Long(i.longValue());
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(int i)
      throws ConversionException
   {
      return i;
   }

   public static int toInteger(Integer i)
      throws ConversionException
   {
      return i.intValue();
   }

   public static Integer toIntegerObject(int i)
      throws ConversionException
   {
      return new Integer(i);
   }

   public static Integer toIntegerObject(Integer i)
      throws ConversionException
   {
      return i;
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(int i)
      throws ConversionException
   {
      if ((i > Short.MAX_VALUE) || (i < Short.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (short)i;
   }

   public static short toShort(Integer i)
      throws ConversionException
   {
      return toShort(i.intValue());
   }

   public static Short toShortObject(int i)
      throws ConversionException
   {
      return new Short(toShort(i));
   }

   public static Short toShortObject(Integer i)
      throws ConversionException
   {
      return toShortObject(i.intValue());
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(int i)
      throws ConversionException
   {
      if ((i > Byte.MAX_VALUE) || (i < Byte.MIN_VALUE))
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
      return (byte)i;
   }

   public static byte toByte(Integer i)
      throws ConversionException
   {
      return toByte(i.intValue());
   }

   public static Byte toByteObject(int i)
      throws ConversionException
   {
      return new Byte(toByte(i));
   }

   public static Byte toByteObject(Integer i)
      throws ConversionException
   {
      return toByteObject(i.intValue());
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(int i)
      throws ConversionException
   {
      if (i == 0)
      {
         return false;
      }
      else if (i == 1)
      {
         return true;
      }
      else
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
   }

   public static boolean toBoolean(Integer i)
      throws ConversionException
   {
      return toBoolean(i.intValue());
   }

   public static Boolean toBooleanObject(int i)
      throws ConversionException
   {
      return new Boolean(toBoolean(i));
   }

   public static Boolean toBooleanObject(Integer i)
      throws ConversionException
   {
      return toBooleanObject(i.intValue());
   }
}