// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01:
// * Renamed from KeyGeneratorImpl to KeyGeneratorHighLow
// * Added initialize(Props) and close() methods for new KeyGenerator interface
// * Deleted init() method and removed Connection as argument to constructor

package de.tudarmstadt.ito.xmldbms.helpers;

import de.tudarmstadt.ito.xmldbms.KeyGenerator;
import de.tudarmstadt.ito.xmldbms.KeyException;
import de.tudarmstadt.ito.xmldbms.db.*;

import java.lang.ClassNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Implementation of KeyGenerator using a high-low scheme.
 *
 * <p>This key generator assumes that there is a table in the database
 * with an INTEGER column. The name of the table and column can be
 * specified as part of the initialization process. If no names are given,
 * the table name is assumed to be XMLDBMSKey and the column name is
 * assumed to be HighKey.</p>
 *
 * <p>KeyGeneratorHighLow constructs keys
 * from a high key value and a low key value: the high key value forms the
 * upper 24 bits of the key and the low key value forms the lower 8 bits of
 * the key. The high key value is retrieved from the high key table, which
 * is then incremented by 1. The low key value is initialized to 0 and
 * incremented by 1 each time generateKey() is called; when it reaches 255,
 * a new high key value is retrieved. Thus, KeyGeneratorHighLow can generate
 * 256 unique key values with a single database access.</p>
 *
 * <p>For example, the following code instantiates KeyGeneratorHighLow and
 * passes it to DOMToDBMS, which uses it to generate keys.</p>
 * <pre>
 *    // Instantiate KeyGeneratorHighLow and initialize it with a Connection.
 *    KeyGenerator keyGenerator = new KeyGeneratorHighLow();
 *    Properties p = new Properties();
 *    p.put(HighLowProps.HIGHLOWDRIVER, "sun.jdbc.odbc.JdbcOdbcDriver");
 *    p.put(HighLowProps.HIGHLOWURL, "jdbc:odbc:xmldbms");
 *    p.put(HighLowProps.HIGHLOWUSER, "ron");
 *    p.put(HighLowProps.HIGHLOWPASSWORD, "ronpwd");
 *    keyGenerator.initialize(p);<br />
 *
 *    // Pass the key generator to DOMToDBMS.
 *    domToDBMS = new DOMToDBMS(map, keyGenerator, null);
 * </pre>
 *
 * @author Ronald Bourret
 * @author Adam Flinton
 * @version 1.1
 * @see HighLowProps
 */

public class KeyGeneratorHighLow implements KeyGenerator
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private int        highKeyValue = -1, lowKeyValue = 0xFF;
   private Connection conn = null;
   private String     queryString, updateString;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
	* Construct a new KeyGeneratorHighLow object.
	*/

   public KeyGeneratorHighLow()
   {
   }      

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Initialize the KeyGeneratorHighLow object.
	*
	* <p>Applications must call this method before passing a KeyGeneratorHighLow
	* object to DOMToDBMS. The following properties are accepted by this
	* method for JDBC 1.0 drivers:</p>
	*
	* <ul>
	* <li>HighLowDriver: Name of the JDBC driver class to use. Required.</li>
	* <li>HighLowURL: URL of the database containing the high key table. Required.</li>
	* </ul>
	*
    	* <p>The following properties are accepted for JDBC 2.0 drivers:</p>
    	*
    	* <ul>
    	* <li>HighLowDBInitialContext: Name of the JNDI Context in which to create
      *     the JDBC 2.0 DataSource. Required.</li>
    	* <li>HighLowDataSource: Logical name of the database containing the
      *     high key table. Required.</li>
    	* </ul>
	*
    	* <p>The following properties are accepted for all drivers:</p>
	*
	* <ul>
	* <li>HighLowUser: Database user name. Depends on database.</li>
	* <li>HighLowPassword: Database password. Depends on database.</li>
      * <li>HighLowTable: Name of the high key table. Optional.</li>
      * <li>HighLowColumn: Name of the high key table. Optional.</li>
	* </ul>
    	*
	* @param props A properties object containing the above properties.
	*
	* @exception KeyException Thrown if an error occurred initializing the 
	*  database. Usually, this will be an error setting the auto-commit or
	*  transaction isolation level.
	*/

   public void initialize(Properties props) throws KeyException
   {
	 DatabaseMetaData dbm;
	 
	 // Set auto-commit to false and set the highest supported transaction
	 // isolation level. This helps in locking the database when accessing
	 // the XMLDBMSKey value.

	 try
	 {
 
	   setDatabaseProperties(props);
	   dbm = conn.getMetaData();
	   setDBNames(props, dbm);
	   
//         conn.setAutoCommit(false);
	   if (dbm.supportsTransactionIsolationLevel(
						   Connection.TRANSACTION_SERIALIZABLE))
	   {
		 conn.setTransactionIsolation(
						   Connection.TRANSACTION_SERIALIZABLE);
	   }
	   else if (dbm.supportsTransactionIsolationLevel(
						   Connection.TRANSACTION_REPEATABLE_READ))
	   {
		 conn.setTransactionIsolation(
						   Connection.TRANSACTION_REPEATABLE_READ);
	   }
	   else if (dbm.supportsTransactionIsolationLevel(
						   Connection.TRANSACTION_READ_COMMITTED))
	   {
		 conn.setTransactionIsolation(
						   Connection.TRANSACTION_READ_COMMITTED);
	   }
	   else if (dbm.supportsTransactionIsolationLevel(
						   Connection.TRANSACTION_READ_UNCOMMITTED))
	   {
		 conn.setTransactionIsolation(
						   Connection.TRANSACTION_READ_UNCOMMITTED);
	   }
	   
	   getHighKey();
	 }
	 catch (Exception e)
	 {
	   throw new KeyException(e.getMessage());
	 }
   }                     

   /**
	* Generates a key.
	*
	* <p>This method is called by DOMToDBMS. Applications using DOMToDBMS
	* do not need to call this method.</p>
	*
	* @return The key as an array of Objects.
	* @exception KeyException Thrown if the high key table is not initialized
	*                         or if a SQLException occurs.
	*/

   public Object[] generateKey() throws KeyException
   {
	 Object[] pk = new Object[1];

	 if (highKeyValue == -1)
	   throw new KeyException("KeyGeneratorHighLow not initialized.");

	 if (lowKeyValue == 0xFF)
	 {
	   try
	   {
		 getHighKey();
	   }
	   catch (SQLException e)
	   {
		 throw new KeyException(e.getMessage());
	   }
	 }

	 pk[0] = new Integer(highKeyValue + lowKeyValue++);
	 return pk;
   }      

   /**
	* Closes the database connection used by KeyGeneratorHighLow.
	*
	* <p>Applications must call this method before releasing a
	* KeyGeneratorHighLow object.</p>
	*
	* @exception KeyException Thrown if a SQLException occurs.
	*/

   public void close() throws KeyException
   {
	  try
	  {
		 if (conn != null) conn.close();
	  }
	  catch (SQLException e)
	  {
		 throw new KeyException(e.getMessage());
	  }
   }      

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void getHighKey() throws SQLException, KeyException
   {
	 Statement select, update;
	 ResultSet rs;

	 // Create the statements.

	 select = conn.createStatement();
	 update = conn.createStatement();

	 // Increment the high key value.

	 update.executeUpdate(updateString);

	 // Get the new high key value.

	 rs = select.executeQuery(queryString);
	 if (!rs.next())
	 {
	   throw new KeyException("High key table (XMLDBMSKey) not initialized. A single row with a value of 0 in the high key column (HighKey) is needed.");
	 }
	 highKeyValue = rs.getInt(1);

	 // Check if the high key value will cause an overflow, then shift.
	 if (highKeyValue > 0x00FFFFFF)
	 {
	   throw new KeyException("High key value has exceeded maximum.");
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

   private void setDatabaseProperties(Properties props)
	 throws ClassNotFoundException, IllegalAccessException, InstantiationException, java.lang.Exception

   {
      String value;
      DbConn dbConn;

	Properties props1 = new Properties();
	props1 = props;

      value = props.getProperty(HighLowProps.HIGHLOWDRIVER);
	if (value != null) {
	props1.put(DBProps.DRIVER, value);
	}

      value = props.getProperty(HighLowProps.HIGHLOWURL);
	if (value != null) {
	props1.put(DBProps.URL, value);
	}

      value = props.getProperty(HighLowProps.HIGHLOWUSER);
	if (value != null) {
	props1.put(DBProps.USER, value);
	}		

      value = props.getProperty(HighLowProps.HIGHLOWPASSWORD);
	if (value != null) {
	props1.put(DBProps.PASSWORD, value);
	}

      value = props.getProperty(HighLowProps.HIGHLOWJDBCLEVEL);
	if (value != null) {
	props1.put(DBProps.JDBCLEVEL, value);
	}

      value = props.getProperty(HighLowProps.HIGHLOWDATASOURCE);
	if (value != null) {
	props1.put(DBProps.DATASOURCE, value);
	}		

      value = props.getProperty(HighLowProps.HIGHLOWDBINITIALCONTEXT);
	if (value != null) {
	props1.put(DBProps.DBINITIALCONTEXT, value);
	}
	 
	int i = 1;
	String JDBC = props1.getProperty(DBProps.JDBCLEVEL);
	if (JDBC != null) {
		try {
			i = Integer.parseInt(JDBC.trim());
		}
		catch (NumberFormatException nfe)
		{
         		throw new IllegalArgumentException("Invalid value for JDBCLevel property: " + JDBC);
		}
	}	

	switch (i) {
		case 1: dbConn = (DbConn)instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn1"); break;
		case 2: dbConn = (DbConn)instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn2"); break;
            default: throw new IllegalArgumentException("Invalid value for JDBCLevel property: " + JDBC);
	}

	dbConn.setDB(props1);
	this.conn = dbConn.getConn();
   }                                                                                                   

   private Object instantiateClass(String className)
	  throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
	 return Class.forName(className).newInstance();
   }            

   private void setDBNames(Properties props, DatabaseMetaData dbm) throws java.sql.SQLException
   {
      String value, quote, table, schemaSeparator, schema, catalog, column;

	// Initialize the database names

	table = "XMLDBMSKey";
      column = "HighKey";
	schemaSeparator = ".";
	schema = null;
	catalog = null;

	// Set the Column/Table/Schema/Catalog names if different

      value = props.getProperty(HighLowProps.HIGHLOWCOLUMN);
      if (value != null) {column = value;}

      value = props.getProperty(HighLowProps.HIGHLOWTABLE);
      if (value != null) {table = value;}

      value = props.getProperty(HighLowProps.HIGHLOWSCHEMA);
      if (value != null) {schema = value;}

      value = props.getProperty(HighLowProps.HIGHLOWCATALOG);
      if (value != null) {catalog = value;}

      value = props.getProperty(HighLowProps.HIGHLOWSCHEMASEPARATOR);
      if (value != null) {schemaSeparator = value;}

      // Get the identifier quote character

      quote = dbm.getIdentifierQuoteString();

      // Build the qualified table and column names

      column = quote + column + quote;
	table = quote + table + quote;
		
	if (schema != null)
	{table = quote + schema + quote + schemaSeparator + table;}

	if (catalog != null)
	{
 		if (dbm.isCatalogAtStart() == true)
		{table = quote + catalog + quote + dbm.getCatalogSeparator() + table;}
		else
		{table = table + dbm.getCatalogSeparator() + quote + catalog + quote;}
	}

      // Build the SELECT and UPDATE strings

      queryString = "SELECT " + column + " FROM " + table;
      updateString = "UPDATE " + table + " SET " + column + " = " + column + " + 1";
   }
}