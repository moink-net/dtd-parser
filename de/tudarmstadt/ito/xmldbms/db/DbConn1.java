// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.db;

import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * An implementation of the DbConn interface for JDBC 1.0.
 *
 * @author Adam Flinton
 * @version 1.1
 */

public class DbConn1 implements DbConn
{
    private String user, password, url, Driver;

    /**
     * Get a JDBC Connection.
     *
     * <p>setDB must be called before this method is called.</p>
     *
     * @return The Connection.
     */
    public java.sql.Connection getConn() throws java.sql.SQLException
    {
        Connection conn = null;
        conn = DriverManager.getConnection(url, user, password);
        return conn;
    }

    /**
     * Specify the properties needed to get a connection.
     *
    	* <p>This method must be called before calling getConn().
    	* The following properties are accepted:</p>
    	*
    	* <ul>
    	* <li>Driver: Name of the JDBC driver class to use. Required.</li>
    	* <li>URL: URL of the database. Required.</li>
    	* <li>User: Database user name. Depends on database.</li>
    	* <li>Password: Database password. Depends on database.</li>
    	* </ul>
    	*
    	* @param props A Properties object containing the above properties.
     */
    public void setDB(java.util.Properties props)
        throws java.lang.ClassNotFoundException
    {
        Driver = props.getProperty(DBProps.DRIVER);
        url = props.getProperty(DBProps.URL);
        user = props.getProperty(DBProps.USER);
        password = props.getProperty(DBProps.PASSWORD);
        if (Driver == null)
            {
            throw new IllegalArgumentException("Driver property not set.");
        }
        else
            {
            // Load the driver.
            Class.forName(Driver);
        }
    }
}