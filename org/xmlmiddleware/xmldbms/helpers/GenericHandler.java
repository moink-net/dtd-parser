package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;

class GenericHandler
    extends DataHandlerBase
{
    GenericHandler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);
    }


	public void insert(Table table, Row row)
        throws SQLException
    {     
        PreparedStatement stmt = makeInsert(table, row);
        int numRows = stmt.executeUpdate();

        Column[] refreshCols = getRefreshCols(table, row);

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

            if(!rs.next())
                throw new SQLException("[xmldbms] Couldn't retrieve inserted row due to changed values.");

            // Set them in the row
            for(int i = 0; i < refreshCols.length; i++)
                row.setColumnValue(refreshCols[i], rs.getObject(refreshCols[i].getName()));

            if(rs.next())
                throw new SQLException("[xmldbms] Couldn't retrieve inserted row due to multiple rows with identical values.");
        }

    }

}
