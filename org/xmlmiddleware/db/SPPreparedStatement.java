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

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Wraps a pooled statement.
 *
 * <p><b>NOTE:</b> To compile against JDK 1.2 or greater, uncomment the methods
 * used by the corresponding version of PreparedStatement and Statement.</p>
 *
 * @author Sean Walter, 2001
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class SPPreparedStatement 
   implements PreparedStatement, Statement, SPStatementEventSource
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
     * Create a new SPPreparedStatement.
     *
     * @param statement The underlying prepared statement
     */
   protected SPPreparedStatement(PreparedStatement statement)
   {
      m_statement = statement;
      m_listeners = new Hashtable();
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
   // PreparedStatement methods
   //**************************************************************************

   public ResultSet executeQuery()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.executeQuery();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public int executeUpdate()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.executeUpdate();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setNull(int parameterIndex, int sqlType)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setNull(parameterIndex, sqlType);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setBoolean(int parameterIndex, boolean x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setBoolean(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }
   public void setByte(int parameterIndex, byte x) 
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setByte(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setShort(int parameterIndex, short x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setShort(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setInt(int parameterIndex, int x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setInt(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setLong(int parameterIndex, long x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setLong(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setFloat(int parameterIndex, float x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setFloat(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setDouble(int parameterIndex, double x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setDouble(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setBigDecimal(int parameterIndex, BigDecimal x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setBigDecimal(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setString(int parameterIndex, String x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setString(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setBytes(int parameterIndex, byte[] x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setBytes(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setDate(int parameterIndex, java.sql.Date x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setDate(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setTime(int parameterIndex, Time x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setTime(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setTimestamp(int parameterIndex, Timestamp x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setTimestamp(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setAsciiStream(int parameterIndex, InputStream x, int length)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setAsciiStream(parameterIndex, x, length);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setUnicodeStream(int parameterIndex, InputStream x, int length)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setUnicodeStream(parameterIndex, x, length);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setBinaryStream(int parameterIndex, InputStream x, int length)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setBinaryStream(parameterIndex, x, length);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void clearParameters()
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.clearParameters();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setObject(parameterIndex, x, targetSqlType, scale);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setObject(parameterIndex, x, targetSqlType);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setObject(int parameterIndex, Object x)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setObject(parameterIndex, x);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public boolean execute()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.execute();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   //**************************************************************************
   // JDBC 2.0 PreparedStatement methods
   //**************************************************************************

   // Uncomment these methods to compile with JDK 1.2 or greater

/*
   public void addBatch() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setRef(int i, Ref x) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setBlob(int i, Blob x) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setClob(int i, Clob x) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setArray(int i, Array x) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public ResultSetMetaData getMetaData() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }
*/

   // Uncomment these methods to compile with JDK 1.4 or greater

/*

   public void setURL(int parameterIndex, java.net.URL x)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public ParameterMetaData getParameterMetaData()
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public boolean getMoreResults(int current)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
 
   public ResultSet getGeneratedKeys()
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
   
   public int executeUpdate(String sql, int autoGeneratedKeys)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
 
   public int executeUpdate(String sql, int[] columnIndexes)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
 
   public int executeUpdate(String sql, String[] columnNames)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
   
   public boolean execute(String sql, int autoGeneratedKeys)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
   
   public boolean execute(String sql, int[] columnIndexes)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
   
   public boolean execute(String sql, String[] columnNames)
      throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not 
implemented.", "HY000"); }
   
   public int getResultSetHoldability()
      throws SQLException
      { throw new SQLException("[XML-DBMS][CPConnection] Not 
implemented.", "HY000"); }


*/

   //**************************************************************************
   // Statement methods
   //**************************************************************************

   public ResultSet executeQuery(String sql)
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.executeQuery(sql);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public int executeUpdate(String sql)
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.executeUpdate(sql);
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

   public int getMaxFieldSize()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.getMaxFieldSize();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setMaxFieldSize(int max)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setMaxFieldSize(max);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public int getMaxRows()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.getMaxRows();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setMaxRows(int max)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setMaxRows(max);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setEscapeProcessing(boolean enable)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setEscapeProcessing(enable);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public int getQueryTimeout()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.getQueryTimeout();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setQueryTimeout(int seconds)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setQueryTimeout(seconds);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void cancel()
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.cancel();
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
         return m_statement.getWarnings();
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
         m_statement.clearWarnings();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public void setCursorName(String name)
      throws SQLException
   {
      checkState();
      try
      {
         m_statement.setCursorName(name);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public boolean execute(String sql)
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.execute(sql);
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public ResultSet getResultSet()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.getResultSet();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public int getUpdateCount()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.getUpdateCount();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   public boolean getMoreResults()
      throws SQLException
   {
      checkState();
      try
      {
         return m_statement.getMoreResults();
      }
      catch(SQLException e)
      {
         fireErrorEvent(e);
         throw e;
      }
   }

   //**************************************************************************
   // JDBC 2.0 Statement methods
   //**************************************************************************

   // Comment these methods to compile with JDBC 1.0
/**/
   public void setFetchDirection(int direction) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public int getFetchDirection() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void setFetchSize(int rows) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public int getFetchSize() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public int getResultSetConcurrency() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public int getResultSetType() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void addBatch(String sql) throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public void clearBatch() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public int[] executeBatch() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }

   public Connection getConnection() throws SQLException
      { throw new SQLException("[XML-DBMS][SPreparedStatement] Not implemented.", "HY000"); }
/**/

   // **************************************************************************
   // SPStatementEventSource interface
   // **************************************************************************

   public void addStatementEventListener(SPStatementEventListener listener)
   {
      m_listeners.put(listener, obj);
   }   

   public void removeStatementEventListener(SPStatementEventListener listener)
   {
      m_listeners.remove(listener);
   }

   //**************************************************************************
   // SPPreparedStatement methods
   //**************************************************************************

   public PreparedStatement getUnderlyingStatement()
   {
      return m_statement;
   }

   protected void invalidate()
   {
      m_statement = null;
   }

   //**************************************************************************
   // Helper methods
   //**************************************************************************
   
   private void fireCloseEvent()
   {
      SPStatementEvent event = new SPStatementEvent(this);

      // Run through listeners and send event
      for(Enumeration e = m_listeners.keys(); e.hasMoreElements(); )
      {
         SPStatementEventListener listener = (SPStatementEventListener)e.nextElement();
         listener.statementClosed(event);
      }
   }

   private void fireErrorEvent(SQLException s)
   {
      SPStatementEvent event = new SPStatementEvent(this, s);

      // Run through listeners and send event
      for(Enumeration e = m_listeners.keys(); e.hasMoreElements(); )
      {
         SPStatementEventListener listener = (SPStatementEventListener)e.nextElement();
         listener.statementErrorOccurred(event);
      }
   }

   private void checkState()
      throws SQLException
   {
      if (m_statement == null)
         throw new SQLException("[XML-DBMS][SPPreparedStatement] Statement closed.", "HY000");
   }

   //**************************************************************************
   // Class variables
   //**************************************************************************

   protected Hashtable m_listeners;
   protected PreparedStatement m_statement;
   private final static Object obj = new Object();
}
