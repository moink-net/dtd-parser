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

import org.xmlmiddleware.utils.XMLMiddlewareException;

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
   {
      return (double)l;
   }

   public static double toDouble(Long l)
   {
      return l.doubleValue();
   }

   public static Double toDoubleObject(long l)
   {
      return new Double((double)l);
   }

   public static Double toDoubleObject(Long l)
   {
      return toDoubleObject(l.longValue());
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(long l)
   {
      return (float)l;
   }

   public static float toFloat(Long l)
   {
      return l.floatValue();
   }

   public static Float toFloatObject(long l)
   {
      return new Float((float)l);
   }

   public static Float toFloatObject(Long l)
   {
      return toFloatObject(l.longValue());
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(long l)
   {
      return new BigDecimal((double)l);
   }

   public static BigDecimal toBigDecimal(Long l)
   {
      return toBigDecimal(l.longValue());
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(long l)
   {
      return l;
   }

   public static long toLong(Long l)
   {
      return l.longValue();
   }

   public static Long toLongObject(long l)
   {
      return new Long(l);
   }

   public static Long toLongObject(Long l)
   {
      return l;
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(long l)
      throws XMLMiddlewareException
   {
      if ((l > Integer.MAX_VALUE) || (l < Integer.MIN_VALUE))
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
      return (int)l;
   }

   public static int toInteger(Long l)
      throws XMLMiddlewareException
   {
      return toInteger(l.longValue());
   }

   public static Integer toIntegerObject(long l)
      throws XMLMiddlewareException
   {
      return new Integer(toInteger(l));
   }

   public static Integer toIntegerObject(Long l)
      throws XMLMiddlewareException
   {
      return toIntegerObject(l.longValue());
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(long l)
      throws XMLMiddlewareException
   {
      if ((l > Short.MAX_VALUE) || (l < Short.MIN_VALUE))
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
      return (short)l;
   }

   public static short toShort(Long l)
      throws XMLMiddlewareException
   {
      return toShort(l.longValue());
   }

   public static Short toShortObject(long l)
      throws XMLMiddlewareException
   {
      return new Short(toShort(l));
   }

   public static Short toShortObject(Long l)
      throws XMLMiddlewareException
   {
      return toShortObject(l.longValue());
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(long l)
      throws XMLMiddlewareException
   {
      if ((l > Byte.MAX_VALUE) || (l < Byte.MIN_VALUE))
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
      return (byte)l;
   }

   public static byte toByte(Long l)
      throws XMLMiddlewareException
   {
      return toByte(l.longValue());
   }

   public static Byte toByteObject(long l)
      throws XMLMiddlewareException
   {
      return new Byte(toByte(l));
   }

   public static Byte toByteObject(Long l)
      throws XMLMiddlewareException
   {
      return toByteObject(l.longValue());
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(long l)
      throws XMLMiddlewareException
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
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
   }

   public static boolean toBoolean(Long l)
      throws XMLMiddlewareException
   {
      return toBoolean(l.longValue());
   }

   public static Boolean toBooleanObject(long l)
      throws XMLMiddlewareException
   {
      return new Boolean(toBoolean(l));
   }

   public static Boolean toBooleanObject(Long l)
      throws XMLMiddlewareException
   {
      return toBooleanObject(l.longValue());
   }
}
