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
// Changes from version 1.1: Updated/renamed for version 2.0

package org.xmlmiddleware.xmldbms.tools;

import org.xmlmiddleware.db.*;
import org.xmlmiddleware.schemas.dtds.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.factories.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;
import org.xmlmiddleware.xmlutils.*;

import org.xml.sax.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Simplified interface to the map factories and utilities.
 *
 * <p><b>Introduction</b></p>
 *
 * <p>MapManager provides three interfaces for using map factories and utilities:
 * a command line interface, a dispatch-style API, and a high-level, traditional API.
 * The command line interface consists of a single method (main). The dispatch-style
 * API also consists of a single method (dispatch).  All other methods (writeMap,
 * createDTD, etc.) belong to the traditional API.</p>
 *
 * <p>The command line interface and dispatch-style API take a set of property-value
 * pairs that describe the action to be taken. The most important of these properties
 * are Input (how to create a map) and either Output or Output1, Output2, ... (what to
 * create from the map).</p>
 *
 * <p>The traditional API either creates an XMLDBMSMap object from the specified input
 * (map file, DTD, database) or creates something from an XMLDBMSMap object (map file,
 * DTD, set of CREATE TABLE statements).</p>
 *
 * <p><b>Properties</b></p>
 *
 * <p>All of MapManager's interfaces use properties. The dispatch-style
 * API and command-line interface are entirely properties based, while
 * the traditional API uses properties for database information (data source class,
 * URL, etc.) and configuration information (output encoding, whether to map columns
 * as element types or attributes, etc.).</p>
 *
 * <p>Properties fall into the following categories:</p>
 *
 * <ul>
 * <li><p>Property-processing properties are used to process other properties.
 *    This is File.</p></li>
 *
 * <li><p>Parser properties provide information about the XML parser / DOM
 *    implementation. These are ParserUtilsClass and Validate.</p></li>
 *
 * <li><p>Input and output properties specify what is to be done (create a map
 *    from a DTD, write a map file, etc.). These are Input, Output, MapFile,
 *    DTDFile, SQLFile, RootDatabaseName(n), RootCatalogName(n), RootSchemaName(n),
 *    RootTableName(n), StopDatabaseName(n), StopCatalogName(n), StopSchemaName(n),
 *    and StopTableName(n). See below for details.</p></li>
 *
 * <li><p>Database properties are used to connect to the database(s). They are
 *    DBName, DataSourceClass, User, and Password. If there
 *    is more than one database, use sequentially numbered versions of these
 *    properties -- DBName1, DBName2, etc.</p>
 *
 *    <p>When the value of DataSourceClass is "JDBC1DataSource", the Driver and
 *    URL properties are used to configure the data source. When the value
 *    of DataSourceClass is "JDBC2DataSource", the JNDIContext and
 *    JNDILookupName properties are used to configure the data source. If
 *    the main database properties are numbered, these properties must also
 *    be numbered.</p>
 *
 *    <p>Note that the DBName property is optional. If DBName is omitted, "Default"
 *    is used. This is an error if there is more than one database.</p></li>
 *
 * <li><p>Configuration properties specify how the classes are to function.
 *    They are OrderType, DatabaseName, CatalogName, SchemaName,
 *    Prefix(n), URI(n), MapColumnsAs, FollowPrimaryKeys,
 *    FollowForeignKeys, Encoding, SystemID, PublicID, Pretty, Indent, and SQLSeparator.
 *    See below for details.</p></li>
 * </ul>
 *
 * <p>When using the command line or the dispatch-style API, the Input and Output properties
 * specify the actions to take. Legal values for the Input property are Map, DTD, and
 * Database. Legal values for the Output property are Map, DTD, and SQL. (When using the
 * traditional API, the Input and Output properties are not needed since this information
 * is inherent in the method called.)</p>
 *
 * <p>The following table shows which properties are used with each value of the Input
 * property. Required properties are marked with an asterisk (*). Properties
 * that may have more than one value are labeled (n). For example, DBName(n) means that
 * there may be a DBName property or DBName1, DBName2, DBName3, ... properties.</p>
 *
 * <table border="1" cellpadding="3">
 * <tr><th>Input value</th><th>Input properties</th><th>Database properties</th>
 * <th>Configuration properties</th></tr>
 * <tr valign="top">
 * <td>Map</td>
 * <td>MapFile*</td>
 * <td>Not used.</td>
 * <td>Validate [1]</td>
 * </tr>
 *
 * <tr valign="top">
 * <td>DTD</td>
 * <td>DTDFile*</td>
 * <td>Single set of values. Optional. [2]</td>
 * <td>DatabaseName [3]<br />CatalogName [3]<br />SchemaName [3]<br />
 *     Prefix(n) [4]<br />URI(n) [4]</td>
 * </tr>
 *
 * <tr valign="top">
 * <td>Database [5]</td>
 * <td>RootDatabase(n)<br />RootCatalog(n)<br />RootSchema(n)<br />RootTable(n)*<br />
 *     StopDatabase(n)<br />StopCatalog(n)<br />StopSchema(n)<br />StopTable(n)</td>
 * <td>Set of values for each database that is read. Required.</td>
 * <td>MapColumnsAs [6]<br />FollowPrimaryKeys [1]<br />FollowForeignKeys [1]<br />
 *     Prefix [7]<br />URI [7]</td>
 * </tr>
 * </table>
 *
 * <p>NOTES:<br />
 * [1] Legal values are Yes (default) and No.<br />
 * [2] Used to check that generated names are legal and do not conflict with existing
 *     names.<br />
 * [3] Database structure to which element types and attributes are mapped.<br />
 * [4] Maps namespace prefixes in DTD to namespace URIs.<br />
 * [5] See MapFactory_Database for an explanation of properties.<br />
 * [6] Legal values are ElementTypes (default) and Attributes.<br />
 * [7] Namespace prefix and URI of generated element type names.
 * </p>
 *
 * <p>The following table shows which properties are used with each value of the
 * Output property. Required properties are marked with an asterisk (*). Properties
 * that may have more than one value are labeled (n). For example, DBName(n) means that
 * there may be a DBName property or DBName1, DBName2, DBName3, ... properties.</p>
 *
 * <table border="1" cellpadding="3">
 * <tr><th>Output value</th><th>Input properties</th><th>Database properties</th>
 * <th>Configuration properties</th></tr>
 *
 * <tr valign="top">
 * <td>Map</td>
 * <td>MapFile*</td>
 * <td>Not used</td>
 * <td>Encoding<br />URI(n) [1]<br />Prefix(n) [1]<br />SystemID<br />PublicID<br />
 *     Pretty [2]<br />Indent [3]</td>
 * </tr>
 *
 * <tr valign="top">
 * <td>DTD</td>
 * <td>DTDFile*</td>
 * <td>Not used</td>
 * <td>Encoding<br />Pretty [2]</td>
 * </tr>
 *
 * <tr valign="top">
 * <td>SQL</td>
 * <td>SQLFile*</td>
 * <td>Set of values for each database used by the map. Optional. [4]</td>
 * <td>Encoding<br />SQLSeparator [5]</td>
 * </tr>
 * </table>
 *
 * <p>NOTES:<br />
 * [1] Overrides the namespace prefixes currently in the map (if any). Rarely used.<br />
 * [2] Whether to pretty-print. Legal values are Yes (default) and No.<br />
 * [3] Number of spaces to indent when pretty-printing. Default is 3.<br />
 * [4] Used to retrieve database-specific type names.<br />
 * [5] Character(s) used to separate the CREATE TABLE statements. Default is semi-colon
 *     (;) plus the line separator system property.
 * </p>
 *
 * <p>For a complete description of the properties used by Transfer,
 * see ?????.</p>
 *
 * <p><b>Command Line Syntax</b></p>
 *
 * <p>The syntax of the command-line interface is:</p>
 *
 * <pre>
 *   java MapManager <property>=<value> [<property>=<value>...]
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
 *   java org.xmlmiddleware.xmldbms.tools.MapManager
 *              DataSourceClass=JDBC1DataSource
 *              Driver=sun.jdbc.odbc.JdbcOdbcDriver URL=jdbc:odbc:xmldbms
 *              User=ron Password=ronpwd
 *              Input=DTD DTDFile=sales.dtd
 *              Output1=Map MapFile=sales.map
 *              Output2=SQL SQLFile=sales.sql
 * </pre>
 *
 * <p>A special property, File, can be used to designate a file containing
 * other properties. For example, if the database properties are stored in db.props,
 * the following is equivalent to the previous command line:</p>
 *
 * <pre>
 *   java org.xmlmiddleware.xmldbms.tools.MapManager File=db.props
 *              Input=DTD DTDFile=sales.dtd
 *              Output1=Map MapFile=sales.map
 *              Output2=SQL SQLFile=sales.sql
 * </pre>
 *
 * <p>If more than one File property is used, the File properties
 * must be numbered sequentially. File properties can also be used inside
 * property files, making it possible to have a hierarchy of property files.
 * File properties can be used from the command line and with the
 * dispatch-style interface. They cannot be used with the traditional API.</p>
 *
 * <p><b>Dispatch-style Interface</b></p>
 *
 * <p>The dispatch-style interface is called that because methods are not called directly.
 * Instead, the name of the method and its arguments are passed as properties and MapManager
 * "dispatches" the call to the actual method based on these properties.</p>
 *
 * <p>The dispatch-style interface consists of a single method, dispatch(). In general,
 * applications that want to call MapManager programmatically should probably use the
 * traditional API instead.</p>
 *
 * <p><b>Traditional API</b></p>
 *
 * <p>The traditional API consists of a number of methods: compileMap, createMapFromDTD,
 * createMapFromDatabase, writeMap, createDTD, and createSQL. Some of these methods
 * accept database properties; all of them accept configuration properties. The database
 * and configuration properties are described above.</p>
 *
 * @author Ronald Bourret
 * @author Adam Flinton
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.tools.XMLDBMSProps
 */
public class MapManager extends PropertyProcessor
{
   // ************************************************************************
   // Class variables
   // ************************************************************************

   private ParserUtils utils;
   private static final Properties emptyProps = new Properties();

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String DEFAULT = "Default";
   private static String JDBC1DATASOURCE = "org.xmlmiddleware.db.JDBC1DataSource";
   private static String JDBC2DATASOURCE = "org.xmlmiddleware.db.JDBC2DataSource";

   // ************************************************************************
   // Constructor
   // ************************************************************************

   private MapManager()
   {
      // We need this constructor when MapManager is called from the command
      // line. This is because we don't yet know the name of the ParserUtils
      // class. This constructor is private because constructing MapManager
      // without a ParserUtils class is a dangerous thing to do.

      super();
   }

   /**
    * Construct a MapManager object.
    *
    * @param props A Properties object containing the ParserUtilsClass property.
    * @exception XMLMiddlewareException An error occurs instantiating the ParserUtils class.
    */
   public MapManager(Properties props)
      throws XMLMiddlewareException
   {
      super();
      if (props == null) props = emptyProps;
      setParserUtils(props);
   }

   /**
    * Construct a MapManager object.
    *
    * @param utils An object that implements the ParserUtils interface.
    */
   public MapManager(ParserUtils utils)
   {
      super();
      if (utils == null)
         throw new IllegalArgumentException("utils argument must not be null.");
      this.utils = utils;
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Run MapManager from a command line.
    *
    * <p>See the introduction for the command line syntax.</p>
    *
    * @param args An array of property/value pairs.
    */
   public static void main(String[] args)
   {
      MapManager manager = new MapManager();
      Properties props = new Properties();

      if (args.length < 1)
      {
         System.out.println("Usage: java MapManager <property>=<value> [<property>=<value>...]\n\n" + "See the documentation for a list of valid properties.");
         return;
      }

      try
      {
         // Add the properties, set the ParserUtils class, and execute the
         // specified method.

         manager.addPropertiesFromArray(props, args, 0, true);
         manager.setParserUtils(props);
         manager.dispatch(props);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Creates a map according the Input property and generates output according
    * to one or more Output properties.
    *
    * <p>For a list of valid inputs and outputs and the properties that each input
    * or output type needs, see the introduction.</p>
    *
    * @param props A Properties object describing the input and output(s)
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors: file not found,
    *    invalid map document, class not found, etc.
    */
   public void dispatch(Properties props)
      throws XMLMiddlewareException, SQLException
   {
      String     input, temp, filename;
      String[]   outputs,
                 rootDatabaseNames, rootCatalogNames, rootSchemaNames, rootTableNames,
                 stopDatabaseNames, stopCatalogNames, stopSchemaNames, stopTableNames;
      XMLDBMSMap map;

      if (props == null) props = emptyProps;

      // Get the input property.

      input = props.getProperty(XMLDBMSProps.INPUT);
      if (input == null)
         throw new XMLMiddlewareException("Input property not specified.");

      // Get the output property or properties.

      temp = props.getProperty(XMLDBMSProps.OUTPUT);
      if (temp != null)
      {
         outputs = new String[1];
         outputs[0] = temp;
      }
      else
      {
         outputs = getNumberedProps(XMLDBMSProps.OUTPUT, props);
         if (outputs == null)
            throw new XMLMiddlewareException("Output property or Output1, Output2, ... properties not specified.");
      }

      // Create a map from the input source.

      if (input.equals(XMLDBMSProps.DTD))
      {
         filename = props.getProperty(XMLDBMSProps.DTDFILE);
         if (filename == null)
            throw new XMLMiddlewareException("You must specify the DTDFile property when the value of the Input property is DTD.");
         map = createMapFromDTD(props, props, filename);
      }
      else if (input.equals(XMLDBMSProps.DATABASE))
      {
         // Check if the user specified RootTable or RootTable1, RootTable2, etc.
         // and get the root table information accordingly.

         if (props.getProperty(XMLDBMSProps.ROOTTABLE) != null)
         {
            rootDatabaseNames = getUnnumberedProps(XMLDBMSProps.ROOTDATABASE, props);
            rootCatalogNames = getUnnumberedProps(XMLDBMSProps.ROOTCATALOG, props);
            rootSchemaNames = getUnnumberedProps(XMLDBMSProps.ROOTSCHEMA, props);
            rootTableNames = getUnnumberedProps(XMLDBMSProps.ROOTTABLE, props);
         }
         else
         {
            rootDatabaseNames = getNumberedProps(XMLDBMSProps.ROOTDATABASE, props);
            rootCatalogNames = getNumberedProps(XMLDBMSProps.ROOTCATALOG, props);
            rootSchemaNames = getNumberedProps(XMLDBMSProps.ROOTSCHEMA, props);
            rootTableNames = getNumberedProps(XMLDBMSProps.ROOTTABLE, props);
         }

         // Check that the user specified some root table information.

         if (rootTableNames == null)
            throw new IllegalArgumentException("You must specify a " + XMLDBMSProps.ROOTTABLE + "n property or properties, where n is optional but must be numbered 1, 2, 3, ... if present.");

         // Check if the user specified StopTable or StopTable1, StopTable2, etc.
         // and get the stop table information accordingly.

         if (props.getProperty(XMLDBMSProps.STOPTABLE) != null)
         {
            stopDatabaseNames = getUnnumberedProps(XMLDBMSProps.STOPDATABASE, props);
            stopCatalogNames = getUnnumberedProps(XMLDBMSProps.STOPCATALOG, props);
            stopSchemaNames = getUnnumberedProps(XMLDBMSProps.STOPSCHEMA, props);
            stopTableNames = getUnnumberedProps(XMLDBMSProps.STOPTABLE, props);
         }
         else
         {
            stopDatabaseNames = getNumberedProps(XMLDBMSProps.STOPDATABASE, props);
            stopCatalogNames = getNumberedProps(XMLDBMSProps.STOPCATALOG, props);
            stopSchemaNames = getNumberedProps(XMLDBMSProps.STOPSCHEMA, props);
            stopTableNames = getNumberedProps(XMLDBMSProps.STOPTABLE, props);
         }

         // Call createMapFromDatabase.

         map = createMapFromDatabase(props, props, rootDatabaseNames, rootCatalogNames, rootSchemaNames, rootTableNames, stopDatabaseNames, stopCatalogNames, stopSchemaNames, stopTableNames);

      }
      else if (input.equals(XMLDBMSProps.MAP))
      {
         filename = props.getProperty(XMLDBMSProps.MAPFILE);
         if (filename == null)
            throw new XMLMiddlewareException("You must specify the MapFile property when the value of the Input property is Map.");
         map = compileMap(props, filename);
      }
      else
         throw new XMLMiddlewareException("Invalid value of " + XMLDBMSProps.INPUT + " property: " + input);

      // Create the requested outputs

      for (int i = 0; i < outputs.length; i++)
      {
         if (outputs[i].equals(XMLDBMSProps.MAP))
         {
            filename = props.getProperty(XMLDBMSProps.MAPFILE);
            if (filename == null)
               throw new XMLMiddlewareException("You must specify the MapFile property when the value of the Output(n) property is Map.");
            writeMap(props, map, filename);
         }
         else if (outputs[i].equals(XMLDBMSProps.DTD))
         {
            filename = props.getProperty(XMLDBMSProps.DTDFILE);
            if (filename == null)
               throw new XMLMiddlewareException("You must specify the DTDFile property when the value of the Output(n) property is DTD.");
            createDTD(props, map, filename);
         }
         else if (outputs[i].equals(XMLDBMSProps.SQL))
         {
            filename = props.getProperty(XMLDBMSProps.SQLFILE);
            if (filename == null)
               throw new XMLMiddlewareException("You must specify the SQLFile property when the value of the Output(n) property is SQL.");
            createSQL(props, props, map, filename);
         }
         else
            throw new XMLMiddlewareException("Invalid value of " + XMLDBMSProps.OUTPUT + " property: " + outputs[i]);
      }
   }

   /**
    * Compiles a map file.
    *
    * @param configProps See the introduction. May be null.
    * @param mapFilename Name of the map file.
    * @exception XMLMiddlewareException Thrown for all errors
    */
   public XMLDBMSMap compileMap(Properties configProps, String mapFilename)
      throws XMLMiddlewareException
   {
      MapCompiler compiler = null;
      String      validateString;
      boolean     validate;

      if (configProps == null) configProps = emptyProps;

      // Compile the map.

      try
      {
         validateString = configProps.getProperty(XMLDBMSProps.VALIDATE);
         validate = (validateString == null) ? true : getYesNo(validateString);
         compiler = new MapCompiler(utils.getXMLReader(validate));
         return compiler.compile(new InputSource(new FileReader(mapFilename)));
      }
      catch (SAXException s)
      {
         processSAXException(s);
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }

      // Make the compiler happy...

      return null;
   }

   /**
    * Creates a map from a DTD.
    *
    * @param dbProps A Properties object describing the database to which to map
    *    the DTD. May be null.
    * @param configProps See the introduction. May be null.
    * @param dtdFilename Name of the DTD file.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors
    */
   public XMLDBMSMap createMapFromDTD(Properties dbProps, Properties configProps, String dtdFilename)
      throws XMLMiddlewareException, SQLException
   {
      DBInfo         dbInfo;
      Connection     conn;
      MapFactory_DTD factory;
      String         order, databaseName, catalogName, schemaName;
      Hashtable      namespaceURIs;

      if (dbProps == null) dbProps = emptyProps;
      if (configProps == null) configProps = emptyProps;

      // Create a new map factory.

      factory = new MapFactory_DTD();

      // Set the database properties, if any.

      dbInfo = getDBInfo(dbProps, false);
      if (dbInfo != null)
      {
         conn = dbInfo.dataSource.getConnection(dbInfo.user, dbInfo.password);
         factory.setConnection(conn);
      }

      // Set the order options, if any.

      order = configProps.getProperty(XMLDBMSProps.ORDERTYPE);
      if (order != null)
      {
         if (order.equals(XMLDBMSProps.FIXED))
         {
            factory.setOrderType(MapFactory_DTD.ORDER_FIXED);
         }
         else if (order.equals(XMLDBMSProps.NONE))
         {
            factory.setOrderType(MapFactory_DTD.ORDER_NONE);
         }
         else if (order.equals(XMLDBMSProps.COLUMNS))
         {
            factory.setOrderType(MapFactory_DTD.ORDER_COLUMNS);
         }
         else
            throw new IllegalArgumentException("Invalid value of " + XMLDBMSProps.ORDERTYPE + " property: " + order);
      }

      // Set the database names, if any.

      databaseName = dbProps.getProperty(XMLDBMSProps.DATABASENAME);
      catalogName = dbProps.getProperty(XMLDBMSProps.CATALOGNAME);
      schemaName = dbProps.getProperty(XMLDBMSProps.SCHEMANAME);
      if ((databaseName != null) || (catalogName != null) || (schemaName != null))
      {
         factory.setDatabaseNames(databaseName, catalogName, schemaName);
      }

      // Get the namespace URIs and prefixes, if any.

      namespaceURIs = getNamespaceURIOrURIs(configProps);

      // Create and return the map;

      try
      {
         return factory.createMap(new InputSource(new FileReader(dtdFilename)), MapFactory_DTD.DTD_EXTERNAL, namespaceURIs);
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Creates a map from a database.
    *
    * @param dbProps A Properties object describing the database to which to map
    *    the DTD. Required.
    * @param configProps See the introduction. May be null.
    * @param rootDatabaseNames See MapFactory_Database. May be null.
    * @param rootCatalogNames See MapFactory_Database. May be null.
    * @param rootSchemaNames See MapFactory_Database. May be null.
    * @param rootTableNames See MapFactory_Database. Required.
    * @param stopDatabaseNames See MapFactory_Database. May be null.
    * @param stopCatalogNames See MapFactory_Database. May be null.
    * @param stopSchemaNames See MapFactory_Database. May be null.
    * @param stopTableNames See MapFactory_Database. May be null.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors
    */
   public XMLDBMSMap createMapFromDatabase(Properties dbProps, Properties configProps, String[] rootDatabaseNames, String[] rootCatalogNames, String[] rootSchemaNames, String[] rootTableNames, String[] stopDatabaseNames, String[] stopCatalogNames, String[] stopSchemaNames, String[] stopTableNames)
      throws XMLMiddlewareException, SQLException
   {
      MapFactory_Database factory;
      Hashtable           dbInfos;
      Enumeration         dbNames;
      String[]            nameArray;
      Connection[]        connArray;
      String              dbName, mapColumnsAs, followPrimaryKeys, followForeignKeys, uri;
      DBInfo              dbInfo;
      Connection          conn;
      int                 i = 0;

      if (dbProps == null) dbProps = emptyProps;
      if (configProps == null) configProps = emptyProps;

      // Get the database property values.

      dbInfos = getDBInfoOrDBInfos(dbProps);

      // Build the arrays of names and connections.

      dbNames = dbInfos.keys();
      nameArray = new String[dbInfos.size()];
      connArray = new Connection[dbInfos.size()];
      while (dbNames.hasMoreElements())
      {
         dbName = (String)dbNames.nextElement();
         dbInfo = (DBInfo)dbInfos.get(dbName);
         conn = dbInfo.dataSource.getConnection(dbInfo.user, dbInfo.password);
         nameArray[i] = dbName;
         connArray[i] = conn;
         i++;
      }

      // Create a new map factory.

      factory = new MapFactory_Database(nameArray, connArray);

      // Set whether columns are mapped to element types or attributes.

      mapColumnsAs = configProps.getProperty(XMLDBMSProps.MAPCOLUMNSAS);
      if (mapColumnsAs != null)
      {
         if (mapColumnsAs.equals(XMLDBMSProps.ELEMENTTYPES))
         {
            factory.columnsAreElementTypes(true);
         }
         else if (mapColumnsAs.equals(XMLDBMSProps.ATTRIBUTES))
         {
            factory.columnsAreElementTypes(false);
         }
         else
            throw new XMLMiddlewareException("Invalid value of " + XMLDBMSProps.MAPCOLUMNSAS + " property: " + mapColumnsAs);
      }

      // Set whether to follow primary and/or foreign keys.

      followPrimaryKeys = configProps.getProperty(XMLDBMSProps.FOLLOWPRIMARYKEYS);
      if (followPrimaryKeys != null)
      {
         factory.followPrimaryKeys(getYesNo(followPrimaryKeys));
      }

      followForeignKeys = configProps.getProperty(XMLDBMSProps.FOLLOWFOREIGNKEYS);
      if (followForeignKeys != null)
      {
         factory.followForeignKeys(getYesNo(followForeignKeys));
      }

      // Set the namespace URI and prefix.

      uri = configProps.getProperty(XMLDBMSProps.URI);
      if (uri != null)
      {
         factory.setNamespaceInfo(uri, configProps.getProperty(XMLDBMSProps.PREFIX));
      }

      // Make sure we have non-null arrays.

      if (rootDatabaseNames == null)
      {
         rootDatabaseNames = new String[rootTableNames.length];
      }
      if (rootCatalogNames == null)
      {
         rootCatalogNames = new String[rootTableNames.length];
      }
      if (rootSchemaNames == null)
      {
         rootSchemaNames = new String[rootTableNames.length];
      }

      if (stopTableNames != null)
      {
         if (stopDatabaseNames == null)
         {
            stopDatabaseNames = new String[stopTableNames.length];
         }
         if (stopCatalogNames == null)
         {
            stopCatalogNames = new String[stopTableNames.length];
         }
         if (stopSchemaNames == null)
         {
            stopSchemaNames = new String[stopTableNames.length];
         }
      }

      // Create the map. The form of createMap that we call depends on whether there
      // are stop tables or not.

      if (stopTableNames == null)
      {
         return factory.createMap(rootDatabaseNames, rootCatalogNames, rootSchemaNames, rootTableNames);
      }
      else
      {
         return factory.createMap(rootDatabaseNames, rootCatalogNames, rootSchemaNames, rootTableNames, stopDatabaseNames, stopCatalogNames, stopSchemaNames, stopTableNames);
      }
   }

   /**
    * Writes a map to a file.
    *
    * @param configProps See the introduction. May be null.
    * @param map The map
    * @param mapFilename The name of the file to write the map to.
    * @exception XMLMiddlewareException Thrown for all other errors
    */
   public void writeMap(Properties configProps, XMLDBMSMap map, String mapFilename)
      throws XMLMiddlewareException
   {
      String        systemID, prettyString, indentString;
      Writer        writer;
      MapSerializer serializer;
      boolean       pretty;
      Hashtable     uris;
      Enumeration   prefixes;
      String[]      uriArray, prefixArray;
      int           indent, i;

      if (configProps == null) configProps = emptyProps;

      // Get the Writer and create the MapSerializer.

      writer = getWriter(mapFilename, configProps);
      serializer = new MapSerializer(writer);

      // Set the pretty printing option.

      prettyString = configProps.getProperty(XMLDBMSProps.PRETTY);
      pretty = (prettyString == null) ? true : getYesNo(prettyString);
      try
      {
         indentString = configProps.getProperty(XMLDBMSProps.INDENT);
         indent = (indentString == null) ? 3 : Integer.valueOf(indentString).intValue();
      }
      catch (NumberFormatException e)
      {
         throw new XMLMiddlewareException(e);
      }
      serializer.setPrettyPrinting(pretty, indent);

      // Build the arrays of namespace URIs and prefixes.

      uris = getNamespaceURIOrURIs(configProps);
      if (uris != null)
      {
         prefixes = uris.keys();
         prefixArray = new String[uris.size()];
         uriArray = new String[uris.size()];
         i = 0;
         while (prefixes.hasMoreElements())
         {
            prefixArray[i] = (String)prefixes.nextElement();
            uriArray[i] = (String)uris.get(prefixArray[i]);
            i++;
         }
         serializer.usePrefixes(prefixArray, uriArray);
      }

      // Serialize the map. Which method we use depends on whether the user
      // specified a system ID or not.

      try
      {
         systemID = configProps.getProperty(XMLDBMSProps.SYSTEMID);
         if (systemID != null)
         {
            serializer.serialize(map, systemID, configProps.getProperty(XMLDBMSProps.PUBLICID));
         }
         else
         {
            serializer.serialize(map);
         }

         // Close the writer.

         writer.close();
      }
      catch (IOException io)
      {
         throw new XMLMiddlewareException(io);
      }
   }

   /**
    * Creates a DTD from a map and writes it to a file.
    *
    * @param configProps See the introduction. May be null.
    * @param map The map
    * @param dtdFilename The name of the file to write the DTD to.
    * @exception XMLMiddlewareException Thrown for all other errors
    */
   public void createDTD(Properties configProps, XMLDBMSMap map, String dtdFilename)
      throws XMLMiddlewareException
   {
      DTDGenerator  generator;
      DTD           dtd;
      DTDSerializer serializer;
      Writer        writer;
      String        prettyString;
      boolean       pretty;

      if (configProps == null) configProps = emptyProps;

      // Create a new DTDGenerator and generate the DTD.

      generator = new DTDGenerator();
      dtd = generator.getDTD(map);

      // Get the Writer and whether to pretty print;

      writer = getWriter(dtdFilename, configProps);
      prettyString = configProps.getProperty(XMLDBMSProps.PRETTY);
      pretty = (prettyString == null) ? true : getYesNo(prettyString);

      // Create a new DTDSerializer and serialize the DTD.

      try
      {
         serializer = new DTDSerializer();
         serializer.serialize(dtd, writer, pretty);

         // Close the Writer.

         writer.close();
      }
      catch (IOException io)
      {
         throw new XMLMiddlewareException(io);
      }

   }

   /**
    * Creates CREATE TABLE statements from a map and writes them to a file.
    *
    * @param dbProps Properties for database to get type names, etc. from. May be null.
    * @param configProps See the introduction. May be null.
    * @param map The map
    * @param sqlFilename The name of the file to write the CREATE TABLE statements to.
    * @exception SQLException Thrown if a database error occurs.
    * @exception XMLMiddlewareException Thrown for all other errors
    */
   public void createSQL(Properties dbProps, Properties configProps, XMLDBMSMap map, String sqlFilename)
      throws XMLMiddlewareException, SQLException
   {
      DBInfo             dbInfo;
      Connection         conn;
      DDLGenerator       generator;
      Hashtable          dbInfos;
      Enumeration        dbNames;
      String[]           nameArray;
      DatabaseMetaData[] metaArray;
      String             dbName, separator;
      int                numDBNames;
      Vector             strings;
      Writer             writer;

      if (dbProps == null) dbProps = emptyProps;
      if (configProps == null) configProps = emptyProps;

      // Create the DDLGenerator. There are three cases here (listed in the order of
      // the if clauses): the user specifies a single connection, the user specifies
      // multiple connections, and the user specifies no connection. We test for which
      // case is present by testing for the DataSourceClass property.

      if (dbProps.getProperty(XMLDBMSProps.DATASOURCECLASS) != null)
      {
         dbInfo = getDBInfo(dbProps, true);
         conn = dbInfo.dataSource.getConnection(dbInfo.user, dbInfo.password);
         dbName = dbProps.getProperty(XMLDBMSProps.DBNAME);
         generator = new DDLGenerator(dbName, conn.getMetaData());
      }
      else if (dbProps.getProperty(XMLDBMSProps.DATASOURCECLASS + "1") != null)
      {
         // Get the database property values.

         dbInfos = getDBInfoOrDBInfos(dbProps);

         // Build the arrays of names and database metadata objects.

         dbNames = dbInfos.keys();
         nameArray = new String[dbInfos.size()];
         metaArray = new DatabaseMetaData[dbInfos.size()];
         numDBNames = 0;
         while (dbNames.hasMoreElements())
         {
            dbName = (String)dbNames.nextElement();
            dbInfo = (DBInfo)dbInfos.get(dbName);
            conn = dbInfo.dataSource.getConnection(dbInfo.user, dbInfo.password);
            nameArray[numDBNames] = dbName;
            metaArray[numDBNames] = conn.getMetaData();
            numDBNames++;
         }

         // Construct a new DDLGenerator.

         generator = new DDLGenerator(nameArray, metaArray);
      }
      else
      {
         generator = new DDLGenerator();
      }

      // Get the CREATE TABLE strings.

      strings = generator.getCreateTableStrings(map);

      // Get the writer.

      writer = getWriter(sqlFilename, configProps);

      // Get the statement separator, if any.

      separator = configProps.getProperty(XMLDBMSProps.SQLSEPARATOR);
      if (separator == null)
      {
         separator = ";" + System.getProperty("line.separator");
      }

      // Write out the CREATE TABLE strings.

      try
      {
         for (int i = 0; i < strings.size(); i++)
         {
            writer.write((String)strings.elementAt(i));
            writer.write(separator);
         }

         // Close the Writer.

         writer.close();
      }
      catch (IOException io)
      {
         throw new XMLMiddlewareException(io);
      }
   }

   // ************************************************************************
   // Private methods -- database stuff
   // ************************************************************************

   private Hashtable getDBInfoOrDBInfos(Properties props)
      throws XMLMiddlewareException
   {
      Hashtable dbInfos;
      DBInfo    dbInfo;
      String    dbName;

      // The user can specify a single data source (with the DataSourceClass
      // property) or multiple data sources (with the DataSourceClass1, 2, ...
      // property). Check which one is being used.

      if (props.getProperty(XMLDBMSProps.DATASOURCECLASS) != null)
      {
         dbInfos = new Hashtable();
         dbInfo = getDBInfo(props, true);
         dbName = props.getProperty(XMLDBMSProps.DBNAME);
         if (dbName == null) dbName = DEFAULT;
         dbInfos.put(dbName, dbInfo);
      }
      else
      {
         dbInfos = getDBInfos(props);
      }

      return dbInfos;
   }

   private DBInfo getDBInfo(Properties props, boolean required)
      throws XMLMiddlewareException
   {
      String dataSourceClass;
      DBInfo dbInfo;

      // Get the DataSourceClass property. If we are required to have database
      // properties, thrown an exception if it is missing. Otherwise, return null.

      dataSourceClass = props.getProperty(XMLDBMSProps.DATASOURCECLASS);
      if (dataSourceClass == null)
      {
         if (required)
            throw new XMLMiddlewareException("You must specify a value for the " + XMLDBMSProps.DATASOURCECLASS + " property.");
         else
            return null;
      }

      // Create a new DBInfo object and fill it in.

      dbInfo = new DBInfo();
      dbInfo.dataSource = createDataSource(dataSourceClass, props, 0);
      dbInfo.user = props.getProperty(XMLDBMSProps.USER);
      dbInfo.password = props.getProperty(XMLDBMSProps.PASSWORD);

      // Return the DBInfo object.

      return dbInfo;
   }

   private Hashtable getDBInfos(Properties props)
      throws XMLMiddlewareException
   {
      String[]  dbNames, dataSourceClasses, users, passwords;
      Hashtable dbInfos = new Hashtable();
      DBInfo    dbInfo;

      // Get the various property values.

      dbNames = getNumberedProps(XMLDBMSProps.DBNAME, props);
      dataSourceClasses = getNumberedProps(XMLDBMSProps.DATASOURCECLASS, props);
      users = getNumberedProps(XMLDBMSProps.USER, props);
      passwords = getNumberedProps(XMLDBMSProps.PASSWORD, props);

      // Check that we have valid properties.

      if ((dbNames == null) || (dataSourceClasses == null))
         throw new XMLMiddlewareException("You must specify a single DataSourceClass property, or one or more DBName1 (2, 3, ...) and DataSourceClass1 (2, 3, ...) properties.");

      // Process the properties.

      try
      {
         for (int i = 0; i < dataSourceClasses.length; i++)
         {
            // Create a new DBInfo object.

            dbInfo = new DBInfo();
            dbInfos.put(dbNames[i], dbInfo);

            // Create the DataSource object and fill in the DBInfo object.

            dbInfo.dataSource = createDataSource(dataSourceClasses[i], props, i);
            dbInfo.user = (users == null) ? null : users[i];
            dbInfo.password = (passwords == null) ? null : passwords[i];
         }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new XMLMiddlewareException("If you specify them, you must have the same number of " + XMLDBMSProps.DBNAME + ", " + XMLDBMSProps.DATASOURCECLASS + ", " + XMLDBMSProps.USER + ", and " + XMLDBMSProps.PASSWORD + " properties.");
      }

      // Return the Hashtable of DBInfos

      return dbInfos;
   }

   private DataSource createDataSource(String dataSourceClass, Properties props, int suffix)
   {
      if (dataSourceClass == null)
         throw new IllegalArgumentException("You must specify a DataSourceClass property");

      // Currently, we only recognize the JDBC1DataSource and
      // JDBC2DataSource classes. In the future, we should instantiate
      // other data source classes through Reflection.

      if (dataSourceClass.equals(JDBC1DATASOURCE))
      {
         return createJDBC1DataSource(props, suffix);
      }
      else if (dataSourceClass.equals(JDBC2DATASOURCE))
      {
         return createJDBC2DataSource(props, suffix);
      }
      else
      {
         throw new IllegalArgumentException("The data source class must be c or org.xmlmiddleware.db.JDBC2DataSource.");
      }
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

   // ************************************************************************
   // Private methods -- namespaces
   // ************************************************************************

   private Hashtable getNamespaceURIOrURIs(Properties props)
      throws XMLMiddlewareException
   {
      Hashtable namespaceURIs;
      String    uri, prefix;

      // The user can specify a single namespace or multiple namespaces.
      // Check which one is being used.

      uri = props.getProperty(XMLDBMSProps.URI);
      if (uri != null)
      {
         namespaceURIs = new Hashtable();
         prefix = props.getProperty(XMLDBMSProps.PREFIX);
         if (prefix == null)
            throw new XMLMiddlewareException("Invalid to specify namespace URI without namespace prefix.");
         namespaceURIs.put(prefix, uri);
      }
      else
      {
         namespaceURIs = getNamespaceURIs(props);
      }

      return namespaceURIs;
   }

   private Hashtable getNamespaceURIs(Properties props)
      throws XMLMiddlewareException
   {
      String[]  uris, prefixes;
      Hashtable namespaceURIs;

      // Get the various property values.

      uris = getNumberedProps(XMLDBMSProps.URI, props);
      prefixes = getNumberedProps(XMLDBMSProps.PREFIX, props);

      // Check that we have valid properties.

      if ((uris == null) && (prefixes == null)) return null;

      if (((uris == null) && (prefixes != null)) ||
          ((uris != null) && (prefixes == null)))
         throw new IllegalArgumentException("If you specify numbered " + XMLDBMSProps.URI + " properties, you must also specify numbered " + XMLDBMSProps.PREFIX + " properties and vice versa.");

      // Process the properties.

      namespaceURIs = new Hashtable();
      try
      {
         for (int i = 0; i < uris.length; i++)
         {
            namespaceURIs.put(prefixes[i], uris[i]);
         }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new XMLMiddlewareException("If you specify them, you must have the same number of " + XMLDBMSProps.URI + " and " + XMLDBMSProps.PREFIX + " properties.");
      }

      // Return the Hashtable of namespace URIs

      return namespaceURIs;
   }

   // ************************************************************************
   // Private methods -- general
   // ************************************************************************

   private Writer getWriter(String filename, Properties props)
      throws XMLMiddlewareException
   {
      String        encoding;
      OutputStream  outputStream;

      // Create a new Writer. How we do this depends on whether we use the
      // default encoding or a different encoding.

      try
      {
         encoding = props.getProperty(XMLDBMSProps.ENCODING);
         if (encoding != null)
         {
            outputStream = new FileOutputStream(filename);
            return new OutputStreamWriter(outputStream, encoding);
         }
         else
         {
         return new FileWriter(filename);
         }
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }              
   }

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

   private boolean getYesNo(String value)
      throws XMLMiddlewareException
   {
      if (value.equals(XMLDBMSProps.YES)) return true;
      if (value.equals(XMLDBMSProps.NO)) return false;
      throw new XMLMiddlewareException("Invalid Yes/No value: " + value);
   }

   private String[] getUnnumberedProps(String prop, Properties props)
   {
      String   value;
      String[] values;

      value = props.getProperty(prop);
      if (value == null) return null;
      values = new String[1];
      values[0] = value;
      return values;
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