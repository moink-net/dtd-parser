package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.sql.*;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * Interface for abstracting database access. 
 *
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
