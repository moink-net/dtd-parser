// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms;


import de.tudarmstadt.ito.domutils.NameQualifier;
import de.tudarmstadt.ito.domutils.ParserUtils;
import de.tudarmstadt.ito.xmldbms.DBMSToDOM;
import de.tudarmstadt.ito.xmldbms.DOMToDBMS;
import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import de.tudarmstadt.ito.xmldbms.Map;
import de.tudarmstadt.ito.xmldbms.mapfactories.MapFactory_MapDocument;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;

import java.io.File;
import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.w3c.dom.Document;

import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;

import java.util.HashMap;
import de.tudarmstadt.ito.xmldbms.objectcache.ObjectCache;
import de.tudarmstadt.ito.xmldbms.db.DbConn;


import de.tudarmstadt.ito.xmldbms.tools.GetFileException;

/**
 * High level interface to XML-DBMS.
 *
 * <p>TransferEngine provides a high-level interface to XML-DBMS.
 * In its current form, it allows applications to transfer data between
 * an XML document and a database by providing only a map file name, an
 * XML file name, and database and parser information.</p>
 *
 * <p>Future versions of TransferEngine are likely to provide a
 * significantly different API -- more closely related to a JDBC driver
 * -- although they will probably continue to support simple data
 * transfer abilities as well. In addition, it is likely that
 * TransferEngine is where connection pooling, etc. will be handled.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see de.tudarmstadt.ito.xmldbms.Transfer
 */

public class TransferEngine
{
   // ************************************************************************
   // Class variables
   // ************************************************************************

   private String           url, user, password;
   private NameQualifier    nameQualifier;

//   private DocumentFactory  documentFactory;
   private ParserUtils      parserUtils;
   private DbConn			dbConn;   
   private static ObjectCache oc = new ObjectCache();

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String PARSERUTILS = "ParserUtils";

//   private static String DOCUMENTFACTORY = "DocumentFactory";
   private static String NAMEQUALIFIER = "NameQualifier";

   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
	* Construct a TransferEngine object.
	*/
   public TransferEngine()
   {
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

   /**
	* Set the user name and password.
	*
	* <p>This method may be used to set the user name and
	* password. In some cases, this is easier than loading the user
	* name and password into properties and calling setDatabaseProperties.
	*
	* @param user User name
	* @param password Password
	*/

   public void setUserInfo(String user, String password)
   {
	 this.user = user;
	 this.password = password;
   }                     

   /**
	* Set the parser properties.
	*
	* <p>This method must be called before transferring data between an
	* XML document and the database. The following properties are accepted:</p>
	*
	* <ul>
	* <li>NameQualifierClass. Name of a class that implements the NameQualifier interface for the parser. Required when calling storeDocument and namespaces are used.</li>
	* <li>DocumentFactoryClass. Name of a class that implements the DocumentFactory interface for the parser. Required when calling retrieveDocument.</li>
	* <li>ParserUtilsClass. Name of a class that implements the ParserUtils interface for the parser. Required.</li>
	* </ul>
	*
	* @param props A properties object containing the above properties.
	*/

   public void setParserProperties(Properties props)
	  throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
	 nameQualifier = (NameQualifier)instantiateClass((String)props.getProperty(XMLDBMSProps.NAMEQUALIFIERCLASS));
//	 documentFactory = (DocumentFactory)instantiateClass((String)props.getProperty(XMLDBMSProps.DOCUMENTFACTORYCLASS));
	 parserUtils = (ParserUtils)instantiateClass((String)props.getProperty(XMLDBMSProps.PARSERUTILSCLASS));
   }                              

   /**
	* Retrieves an XML document from the rows returned by a SELECT statement.
	*
	* <p>The map document must contain a ClassMap for an element mapped to
	* the table named "Result Set". This class map specifies how to map the
	* columns in the result set. Note that if it contains RelatedClass
	* elements, then additional data will be retrieved from the database.</p>
	*
	* <p>If the result set contains more than one row, the map must include an
	* ignored root</p>
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param select      SELECT statement specifying the data to retrieve. Any
	*                    valid SELECT statement (including joins, group bys,
	*                    etc.) is legal.
	*/

   public void retrieveDocument(String mapFilename,
								String xmlFilename,
								String select) 
	  throws Exception
   {
	 Connection       conn = null;
	 Statement        stmt = null;
	 ResultSet        rs = null;
	 Map              map;
	 DBMSToDOM        dbmsToDOM;
	 Document         doc;

	 checkState(PARSERUTILS, parserUtils);
	// checkState(DOCUMENTFACTORY, documentFactory);

	 try
	 {
	   // Connect to the database and get the result set
	   conn = dbConn.getConn();
	   stmt = conn.createStatement();
	   rs = stmt.executeQuery(select);

	   // Create the Map object.
	   map = createMap(mapFilename, conn, rs);

	   // Create a new DBMSToDOM object and transfer the data.
	   dbmsToDOM = new DBMSToDOM(map, parserUtils);
	   doc = dbmsToDOM.retrieveDocument(rs);
	 }
	 finally
	 {
	   // Close the result set, statement, and connection
	   if (rs != null) rs.close();
	   if (stmt != null) stmt.close();
	   if (conn != null) conn.close();
	 }

	 // Write the DOM tree to a file.
	parserUtils.writeDocument(doc, xmlFilename);
	// Return Doc

   }                                    

   /**
	* Retrieves an XML document with the specified table and key as its root.
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param tableName   Name of the table from which to retrieve the root
	*                    element.
	* @param key         Key value of the root element.
	*/

   public void retrieveDocument(String mapFilename,
								String xmlFilename,
								String tableName,
								Object[] key)
	  throws Exception
   {
	 String[]   tableNames = new String[1];
	 Object[][] keys = new Object[1][];
	 Document doc;

	 tableNames[0] = tableName;
	 keys[0] = key;
	 retrieveDocument(mapFilename, xmlFilename, tableNames, keys);
	 
   }                              

   /**
	* Retrieves an XML document with the specified tables and keys as its
	* (pseudo) root(s).
	*
	* <p>If more than one row is retrieved, the map must include an
	* ignored root.</p>
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param tableNames  Names of the tables from which to retrieve the (pseudo)
	*                    root element(s).
	* @param keys        Key values of the (pseudo)root element(s). There must
	*                    be as many key values as table names.
	*/

   public void retrieveDocument(String mapFilename,
								String xmlFilename,
								String[] tableNames,
								Object[][] keys)
	  throws Exception
   {
	 Map              map;
	 DBMSToDOM        dbmsToDOM;
	 Document         doc;
	 Connection conn = null;

	 checkState(PARSERUTILS, parserUtils);
	 //checkState(DOCUMENTFACTORY, documentFactory);

	  // Connect to the database.
	 conn = dbConn.getConn();

	 try
	 {
		 

	   // Create the Map object.
	   map = createMap(mapFilename, conn );

	   // Create a new DBMSToDOM object and transfer the data.
	   dbmsToDOM = new DBMSToDOM(map, parserUtils);
	   doc = dbmsToDOM.retrieveDocument(tableNames, keys);
	 }
	 finally
	 {
	   if (conn != null) conn.close();
	 }

	 // Write the DOM tree to a file.
	parserUtils.writeDocument(doc, xmlFilename);
	//Return doc
   }                                                      

   /**
	* Stores an XML document.
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param commitMode  The commit mode to use. For more information, see
	*                    DOMToDBMS.
	* @param keyGenerator ClassName Name of a class that implements the
	*                     KeyGenerator interface. Null if no key generator used.
	* @param initProps   Properties needed to initialize the key generator. Null
	*                    if no key generator used or no initialization
	*                    properties needed.
	*/
   public void storeDocument(String mapFilename,
							 String xmlFilename,
							 int commitMode,
							 String keyGeneratorClassName,
							 Properties initProps)
	  throws Exception
   {
	 Connection   conn = null;
	 Map          map;
	 Document     doc;
	 DOMToDBMS    domToDBMS;
	 KeyGenerator keyGenerator = null;

	 // Make sure we have a ParserUtils class. No need to check
	 // the NameQualifier, since DOMToDBMS will use the default
	 // if none is set. No need to check the KeyGenerator, since
	 // DOMToDBMS will throw an exception if this is null and a
	 // KeyGenerator is needed.

	 checkState(PARSERUTILS, parserUtils);

	 try
	 {
	   // Connect to the database. 
	   conn = dbConn.getConn();

	   // Create and initialize a key generator
	   keyGenerator = (KeyGenerator)instantiateClass(keyGeneratorClassName);
	   if (keyGenerator != null)
	   {
		 keyGenerator.initialize(initProps);
	   }

	   // Create the Map object and open the XML document.
	   map = createMap(mapFilename, conn);
	   doc = parserUtils.openDocument(xmlFilename);

	   // Create a new DOMToDBMS object, set the commit mode,
	   // and transfer the data.
	   domToDBMS = new DOMToDBMS(map, keyGenerator, nameQualifier);
	   domToDBMS.setCommitMode(commitMode);
	   domToDBMS.storeDocument(doc);
	 }
	 finally
	 {
	   if (keyGenerator != null) keyGenerator.close();
	   if (conn != null) conn.close();
	 }
   }                     

   

   private void checkState(String interfaceName, Object interfaceObject)
   {
	 if (interfaceObject == null)
	   throw new IllegalStateException("Name of class that implements " + interfaceName + " not set.");
   }                  

   private Map createMap(String mapFilename, Connection conn)
	  throws SQLException, InvalidMapException, GetFileException
   {
	 MapFactory_MapDocument    factory;
	 GetFileURL gfu = new GetFileURL();
	 Map map;

	 HashMap h = oc.getMap();
		map = (Map)h.get(mapFilename);
		if(map == null)
		{
			synchronized(TransferEngine.class) // make thread safe
			{
				map = (Map)h.get(mapFilename);
				if(map == null) // may have changed between first if and synch call...
				{ 				
	 			// Create a new map factory and create the Map.
	 			factory = new MapFactory_MapDocument(conn, parserUtils.getSAXParser());

	 			//System.out.println("Mapfile Name =" +gfu.fullqual(mapFilename));
/*
	 			String fullfn = gfu.fullqual(mapFilename);
		InputSource src1 = new InputSource(fullfn);
		String SrcURL1 = src1.getSystemId();
		
	 			map = factory.createMap(new InputSource(SrcURL1));
*/

	 			
	 			
	 			map = factory.createMap(new InputSource(gfu.fullqual(mapFilename)));
   				}
			}
		}
	return map;
}	
	    

   private Map createMap(String mapFilename, Connection conn, ResultSet rs)
	  throws SQLException, InvalidMapException, GetFileException
   {
	 MapFactory_MapDocument    factory;
	 Parser                    parser;

	 // Get the parser and the factory.
	 parser = parserUtils.getSAXParser();
	 factory = new MapFactory_MapDocument();
	 GetFileURL gfu = new GetFileURL();

	 // Create the map
	 return factory.createMap(conn, rs, parser, new InputSource(gfu.fullqual(mapFilename)));
   }                                 

   private Object instantiateClass(String className)
	  throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
	 if (className == null) return null;
	 return Class.forName(className).newInstance();
   }                  
	  /**
	* Retrieves an XML document AS String with the specified tables and keys as its
	* (pseudo) root(s).
	*
	* <p>If more than one row is retrieved, the map must include an
	* ignored root.</p>
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param tableNames  Names of the tables from which to retrieve the (pseudo)
	*                    root element(s).
	* @param keys        Key values of the (pseudo)root element(s). There must
	*                    be as many key values as table names.
	*/

   public String retrieveDocument_s(String mapFilename,
								String xmlFilename,
								String[] tableNames,
								Object[][] keys)
	  throws Exception
   {
	 Map              map;
	 DBMSToDOM        dbmsToDOM;
	 Document         doc;
	 Connection conn = null;

	 checkState(PARSERUTILS, parserUtils);
	// checkState(DOCUMENTFACTORY, documentFactory);

	  // Connect to the database.
	 conn = dbConn.getConn();

	 try
	 {
		 

	   // Create the Map object.
	   map = createMap(mapFilename, conn );

	   // Create a new DBMSToDOM object and transfer the data.
	   dbmsToDOM = new DBMSToDOM(map, parserUtils);
	   doc = dbmsToDOM.retrieveDocument(tableNames, keys);
	 }
	 finally
	 {
	   if (conn != null) conn.close();
	 }

	 // Write the DOM tree to a file.
	// parserUtils.writeDocument(doc, xmlFilename);
	//Return doc
	String s = null;
	s = parserUtils.returnString(doc);	
	
	return s;
   }                              /**
	* Retrieves an XML document from the rows returned by a SELECT statement.
	*
	* <p>The map document must contain a ClassMap for an element mapped to
	* the table named "Result Set". This class map specifies how to map the
	* columns in the result set. Note that if it contains RelatedClass
	* elements, then additional data will be retrieved from the database.</p>
	*
	* <p>If the result set contains more than one row, the map must include an
	* ignored root</p>
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param select      SELECT statement specifying the data to retrieve. Any
	*                    valid SELECT statement (including joins, group bys,
	*                    etc.) is legal.
	*/

   /**
	* Retrieves an XML AS STRING from the rows returned by a SELECT statement.
	*
	* <p>The map document must contain a ClassMap for an element mapped to
	* the table named "Result Set". This class map specifies how to map the
	* columns in the result set. Note that if it contains RelatedClass
	* elements, then additional data will be retrieved from the database.</p>
	*
	* <p>If the result set contains more than one row, the map must include an
	* ignored root</p>
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param select      SELECT statement specifying the data to retrieve. Any
	*                    valid SELECT statement (including joins, group bys,
	*                    etc.) is legal.
	*/


 
	public String retrieveDocument_s(String mapFilename,
								String xmlFilename,
								String select) 
	  throws Exception
   {
	 Connection       conn = null;
	 Statement        stmt = null;
	 ResultSet        rs = null;
	 Map              map;
	 DBMSToDOM        dbmsToDOM;
	 Document         doc;

	 checkState(PARSERUTILS, parserUtils);
//	 checkState(DOCUMENTFACTORY, documentFactory);

	 try
	 {
	   // Connect to the database and get the result set
	   conn = dbConn.getConn();
	   stmt = conn.createStatement();
	   rs = stmt.executeQuery(select);

	   // Create the Map object.
	   map = createMap(mapFilename, conn, rs);

	   // Create a new DBMSToDOM object and transfer the data.
	   dbmsToDOM = new DBMSToDOM(map, parserUtils);
	   doc = dbmsToDOM.retrieveDocument(rs);
	 }
	 finally
	 {
	   // Close the result set, statement, and connection
	   if (rs != null) rs.close();
	   if (stmt != null) stmt.close();
	   if (conn != null) conn.close();
	 }

	 String s = parserUtils.returnString(doc);

	return s;
   }                           /**
	* Retrieves an XML document with the specified table and key as its root.
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param tableName   Name of the table from which to retrieve the root
	*                    element.
	* @param key         Key value of the root element.
	*/

   /**
	* Retrieves an XML document AS STRING with the specified table and key as its root.
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFilename Name of the XML file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param tableName   Name of the table from which to retrieve the root
	*                    element.
	* @param key         Key value of the root element.
	*/
 
 
	public String retrieveDocument_s(String mapFilename,
								String xmlFilename,
								String tableName,
								Object[] key)
	  throws Exception
   {
	 String[]   tableNames = new String[1];
	 Object[][] keys = new Object[1][];
	 Document doc;

	 tableNames[0] = tableName;
	 keys[0] = key;
	 String s = null;
	 s = retrieveDocument_s(mapFilename, xmlFilename, tableNames, keys);
	 return s;
   }                         
   
   /**
	* Stores an XML fed in as an InputStream.
	*
	* <p>setDatabaseProperties and setParserProperties must be called before
	* calling this method.</p>
	*
	* @param mapFilename Name of the map file. This may provide a full path or
	*                    a path relative to the current directory.
	* @param xmlFile	 Name of the XML InputStream. 
	* @param commitMode  The commit mode to use. For more information, see
	*                    DOMToDBMS.
	* @param keyGenerator ClassName Name of a class that implements the
	*                     KeyGenerator interface. Null if no key generator used.
	* @param initProps   Properties needed to initialize the key generator. Null
	*                    if no key generator used or no initialization
	*                    properties needed.
	*/

	
   public void storeDocument(String mapFilename,
							 java.io.InputStream xmlFile,
							 int commitMode,
							 String keyGeneratorClassName,
							 Properties initProps)
	  throws Exception
   {
	 Connection   conn = null;
	 Map          map;
	 Document     doc;
	 DOMToDBMS    domToDBMS;
	 KeyGenerator keyGenerator = null;

	 // Make sure we have a ParserUtils class. No need to check
	 // the NameQualifier, since DOMToDBMS will use the default
	 // if none is set. No need to check the KeyGenerator, since
	 // DOMToDBMS will throw an exception if this is null and a
	 // KeyGenerator is needed.

	 checkState(PARSERUTILS, parserUtils);

	 try
	 {
	   // Connect to the database. 
	   conn = dbConn.getConn();

	   // Create and initialize a key generator
	   keyGenerator = (KeyGenerator)instantiateClass(keyGeneratorClassName);
	   if (keyGenerator != null)
	   {
		 keyGenerator.initialize(initProps);
	   }

	   // Create the Map object and open the XML document.
	   map = createMap(mapFilename, conn);
	   doc = parserUtils.openDocument(xmlFile);

	   // Create a new DOMToDBMS object, set the commit mode,
	   // and transfer the data.
	   domToDBMS = new DOMToDBMS(map, keyGenerator, nameQualifier);
	   domToDBMS.setCommitMode(commitMode);
	   domToDBMS.storeDocument(doc);
	 }
	 finally
	 {
	   if (keyGenerator != null) keyGenerator.close();
	   if (conn != null) conn.close();
	 }
   }                                 
   
   
   /**
 * The init(props) method combines the SetDatabaseProperties & the SetParserProperties methods.
 * @author Adam Flinton 
 * Creation date: (01/05/01 13:59:54)
 */
public void init(Properties props) throws ClassNotFoundException, IllegalAccessException, InstantiationException, java.lang.Exception
{
	setDatabaseProperties(props);
	setParserProperties(props);
	
	}
}