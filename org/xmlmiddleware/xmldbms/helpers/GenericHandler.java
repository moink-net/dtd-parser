
package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * <p>DataHandler implementation for databases not directly supported. This 
 * includes the JDBC-ODBC bridge. </p>
 *
 * <p>The key values of inserted rows are retrieved by using all other (non key) 
 * values in the table in the WHERE clause of a SELECT statement. This is
 * touchy and may not always work. Caution is called for when using this 
 * DataHandler with keys generated from the database.</p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public class GenericHandler
    extends DataHandlerBase
{
    /** 
     * Creates a GenericHandler.
     *
     * @param dataSource The Datasource to retrive connections from.
     * @param user Login name for dataSource.
     * @param password Password for dataSource.
     */
    public GenericHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);
    }

    /**
     * Inserts a row into the table. Refreshes any key columns needed. Does this
     * by selecting all other values against the table. 
     *
     * @param table Table to insert into.
     * @param row Row to insert.
     */
	public void insert(Table table, Row row)
        throws SQLException
    {     
        PreparedStatement stmt = makeInsert(table, row);
        int numRows = stmt.executeUpdate();

        executedStatement();

        Column[] refreshCols = getRefreshCols(table);

        if(refreshCols.length > 0)
        {
            // Yes this is hokey! I'll say it again. It's H-O-K-E-Y! 
            // But it's the best we could come up with. 
            // Better ideas totally welcome!


            // Get a list of all the columns we don't need to refresh

            Vector colVec = row.getColumnVectorFor(table);

            for(int i = 0; i < refreshCols.length; i++)
                colVec.remove(refreshCols[i]);

            Column[] selCols = new Column[colVec.size()];
            colVec.copyInto(selCols);


            // SELECT using those columns as a WHERE clause
            Key key = Key.createUniqueKey(null);
            key.setColumns(selCols);

            String sql = m_dml.getSelect(table, key, refreshCols);
            PreparedStatement selStmt = m_connection.prepareStatement(sql);

            // Set the parameters
            Parameters.setParameters(selStmt, 0, selCols, row.getColumnValues(selCols));

            // Execute it 
            ResultSet rs = selStmt.executeQuery();

            // Make sure at least 1 row.
            if(!rs.next())
                throw new SQLException("[xmldbms] Couldn't retrieve inserted row due to changed values.");

            // Set them in the row
            for(int i = 0; i < refreshCols.length; i++)
                row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));

            // If more than one row then error.
            if(rs.next())
                throw new SQLException("[xmldbms] Couldn't retrieve inserted row due to multiple rows with identical values.");
        }

    }

}
