package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;


class MySQLHandler
    extends DataHandlerBase
{
    /** 
     * Creates a MySQLHandler
     *
     * @param dataSource The Datasource to retrive connections from.
     * @param user Login name for dataSource.
     * @param password Password for dataSource.
     */
    MySQLHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);

        // Hashtable of keys used
        m_usedKeys = new Hashtable();
    }

  
    /**
     * Inserts a row into the table. Refreshes any key columns needed. Does this
     * via an AUTO_INCREMENT column.
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
            org.gjt.mm.mysql.Statement mysqlStmt = (org.gjt.mm.mysql.Statement)stmt;
            Long lastInsert = new Long(mysqlStmt.getLastInsertID());

            Key key = getAutoIncrementKey(table);

            // If that's the only column to be retrieved...
            if(refreshCols.length == 1 &&
               refreshCols[0].getName().equalsIgnoreCase(key.getColumns()[0].getName()))
            {
                // ... then just set that column
                row.setColumnValue(refreshCols[0], lastInsert);
            }
            else
            {
                // Otherwise do a SELECT on the row with that key
                String sql = m_dml.getSelect(table, key, refreshCols);
                PreparedStatement selStmt = m_connection.prepareStatement(sql);

                // Put the last insert value in
                Parameters.setParameter(selStmt, 1, key.getColumns()[0].getType(), lastInsert);

                // Execute it 
                ResultSet rs = selStmt.executeQuery();

                if(!rs.next())
                    throw new SQLException("[xmldbms] Couldn't retrieve inserted row.");

                // Set them in the row
                for(int i = 0; i < refreshCols.length; i++)
                    row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));
            }
        }

    }

    // Values used when parsing the 'SHOW COLUMS' statement
    private final static String MYSQL_COL_FIELD = "Field";
    private final static String MYSQL_COL_EXTRA = "Extra";
    private final static String MYSQL_AUTOINCREMENT = "auto_increment";
    private final static String MYSQL_COLUMN_SQL = "SHOW COLUMNS FROM ";

    /**
     * Retrieves the AUTO_INCREMENT column for a given table.
     *
     * @param table The table.
     */
    protected Key getAutoIncrementKey(Table table)
        throws SQLException
    {
        if(m_usedKeys.contains(table))
            return (Key)m_usedKeys.get(table);


        // TODO: More efficient way to do this?

        // We create a MySQL statement to get the table definition
        String sql = MYSQL_COLUMN_SQL + m_dml.getTableName(table) + ";";

        // Execute it
        Statement stmt = m_connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next())
        {
            // NOTE: The assumption is made that MySQL can only have 
            // one auto-increment column. This is true in the versions 
            // tested (3.22)

            String extra = rs.getString(MYSQL_COL_EXTRA);
            if(extra.equalsIgnoreCase(MYSQL_AUTOINCREMENT))
            {
                // Get the column type for the autoincrement fellow
                Column col = table.getColumn(rs.getString(MYSQL_COL_FIELD));

                // TODO: Should we throw an exception if the column
                // is not found?
                if(col == null)
                    continue;

                // Create the key
                Key key = createColumnKey(col.getName(), col.getType());
                
                // Cache it
                m_usedKeys.put(table, key);

                // and return it.
                return key;
            }
        }

        // If we got here then there are no auto-increment columns
        // which means we can't really support database generated keys
        // at this point in time.

        throw new SQLException("[xmldbms] Cannot support database generated keys on a MySQL table with no AUTO_INCREMENT field.");
    }       


    // Cache of AUTO_INCREMENT columns found in different tables
    private Hashtable m_usedKeys;
}
