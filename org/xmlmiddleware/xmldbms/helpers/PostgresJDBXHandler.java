package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;

class PostgresJDBXHandler
    extends DataHandlerBase
{
    protected final static String OIDNAME = "oid";

    PostgresJDBXHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);

        // Create the oid column
        m_oidKey = createColumnKey(OIDNAME, Types.INTEGER);
    }

	public void insert(Table table, Row row)
        throws SQLException
    {     
        PreparedStatement stmt = makeInsert(table, row);
        int numRows = stmt.executeUpdate();

        Column[] refreshCols = getRefreshCols(table, row);

        if(refreshCols.length > 0)
        {
            // Get the OID of the last row
            PGPreparedStatement psqlStmt = (PGPreparedStatement)stmt;
            ResultSet oidRs = psqlStmt.getGeneratedKeys();
            int oid = oidRs.getInt(OIDNAME);

            // SELECT the columns with that oid
            String sql = m_dml.getSelect(table, m_oidKey, refreshCols);
            PreparedStatement selStmt = m_connection.prepareStatement(sql);

            // Put the oid in
            selStmt.setInt(1, oid);

            // Execute it 
            ResultSet rs = selStmt.execute();

            // Set them in the row
            for(int i = 0; i < refreshCols.length; i++)
                row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));

        }
    }

    private Key m_oidKey;
}
