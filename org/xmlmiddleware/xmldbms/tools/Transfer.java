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
import org.xmlmiddleware.xmldbms.maps.utils.*;
import org.xmlmiddleware.xmldbms.tools.resolvers.*;
import org.xmlmiddleware.xmlutils.*;

import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.*;
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
 * document location (file, URL, etc.), a string, or an input or output stream.
 * Strings are a useful way to represent an XML document, since they can
 * easily be passed to/from an XSLT processor, HTTP, etc. The dispatch-style API
 * interface can transfer data between the database and a document location.
 * The command line interface can only transfer data between a database and a file.</p>
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
 *     The only property-processing property is File.</p></li>
 *
 * <li><p>Parser properties provide information about the XML parser / DOM
 *     implementation. The only parser property is ParserUtilsClass.</p></li>
 *
 * <li><p>Database properties are used to connect to the database(s). The
 *     database properties are DBName, DataHandlerClass, DataSourceClass,
 *     User, and Password. If there is more than one database, use
 *     sequentially numbered versions of these properties -- DBName1, DBName2, etc.</p>
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
 *     delete a document) and the document locations to use. The transfer properties
 *     are Method, MapLocation, XMLLocation, ActionLocation, FilterLocation,
 *     MapResolverClass, XMLResolverClass, ActionResolverClass, and
 *     FilterResolverClass. See below for details.</p></li>
 *
 * <li><p>Select properties specify result sets to use when retrieving data.
 *     The select properties are Select, SelectDBName, and SelectResultSetName.</p></li>
 *
 * <li><p>Configuration properties specify how the underlying data transfer
 *     classes are to function. The configuration properties are Encoding, SystemID,
 *     PublicID, CommitMode, StopOnError, ReturnFilter, KeyGeneratorName, and
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
 * <tr><th>Value of Method property</th><th>Transfer properties</th></tr>
 * <tr valign="top"><td>StoreDocument</td><td>MapLocation<br />XMLLocation[1]<br />
 * ActionLocation[1]<br />FilterLocation[1] (when ReturnFilter is "Yes")</td></tr>
 * <tr valign="top"><td>RetrieveDocumentByFilter</td><td>MapLocation[1]<br />
 * XMLLocation[1]<br />FilterLocation[1][2]<br />INParameters[3]</td></tr>
 * <tr valign="top"><td>RetrieveDocumentBySQL</td><td>MapLocation<br />XMLLocation[1]
 * <br />FilterLocation[1][2]<br />INParameters[3]<br />Select[4]<br />SelectDBName[4][5]
 * <br />SelectResultSetName[4][6]</td></tr>
 * <tr valign="top"><td>DeleteDocument</td><td>MapLocation[1]<br />ActionLocation[1]
 * <br />FilterLocation[1]<br />INParameters[3]</td></tr>
 * </table>
 *
 * <p>NOTES:<br />
 * [1] XxxxLocation properties provide the location of named resources, such as
 * a filename or URL. A location name is resolved by a class that implements
 * the LocationResolver interface; this is specified with the XxxxResolverClass property
 * (see below). If no XxxxResolverClass is specified, then the
 * org.xmlmiddleware.xmldbms.tools.resolvers.FilenameResolver class is used. That is,
 * the location name is assumed to be a filename.<br /><br />
 * [2] If the filter document uses parameters, these should be passed in as well.
 * Because parameter names begin with a dollar sign ($), there should be no conflict
 * between parameter names and the names of other properties.<br /><br />
 * [3] Space-separated list of names of parameters that are used in IN clauses in
 * filters. For example, if the $SONumber and $PartNumber parameters are used in
 * IN clauses, INParameters would be declared as follows:
 * <br /><br />
 * <pre>   INParameter=$SONumber $PartNumber</pre>
 * <br />
 * The value of a parameter used in a IN clause must be a space-separated list of values.
 * For example, if the value of $SONumber is the sales order numbers 123 and 456, $SONumber
 * would be declared as follows:
 * <br /><br />
 * <pre>   $SONumber=123 456</pre>
 * <br />
 * If a value has a space in it, escape this with a backslash (all other backslashes
 * are treated as literals). For example, to use the three sales order numbers 123, 456,
 * and 789&nbsp;012, declare $SONumber as follows:
 * <br /><br />
 * <pre>   $SONumber=123 456 789\ 012</pre>
 * <br />
 * Note that backslashes in property files are treated as escape characters, so the
 * backslash in the above property/value pair in a property file would need to be:
 * <br /><br />
 * <pre>   $SONumber=123 456 789\\ 012</pre>
 * <br />
 * [4] If there is more than one result set, use Select1, Select2, ...,
 * SelectDBName1, SelectDBName2, etc.<br /><br />
 * [5] Optional. If no database name is specified, "Default" is used.<br /><br />
 * [6] Optional if there is only one result set, in which case "Default" is used.
 * Required if there is more than one result set. Result set names correspond to
 * result set names in the filter document.</p>
 *
 * <p>The following table shows which configuration properties apply to each value
 * of the Method property. These properties are also used by the methods in the
 * traditional API.</p>
 *
 * <table border="1" cellpadding="3">
 * <tr valign="top"><th>Value of Method property<br />(Method)</th>
 * <th>Configuration properties</th></tr>
 * <tr valign="top"><td>StoreDocument<br />(storeXMLXxxxx)</td><td>CommitMode[1]
 * <br />StopOnError<br />ReturnFilter<br />KeyGeneratorName[2]<br />KeyGeneratorClass[2][3]
 * <br />Encoding[4]<br />SystemID[4]<br />PublicID[4]<br />Validate[5]<br />
 * MapResolverClass[6]<br />XMLResolverClass[6]<br />ActionResolverClass[6]
 * <br />FilterResolverClass[6]</td></tr>
 * <tr valign="top"><td>RetrieveDocumentByFilter<br />RetrieveDocumentBySQL<br />
 * (retrieveXMLXxxx)</td>
 * <td>Encoding<br />SystemID<br />PublicID<br />Validate[7]<br />
 * MapResolverClass[6]<br />XMLResolverClass[6]<br />FilterResolverClass[6]</td></tr>
 * <tr valign="top"><td>DeleteDocument<br />(deleteXMLDocument)</td>
 * <td>CommitMode[1]<br />Validate[8]<br />MapResolverClass[6]<br />XMLResolverClass[6]
 * <br />ActionResolverClass[6]<br />FilterResolverClass[6]</td></tr>
 * </table>
 *
 * <p>NOTES:<br />
 * [1] Legal values of CommitMode are AfterStatement, AfterDocument, None, and
 * NoTransactions.<br /><br />
 * [2] If there is more than one key generator, use KeyGeneratorName1,
 * KeyGeneratorName2, ... KeyGeneratorClass1, KeyGeneratorClass2, etc.<br /><br />
 * [3] If the key generator requires initialization properties, these should be
 * passed in as well. If there is more than one key generator, the initialization
 * properties should have the same numerical suffix as KeyGeneratorName and
 * KeyGeneratorClass. See the documentation for your key generator for information
 * about the initialization properties.<br /><br />
 * [4] Applies to the output filter document, if any.<br /><br />
 * [5] Value is a space-separated list containing Map, XML, and/or Action.<br /><br />
 * [6] Optional. XxxxResolverClass properties specify the class used to resolve XxxxLocations.
 * For example, if a location is a URL, then the resolver class should be
 * org.xmlmiddleware.xmldbms.tools.resolvers.URLResolver. The XxxxResolverClass
 * properties are optional; org.xmlmiddleware.xmldbms.tools.resolvers.FilenameResolver
 * is used by default. That is, locations are assumed to be filenames by default.<br /><br />
 * [7] Value is a space-separated list containing Map and/or Filter.<br /><br />
 * [8] Value is a space-separated list containing Map, Action, and/or Filter.</p>
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
 * sales.xml file in the database:</p>
 *
 * <pre>
 *   java org.xmlmiddleware.xmldbms.tools.Transfer 
 *                  ParserUtilsClass=org.xmlmiddleware.xmlutils.external.ParserUtilsXerces
 *                  DataSourceClass=JDBC1DataSource
 *                  Driver=sun.jdbc.odbc.JdbcOdbcDriver URL=jdbc:odbc:xmldbms
 *                  User=ron Password=ronpwd
 *                  Method=StoreDocument MapLocation=sales.map
 *                  XMLLocation=sales.xml ActionLocation=sales.act
 * </pre>
 *
 * <p>Notice that no XxxxResolverClass properties are present, so XxxxLocation properties
 * are interpreted as filenames.</p>
 *
 * <p>A special property, File, can be used to designate a file containing
 * other properties. For example, if the parser properties are stored in
 * xerces.props and the database properties stored in db.props, the following
 * is equivalent to the previous command line:</p>
 *
 * <pre>
 *   java org.xmlmiddleware.xmldbms.tools.Transfer File1=xerces.props File2=db.props
 *                  Method=StoreDocument MapLocation=sales.map
 *                  XMLLocation=sales.xml ActionLocation=sales.act
 * </pre>
 *
 * <p>Notice that when more than one File property is used, the File properties
 * must be numbered sequentially. File properties can also be used inside
 * property files, making it possible to have a hierarchy of property files.
 * File properties can be used from the command line and with the
 * dispatch-style interface. They cannot be used with the traditional API.</p>
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
 * methods allow you to transfer data between the database and an XML document, a string, or
 * an input or output stream. Applications using this interface must call
 * setDatabaseProperties() before calling any of the other methods.</p>
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
 * the item to which the key points (such as a map document) changes between calls to
 * these methods, the new object will not be used. To use the new object, applications must
 * instantiate and use a new Transfer object. Note that database objects (DataSource,
 * DataHandler, etc.) are cached between calls to setDatabaseProperties().</p>
 *
 * <pre>
 * XMLDBMSMap (MapLocation)
 * Actions (ActionLocation)
 * FilterSet (FilterLocation)
 * KeyGenerator (KeyGeneratorName)
 * LocationResolver (MapResolverClass, XMLResolverClass, ActionResolverClass, FilterResolverClass)
 * </pre>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.tools.XMLDBMSProps
 * @see org.xmlmiddleware.xmldbms.tools.resolvers.LocationResolver
 */

public class Transfer extends PropertyProcessor
{
   // ************************************************************************
   // Class variables
   // ************************************************************************

   ParserUtils utils;
   Hashtable   locationObjects = new Hashtable(),
               keyGenerators = new Hashtable(),
               dbMaps = new Hashtable(),
               dbInfos = new Hashtable(),
               dataHandlers = new Hashtable(),
               resolvers = new Hashtable();
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
   private static String FILENAMERESOLVER = "org.xmlmiddleware.xmldbms.tools.resolvers.FilenameResolver";
   private static String DEFAULT = "Default";
   private static String YES = "YES";

   private static int NORMAL = 0;
   private static int BACKSLASH = 1;

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

      dbMaps.clear();
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
    * Store (insert or update) data from an XML document.
    *
    * <p>See the introduction for details about the configProps, mapLocation,
    * actionLocation, and xmlLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param actionLocation Location of the action document.
    * @param xmlLocation Location of the XML document
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    * invalid map document, class not found, etc.
    * @return A FilterSet describing the stored document. This is returned only if
    *    the ReturnFilter property is set to "Yes".
    */
   public FilterSet storeXMLDocument(Properties configProps, String mapLocation, String actionLocation, String xmlLocation)
   throws SQLException, XMLMiddlewareException
   {
      InputSource src;
      LocationResolver resolver;

      resolver = getLocationResolver(configProps, XMLDBMSProps.XMLRESOLVERCLASS);
      src = getInputSource(resolver, xmlLocation);
      return storeDocumentInternal(configProps, mapLocation, actionLocation, src);
   }

   /**
    * Store (insert or update) data from an XML string.
    *
    * <p>See the introduction for details about the configProps, mapLocation,
    * and actionLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param actionLocation Location of the action document.
    * @param xmlString The XML document
    * @return A FilterSet describing the stored document. This is returned only if
    *    the ReturnFilter property is set to "Yes".
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public FilterSet storeXMLString(Properties configProps, String mapLocation, String actionLocation, String xmlString)
      throws SQLException, XMLMiddlewareException
   {
      InputSource src;

      src = new InputSource(new StringReader(xmlString));
      return storeDocumentInternal(configProps, mapLocation, actionLocation, src);
   }

   /**
    * Store (insert or update) data from an InputStream containing XML.
    *
    * <p>See the introduction for details about the configProps, mapLocation,
    * and actionLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param actionLocation Location of the action document.
    * @param stream The InputStream containing XML.
    * @return A FilterSet describing the stored document. This is returned only if
    *    the ReturnFilter property is set to "Yes".
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public FilterSet storeXMLInputStream(Properties configProps, String mapLocation, String actionLocation, InputStream stream)
      throws SQLException, XMLMiddlewareException
   {
      InputSource src;

      src = new InputSource(stream);
      return storeDocumentInternal(configProps, mapLocation, actionLocation, src);
   }

   /**
    * Retrieve data as an XML document.
    *
    * <p>See the introduction for details about the configProps, mapLocation,
    * filterLocation, and xmlLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param filterLocation Location of the filter document.
    * @param params A Hashtable of filter parameters. May be null. Note that
    *    this may be a Properties object, since Properties inherits from Hashtable.
    * @param xmlLocation Location of the XML document.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void retrieveXMLDocument(Properties configProps, String mapLocation, String filterLocation, Hashtable params, String xmlLocation)
      throws SQLException, XMLMiddlewareException
   {
      String           encoding = null;
      Document         doc;
      LocationResolver resolver;

      if (configProps != null) encoding = configProps.getProperty(XMLDBMSProps.ENCODING);

      doc = retrieveDocumentInternal(configProps, mapLocation, filterLocation, params);
      resolver = getLocationResolver(configProps, XMLDBMSProps.XMLRESOLVERCLASS);
      writeDocument(resolver, doc, xmlLocation, encoding);
   }

   /**
    * Retrieve data as an XML string.
    *
    * <p>See the introduction for details about the configProps, mapLocation,
    * and filterLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param filterLocation Location of the filter document.
    * @param params A Hashtable of filter parameters. May be null. Note that
    *    this may be a Properties object, since Properties inherit from Hashtable.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    * @return A String containing the XML document
    */
   public String retrieveXMLString(Properties configProps, String mapLocation, String filterLocation, Hashtable params)
      throws SQLException, XMLMiddlewareException
   {
      Document doc;

      doc = retrieveDocumentInternal(configProps, mapLocation, filterLocation, params);
      return utils.writeDocument(doc);
   }

   /**
    * Retrieve data as XML written to an OutputStream.
    *
    * <p>See the introduction for details about the configProps, mapLocation,
    * and filterLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param filterLocation Location of the filter document.
    * @param params A Hashtable of filter parameters. May be null. Note that this
    *    may be a Properties object, since Properties inherit from Hashtable.
    * @param stream The OutputStream in which to return the XML.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void retrieveXMLOutputStream(Properties configProps, String mapLocation, String filterLocation, Hashtable params, OutputStream stream)
      throws SQLException, XMLMiddlewareException
   {
      Document doc;

      doc = retrieveDocumentInternal(configProps, mapLocation, filterLocation, params);
      utils.writeDocument(doc, stream);
   }

   /**
    * Retrieve a result set as an XML document.
    *
    * <p>If the class map for the element type corresponding to the result
    * set contains related classes, this method retrieves additional data
    * from the database. See the introduction for details about the configProps,
    * mapLocation, filterLocation, selects, and xmlLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param selects A Properties object describing the result set.
    * @param filterLocation Location of the filter document.
    * @param params A Hashtable of filter parameters. May be null. Note that
    *    this may be a Properties object, since Properties inherit from Hashtable.
    * @param xmlLocation Location of the XML document.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void retrieveXMLDocument(Properties configProps, String mapLocation, Properties selects, String filterLocation, Hashtable params, String xmlLocation)
      throws SQLException, XMLMiddlewareException
   {
      String           encoding = null;
      Document         doc;
      LocationResolver resolver;

      if (configProps != null) encoding = configProps.getProperty(XMLDBMSProps.ENCODING);

      doc = retrieveDocumentInternal(configProps, mapLocation, selects, filterLocation, params);
      resolver = getLocationResolver(configProps, XMLDBMSProps.XMLRESOLVERCLASS);
      writeDocument(resolver, doc, xmlLocation, encoding);
   }

   /**
    * Retrieve a result set as an XML string.
    *
    * <p>If the class map for the element type corresponding to the result
    * set contains related classes, this method retrieves additional data
    * from the database. See the introduction for details about the configProps,
    * mapLocation, filterLocation, and selects parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param selects A Properties object describing the result set.
    * @param filterLocation Location of the filter document.
    * @param params A Hashtable of filter parameters. May be null. Note that
    *    this may be a Properties object, since Properties inherit from Hashtable.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    * invalid map document, class not found, etc.
    * @return A String containing the XML document
    */
   public String retrieveXMLString(Properties configProps, String mapLocation, Properties selects, String filterLocation, Hashtable params)
      throws SQLException, XMLMiddlewareException
   {
      Document doc;

      doc = retrieveDocumentInternal(configProps, mapLocation, selects, filterLocation, params);
      return utils.writeDocument(doc);
   }

   /**
    * Retrieve a result set as XML written to an OutputStream.
    *
    * <p>If the class map for the element type corresponding to the result
    * set contains related classes, this method retrieves additional data
    * from the database. See the introduction for details about the configProps,
    * mapLocation, filterLocation, and selects parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param selects A Properties object describing the result set.
    * @param filterLocation Location of the filter document.
    * @param params A Hashtable of filter parameters. May be null. Note that
    *    this may be a Properties object, since Properties inherit from Hashtable.
    * @param stream The OutputStream to which to return the XML
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void retrieveXMLOutputStream(Properties configProps, String mapLocation, Properties selects, String filterLocation, Hashtable params, OutputStream stream)
      throws SQLException, XMLMiddlewareException
   {
      Document doc;

      doc = retrieveDocumentInternal(configProps, mapLocation, selects, filterLocation, params);
      utils.writeDocument(doc, stream);
   }

   /**
    * Delete data from the database.
    *
    * <p>See the introduction for details about the configProps, mapLocation,
    * and actionLocation parameters.</p>
    *
    * @param configProps Configuration properties. May be null.
    * @param mapLocation Location of the map document.
    * @param actionLocation Location of the action document.
    * @param filterLocation Location of the filter document.
    * @param params A Hashtable of filter parameters. May be null. Note that
    *    this may be a Properties object, since Properties inherit from Hashtable.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    * @return A String containing the XML document
    */
   public void deleteXMLDocument(Properties configProps, String mapLocation, String actionLocation, String filterLocation, Hashtable params)
      throws SQLException, XMLMiddlewareException
   {
      String       validateStr;
      boolean      validate;
      XMLDBMSMap   map;
      DBEnabledMap dbMap;
      Actions      actions;
      FilterSet    filterSet;

      // Create the various objects needed by DBMSDelete.

      validateStr = " " + configProps.getProperty(XMLDBMSProps.VALIDATE) + " ";
      validate = (validateStr.indexOf(XMLDBMSProps.MAPTOKEN) != -1);
      map = createMap(configProps, mapLocation, validate);
      dbMap = createDBEnabledMap(map);
      validate = (validateStr.indexOf(XMLDBMSProps.ACTIONTOKEN) != -1);
      actions = createActions(configProps, map, actionLocation, validate);
      validate = (validateStr.indexOf(XMLDBMSProps.FILTERTOKEN) != -1);
      filterSet = createFilterSet(configProps, map, filterLocation, validate);

      // Configure the DBMSDelete object

      configDBMSDelete(configProps);

      // Delete the document

      dbmsDelete.deleteDocument(dbMap, filterSet, params, actions);
   }

   // ************************************************************************
   // Private methods -- dispatch
   // ************************************************************************

   private void dispatchStoreDocument(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String    mapLocation, actionLocation, xmlLocation;
      FilterSet filterSet;

      // Get the names of the map, action, and XML locations

      mapLocation = getProperty(props, XMLDBMSProps.MAPLOCATION);
      actionLocation = getProperty(props, XMLDBMSProps.ACTIONLOCATION);
      xmlLocation = getProperty(props, XMLDBMSProps.XMLLOCATION);

      // Store the document. If the user requested a FilterSet, write it out
      // now. Note that filterSet is null unless the ReturnFilter property was
      // set to "Yes".

      filterSet = storeXMLDocument(props, mapLocation, actionLocation, xmlLocation);
      if (filterSet != null)
      {
         writeFilterSet(props, filterSet);
      }
   }

   private void writeFilterSet(Properties props, FilterSet filterSet)
      throws XMLMiddlewareException
   {
      String           filterLocation, encoding, systemID, publicID;
      LocationResolver resolver;
      Writer           writer;
      FilterSerializer serializer;

      try
      {

         // Get the name of the filter location. Do nothing if there is no filter location.

         filterLocation = props.getProperty(XMLDBMSProps.FILTERLOCATION);
         if (filterLocation == null) return;

         // Get the encoding (if any) and the LocationResolver, then get a Writer.

         encoding = props.getProperty(XMLDBMSProps.ENCODING);
         resolver = getLocationResolver(props, XMLDBMSProps.FILTERRESOLVERCLASS);
         if (resolver.supportsWriter())
         {
            writer = resolver.getWriter(filterLocation, encoding);
         }
         else
         {
            if (encoding != null)
               throw new XMLMiddlewareException("Encodings not supported by LocationResolver: " + resolver.getClass().getName());
            writer = new OutputStreamWriter(resolver.getOutputStream(filterLocation));
         }

         // Get the system ID and public ID of the filter document DTD.

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

         // Close the writer.

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
      String     mapLocation, filterLocation, xmlLocation;
      Properties configProps = props;
      Hashtable  params      = props;

      // Get the names of the map, filter, and XML document locations

      mapLocation = getProperty(props, XMLDBMSProps.MAPLOCATION);
      filterLocation = getProperty(props, XMLDBMSProps.FILTERLOCATION);
      xmlLocation = getProperty(props, XMLDBMSProps.XMLLOCATION);

      // Process parameters used in IN clauses in filters, if any.

      processINParameters(params);

      // Retrieve the document.

      retrieveXMLDocument(configProps, mapLocation, filterLocation, params, xmlLocation);
   }

   private void dispatchRetrieveDocumentBySQL(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String     mapLocation, filterLocation, xmlLocation;
      Properties configProps = props, selects = props;
      Hashtable  params      = props;

      // Get the names of the map, filter, and XML document locations

      mapLocation = getProperty(props, XMLDBMSProps.MAPLOCATION);
      filterLocation = getProperty(props, XMLDBMSProps.FILTERLOCATION);
      xmlLocation = getProperty(props, XMLDBMSProps.XMLLOCATION);

      // Process parameters used in IN clauses in filters, if any.

      processINParameters(params);

      // Retrieve the document.

      retrieveXMLDocument(configProps, mapLocation, selects, filterLocation, params, xmlLocation);
   }

   private void dispatchDeleteDocument(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String mapLocation, actionLocation, filterLocation;
      Properties configProps = props;
      Hashtable  params      = props;

      // Get the names of the map, action, and filter document locations

      mapLocation = getProperty(props, XMLDBMSProps.MAPLOCATION);
      actionLocation = getProperty(props, XMLDBMSProps.ACTIONLOCATION);
      filterLocation = getProperty(props, XMLDBMSProps.FILTERLOCATION);

      // Process parameters used in IN clauses in filters, if any.

      processINParameters(params);

      // Delete the document

      deleteXMLDocument(configProps, mapLocation, actionLocation, filterLocation, params);
   }

   // ************************************************************************
   // Private methods -- store, retrieve, and delete documents
   // ************************************************************************

   private FilterSet storeDocumentInternal(Properties configProps, String mapLocation, String actionLocation, InputSource src)
      throws XMLMiddlewareException, SQLException
   {
      String       validateStr;
      boolean      validate;
      XMLDBMSMap   map;
      DBEnabledMap dbMap;
      Actions      actions;
      Document     doc;

      // Create the various objects needed by DOMToDBMS.storeDocument.

      validateStr = " " + configProps.getProperty(XMLDBMSProps.VALIDATE) + " ";
      validate = (validateStr.indexOf(XMLDBMSProps.MAPTOKEN) != -1);
      map = createMap(configProps, mapLocation, validate);
      dbMap = createDBEnabledMap(map);
      validate = (validateStr.indexOf(XMLDBMSProps.ACTIONTOKEN) != -1);
      actions = createActions(configProps, map, actionLocation, validate);

      // Configure the DOMToDBMS object

      configDOMToDBMS(configProps);

      // Open a DOM tree over the InputSource and store it in the database

      validate = (validateStr.indexOf(XMLDBMSProps.XMLTOKEN) != -1);
      doc = utils.readDocument(src, validate);
      return domToDBMS.storeDocument(dbMap, doc, actions);
   }

   private Document retrieveDocumentInternal(Properties configProps, String mapLocation, String filterLocation, Hashtable params)
      throws XMLMiddlewareException, SQLException
   {
      String       validateStr;
      boolean      validate;
      XMLDBMSMap   map;
      DBEnabledMap dbMap;
      FilterSet    filterSet;

      // Create the various objects needed by DBMSToDOM.retrieveDocument.

      validateStr = " " + configProps.getProperty(XMLDBMSProps.VALIDATE) + " ";
      validate = (validateStr.indexOf(XMLDBMSProps.MAPTOKEN) != -1);
      map = createMap(configProps, mapLocation, validate);
      dbMap = createDBEnabledMap(map);
      validate = (validateStr.indexOf(XMLDBMSProps.FILTERTOKEN) != -1);
      filterSet = createFilterSet(configProps, map, filterLocation, validate);

      // Configure the DBMSToDOM object

      configDBMSToDOM(configProps);

      // Retrieve and return the document

      return dbmsToDOM.retrieveDocument(dbMap, filterSet, params, null);
   }

   private Document retrieveDocumentInternal(Properties configProps, String mapLocation, Properties selects, String filterLocation, Hashtable params)
      throws XMLMiddlewareException, SQLException
   {
      String       validateStr;
      boolean      validate;
      XMLDBMSMap   map;
      Hashtable    resultSets;
      DBEnabledMap dbMap;
      FilterSet    filterSet;
      Document     doc;

      // Create the map and DB-enabled map objects.

      validateStr = " " + configProps.getProperty(XMLDBMSProps.VALIDATE) + " ";
      validate = (validateStr.indexOf(XMLDBMSProps.MAPTOKEN) != -1);
      map = createMap(configProps, mapLocation, validate);
      dbMap = createDBEnabledMap(map);

      // Create the filter set.

      validate = (validateStr.indexOf(XMLDBMSProps.FILTERTOKEN) != -1);
      filterSet = createFilterSet(configProps, map, filterLocation, validate);

      // Create the result sets.

      resultSets = createResultSets(selects);
      initTableMetadata(map, resultSets, filterSet);

      // Configure the DBMSToDOM object

      configDBMSToDOM(configProps);

      // Retrieve the document

      doc = dbmsToDOM.retrieveDocument(dbMap, resultSets, filterSet, params, null);

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

      dbName = props.getProperty(XMLDBMSProps.DBNAME, DEFAULT);

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
      keyGen = createKeyGenerator(name, className, configProps, 0);

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

            keyGen = createKeyGenerator(names[i], classes[i], configProps, i);
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

   private void initTableMetadata(XMLDBMSMap map, Hashtable resultSets, FilterSet filterSet)
      throws XMLMiddlewareException
   {
      MetadataInitializer initializer;
      Vector              filters;
      Object              o;
      ResultSetFilter     rsFilter;
      String              rsName;
      ResultSet           rs;
      Table               table;

      // Get a new MetadataInitializer.

      initializer = new MetadataInitializer(map);

      // Get the Vector of RootFilters and ResultSetFilters. Loop through the
      // filters and, for each ResultSetFilter that is found, use the result set
      // metadata to initialize the corresponding table metadata. Note that
      // ResultSetFilters associate result set names with table names.

      filters = filterSet.getFilters();
      for (int i = 0; i < filters.size(); i++)
      {
         // Get the next object and process it if it is a ResultSetFilter.

         o = filters.elementAt(i);
         if (o instanceof ResultSetFilter)
         {
            rsFilter = (ResultSetFilter)o;

            // Get the name of the result set specified in the filter and
            // get the corresponding ResultSet object.

            rsName = rsFilter.getResultSetName();
            rs = (ResultSet)resultSets.get(rsName);
            if (rs == null)
               throw new XMLMiddlewareException("Filter document specifies a result set with the name " + rsName + ". No result set with this name was specified in the properties passed to Transfer.");

            // Get the name of the table specified in the filter; this is the
            // name of the table in the map that maps the result set. Get the
            // corresponding Table object.

            table = map.getTable(rsFilter.getDatabaseName(),
                                 rsFilter.getCatalogName(),
                                 rsFilter.getSchemaName(),
                                 rsFilter.getTableName());
            if (table == null)
               throw new XMLMiddlewareException("Table specified in filter document but not found in map: " + Table.getUniversalName(rsFilter.getDatabaseName(), rsFilter.getCatalogName(), rsFilter.getSchemaName(), rsFilter.getTableName()));

            // Initialize the metadata for the columns in the table from
            // the result set metadata.

            initializer.initializeMetadata(table, rs);
         }
      }
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

   private DBEnabledMap createDBEnabledMap(XMLDBMSMap map)
   {
      DBEnabledMap dbMap;
      Enumeration  dbNames;
      String       dbName;
      DataHandler  dataHandler;

      // Check if we have already created a DBEnabledMap object for this
      // map. If so, use it. If not, create it now.

      dbMap = (DBEnabledMap)dbMaps.get(map);
      if (dbMap == null)
      {
         // Create a new DBEnabledMap object and cache it.

         dbMap = new DBEnabledMap(map);
         dbMaps.put(map, dbMap);

         // Add all current databases to the DBEnabledMap object.

         dbNames = dataHandlers.keys();
         while (dbNames.hasMoreElements())
         {
            dbName = (String)dbNames.nextElement();
            dataHandler = (DataHandler)dataHandlers.get(dbName);
            dbMap.addDataHandler(dbName, dataHandler);
         }
      }

      // Return the DBEnabledMap object.

      return dbMap;
   }

   private KeyGenerator createKeyGenerator(String name, String className, Properties configProps, int suffix)
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
         keyGen.initialize(configProps, suffix);
         keyGenerators.put(name, keyGen);
      }
      return keyGen;
   }

   private XMLDBMSMap createMap(Properties configProps, String mapLocation, boolean validate)
      throws XMLMiddlewareException
   {
      MapCompiler      compiler = null;
      LocationResolver resolver;
      XMLDBMSMap       map;

      // Check if we have already compiled the map document. If so, use the cached
      // XMLDBMSMap object. If not create a new map compiler and compile the map document.

      map = (XMLDBMSMap)locationObjects.get(mapLocation);
      if (map == null)
      {
         try
         {
            compiler = new MapCompiler(utils.getXMLReader(validate));
            resolver = getLocationResolver(configProps, XMLDBMSProps.MAPRESOLVERCLASS);
            map = compiler.compile(getInputSource(resolver, mapLocation));
         }
         catch (SAXException e)
         {
            processSAXException(e);
         }
         locationObjects.put(mapLocation, map);
      }
      return map;
   }

   private Actions createActions(Properties configProps, XMLDBMSMap map, String actionLocation, boolean validate)
      throws XMLMiddlewareException
   {
      ActionCompiler   compiler = null;
      LocationResolver resolver;
      Actions          actions;

      // Check if we have already compiled the action document. If so, use the cached
      // Actions object. If not create a new action compiler and compile the action document.

      actions = (Actions)locationObjects.get(actionLocation);
      if (actions == null)
      {
         try
         {
            compiler = new ActionCompiler(utils.getXMLReader(validate));
            resolver = getLocationResolver(configProps, XMLDBMSProps.ACTIONRESOLVERCLASS);
            actions = compiler.compile(map, getInputSource(resolver, actionLocation));
         }
         catch (SAXException e)
         {
            processSAXException(e);
         }
         locationObjects.put(actionLocation, actions);
      }
      return actions;
   }

   private FilterSet createFilterSet(Properties configProps, XMLDBMSMap map, String filterLocation, boolean validate)
      throws XMLMiddlewareException
   {
      FilterCompiler   compiler = null;
      LocationResolver resolver;
      FilterSet        filterSet;

      // Check if we have already compiled the filter document. If so, use the cached
      // FilterSet object. If not create a new filter compiler and compile the filter document.

      filterSet = (FilterSet)locationObjects.get(filterLocation);
      if (filterSet == null)
      {
         try
         {
            compiler = new FilterCompiler(utils.getXMLReader(validate));
            resolver = getLocationResolver(configProps, XMLDBMSProps.FILTERRESOLVERCLASS);
            filterSet = compiler.compile(map, getInputSource(resolver, filterLocation));
         }
         catch (SAXException e)
         {
            processSAXException(e);
         }
         locationObjects.put(filterLocation, filterSet);
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

   private void processINParameters(Hashtable params)
   {
      String inParamsString, valueString;
      Vector inParamsVector, valueVector;
      Object paramName;

      // Get the value of the INParameters property. If the property isn't found,
      // just return.

      inParamsString = (String)params.get(XMLDBMSProps.INPARAMETERS);
      if (inParamsString == null) return;

      // Parse the list of parameters used in IN clauses and construct a Vector.

      inParamsVector = parseINParameters(inParamsString);

      // Process the parameters used in IN clauses. For each such parameter, we
      // parse the list of values and construct a Vector of these values, then
      // replace the value of the property with that Vector.

      for (int i = 0; i < inParamsVector.size(); i++)
      {
         paramName = inParamsVector.elementAt(i);
         valueString = (String)params.get(paramName);
         valueVector = parseValueString(valueString);
         params.put(paramName, valueVector);
      }
   }

   private Vector parseINParameters(String inParamsString)
   {
      StringTokenizer tokenizer;
      Vector          paramNames;

      // Construct a StringTokenizer to parse the string. This separates tokens with
      // spaces, tabs, newlines, and carriage returns.

      tokenizer = new StringTokenizer(inParamsString);

      // Parse the string and build a Vector of the tokens, which are the names of
      // parameters used in IN clauses.

      paramNames = new Vector();
      while (tokenizer.hasMoreTokens())
      {
         paramNames.addElement(tokenizer.nextToken());
      }

      // Return the Vector of parameter names.

      return paramNames;
   }

   private Vector parseValueString(String valueString)
   {
      int     state = NORMAL, save = 0;
      char[]  valueChars;
      Vector  values = new Vector();
      boolean charEscaped = false;
      String  newValue, value = null;

      // Copy the string to a character array. Append a final space to simplify
      // the parsing code.

      valueChars = new char[valueString.length() + 1];
      valueString.getChars(0, valueString.length(), valueChars, 0);
      valueChars[valueChars.length - 1] = ' ';

      // Parse the string of space-separated values and build a Vector of String values.

      for (int i = 0; i < valueChars.length; i++)
      {
         switch (valueChars[i])
         {
            case ' ':
               if (state == NORMAL)
               {
                  // Spaces separate individual values. When we hit one, build a new
                  // string from the characters we have read since the last save point.

                  newValue = new String(valueChars, save, i - save);

                  // If the characters since the last "real" space contained an
                  // escaped space, then append the newly read characters to those
                  // before the slash that followed the previous escape character
                  // (backslash). Otherwise, just use the newly read characters.

                  value = (charEscaped) ? value + newValue : newValue;

                  // Save the new value.

                  values.addElement(value);

                  // Reset the escaped-character flag and set the save point to the
                  // character after the space.

                  charEscaped = false;
                  save = i + 1;
               }
               else // if (state == BACKSLASH)
               {
                  // If the character before the space was a backslash, save the
                  // characters before the backslash. We will ignore the backslash
                  // by setting the save point to the space, which is the
                  // character after the backslash.

                  newValue = new String(valueChars, save, i - save - 1);

                  // If the characters since the last "real" space contained an
                  // escaped space, then append the newly read characters to those
                  // before the slash that followed the previous escape character
                  // (backslash). Otherwise, just use the newly read characters.

                  value = (charEscaped) ? value + newValue : newValue;

                  // Set the escaped-character flag, set the save point to the
                  // space (which we want to include in the next set of characters),
                  // and reset the state to NORMAL (no backslash).

                  charEscaped = true;
                  save = i;
                  state = NORMAL;
               }
               break;

            case '\\':
               // If we find a slash, then we need a different state to check if it
               // is being used to escape a space.

               state = BACKSLASH;
               break;

            default:
               // If the current character is not a space and not a backslash, then
               // any backslash we might have found was simply a literal and we can
               // treat it as a normal character.

               state = NORMAL;
               break;
         }
      }

      // Return the Vector of parameter values.

      return values;
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

   private LocationResolver getLocationResolver(Properties props, String propName)
      throws XMLMiddlewareException
   {
      String           resolverClass;
      LocationResolver resolver;

      // Get the name of the LocationResolver class. If the name isn't passed in,
      // use org.xmlmiddleware.xmldbms.tools.resolvers.FilenameResolver.

      if (props == null)
      {
         resolverClass = FILENAMERESOLVER;
      }
      else
      {
         resolverClass = props.getProperty(propName, FILENAMERESOLVER);
      }

      // Get the LocationResolver from the hashtable. If it doesn't exist,
      // instantiate it.

      resolver = (LocationResolver)resolvers.get(resolverClass);
      if (resolver == null)
      {
         resolver = (LocationResolver)instantiateObject(resolverClass);
         resolvers.put(resolverClass, resolver);
      }
      return resolver;
   }

   private InputSource getInputSource(LocationResolver resolver, String location)
      throws XMLMiddlewareException
   {
      if (resolver.supportsReader())
      {
         return new InputSource(resolver.getReader(location));
      }
      else
      {
         return new InputSource(resolver.getInputStream(location));
      }
   }

   private void writeDocument(LocationResolver resolver, Document doc, String location, String encoding)
      throws XMLMiddlewareException
   {
      try
      {
         if (resolver.supportsWriter())
         {
            Writer writer;

            writer = resolver.getWriter(location, encoding);
            utils.writeDocument(doc, writer);
            writer.close();
         }
         else
         {
            OutputStream stream;

            if (encoding != null)
               throw new XMLMiddlewareException("Encodings not supported by LocationResolver: " + resolver.getClass().getName());
            stream = resolver.getOutputStream(location);
            utils.writeDocument(doc, stream);
            stream.close();
         }
      }
      catch (IOException e)
      {
         throw new XMLMiddlewareException(e);
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
