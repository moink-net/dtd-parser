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
import javax.sql.*;
import java.util.*;
import java.io.*;

import org.xmlmiddleware.utils.*;

/**
 * Implements a JDBC 2.0 DataSource for a JDBC 1.0 driver.
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public class JDBC1DataSource 
   extends ConnectionPool
   implements DataSource
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new JDBC1DataSource.
    */
   public JDBC1DataSource(String driver, String url)
   {
      init(driver, url);
   }

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void init(String driver, String url)
   {
      if (driver == null)
         throw new IllegalArgumentException("Driver class must not be null.");
      if (url == null)
         throw new IllegalArgumentException("URL must not be null.");

      try
      {
         Class.forName(driver);
      }
      catch(ClassNotFoundException e)
      {
         throw new IllegalArgumentException("ClassNotFoundException: " + e.getMessage());
      }

      m_url = url;
   }

   //**************************************************************************
   // Protected methods
   //**************************************************************************

    /**
     * Create a new Connection.
     *
     * @param id This must be a ConnectionID object.
     *
     * @return The Connection.
     * @exception PoolException Thrown if the factory cannot return a connection, such as
     *               when the maximum number of connections has been exceeded.
     */
   protected Object createObject(Object id)
      throws PoolException
   {
      if (!(id instanceof ConnectionID))
         throw new PoolException("JDBC1DataSource: Object ID must be a ConnectionID.");

      try
      {
         ConnectionID connId = (ConnectionID)id;
         if(connId.username == null || connId.password == null)
            return DriverManager.getConnection(m_url);
         else
            return DriverManager.getConnection(m_url, connId.username, connId.password);
      }
      catch(SQLException e)
      {
         throw new PoolException(e);
      }
   }

   /**
    * Close a Connection created by the factory.
    *
    * @param object The connection object to close.
    *
    * @exception PoolException Thrown if the factory does not recognize the Connection or an
    *               error occurs while closing it.
    */
   protected void closeObject(Object object)
      throws PoolException
   {
      if(!(object instanceof Connection))
         throw new PoolException("JDBC1DataSource: Invalid connection object");

      try
      {
         ((Connection)object).close();
      }
      catch(SQLException e)
      {
         throw new PoolException(e);
      }
   }


   //*************************************************************************
   // DataSource Methods
   //*************************************************************************

   public Connection getConnection()
      throws SQLException
   {
      try
      {
         return (Connection)checkOut(new ConnectionID());
      }
      catch(PoolException e)
      {
         if(e.getException() instanceof SQLException)
            throw (SQLException)e.getException();

         throw new SQLException(e.getMessage());
      }
   }

   public Connection getConnection(String username, String password)
      throws SQLException
   {
      try
      {
         return (Connection)checkOut(new ConnectionID(username, password));
      }
      catch(PoolException e)
      {
         if(e.getException() instanceof SQLException)
            throw (SQLException)e.getException();

         throw new SQLException(e.getMessage());
      }
   }

   /**
    * Always returns null. See the code if you are using JDBC 2.0.
    */
   public PrintWriter getLogWriter()
      throws SQLException
   {
      // Uncomment the following statement if you are using JDBC 2.0.
      //return DriverManager.getLogWriter();

      // Comment out the following statement if you are using JDBC 2.0.
      return null;
   }

   /**
    * Does nothing. See the code if you are using JDBC 2.0.
    */
   public void setLogWriter(PrintWriter out)
      throws SQLException
   {
      // Uncomment the following statement if you are using JDBC 2.0.
      //DriverManager.setLogStream(out);
   }

   public void setLoginTimeout(int seconds)
      throws SQLException
   {
      DriverManager.setLoginTimeout(seconds);
   }

   public int getLoginTimeout()
      throws SQLException
   {
      return DriverManager.getLoginTimeout();
   }

   //**************************************************************************
   // Class variables
   //**************************************************************************

   protected String m_url;
}