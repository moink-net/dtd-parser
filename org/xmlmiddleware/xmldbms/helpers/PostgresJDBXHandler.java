package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.xmldbms.maps.*;

import org.sourceforge.jxdbcon.postgresql.PGPreparedStatement;

/**
 * <p>DataHandler implementation for Postgres using the JDBXCon drivers
 * (http://jxdbcon.sourceforge.net/).</p>
 *
 * <p>Database generated keys are retrieved using the the row oid.</p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public class PostgresJDBXHandler
    extends DataHandlerBase
{
    protected final static String OIDNAME = "oid";

    /** 
     * Creates a PostgresJDBXHandler
     *
     * @param dataSource The Datasource to retrive connections from.
     * @param user Login name for dataSource.
     * @param password Password for dataSource.
     */
    public PostgresJDBXHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);

        // Create the oid column
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

        executedStatement();

        Column[] refreshCols = getRefreshCols(table);

        if(refreshCols.length > 0)
        {
            PGPreparedStatement psqlStmt = (PGPreparedStatement)getRawStatement(stmt);

            // Get the OID of the last row
            ResultSet oidRs = psqlStmt.getGeneratedKeys();
            int oid = oidRs.getInt(OIDNAME);

            // SELECT the columns with that oid
            String sql = m_dml.getSelect(table, m_oidKey, refreshCols);
            PreparedStatement selStmt = m_connection.prepareStatement(sql);

            // Put the oid in
            selStmt.setInt(1, oid);

            // Execute it 
            ResultSet rs = selStmt.executeQuery();

            if(!rs.next())
                throw new SQLException("[xmldbms] Couldn't retrieve inserted row.");

            // Set them in the row
            for(int i = 0; i < refreshCols.length; i++)
                row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));

        }
    }

    // The key for the 'oid' column
    private Key m_oidKey;
}
