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
import java.util.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.utils.*;

/**
 * Implements a connection pool.
 *
 * <p>This class is abstract. Classes that need connection pooling
 * extend this class and implement the createObject and removeObject
 * methods from Pool.</p>
 *
 * @author Sean Walter, 2001
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public abstract class ConnectionPool 
   extends Pool
   implements CPConnectionEventListener
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new ConnectionPool object.
    */
   public ConnectionPool()
   {
      super();
      m_statementPools = new Hashtable();
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Check a connection with the specified ID out of the pool.
    *
    * <p>Overrides Pool.checkOut(Object).</p>
    *
    * @return The object.
    * @exception PoolException Thrown if the pool does not recognize the ID or cannot
    *                          return the specified object for any reason, such as lack
    *                          of resources.
    */
   public Object checkOut(Object id)
      throws PoolException
   {
      Connection    realConn;
      CPConnection  conn;
      StatementPool statements;

      // Get the underlying connection and its associated statement pool, if any.
      realConn = (Connection)super.checkOut(id);
      statements = (StatementPool)m_statementPools.get(realConn);

      // Create a new CPConnection to wrap the connection and statement pool.
      try
      {
         conn = new CPConnection(realConn, statements);
      }
      catch (SQLException e)
      {
         throw new PoolException(e);
      }

      // Add this connection pool as a listener for connection events.
      conn.addConnectionEventListener(this);

      // Return the CPConnection.
      return conn;
   }

   /**
    * Check a connection back into the pool.
    *
    * <p>Overrides Pool.checkIn(Object).</p>
    *
    * @param object The object.
    * @exception PoolException Thrown if the object does not belong to this pool.
    */
   public void checkIn(Object object)
      throws PoolException
   {
      Connection    realConn;
      CPConnection  conn;
      StatementPool statements;

      if(!(object instanceof CPConnection))
         throw new PoolException("ConnectionPool requires a CPConnection object.");

      conn = (CPConnection)object;

      // Get the underlying connection and its associated statement pool, then
      // invalidate the state of the CPConnection so it can't be used any more.

      realConn = conn.getConnection();
      statements = conn.getStatementPool();
      conn.invalidate();

      // Check the connection back into the connection pool and store the
      // statement pool for later use.

      super.checkIn(realConn);
      m_statementPools.put(realConn, statements);

      // Remove this connection pool as a listener on the CPConnection object.

      conn.removeConnectionEventListener(this);
   }

   //**************************************************************************
   // CPConnectionEventListener methods
   //**************************************************************************

   /**
    * When a CPConnection is closed, checks the underlying connection back
    * in to the pool.
    *
    * @param CPConnectionEvent object containing the connection.
    */
   public void connectionClosed(CPConnectionEvent event)
   {
      try
      {
         CPConnection conn = (CPConnection)event.getSource();
         checkIn(conn);
      }
      catch(Exception e)
      {
         // This code should never be reached. The first and second statements throw
         // exceptions if the object is not a CPConnection object, but ConnectionPool
         // only registers for events on CPConnection objects. The second statement
         // can also throw an exception if the object is not checked out, but
         // ConnectionPool only registers for events on checked out objects.
      }
   }

   /**
    * When an error occurs on a CPConnection, removes it from the pool.
    *
    * <p>Removing a connection from the pool in the case of an error is
    * admittedly drastic, but there is no way to tell if the error was
    * fatal to the connection or not.</p>
    *
    * @param CPConnectionEvent object containing the connection.
    */
   public void connectionErrorOccurred(CPConnectionEvent event)
   {
      try
      {
         CPConnection conn = (CPConnection)event.getSource();
         remove(conn);
      }
      catch(Exception e)
      {
         // This code should never be reached. The first and second statements throw
         // exceptions if the object is not a CPConnection object, but ConnectionPool
         // only registers for events on CPConnection objects. The second statement
         // also throws an exception if the object is not checked out, but
         // ConnectionPool only registers for events on checked out objects.
      }
   }

   //**************************************************************************
   // Protected methods
   //**************************************************************************

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
    * Remove the underlying connection from the pool, such as in case of an error.
    *
    * <p>This method closes the object. It ignores any errors encountered while
    * closing the object.</p>
    *
    * @param object The object.
    * @exception PoolException Thrown if the object is not checked out.
    */
   protected void remove(Object object)
      throws PoolException
   {
      Connection    realConn;
      CPConnection  conn;
      StatementPool statements;

      if(!(object instanceof CPConnection))
         throw new PoolException("Pool requires a CPConnection object.");

      conn = (CPConnection)object;

      // Get the underlying connection and its associated statement pool, then
      // invalidate the state of the CPConnection so it can't be used any more.

      realConn = conn.getConnection();
      statements = conn.getStatementPool();
      conn.invalidate();

      // Remove the connection from the connection pool and the statement pool
      // from the hashtable of statement pools.

      super.remove(realConn);
      m_statementPools.remove(realConn);

      // Remove this connection pool as a listener on the CPConnection object.

      conn.removeConnectionEventListener(this);
   }

   //**************************************************************************
   // Class members
   //**************************************************************************

   Hashtable m_statementPools;
}