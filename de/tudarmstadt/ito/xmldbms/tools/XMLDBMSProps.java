// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;

// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

/**
 * Properties constants for Transfer and GenerateMap.
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see GenerateMap
 * @see Transfer
 * @see ProcessProperties
 */

public class XMLDBMSProps
{

   // Database properties

   /** Database URL */
   public static String URL      = "URL";

   /** JDBC driver class name */
   public static String DRIVER   = "Driver";

   /**  Database user name */
   public static String USER     = "User";

   /**  Database password */
   public static String PASSWORD = "Password";

   // Parser properties

   /**  Name of class that implements the de.tudarmstadt.ito.domutils.NameQualifier interface */
   public static String NAMEQUALIFIERCLASS = "NameQualifierClass";

   /**  Name of class that implements the de.tudarmstadt.ito.domutils.DocumentFactory interface */
   public static String DOCUMENTFACTORYCLASS = "DocumentFactoryClass";

   /**  Name of class that implements the de.tudarmstadt.ito.domutils.ParserUtils interface */
   public static String PARSERUTILSCLASS = "ParserUtilsClass";

   // Action properties

   /**
	* The name of an action to take.
	*
	* <p>For more information, see de.tudarmstadt.ito.xmldbms.tools.Transfer
	* and de.tudarmstadt.ito.xmldbms.tools.GenerateMap.</p>
   */   
   public static String ACTION            = "Action";

   /**  Name of the map file. */
   public static String MAPFILE           = "MapFile";

   /**  Name of the XML file to transfer data to/from.*/
   public static String XMLFILE           = "XMLFile";

   /**
	* Commit mode when transferring data to the database.
	*
	* <p>Legal values are "AfterInsert" and "AfterDocument".</p>
	*/
   public static String COMMITMODE        = "CommitMode";

   /** Name of the class that implements the de.tudarmstadt.ito.xmldbms.KeyGenerator interface. */
   public static String KEYGENERATORCLASS = "KeyGeneratorClass";

   /**  A SELECT statement to use when transferring data to XML. */
   public static String SELECT            = "Select";

   /**  Table name. */
   public static String TABLE             = "Table";

   /**  Key value. */
   public static String KEY               = "Key";

   /**  XML schema file name.
	*
	* <p>Currently, only DTDs (external or in an XML document)
	* and DDML files are supported.</p>
	*/
   public static String SCHEMAFILE        = "SchemaFile";

   /**
	* Whether order columns should be generated.
	*
	* <p>Legal values are "Yes" and "No".</p>
	*/
   public static String ORDERCOLUMNS      = "OrderColumns";

   /** Database catalog name. */
   public static String CATALOG           = "Catalog";

   /** Database schema name. */
   public static String SCHEMA            = "Schema";

   /**
	* Separator to be used between CREATE TABLE statements.
	*
	* <p>If this is not set, a semi-colon (;) is used.
	*/
   public static String SQLSEPARATOR      = "SQLSeparator";

   /** Namespace prefix. */
   public static String PREFIX            = "Prefix";

   /** Namespace URI. */
   public static String NAMESPACEURI      = "NamespaceURI";

   /** ???? */
   public static String BASENAME          = "Basename";

   // Legal property values

   /** A "Yes" value for yes/no properties. Case sensitive. */
   public static String YES               = "Yes";

   /** A "No" value for yes/no properties. Case sensitive. */
   public static String NO                = "No";

   /** An "AfterInsert" value for the CommitMode property. Case sensitive. */
   public static String AFTERINSERT       = "AfterInsert";

   /** An "AfterDocument" value for the CommitMode property. Case sensitive. */
   public static String AFTERDOCUMENT     = "AfterDocument";

   /** "StoreDocument" action. Case sensitive. */
   public static String STOREDOCUMENT     = "StoreDocument";

   /** "RetrieveDocumentBySQL" action. Case sensitive. */
   public static String RETRIEVEDOCUMENTBYSQL     = "RetrieveDocumentBySQL";

   /** "RetrieveDocumentByKey" action. Case sensitive. */
   public static String RETRIEVEDOCUMENTBYKEY     = "RetrieveDocumentByKey";

   /** "RetrieveDocumentByKeys" action. Case sensitive. */
   public static String RETRIEVEDOCUMENTBYKEYS     = "RetrieveDocumentByKeys";

   /** "CreateMapFromXMLSchema" action. Case sensitive. */
   public static String CREATEMAPFROMXMLSCHEMA     = "CreateMapFromXMLSchema";

   /** "CreateMapFromDTD" action. Case sensitive. */
   public static String CREATEMAPFROMDTD     = "CreateMapFromDTD";

   /** "CreateMapFromTable" action. Case sensitive. */
   public static String CREATEMAPFROMTABLE     = "CreateMapFromTable";

   /** "CreateMapFromTables" action. Case sensitive. */
   public static String CREATEMAPFROMTABLES     = "CreateMapFromTables";

   /** "CreateMapFromSelect" action. Case sensitive. */
   public static String CREATEMAPFROMSELECT     = "CreateMapFromSelect";


   // Read / write properties files

   /** Name of the input file from which to read properties. */
   public static String FILE = "File";

   /** Name of the output file in which to store properties. */
   public static String OUTPUTFILE = "OutputFile";

   // ************************************************************************
   // Constructor
   // ************************************************************************

   public XMLDBMSProps()
   {
	  super();
   }   
	public static String DATASOURCE = "DataSource";/** JDBC Level & JDBC 2.0 */	
	public static String DBINITIALCONTEXT = "DBInitialContext";/** Document Root */

	public static String DOCROOT = "DocRoot";/** JNDI information */
	public static String INITIALCONTEXT = "InitialContext";	public static String JDBCLEVEL     = "JDBCLevel";	public static String JMSACKMODE = "JMSAckMode";/** JMS Info */	

	public static String JMSCONTEXT = "JMSContext";	public static String JMSMESSAGE = "JMSMessage";	public static String JMSPASSWORD = "JMSPassword";	public static String JMSPROVIDERURL = "JMSProviderURL";	public static String JMSTCF = "JMSTCF";	public static String JMSTOPIC = "JMSTopic";	public static String JMSUSER = "JMSUser";	public static String JNDI     = "JNDI";	public static String KEYGENCAT = "KeyGenCat";	public static String KEYGENSCHEMA = "KeyGenSchema";	public static String KEYGENSEP = "KeyGenSep";/** Key Gen Info */	

	public static String KEYGENTABLE = "KeyGenTable";	public static String USEDOCROOT = "UseDocRoot";	public static String XSLTCLASS = "XSLTClass";	public static String XSLTOUTPUT = "XSLTOutput";/** XSLT info */

	public static String XSLTSCRIPT = "XSLTScript";}