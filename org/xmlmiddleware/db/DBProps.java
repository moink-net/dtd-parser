// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.1: None
// Changes from version 1.01: New in 1.1

package org.xmlmiddleware.db;

/**
 * Properties constants for DataSource implementations.
 *
 * @author Adam Flinton
 * @version 2.0
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