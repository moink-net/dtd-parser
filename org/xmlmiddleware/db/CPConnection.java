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
 * Wraps a pooled connection and its associated statement pool.
 *
 * <p><b>NOTE:</b> To compile against JDK 1.2 or greater, uncomment the methods
 * used by the corresponding version of Connection.</p>
 *
 * @author Sean Walter, 2001
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class CPConnection
   implements Connection, CPConnectionEventSource
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new CPConnection.
    *
    * @param conn The actual connection to the database.
    * @param statements A StatementPool containing statements for the connection.
    *                   May be null.
    * @exception SQLException Thrown if there are problems getting database metadata.
    */
   protected CPConnection(Connection conn, StatementPool statements)
      throws SQLException
   {
      m_connection = conn;
      m_statements = (statements == null) ? new StatementPool(conn) : statements;
      m_listeners = new Hashtable();

      DatabaseMetaData meta = conn.getMetaData();
      m_statementsAcrossCommit = meta.supportsOpenStatementsAcrossCommit();
      m_statementsAcrossRollback = meta.supportsOpenCursorsAcrossRollback();
   }

   protected void finalize()
   {
      try
      {
         close();
      }
      catch(Exception e)
         { }
   }
         

   //**************************************************************************
   // Connection methods
   //**************************************************************************

   public Statement createStatement()
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.createStatement();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public PreparedStatement prepareStatement(String sql)
      throws SQLException
   {
      checkState();
      try
      {
         return (PreparedStatement)m_statements.checkOut(sql);
      }
      catch(XMLMiddlewareException e)
      {
         if(e.getException() instanceof SQLException)
            throw (SQLException)e.getException();

         throw new SQLException(e.getMessage());
      }
   }

   public CallableStatement prepareCall(String sql)
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.prepareCall(sql);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public String nativeSQL(String sql)
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.nativeSQL(sql);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setAutoCommit(boolean autoCommit)
      throws SQLException
   {
      checkState();
      try
      {
         m_connection.setAutoCommit(autoCommit);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public boolean getAutoCommit()
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.getAutoCommit();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void commit()
      throws SQLException
   {
      checkState();
      try
      {
         if(!m_statementsAcrossCommit)
            m_statements.clear();

         m_connection.commit();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void rollback()
      throws SQLException
   {
      checkState();
      try
      {
         if(!m_statementsAcrossRollback)
            m_statements.clear();

         m_connection.rollback();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void close()
      throws SQLException
   {
      checkState();
      fireCloseEvent();
   }

   public boolean isClosed()
      throws SQLException
   {
      try
      {
         if(m_connection == null)
            return true;

         return m_connection.isClosed();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public DatabaseMetaData getMetaData()
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.getMetaData();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setReadOnly(boolean readOnly)
      throws SQLException
   {
      checkState();
      try
      {
         m_connection.setReadOnly(readOnly);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public boolean isReadOnly()
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.isReadOnly();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setCatalog(String catalog)
      throws SQLException
   {
      checkState();
      try
      {
         m_connection.setCatalog(catalog);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public String getCatalog()
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.getCatalog();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setTransactionIsolation(int level)
      throws SQLException
   {
      checkState();
      try
      {
         m_connection.setTransactionIsolation(level);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public int getTransactionIsolation()
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.getTransactionIsolation();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public SQLWarning getWarnings()
      throws SQLException
   {
      checkState();
      try
      {
         return m_connection.getWarnings();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void clearWarnings()
      throws SQLException
   {
      checkState();
      try
      {
         m_connection.clearWarnings();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   //**************************************************************************
   // JDBC 2.0 Connection methods
   //**************************************************************************

   // Uncomment these methods to compile with JDK 1.2 or greater

/*
   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public Map getTypeMap() throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public void setTypeMap(Map map) throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }
*/

   // Uncomment these methods to compile with JDK 1.4 or greater

/*
   public void setHoldability(int holdability)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public int getHoldability()
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public Savepoint setSavepoint()
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public void rollback(Savepoint savepoint)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public void releaseSavepoint(Savepoint savepoint)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public PreparedStatement prepareStatement(String sql, int flag)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }

   public PreparedStatement prepareStatement(String sql, String[] columnNames)
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not implemented.", "HY000"); }
*/

   //**************************************************************************
   // CPConnectionEventSource methods
   //**************************************************************************

   public void addConnectionEventListener(CPConnectionEventListener listener)
   {
      // We use a hashtable to avoid duplicate listeners, but only care
      // about the key. The value is always the same object.

      m_listeners.put(listener, obj);
   }   

   public void removeConnectionEventListener(CPConnectionEventListener listener)
   {
      m_listeners.remove(listener);
   }

   //**************************************************************************
   // Protected methods
   //**************************************************************************

   protected Connection getConnection()
   {
      return m_connection;
   }

   protected StatementPool getStatementPool()
   {
      return m_statements;
   }

   protected void invalidate()
   {
      m_connection = null;
      m_statements = null;
   }

   //**************************************************************************
   // Helper methods
   //**************************************************************************

   private void fireCloseEvent()
   {
      CPConnectionEvent event = new CPConnectionEvent(this);

      // Run through listeners and send event
      for(Enumeration e = m_listeners.keys(); e.hasMoreElements(); )
      {
         CPConnectionEventListener listener = (CPConnectionEventListener)e.nextElement();
         listener.connectionClosed(event);
      }
   }

   private void fireErrorEvent(SQLException s)
   {
      CPConnectionEvent event = new CPConnectionEvent(this, s);

      // Run through listeners and send event
      for(Enumeration e = m_listeners.keys(); e.hasMoreElements(); )
      {
         CPConnectionEventListener listener = (CPConnectionEventListener)e.nextElement();
         listener.connectionErrorOccurred(event);
      }
   }

   private void checkState()
      throws SQLException
   {
      if (m_connection == null)
         throw new SQLException("[XML-DBMS][CPConnection] Connection closed.", "08003");
   }

   //**************************************************************************
   // Class variables
   //**************************************************************************

   // Connection we're wrapping
   protected Connection m_connection;
   protected StatementPool m_statements;

   // How to treat prepared statements on commit/rollback.
   protected boolean m_statementsAcrossCommit;
   protected boolean m_statementsAcrossRollback;

   // List of listeners to send event to
   protected Hashtable m_listeners;

   // Dummy object for hashtable of statement pools
   private final static Object obj = new Object();
}
