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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.conversions;

import java.sql.Time;

/**
 * Converts from java.sql.Times to other data types.
 *
 * <p>Actually, this class is here for completeness. It doesn't
 * do anything useful. We follow the JDBC Getting Started manual with
 * respect to converting Time to Timestamp. ODBC allows this,
 * setting the date to the current date, but the Getting
 * Started manual states that setObject cannot do this. Since
 * the JDBC spec doesn't state whether this conversion is
 * supported, this is the best we have to go on...</p>
 *
 * <p>To convert to/from String, use an implementation of the
 * Formatter interface.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ConvertTime
{
   //**************************************************************************
   // Public methods
   //**************************************************************************

   public static Time toTime(Time t)
   {
      return t;
   }
}
