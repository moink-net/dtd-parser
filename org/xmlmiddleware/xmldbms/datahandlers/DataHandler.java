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
    /** 
     * Commit transaction after each statement. 
     */
    public static final int COMMIT_AFTERSTATEMENT = 1;

    /**
     * Commit transaction after the whole document
     */
    public static final int COMMIT_AFTERDOCUMENT = 2;

    /**
     * Let calling code commit transaction. Useful when part of 
     * a larger transaction.
     */
    public static final int COMMIT_NONE = 3;

    /**
     * Disable transactions all together.
     */
    public static final int COMMIT_NOTRANSACTIONS = 4;


    /**
     * Initialize a DataHandler object
     *
     * @param dataSource The DataSource to get Connection's from.
     * @param user User to connect to the database as.
     * @param password Password to connect to the database with.
     */
    public void initialize(DataSource dataSource, String user, String password)
        throws SQLException;

    public void startDocument(int commitMode)
        throws SQLException;

    public void endDocument()
        throws SQLException;

    public void insert(Table table, Row row)
        throws SQLException;

    public void update(Table table, Row row, Column[] columns)
        throws SQLException;

    public void updateOrInsert(Table table, Row row)
        throws SQLException;

    public void delete(Table table, Row row, Key key)
        throws SQLException;

    /**
     * Delete rows from a given table.
     *
     * <p>The DELETE statement has the form:</p>
     *
     * <pre>
     *    SELECT FROM Table WHERE Key = ? AND &lt;where>
     * </pre>
     *
     * @param t The table to select from. Must not be null.
     * @param key The key to restrict with. May be null.
     * @param keyValue The value of the key.
     * @param where An additional where constraint. May be null.
     * @param paramColumns The columns corresponding to parameters in the where constraint.
     *    Null if there are no parameters.
     * @param paramValues The values of parameters in the where constraint. Null if there
     *    are no parameters.
     */
    public void delete(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues)
        throws SQLException;

    /**
     * Select rows from a given table.
     *
     * <p>The SELECT statement has the form:</p>
     *
     * <pre>
     *    SELECT * FROM Table WHERE Key = ? AND &lt;where> ORDER BY ?
     * </pre>
     *
     * @param t The table to select from. Must not be null.
     * @param key The key to restrict with. May be null.
     * @param keyValue The value of the key.
     * @param where An additional where constraint. May be null.
     * @param paramColumns The columns corresponding to parameters in the where constraint.
     *    Null if there are no parameters.
     * @param paramValues The values of parameters in the where constraint. Null if there
     *    are no parameters.
     * @param order The sort information. May be null.
     * @return The result set.
     */
    public ResultSet select(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues, OrderInfo orderInfo)
        throws SQLException;
}
