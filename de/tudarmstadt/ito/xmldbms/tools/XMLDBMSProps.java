// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001
// Version 1.1
// Changes from version 1.01: New in 1.1
package de.tudarmstadt.ito.xmldbms.tools;

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
    // ************************************************************************
    // Parser properties
    // ************************************************************************

    /**  Name of class that implements the de.tudarmstadt.ito.domutils.ParserUtils interface */
    public static String PARSERUTILSCLASS = "ParserUtilsClass";

    /**  Name of class that implements the de.tudarmstadt.ito.domutils.NameQualifier interface */
    public static String NAMEQUALIFIERCLASS = "NameQualifierClass";

    // ************************************************************************
    // Action properties
    // ************************************************************************

    /**
    	* The name of an action to take.
    	*
    	* <p>For more information, see de.tudarmstadt.ito.xmldbms.tools.Transfer
    	* and de.tudarmstadt.ito.xmldbms.tools.GenerateMap.</p>
    */
    public static String ACTION = "Action";

    /**  Name of the map file. */
    public static String MAPFILE = "MapFile";

    /**  Name of the XML file to transfer data to/from.*/
    public static String XMLFILE = "XMLFile";

    /**
    	* Commit mode when transferring data to the database.
    	*
    	* <p>Legal values are "AfterInsert" and "AfterDocument".</p>
    	*/
    public static String COMMITMODE = "CommitMode";

    /** Name of the class that implements the de.tudarmstadt.ito.xmldbms.KeyGenerator interface. */
    /**  A SELECT statement to use when transferring data to XML. */
    public static String SELECT = "Select";

    /**  Table name. */
    public static String TABLE = "Table";

    /**  Key value. */
    public static String KEY = "Key";

    // ************************************************************************
    // Key generator properties
    // ************************************************************************

    /**  Name of a class that implements the KeyGenerator interface. */
    public static String KEYGENERATORCLASS = "KeyGeneratorClass";

    // ************************************************************************
    // GenerateMap properties
    // ************************************************************************

    /**  XML schema file name.
    	*
    	* <p>Currently, only DTDs (external or in an XML document)
    	* and DDML files are supported.</p>
    	*/
    public static String SCHEMAFILE = "SchemaFile";

    /**
    	* Whether order columns should be generated.
    	*
    	* <p>Legal values are "Yes" and "No".</p>
    	*/
    public static String ORDERCOLUMNS = "OrderColumns";

    /** Database catalog name. */
    public static String CATALOG = "Catalog";

    /** Database schema name. */
    public static String SCHEMA = "Schema";

    /**
    	* Separator to be used between CREATE TABLE statements.
    	*
    	* <p>If this is not set, a semi-colon (;) is used.
    	*/
    public static String SQLSEPARATOR = "SQLSeparator";

    /** Namespace prefix. */
    public static String PREFIX = "Prefix";

    /** Namespace URI. */
    public static String NAMESPACEURI = "NamespaceURI";

    /** Basename of the map file to generate */
    public static String BASENAME = "Basename";

    // ************************************************************************
    // Legal property values
    // ************************************************************************

    /** A "Yes" value for yes/no properties. Case sensitive. */
    public static String YES = "Yes";

    /** A "No" value for yes/no properties. Case sensitive. */

    public static String NO = "No";

    /** An "AfterInsert" value for the CommitMode property. Case sensitive. */
    public static String AFTERINSERT = "AfterInsert";

    /** An "AfterDocument" value for the CommitMode property. Case sensitive. */
    public static String AFTERDOCUMENT = "AfterDocument";

    /** A "None" value for the CommitMode property. Case sensitive. */
    public static String NONE = "None";

    /** "StoreDocument" action. Case sensitive. */
    public static String STOREDOCUMENT = "StoreDocument";

    /** "RetrieveDocumentBySQL" action. Case sensitive. */
    public static String RETRIEVEDOCUMENTBYSQL = "RetrieveDocumentBySQL";

    /** "RetrieveDocumentByKey" action. Case sensitive. */
    public static String RETRIEVEDOCUMENTBYKEY = "RetrieveDocumentByKey";

    /** "RetrieveDocumentByKeys" action. Case sensitive. */
    public static String RETRIEVEDOCUMENTBYKEYS = "RetrieveDocumentByKeys";

    /** "CreateMapFromXMLSchema" action. Case sensitive. */
    public static String CREATEMAPFROMXMLSCHEMA = "CreateMapFromXMLSchema";

    /** "CreateMapFromDTD" action. Case sensitive. */
    public static String CREATEMAPFROMDTD = "CreateMapFromDTD";

//    /** "CreateMapFromTable" action. Case sensitive. */
//    public static String CREATEMAPFROMTABLE = "CreateMapFromTable";
//
//    /** "CreateMapFromTables" action. Case sensitive. */
//    public static String CREATEMAPFROMTABLES = "CreateMapFromTables";
//
//    /** "CreateMapFromSelect" action. Case sensitive. */
//    public static String CREATEMAPFROMSELECT = "CreateMapFromSelect";

    // ************************************************************************
    // File properties
    // ************************************************************************

    /** Name of the input file from which to read properties. */
    public static String FILE = "File";

    /** Name of the output file in which to store properties. */
    public static String OUTPUTFILE = "OutputFile";

    /** Root directory for documents */
    public static String DOCROOT = "DocRoot";

    /** Whether to use the DocRoot property.
     *
     * <p>Legal values are Yes and No.</p>
     */
    public static String USEDOCROOT = "UseDocRoot";

    // ************************************************************************
    // Miscellaneous properties
    // ************************************************************************

    /** JNDI initial context */
    public static String INITIALCONTEXT = "InitialContext";
}