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
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;

import java.lang.ClassNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import de.tudarmstadt.ito.xmldbms.db.*;

/**
 * Implementation of KeyGenerator using a high-low scheme.
 *
 * <P>This key generator assumes that there is a table named XMLDBMSKey
 * in the database with an INTEGER column named HighKey. It constructs keys
 * from a high key value and a low key value: the high key value forms the
 * upper 24 bits of the key and the low key value forms the lower 8 bits of
 * the key. The high key value is retrieved from the XMLDBMSKey table, which
 * is then incremented by 1. The low key value is initialized to 0 and
 * incremented by 1 each time generateKey() is called; when it reaches 255,
 * a new high key value is retrieved. Thus, KeyGeneratorHighLow can generate
 * 256 unique key values with a single database access.</P>
 *
 * <P>For example, the following code instantiates KeyGeneratorHighLow and
 * passes it to DOMToDBMS, which uses it to generate keys.</P>
 * <PRE>
 *    // Instantiate KeyGeneratorHighLow and initialize it with a Connection.
 *    KeyGenerator keyGenerator = new KeyGeneratorHighLow();
 *    keyGenerator.initialize(conn);<BR />
 *
 *    // Pass the key generator to DOMToDBMS.
 *    domToDBMS = new DOMToDBMS(map, keyGenerator, null);
 * </PRE>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class KeyGeneratorArm implements KeyGenerator
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private int        highKeyValue = -1, lowKeyValue = 0xFF;
   private Connection conn = null;
   private String     url, user, password,table,schema,cat,sep,fulltable;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
	* Construct a new KeyGeneratorHighLow object.
	*/

   public KeyGeneratorArm()
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
	* method:</p>
	*
	* <ul>
	* <li>Driver: Name of the JDBC driver class to use. Required.</li>
	* <li>URL: URL of the database containing the XMLDBMSKey table. Required.</li>
	* <li>User: Database user name. Optional.</li>
	* <li>Password: Database password. Optional.</li>
	* </ul>
	*
	* @param props A properties object containing the above properties.
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
	   setName(props);
	   this.conn = dbConn.getConn();
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
	* @exception KeyException Thrown if the XMLDBMSKey table is not initialized
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

	 String Upd = "UPDATE " +fulltable +" SET HighKey = HighKey + 1";
	 String exQ = "SELECT HighKey FROM " +fulltable;
//	System.out.println("Update Statement = " +Upd);
//	 System.out.println("Execute Statement = " +exQ);

	 update.executeUpdate(Upd);
	 

	 // Get the new high key value.

	 rs = select.executeQuery(exQ);
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

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
	* Set the properties used to connect to the database.
	*
	* <p>This method must be called before transferring data between
	* an XML document and a database. The following properties are
	* accepted:</p>
	*
	* <ul>
	* <li>Driver: Name of the JDBC driver class to use. Required.</li>
	* <li>URL: URL of the database containing the XMLDBMSKey table. Required.</li>
	* <li>User: Database user name. Optional.</li>
	* <li>Password: Database password. Optional.</li>
	* </ul>
	*
	* @param props A Properties object containing the above properties.
	* Changed by Adam Flinton 08/04/2001. Now the method evaluates the JDBC level to work out
	* which JDBC level to use (presently 1 or 2). Level 2 requires JNDI & Javax.sql so to compensate
	* for the possibility of level 1 useage instantiation is used.
	* If & when JDBC level 3 comes along shoving it in as well should be easy.
	*/

   public void setDatabaseProperties(Properties props)
	 throws ClassNotFoundException, IllegalAccessException, InstantiationException, java.lang.Exception

   {
	   int i = 0;
	String JDBC = props.getProperty(XMLDBMSProps.JDBCLEVEL);
	if (JDBC == null) {i = 1; }

	else {
	try {
		i = Integer.parseInt(JDBC.trim());
	}
	catch (NumberFormatException nfe)
	{ System.out.println ("JDBC Level MUST be a number " + nfe.getMessage());
		//For safety try resorting to JDBC level 1
		i = 1;	
	}

	}	
	switch (i) {
		case 1: dbConn = (DbConn)instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn1"); break;
		case 2: dbConn = (DbConn)instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn2"); break;
	}
	dbConn.setDB(props);
	
   }                                                      

   private DbConn			dbConn;

   private Object instantiateClass(String className)
	  throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
	 if (className == null) return null;
	 return Class.forName(className).newInstance();
   }            

/**
 * Insert the method's description here.
 * Creation date: (19/04/01 13:56:08)
 */
public void setName(Properties props) {

	table = "XMLDBMSKey";
	sep = ".";
	schema = null;
	cat = null;

	
	// Set the Table/Schema/Catalog names if different
	if ((String)props.getProperty(XMLDBMSProps.KEYGENTABLE) != null)
	{table = (String)props.getProperty(XMLDBMSProps.KEYGENTABLE);}
	
	if ((String)props.getProperty(XMLDBMSProps.KEYGENSCHEMA) != null)
	{schema = (String)props.getProperty(XMLDBMSProps.KEYGENSCHEMA);}
	
	if ((String)props.getProperty(XMLDBMSProps.KEYGENCAT) != null)
	{cat = (String)props.getProperty(XMLDBMSProps.KEYGENCAT);}

	if ((String)props.getProperty(XMLDBMSProps.KEYGENSEP) != null)
	{sep = (String)props.getProperty(XMLDBMSProps.KEYGENSEP);}	


	fulltable = table; 

	//System.out.println("Table1 = " +fulltable);
	if (schema != null)
	{fulltable = schema + sep +fulltable;}

	if (cat != null)
	{fulltable = cat + sep +fulltable;}	

	//System.out.println("Table2 = " +fulltable);
	
	}
}