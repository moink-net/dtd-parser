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
import javax.sql.*;
import javax.naming.*;
import java.io.*;

import org.xmlmiddleware.utils.*;

/**
 * Implements a JDBC 2.0 DataSource that pools connections and statements
 * for a DataSource that doesn't.
 *
 * <p>If you have a JDBC 2.0 DataSource that pools connections but not statements,
 * use StatementPoolDataSource. If you have a JDBC 2.0 DataSource that pools
 * both connections and statements, use it directly.</p>
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public class JDBC2DataSource 
   extends ConnectionPool
   implements DataSource
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new JDBC2DataSource.
    *
    * @param ds A JDBC 2.0 DataSource that does not pool statements or connections.
    */
   public JDBC2DataSource(DataSource ds)
      throws SQLException
   {
      m_dataSource = ds;
   }
   
   public JDBC2DataSource(Properties props)
      throws SQLException
   {
      String initContext = props.getProperty(DBProps.DBINITIALCONTEXT);
      String dataSource = props.getProperty(DBProps.DATASOURCE);

      if(initContext == null)
         throw new SQLException("JDBC2DataSource: DBInitialContext property not set");
        
      if(dataSource == null)
         throw new SQLException("JDBC2DataSource: DataSource property not set");

      try
      {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, initContext.trim());
         Context ctx = new InitialContext(env);
         m_dataSource = (DataSource)ctx.lookup(dataSource);
      }
      catch(NamingException e)
      {
         throw new SQLException(e.getMessage());
      }
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
         throw new PoolException("JDBC2DataSource: Object ID must be a ConnectionID.");
      try
      {
         ConnectionID connId = (ConnectionID)id;
         if(connId.username == null || connId.password == null)
            return m_dataSource.getConnection();
         else
            return m_dataSource.getConnection(connId.username, connId.password);
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
         throw new PoolException("Invalid connection object");

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
      throws java.sql.SQLException
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

   public PrintWriter getLogWriter()
      throws SQLException
   {
      return m_dataSource.getLogWriter();
   }

   public void setLogWriter(PrintWriter out)
      throws SQLException
   {
      m_dataSource.setLogWriter(out);
   }

   public void setLoginTimeout(int seconds)
      throws SQLException
   {
      m_dataSource.setLoginTimeout(seconds);
   }

   public int getLoginTimeout()
      throws SQLException
   {
      return m_dataSource.getLoginTimeout();
   }

   //*************************************************************************
   // Class variables
   //*************************************************************************

   protected DataSource m_dataSource;
}