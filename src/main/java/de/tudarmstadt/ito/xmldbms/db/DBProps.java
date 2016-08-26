// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001
//
// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.db;

/**
 * Properties constants for DbConn implementations.
 *
 * @author Adam Flinton
 * @version 1.1
 */
public class DBProps
{
    /** JDBC driver class name (JDBC 1.0) */
    public static String DRIVER = "Driver";

    /** Database URL (JDBC 1.0) */
    public static String URL = "URL";

    /**  Database user name (JDBC 1.0 and 2.0) */
    public static String USER = "User";

    /**  Database password (JDBC 1.0 and 2.0) */
    public static String PASSWORD = "Password";

    /** Logical name of the database (JDBC 2.0) */
    public static String DATASOURCE = "DataSource";

    /** Name of the JNDI Context in which to create the JDBC 2.0 DataSource (JDBC 2.0)*/
    public static String DBINITIALCONTEXT = "DBInitialContext";

    /** JDBC level. Must be either 1 or 2. */
    public static String JDBCLEVEL = "JDBCLevel";
}