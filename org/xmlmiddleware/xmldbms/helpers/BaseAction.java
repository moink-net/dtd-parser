package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.sql.*;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;

class BaseAction
{
    public void update(Connection conn, PreparedStatement stmt, Table table, Row row, Column[] refreshCols)
        throws SQLException
    {
        initDML(conn);

        // Execute statement
        stmt.executeUpdate();

        // TODO: if refreshCols == primaryKey skip below

        // Select statement based on primaryKey with refreshCols
        String sql = m_dml.getSelect(table, table.getPrimaryKey(), refreshCols);
        PreparedStatement selStmt = conn.prepareStatement(sql);

        // Set the key values on the statement
        // TODO: !!!! Set parameters once we know how !!!!
        
        // Execute it
        ResultSet rs = selStmt.execute();

        // Copy values back to row
        // TODO: !!!! Get values once we know how !!!!
    }

    public void delete(Connection conn, PreparedStatement stmt, Table table)
        throws SQLException
    {
        // Execute the statement
        stmt.executeUpdate();
    }


    protected void initDML(Connection conn)
        throws SQLException
    {
        if(m_dml == null)
            m_dml = new DMLGenerator(conn.getMetaData());
    }

    protected DMLGenerator m_dml = null;
}
