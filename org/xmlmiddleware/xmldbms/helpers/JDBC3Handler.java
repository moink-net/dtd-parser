package org.xmlmiddleware.xmldbms.helpers;

import java.lang.*;
import java.sql.*;
import javax.sql.*;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;


class JDBC3Handler
    extends DataHandlerBase
{
    protected final static String OIDNAME = "oid";

    JDBC3Handler(DataSource dataSource, String user, String password)
        throws SQLException
    {
        super(dataSource, user, password);

        // TODO!!        
    }


	public void insert(Table table, Row row)
        throws SQLException
    {     
        // TODO!!!
    }

}
