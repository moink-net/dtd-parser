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

import java.sql.*;
import java.util.*;

/**
 * An event on a prepared statement.
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public class SPStatementEvent 
   extends EventObject
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create an SPStatementEvent.
    *
    * @param stmt Source object.
    */
   SPStatementEvent(SPPreparedStatement stmt)
   {
      super(stmt);
   }

   /**
    * Create StatementEvent.
    *
    * @param stmt Source object.
    * @param ex Exception associated with event.
    */
   SPStatementEvent(SPPreparedStatement stmt, SQLException ex)
   {
      super(stmt);
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
