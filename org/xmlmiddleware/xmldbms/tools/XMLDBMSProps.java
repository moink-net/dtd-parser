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
 * Properties constants for Transfer and MapGenerator.
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 * @see MapGenerator
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

   // Transfer properties

   public static String METHOD = "Method";
   public static String MAPFILE = "MapFile";
   public static String XMLFILE = "XMLFile";
   public static String ACTIONFILE = "ActionFile";
   public static String FILTERFILE = "FilterFile";

   public static String SELECT = "Select";
   public static String SELECTDBNAME = "SelectDBName";
   public static String SELECTRESULTSETNAME = "SelectResultSetName";

   // Configuration properties

   public static String ENCODING = "Encoding";
   public static String SYSTEMID = "SystemID";
   public static String PUBLICID = "PublicID";
   public static String COMMITMODE = "CommitMode";
   public static String STOPONERROR = "StopOnError";
   public static String RETURNFILTER = "ReturnFilter";
   public static String KEYGENERATORNAME = "KeyGeneratorName";
   public static String KEYGENERATORCLASS = "KeyGeneratorClass";

   // Property values -- methods

   public static String STOREDOCUMENT = "StoreDocument";
   public static String RETRIEVEDOCUMENTBYSQL = "RetrieveDocumentBySQL";
   public static String RETRIEVEDOCUMENTBYFILTER = "RetrieveDocumentByFilter";
   public static String DELETEDOCUMENT = "DeleteDocument";

   // Property values -- commit modes

   public static String AFTERSTATEMENT = "AfterStatement";
   public static String AFTERDOCUMENT = "AfterDocument";
   public static String NONE = "None";
   public static String NOTRANSACTIONS = "NoTransactions";
}