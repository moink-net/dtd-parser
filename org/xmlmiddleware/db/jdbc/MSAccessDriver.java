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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.db.jdbc;

import java.sql.*;
import java.util.*;

/**
 * Implements a JDBC driver for Microsoft Access.
 *
 * <p>The primary function of this driver is to implement functionality missing
 * from Microsoft's ODBC driver for Microsoft Access. In particular, this driver
 * does not implement some of the catalog functions needed by MapFactory_Database.
 * Thus, using MapFactory_Database with the JDBC-ODBC Bridge and this ODBC driver
 * results in a "driver not capable" error. This driver implements the missing
 * functionality directly on top of the Microsoft Access system tables.</p>
 *
 * <p>This driver uses a URL of the form:</p>
 *
 * <pre>
 *   jdbc:xmlmiddleware-access:&lt;bridge-URL>
 * </pre>
 *
 * <p>where &lt;bridge-URL> is the connection string used by the JDBC-ODBC Bridge. For
 * example, to use this driver with the ODBC data source named sales, you would
 * use the following URL:</p>
 *
 * <pre>
 *   jdbc:xmlmiddleware-access:jdbc:odbc:sales
 * </pre>
 *
 * <p>since the URL for the JDBC-ODBC bridge in this case is jdbc:odbc:sales.</p>
 *
 * <p>This driver has been tested with Microsoft Access 97.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class MSAccessDriver implements Driver
{
   //**************************************************************************
   // Static section
   //**************************************************************************

   static
   {
      // Register the driver in a static section. This allows the driver to be
      // used with a call to Class.forName().

      try
      {
         Driver d1 = new MSAccessDriver();
         DriverManager.registerDriver(d1);
      }
      catch (Exception e){}
   }

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Create a new MSAccessDriver.
    */
   public MSAccessDriver()
   {
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Connect to the database.
    *
    * @param url Must be of the form jdbc:xmlmiddleware-access:&lt;bridge-URL>
    * @param info Ignored.
    * @return The Connection.
    */
   public Connection connect(String url, Properties info) throws SQLException
   {
      // Remove the "jdbc:xmlmiddleware-access:" prefix.

      String realURL = url.substring(26);

      // Instantiate the bridge driver and get a connection.

      Driver bridge = new sun.jdbc.odbc.JdbcOdbcDriver();
      Connection conn = bridge.connect(realURL, info);

      // Return a connection that encapsulates the bridge driver's connection

      return new MSAccessConnection(conn);
   }

   /**
    * Whether the driver accepts a URL.
    *
    * @param url The URL
    * @return True if the URL is of the form jdbc:xmlmiddleware-access:&lt;bridge-URL>.
    *    Otherwise false.
    */
   public boolean acceptsURL(String url) throws SQLException
   {
      return url.startsWith("jdbc:xmlmiddleware-access:jdbc:odbc:");
   }

   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
   {
      return null;
   }

   public int getMajorVersion()
   {
      return 2;
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public boolean jdbcCompliant()
   {
      return true;
   }
}