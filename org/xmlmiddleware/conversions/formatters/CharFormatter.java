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

import java.sql.Types;

/**
 * Implements the StringFormatter interface for Strings.
 *
 * <p>Note that this implementation is trivial -- it simply returns the
 * String that is passed to parse() or format(). It exists only for
 * completeness. That is, XML-DBMS needs a default StringFormatter for
 * each JDBC type and this is the implementation for CHAR, VARCHAR, and
 * LONGVARCHAR.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class CharFormatter implements StringFormatter
{
   // ********************************************************************
   // Constructors
   // ********************************************************************

   /** Construct a new CharFormatter object. */
   public CharFormatter()
   {
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * "Parse" a string and return (the same) string.
    *
    * @param The string to "parse".
    * @param A JDBC Types value indicating the type of object to return.
    * @return A String
    * @exception ConversionException Thrown if jdbcType is not CHAR, VARCHAR, or
    *    LONGVARCHAR.
    */
   public Object parse(String s, int jdbcType) throws ConversionException
   {
      switch(jdbcType)
      {
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            return s;

         default:
            throw new ConversionException("Conversion to specified JDBC type not supported.");
      }
   }

   /**
    * "Format" a string and return (the same) string.
    *
    * @param The string to "format"
    * @return The string
    * @exception ConversionException Thrown if the object is not a String.
    */
   public String format(Object o) throws ConversionException
   {
      if (o instanceof String)
      {
         return (String)o;
      }
      else
         throw new ConversionException("Object must be a String.");
   }

   /**
    * Whether the class can convert to/from a certain type of object.
    *
    * <p>This method returns true for Types.CHAR, VARCHAR, and LONGVARCHAR.
    * It returns false for all other types.</p>
    *
    * @param type The JDBC Types value corresponding to the object type.
    * @return Whether the type is supported
    */
   public boolean canConvert(int type)
   {
      return ((type == Types.CHAR) ||
              (type == Types.VARCHAR) ||
              (type == Types.LONGVARCHAR));
   }
}
