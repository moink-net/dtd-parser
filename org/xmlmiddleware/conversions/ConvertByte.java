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
 * Converts from bytes to other data types.
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

public class ConvertByte
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(byte b)
      throws ConversionException
   {
      return (double)b;
   }

   public static double toDouble(Byte b)
      throws ConversionException
   {
      return b.doubleValue();
   }

   public static Double toDoubleObject(byte b)
      throws ConversionException
   {
      return new Double((double)b);
   }

   public static Double toDoubleObject(Byte b)
      throws ConversionException
   {
      return new Double(b.doubleValue());
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(byte b)
      throws ConversionException
   {
      return (float)b;
   }

   public static float toFloat(Byte b)
      throws ConversionException
   {
      return b.floatValue();
   }

   public static Float toFloatObject(byte b)
      throws ConversionException
   {
      return new Float((float)b);
   }

   public static Float toFloatObject(Byte b)
      throws ConversionException
   {
      return new Float(b.floatValue());
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(byte b)
      throws ConversionException
   {
      return new BigDecimal((double)b);
   }

   public static BigDecimal toBigDecimal(Byte b)
      throws ConversionException
   {
      return new BigDecimal(b.doubleValue());
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(byte b)
      throws ConversionException
   {
      return (long)b;
   }

   public static long toLong(Byte b)
      throws ConversionException
   {
      return b.longValue();
   }

   public static Long toLongObject(byte b)
      throws ConversionException
   {
      return new Long((long)b);
   }

   public static Long toLongObject(Byte b)
      throws ConversionException
   {
      return new Long(b.longValue());
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(byte b)
      throws ConversionException
   {
      return (int)b;
   }

   public static int toInteger(Byte b)
      throws ConversionException
   {
      return b.intValue();
   }

   public static Integer toIntegerObject(byte b)
      throws ConversionException
   {
      return new Integer((int)b);
   }

   public static Integer toIntegerObject(Byte b)
      throws ConversionException
   {
      return new Integer(b.intValue());
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(byte b)
      throws ConversionException
   {
      return (short)b;
   }

   public static short toShort(Byte b)
      throws ConversionException
   {
      return b.shortValue();
   }

   public static Short toShortObject(byte b)
      throws ConversionException
   {
      return new Short((short)b);
   }

   public static Short toShortObject(Byte b)
      throws ConversionException
   {
      return new Short(b.shortValue());
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(byte b)
      throws ConversionException
   {
      return b;
   }

   public static byte toByte(Byte b)
      throws ConversionException
   {
      return b.byteValue();
   }

   public static Byte toByteObject(byte b)
      throws ConversionException
   {
      return new Byte(b);
   }

   public static Byte toByteObject(Byte b)
      throws ConversionException
   {
      return b;
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(byte b)
      throws ConversionException
   {
      if (b == 0)
      {
         return false;
      }
      else if (b == 1)
      {
         return true;
      }
      else
         throw new ConversionException("Conversion resulted in truncation of significant digits.");
   }

   public static boolean toBoolean(Byte b)
      throws ConversionException
   {
      return toBoolean(b.byteValue());
   }

   public static Boolean toBooleanObject(byte b)
      throws ConversionException
   {
      return new Boolean(toBoolean(b));
   }

   public static Boolean toBooleanObject(Byte b)
      throws ConversionException
   {
      return toBooleanObject(b.byteValue());
   }
}