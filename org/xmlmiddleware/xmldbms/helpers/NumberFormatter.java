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
// Changes from version 1.0: New in version 2.0

package org.xmlmiddleware.xmldbms.helpers;

import org.xmlmiddleware.xmldbms.XMLFormatter;
import org.xmlmiddleware.xmldbms.XMLFormatterException;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Wraps a NumberFormat in an XMLFormatter interface.
 *
 * <p>This can also be used to wrap subclasses of NumberFormat, such as DecimalFormat.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class NumberFormatter implements XMLFormatter
{
   // ********************************************************************
   // Class variables
   // ********************************************************************

   private NumberFormat formatter;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   public NumberFormatter(NumberFormat formatter)
   {
      if (formatter == null)
         throw new IllegalArgumentException("formatter argument must be non-null");
      this.formatter = formatter;
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Parse a string according to the parse format used
    * by the underlying NumberFormat object.
    *
    * @param The string to parse.
    * @return A number. This is a Long if possible; otherwise, it is a Double.
    * @exception XMLFormatterException Thrown if the string can't be parsed.
    */
   public Object parse(String s) throws XMLFormatterException
   {
      try
      {
         return formatter.parse(s);
      }
      catch (ParseException p)
      {
         throw new XMLFormatterException(p);
      }
   }

   /**
    * Format an object according to the format used by the
    * underlying NumberFormat object.
    *
    * @param The object to serialize. Must be a Long, Integer,
    *        Short, Byte, Double, or Float.
    * @return The string
    * @exception XMLFormatterException Thrown if the object is not a Long,
    *            Integer, Short, Byte, Double, or Float.
    */
   public String format(Object o) throws XMLFormatterException
   {
      if ((o instanceof Long) || (o instanceof Integer) ||
          (o instanceof Short) ||(o instanceof Byte))
      {
         return formatter.format(((Number)o).longValue());
      }
      else if ((o instanceof Double) || (o instanceof Float))
      {
         return formatter.format(((Number)o).doubleValue());
      }
      else
         throw new XMLFormatterException("Object must be a Long, Integer, Short, Byte, Double, or Float.");
   }

   /**
    * Get the underlying NumberFormat object.
    *
    * @return The NumberFormat object.
    */
   public NumberFormat getNumberFormat()
   {
      return formatter;
   }
}