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
//   http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.01: New in 1.1
// Changes from version 1.1: Updated for version 2.0

package org.xmlmiddleware.xmldbms.tools;

import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.actions.*;
import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.filters.*;
import org.xmlmiddleware.xmldbms.keygenerators.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.factories.*;
import org.xmlmiddleware.xmlutils.*;

import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Simplified interface to XML-DBMS.
 *
 * <p><b>Introduction</b></p>
 *
 * <p>Transfer provides three interfaces for using XML-DBMS: a command
 * line interface, a dispatch-style API, and a high-level, traditional API.
 * The command line interface also consists of a single method (main).
 * The dispatch-style API consists of a single method (dispatch).  All
 * other methods (setDatabaseProperties, storeDocument, retrieveDocument,
 * and deleteDocument) belong to the traditional API.</p>
 *
 * <p>The traditional API can transfer data between the database and a
 * file or a string. Strings are a useful way to represent an XML
 * document, since they can easily be passed to/from an XSLT processor,
 * HTTP, etc. The dispatch-style API and the command line interface can
 * only transfer data between a database and a file.</p>
 *
 * <p><b>Properties</b></p>
 *
 * <p>All of Transfer's interfaces use properties. The dispatch-style
 * API and command-line interface are entirely properties based, while
 * the traditional API uses properties for configuration information such
 * as the commit mode and which key generators to use.</p>
 *
 * <p>Properties fall into the following categories:</p>
 *
 * <ul>
 * <li><p>Property-processing properties are used to process other properties.
 *     They are File and BaseURL.</p></li>
 *
 * <li><p>Parser properties provide information about the XML parser / DOM
 *     implementation. They are ParserUtilsClass.</p></li>
 *
 * <li><p>Database properties are used to connect to the database(s). They are
 *     DBName, DataHandlerClass, DataSourceClass, User, and Password. If there
 *     is more than one database, use sequentially numbered versions of these
 *     properties -- DBName1, DBName2, etc.</p>
 *
 *     <p>When the value of DataSourceClass is "JDBC1DataSource", the Driver and
 *     URL properties are used to configure the data source. When the value
 *     of DataSourceClass is "JDBC2DataSource", the JNDIContext and
 *     JNDILookupName properties are used to configure the data source. If
 *     the main database properties are numbered, these properties must also
 *     be numbered.</p>
 *
 *     <p>Note that DBName and DataHandlerClass are both optional. If DBName
 *     is omitted, "Default" is used. This is an error if there is more than
 *     one database. If DataHandlerClass is omitted,
 *     org.xmlmiddleware.xmldbms.datahandlers.GenericHandler is used.</p></li>
 *
 * <li><p>Transfer properties specify what is to be done (store, retrieve, or
 *     delete a document) and the files to use. They are Method, MapFile,
 *     XMLFile, ActionFile, and FilterFile. See below for details.</p></li>
 *
 * <li><p>Select properties specify result sets to use when retrieving data.
 *     They are Select, SelectDBName, and SelectResultSetName.</p></li>
 *
 * <li><p>Configuration properties specify how the underlying data transfer
 *     classes are to function. They are Encoding, SystemID, PublicID,
 *     CommitMode, StopOnError, ReturnFilter, KeyGeneratorName, and
 *     KeyGeneratorClass. See below for details.</p></li>
 * </ul>
 *
 * <p>When using the command line or the dispatch-style API, the Method property
 * specifies the action to take. Legal values are StoreDocument, RetrieveDocumentByFilter,
 * RetrieveDocumentBySQL, and DeleteDocument. (When using the traditional API, the
 * Method property is not needed since this information is inherent in the method called.)
 * The following table shows which transfer properties are used with each value of the
 * Method property:</p>
 *
 * <table border="1" cellpadding="3">
 * <tr><th>Method</th><th>Transfer properties</th></tr>
 * <tr valign="top"><td>StoreDocument</td><td>MapFile<br />XMLFile<br />ActionFile
 * <br />FilterFile (when ReturnFilter is "Yes")</td></tr>
 * <tr valign="top"><td>RetrieveDocumentByFilter</td><td>MapFile<br />XMLFile
 * <br />FilterFile[1]</td></tr>
 * <tr valign="top"><td>RetrieveDocumentBySQL</td><td>MapFile<br />XMLFile<br />
 * FilterFile[1]<br />Select[2]<br />SelectDBName[2][3]<br />SelectResultSetName[2][4]</td></tr>
 * <tr valign="top"><td>DeleteDocument</td><td>MapFile<br />ActionFile<br />FilterFile</td></tr>
 * </table>
 *
 * <p>NOTES:<br />
 * [1] If the filter document uses parameters, these should be passed in as well.
 * Because parameter names begin with a dollar sign ($), there should be no conflict
 * between parameter names and the names of other properties.<br />
 * [2] If there is more than one result set, use Select1, Select2, ...,
 * SelectDBName1, SelectDBName2, etc.<br />
 * [3] Optional. If no database name is specified, "Default" is used.<br />
 * [4] Optional if there is only one result set, in which case "Default" is used.
 * Required if there is more than one result set. Result set names correspond to
 * result set names in the filter document.</p>
 *
 * <p>The following table shows which configuration properties apply to each method.
 * Configuration properties are used by all three interfaces.</p>
 *
 * <table border="1" cellpadding="3">
 * <tr valign="top"><th>Method</th><th>Configuration properties</th></tr>
 * <tr valign="top"><td>StoreDocument</td><td>CommitMode[1]<br />StopOnError
 * <br />ReturnFilter<br />KeyGeneratorName[2]<br />KeyGeneratorClass[2][3]
 * <br />Encoding[4]<br />SystemID[4]<br />PublicID[4]</td></tr>
 * <tr valign="top"><td>RetrieveDocumentByFilter<br />RetrieveDocumentBySQL</td>
 * <td>Encoding<br />SystemID<br />PublicID</td></tr>
 * <tr valign="top"><td>DeleteDocument</td><td>CommitMode[1]</td></tr>
 * </table>
 *
 * <p>NOTES:<br />
 * [1] Legal values of CommitMode are AfterStatement, AfterDocument, None, and
 * NoTransactions.<br />
 * [2] If there is more than one key generator, use KeyGeneratorName1,
 * KeyGeneratorName2, ... KeyGeneratorClass1, KeyGeneratorClass2, etc.<br />
 * [3] If the key generator requires initialization properties, these should be
 * passed in as well. See the documentation for your key generator to see what
 * these are.<br />
 * [4] Applies to the output filter file, if any.</p>
 *
 * <p>For a complete description of the properties used by Transfer,
 * see ?????.</p>
 *
 * <p><b>Command Line Syntax</b></p>
 *
 * <p>The syntax of the command-line interface is:</p>
 *
 * <pre>
 *   java Transfer <property>=<value> [<property>=<value>...]
 * </pre>
 *
 * <p>Property/value pairs are read in order and, if a property occurs more
 * than once, the last value is used. If a property/value pair contains
 * spaces, the entire pair must be enclosed in quotes.</p>
 *
 * <p>For example, the following is used to store data from the
 * sales.xml file to the database:</p>
 *
 * <pre>
 *   java org.xmlmiddleware.xmldbms.tools.Transfer 
 *                  ParserUtilsClass=org.xmlmiddleware.domutils.helpers.ParserUtilsXerces
 *                  DataSourceClass=JDBC1DataSource
 *                  Driver=sun.jdbc.odbc.JdbcOdbcDriver URL=jdbc:odbc:xmldbms
 *                  User=ron Password=ronpwd
 *                  Method=StoreDocument
 *                  MapFile=sales.map XMLFile=sales.xml ActionFile=sales.act
 * </pre>
 *
 * <p>A special property, File, can be used to designate a file containing
 * other properties. For example, if the parser properties are stored in
 * xerces.props and the database properties stored in db.props, the following
 * is equivalent to the previous command line:</p>
 *
 * <pre>
 *   java org.xmlmiddleware.xmldbms.tools.Transfer File1=xerces.props File2=db.props
 *           Method=StoreDocument MapFile=sales.map XMLFile=sales.xml ActionFile=sales.act
 * </pre>
 *
 * <p>Notice that when more than one File property is used, the File properties
 * must be numbered sequentially. File properties can also be used inside
 * property files, making it possible to have a hierarchy of property files.
 * File properties can be used from the command line and with the
 * dispatch-style interface. They cannot be used with the traditional API.</p>
 *
 * <p>The value of a File property is a URL. This can be either a complete URL,
 * such as http://www.rpbourret.com/props/myprops.props or a relative URL, such
 * as myprops.props. In the case of a relative URL, the value of the BaseURL
 * property is prepended to the value of the File property. If no BaseURL property
 * is specified, then the value of the File property is treated as a local file name.</p>
 *
 * <p>The purpose of the BaseURL property is to make it easy to deploy files on
 * different machines simply by changing the BaseURL property. The BaseURL property
 * is applied hierarchically. That is, if it occurs in the command line or in a given
 * property file, it applies to those properties and all descendant properties until
 * it is overridden.</p>
 *
 * <p>The BaseURL property applies to the value of the File, MapFile, XMLFile, ActionFile
 * and FilterFile properties.</p>
 *
 * <p><b>Dispatch-style Interface</b></p>
 *
 * <p>The dispatch-style interface is called that because methods are not called directly.
 * Instead, the name of the method and its arguments are passed as properties and Transfer
 * "dispatches" the call to the actual method based on these properties.</p>
 *
 * <p>The dispatch-style interface consists of a single method, dispatch(). In general,
 * applications that want to call Transfer programmatically should probably use the traditional
 * API instead. Not only is it easier to use, it is more efficient because it holds database
 * connections open across calls. The dispatch() method connects to the database each time
 * it is called.</p>
 *
 * <p><b>Traditional API</b></p>
 *
 * <p>The traditional API consists of a number of methods: setDatabaseProperties() and a
 * number of variations of storeDocument(), retrieveDocument(), and deleteDocument(). These
 * methods allow you to transfer data between the database and an XML file, a string, or
 * (in the case of storeDocument()) an InputStream. Applications using this interface must
 * call setDatabaseProperties() before calling any of the other methods.</p>
 *
 * <p>storeDocument(), retrieveDocument(), and deleteDocument() accept configuration parameters
 * in the form of properties. All of these have defaults except for the key generator
 * properties, which are required by storeDocument when the specified map uses key
 * generators.</p>
 *
 * <p><b>Object Caching</b></p>
 *
 * <p>When Transfer is called through the dispatch(), storeDocument(), retrieveDocument(),
 * and deleteDocument() methods, it caches various objects for reuse in subsequent
 * calls. The following objects are cached, with the key (property) shown in parentheses. If
 * the item to which the key points (such as a map file) changes between calls to
 * these methods, the new object will not be used. To use the new object, applications must
 * instantiate and use a new Transfer object. Note that database objects (DataSource,
 * DataHandler, etc.) are cached between calls to setDatabaseProperties().</p>
 *
 * <pre>
 * XMLDBMSMap (MapFile)
 * Actions (ActionFile)
 * FilterSet (FilterFile)
 * KeyGenerator (KeyGeneratorName)
 * </pre>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.tools.XMLDBMSProps
 */

public class Transfer extends PropertyProcessor
{
   // ************************************************************************
   // Class variables
   // ************************************************************************

   ParserUtils utils;
   Hashtable   fileObjects = new Hashtable(),
               keyGenerators = new Hashtable(),
               transferInfos = new Hashtable(),
               dbInfos = new Hashtable(),
               dataHandlers = new Hashtable();
   Vector      conns = new Vector();
   DOMToDBMS   domToDBMS = null;
   DBMSToDOM   dbmsToDOM = null;
   DBMSDelete  dbmsDelete = null;

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String GENERICHANDLER = "org.xmlmiddleware.xmldbms.datahandlers.GenericHandler";
   private static String JDBC1DATASOURCE = "org.xmlmiddleware.db.JDBC1DataSource";
   private static String JDBC2DATASOURCE = "org.xmlmiddleware.db.JDBC2DataSource";
   private static String DEFAULT = "Default";
   private static String YES = "YES";

   // ************************************************************************
   // Constructor
   // ************************************************************************

   private Transfer()
   {
      // We need this constructor when Transfer is called from the command
      // line. This is because we don't yet know the name of the ParserUtils
      // class. This constructor is private because constructing Transfer
      // without a ParserUtils class is a dangerous thing to do.

      super();
      setFinalizers();
   }

   /**
    * Construct a Transfer object.
    *
    * @param props A Properties object containing the ParserUtilsClass property.
    * @exception XMLMiddlewareException An error occurs instantiating the ParserUtils class.
    */
   public Transfer(Properties props)
      throws XMLMiddlewareException
   {
      super();
      setParserUtils(props);
      setFinalizers();
   }

   /**
    * Construct a Transfer object.
    *
    * @param utils An object that implements the ParserUtils interface.
    */
   public Transfer(ParserUtils utils)
   {
      super();
      if (utils == null)
         throw new IllegalArgumentException("utils argument must not be null.");
      this.utils = utils;
      setFinalizers();
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Run Transfer from a command line.
    *
    * <p>See the introduction for the command line syntax.</p>
    *
    * @param args An array of property/value pairs.
    */

   public static void main(String[] args)
   {
      Transfer   transfer = new Transfer();
      Properties props = new Properties();

      if (args.length < 1)
      {
         System.out.println("Usage: java Transfer <property>=<value> [<property>=<value>...]\n\n" + "See the documentation for a list of valid properties.");
         return;
      }

      try
      {
         // Add the properties, set the ParserUtils class, and execute the
         // specified method.

         transfer.addPropertiesFromArray(props, args, 0, true);
         transfer.setParserUtils(props);
         transfer.dispatch(props);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Set the database properties.
    *
    * <p>For a list of database properties, see the introduction. Applications that
    * call storeDocument(), retrieveDocument(), or deleteDocument() must call this
    * method before calling those methods. Calling this method closes any existing
    * connections to the database, so it should be used sparingly.</p>
    *
    * @param props The database properties
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */

   public void setDatabaseProperties(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      // Clear out the existing database objects. Note that doing this removes any
      // references to those objects, so those objects are marked to be discarded.
      // Run the garbage collector so that any open database connections are forced
      // to be closed.

      transferInfos.clear();
      dbInfos.clear();
      dataHandlers.clear();
      conns.removeAllElements();
      System.gc();

      // The user can specify a single data source (with the DataSourceClass
      // property) or multiple data sources (with the DataSourceClass1, 2, ...
      // property). Check which one is being used.

      if (props.getProperty(XMLDBMSProps.DATASOURCECLASS) != null)
      {
         configSingleDatabase(props);
      }
      else
      {
         configMultipleDatabases(props);
      }
   }

   /**
    * Executes the method specified by the Method property.
    *
    * <p>For a list of valid methods (values of the Method property) and
    * the properties each of these methods needs, see the introduction.</p>
    *
    * @param props A Properties object describing the method to be executed
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void dispatch(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String method;

      // Set the database properties. Note that this means dispatch is an inefficient
      // way to make multiple calls to Transfer, as connections are not held open.

      setDatabaseProperties(props);

      // Get the method

      method = props.getProperty(XMLDBMSProps.METHOD);
      if (method == null)
         throw new IllegalArgumentException(XMLDBMSProps.METHOD + " property not specified.");

      // Dispatch the method

      if (method.equals(XMLDBMSProps.STOREDOCUMENT))
      {
         dispatchStoreDocument(props);
      }
      else if (method.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYSQL))
      {
         dispatchRetrieveDocumentBySQL(props);
      }
      else if (method.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYFILTER))
      {
         dispatchRetrieveDocumentByFilter(props);
      }
      else if (method.equals(XMLDBMSProps.DELETEDOCUMENT))
      {
         dispatchDeleteDocument(props);
      }
      else
         throw new IllegalArgumentException("Unknown value of the " + XMLDBMSProps.METHOD + " property: " + method);
   }

   /**
    * Store (insert or update) data from an XML file in the database
    *
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param xmlFilename Filename or URL of the XML file.
    * @param mapFilename Filename or URL of the map file.
    * @param actionFilename Filename or URL of the action file.
    * @return A FilterSet describing the stored document. This is returned only if
    *    the ReturnFilter property is set to "Yes".
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public FilterSet storeDocument(Properties configProps, String xmlFilename, String mapFilename, String actionFilename)
      throws XMLMiddlewareException, SQLException
   {
      InputSource src;

      // Note that buildURLString is in PropertyProcessor (the base class).

      src = new InputSource(buildURLString(null, xmlFilename));
      return storeDocumentInternal(configProps, mapFilename, actionFilename, src);
   }

   /**
    * Store (insert or update) data from an XML string in the database
    *
    * @param xmlString A string containing the XML to store.
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param mapFilename Filename or URL of the map file.
    * @param actionFilename Filename or URL of the action file.
    * @return A FilterSet describing the stored document. This is returned only if
    *    the ReturnFilter property is set to "Yes".
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public FilterSet storeDocument(String xmlString, Properties configProps, String mapFilename, String actionFilename)
      throws XMLMiddlewareException, SQLException
   {
      InputSource src;

      src = new InputSource(new StringReader(xmlString));
      return storeDocumentInternal(configProps, mapFilename, actionFilename, src);
   }

   /**
    * Store (insert or update) data from an XML InputStream in the database
    *
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param mapFilename Filename or URL of the map file.
    * @param actionFilename Filename or URL of the action file.
    * @param xmlStream An InputStream containing the XML to store.
    * @return A FilterSet describing the stored document. This is returned only if
    *    the ReturnFilter property is set to "Yes".
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public FilterSet storeDocument(Properties configProps, String mapFilename, String actionFilename, InputStream xmlStream)
      throws XMLMiddlewareException, SQLException
   {
      InputSource src;

      src = new InputSource(xmlStream);
      return storeDocumentInternal(configProps, mapFilename, actionFilename, src);
   }

   /**
    * Retrieve data from the database as an XML string
    *
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param mapFilename Filename or URL of the map file.
    * @param filterFilename Filename or URL of the filter file.
    * @param params A Hashtable of filter parameters. May be null. Note that this may
    *    be a Properties object, since Properties inherit from Hashtable.
    * @return A string containing the retrieved XML
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public String retrieveDocument(Properties configProps, String mapFilename, String filterFilename, Hashtable params)
      throws XMLMiddlewareException, SQLException
   {
      Document doc;

      doc = retrieveDocumentInternal(configProps, mapFilename, filterFilename, params);
      return utils.writeDocument(doc);
   }

   /**
    * Retrieve data from the database as an XML file
    *
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param mapFilename Filename or URL of the map file.
    * @param filterFilename Filename or URL of the filter file.
    * @param params A Hashtable of filter parameters. May be null. Note that this may
    *    be a Properties object, since Properties inherit from Hashtable.
    * @param xmlFilename Filename or URL of the XML file.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void retrieveDocument(Properties configProps, String mapFilename, String filterFilename, Hashtable params, String xmlFilename)
      throws XMLMiddlewareException, SQLException
   {
      Document doc;
      String   encoding = null;

      if (configProps != null) encoding = configProps.getProperty(XMLDBMSProps.ENCODING);

      doc = retrieveDocumentInternal(configProps, mapFilename, filterFilename, params);
      utils.writeDocument(doc, xmlFilename, encoding);
   }

   /**
    * Retrieve data from a result set as an XML string
    *
    * <p>If the class map for the element type corresponding to the result set contains
    * related classes, this method retrieves additional data from the database.</p>
    *
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param mapFilename Filename or URL of the map file.
    * @param selects A Properties object describing the result set. See the introduction
    *    for details.
    * @param filterFilename Filename or URL of the filter file.
    * @param params A Hashtable of filter parameters. May be null. Note that this may
    *    be a Properties object, since Properties inherit from Hashtable.
    * @return A string containing the retrieved XML
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public String retrieveDocument(Properties configProps, String mapFilename, Properties selects, String filterFilename, Hashtable params)
      throws XMLMiddlewareException, SQLException
   {
      Document doc;

      doc = retrieveDocumentInternal(configProps, mapFilename, selects, filterFilename, params);
      return utils.writeDocument(doc);
   }

   /**
    * Retrieve data from a result set as an XML file
    *
    * <p>If the class map for the element type corresponding to the result set contains
    * related classes, this method retrieves additional data from the database.</p>
    *
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param mapFilename Filename or URL of the map file.
    * @param selects A Properties object describing the result set. See the introduction
    *    for details.
    * @param filterFilename Filename or URL of the filter file.
    * @param params A Hashtable of filter parameters. May be null. Note that this may
    *    be a Properties object, since Properties inherit from Hashtable.
    * @param xmlFilename Filename or URL of the XML file.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void retrieveDocument(Properties configProps, String mapFilename, Properties selects, String filterFilename, Hashtable params, String xmlFilename)
      throws XMLMiddlewareException, SQLException
   {
      Document doc;
      String   encoding = null;

      if (configProps != null) encoding = configProps.getProperty(XMLDBMSProps.ENCODING);

      doc = retrieveDocumentInternal(configProps, mapFilename, selects, filterFilename, params);
      utils.writeDocument(doc, xmlFilename, encoding);
   }

   /**
    * Delete data from the database
    *
    * @param configProps Configuration properties. May be null. See the introduction
    *    for details.
    * @param mapFilename Filename or URL of the map file.
    * @param actionFilename Filename or URL of the action file.
    * @param filterFilename Filename or URL of the filter file.
    * @param params A Hashtable of filter parameters. May be null. Note that this may
    *    be a Properties object, since Properties inherit from Hashtable.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void deleteDocument(Properties configProps, String mapFilename, String actionFilename, String filterFilename, Hashtable params)
      throws XMLMiddlewareException, SQLException
   {
      XMLDBMSMap   map;
      TransferInfo transferInfo;
      Actions      actions;
      FilterSet    filterSet;

      // Create the various objects needed by DBMSDelete.

      map = createMap(mapFilename);
      transferInfo = createTransferInfo(map);
      actions = createActions(map, actionFilename);
      filterSet = createFilterSet(map, filterFilename);

      // Configure the DBMSDelete object

      configDBMSDelete(configProps);

      // Delete the document

      dbmsDelete.deleteDocument(transferInfo, filterSet, params, actions);
   }

   // ************************************************************************
   // Private methods -- dispatch
   // ************************************************************************

   private void dispatchStoreDocument(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String    mapFile, actionFile, xmlFile;
      FilterSet filterSet;

      // Get the names of the map, action, and XML files

      mapFile = getProperty(props, XMLDBMSProps.MAPFILE);
      actionFile = getProperty(props, XMLDBMSProps.ACTIONFILE);
      xmlFile = getProperty(props, XMLDBMSProps.XMLFILE);

      // Store the document. If the user requested a FilterSet, write it out
      // now. Note that filterSet is null unless the ReturnFilter property was
      // set to "Yes".

      filterSet = storeDocument(props, xmlFile, mapFile, actionFile);
      if (filterSet != null)
      {
         writeFilterSet(props, filterSet);
      }
   }

   private void writeFilterSet(Properties props, FilterSet filterSet)
      throws XMLMiddlewareException
   {
      String           filterFile, encoding, systemID, publicID;
      URL              filterURL;
      URLConnection    conn;
      OutputStream     outputStream;
      Writer           writer;
      FilterSerializer serializer;

      try
      {

         // Get the name of the filter file. Do nothing if there is no filter file.

         filterFile = props.getProperty(XMLDBMSProps.FILTERFILE);
         if (filterFile == null) return;

         // We don't know if the filter filename is a URL or not. To find this
         // out, attempt to create a URL over the file name. If there is no
         // protocol, this will throw an exception. Hacky, but it works.

         try
         {
            // If the filename is a URL, open the URL connection and get
            // an output stream that writes to that connection.

            filterURL = new URL(filterFile);
            conn = filterURL.openConnection();
            conn.setDoOutput(true);
            outputStream = conn.getOutputStream();
         }
         catch (MalformedURLException m)
         {
            // If the filename is not a URL (i.e. it is a local filename),
            // create an OutputStream over the file.

            outputStream = new FileOutputStream(filterFile);
         }

         // Get the encoding (if any) and create an OutputStreamWriter accordingly.

         encoding = props.getProperty(XMLDBMSProps.ENCODING);
         writer = (encoding == null) ? new OutputStreamWriter(outputStream) :
                                       new OutputStreamWriter(outputStream, encoding);

         // Get the system ID and public ID of the filter file DTD.

         systemID = props.getProperty(XMLDBMSProps.SYSTEMID);
         publicID = props.getProperty(XMLDBMSProps.PUBLICID);

         // Serialize the filter set. We only write the DTD information if it is provided.

         serializer = new FilterSerializer(writer);
         serializer.setPrettyPrinting(true, 3);

         if (systemID == null)
         {
            serializer.serialize(filterSet);
         }
         else
         {
            serializer.serialize(filterSet, systemID, publicID);
         }

         // Close the writer. It does not appear that we can/need to close the URLConnection.

         writer.close();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   private void dispatchRetrieveDocumentByFilter(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String     mapFile, filterFile, xmlFile;
      Properties configProps = props;
      Hashtable  params      = props;

      // Get the names of the map, filter, and XML files

      mapFile = getProperty(props, XMLDBMSProps.MAPFILE);
      filterFile = getProperty(props, XMLDBMSProps.FILTERFILE);
      xmlFile = getProperty(props, XMLDBMSProps.XMLFILE);

      // Retrieve the document.

      retrieveDocument(configProps, mapFile, filterFile, params, xmlFile);
   }

   private void dispatchRetrieveDocumentBySQL(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String     mapFile, filterFile, xmlFile;
      Properties configProps = props, selects = props;
      Hashtable  params      = props;

      // Get the names of the map, filter, and XML files

      mapFile = getProperty(props, XMLDBMSProps.MAPFILE);
      filterFile = getProperty(props, XMLDBMSProps.FILTERFILE);
      xmlFile = getProperty(props, XMLDBMSProps.XMLFILE);

      // Retrieve the document.

      retrieveDocument(configProps, mapFile, selects, filterFile, params, xmlFile);
   }

   private void dispatchDeleteDocument(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String mapFile, actionFile, filterFile;

      // Get the names of the map, action, and filter files

      mapFile = getProperty(props, XMLDBMSProps.MAPFILE);
      actionFile = getProperty(props, XMLDBMSProps.ACTIONFILE);
      filterFile = getProperty(props, XMLDBMSProps.FILTERFILE);

      // Delete the document

      deleteDocument(props, mapFile, actionFile, filterFile, props);
   }

   // ************************************************************************
   // Private methods -- store, retrieve, and delete documents
   // ************************************************************************

   private FilterSet storeDocumentInternal(Properties configProps, String mapFilename, String actionFilename, InputSource src)
      throws XMLMiddlewareException, SQLException
   {
      XMLDBMSMap   map;
      TransferInfo transferInfo;
      Actions      actions;
      Document     doc;

      // Create the various objects needed by DOMToDBMS.storeDocument.

      map = createMap(mapFilename);
      transferInfo = createTransferInfo(map);
      actions = createActions(map, actionFilename);

      // Configure the DOMToDBMS object

      configDOMToDBMS(configProps);

      // Open a DOM tree over the InputSource and store it in the database

      doc = utils.openDocument(src);
      return domToDBMS.storeDocument(transferInfo, doc, actions);
   }

   private Document retrieveDocumentInternal(Properties configProps, String mapFilename, String filterFilename, Hashtable params)
      throws XMLMiddlewareException, SQLException
   {
      XMLDBMSMap   map;
      TransferInfo transferInfo;
      FilterSet    filterSet;

      // Create the various objects needed by DBMSToDOM.retrieveDocument.

      map = createMap(mapFilename);
      transferInfo = createTransferInfo(map);
      filterSet = createFilterSet(map, filterFilename);

      // Configure the DBMSToDOM object

      configDBMSToDOM(configProps);

      // Retrieve and return the document

      return dbmsToDOM.retrieveDocument(transferInfo, filterSet, params, null);
   }

   private Document retrieveDocumentInternal(Properties configProps, String mapFilename, Properties selects, String filterFilename, Hashtable params)
      throws XMLMiddlewareException, SQLException
   {
      XMLDBMSMap   map;
      Hashtable    resultSets;
      TransferInfo transferInfo;
      FilterSet    filterSet;
      Document     doc;

      // Create the various objects needed by DBMSToDOM.retrieveDocument.

      map = createMap(mapFilename);
      resultSets = createResultSets(selects);
      transferInfo = createTransferInfo(map);
      filterSet = createFilterSet(map, filterFilename);

      // Configure the DBMSToDOM object

      configDBMSToDOM(configProps);

      // Retrieve the document

      doc = dbmsToDOM.retrieveDocument(transferInfo, resultSets, filterSet, params, null);

      // Close the result sets and the connections they use.

      closeResultSets(resultSets);
      closeConnections();

      // Return the document

      return doc;
   }

   // ************************************************************************
   // Private methods -- configuration
   // ************************************************************************

   private void setParserUtils(Properties props)
      throws XMLMiddlewareException
   {
      String parserUtilsClass;

      // Get the ParserUtils class name

      parserUtilsClass = props.getProperty(XMLDBMSProps.PARSERUTILSCLASS);
      if (parserUtilsClass == null)
         throw new IllegalArgumentException("Properties object must contain " + XMLDBMSProps.PARSERUTILSCLASS + " property.");

      // Create a ParserUtils object and set the global

      utils = (ParserUtils)instantiateObject(parserUtilsClass);
   }

   private void configSingleDatabase(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String dbName, dataSourceClass, dataHandlerClass;
      DBInfo dbInfo;

      // Get the database name. If this is null, use "Default".

      dbName = props.getProperty(XMLDBMSProps.DBNAME);
      if (dbName == null) dbName = DEFAULT;

      // Create a new DBInfo object.

      dbInfo = new DBInfo();
      dbInfos.put(dbName, dbInfo);

      // Create the DataSource object and fill in the DBInfo object.

      dataSourceClass = props.getProperty(XMLDBMSProps.DATASOURCECLASS);
      dbInfo.dataSource = createDataSource(dbName, dataSourceClass, props, 0);
      dbInfo.user = props.getProperty(XMLDBMSProps.USER);
      dbInfo.password = props.getProperty(XMLDBMSProps.PASSWORD);

      // Create the DataHandler object.

      dataHandlerClass = props.getProperty(XMLDBMSProps.DATAHANDLERCLASS);
      createDataHandler(dbName, dataHandlerClass, dbInfo);
   }

   private void configMultipleDatabases(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String[] dbNames, dataSourceClasses, dataHandlerClasses, users, passwords;
      String   dataHandlerClass;
      DBInfo   dbInfo;

      // Get the various property values.

      dbNames = getNumberedProps(XMLDBMSProps.DBNAME, props);
      dataSourceClasses = getNumberedProps(XMLDBMSProps.DATASOURCECLASS, props);
      dataHandlerClasses = getNumberedProps(XMLDBMSProps.DATAHANDLERCLASS, props);
      users = getNumberedProps(XMLDBMSProps.USER, props);
      passwords = getNumberedProps(XMLDBMSProps.PASSWORD, props);

      // Check that we have valid properties.

      if ((dbNames == null) || (dataSourceClasses == null))
         throw new IllegalArgumentException("You must specify a single DataSourceClass property, or one or more DBName1 (2, 3, ...) and DataSourceClass1 (2, 3, ...) properties.");

      // Process the properties.

      try
      {
         for (int i = 0; i < dataSourceClasses.length; i++)
         {
            // Create a new DBInfo object.

            dbInfo = new DBInfo();
            dbInfos.put(dbNames[i], dbInfo);

            // Create the DataSource object and fill in the DBInfo object.

            dbInfo.dataSource = createDataSource(dbNames[i], dataSourceClasses[i], props, i);
            dbInfo.user = (users == null) ? null : users[i];
            dbInfo.password = (passwords == null) ? null : passwords[i];

            // Create the DataHandler object.

            dataHandlerClass = (dataHandlerClasses == null) ? null : dataHandlerClasses[i];
            createDataHandler(dbNames[i], dataHandlerClass, dbInfo);
         }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new XMLMiddlewareException("If you specify them, you must have the same number of " + XMLDBMSProps.DBNAME + ", " + XMLDBMSProps.DATASOURCECLASS + ", " + XMLDBMSProps.DATAHANDLERCLASS + ", " + XMLDBMSProps.USER + ", and " + XMLDBMSProps.PASSWORD + " properties.");
      }
   }

   private void configDBMSToDOM(Properties configProps)
      throws XMLMiddlewareException
   {
      String systemID = null, publicID = null;

      // Create a DBMSToDOM object if one doesn't already exist

      try
      {
         if (dbmsToDOM == null) dbmsToDOM = new DBMSToDOM(utils);
      }
      catch (SAXException e)
      {
         processSAXException(e);
      }

      // If there are configuration properties, get the system ID and public ID
      // of the document being created.

      if (configProps != null)
      {
         systemID = configProps.getProperty(XMLDBMSProps.SYSTEMID);
         publicID = configProps.getProperty(XMLDBMSProps.PUBLICID);
      }

      // Set the system ID and public ID to use. These may be null.

      dbmsToDOM.setDTDInfo(systemID, publicID);
   }

   private void configDOMToDBMS(Properties configProps)
      throws XMLMiddlewareException
   {
      String  value;
      int     commitMode = DataHandler.COMMIT_AFTERSTATEMENT;
      boolean stopOnError = true, returnFilterSet = false;

      // Create a DOMToDBMS object if one doesn't already exist

      if (domToDBMS == null) domToDBMS = new DOMToDBMS();

      // Add the key generators (if any).

      addKeyGenerators(configProps);

      // If there are configuration properties, get the commit mode,
      // whether to stop on errors, and whether to return a FilterSet.

      if (configProps != null)
      {
         value = configProps.getProperty(XMLDBMSProps.COMMITMODE);
         if (value != null) commitMode = getCommitMode(value);
         value = configProps.getProperty(XMLDBMSProps.STOPONERROR);
         if (value != null) stopOnError = isYes(value);
         value = configProps.getProperty(XMLDBMSProps.RETURNFILTER);
         if (value != null) returnFilterSet = isYes(value);
      }

      // Configure the DOMToDBMS object. Note that we use the default
      // values if for any configuration properties not set.

      domToDBMS.setCommitMode(commitMode);
      domToDBMS.stopOnException(stopOnError);
      domToDBMS.setFilterSetReturned(returnFilterSet);
   }

   private void configDBMSDelete(Properties configProps)
   {
      String value;
      int    commitMode = DataHandler.COMMIT_AFTERSTATEMENT;

      // Create a DBMSDelete object if one doesn't already exist

      if (dbmsDelete == null) dbmsDelete = new DBMSDelete();

      // If there are configuration properties, get the commit mode (if any).

      if (configProps != null)
      {
         value = configProps.getProperty(XMLDBMSProps.COMMITMODE);
         if (value != null) commitMode = getCommitMode(value);
      }

      // Set the commit mode.

      dbmsDelete.setCommitMode(commitMode);
   }

   private void addKeyGenerators(Properties configProps)
      throws XMLMiddlewareException
   {
      // Remove all current key generators.

      domToDBMS.removeAllKeyGenerators();

      // If there are no configuration properties, we are done.

      if (configProps == null) return;

      // Check if there is a single or multiple key generators and process
      // accordingly.

      if (configProps.getProperty(XMLDBMSProps.KEYGENERATORNAME) != null)
      {
         addSingleKeyGenerator(configProps);
      }
      else
      {
         addMultipleKeyGenerators(configProps);
      }
   }

   private void addSingleKeyGenerator(Properties configProps)
      throws XMLMiddlewareException
   {
      String       name, className;
      KeyGenerator keyGen;

      // Get the logical name and class of the key generator, then
      // instantiate it.

      name = configProps.getProperty(XMLDBMSProps.KEYGENERATORNAME);
      className = configProps.getProperty(XMLDBMSProps.KEYGENERATORCLASS);
      keyGen = createKeyGenerator(name, className, configProps);

      // Add the key generator to DOMToDBMS.

      domToDBMS.addKeyGenerator(name, keyGen);
   }

   private void addMultipleKeyGenerators(Properties configProps)
      throws XMLMiddlewareException
   {
      String[]     names, classes;
      KeyGenerator keyGen;

      // Get the logical key generator names. If there are none, just return.

      names = getNumberedProps(XMLDBMSProps.KEYGENERATORNAME, configProps);
      if (names == null) return;

      // Get the key generator classes. Note that we throw an error if none
      // are specified or too few are specified.

      classes = getNumberedProps(XMLDBMSProps.KEYGENERATORCLASS, configProps);
      try
      {
         if (classes == null) throw new ArrayIndexOutOfBoundsException();
         for (int i = 0; i < names.length; i++)
         {
            // Instantiate the key generator and add it to the DOMToDBMS object.

            keyGen = createKeyGenerator(names[i], classes[i], configProps);
            domToDBMS.addKeyGenerator(names[i], keyGen);
         }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new XMLMiddlewareException("The number of " + XMLDBMSProps.KEYGENERATORNAME + " properties must match the number of " + XMLDBMSProps.KEYGENERATORCLASS + " properties.");
      }
   }

   // ************************************************************************
   // Private methods -- object creation
   // ************************************************************************

   private DataSource createDataSource(String dbName, String dataSourceClass, Properties props, int suffix)
   {
      DBInfo     dbInfo;
      DataSource dataSource;

      if (dataSourceClass == null)
         throw new IllegalArgumentException("You must specify a DataSourceClass property");

      // Get the DBInfo object for the database from the hashtable and
      // see if we have already created a DataSource for this database.
      // If so, reuse it. If not, create it now.

      dbInfo = (DBInfo)dbInfos.get(dbName);
      dataSource = dbInfo.dataSource;
      if (dataSource == null)
      {
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
      }

      // Return the DataSource

      return dataSource;
   }

   private DataSource createJDBC1DataSource(Properties props, int suffix)
   {
      String driver, url, prop;

      // Construct the name of the driver property and get it now. Suffixes
      // are non-zero when there is more than one database.

      prop = (suffix == 0) ? XMLDBMSProps.DRIVER : XMLDBMSProps.DRIVER + suffix;
      driver = props.getProperty(prop);
      if (driver == null)
         throw new IllegalArgumentException("You must specify the driver when using a JDBC1DataSource.");

      // Construct the name of the URL property and get it now.

      prop = (suffix == 0) ? XMLDBMSProps.URL : XMLDBMSProps.URL + suffix;
      url = props.getProperty(prop);
      if (url == null)
         throw new IllegalArgumentException("You must specify the URL when using a JDBC1DataSource.");

      // Create the DataSource

      return new JDBC1DataSource(driver, url);
   }

   private DataSource createJDBC2DataSource(Properties props, int suffix)
   {
      String jndiContext, jndiLookupName, prop;

      // Construct the name of the JNDI context property and get it now. Suffixes
      // are non-zero when there is more than one database.

      prop = (suffix == 0) ? XMLDBMSProps.JNDICONTEXT : XMLDBMSProps.JNDICONTEXT + suffix;
      jndiContext = props.getProperty(prop);
      if (jndiContext == null)
         throw new IllegalArgumentException("You must specify the JNDI context when using a JDBC2DataSource.");

      // Construct the name of the JNDI lookup name property and get it now.

      prop = (suffix== 0) ? XMLDBMSProps.JNDILOOKUPNAME : XMLDBMSProps.JNDILOOKUPNAME + suffix;
      jndiLookupName = props.getProperty(prop);
      if (jndiLookupName == null)
         throw new IllegalArgumentException("You must specify the JNDI lookup name of the data source when using a JDBC2DataSource.");

      // Create the DataSource

      return new JDBC2DataSource(jndiContext, jndiLookupName);
   }

   private void createDataHandler(String dbName, String dataHandlerClass, DBInfo dbInfo)
      throws XMLMiddlewareException, SQLException
   {
      DataHandler dataHandler;

      // If no DataHandler class is specified, use GenericHandler.

      if (dataHandlerClass == null) dataHandlerClass = GENERICHANDLER;

      // Instantiate and initialize the data handler class, then store it in
      // the hashtable of DataHandlers.

      dataHandler = (DataHandler)instantiateObject(dataHandlerClass);
      dataHandler.initialize(dbInfo.dataSource, dbInfo.user, dbInfo.password);
      dataHandlers.put(dbName, dataHandler);
   }

   private Hashtable createResultSets(Properties selects)
      throws XMLMiddlewareException, SQLException
   {
      if (selects == null)
         throw new IllegalArgumentException("You must specify one or more Select properties when retrieving data from a result set.");

      // Check if we are using a single SELECT property or
      // SELECT1, SELECT2, etc. properties and act accordingly.

      if (selects.getProperty(XMLDBMSProps.SELECT) != null)
      {
         return createSingleResultSet(selects);
      }
      else
      {
         return createMultipleResultSets(selects);
      }
   }

   private Hashtable createSingleResultSet(Properties selects)
      throws XMLMiddlewareException, SQLException
   {
      Hashtable  resultSets = new Hashtable();
      String     select, dbName, rsName;
      DBInfo     dbInfo;
      Connection conn;
      Statement  stmt;
      ResultSet  rs;

      // Get the SELECT statement, the result set name, and the database name.
      // Note that we use Default as the result set name and database name if
      // none are specified.

      select = selects.getProperty(XMLDBMSProps.SELECT);

      rsName = selects.getProperty(XMLDBMSProps.SELECTRESULTSETNAME);
      if (rsName == null) rsName = DEFAULT;

      dbName = selects.getProperty(XMLDBMSProps.SELECTDBNAME);
      if (dbName == null) dbName = DEFAULT;

      // Get the information for the database.

      dbInfo = (DBInfo)dbInfos.get(dbName);
      if (dbInfo == null)
         throw new IllegalArgumentException("No data source information specified for the " + dbName + " database.");

      // Connect to the database and execute the SELECT statement.

      conn = dbInfo.dataSource.getConnection(dbInfo.user, dbInfo.password);
      stmt = conn.createStatement();
      rs = stmt.executeQuery(select);

      // Store the result set so we can read it. Store the connection so
      // we can close it later. Note that the connection is stored in a global,
      // which is somewhat hacky, but I'm too lazy to create yet another
      // enclosing object.

      resultSets.put(rsName, rs);
      conns.addElement(conn);

      // Return the result set hashtable.

      return resultSets;
   }

   private Hashtable createMultipleResultSets(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      Hashtable  resultSets = new Hashtable();
      String[]   selects, dbNames, rsNames;
      String     dbName;
      DBInfo     dbInfo;
      Connection conn;
      Statement  stmt;
      ResultSet  rs;

      // Get the SELECT statements.

      selects = getNumberedProps(XMLDBMSProps.SELECT, props);
      if (selects == null)
         throw new IllegalArgumentException("You must specify one or more SELECT statements when retrieving data from a result set.");

      // Get the result set names. These must be present and equal in
      // number to the SELECT statements.

      rsNames = getNumberedProps(XMLDBMSProps.SELECTRESULTSETNAME, props);
      if (rsNames == null)
         throw new IllegalArgumentException("You must specify the names of the result sets created from the SELECT statements. These must match the names of the result sets in the filter document.");
      if (rsNames.length != selects.length)
         throw new IllegalArgumentException("There must be the same number of " + XMLDBMSProps.SELECTRESULTSETNAME + " properties as " + XMLDBMSProps.SELECT + " properties.");

      // Get the database names. If these are missing, we will use "Default". If
      // these are present, they must be equal in number to the SELECT statements.

      dbNames = getNumberedProps(XMLDBMSProps.SELECTDBNAME, props);
      if (dbNames != null)
         if (dbNames.length != selects.length)
            throw new IllegalArgumentException("There must be the same number of " + XMLDBMSProps.SELECTDBNAME + " properties as " + XMLDBMSProps.SELECT + " properties.");

      // Create the result sets.

      for (int i = 0; i < selects.length; i++)
      {
         // Get the information for the database.

         dbName = (dbNames == null) ? DEFAULT : dbNames[i];
         dbInfo = (DBInfo)dbInfos.get(dbName);
         if (dbInfo == null)
            throw new IllegalArgumentException("No data source information specified for the " + dbName + " database.");

         // Connect to the database and execute the SELECT statement.
         conn = dbInfo.dataSource.getConnection(dbInfo.user, dbInfo.password);
         stmt = conn.createStatement();
         rs = stmt.executeQuery(selects[i]);

         // Store the result set so we can read it. Store the connection so
         // we can close it later.

         resultSets.put(rsNames[i], rs);
         conns.addElement(conn);
      }

      // Return the result sets.

      return resultSets;
   }

   private void closeResultSets(Hashtable resultSets)
   {
      Enumeration enum;
      ResultSet   rs;

      // Close the result sets.

      enum = resultSets.elements();
      while (enum.hasMoreElements())
      {
         rs = (ResultSet)enum.nextElement();
         try
         {
            rs.close();
         }
         catch (SQLException e)
         {
            // Ignore errors and continue closing result sets
         }
      }
   }

   private void closeConnections()
   {
      Connection conn;

      // Close the connections used by the result sets.

      for (int i = 0; i < conns.size(); i++)
      {
         conn = (Connection)conns.elementAt(i);
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
            // Ignore errors and continue closing result sets
         }
      }
   }

   private TransferInfo createTransferInfo(XMLDBMSMap map)
   {
      TransferInfo transferInfo;
      Enumeration  dbNames;
      String       dbName;
      DataHandler  dataHandler;

      // Check if we have already created a TransferInfo object for this
      // map. If so, use it. If not, create it now.

      transferInfo = (TransferInfo)transferInfos.get(map);
      if (transferInfo == null)
      {
         // Create a new TransferInfo object and cache it.

         transferInfo = new TransferInfo(map);
         transferInfos.put(map, transferInfo);

         // Add all current databases to the TransferInfo object.

         dbNames = dataHandlers.keys();
         while (dbNames.hasMoreElements())
         {
            dbName = (String)dbNames.nextElement();
            dataHandler = (DataHandler)dataHandlers.get(dbName);
            transferInfo.addDataHandler(dbName, dataHandler);
         }
      }

      // Return the TransferInfo object.

      return transferInfo;
   }

   private KeyGenerator createKeyGenerator(String name, String className, Properties configProps)
      throws XMLMiddlewareException
   {
      KeyGenerator keyGen;

      // Check if the key generator already exists. If not, create it now.
      // Note that we assume that any initialization properties are passed
      // in with the rest of the properties, such as on the command line.

      keyGen = (KeyGenerator)keyGenerators.get(name);
      if (keyGen == null)
      {
         keyGen = (KeyGenerator)instantiateObject(className);
         keyGen.initialize(configProps);
         keyGenerators.put(name, keyGen);
      }
      return keyGen;
   }

   private XMLDBMSMap createMap(String mapFilename)
      throws XMLMiddlewareException
   {
      MapCompiler compiler = null;
      String      url;
      XMLDBMSMap  map;

      // Build a URL string from the map file name. See PropertyProcessor.buildURLString
      // for details.

      url = buildURLString(null, mapFilename);

      // Check if we have already compiled the map file. If so, use the cached
      // XMLDBMSMap object. If not create a new map compiler and compile the map file.

      map = (XMLDBMSMap)fileObjects.get(url);
      if (map == null)
      {
         try
         {
            compiler = new MapCompiler(utils.getXMLReader());
         }
         catch (SAXException e)
         {
            processSAXException(e);
         }
         map = compiler.compile(new InputSource(url));
         fileObjects.put(url, map);
      }
      return map;
   }

   private Actions createActions(XMLDBMSMap map, String actionFilename)
      throws XMLMiddlewareException
   {
      ActionCompiler compiler = null;
      String         url;
      Actions        actions;

      // Build a URL string from the action file name. See PropertyProcessor.buildURLString
      // for details.

      url = buildURLString(null, actionFilename);

      // Check if we have already compiled the action file. If so, use the cached
      // Actions object. If not create a new action compiler and compile the action file.

      actions = (Actions)fileObjects.get(url);
      if (actions == null)
      {
         try
         {
            compiler = new ActionCompiler(utils.getXMLReader());
         }
         catch (SAXException e)
         {
            processSAXException(e);
         }
         actions = compiler.compile(map, new InputSource(url));
         fileObjects.put(url, actions);
      }
      return actions;
   }

   private FilterSet createFilterSet(XMLDBMSMap map, String filterFilename)
      throws XMLMiddlewareException
   {
      FilterCompiler compiler = null;
      String         url;
      FilterSet      filterSet;

      // Build a URL string from the filter file name. See PropertyProcessor.buildURLString
      // for details.

      url = buildURLString(null, filterFilename);

      // Check if we have already compiled the filter file. If so, use the cached
      // FilterSet object. If not create a new filter compiler and compile the filter file.

      filterSet = (FilterSet)fileObjects.get(url);
      if (filterSet == null)
      {
         try
         {
            compiler = new FilterCompiler(utils.getXMLReader());
         }
         catch (SAXException e)
         {
            processSAXException(e);
         }
         filterSet = compiler.compile(map, new InputSource(url));
         fileObjects.put(url, filterSet);
      }
      return filterSet;
   }

   // ************************************************************************
   // Private methods -- process properties
   // ************************************************************************

   private String getProperty(Properties props, String property)
   {
      String value;

      value = props.getProperty(property);
      if (value == null)
         throw new IllegalArgumentException("Invalid call. The " + property + " property must be set when executing the " + props.getProperty(XMLDBMSProps.METHOD) + " method.");
      else
         return value;
   }

   private int getCommitMode(String modeName)
   {
      if (modeName.equals(XMLDBMSProps.AFTERSTATEMENT))
      {
         return DataHandler.COMMIT_AFTERSTATEMENT;
      }
      else if (modeName.equals(XMLDBMSProps.AFTERDOCUMENT))
      {
         return DataHandler.COMMIT_AFTERDOCUMENT;
      }
      else if (modeName.equals(XMLDBMSProps.NONE))
      {
         return DataHandler.COMMIT_NONE;
      }
      else if (modeName.equals(XMLDBMSProps.NOTRANSACTIONS))
      {
         return DataHandler.COMMIT_NOTRANSACTIONS;
      }
      else
         throw new IllegalArgumentException("Invalid commit mode value: " + modeName);
   }

   private boolean isYes(String yesNo)
   {
      return (yesNo.toUpperCase().equals(YES));
   }

   // ************************************************************************
   // Private methods -- various
   // ************************************************************************

   private void setFinalizers()
   {
      // Tell the JVM to run finalizers on exit. This is necessary to ensure
      // that database connections are properly closed.

      System.runFinalizersOnExit(true);
   }

   private Object instantiateObject(String className)
      throws XMLMiddlewareException
   {
      try
      {
         return Class.forName(className).newInstance();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e.getClass().getName() + ": " + e.getMessage());
      }
   }

   private void processSAXException(SAXException s)
      throws XMLMiddlewareException
   {
      Exception e;

      // Get the embedded Exception (if any) and check if it's a XMLMiddlewareException.

      e = s.getException();
      if (e != null)
      {
         if (e instanceof XMLMiddlewareException)
            throw (XMLMiddlewareException)e;
         else
            throw new XMLMiddlewareException(e);
      }
      else
         throw new XMLMiddlewareException(s);
   }

   // ************************************************************************
   // Inner class
   // ************************************************************************

   private class DBInfo
   {
      DataSource dataSource;
      String     user;
      String     password;

      DBInfo()
      {
      }
   }
}
