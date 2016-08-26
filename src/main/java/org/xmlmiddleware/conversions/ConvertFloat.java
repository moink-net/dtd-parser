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
 * Converts from floats to other data types.
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

public class ConvertFloat
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(float f)
   {
      return (double)f;
   }

   public static double toDouble(Float f)
   {
      return f.doubleValue();
   }

   public static Double toDoubleObject(float f)
   {
      return new Double((double)f);
   }

   public static Double toDoubleObject(Float f)
   {
      return new Double(f.doubleValue());
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(float f)
   {
      return f;
   }

   public static float toFloat(Float f)
   {
      return f.floatValue();
   }

   public static Float toFloatObject(float f)
   {
      return new Float(f);
   }

   public static Float toFloatObject(Float f)
   {
      return f;
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(float f)
   {
      return new BigDecimal((double)f);
   }

   public static BigDecimal toBigDecimal(Float f)
   {
      return new BigDecimal(f.doubleValue());
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(float f)
      throws XMLMiddlewareException
   {
      if ((f > Long.MAX_VALUE) || (f < Long.MIN_VALUE))
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
      return (long)f;
   }

   public static long toLong(Float f)
      throws XMLMiddlewareException
   {
      return toLong(f.floatValue());
   }

   public static Long toLongObject(float f)
      throws XMLMiddlewareException
   {
      return new Long(toLong(f));
   }

   public static Long toLongObject(Float f)
      throws XMLMiddlewareException
   {
      return toLongObject(f.floatValue());
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(float f)
      throws XMLMiddlewareException
   {
      if ((f > Integer.MAX_VALUE) || (f < Integer.MIN_VALUE))
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
      return (int)f;
   }

   public static int toInteger(Float f)
      throws XMLMiddlewareException
   {
      return toInteger(f.floatValue());
   }

   public static Integer toIntegerObject(float f)
      throws XMLMiddlewareException
   {
      return new Integer(toInteger(f));
   }

   public static Integer toIntegerObject(Float f)
      throws XMLMiddlewareException
   {
      return toIntegerObject(f.floatValue());
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(float f)
      throws XMLMiddlewareException
   {
      if ((f > Short.MAX_VALUE) || (f < Short.MIN_VALUE))
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
      return (short)f;
   }

   public static short toShort(Float f)
      throws XMLMiddlewareException
   {
      return toShort(f.floatValue());
   }

   public static Short toShortObject(float f)
      throws XMLMiddlewareException
   {
      return new Short(toShort(f));
   }

   public static Short toShortObject(Float f)
      throws XMLMiddlewareException
   {
      return toShortObject(f.floatValue());
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(float f)
      throws XMLMiddlewareException
   {
      if ((f > Byte.MAX_VALUE) || (f < Byte.MIN_VALUE))
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
      return (byte)f;
   }

   public static byte toByte(Float f)
      throws XMLMiddlewareException
   {
      return toByte(f.floatValue());
   }

   public static Byte toByteObject(float f)
      throws XMLMiddlewareException
   {
      return new Byte(toByte(f));
   }

   public static Byte toByteObject(Float f)
      throws XMLMiddlewareException
   {
      return toByteObject(f.floatValue());
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(float f)
      throws XMLMiddlewareException
   {
      if (f == 0.0)
      {
         return false;
      }
      else if (f == 1.0)
      {
         return true;
      }
      else
         throw new XMLMiddlewareException("Conversion resulted in truncation of significant digits.");
   }

   public static boolean toBoolean(Float f)
      throws XMLMiddlewareException
   {
      return toBoolean(f.floatValue());
   }

   public static Boolean toBooleanObject(float f)
      throws XMLMiddlewareException
   {
      return new Boolean(toBoolean(f));
   }

   public static Boolean toBooleanObject(Float f)
      throws XMLMiddlewareException
   {
      return toBooleanObject(f.floatValue());
   }
}
