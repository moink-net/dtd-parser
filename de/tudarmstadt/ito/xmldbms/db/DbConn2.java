// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.db;

import java.util.Hashtable;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.*;
import javax.naming.*;

/**
 * An implementation of the DbConn interface for JDBC 2.0.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class DbConn2 implements DbConn
{
    private String user, password, InitContext, Data_Source;
    private DataSource ds;

    /**
     * Get a JDBC Connection.
     *
     * <p>setDB must be called before this method is called.</p>
     *
     * @return The Connection.
     */
    public java.sql.Connection getConn() throws java.sql.SQLException
    {
        Connection conn = ds.getConnection(user, password);
        return conn;
    }

    /**
     * Specify the properties needed to get a connection.
     *
    	* <p>This method must be called before calling getConn().
    	* The following properties are accepted:</p>
    	*
    	* <ul>
    	* <li>DBInitialContext: Name of the JNDI Context in which to create
      *     the JDBC 2.0 DataSource. Required.</li>
    	* <li>DataSource: Logical name of the database. Required.</li>
    	* <li>User: Database user name. Depends on database.</li>
    	* <li>Password: Database password. Depends on database.</li>
    	* </ul>
      *
    	* @param props A Properties object containing the above properties.
     */
    public void setDB(java.util.Properties props)
        throws javax.naming.NamingException
    {
        InitContext = props.getProperty(DBProps.DBINITIALCONTEXT);
        Data_Source = props.getProperty(DBProps.DATASOURCE);
        user = props.getProperty(DBProps.USER);
        password = props.getProperty(DBProps.PASSWORD);
        if (InitContext == null)
            {
            throw new IllegalArgumentException("DBInitialContext property not set.");
        }
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, InitContext.trim());
        Context ctx = new InitialContext(env);
        ds = (DataSource) ctx.lookup(Data_Source);
    }
}