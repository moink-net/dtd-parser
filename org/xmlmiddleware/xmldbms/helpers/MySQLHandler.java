package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;


class MySQLHandler
    extends DataHandlerBase
{
    MySQLHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);

        m_usedKeys = new Hashtable();
    }


	public void insert(Table table, Row row)
        throws SQLException
    {   
        PreparedStatement stmt = makeInsert(table, row);
        int numRows = stmt.executeUpdate();

        Column[] refreshCols = getRefreshCols(table);

        if(refreshCols.length > 0)
        {
            org.gjt.mm.mysql.Statement mysqlStmt = (org.gjt.mm.mysql.Statement)stmt;
            int lastInsert = getLastInsertID();

            Key key = getAutoIncrementKey(table);

            // SELECT the columns with that key
            String sql = m_dml.getSelect(table, key, refreshCols);
            PreparedStatement selStmt = conn.prepareStatement(sql);

            // Put the last insert value in
            selStmt.setInt(1, lastInsert);

            // Execute it 
            ResultSet rs = selStmt.execute();

            // Set them in the row
            for(int i = 0; i < refreshCols.length; i++)
                row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));

        }

    }


    protected Key getAutoIncrementKey(Table table)
        throws SQLException
    {
        if(m_usedKeys.contains(table))
            return (Key)m_usedKeys.get(table);


        // TODO: More efficient way to do this?

        // We create a SELECT statement for the table 

        // SELECT * FROM table WHERE 1 = 0;
        String sql = m_dml.getSelect(table, "1 = 0", null);

        // Execute it
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery();

        // Get the Metadata ...
        ResultSetMetaData rsMeta = rs.getMetaData();

        // Look far an auto increment column
        for(int i = 0; i < rsMeta.getColumnCount(); i++)
        {
            // NOTE: The assumption is made that MySQL can only have 
            // one auto-increment column. This is true in the versions 
            // tested (3.22)

            if(rsMeta.isAutoIncrement(i))
            {
                // Create the key
                Key key = createKey(rsMeta.getColumnName(i), rsMeta.getColumnType(i));
                
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

            

    private Hashtable m_usedKeys;
}
