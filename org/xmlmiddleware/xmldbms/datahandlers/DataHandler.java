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
// Changes from version 1.1: New in version 2.0

package org.xmlmiddleware.xmldbms.datahandlers;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import javax.sql.*;

/**
 * Interface for abstracting database access.
 *
 * <p>Objects that implement DataHandler should provide a no-argument constructor.</p>
 */
public interface DataHandler
{
   // ************************************************************************
   // Public constants
   // ************************************************************************

   /**
    * Commit transactions after each statement is executed.
    *
    * <p>The DataHandler commits a transaction after each statement is
    * successfully executed. It does not roll back the current transaction
    * after an exception, since there is nothing to roll back.</p>
    */
   public static final int COMMIT_AFTERSTATEMENT = 1;

   /**
    * Commit a transaction after the whole document is processed.
    *
    * <p>The DataHandler commits a transaction after the entire document is
    * successfully processed. It rolls back the current transaction if an
    * exception occurs.</p>
    */
   public static final int COMMIT_AFTERDOCUMENT = 2;

   /**
    * The calling code commits and rolls back transactions.
    *
    * <p>The DataHandler must not commit or roll back any transactions.
    * (This includes committing transactions indirectly through auto-commit.)
    * Instead, it is the responsibility of the calling code to commit or
    * roll back transactions. This is useful when the XML-DBMS operation
    * is part of a larger transaction.</p>
    */
   public static final int COMMIT_NONE = 3;

   /**
    * Do not use transactions.
    *
    * <p>The DataHandler issues no calls related to transactions, including
    * enabling or disabling auto-commit. This mode is used when the database
    * does not support transactions.</p>
    */
   public static final int COMMIT_NOTRANSACTIONS = 4;

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Initialize a DataHandler object
    *
    * @param dataSource The DataSource to get Connections from.
    * @param user The user to connect to the database as. May be null.
    * @param password The password to connect to the database with. May be null.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void initialize(DataSource dataSource, String user, String password)
      throws SQLException;

   /**
    * Start processing a new document.
    *
    * <p>Implementations generally set up transaction handling based on the
    * commit mode.</p>
    *
    * @param commitMode One of COMMIT_AFTERSTATEMENT, COMMIT_AFTERDOCUMENT,
    *    COMMIT_NONE, or COMMIT_NOTRANSACTIONS.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void startDocument(int commitMode)
      throws SQLException;

   /**
    * Finish processing a new document.
    *
    * <p>Implementations commit the transaction if the commit mode is
    * COMMIT_AFTERDOCUMENT.</p>
    *
    * @exception SQLException Thrown if a database error occurs.
    */
   public void endDocument()
      throws SQLException;

   /**
    * Recover from an exception.
    *
    * <p>This method performs any cleanup necessary to recover from an
    * exception encountered while processing a document. If the commit mode
    * is COMMIT_AFTERDOCUMENT, implementations roll back any changes made
    * in the current transaction.</p>
    *
    * <p>No rollback is performed for the other commit modes. With
    * COMMIT_AFTERSTATEMENT, there are never any changes to roll back,
    * since all changes are committed immediately. With COMMIT_NONE, it
    * is the application's responsibility to roll back changes. With
    * COMMIT_NOTRANSACTIONS, rollbacks are not suported.</p>
    *
    * <p>Implementations must not attempt to recover from exceptions that
    * occur in other methods, except when this method is called. For example,
    * if an exception occurs while inserting a row, the implementation should
    * throw that exception rather than attempting to clean up after it by
    * rolling back the current transaction.</p>
    *
    * <p>The reason for this is two-fold. First, the calling code needs to
    * know about all exceptions so it can coordinate rollbacks across
    * multiple databases. Second, exceptions such as conversions errors cannot
    * be detected by DataHandler implementations and isolating all exception
    * recovery in a single method results in cleaner code.</p>
    *
    * @exception SQLException Thrown if a database error occurs.
    */
   public void recoverFromException()
      throws SQLException;

   /**
    * Insert a row into a table using values from the Row object.
    *
    * <p>All non-nullable columns (except for database-generated columns) must
    * have non-null values in the Row object.</p>
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void insert(Table table, Row row)
      throws SQLException;

   /**
    * Update the specified columns in a table using values from the Row object.
    *
    * <p>The update statement identifies the row in the database by its
    * primary key. All non-nullable columns in the primary key must have
    * non-null values in the Row object.</p>
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @param columns The columns to update. These must have values in the
    *    Row object unless they are nullable. If this is null, all non-key
    *    columns that have values in the Row object are updated.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void update(Table table, Row row, Column[] columns)
      throws SQLException;

   /**
    * If a row already exists, update it. Otherwise, insert it.
    *
    * <p>If a row already exists, updateOrInsert updates the non-key columns that
    * have values from the Row object. The update statement identifies
    * the row in the database by its primary key. (All non-nullable columns
    * in the primary key must have non-null values in the Row object.)</p>
    *
    * <p>If the row does not exist, updateOrInsert inserts the row using values
    * from the Row object. All non-nullable columns must have non-null values in
    * the Row object.</p>
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void updateOrInsert(Table table, Row row)
      throws SQLException;

   /**
    * Delete a row or rows from a table.
    *
    * <p>The delete statement identifies the row in the database by its
    * primary key. All non-nullable columns in the primary key must have
    * non-null values in the Row object or no rows will be deleted.</p>
    *
    * @param table The table.
    * @param row The row. This may contain values for columns that do not
    *    belong to the table.
    * @param The key. This may be any type of key (primary, unique, or foreign).
    * @exception SQLException Thrown if a database error occurs.
    */
   public void delete(Table table, Row row, Key key)
      throws SQLException;

   /**
    * Delete a row or rows from a table based on a set of columns.
    *
    * <p>The DELETE statement has the form:</p>
    *
    * <pre>
    *   DELETE FROM Table WHERE Key = ? AND &lt;where>
    * </pre>
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
      throws SQLException;

   /**
    * Select rows from a given table.
    *
    * <p>The SELECT statement has the form:</p>
    *
    * <pre>
    *   SELECT * FROM Table WHERE Key = ? AND &lt;where> ORDER BY ?
    * </pre>
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
      throws SQLException;
}
