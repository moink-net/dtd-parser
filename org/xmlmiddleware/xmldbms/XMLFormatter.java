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

package org.xmlmiddleware.xmldbms;

/**
 * Interface for custom formatting classes.
 *
 * <p>Classes that are used with the FormatClass element in the XML-DBMS
 * mapping language must implement this interface and provide a no-argument
 * constructor. Such classes sometimes support formatting for only a single
 * type of object, such as Boolean. Other times, they support formatting for
 * a number of object types, such as all numeric types or all date/time types.</p>
 *
 * <p><b>NOTE:</b> One possible use of custom formatting classes is a
 * to convert binary data to/from Base64. This could easily be done by
 * wrapping a Base64 converter with an XMLFormatter class.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public interface XMLFormatter
{
   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Parse a string and create an object.
    *
    * <p>The calling application specifies the type of object it wants using
    * a JDBC Types value. JDBC types map to Java types as follows:</p>
    *
    * <pre>
    * CHAR, VARCHAR, LONGVARCHAR:        String<br />
    * DATE, TIME, TIMESTAMP:             java.sql.Date<br />
    * BIGINT:                            Long<br />
    * INTEGER:                           Integer<br />
    * SMALLINT:                          Short<br />
    * TINYINT:                           Byte<br />
    * DECIMAL, NUMERIC:                  BigDecimal<br />
    * DOUBLE, FLOAT:                     Double<br />
    * REAL:                              Float<br />
    * BINARY, VARBINARY, LONGVARBINARY:  byte[]??????????<br />
    * BIT:                               Boolean<br />
    * </pre>
    *
    * @param The string to parse.
    * @param A JDBC Types value indicating the type of object to return.
    * @return The object
    * @exception XMLFormatterException Thrown if the string can't be parsed by
    *     the implementing class.
    */
   public Object parse(String s, int jdbcType) throws XMLFormatterException;

   /**
    * Serialize an object as a string.
    *
    * <p>The implementing class will generally support only a specific type
    * of object, such as an Integer or a Date.</p>
    *
    * @param The object to serialize.
    * @return The string
    * @exception XMLFormatterException Thrown if the object can't be serialized. This
    *    usually occurs when the object is of a type not recognized by the implementing
    *    class, such as when the implementing class operates only on Integers and the
    *    calling code passes a Date.
    */
   public String format(Object o) throws XMLFormatterException;
}