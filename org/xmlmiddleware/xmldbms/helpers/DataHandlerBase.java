package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;

abstract class DataHandlerBase
    implements DataHandler
{
    protected Connection m_connection;
    protected DMLGenerator m_dml;
    protected int m_commitMode;

    /**
     * Creates a DataHandlerBase
     */

    DataHandlerBase(DataSource dataSource, String user, String password)
        throws SQLException
    {
        m_connection = dataSource.getConnection(user, password);
        m_dml = new DMLGenerator(m_connection.getMetaData());

        // TODO: Shouldn't the commit modes be moved to another class?
        m_commitMode = COMMIT_AFTERSTATEMENT;
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
        PreparedStatement stmt = makeUpdate(table, row, cols);
        int numRows = stmt.executeUpdate();

        if(numRows == 0)
            throw new SQLException("[xmldbms] Row to be updated is not present in table.");
        else if(numRows > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");

        // TODO: Do we need to refresh values here? I think not as any changes 
        // should have been done by us, and updating keys is already suspect.
    }


    public void updateOrInsert(Table table, Row row)
        throws SQLException
    {
        PreparedStatement stmt = makeUpdate(table, row, null);  
        int numRows = stmt.executeUpdate();

        if(numRows == 0)
            insert(table, row);
        else if(numRows > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");
    }


    public void delete(Table table, Row row)
        throws SQLException
    {
        PreparedStatement stmt = makeDelete(table, row);
        int numRows = stmt.executeUpdate();

        if(numRows == 0)
            throw new SQLException("[xmldbms] Row to be deleted is not present in table.");
        else if(numRows > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows deleted!");
    }

    public ResultSet select(Table table, Object[] key, OrderInfo orderInfo)
        throws SQLException
    {
        PreparedStatement stmt = makeSelect(table, key, orderInfo);
        return stmt.executeQuery();
    }

       




    protected PreparedStatement makeSelect(Table table, Object[] key, OrderInfo orderInfo)
        throws SQLException
    {
        Key priKey = table.getPrimaryKey();

        // Make the SELECT statement
        String sql = m_dml.getSelect(table, priKey, orderInfo);
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        // Set the paremeters
        Parameters.setParameters(stmt, 0, priKey.getColumns(), key);

        return stmt;
    }

    protected PreparedStatement makeInsert(Table table, Row row)
        throws SQLException
    {
        Column[] cols = row.getColumnsFor(table);

        // Make the INSERT statement
        String sql = m_dml.getInsert(table, cols);
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        // Set the parameters
        Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

        return stmt;
    }

    protected PreparedStatement makeUpdate(Table table, Row row, Column[] cols)
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


        // Return the statement ready for execution
        return stmt;
    }


    protected PreparedStatement makeDelete(Table table, Row row)
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
        return stmt;
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

    protected Key createColumnKey(String colName, int type)
    {
        Column[] keyCols = { Column.create(colName) };
        keyCols[0].setType(type);

        // Make a key out of it
        Key key = Key.createPrimaryKey(null);
        key.setColumns(keyCols);

        return key;
    }        
}
