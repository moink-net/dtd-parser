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
 * Converts from booleans to other data types.
 *
 * <p>The following rules from the ODBC specification are followed; the
 * JDBC specification does not address these in spite of allowing the
 * conversions they apply to:</p>
 *
 * <ul>
 * <li>When converting from boolean, true is 1 and false is 0.</li>
 * </ul>
 *
 * <p>To convert to/from String, use an implementation of the
 * StringFormatter interface.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ConvertBoolean
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // To double
   //**************************************************************************

   public static double toDouble(boolean b)
   {
      return (b ? 1.0 : 0.0);
   }

   public static double toDouble(Boolean b)
   {
      return (b.booleanValue() ? 1.0 : 0.0);
   }

   public static Double toDoubleObject(boolean b)
   {
      return new Double(b ? 1.0 : 0.0);
   }

   public static Double toDoubleObject(Boolean b)
   {
      return new Double(b.booleanValue() ? 1.0 : 0.0);
   }

   //**************************************************************************
   // To float
   //**************************************************************************

   public static float toFloat(boolean b)
   {
      return (b ? (float)1.0 : (float)0.0);
   }

   public static float toFloat(Boolean b)
   {
      return (b.booleanValue() ? (float)1.0 : (float)0.0);
   }

   public static Float toFloatObject(boolean b)
   {
      return new Float(b ? (float)1.0 : (float)0.0);
   }

   public static Float toFloatObject(Boolean b)
   {
      return new Float(b.booleanValue() ? (float)1.0 : (float)0.0);
   }

   //**************************************************************************
   // To BigDecimal
   //**************************************************************************

   public static BigDecimal toBigDecimal(boolean b)
   {
      return new BigDecimal(b ? 1.0 : 0.0);
   }

   public static BigDecimal toBigDecimal(Boolean b)
   {
      return new BigDecimal(b.booleanValue() ? 1.0 : 0.0);
   }

   //**************************************************************************
   // To long
   //**************************************************************************

   public static long toLong(boolean b)
   {
      return (b ? 1 : 0);
   }

   public static long toLong(Boolean b)
   {
      return (b.booleanValue() ? 1 : 0);
   }

   public static Long toLongObject(boolean b)
   {
      return new Long(b ? 1 : 0);
   }

   public static Long toLongObject(Boolean b)
   {
      return new Long(b.booleanValue() ? 1 : 0);
   }

   //**************************************************************************
   // To integer
   //**************************************************************************

   public static int toInteger(boolean b)
   {
      return (b ? 1 : 0);
   }

   public static int toInteger(Boolean b)
   {
      return (b.booleanValue() ? 1 : 0);
   }

   public static Integer toIntegerObject(boolean b)
   {
      return new Integer(b ? 1 : 0);
   }

   public static Integer toIntegerObject(Boolean b)
   {
      return new Integer(b.booleanValue() ? 1 : 0);
   }

   //**************************************************************************
   // To short
   //**************************************************************************

   public static short toShort(boolean b)
   {
      return (b ? (short)1 : (short)0);
   }

   public static short toShort(Boolean b)
   {
      return (b.booleanValue() ? (short)1 : (short)0);
   }

   public static Short toShortObject(boolean b)
   {
      return new Short(b ? (short)1 : (short)0);
   }

   public static Short toShortObject(Boolean b)
   {
      return new Short(b.booleanValue() ? (short)1 : (short)0);
   }

   //**************************************************************************
   // To byte
   //**************************************************************************

   public static byte toByte(boolean b)
   {
      return (b ? (byte)1 : (byte)0);
   }

   public static byte toByte(Boolean b)
   {
      return (b.booleanValue() ? (byte)1 : (byte)0);
   }

   public static Byte toByteObject(boolean b)
   {
      return new Byte(b ? (byte)1 : (byte)0);
   }

   public static Byte toByteObject(Boolean b)
   {
      return new Byte(b.booleanValue() ? (byte)1 : (byte)0);
   }

   //**************************************************************************
   // To boolean
   //**************************************************************************

   public static boolean toBoolean(boolean b)
   {
      return b;
   }

   public static boolean toBoolean(Boolean b)
   {
      return b.booleanValue();
   }

   public static Boolean toBooleanObject(boolean b)
   {
      return new Boolean(b);
   }

   public static Boolean toBooleanObject(Boolean b)
   {
      return b;
   }
}
