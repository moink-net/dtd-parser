// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.db;

import java.util.Properties;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A generic interface for getting database connections.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public interface DbConn
{
    /**
     * Get a JDBC Connection.
     *
     * <p>setDB must be called before this method is called.</p>
     *
     * @return The Connection.
     */
    public Connection getConn() throws SQLException;

    /**
     * Specify the properties needed to get a connection.
     *
    	* <p>This method must be called before calling getConn().</p>
      *
    	* @param props A Properties object containing the necessary properties.
     */
    public void setDB(Properties prop) throws java.lang.Exception;
}