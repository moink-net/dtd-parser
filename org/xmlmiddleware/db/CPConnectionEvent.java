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

package org.xmlmiddleware.db;

import java.lang.*;
import java.sql.*;
import java.util.*;

/**
 * An event on a connection.
 *
 * <p>For why javax.sql.ConnectionEvent is not used, see CPConnectionEventSource.</p>
 *
 * @author Sean Walter, 2001
 * @version 2.0
 * @see CPConnectionEventSource
 */

public class CPConnectionEvent 
   extends EventObject
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a CPConnectionEvent.
    *
    * @param conn Source object.
    */
   CPConnectionEvent(CPConnection conn)
   {
      super(conn);
   }

   /**
    * Create a CPConnectionEvent.
    *
    * @param conn Source object.
    * @param ex Exception associated with event.
    */
   CPConnectionEvent(CPConnection conn, SQLException ex)
   {
      super(conn);
      m_exception = ex;
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Get associated exception if present.
    *
    * @return Associated exception, or null if none.
    */
   public SQLException getSQLException()
   {
      return m_exception;
   }

   //**************************************************************************
   // Class variables
   //**************************************************************************

   protected SQLException m_exception;
}