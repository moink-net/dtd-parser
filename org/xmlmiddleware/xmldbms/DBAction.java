package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.sql.*;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * Interface for abstracting database access. 
 *
 */
public interface DBAction  
{
	public void insert(Table table, Row row, boolean soft)
        throws SQLException;

    public void update(Table table, Row row)
        throws SQLException;

    public void updateOrInsert(Table table, Row row)
        throws SQLException;

    public void delete(Table table, Row row, boolean soft)
        throws SQLException;

}
