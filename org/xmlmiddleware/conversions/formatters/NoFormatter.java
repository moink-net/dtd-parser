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

/**
 * StringFormatter that just throws exceptions. For unsupported data types.
 *
 * @author Ronald Bourret, 2002
 * @version 2.0
 */

public class NoFormatter implements StringFormatter
{
   // ********************************************************************
   // Constructors
   // ********************************************************************

   public NoFormatter()
   {
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Throw an exception instead of parsing a string.
    *
    * @param The string to parse.
    * @param A JDBC Types value indicating the type of object to return.
    * @return Never reached.
    * @exception XMLMiddlewareException Always thrown
    */
   public Object parse(String s, int jdbcType) throws XMLMiddlewareException
   {
      throw new XMLMiddlewareException("Conversion to specified JDBC type not supported.");
   }

   /**
    * Throw an exception instead of formatting an object.
    *
    * @param The object to serialize.
    * @return Never reached.
    * @exception XMLMiddlewareException Always thrown.
    */
   public String format(Object o) throws XMLMiddlewareException
   {
      throw new XMLMiddlewareException("Object cannot be converted.");
   }

   /**
    * Whether the class can convert to/from a certain type of object.
    *
    * @param type The JDBC Types value corresponding to the object type.
    * @return Always false.
    */
   public boolean canConvert(int type)
   {
      return false;
   }
}
