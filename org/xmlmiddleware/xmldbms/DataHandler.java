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

    public void delete(Table table, Row row)
        throws SQLException;

    public ResultSet select(Table table, Object[] key, OrderInfo orderInfo)
        throws SQLException;
}
