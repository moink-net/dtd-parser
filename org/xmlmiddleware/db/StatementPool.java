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

import org.xmlmiddleware.utils.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Implements a statement pool.
 *
 * @author Sean Walter, 2001
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class StatementPool 
   extends Pool
   implements SPStatementEventListener
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new StatementPool.
    *
    * @param conn Connection to prepare statements from.
    */
   public StatementPool(Connection conn)
   {
      m_connection = conn;
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Check out a prepared statement for the specified SQL string.
    *
    * @param sqlString The SQL string to use.
    *
    * @return The PreparedStatement.
    * @exception XMLMiddlewareException Thrown if the pool does not recognize the string or cannot
    * return the specified object for any reason, such as lack of resources.
    */
   public Object checkOut(Object sqlString)
      throws XMLMiddlewareException
   {
      PreparedStatement   realStmt;
      SPPreparedStatement stmt;

      if (!(sqlString instanceof String))
         throw new XMLMiddlewareException("Object ID must be a String.");

      // Check out a PreparedStatement, wrap it ...
      realStmt = (PreparedStatement)super.checkOut(sqlString);
      stmt = new SPPreparedStatement(realStmt);

      // ... hook events ...
      stmt.addStatementEventListener(this);

      // ... and return.
      return stmt;      
   }

   /**
    * Check a prepared statement back into the pool.
    *
    * @param pooledStatement The pooled statement.
    * @exception XMLMiddlewareException Thrown if the statement does not belong to this pool.
    */
   public void checkIn(Object pooledStatement)
      throws XMLMiddlewareException
   {
      PreparedStatement   realStmt;
      SPPreparedStatement stmt;

      // It has to be something we created
      if(!(pooledStatement instanceof SPPreparedStatement))
         throw new XMLMiddlewareException("StatementPool requires a SPPreparedStatement object.");

      // Get actual object
      stmt = (SPPreparedStatement)pooledStatement;

      // Check in the underlying object
      realStmt = stmt.getUnderlyingStatement();
      stmt.invalidate();
      super.checkIn(realStmt);

      // Unhook events
      stmt.removeStatementEventListener(this);
   }

   //**************************************************************************
   // SPStatementEventListener methods
   //**************************************************************************

   /**
    * When a SPPreparedStatement is closed, checks the underlying statement back
    * in to the pool.
    *
    * @param SPStatementEvent object containing the statement.
    */
   public void statementClosed(SPStatementEvent event)
   {
      try
      {
         // Just check in statement
         SPPreparedStatement stmt = (SPPreparedStatement)event.getSource();
         checkIn(stmt);
      }
      catch(Exception e)
      { 
         // This code should never be reached. The first and second statements throw
         // exceptions if the object is not a SPPreparedStatement object, but StatementPool
         // only registers for events on SPPreparedStatement objects. The second statement
         // can also throw an exception if the object is not checked out, but
         // StatementPool only registers for events on checked out objects.
      }
   }

   /**
    * When an error occurs on a SPPreparedStatement, removes it from the pool.
    *
    * <p>Removing a statement from the pool in the case of an error is
    * admittedly drastic, but there is no way to tell if the error was
    * fatal to the statement or not.</p>
    *
    * @param SPStatementEvent object containing the statement.
    */
   public void statementErrorOccurred(SPStatementEvent event)
   {
      try
      {
         SPPreparedStatement stmt = (SPPreparedStatement)event.getSource();
         remove(stmt);
      }
      catch(Exception e)
      { 
         // This code should never be reached. The first and second statements throw
         // exceptions if the object is not a SPPreparedStatement, but StatementPool
         // only registers for events on SPPreparedStatement objects. The second statement
         // also throws an exception if the object is not checked out, but
         // StatementPool only registers for events on checked out objects.
      }
   }

   //**************************************************************************
   // Protected methods
   //**************************************************************************

   /**
    * Creates a new prepared statement.
    *
    * <p>Overrides Pool.createObject.</p>
    *
    * @param id SQL string for the prepared statement.
    * @return The PreparedStatement.
    * @exception XMLMiddlewareException Thrown if statement cannot be created or id is invalid.
    */
   protected Object createObject(Object id)
      throws XMLMiddlewareException
   {
      if(!(id instanceof String))
         throw new XMLMiddlewareException("StatementPool.createObject needs a String id.");

      try
      {   
         // Create the statement
         return m_connection.prepareStatement((String)id);
      }
      catch(SQLException e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Closes a prepared statement.
    *
    * <p>Overrides Pool.closeObject.</p>
    *
    * @param obj The prepared statement to close.
    * @exception XMLMiddlewareException Thrown if object is not valid or an error occurs
    *            while closing it.
    */
   protected void closeObject(Object object)
      throws XMLMiddlewareException
   {
      if(!(object instanceof PreparedStatement))
         throw new XMLMiddlewareException("StatementPool.closeObject needs PreparedStatement.");

      try
      {
         // Get actual PreparedStatement
         PreparedStatement stmt = (PreparedStatement)object;

         // Close actual statement.
         stmt.close();
      }
      catch(SQLException e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Remove all objects from the pool.
    *
    * <p>This method closes all objects in the pool, regardless of whether they
    * are checked in or out. It ignores any errors encountered while closing
    * objects.</p>
    */
   protected void clear()
   {
      // We need this method only to get around the fact that Pool.clear()
      // is protected and in a different package.
      super.clear();
   }

   /**
    * Remove the underlying statement from the pool, such as in case of an error.
    *
    * <p>This method closes the object. It ignores any errors encountered while
    * closing the object.</p>
    *
    * @param object The object.
    * @exception XMLMiddlewareException Thrown if the object is not checked out.
    */
   protected void remove(Object object)
      throws XMLMiddlewareException
   {
      PreparedStatement   realStmt;
      SPPreparedStatement stmt;

      if(!(object instanceof SPPreparedStatement))
         throw new XMLMiddlewareException("StatementPool requires a SPPreparedStatement object.");

      stmt = (SPPreparedStatement)object;

      // Get the underlying prepared statement, then invalidate the state of
      // the SPPreparedStatement so it can't be used any more.

      realStmt = stmt.getUnderlyingStatement();
      stmt.invalidate();

      // Remove the statement from the statement pool.

      super.remove(realStmt);

      // Remove this statement pool as a listener on the SPPreparedStatement object.

      stmt.removeStatementEventListener(this);
   }

   //**************************************************************************
   // Class variables
   //**************************************************************************

   protected Connection m_connection;
}
