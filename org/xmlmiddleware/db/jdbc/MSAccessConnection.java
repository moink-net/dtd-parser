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

package org.xmlmiddleware.db.jdbc;

import java.sql.*;
import java.util.*;

/**
 * Wrapper around a JDBC-ODBC Bridge Connection.
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class MSAccessConnection implements Connection
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   Connection conn = null;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new MSAccessConnection.
    *
    * @param conn A JDBC-ODBC Bridge Connection object.
    * @return The MSAccessConnection.
    */
   public MSAccessConnection(Connection conn)
   {
      this.conn = conn;
   }

   public void finalize() throws Throwable
   {
      if (conn != null) conn.close();
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   public boolean isClosed() throws SQLException
   {
      return conn.isClosed();
   }

   public void close() throws SQLException
   {
      conn.close();
      conn = null;
   }

   /**
    * Get a DatabaseMetaData object.
    *
    * <p>The returned object wraps a JDBC-ODBC Bridge DatabaseMetaData object.</p>
    *
    * @return The DatabaseMetaData object.
    */
   public DatabaseMetaData getMetaData() throws SQLException
   {
      DatabaseMetaData meta = conn.getMetaData();
      return new MSAccessDBMetaData(conn, meta);
   }

   public boolean getAutoCommit() throws SQLException
   {
      return conn.getAutoCommit();
   }

   public void setAutoCommit(boolean autoCommit) throws SQLException
   {
      conn.setAutoCommit(autoCommit);
   }

   public int getTransactionIsolation() throws SQLException
   {
      return conn.getTransactionIsolation();
   }

   public void setTransactionIsolation(int level) throws SQLException
   {
      conn.setTransactionIsolation(level);
   }

   public void commit() throws SQLException
   {
      conn.commit();
   }

   public void rollback() throws SQLException
   {
      conn.rollback();
   }

   public String getCatalog() throws SQLException
   {
      return conn.getCatalog();
   }

   public void setCatalog(String catalog) throws SQLException
   {
      conn.setCatalog(catalog);
   }

   public boolean isReadOnly() throws SQLException
   {
      return conn.isReadOnly();
   }

   public void setReadOnly(boolean readOnly) throws SQLException
   {
      conn.setReadOnly(readOnly);
   }

   public void clearWarnings() throws SQLException
   {
      conn.clearWarnings();
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return conn.getWarnings();
   }

   public String nativeSQL(String sql) throws SQLException
   {
      return conn.nativeSQL(sql);
   }

   public Statement createStatement() throws SQLException
   {
      return conn.createStatement();
   }

   public CallableStatement prepareCall(String sql) throws SQLException
   {
      return conn.prepareCall(sql);
   }

   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      return conn.prepareStatement(sql);
   }
}
