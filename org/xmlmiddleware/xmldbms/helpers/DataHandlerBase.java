package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;

abstract class DataHandlerBase
    implements DataHandler
{
    protected Connection m_connection;
    protected DMLGenerator m_dml;
    protected int m_commitMode;

    DataHandlerBase(DataSource dataSource, String user, String password)
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


    public void update(Table table, Row row, Column[] cols)
        throws SQLException
    {
        int ret = doUpdate(table, row, cols);

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
        int ret = doUpdate(table, row, null);

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
        Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

        return stmt.executeUpdate();
    }

    protected int doUpdate(Table table, Row row, Column[] cols)
        throws SQLException
    { 
        Column[] priCols = table.getPrimaryKey().getColumns();

        if(cols == null)
        {
            // Get the columns that were set
            Vector colVec = row.getColumnVectorFor(table);
    
            // Remove the primary key from columns
            for(int i = 0; i < priCols.length; i++)
            {
                if(!colVec.contains(priCols[i]))
                    throw new SQLException("[xmldbms] Primary key value not supplied for UPDATE.");
            
                colVec.remove(priCols[i]);
            }

            // Remove unique key columns. These should not be updated
            Enumeration e = table.getUniqueKeys();
            while(e.hasMoreElements())
            {
                Column[] keyCols = ((Key)e.nextElement()).getColumns();
                for(int i = 0; i < keyCols.length; i++)
                {
                    colVec.remove(keyCols[i]);
                }
            }

            cols = (Column[])colVec.toArray();
        }


        // Make the UPDATE statement
        String sql = m_dml.getUpdate(table, table.getPrimaryKey(), cols);
        PreparedStatement stmt = m_connection.prepareStatement(sql);


        // Set the parameters for SET clauses
        Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

        // And the parameters for the WHERE clause
        Parameters.setParameters(stmt, cols.length, priCols, row.getColumnValues(priCols));


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

        Column[] cols = priKey.getColumns();
        // Set the parameters
        Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

        // Execute Statement
        return stmt.executeUpdate();
    }


    protected Column[] getRefreshCols(Table table, Row row)
    {
        Vector colVec = new Vector();

        // Add the primary key
        Key priKey = table.getPrimaryKey();
        if(priKey.getKeyGeneration() == Key.DATABASE)
        {
            Column[] priCols = priKey.getColumns();
            for(int i = 0; i < priCols.length; i++)
                colVec.addElement(priCols[i]);
        }

        // Add unique key columns. These should not be updated
        Enumeration e = table.getUniqueKeys();
        while(e.hasMoreElements())
        {
            Key key = (Key)e.nextElement();
            if(key.getKeyGeneration() == Key.DATABASE)
            {
                Column[] keyCols = key.getColumns();
                for(int i = 0; i < keyCols.length; i++)
                    colVec.addElement(keyCols[i]);
            }
        }

        return (Column[])colVec.toArray();    
    }
}
