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
import java.io.*;
import javax.sql.*;
import java.util.*;

import org.xmlmiddleware.utils.*;

/**
 * Implements a JDBC 2.0 DataSource for an array of connections.
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public class ArrayDataSource 
   extends ConnectionPool
   implements DataSource
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new ArrayDataSource. (Convenience method for single connection.)
    *
    * @param conn The connection.
    */
   public ArrayDataSource(Connection conn)
   {
      Connection[] conns = { conn };
      init(conns);
   }

   /**
    * Create a new ArrayDataSource.
    *
    * @params conns An array of connections.
    */
   public ArrayDataSource(Connection[] conns)
   {
      init(conns);
   }

   private void init(Connection[] conns)
   {
      // Make a new stack ...
      m_connections = new Stack();

      // ... and push all connections passed to it
      for(int i = 0; i < conns.length; i++)
         m_connections.push(conns[i]);
   }

   //**************************************************************************
   // Protected methods
   //**************************************************************************

   /**
    * Create a new Connection.
    *
    * <p>Implements Pool.createObject(Object).</p>
    *
    * @param id Ignored.
    *
    * @return The Connection.
    * @exception PoolException Thrown if the factory cannot return a connection, such as
    *               when the maximum number of connections has been exceeded.
    */
   protected Object createObject(Object id)
      throws PoolException
   {
      // Check if any left
      if(m_connections.empty())
         throw new PoolException("ArrayDataSource: No more connections available.");

      return m_connections.pop();
   }

   /**
    * Close a Connection created by the factory.
    *
    * <p>Implements Pool.closeObject(Object).</p>
    *
    * @param object Connection to close.
    *
    * @exception PoolException Thrown if the factory does not recognize the Connection or an
    *               error occurs while closing it.
    */
   protected void closeObject(Object object)
      throws PoolException
   {
      // Make sure it's a connection
      if(!(object instanceof Connection))
         throw new PoolException("ArrayDataSource: Invalid connection passed to ArrayDataSource");

      // Put it back on stack
      m_connections.push(object);
   }

   //*************************************************************************
   // DataSource Methods
   //*************************************************************************

   /**
    * Get the next available connection from the array.
    */
   public Connection getConnection()
      throws SQLException
   {
      try
      {
         return (Connection)checkOut(ARRAYCONNECTION);
      }
      catch(PoolException e)
      {
         if(e.getException() instanceof SQLException)
            throw (SQLException)e.getException();

         throw new SQLException(e.getMessage());
      }
   }

   /**
    * Get the next available connection from the array.
    *
    * <p>The user name and password are ignored.</p>
    */
   public Connection getConnection(String username, String password)
      throws java.sql.SQLException
   {
      return getConnection();
   }

   /**
    * Always returns null.
    */
   public PrintWriter getLogWriter()
      throws SQLException
   {
      return null;
   }

   /**
    * Log writer parameter ignored.
    */
   public void setLogWriter(PrintWriter out)
      throws SQLException
   {

   }

   /**
    * Login timeout parameter ignored.
    */
   public void setLoginTimeout(int seconds)
      throws SQLException
   {
      
   }

   /**
    * Always returns -1.
    */
   public int getLoginTimeout()
      throws SQLException
   {
      return -1;
   }

   //*************************************************************************
   // Class variables
   //*************************************************************************

   // Stack of connections passed in constructor
   protected Stack m_connections;

   // Dummy string to use as ID
   private static String ARRAYCONNECTION="ArrayConnection";
}

