package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;


class PostgresHandler
    extends DataHandlerBase
{
    protected final static String OIDNAME = "oid";

    /** 
     * Creates a PostgresHandler. 
     *
     * @param dataSource The Datasource to retrive connections from.
     * @param user Login name for dataSource.
     * @param password Password for dataSource.
     */
    PostgresHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);

        // Create the key
        m_oidKey = createColumnKey(OIDNAME, Types.INTEGER); 
    }


    /**
     * Inserts a row into the table. Refreshes any key columns needed. Does this
     * via the oid column. 
     *
     * @param table Table to insert into.
     * @param row Row to insert.
     */
	public void insert(Table table, Row row)
        throws SQLException
    {     
        PreparedStatement stmt = makeInsert(table, row);
        int numRows = stmt.executeUpdate();

        Column[] refreshCols = getRefreshCols(table, row);

        if(refreshCols.length > 0)
        {
            // Get the OID of the last row
            org.postgresql.Statement psqlStmt = (org.postgresql.Statement)stmt;
            int oid = psqlStmt.getInsertedOID();

            // SELECT the columns with that oid
            String sql = m_dml.getSelect(table, m_oidKey, refreshCols);
            PreparedStatement selStmt = m_connection.prepareStatement(sql);

            // Put the oid in
            selStmt.setInt(1, oid);

            // Execute it 
            ResultSet rs = selStmt.executeQuery();

            // Set them in the row
            for(int i = 0; i < refreshCols.length; i++)
                row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));

        }

    }

    // The key for the 'oid' column
    private Key m_oidKey;
}
