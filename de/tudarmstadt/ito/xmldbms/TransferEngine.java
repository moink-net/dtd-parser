// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
// Version 1.1
// Changes from version 1.01: New in 1.1
package de.tudarmstadt.ito.xmldbms;
import de.tudarmstadt.ito.domutils.NameQualifier;
import de.tudarmstadt.ito.domutils.ParserUtils;
import de.tudarmstadt.ito.domutils.ParserUtilsException;
import de.tudarmstadt.ito.xmldbms.mapfactories.MapFactory_MapDocument;
import de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
import de.tudarmstadt.ito.xmldbms.objectcache.ObjectCache;
import de.tudarmstadt.ito.xmldbms.db.*;
import de.tudarmstadt.ito.xmldbms.tools.GetFileException;
import java.io.File;
import java.io.InputStream;
import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Hashtable;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.w3c.dom.Document;
/**
 * High level interface to XML-DBMS.
 *
 * <p>TransferEngine provides a high-level interface to XML-DBMS.
 * In its current form, it allows applications to transfer data between
 * an XML document and a database by providing only a map file name, an
 * XML file name, and database and parser information.</p>
 *
 * <p>In the future, TransferEngine will probably be replaced by an
 * implementation of the XML:DB API.</p>
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

    private String url, user, password;
    private NameQualifier nameQualifier;
    private ParserUtils parserUtils;
    private DbConn dbConn;
    private static ObjectCache oc = new ObjectCache();

    // ************************************************************************
    // Constants
    // ************************************************************************

    private static String PARSERUTILS = "ParserUtils";
    private static String NAMEQUALIFIER = "NameQualifier";
    private static String TABLENAME = "tableName";
    private static String TABLENAMES = "tableNames";
    private static String KEY = "key";
    private static String KEYS = "keys";

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
    	* accepted for JDBC 1.0 drivers:</p>
    	*
    	* <ul>
    	* <li>Driver: Name of the JDBC driver class to use. Required.</li>
    	* <li>URL: URL of the database. Required.</li>
    	* <li>User: Database user name. Depends on database.</li>
    	* <li>Password: Database password. Depends on database.</li>
    	* </ul>
    	*
    	* <p>The following properties are accepted for JDBC 2.0 drivers:</p>
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
    public void setDatabaseProperties(Properties props)
        throws
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            java.lang.Exception
    {
        int i = 0;
        String JDBC = props.getProperty(DBProps.JDBCLEVEL);
        if (JDBC == null)
            {
            i = 1;
        }
        else
            {
            try
                {
                i = Integer.parseInt(JDBC.trim());
            }
            catch (NumberFormatException nfe)
                {
                throw new IllegalArgumentException("Invalid value for JDBCLevel property: " + JDBC);
            }
        }
        switch (i)
            {
            case 1 :
                dbConn = (DbConn) instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn1");
                break;
            case 2 :
                dbConn = (DbConn) instantiateClass("de.tudarmstadt.ito.xmldbms.db.DbConn2");
                break;
            default: throw new IllegalArgumentException("Invalid value for JDBCLevel property: " + JDBC);

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
    	* <li>NameQualifierClass. Name of a class that implements the
      *     NameQualifier interface for the parser. Required when calling
      *     storeDocument and namespaces are used.</li>
    	* <li>ParserUtilsClass. Name of a class that implements the ParserUtils
      *     interface for the parser. Required.</li>
    	* </ul>
    	*
    	* @param props A properties object containing the above properties.
    	*/
    public void setParserProperties(Properties props)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        nameQualifier =
            (NameQualifier) instantiateClass(props
                .getProperty(XMLDBMSProps.NAMEQUALIFIERCLASS));
        parserUtils =
            (ParserUtils) instantiateClass(props
                .getProperty(XMLDBMSProps.PARSERUTILSCLASS));
    }

    /**
     * Convenience method that calls setDatabaseProperties and setParserProperties.
     *
     * @param props A Properties object. See setDatabaseProperties and
     *  setParserProperties for the properties it should contain.
    */
    public void init(Properties props)
        throws
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            java.lang.Exception
    {
        setDatabaseProperties(props);
        setParserProperties(props);
    }

    /**
    * Retrieves an XML document with the specified table and key as its root
    * and returns it as a file.
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
    public void retrieveDocument(
        String mapFilename,
        String xmlFilename,
        String tableName,
        Object[] key)
        throws Exception
    {
        String[] tableNames;
        Object[][] keys;

        checkArgNull(TABLENAME, tableName);
        checkArgNull(KEY, key);

        tableNames = new String[1];
        keys = new Object[1][];
        tableNames[0] = tableName;
        keys[0] = key;
        retrieveDocument(mapFilename, xmlFilename, tableNames, keys);
    }

    /**
      * Retrieves an XML document with the specified table and key as its root
      * and returns it as a string.
    	*
    	* <p>setDatabaseProperties and setParserProperties must be called before
    	* calling this method.</p>
    	*
    	* @param mapFilename Name of the map file. This may provide a full path or
    	*                    a path relative to the current directory.
    	* @param tableName   Name of the table from which to retrieve the root
    	*                    element.
    	* @param key         Key value of the root element.
    	*/
    public String retrieveDocument(
        String mapFilename,
        String tableName,
        Object[] key)
        throws Exception
    {
        String[] tableNames;
        Object[][] keys;

        checkArgNull(TABLENAME, tableName);
        checkArgNull(KEY, key);

        tableNames = new String[1];
        keys = new Object[1][];
        tableNames[0] = tableName;
        keys[0] = key;

        return retrieveDocument(mapFilename, tableNames, keys);
    }

    /**
    * Retrieves an XML document with the specified tables and keys as its
    * (pseudo) root(s) and returns it as a file.
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
    public void retrieveDocument(
        String mapFilename,
        String xmlFilename,
        String[] tableNames,
        Object[][] keys)
        throws Exception
    {
        Document doc;

        checkArgNull(TABLENAMES, tableNames);
        checkArgNull(KEYS, keys);

        doc = retrieveDOMDocument(mapFilename, tableNames, keys);
        parserUtils.writeDocument(doc, xmlFilename);
    }

    /**
    * Retrieves an XML document with the specified tables and keys as its
    * (pseudo) root(s) and returns it as a string.
    *
    * <p>If more than one row is retrieved, the map must include an
    * ignored root.</p>
    *
    * <p>setDatabaseProperties and setParserProperties must be called before
    * calling this method.</p>
    *
    * @param mapFilename Name of the map file. This may provide a full path or
    *                    a path relative to the current directory.
    * @param tableNames  Names of the tables from which to retrieve the (pseudo)
    *                    root element(s).
    * @param keys        Key values of the (pseudo)root element(s). There must
    *                    be as many key values as table names.
    */
    public String retrieveDocument(
        String mapFilename,
        String[] tableNames,
        Object[][] keys)
        throws Exception
    {
        Document doc;

        checkArgNull(TABLENAMES, tableNames);
        checkArgNull(KEYS, keys);

        doc = retrieveDOMDocument(mapFilename, tableNames, keys);
        return parserUtils.writeDocument(doc);
    }

    /**
    	* Retrieves an XML from the rows returned by a SELECT statement and returns
      * it as a file.
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
    public void retrieveDocument(
        String mapFilename,
        String xmlFilename,
        String select)
        throws Exception
    {
        Document doc;

        doc = retrieveDOMDocument(mapFilename, select);
        parserUtils.writeDocument(doc, xmlFilename);
    }

    /**
    	* Retrieves an XML from the rows returned by a SELECT statement and returns
      * it as a string.
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
    	* @param select      SELECT statement specifying the data to retrieve. Any
    	*                    valid SELECT statement (including joins, group bys,
    	*                    etc.) is legal.
    	*/
    public String retrieveDocument(String mapFilename, String select)
        throws Exception
    {
        Document doc;

        doc = retrieveDOMDocument(mapFilename, select);
        return parserUtils.writeDocument(doc);
    }

    /**
    	* Store an XML document that is in a file.
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
    	* @param keyGeneratorClassName Name of a class that implements the
    	*                     KeyGenerator interface. Null if no key generator used.
    	* @param initProps   Properties needed to initialize the key generator. Null
    	*                    if no key generator used or no initialization
    	*                    properties needed.
    	*/
    public void storeXMLFile(
        String mapFilename,
        String xmlFilename,
        int commitMode,
        String keyGeneratorClassName,
        Properties initProps)
        throws Exception
    {
        Document doc;

        doc = parserUtils.openDocument(xmlFilename);
        storeDocument(mapFilename, doc, commitMode, keyGeneratorClassName, initProps);
    }

    /**
    	* Store an XML document that is in an InputStream.
    	*
    	* <p>setDatabaseProperties and setParserProperties must be called before
    	* calling this method.</p>
    	*
    	* @param mapFilename Name of the map file. This may provide a full path or
    	*                    a path relative to the current directory.
    	* @param inputStream An InputStream containing the XML document. 
    	* @param commitMode  The commit mode to use. For more information, see
    	*                    DOMToDBMS.
    	* @param keyGeneratorClassName Name of a class that implements the
    	*                     KeyGenerator interface. Null if no key generator used.
    	* @param initProps   Properties needed to initialize the key generator. Null
    	*                    if no key generator used or no initialization
    	*                    properties needed.
    	*/
    public void storeXMLInputStream(
        String mapFilename,
        InputStream inputStream,
        int commitMode,
        String keyGeneratorClassName,
        Properties initProps)
        throws Exception
    {
        Document doc;

        doc = parserUtils.openDocument(inputStream);
        storeDocument(mapFilename, doc, commitMode, keyGeneratorClassName, initProps);
    }

    /**
    	* Store an XML document that is in a string.
    	*
    	* <p>setDatabaseProperties and setParserProperties must be called before
    	* calling this method.</p>
    	*
    	* @param mapFilename Name of the map file. This may provide a full path or
    	*                    a path relative to the current directory.
    	* @param xml	 String containing the XML document. 
    	* @param commitMode  The commit mode to use. For more information, see
    	*                    DOMToDBMS.
    	* @param keyGeneratorClassName Name of a class that implements the
    	*                     KeyGenerator interface. Null if no key generator used.
    	* @param initProps   Properties needed to initialize the key generator. Null
    	*                    if no key generator used or no initialization
    	*                    properties needed.
    	*/
    public void storeXMLString(
        String mapFilename,
        String xml,
        int commitMode,
        String keyGeneratorClassName,
        Properties initProps)
        throws Exception
    {
        Document doc;

        byte[] b = xml.getBytes();
        InputStream is = new java.io.ByteArrayInputStream(b);
        doc = parserUtils.openDocument(is);
        storeDocument(mapFilename, doc, commitMode, keyGeneratorClassName, initProps);
    }

    // ************************************************************************
    // Private methods
    // ************************************************************************

   private Document retrieveDOMDocument(
        String mapFilename,
        String select)
        throws Exception
   {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Map map;
        DBMSToDOM dbmsToDOM;
        Document doc;
        checkState(PARSERUTILS, parserUtils);
        select = select.trim();
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
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }
        return doc;
    }

   private Document retrieveDOMDocument(
        String mapFilename,
        String[] tableNames,
        Object[][] keys)
        throws Exception
   {
        Map map;
        DBMSToDOM dbmsToDOM;
        Document doc;
        Connection conn = null;
        checkState(PARSERUTILS, parserUtils);

        // Check that the array sizes of tableNames and keys are the same

        if (tableNames.length != keys.length)
           throw new IllegalArgumentException("The same number of table names and keys must be passed. The number of table names that were passed was " + tableNames.length + " and the number of keys that were passed was " + keys.length + ". A common cause of this problem is using the incorrect property names when using Transfer. For example, you used Table1, Table2, etc. but used Key1, Key2, etc. instead of Key1_1, Key2_1, etc.");

        // Connect to the database.
        conn = dbConn.getConn();
        try
            {
            // Create the Map object.
            map = createMap(mapFilename, conn);
            // Create a new DBMSToDOM object and transfer the data.
            dbmsToDOM = new DBMSToDOM(map, parserUtils);
            doc = dbmsToDOM.retrieveDocument(tableNames, keys);
        }
        finally
            {
            if (conn != null)
                conn.close();
        }
        return doc;
    }

    private void storeDocument(
        String mapFilename,
        Document doc,
        int commitMode,
        String keyGeneratorClassName,
        Properties initProps)
        throws Exception
    {
        Connection conn = null;
        Map map;
        DOMToDBMS domToDBMS;
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
            keyGenerator = (KeyGenerator) instantiateClass(keyGeneratorClassName);
            if (keyGenerator != null)
                {
                keyGenerator.initialize(initProps);
            }

            // Create the Map object and open the XML document.
            map = createMap(mapFilename, conn);

            // Create a new DOMToDBMS object, set the commit mode,
            // and transfer the data.
            domToDBMS = new DOMToDBMS(map, keyGenerator, nameQualifier);
            domToDBMS.setCommitMode(commitMode);
            domToDBMS.storeDocument(doc);
        }
        finally
            {
            if (keyGenerator != null)
                keyGenerator.close();
            if (conn != null)
                conn.close();
        }
    }



    /**
    	 * Checks that an Object has been created 
    	 */
    private void checkState(String interfaceName, Object interfaceObject)
    {
        if (interfaceObject == null)
            throw new IllegalStateException(
                "Name of class that implements " + interfaceName + " not set.");
    }

    private void checkArgNull(String argumentName, Object argument)
    {
       if (argument == null)
          throw new IllegalArgumentException("Argument " + argumentName + " must not be null. A common cause of this problem is using the incorrect property name with Transfer, such as using Key1, Key2, etc. instead of Key1_1, Key2_1, etc. when the action is RetrieveDocumentByKeys.");
    }

    /** Create a map from a mapfilename & a JDBC Connection.
     *  It will attempt first to get an exiting Map object from the Cache if possible so as not to have 
     * to create a map everytime
     */
    private Map createMap(String mapFilename, Connection conn)
        throws SQLException, InvalidMapException, GetFileException, ParserUtilsException
    {
        MapFactory_MapDocument factory;
        GetFileURL gfu = new GetFileURL();
        Map map;
        Hashtable h = oc.getMap();
        map = (Map) h.get(mapFilename);
        if (map == null)
            {
            synchronized (TransferEngine.class) // make thread safe
            
            {
                map = (Map) h.get(mapFilename);
                if (map == null) // may have changed between first if and synch call...
                    {
                    // Create a new map factory and create the Map.
                    factory = new MapFactory_MapDocument(conn, parserUtils.getSAXParser());
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
    /** Create a map from a mapfilename & a JDBC Connection and a Result Set
     */
    private Map createMap(String mapFilename, Connection conn, ResultSet rs)
        throws SQLException, InvalidMapException, GetFileException, ParserUtilsException
    {
        MapFactory_MapDocument factory;
        Parser parser;
        // Get the parser and the factory.
        parser = parserUtils.getSAXParser();
        factory = new MapFactory_MapDocument();
        GetFileURL gfu = new GetFileURL();
        // Create the map
        return factory.createMap(
            conn,
            rs,
            parser,
            new InputSource(gfu.fullqual(mapFilename)));
    }
    /** Creates an object if the classname is supplied as a string
     */
    private Object instantiateClass(String className)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        if (className == null)
            return null;
        return Class.forName(className).newInstance();
    }
}