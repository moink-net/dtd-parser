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

package org.xmlmiddleware.conversions.formatters;

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;

import java.sql.Types;
import java.text.*;
import java.util.Date;

/**
 * Wraps a DateFormat in an StringFormatter interface.
 *
 * <p>This can also be used to wrap subclasses of DateFormat, such as SimpleDateFormat.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class DateFormatter implements StringFormatter
{
   // ********************************************************************
   // Class variables
   // ********************************************************************

   private DateFormat formatter;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   public DateFormatter(DateFormat formatter)
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
    * by the underlying DateFormat object.
    *
    * @param The string to parse.
    * @param A JDBC Types value indicating the type of object to return.
    * @return A Date.
    * @exception XMLMiddlewareException Thrown if the string can't be parsed.
    */
   public Object parse(String s, int jdbcType) throws XMLMiddlewareException
   {
      java.util.Date datetime;

      try
      {
         datetime = formatter.parse(s);
      }
      catch (ParseException p)
      {
         throw new XMLMiddlewareException(p);
      }

      switch(jdbcType)
      {
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
            throw new XMLMiddlewareException("Conversion to binary types not supported.");

         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            throw new XMLMiddlewareException("Use an implementation of StringFormatter to convert to strings.");

         case Types.DOUBLE:
         case Types.FLOAT:
         case Types.REAL:
         case Types.DECIMAL:
         case Types.NUMERIC:
         case Types.BIGINT:
         case Types.INTEGER:
         case Types.SMALLINT:
         case Types.TINYINT:
         case Types.BIT:
            throw new XMLMiddlewareException("Conversion to numeric types not supported.");

         case Types.DATE:
            return new java.sql.Date(datetime.getTime());

         case Types.TIME:
            return new java.sql.Time(datetime.getTime());

         case Types.TIMESTAMP:
            return new java.sql.Timestamp(datetime.getTime());

         default:
            throw new XMLMiddlewareException("Conversion to specified JDBC type not supported.");
      }
   }

   /**
    * Format an object according to the format used by the
    * underlying DateFormat object.
    *
    * @param The object to serialize. Must be a Date.
    * @return The string
    * @exception XMLMiddlewareException Thrown if the object is not a Date.
    */
   public String format(Object o) throws XMLMiddlewareException
   {
      // Note that this works because java.sql.Date, Time, and Timestamp
      // all extend java.util.Date.

      if (o instanceof java.util.Date)
      {
         return formatter.format((Date)o);
      }
      else
         throw new XMLMiddlewareException("Object must be a Date.");
   }

   /**
    * Whether the class can convert to/from a certain type of object.
    *
    * <p>This method returns true for Types.DATE, TIME, and TIMESTAMP.
    * It returns false for all other types.</p>
    *
    * @param type The JDBC Types value corresponding to the object type.
    * @return Whether the type is supported
    */
   public boolean canConvert(int type)
   {
      return ((type == Types.DATE) ||
              (type == Types.TIME) ||
              (type == Types.TIMESTAMP));
   }

   /**
    * Get the underlying DateFormat object.
    *
    * @return The DateFormat object.
    */
   public DateFormat getDateFormat()
   {
      return formatter;
   }
}
