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
// Changes from version 1.0: None
// Changes from version 1.01:
// * Renamed from KeyGeneratorImpl to HighLow
// * Added initialize(Props) and close() methods for new KeyGenerator interface
// * Deleted init() method and removed Connection as argument to constructor
// * Removed code to set transaction isolation

package org.xmlmiddleware.xmldbms.keygenerators;

import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.tools.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Implementation of KeyGenerator using a high-low scheme.
 *
 * <p>This key generator assumes that there is a table in the database
 * with an INTEGER column. The name of the table and column can be
 * specified as part of the initialization process. If no names are given,
 * the table name is assumed to be XMLDBMSKey and the column name is
 * assumed to be HighKey.</p>
 *
 * <p>HighLow constructs keys
 * from a high key value and a low key value: the high key value forms the
 * upper 24 bits of the key and the low key value forms the lower 8 bits of
 * the key. The high key value is retrieved from the high key table, which
 * is then incremented by 1. The low key value is initialized to 0 and
 * incremented by 1 each time generateKey() is called; when it reaches 255,
 * a new high key value is retrieved. Thus, HighLow can generate
 * 256 unique key values with a single database access.</p>
 *
 * <p>For example, the following code instantiates HighLow and
 * passes it to DOMToDBMS, which uses it to generate keys.</p>
 *
 * <pre>
 *    // Instantiate and initialize HighLow.
 *    <br />
 *    KeyGenerator highLow = new HighLow();
 *    Properties p = new Properties();
 *    p.put(XMLDBMSProps.DATASOURCECLASS, "org.xmlmiddleware.db.JDBC1DataSource");
 *    p.put(XMLDBMSProps.DRIVER, "sun.jdbc.odbc.JdbcOdbcDriver");
 *    p.put(XMLDBMSProps.URL, "jdbc:odbc:xmldbms");
 *    p.put(XMLDBMSProps.USER, "ron");
 *    p.put(XMLDBMSProps.PASSWORD, "ronpwd");
 *    highLow.initialize(p);<br />
 *
 *    // Pass the key generator to DOMToDBMS. Name matches name used in map.
 *    <br />
 *    domToDBMS = new DOMToDBMS();
 *    domToDBMS.addKeyGenerator("MyDatabase", highLow);
 * </pre>
 *
 * @author Ronald Bourret
 * @author Adam Flinton
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.tools.XMLDBMSProps
 */

public class HighLow implements KeyGenerator
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private int        highKeyValue = -1, lowKeyValue = 0xFF;
   private Connection conn = null;
   private String     selectString, updateString;

   //**************************************************************************
   // Constants
   //**************************************************************************

   /* Name of the high key catalog. If this is not present, no catalog is used.*/
   public static final String HIGHLOWCATALOG = "HighLowCatalog";

   /* Name of the high key schema. If this is not present, no schema is used.*/
   public static final String HIGHLOWSCHEMA = "HighLowSchema";

   /* Name of the high key table. If this is not present, XMLDBMSKey is used.*/
   public static final String HIGHLOWTABLE = "HighLowTable";

   /* Name of the high key column. If this is not present, HighKey is used.*/
   public static final String HIGHLOWCOLUMN = "HighLowColumn";

   private static final String PERIOD = ".";
   private static final String XMLDBMSKEY = "XMLDBMSKey";
   private static final String HIGHKEY = "HighKey";
   private static final String JDBC1DATASOURCE = "org.xmlmiddleware.db.JDBC1DataSource";
   private static final String JDBC2DATASOURCE = "org.xmlmiddleware.db.JDBC2DataSource";

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
   * Construct a new HighLow object.
   */
   public HighLow()
   {
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Initialize the HighLow object.
    *
    * <p>Applications must call this method before passing a HighLow
    * object to DOMToDBMS. The following properties are accepted by this
    * method:
    *
    * <ul>
    * <li>DataSourceClass: The JDBC 2.0 data source to use.
    *    org.xmlmiddleware.db.JDBC1DataSource or org.xmlmiddleware.db.JDBC2DataSource.</p>
    * <li>User: Database user name. Optional.</li>
    * <li>Password: Database password. Optional.</li>
    * <li>HighLowCatalog: Name of the high key catalog. If this is not present,
    *     no catalog is used.</li>
    * <li>HighLowSchema: Name of the high key schema. If this is not present,
    *     no schema is used.</li>
    * <li>HighLowTable: Name of the high key table. If this is not present,
    *     XMLDBMSKey is used.</li>
    * <li>HighLowColumn: Name of the high key table. If this is not present,
    *     HighKey is used.</li>
    * </ul>
    *
    * <p>When the data source class is org.xmlmiddleware.db.JDBC1DataSource, the
    * following properties are required:</p>
    *
    * <ul>
    * <li>Driver: Name of the JDBC driver class to use.</li>
    * <li>URL: URL of the database containing the high key table.</li>
    * </ul>
    *
    * <p>When the data source class is org.xmlmiddleware.db.JDBC2DataSource, the
    * following properties are required:</p>
    *
    * <ul>
    * <li>DBInitialContext: Name of the JNDI Context in which to create
    *     the JDBC 2.0 DataSource.</li>
    * <li>JNDILookupName: JNDI lookup name of the data source.</li>
    * </ul>
    *
    * <p>Constants for all of these properties except for the catalog, schema, table,
    * and column name are found in the XMLDBMSProps class. For applications driven
    * by a single set of properties, such as when Transfer is used in command-line
    * mode, this means that the high-low key table must be in the same database as
    * the data.</p>
    *
    * @param props A properties object containing the above properties.
    * @param suffix A numeric suffix to be added to property names. If this
    *     is 0, no suffix is added.
    *
    * @exception XMLMiddlewareException Thrown if an error occurred initializing the
    *  database. Usually, this will be an error setting the auto-commit or
    *  transaction isolation level.
    */
   public void initialize(Properties props, int suffix) throws XMLMiddlewareException
   {
      DatabaseMetaData dbm;

      try
      {
         // Set the database properties and names to use.

         setDatabaseProperties(props, suffix);
         dbm = conn.getMetaData();
         setDBNames(dbm, props, suffix);

         // Set auto-commit to false.

         if (conn.getAutoCommit())
         {
            conn.setAutoCommit(false);
         }

         // Get the initial high key value.

         getHighKey();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Generates a key.
    *
    * <p>This method is called by DOMToDBMS. Applications using DOMToDBMS
    * do not need to call this method.</p>
    *
    * @return The key as an array of Objects.
    * @exception XMLMiddlewareException Thrown if the high key table is not initialized
    *                         or if a SQLException occurs.
    */

   public Object[] generateKey() throws XMLMiddlewareException
   {
      Object[] pk = new Object[1];

      if (highKeyValue == -1)
         throw new IllegalStateException("Key generator HighLow not initialized.");

      if (lowKeyValue == 0xFF)
      {
         try
         {
            getHighKey();
         }
         catch (SQLException e)
         {
            throw new XMLMiddlewareException(e);
         }
      }

      pk[0] = new Integer(highKeyValue + lowKeyValue++);
      return pk;
   }

   /**
    * Closes the database connection used by HighLow.
    *
    * <p>Applications must call this method before releasing a HighLow object.</p>
    *
    * @exception XMLMiddlewareException Thrown if a SQLException occurs.
    */
   public void close() throws XMLMiddlewareException
   {
      try
      {
         if (conn != null) conn.close();
      }
      catch (SQLException e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void getHighKey() throws SQLException
   {
      Statement select, update;
      ResultSet rs;

      // Create the statements.

      select = conn.createStatement();
      update = conn.createStatement();

      // Increment the high key value.

      update.executeUpdate(updateString);

      // Get the new high key value.

      rs = select.executeQuery(selectString);
      if (!rs.next())
      {
         throw new IllegalStateException("High key table (XMLDBMSKey) not initialized. A single row with a value of 0 in the high key column (HighKey) is needed.");
      }
      highKeyValue = rs.getInt(1);

      // Check if the high key value will cause an overflow, then shift.
      if (highKeyValue > 0x00FFFFFF)
      {
         throw new IllegalStateException("High key value has exceeded maximum.");
      }
      else
      {
         highKeyValue = highKeyValue << 8;
      }

      // Set the low key value.

      lowKeyValue = 0;

      // Close the statements and commit the transaction.

      update.close();
      rs.close();
      select.close();
      conn.commit();
   }

   private void setDatabaseProperties(Properties props, int suffix)
      throws SQLException
   {
      String     dataSourceClass, user, password;
      DataSource dataSource;

      // We use class-specific properties if they exist. Otherwise, use the
      // XMLDBMSProps properties. The former allows the key generator to use a
      // different database than the data, while the latter is a convenience for the
      // common situation where the key table is on the same database as the data.

      // Create the DataSource object.

      dataSourceClass = props.getProperty(getPropName(XMLDBMSProps.DATASOURCECLASS, suffix));
      if (dataSourceClass == null)
         throw new IllegalArgumentException("You must specify a DataSourceClass property");

      // Currently, we only recognize the JDBC1DataSource and
      // JDBC2DataSource classes. In the future, we should instantiate
      // other data source classes through Reflection.

      if (dataSourceClass.equals(JDBC1DATASOURCE))
      {
         dataSource = createJDBC1DataSource(props, suffix);
      }
      else if (dataSourceClass.equals(JDBC2DATASOURCE))
      {
         dataSource = createJDBC2DataSource(props, suffix);
      }
      else
      {
         throw new IllegalArgumentException("The data source class must be org.xmlmiddleware.db.JDBC1DataSource or org.xmlmiddleware.db.JDBC2DataSource.");
      }

      user = props.getProperty(getPropName(XMLDBMSProps.USER, suffix));
      password = props.getProperty(getPropName(XMLDBMSProps.PASSWORD, suffix));

      this.conn = (user != null) ? dataSource.getConnection(user, password) :
                                   dataSource.getConnection();
   }

   private DataSource createJDBC1DataSource(Properties props, int suffix)
   {
      String driver, url;

      // Construct the name of the driver property and get it now.

      driver = props.getProperty(getPropName(XMLDBMSProps.DRIVER, suffix));
      if (driver == null)
         throw new IllegalArgumentException("You must specify the driver when using a JDBC1DataSource.");

      // Construct the name of the URL property and get it now.

      url = props.getProperty(getPropName(XMLDBMSProps.URL, suffix));
      if (url == null)
         throw new IllegalArgumentException("You must specify the URL when using a JDBC1DataSource.");

      // Create the DataSource

      return new JDBC1DataSource(driver, url);
   }

   private DataSource createJDBC2DataSource(Properties props, int suffix)
   {
      String jndiContext, jndiLookupName, prop;

      // Construct the name of the JNDI context property and get it now.

      jndiContext = props.getProperty(getPropName(XMLDBMSProps.JNDICONTEXT, suffix));
      if (jndiContext == null)
         throw new IllegalArgumentException("You must specify the JNDI context when using a JDBC2DataSource.");

      // Construct the name of the JNDI lookup name property and get it now.

      jndiLookupName = props.getProperty(getPropName(XMLDBMSProps.JNDILOOKUPNAME, suffix));
      if (jndiLookupName == null)
         throw new IllegalArgumentException("You must specify the JNDI lookup name of the data source when using a JDBC2DataSource.");

      // Create the DataSource

      return new JDBC2DataSource(jndiContext, jndiLookupName);
   }

   private void setDBNames(DatabaseMetaData dbm, Properties props, int suffix)
      throws SQLException
   {
      String catalog, schema, table, column, quote;

      // Get the catalog, schema, table, and column names.

      catalog = props.getProperty(getPropName(HIGHLOWCATALOG, suffix));
      schema = props.getProperty(getPropName(HIGHLOWSCHEMA, suffix));
      table = props.getProperty(getPropName(HIGHLOWTABLE, suffix), XMLDBMSKEY);
      column = props.getProperty(getPropName(HIGHLOWCOLUMN, suffix), HIGHKEY);

      // Get the identifier quote character

      quote = dbm.getIdentifierQuoteString();

      // Build the qualified table and column names

      column = quote + column + quote;
      table = quote + table + quote;

      if (schema != null)
      {
         table = quote + schema + quote + PERIOD + table;
      }

      if (catalog != null)
      {
         if (dbm.isCatalogAtStart() == true)
         {
            table = quote + catalog + quote + dbm.getCatalogSeparator() + table;
         }
         else
         {
            table = table + dbm.getCatalogSeparator() + quote + catalog + quote;
         }
      }

      // Build the SELECT and UPDATE strings

      selectString = "SELECT " + column + " FROM " + table;
      updateString = "UPDATE " + table + " SET " + column + " = " + column + " + 1";
   }

   private String getPropName(String name, int suffix)
   {
      return (suffix == 0) ? name : name + String.valueOf(suffix);
   }
}