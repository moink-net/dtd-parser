package org.xmlmiddleware.xmldbms.datahandlers.external;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import javax.sql.*;

/**
 * <p>DataHandler implementation for Postgres using the default JDBC drivers.</p>
 *
 * <p>Database generated keys are retrieved using the the row oid.</p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public class PostgresHandler
    extends DataHandlerBase
{
    protected final static String OIDNAME = "oid";

    /** 
     * Creates a PostgresHandler. 
     */
    public PostgresHandler()
    {
        super();
    }

    /**
     * Overrides DataHandlerBase.initialize().
     */
    public void initialize(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super.initialize(dataSource, user, password);

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
        checkState();

        PreparedStatement stmt = makeInsert(table, row);
        int numRows = stmt.executeUpdate();

        executedStatement();

        Column[] refreshCols = getRefreshCols(table);

        if(refreshCols.length > 0)
        {
            org.postgresql.Statement psqlStmt = 
                    (org.postgresql.Statement)getRawStatement(stmt);

            // Get the OID of the last row
            int oid = psqlStmt.getInsertedOID();


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
                setColumnValue(row, refreshCols[i], rs.getObject(refreshCols[i].getName()));
        }

    }

    // The key for the 'oid' column
    private Key m_oidKey = null;
}
