package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;

abstract class DBActionImpl
    implements DBAction
{
    DBActionImpl(DataSource dataSource, String user, String password)
        throws SQLException
    {
        m_connection = dataSource.getConnection(user, password);
        m_dml = new DMLGenerator(m_connection.getMetaData());
    }


    public void update(Table table, Row row)
        throws SQLException
    {
        if(doUpdate(table, row) == 0)
            throw new SQLException("[xmldbms] Row to be updated is not present in table.");
        
        // TODO: Do we need to check for insane condition where more than one
        // row matches?

        // TODO: Do we need to refresh values here? I think not as any changes 
        // should have been done by us, and updating keys is already suspect.
    }


    public abstract void insert(Table table, Row row, boolean soft)
        throws SQLException;

    
    public void delete(Table table, Row row, boolean soft)
        throws SQLException
    {
        int ret = doDelete(table, row);

        // TODO: Do we need to check for insane condition where more than one
        // row matches?

        if(ret == 0 && !soft)
            throw new SQLException("[xmldbms] Row to be deleted is not present in table.");
    }

    public void updateOrInsert(Table table, Row row)
        throws SQLException
    {
        int ret = doUpdate(table, row);

        // TODO: Do we need to check for insane condition where more than one
        // row matches?

        if(ret == 0)
            insert(table, row, false);
    }
       




    protected int doInsert(Table table, Row row)
        throws SQLException
    {
        Column[] cols = row.getColumnsFor(table);

        // Make the INSERT statement
        String sql = m_dml.getInsert(table, cols);
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        // Set the parameters
        setParameters(stmt, 0, row.getColumnValues(cols));

        return stmt.executeUpdate();
    }

    protected int doUpdate(Table table, Row row)
        throws SQLException
    { 
        // Get the columns that were set
        Vector cols = row.getColumnVectorFor(table);
    
        // Remove the primary key from columns
        Column[] priCols = table.getPrimaryKey().getColumns();

        for(int i = 0; i < priCols.length; i++)
        {
            if(!cols.contains(priCols[i]))
                throw new SQLException("[xmldbms] Primary key value not supplied for UPDATE.");
            
            cols.remove(priCols[i]);
        }

        // Get trimmed array
        Column[] colArray = (Column[])cols.toArray();


        // Make the UPDATE statement
        String sql = m_dml.getUpdate(table, table.getPrimaryKey(), colArray);
        PreparedStatement stmt = m_connection.prepareStatement(sql);


        // Set the parameters
        setParameters(stmt, 0, row.getColumnValues(colArray));
        setParameters(stmt, colArray.length, row.getColumnValues(priCols));


        // Execute statement
        return stmt.executeUpdate();

    }


    protected int doDelete(Table table, Row row)
        throws SQLException
    {
        Key priKey = table.getPrimaryKey();

        // Make the DELETE statement
        String sql = m_dml.getDelete(table, priKey);
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        // Set the parameters
        setParameters(stmt, 0, row.getColumnValues(priKey.getColumns()));

        // Execute Statement
        return stmt.executeUpdate();
    }



    protected void setParameters(PreparedStatement stmt, int offset, Object[] values)
        throws SQLException
    {
        // TODO: Need implementation
        // NOTE: offset is 0 based. Add one to offset to get proper parameter index

    }

    

    protected Connection m_connection;
    protected DMLGenerator m_dml;
}
