package org.xmlmiddleware.xmldbms.datahandlers;

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Implements basic support for the DataHandler interface. The insert(...) 
 * is implemented by child classes.
 *
 * @author Sean Walter
 * @version 2.0
 */
public abstract class DataHandlerBase
    implements DataHandler
{
    // ************************************************************************
    // Variables used by this and child classes
    // ************************************************************************

    /**
     * The connection.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public Connection m_connection = null;

    /**
     * The DMLGenerator used to generate SQL statements.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public DMLGenerator m_dml = null;

    /**
     * The SQLStrings object used to cache the string form of SQL statements.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public SQLStrings m_strings = null;

    // ************************************************************************
    // Private variables
    // ************************************************************************

    // Does the connection need a commit?
    private boolean m_dirtyConnection = false;

    // The current Commit mode
    private int m_commitMode = DataHandler.COMMIT_AFTERSTATEMENT;

    // Cache of refreshCols by table
    private Hashtable m_refreshCols = null;


    // ************************************************************************
    // Constructor
    // ************************************************************************

    /**
     * Creates a DataHandlerBase
     */
    public DataHandlerBase()
    {
    }


    // ************************************************************************
    // Public Methods
    // ************************************************************************

    /**
     * Initialize a DataHandler object
     *
     * @param dataSource The DataSource to get Connection's from.
     * @param user User to connect to the database as.
     * @param password Password to connect to the database with.
     */
    public void initialize(DataSource dataSource, String user, String password)
        throws SQLException
    {
        if (m_dirtyConnection)
           throw new IllegalStateException("Cannot initialize the DataHandler. A connection has uncommitted results.");

        // Get the connection
        m_connection = dataSource.getConnection(user, password);

        // And DML generator
        m_dml = new DMLGenerator(m_connection.getMetaData());
        m_strings = new SQLStrings(m_dml);

        m_commitMode = DataHandler.COMMIT_AFTERSTATEMENT;
        m_dirtyConnection = false;
        m_refreshCols = new Hashtable();
    }

    /**
     * Is called when a document begins processing using this
     * DataHandler
     *
     * @param commitMode Commit mode for the current document.
     */
    public void startDocument(int commitMode)
        throws SQLException
    {
        checkState();

        m_commitMode = commitMode;

        if(m_commitMode == COMMIT_AFTERSTATEMENT)
            m_connection.setAutoCommit(true);

        // TODO: Do we need to do this for COMMIT_NONE?
        // It should have already been done, by the time we get this connection, no?
        else if(m_commitMode == COMMIT_AFTERDOCUMENT || 
                m_commitMode == COMMIT_NONE)
            m_connection.setAutoCommit(false);   

        m_dirtyConnection = false;
    }


    /**
     * Is called when a document has completed processing using
     * this DataHandler. Commits if necessary. 
     */
    public void endDocument()
        throws SQLException
    {
        checkState();

        if(m_dirtyConnection && m_commitMode == COMMIT_AFTERDOCUMENT)
        {
            m_connection.commit();
            m_dirtyConnection = false;
        }

        // TODO: How do errors/commit/rollback interact?
    }


    /** 
     * Implemented in child classes
     *
     * @param table Table to insert into.
     * @param row Row with values to insert.
     */
    public abstract void insert(Table table, Row row)
        throws SQLException; 


    /**
     * Update a row in a table.
     * 
     * @param table Table to update.
     * @param row Row with values to update.
     * @param cols Columns to update. If null then all values present in 'row' will be updated.
     */
    public void update(Table table, Row row, Column[] cols)
        throws SQLException
    {
        checkState();

        PreparedStatement stmt = makeUpdate(table, row, cols);
        int numRows = stmt.executeUpdate();


        if(numRows == 0)
            throw new SQLException("[xmldbms] Row to be updated is not present in table.");
        else if(numRows > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");

        executedStatement();

        // TODO: Do we need to refresh values here? I think not. Any changes 
        // should have been done by us, and updating keys is suspect.
    }


    /** 
     * Update a row in a table if present. If not present then insert.
     *
     * @param table Table to modify.
     * @param row Row with values.
     */
    public void updateOrInsert(Table table, Row row)
        throws SQLException
    {
        checkState();

        PreparedStatement stmt = makeUpdate(table, row, null);  
        int numRows = stmt.executeUpdate();

        if(numRows == 0)
            insert(table, row);
        else if(numRows > 1)
            throw new SQLException("[xmldbms] Primary key not unique. Multiple rows updated!");

        executedStatement();
    }


    /**
     * Delete a row from a table.
     *
     * @param table Table to delete from.
     * @param row Row with key values to delete.
     */
    public void delete(Table table, Row row, Key key)
        throws SQLException
    {
        checkState();

        PreparedStatement stmt = makeDelete(table, row, key);
        int numRows = stmt.executeUpdate();

        if(key.getType() == Key.PRIMARY_KEY)
        {
            if(numRows == 0)
                throw new SQLException("[xmldbms] Row to be deleted is not present in table.");
            else if(numRows > 1)
                throw new SQLException("[xmldbms] Primary key not unique. Multiple rows deleted!");
        }

        executedStatement();
    }

    /**
     * Delete rows from a given table.
     *
     * <p>The DELETE statement has the form:</p>
     *
     * <pre>
     *    SELECT FROM Table WHERE Key = ? AND &lt;where>
     * </pre>
     *
     * @param t The table to select from. Must not be null.
     * @param key The key to restrict with. May be null.
     * @param keyValue The value of the key.
     * @param where An additional where constraint. May be null.
     * @param paramColumns The columns corresponding to parameters in the where constraint.
     *    Null if there are no parameters.
     * @param paramValues The values of parameters in the where constraint. Null if there
     *    are no parameters.
     */
    public void delete(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues)
        throws SQLException
    {
        checkState();

        PreparedStatement stmt = makeDelete(table, key, keyValue, where, paramColumns, paramValues);
        int numRows = stmt.executeUpdate();

        if (key != null)
        {
           if(key.getType() == Key.PRIMARY_KEY)
           {
               if(numRows == 0)
                   throw new SQLException("[xmldbms] Row to be deleted is not present in table.");
               else if(numRows > 1)
                   throw new SQLException("[xmldbms] Primary key not unique. Multiple rows deleted!");
           }
        }

        executedStatement();
    }

    /**
     * Select rows from a given table.
     *
     * <p>The SELECT statement has the form:</p>
     *
     * <pre>
     *    SELECT * FROM Table WHERE Key = ? AND &lt;where> ORDER BY ?
     * </pre>
     *
     * @param t The table to select from. Must not be null.
     * @param key The key to restrict with.
     * @param keyValue The value of the key
     * @param where An additional where constraint. May be null.
     * @param order The sort information. May be null.
     * @param paramColumns The columns corresponding to parameters in the where constraint
     * @param paramValues The values of parameters in the where constraint
     * @return The result set.
     */
    public ResultSet select(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues, OrderInfo orderInfo)
       throws SQLException
    {
        checkState();

        PreparedStatement stmt = makeSelect(table, key, keyValue, where, paramColumns, paramValues, orderInfo);
        return stmt.executeQuery();
    }

    // ************************************************************************
    // Helper methods. Also used by base classes
    // ************************************************************************

    /**
     * Checks whether the DataHandler has been initialized.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public void checkState()
    {
        if (m_connection == null)
            throw new IllegalStateException("Invalid state. DataHandler has not been initialized.");
    }

    /** 
     * To be called after a statement that modifies the database
     * has been executed.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public void executedStatement()
    {
        if(m_commitMode == COMMIT_AFTERDOCUMENT)
        {
            m_dirtyConnection = true;
        }

        // For COMMIT_AFTERSTATEMENT we use auto commit
    }


    /**
     * Makes a SELECT statement.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public PreparedStatement makeSelect(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues, OrderInfo orderInfo)
        throws SQLException
    {
        // These can be cached. Use SQLStrings
        String sql = m_strings.getSelectWhere(table, key, where, orderInfo);

        // Make the SELECT statement
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        // Set the parameters
        int start = 0;
        if (key != null)
        {
           Column[] keyColumns = key.getColumns();
           Parameters.setParameters(stmt, 0, keyColumns, keyValue);
           start = keyColumns.length;
        }
        if (paramColumns != null)
        {
           Parameters.setParameters(stmt, start, paramColumns, paramValues);
        }

        return stmt;
    }

    /**
     * Makes an INSERT statement
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public PreparedStatement makeInsert(Table table, Row row)
        throws SQLException
    {
        // NOTE: The PreparedStatements returned cannot be cached, as the row
        // may have a different amount of valid values each time.

        Vector colVec = row.getColumnVectorFor(table);

        // If any of the database generated key values are null
        // remove from the insert list. Certain DBMS have problems
        // otherwise
        Column[] refreshCols = getRefreshCols(table);
        for(int i = 0; i < refreshCols.length; i++)
        {
            if(colVec.contains(refreshCols[i]) &&
               row.getColumnValue(refreshCols[i]) == null)
            {
                colVec.removeElement(refreshCols[i]);
            }
        }

        Column[] cols = new Column[colVec.size()];
        colVec.copyInto(cols);

        // Make the INSERT statement
        String sql = m_dml.getInsert(table, cols);
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        // Set the parameters
        Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

        return stmt;
    }

    /**
     * Makes an UPDATE statement.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public PreparedStatement makeUpdate(Table table, Row row, Column[] cols)
        throws SQLException
    { 
        // NOTE: The PreparedStatements returned cannot be cached, as the row
        // may have a different amount of valid values each time, or cols may be
        // different.

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
            
                colVec.removeElement(priCols[i]);
            }

            // Remove unique key columns. These should not be updated
            Enumeration e = table.getUniqueKeys();
            while(e.hasMoreElements())
            {
                Column[] keyCols = ((Key)e.nextElement()).getColumns();
                for(int i = 0; i < keyCols.length; i++)
                {
                    colVec.removeElement(keyCols[i]);
                }
            }

            cols = new Column[colVec.size()];
            colVec.copyInto(cols);
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


    /**
     * Makes a DELETE statement.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public PreparedStatement makeDelete(Table table, Row row, Key key)
        throws SQLException
    {
        if(key == null)
            key = table.getPrimaryKey();

        // These can be cached so use SQLStrings
        String sql = m_strings.getDelete(table, key);

        // Make the DELETE statement
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        Column[] cols = key.getColumns();
        // Set the parameters
        Parameters.setParameters(stmt, 0, cols, row.getColumnValues(cols));

        // Execute Statement
        return stmt;
    }

    /**
     * Makes a DELETE statement
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public PreparedStatement makeDelete(Table table, Key key, Object[] keyValue, String where, Column[] paramColumns, Object[] paramValues)
        throws SQLException
    {
        // These can be cached. Use SQLStrings
        String sql = m_strings.getDeleteWhere(table, key, where);

        // Make the DELETE statement
        PreparedStatement stmt = m_connection.prepareStatement(sql);

        // Set the parameters
        int start = 0;
        if (key != null)
        {
           Column[] keyColumns = key.getColumns();
           Parameters.setParameters(stmt, 0, keyColumns, keyValue);
           start = keyColumns.length;
        }
        if (paramColumns != null)
        {
           Parameters.setParameters(stmt, start, paramColumns, paramValues);
        }

        return stmt;
    }

    /**
     * Get the columns in a table that need refreshing
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public Column[] getRefreshCols(Table table)
    {
        if(m_refreshCols.contains(table))
            return (Column[])m_refreshCols.get(table);

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

        Column[] cols = new Column[colVec.size()];
        colVec.copyInto(cols);

        m_refreshCols.put(table, cols);

        return cols;
    }


    /** 
     * Creates a key for a single column.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public Key createColumnKey(String colName, int type)
    {
        Column[] keyCols = { Column.create(colName) };
        keyCols[0].setType(type);

        // Make a key out of it
        Key key = Key.createPrimaryKey(null);
        key.setColumns(keyCols);

        return key;
    }    
    

    /**
     * Retrieves a driver PreparedStatement from a 
     * possibly wrapped one.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public PreparedStatement getRawStatement(PreparedStatement stmt)
    {
        if(stmt instanceof SPPreparedStatement)
            return ((SPPreparedStatement)stmt).getUnderlyingStatement();
        else
            return stmt;
    }

    /**
     * Sets a column value in a Row.
     *
     * <p>For use by DataHandlerBase and child classes only.</p>
     */
    public void setColumnValue(Row row, Column column, Object val)
        throws SQLException
    {
        try
        {
            row.setColumnValue(column, ConvertObject.convertObject(val, column.getType(), column.getFormatter()));
        }
        catch(XMLMiddlewareException e)
        {
            throw new SQLException("[xmldbms]Conversion error: " + e.getMessage());
        }
    }
}   
