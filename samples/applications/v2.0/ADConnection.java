import java.sql.*;
import java.util.*;
import ADDatabaseMetaData;

public class ADConnection implements Connection
{
   Connection conn = null;

   public ADConnection(Connection conn)
   {
      this.conn = conn;
   }

/*
   public static ADConnection create(Driver driver, String url, Properties info) throws SQLException
   {
      return new ADConnection(driver.connect(url, info));
   }
*/

   public void finalize() throws Throwable
   {
      if (conn != null) conn.close();
   }

   public boolean isClosed() throws SQLException
   {
      return conn.isClosed();
   }

   public void close() throws SQLException
   {
      conn.close();
      conn = null;
   }

   public DatabaseMetaData getMetaData() throws SQLException
   {
      DatabaseMetaData meta = conn.getMetaData();
      return new ADDatabaseMetaData(conn, meta);
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
