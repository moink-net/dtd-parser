package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;

// TODO: 2) Implementations of update must check to see that the list of
// updateColumns does not include any primary key or unique key columns.
// (State this in the comments.)

abstract class DBActionBase
    implements DBAction
{
    protected Connection m_connection;
    protected DMLGenerator m_dml;
    protected int m_commitMode;

    DBActionImpl(DataSource dataSource, String user, String password)
        throws SQLException
    {
        m_connection = dataSource.getConnection(user, password);
        m_dml = new DMLGenerator(m_connection.getMetaData());

        // TODO: Shouldn't the commit modes be moved to another class?
        m_commitMode = DOMToDBMS.COMMIT_AFTERSTATEMENT;
    }



    public void startDocument(int commitMode)
        throws SQLException
    {
        m_commitMode = commitMode;

        if(m_commitMode == COMMIT_AFTERSTATEMENT)
            m_connection.setAutoCommit(true);

        // TODO: Do we need to do this for COMMIT_NONE?
        // It should have already been done!
        else if(m_commitMode == COMMIT_AFTERDOCUMENT || 
                m_commitMode == COMMIT_NONE)
            m_connection.setAutoCommit(false);   


    }

    public void endDocument()
        throws SQLException
    {
        if(m_commitMode == COMMIT_AFTERDOCUMENT)
            m_connection.commit();

        // TODO: If there's an error in the document do we rollback?
        // My guess is that, if the connection is closed without commit 
        // then it's automatically rolled back
    }



    public abstract void insert(Table table, Row row)
        throws SQLException; 


    public void update(Table table, Row row)
        throws SQLException
    {
        int ret = doUpdate(table, row);

        if(ret == 0)
            throw new SQLException("[xmldbms] Row to be updated is not present in table.");
        else if(ret > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");

        // TODO: Do we need to refresh values here? I think not as any changes 
        // should have been done by us, and updating keys is already suspect.
    }


    public void updateOrInsert(Table table, Row row)
        throws SQLException
    {
        int ret = doUpdate(table, row);

        if(ret == 0)
            insert(table, row);
        else if(ret > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");
    }


    public void delete(Table table, Row row)
        throws SQLException
    {
        int ret = doDelete(table, row);

        if(ret == 0)
            throw new SQLException("[xmldbms] Row to be deleted is not present in table.");
        else if(ret > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows deleted!");
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

    protected int doUpdate(Table table, Row row, )
        throws SQLException
    { 
        // TODO: Do we assume that the parameter

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

        // TODO: Remove unique key columns 

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
        // NOTE: offset is 0 based. Add one to offset to get proper parameter index

    }

    

}
