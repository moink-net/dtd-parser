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
// Changes from version 1.01: New in 1.1
// Changes from version 1.1: Updated for version 2.0

package org.xmlmiddleware.xmldbms.tools;

/**
 * Properties constants for Transfer and MapManager.
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 * @see MapManager
 * @see Transfer
 * @see PropertyProcessor
 */
public class XMLDBMSProps
{
   // Properties used to process properties.

   public static String BASEURL = "BaseURL";
   public static String FILE = "File";

   // Parser properties

   public static String PARSERUTILSCLASS = "ParserUtilsClass";
   public static String VALIDATE = "Validate";

   // Database properties

   public static String DBNAME = "DBName";
   public static String DATAHANDLERCLASS = "DataHandlerClass";
   public static String DATASOURCECLASS = "DataSourceClass";
   public static String USER = "User";
   public static String PASSWORD = "Password";
   public static String DRIVER = "Driver";
   public static String URL = "URL";
   public static String JNDICONTEXT = "JNDIContext";
   public static String JNDILOOKUPNAME = "JNDILookupName";

   // Transfer and map manager properties

   public static String METHOD = "Method";
   public static String INPUT = "Input";
   public static String OUTPUT = "Output";

   public static String MAPLOCATION = "MapLocation";
   public static String XMLLOCATION = "XMLLocation";
   public static String ACTIONLOCATION = "ActionLocation";
   public static String FILTERLOCATION = "FilterLocation";
   public static String MAPRESOLVERCLASS = "MapResolverClass";
   public static String XMLRESOLVERCLASS = "XMLResolverClass";
   public static String ACTIONRESOLVERCLASS = "ActionResolverClass";
   public static String FILTERRESOLVERCLASS = "FilterResolverClass";

   public static String MAPFILE = "MapFile";
   public static String DTDFILE = "DTDFile";
   public static String SQLFILE = "SQLFile";

   public static String SELECT = "Select";
   public static String SELECTDBNAME = "SelectDBName";
   public static String SELECTRESULTSETNAME = "SelectResultSetName";

   public static String ROOTDATABASE = "RootDatabase";
   public static String ROOTCATALOG = "RootCatalog";
   public static String ROOTSCHEMA = "RootSchema";
   public static String ROOTTABLE = "RootTable";
   public static String STOPDATABASE = "StopDatabase";
   public static String STOPCATALOG = "StopCatalog";
   public static String STOPSCHEMA = "StopSchema";
   public static String STOPTABLE = "StopTable";

   // Configuration properties

   public static String ENCODING = "Encoding";
   public static String SYSTEMID = "SystemID";
   public static String PUBLICID = "PublicID";
   public static String COMMITMODE = "CommitMode";
   public static String STOPONERROR = "StopOnError";
   public static String RETURNFILTER = "ReturnFilter";
   public static String KEYGENERATORNAME = "KeyGeneratorName";
   public static String KEYGENERATORCLASS = "KeyGeneratorClass";
   public static String ORDERTYPE = "OrderType";
   public static String DATABASENAME = "DatabaseName";
   public static String CATALOGNAME = "CatalogName";
   public static String SCHEMANAME = "SchemaName";
   public static String PREFIX = "Prefix";
   public static String URI = "URI";
   public static String MAPCOLUMNSAS = "MapColumnsAs";
   public static String FOLLOWPRIMARYKEYS = "FollowPrimaryKeys";
   public static String FOLLOWFOREIGNKEYS = "FollowForeignKeys";
   public static String PRETTY = "Pretty";
   public static String INDENT = "Indent";
   public static String SQLSEPARATOR = "SQLSeparator";

   // Property values -- methods / input / output

   public static String STOREDOCUMENT = "StoreDocument";
   public static String RETRIEVEDOCUMENTBYSQL = "RetrieveDocumentBySQL";
   public static String RETRIEVEDOCUMENTBYFILTER = "RetrieveDocumentByFilter";
   public static String DELETEDOCUMENT = "DeleteDocument";

   public static String MAP = "Map";
   public static String DTD = "DTD";
   public static String DATABASE = "Database";
   public static String SQL = "SQL";

   // Property values -- commit modes

   public static String AFTERSTATEMENT = "AfterStatement";
   public static String AFTERDOCUMENT = "AfterDocument";
   public static String NONE = "None";
   public static String NOTRANSACTIONS = "NoTransactions";

   // Property values -- general

   public static String YES = "Yes";
   public static String NO = "No";

   // Property values -- validation

   public static String MAPTOKEN = " Map ";
   public static String XMLTOKEN = " XML ";
   public static String ACTIONTOKEN = " Action ";
   public static String FILTERTOKEN = " Filter ";

   // Property values -- order type

   public static String FIXED = "Fixed";
//   public static String NONE = "None"; // Already declared as a commit mode
   public static String COLUMNS = "Columns";

   // Property values -- map column as

   public static String ELEMENTTYPES = "ElementTypes";
   public static String ATTRIBUTES = "Attributes";
}