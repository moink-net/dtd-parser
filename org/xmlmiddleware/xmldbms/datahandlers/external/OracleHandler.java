package org.xmlmiddleware.xmldbms.datahandlers.external;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

// "IMPL:" Comments are implementors requirements or suggestions

/**
 * <p>DataHandler implementation for Oracle.</p>
 *
 * <p>Needs implementation by someone with access to an Oracle server and
 * JDBC coding skills.</p>
 *
 * @author Need Implementor
 * @version 2.0
 */
public class OracleHandler
    extends DataHandlerBase
{

    /** 
     * Creates a OracleHandler. 
     */
    public OracleHandler()
    {
        super();
    }


    /**
     * Inserts a row into the table. Will refresh any key columns needed. 
     *
     * @param table Table to insert into.
     * @param row Row to insert.
     */
	public void insert(Table table, Row row)
        throws SQLException
    {     
        checkState();

        // Make and execute the statement...
        PreparedStatement stmt = makeInsert(table, row);
        int numRows = stmt.executeUpdate();

        // This is a list of columns you need to retrieve from the database
        Column[] refreshCols = getRefreshCols(table);

        if(refreshCols.length > 0)
        {
            // IMPL: Test that this is actually a statement from Oracle, like so:
            // if(!(stmt instanceof oracle.whatever.Statement))
            //    throw new SQLException("[xmldbms] Invalid DataSource / DataHandler combination.");

            // IMPL: Retrieve all columns in 'refreshCols' here, somehow.

            // IMPL: Set columns on 'row' like so:
            // ('col' is the column you're setting, 'obj' is the value as an Object)
            //
            //      setColumnValue(row, col, obj);

            throw new SQLException("[xmldbms] Oracle support not implemented. Looking for implementor.");
        }
    }

}
