package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.sql.*;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * Interface for abstracting database access. 
 *
 */
public interface DBAction  
{
    /**
     * Inserts a row. The stmt parameter must be executed, and columns in refreshCols 
     * must be retrieved from the row inserted. Other actions can be performed
     * against database.
     *
     * @param conn The Connection to execute additional statements against.
     * @param stmt The PreparedStatement to execute.
     * @param table The Table to execute additional statements against.
     * @param row A Row with values that were set. Refreshed values are returned here as well.
     * @param refreshCols Columns that must be retrieved from database for inserted row.
     */
	public void insert(Connection conn, PreparedStatement stmt, Table table, Row row, Column[] refreshCols)
        throws SQLException;

    /**
     * Updates a row. The stmt parameter must be executed, and columns in refreshCols 
     * must be retrieved from the row updated. Other actions can be performed
     * against database.
     *
     * @param conn The Connection to execute additional statements against.
     * @param stmt The PreparedStatement to execute.
     * @param table The Table to execute additional statements against.
     * @param row A Row with values that were set. Refreshed values are returned here as well.
     * @param refreshCols Columns that must be retrieved from database for updated row.
     */
    public void update(Connection conn, PreparedStatement stmt, Table table, Row row, Column[] refreshCols)
        throws SQLException;

    /**
     * Deletes a row. The stmt parameter must be executed. Other actions can be performed
     * against database.
     *
     * @param conn The Connection to execute additional statements against.
     * @param stmt The PreparedStatement to execute.
     * @param table The Table to execute additional statements against.
     */
    public void delete(Connection conn, PreparedStatement stmt, Table table)
        throws SQLException;
}
