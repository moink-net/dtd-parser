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

package org.xmlmiddleware.conversions.formatters.external;

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;

import java.sql.Types;

/**
 * Implements the StringFormatter interface for binary data represented as Base64.
 *
 * <p><b>THIS CLASS NOT YET IMPLEMENTED. IT IS INCLUDED FOR COMPLETENESS.</b></p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 * @see org.xmlmiddleware.conversions.ByteArray
 */

public class Base64Formatter implements StringFormatter
{
   // ********************************************************************
   // Constructors
   // ********************************************************************

   public Base64Formatter()
   {
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Parse a Base64 string and return it as an org.xmlmiddleware.conversions.ByteArray object.
    *
    * @param The string to parse.
    * @param A JDBC Types value indicating the type of object to return.
    * @return An org.xmlmiddleware.conversions.ByteArray.
    * @exception XMLMiddlewareException Thrown if the string can't be parsed.
    */
   public Object parse(String s, int jdbcType) throws XMLMiddlewareException
   {
      switch(jdbcType)
      {
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
            throw new XMLMiddlewareException("Method not yet implemented.");

         default:
            throw new XMLMiddlewareException("Conversion to specified JDBC type not supported.");
      }
   }

   /**
    * Format an org.xmlmiddleware.conversions.ByteArray object as Base64.
    *
    * @param The object to serialize. Must be an org.xmlmiddleware.conversion.ByteArray.
    * @return The string
    * @exception XMLMiddlewareException Thrown if the object is not an
    *   org.xmlmiddleware.conversion.ByteArray.
    */
   public String format(Object o) throws XMLMiddlewareException
   {
      if (o instanceof ByteArray)
      {
         throw new XMLMiddlewareException("Method not yet implemented.");
      }
      else
         throw new XMLMiddlewareException("Object must be an org.xmlmiddleware.conversions.ByteArray.");
   }

   /**
    * Whether the class can convert to/from a certain type of object.
    *
    * <p>This method returns true for Types.BINARY, VARBINARY, and LONGVARBINARY.
    * It returns false for all other types.</p>
    *
    * @param type The JDBC Types value corresponding to the object type.
    * @return Whether the type is supported
    */
   public boolean canConvert(int type)
   {
      return ((type == Types.BINARY) ||
              (type == Types.VARBINARY) ||
              (type == Types.LONGVARBINARY));
   }
}
