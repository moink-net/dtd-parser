// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.01
// Changes from version 1.0: None

package de.tudarmstadt.ito.xmldbms.helpers;

import de.tudarmstadt.ito.xmldbms.KeyGenerator;
import de.tudarmstadt.ito.xmldbms.KeyException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.DriverManager;import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;/**
 * Default KeyGenerator implementation.
 *
 * <P>This key generator assumes that there is a table named XMLDBMSKey
 * in the database with an INTEGER column named HighKey. It constructs keys
 * from a high key value and a low key value: the high key value forms the
 * upper 24 bits of the key and the low key value forms the lower 8 bits of
 * the key. The high key value is retrieved from the XMLDBMSKey table, which
 * is then incremented by 1. The low key value is initialized to 0 and
 * incremented by 1 each time generateKey() is called; when it reaches 255,
 * a new high key value is retrieved. Thus, KeyGeneratorImpl can generate
 * 256 unique key values with a single database access.</P>
 *
 * <P>For example, the following code instantiates KeyGeneratorImpl and
 * passes it to DOMToDBMS, which uses it to generate keys.</P>
 * <PRE>
 *    // Instantiate KeyGeneratorImpl and initialize it with a Connection.
 *    KeyGenerator keyGenerator = new KeyGeneratorImpl();
 *    keyGenerator.initialize(conn);<BR />
 *
 *    // Pass the key generator to DOMToDBMS.
 *    domToDBMS = new DOMToDBMS(map, keyGenerator, null);
 * </PRE>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.01
 */

public class KeyGeneratorImpl implements KeyGenerator
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private int        highKeyValue = -1, lowKeyValue = 0xFF;
   private Connection conn = null;





   public Object[] generateKey() throws KeyException
   {
	  Object[] pk = new Object[1];

	  if (highKeyValue == -1)
		 throw new KeyException("KeyGeneratorImpl not initialized.");

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

	  update.executeUpdate("UPDATE XMLDBMSKey SET HighKey = HighKey + 1");

	  // Get the new high key value.

	  rs = select.executeQuery("SELECT HighKey FROM XMLDBMSKey");
	  if (!rs.next())
	  {
		 throw new KeyException("XMLDBMSKey table not initialized. A single row with a value of 0 in the HighKey column is needed.");
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
   private String          url, user, password;   private String          url, user, password;   private String          url, user, password;   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
	* Construct a new KeyGeneratorImpl object.
	*
	* @param conn A connection to the database containing the XMLDBMSKey table.
	*  Note that this connection must be different from the connection used by
	*  the Map object. The reason for this is that each commits transactions at
	*  different times and using the same connection for both objects would lead
	*  to statements being committed prematurely.
	*/
   public KeyGeneratorImpl()
   {

   }      /**
 * Insert the method's description here.
 * Creation date: (01/04/01 16:18:05)
 */
public void close() throws java.lang.Exception {
	
	conn.close();}   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Initialize the KeyGeneratorImpl object.
	*
	* @exception KeyException Thrown if an error occurred initializing the 
	*  database. Usually, this will be an error setting the auto-commit or
	*  transaction isolation level.
	*/
   public void init(java.util.Properties props) throws KeyException, java.lang.ClassNotFoundException,java.sql.SQLException
   {	   
	  DatabaseMetaData dbm;
	  setDatabaseProperties(props);
	  this.conn = DriverManager.getConnection(url, user, password);


	  
	  // Set auto-commit to false and set the highest supported transaction
	  // isolation level. This helps in locking the database when accessing
	  // the XMLDBMSKey value.

	  try
	  {
//         conn.setAutoCommit(false);
		 dbm = conn.getMetaData();
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
	  catch (SQLException e)
	  {
		 throw new KeyException(e.getMessage());
	  }
   }                           //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Initialize the KeyGeneratorImpl object.
	*
	* @exception KeyException Thrown if an error occurred initializing the 
	*  database. Usually, this will be an error setting the auto-commit or
	*  transaction isolation level.
	*/
   public void initialize(java.util.Properties props) throws java.lang.Exception,KeyException
   {	   
	  init(props);
   }                                 // ************************************************************************
   // Public methods
   // ************************************************************************

   public void setDatabaseProperties(java.util.Properties props) throws java.lang.ClassNotFoundException
   {
	  String driver;

	  // Get the database properties;
	  driver = (String)props.getProperty(XMLDBMSProps.DRIVER);
	  if (driver == null)
		 throw new IllegalArgumentException("No Driver property set.");
	  url = (String)props.getProperty(XMLDBMSProps.URL);
	  if (url == null)
		 throw new IllegalArgumentException("No URL property set.");
	  user = (String)props.getProperty(XMLDBMSProps.USER);
	  password = (String)props.getProperty(XMLDBMSProps.PASSWORD);

	  // Load the driver.
	  Class.forName(driver);
   }               }