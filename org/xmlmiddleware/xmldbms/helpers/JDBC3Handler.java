package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * <p>DataHandler implementation for JDBC3 drivers. Incomplete
 * implementation as JDBC3 has not been finalized yet.</p>
 *
 * <p>Database generated keys are retrieved using the method
 * Statement.getGeneratedKeys(). </p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public class JDBC3Handler
    extends DataHandlerBase
{
    /** 
     * Creates a JDBC3Handler.
     *
     * @param dataSource The Datasource to retrive connections from.
     * @param user Login name for dataSource.
     * @param password Password for dataSource.
     */
    JDBC3Handler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);    
    }


    /**
     * Inserts a row into the table. Refreshes any key columns needed. Does this
     * via Statement.getGeneratedKeys(). 
     *
     * @param table Table to insert into.
     * @param row Row to insert.
     */
	public void insert(Table table, Row row)
        throws SQLException
    {   
        PreparedStatement stmt = makeInsert(table, row);

        // IMPL: Statement.executeUpdate has an argument which tells it
        // to return generated keys. PreparedStatement.executeUpdate 
        // does not. Submitted as a bug at java.sun.com for JDK V1.4
        int numRows = stmt.executeUpdate();

        executedStatement();

        Column[] refreshCols = getRefreshCols(table);
        
        if(refreshCols.length > 0)
        {
            ResultSet rsGen = stmt.getGeneratedKeys();

            // IMPL: The format of the ResultSet has not been defined
            // just yet. At least I can find no docs on it.

            // IMPL: Read keys from rsGen
            
            // IMPL: If there are other keys besides those in rsGen, 
            // make a SELECT statement and get the rest from there.
            
            throw new SQLException("[xmldbms] Not implemented yet!");
        }
    }

}
