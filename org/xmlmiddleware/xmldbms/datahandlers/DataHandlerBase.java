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

package org.xmlmiddleware.xmldbms.datahandlers;

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * (Optional) base class for classes that implement the DataHandler interface.
 *
 * <p>Child classes must implement the insert method.</p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public abstract class DataHandlerBase
   implements DataHandler
{
   // ************************************************************************
   // Variables
   // ************************************************************************

   private Connection   m_connection = null;
   private DMLGenerator m_dml = null;
   private SQLStrings   m_strings = null;
   private boolean      m_dirtyConnection = false;
   private int          m_commitMode = DataHandler.COMMIT_AFTERSTATEMENT;
   private Hashtable    m_refreshCols = null; // Indexed by table.

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static final Object OBJECT = new Object();

   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
    * Creates a DataHandlerBase
    */
   public DataHandlerBase()
   {
   }

   // ************************************************************************
   // Public methods -- accessors
   // ************************************************************************

   /**
    * Get the connection used by the DataHandler.
    *
    * @return The Connection. Null if initialize() has not been called.
    */
   public final Connection getConnection()
   {
      return m_connection;
   }

   /**
    * Get the DMLGenerator used by the DataHandler.
    *
    * @return The DMLGenerator. Null if initialize() has not been called.
    */
   public final DMLGenerator getDMLGenerator()
   {
      return m_dml;
   }

   /**
    * Get the SQLStrings object used by the DataHandler.
    *
    * @return The SQLStrings. Null if initialize() has not been called.
    */
   public final SQLStrings getSQLStrings()
   {
      return m_strings;
   }

   /**
    * Get the commit mode used by the DataHandler.
    *
    * <p>One of DataHandler.COMMIT_AFTERSTATEMENT, COMMIT_AFTERDOCUMENT,
    * COMMIT_NONE, or COMMIT_NOTRANSACTIONS.
    *
    * @return The commit mode. COMMIT_AFTERSTATEMENT if initialize() has not been called.
    */
   public final int getCommitMode()
   {
      return m_commitMode;
   }

   // ************************************************************************
   // Public Methods -- DataHandler interface
   // ************************************************************************

   /**
    * Implements the initialize method in the DataHandler interface.
    *
    * @param dataSource The DataSource to get Connections from.
    * @param user The user to connect to the database as. May be null.
    * @param password The password to connect to the database with. May be null.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void initialize(DataSource dataSource, String user, String password)
      throws SQLException
   {
      if (m_dirtyConnection)
         throw new IllegalStateException("Cannot initialize the DataHandler. A connection has uncommitted results.");

      // Get the connection

      m_connection = (user == null) ? dataSource.getConnection() :
                                      dataSource.getConnection(user, password);

      // And the DML generator

      m_dml = new DMLGenerator(m_connection.getMetaData());
      m_strings = new SQLStrings(m_dml);

      // Set the remaining variables.

      m_commitMode = DataHandler.COMMIT_AFTERSTATEMENT;
      m_dirtyConnection = false;
      m_refreshCols = new Hashtable();
   }

   /**
    * Implements the startDocument method in the DataHandler interface.
    *
    * @param commitMode One of COMMIT_AFTERSTATEMENT, COMMIT_AFTERDOCUMENT,
    *    COMMIT_NONE, or COMMIT_NOTRANSACTIONS.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void startDocument(int commitMode)
      throws SQLException
   {
      checkState();

      // Set the commit mode.

      if ((commitMode != COMMIT_AFTERSTATEMENT) &&
          (commitMode != COMMIT_AFTERDOCUMENT) &&
          (commitMode != COMMIT_NONE) &&
          (commitMode != COMMIT_NOTRANSACTIONS))
         throw new IllegalArgumentException("Invalid commit mode: " + m_commitMode);
      m_commitMode = commitMode;

      // Set the auto-commit mode if necessary.

      if(m_commitMode == COMMIT_AFTERSTATEMENT)
      {
         // Check the auto-commit state first.

         if (!m_connection.getAutoCommit()) m_connection.setAutoCommit(true);
      }
      else if((m_commitMode == COMMIT_AFTERDOCUMENT) ||
              (m_commitMode == COMMIT_NONE))
      {
         // Check the auto-commit state first. MS Access has a bug where
         // setting auto-commit to false more than once wipes out all future
         // statements. That is, they are executed, but the state of the
         // database is not changed.

         if (m_connection.getAutoCommit()) m_connection.setAutoCommit(false);
      }

      m_dirtyConnection = false;
   }

   /**
    * Implements the endDocument method in the DataHandler interface.
    *
    * @exception SQLException Thrown if a database error occurs.
    */
   public void endDocument()
      throws SQLException
   {
      checkState();

      // Commit the transaction if any statements have been executed and
      // we commit after processing the entire document.

      if(m_dirtyConnection && (m_commitMode == COMMIT_AFTERDOCUMENT))
      {
         m_connection.commit();
         m_dirtyConnection = false;
      }
   }

   /**
    * Implements the recoverFromException method in the DataHandler interface.
    *
    * @exception SQLException Thrown if a database error occurs.
    */
   public void recoverFromException()
      throws SQLException
   {
      // If the commit mode is AFTERDOCUMENT, attempt to roll back changes. Note
      // that we don't check the dirty flag, since the error might have occurred
      // after the changes were made but before the dirty flag was set.

      if (m_commitMode == COMMIT_AFTERDOCUMENT)
      {
         m_connection.rollback();
         m_dirtyConnection = false;
      }
   }

   /**
    * Implements the insert method in the DataHandler interface; must be implemented
    * by child classes.
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @exception SQLException Thrown if a database error occurs.
    */
   public abstract void insert(Table table, Row row)
      throws SQLException;

   /**
    * Implements the update method in the DataHandler interface.
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @param columns The columns to update. These must have values in the
    *    Row object unless they are nullable.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void update(Table table, Row row, Column[] cols)
      throws SQLException
   {
      checkState();

      PreparedStatement stmt = buildUpdate(table, row, cols);
      int numRows = stmt.executeUpdate();

      if(numRows == 0)
         throw new SQLException("[xmldbms] Row to be updated is not present in table.");
      else if(numRows > 1)
         throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");

      databaseModified();
   }

   /**
    * Implements the updateOrInsert method in the DataHandler interface.
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void updateOrInsert(Table table, Row row)
      throws SQLException
   {
      checkState();

      PreparedStatement stmt = buildUpdate(table, row, null);
      int numRows = stmt.executeUpdate();

      if(numRows == 0)
      {
         insert(table, row);
      }
      else if(numRows > 1)
         throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");

      databaseModified();
   }

   /**
    * Implements the delete method in the DataHandler interface.
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @param The key. This may be any type of key (primary, unique, or foreign).
    * @exception SQLException Thrown if a database error occurs.
    */
   public void delete(Table table, Row row, Key key)
      throws SQLException
   {
      checkState();

      PreparedStatement stmt = buildDelete(table, row, key);
      int numRows = stmt.executeUpdate();

      if(key.getType() == Key.PRIMARY_KEY)
      {
         if(numRows == 0)
            throw new SQLException("[xmldbms] Row to be deleted is not present in table.");
         else if(numRows > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows deleted!");
      }

      databaseModified();
   }

   /**
    * Implements the delete method in the DataHandler interface.
    *
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with. May be null.
    * @param keyValue The value of the key. Null if the key is null.
    * @param where An additional where constraint. May be null.
    * @param paramColumns The columns corresponding to parameters in the where constraint.
    *   Null if there are no parameters.
    * @param paramValues The values of parameters in the where constraint. Null if there
    *   are no parameters.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void delete(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues)
      throws SQLException
   {
      checkState();

      PreparedStatement stmt = buildDelete(table, key, keyValue, where, paramColumns, paramValues);
      int numRows = stmt.executeUpdate();

      if (key != null)
      {
         if(key.getType() == Key.PRIMARY_KEY)
         {
            if(numRows == 0)
               throw new SQLException("[xmldbms] Row to be deleted is not present in table.");
            else if(numRows > 1)
               throw new SQLException("[xmldbms] Primary key not unique. Multiple rows deleted!");
         }
      }

      databaseModified();
   }

   /**
    * Implements the select method in the DataHandler interface.
    *
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with. May be null.
    * @param keyValue The value of the key.
    * @param where An additional where constraint. May be null.
    * @param paramColumns The columns corresponding to parameters in the where constraint.
    *   Null if there are no parameters.
    * @param paramValues The values of parameters in the where constraint. Null if there
    *   are no parameters.
    * @param order The sort information. May be null.
    * @return The result set.
    * @exception SQLException Thrown if a database error occurs.
    */
   public ResultSet select(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues, OrderInfo orderInfo)
      throws SQLException
   {
      checkState();

      PreparedStatement stmt = buildSelect(table, key, keyValue, where, paramColumns, paramValues, orderInfo);
      return stmt.executeQuery();
   }

   // ************************************************************************
   // Public methods -- build statements
   // ************************************************************************

   /**
    * Builds a prepared INSERT statement
    *
    * @param table The table into which to insert rows.
    * @param row The row containing data to insert.
    * @return The prepared INSERT statement
    * @exception SQLException Thrown if a database error occurs.
    */
   public PreparedStatement buildInsert(Table table, Row row)
      throws SQLException
   {
      // NOTE: The insert string is not cached because the row can have a
      // different set of columns each time.

/*
RPB: This code does not appear to be necessary. If the key column has a value
in the Row object, it is because there was a value in the XML document. And if
this value is null, it is because it was null in the XML document. If this causes
problems in the database, then that is a bug in the data, not a problem XML-DBMS
needs to solve.

      Vector colVec = row.getColumnVectorFor(table);

      // If any of the database generated key values are null
      // remove from the insert list. Certain DBMS have problems
      // otherwise.

      Column[] dbGeneratedCols = getDBGeneratedKeyCols(table);
      for(int i = 0; i < dbGeneratedCols.length; i++)
      {
         if(colVec.contains(dbGeneratedCols[i]) &&
            (row.getColumnValue(dbGeneratedCols[i]) == null))
         {
            colVec.removeElement(dbGeneratedCols[i]);
         }
      }

      Column[] cols = new Column[colVec.size()];
      colVec.copyInto(cols);
*/

      // Get a list of the columns that have values in the row.

      Column[] cols = row.getColumnArrayFor(table);

      // Build the INSERT statement

      String sql = m_dml.getInsert(table, cols);
      PreparedStatement stmt = m_connection.prepareStatement(sql);

      // Set the parameters

      Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

      return stmt;
   }

   /**
    * Builds an UPDATE statement of the form "UPDATE table SET (column = ?, ...) WHERE Key = ?".
    *
    * @param table The table into which to update data.
    * @param row The row containing new data values.
    * @param cols The columns to update. If this is null, all columns not in a
    *    primary or unique key for which there is data in the row are updated.
    * @return The prepared UPDATE statement
    * @exception SQLException Thrown if a database error occurs.
    */
   public PreparedStatement buildUpdate(Table table, Row row, Column[] cols)
      throws SQLException
   {
      Column[]          priCols, keyCols;
      Vector            colVec;
      Hashtable         colHash;
      int               i;
      Enumeration       e;
      String            sql;
      PreparedStatement stmt;

      // NOTE: The update string is not cached because the row can have a
      // different set of columns each time.

      // Get the columns in the primary key.

      priCols = table.getPrimaryKey().getColumns();

      // If no update columns are passed, update all non-unique/primary key
      // columns for which values are supplied.

      if(cols == null)
      {
         // Build a hashtable from the columns in the row that apply to this table.

         colVec = row.getColumnVectorFor(table);
         colHash = new Hashtable(colVec.size());
         for (i = 0; i < colVec.size(); i++)
         {
            colHash.put(colVec.elementAt(i), OBJECT);
         }
         
         // Remove the primary key columns. We don't update primary keys.

         for(i = 0; i < priCols.length; i++)
         {
            if(colHash.remove(priCols[i]) == null)
               throw new SQLException("[xmldbms] When updating data, you must supply values for all primary key columns. No value supplied for the " + priCols[i].getName() + " column.");
         }

         // Remove the unique key columns. We don't update unique keys.

         e = table.getUniqueKeys();
         while(e.hasMoreElements())
         {
            keyCols = ((Key)e.nextElement()).getColumns();
            for(i = 0; i < keyCols.length; i++)
            {
               colHash.remove(keyCols[i]);
            }
         }

         // Copy the remaining columns for the table into an array.

         cols = new Column[colHash.size()];
         e = colHash.keys();
         i = 0;
         while (e.hasMoreElements())
         {
            cols[i++] = (Column)e.nextElement();
         }
      }

      // Build the UPDATE statement

      sql = m_dml.getUpdate(table, table.getPrimaryKey(), cols);
      stmt = m_connection.prepareStatement(sql);

      // Set the parameters for SET clauses

      Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

      // And the parameters for the WHERE clause

      Parameters.setParameters(stmt, cols.length, priCols, row.getColumnValues(priCols));

      // Return the statement ready for execution

      return stmt;
   }

   /**
    * Builds a DELETE statement of the form "DELETE FROM table WHERE Key = ?".
    *
    * @param table The table from which to delete data.
    * @param row The row containing data for the key.
    * @param key The key that identifies the row or rows to delete.
    * @return The prepared DELETE statement
    * @exception SQLException Thrown if a database error occurs.
    */
   public PreparedStatement buildDelete(Table table, Row row, Key key)
      throws SQLException
   {

/*
RPB - Nobody uses this, so let's delete it and force people to pass the primary key.
      if(key == null)
         key = table.getPrimaryKey();
*/

      // These can be cached so use SQLStrings

      String sql = m_strings.getDelete(table, key);

      // Build the DELETE statement

      PreparedStatement stmt = m_connection.prepareStatement(sql);

      // Set the parameters

      Column[] cols = key.getColumns();
      Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

      // Return the prepared statement

      return stmt;
   }

   /**
    * Builds a DELETE statement of the form "DELETE FROM table WHERE Key = ? AND &lt;where>".
    *
    * @param table The table from which to delete data.
    * @param key The key that identifies the row or rows to delete. May be null.
    * @param keyValue The key's value.
    * @param where Additional delete conditions. May be null.
    * @param paramColumns Column objects that correspond to parameter markers (?) in the
    *    where parameter.
    * @param paramValues Parameter values for the where parameter.
    * @return The prepared DELETE statement
    * @exception SQLException Thrown if a database error occurs.
    */
   public PreparedStatement buildDelete(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues)
      throws SQLException
   {
      // These can be cached. Use SQLStrings

      String sql = m_strings.getDelete(table, key, where);

      // Build the DELETE statement

      PreparedStatement stmt = m_connection.prepareStatement(sql);

      // Set the parameters

      int start = 0;
      if (key != null)
      {
         Column[] keyColumns = key.getColumns();
         Parameters.setParameters(stmt, 0, keyColumns, keyValue);
         start = keyColumns.length;
      }
      if (paramColumns != null)
      {
         Parameters.setParameters(stmt, start, paramColumns, paramValues);
      }

      // Return the prepared statement.

      return stmt;
   }

   /**
    * Builds a SELECT statement of the form
    * "SELECT * FROM table WHERE Key = ? AND &lt;where> ORDER BY ?".
    *
    * @param table The table from which to select data.
    * @param key The key that identifies the row or rows to select. May be null.
    * @param keyValue The key's value.
    * @param where Additional select conditions. May be null.
    * @param paramColumns Column objects that correspond to parameter markers (?) in the
    *    where parameter.
    * @param paramValues Parameter values for the where parameter.
    * @param orderInfo The sort information. May be null.
    * @return The prepared SELECT statement
    * @exception SQLException Thrown if a database error occurs.
    */
   public PreparedStatement buildSelect(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues, OrderInfo orderInfo)
      throws SQLException
   {
      // These can be cached. Use SQLStrings

      String sql = m_strings.getSelect(table, key, where, orderInfo);

      // Build the SELECT statement

      PreparedStatement stmt = m_connection.prepareStatement(sql);

      // Set the parameters

      int start = 0;
      if (key != null)
      {
         Column[] keyColumns = key.getColumns();
         Parameters.setParameters(stmt, 0, keyColumns, keyValue);
         start = keyColumns.length;
      }
      if (paramColumns != null)
      {
         Parameters.setParameters(stmt, start, paramColumns, paramValues);
      }

      // Return the statement.

      return stmt;
   }

   // ************************************************************************
   // Public methods -- helpers
   // ************************************************************************

   /**
    * Checks if the DataHandler has been initialized.
    *
    * <p>Throws an exception if the DataHandler has not been initialized.</p>
    */
   public void checkState()
   {
      if (m_connection == null)
         throw new IllegalStateException("Invalid state. DataHandler has not been initialized.");
   }

   /**
    * Child classes must call this method after executing a statement that
    * modifies the database.
    *
    * <p>This allows DataHandlerBase to correctly commit transactions.</p>
    */
   public void databaseModified()
   {
      if(m_commitMode == COMMIT_AFTERDOCUMENT)
      {
         m_dirtyConnection = true;
      }

      // For COMMIT_AFTERSTATEMENT we use auto commit
   }

   /**
    * Get the columns used in database-generated keys.
    *
    * @param table The table for which to get columns.
    * @return The columns. May be empty.
    */
   public Column[] getDBGeneratedKeyCols(Table table)
   {
      // If we have already gotten the columns, just return them.

      if(m_refreshCols.contains(table)) return (Column[])m_refreshCols.get(table);

      // Allocate a new Vector.

      Vector colVec = new Vector();

      // Add the primary key columns if the primary key is generated by the database.
      // Note that there is no requirement that a primary key exist.

      Key priKey = table.getPrimaryKey();
      if (priKey != null)
      {
         if (priKey.getKeyGeneration() == Key.DATABASE)
         {
            Column[] priCols = priKey.getColumns();
            for(int i = 0; i < priCols.length; i++)
            {
               colVec.addElement(priCols[i]);
            }
         }
      }

      // Add unique key columns for unique keys generated by the database.

      Enumeration e = table.getUniqueKeys();
      while(e.hasMoreElements())
      {
         Key key = (Key)e.nextElement();
         if(key.getKeyGeneration() == Key.DATABASE)
         {
            Column[] keyCols = key.getColumns();
            for(int i = 0; i < keyCols.length; i++)
            {
               colVec.addElement(keyCols[i]);
            }
         }
      }

      // Copy the columns into an array, stored the array for later use,
      // and return it.

      Column[] cols = new Column[colVec.size()];
      colVec.copyInto(cols);

      m_refreshCols.put(table, cols);

      return cols;
   }

   /**
    * Create a key for a single column.
    *
    * @param colName The column's name
    * @param type The column's type
    * @return The key
    */
   public Key createColumnKey(String colName, int type)
   {
      // Create a single column array.

      Column[] keyCols = { Column.create(colName) };
      keyCols[0].setType(type);

      // Make a key out of it

      Key key = Key.createPrimaryKey(null);
      key.setColumns(keyCols);

      // Return the key

      return key;
   }

   /**
    * Retrieves the PreparedStatement used by the driver.
    *
    * <p>If the PreparedStatement has been wrapped by SPPreparedStatement, the
    * driver's PreparedStatement is returned. Otherwise, the input PreparedStatement
    * is returned.</p>
    *
    * @param stmt The (possibly wrapped) PreparedStatement.
    * @return The PreparedStatement actually used by the driver.
    */
   public PreparedStatement getRawStatement(PreparedStatement stmt)
   {
      if(stmt instanceof SPPreparedStatement)
         return ((SPPreparedStatement)stmt).getUnderlyingStatement();
      else
         return stmt;
   }

   /**
    * Sets a column value in a Row.
    *
    * <p>This method converts the value to the type used by the column.</p>
    *
    * @param row The Row in which to set the value.
    * @param column The Column for which to set the value.
    * @param val The value. May be null.
    * @exception SQLException Thrown if a conversion error occurs.
    */
   public void setColumnValue(Row row, Column column, Object val)
      throws SQLException
   {
      try
      {
         row.setColumnValue(column, ConvertObject.convertObject(val, column.getType(), column.getFormatter()));
      }
      catch(XMLMiddlewareException e)
      {
         throw new SQLException("[xmldbms]Conversion error: " + e.getMessage());
      }
   }
}