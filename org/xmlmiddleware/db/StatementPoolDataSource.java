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

import javax.sql.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

/**
 * Implements a DataSource with statement pooling over a DataSource
 * that does connection pooling.
 *
 * <p>If you have a JDBC 2.0 DataSource that does not pool connections or
 * statements, use JDBC2DataSource. If you have a JDBC 2.0 DataSource that
 * pools both connections and statements, use it directly.</p>
 *
 * <p><b>WARNING:</b> Unlike the other implementations of DataSource in this
 * package, this data source does not retain statement pools when the Connection
 * is closed. This is because closing the Connection returns it to the pool
 * implemented by the underlying DataSource, which is outside of our control.</p>
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public class StatementPoolDataSource 
   implements DataSource, CPConnectionEventListener
{
   //**************************************************************************
   // Constructor
   //**************************************************************************

   /**
    * Create a new StatementPoolDataSource object.
    *
    * @param ds A DataSource that pools connections but not statements.
    */
   public StatementPoolDataSource(DataSource ds)
   {
      m_dataSource = ds;
   }

   //*************************************************************************
   // DataSource Methods
   //*************************************************************************

   public Connection getConnection()
      throws SQLException
   {
      CPConnection conn = new CPConnection(m_dataSource.getConnection(), null);
      conn.addConnectionEventListener(this);
      return conn;
   }

   public Connection getConnection(String username, String password)
      throws java.sql.SQLException
   {
      CPConnection conn = new CPConnection(m_dataSource.getConnection(username, password), null);
      conn.addConnectionEventListener(this);
      return conn;
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
   // CPConnectionEventListener Methods
   //*************************************************************************

   public void connectionClosed(CPConnectionEvent event)
   {
      Connection   realConn;
      CPConnection conn;

      conn = (CPConnection)event.getSource();
      conn.removeConnectionEventListener(this);

      realConn = conn.getConnection();
      conn.invalidate();

      try
      {
         realConn.close();
      }
      catch(SQLException e)
      {
         // Ignore any exceptions.
      }
   }

   public void connectionErrorOccurred(CPConnectionEvent event)
   {
      // If there is an error on the connection, just close it and invalidate
      // the CPConnection object, as is done when the CPConnection object is closed.

      connectionClosed(event);
   }

   //*************************************************************************
   // Class variables
   //*************************************************************************

   protected DataSource m_dataSource;
}