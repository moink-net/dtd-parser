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

    PostgresHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);

        // Create the oid column
        Column[] keyCols = { Column.create(OIDNAME); }
        keyCol[0].setType(Types.INTEGER);

        // Make a key out of it
        m_oidKey = Key.createPrimaryKey(null);
        m_oidKey.setColumns(keyCols);
    }


	public void insert(Table table, Row row)
        throws SQLException
    {     
        int rows = doInsert();

        Column[] refreshCols = getRefreshCols(table);

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
            ResultSet rs = selStmt.execute();

            // Set them in the row
            for(int i = 0; i < refreshCols.length; i++)
                row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));

        }

    }

    private Column m_oidKey;
}
