// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;
import de.tudarmstadt.ito.xmldbms.DOMToDBMS;
import de.tudarmstadt.ito.xmldbms.TransferEngine;

import org.w3c.dom.DOMException;

import java.util.Properties;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * Simplified interface to XML-DBMS.
 *
 * <p>Transfer provides three interfaces for using XML-DBMS: a
 * traditional API, a dispatch-style API, and a command line interface.
 * The dispatch-style API consists of a single method (dispatch). The
 * command line interface also consists of a single method (main). All
 * other methods belong to the traditional API.</p>
 *
 * <p>The traditional API can transfer data between the database and a
 * file or a string. Strings are a useful way to represent an XML
 * document, since they can easily be passed to/from an XSLT processor,
 * HTTP, etc. The dispatch-style API and the command line interface can
 * only transfer data between a database and a file.</p>
 *
 * <p>All of Transfer's interfaces use properties. The dispatch-style
 * API and command-line interface are entirely properties-based, while
 * the traditional API uses properties for information likely to remain
 * constant over the life of the application and arguments for
 * information that changes more frequently.</p>
 *
 * <p>There are four general classes of properties: database properties,
 * parser properties, key generator properties, and action properties:</p>
 *
 * <ul>
 * <li>Database properties are used to connect to the database. They are
 *     Driver, URL, User, Password, DBInitialContext, DataSource, and
 *     JDBCLevel.</li>
 *
 * <li>ParserProperties provide information about the XML parser / DOM
 *     implementation. They are ParserUtilsClass and NameQualifierClass.</p>
 *
 * <li>Key generator properties provide the name of a class that
 *     implements the KeyGenerator interface (the KeyGeneratorClass
 *     property) and initialization values for the KeyGenerator. Key
 *     generator initialization properties are specific to the key
 *     generator you use; see the key generator's documentation for more
 *     information.</li>
 *
 * <li>Action properties include a special property -- Action -- that
 * tells the dispatch-style interface and command-line interface what
 * action to take. Other action properties are MapFile, XMLFile,
 * CommitMode, Select, Table, and Key. Of these, only MapFile and
 * CommitMode are used by the traditional API.</li>
 * </ul>
 *
 * <p>For a complete description of the properties used by Transfer,
 * see appendix A of the readme file.</p>
 *
 * <p>The syntax of the command-line interface is:</p>
 *
 * <pre>
 *    java Transfer <property>=<value> [<property>=<value>...]
 * </pre>
 *
 * <p>Property/value pairs are read in order and, if a property occurs more
 * than once, the last value is used. If a property/value pair contains
 * spaces, the entire pair must be enclosed in quotes.</p>
 *
 * <p>For example, the following is used to transfer data from the
 * sales_in.xml file to the database:</p>
 *
 * <pre>
 *    java Transfer Driver=sun.jdbc.odbc.JdbcOdbcDriver URL=jdbc:odbc:xmldbms
 *                  User=ron Password=ronpwd
 *                  ParserUtilsClass=de.tudarmstadt.ito.domutils.ParserUtilsXerces
 *                  NameQualifierClass=de.tudarmstadt.ito.domutils.NQ_DOM2
 *                  Action=StoreDocument MapFile=sales.map XMLFile=sales_in.xml
 * </pre>
 *
 * <p>A special property, File, can be used to designate a file containing
 * other properties. For example, if the database properties are stored in
 * db.props and the parser properties stored in xerces.props, the following
 * is equivalent to the previous command line:</p>
 *
 * <pre>
 *    java Transfer File=db.props File=xerces.props Action=StoreDocument
 *                  MapFile=sales.map XMLFile=sales_in.xml
 * </pre>
 *
 * <p>The File property can only be used from the command line. It cannot
 * be used with the dispatch method.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps
 * @see de.tudarmstadt.ito.xmldbms.TransferEngine
 */

public class Transfer extends ProcessProperties {

	TransferEngine engine = new TransferEngine();

	// ************************************************************************
	// Constructor
	// ************************************************************************

	/**
	* Construct a Transfer object.
	*/
	public Transfer() {
		super();
	}

	// ************************************************************************
	// Public methods
	// ************************************************************************

	/**
	* Run Transfer from a command line.
	*
      * @param args Tokens representing property/value pairs. For more information,
      *    see the introduction.
	*/

	public static void main(String[] args) {
		Properties props;
		Transfer trans = new Transfer();

		try {
			if (args.length < 1) {
				System.out.println(
					"Usage: Transfer <property>=<value> [<property>=<value>...]\n"
						+ "See the documentation for a list of valid properties.");
				return;
			}

			props = trans.getProperties(args, 0);
			trans.dispatch(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	* Executes the action specified by the Action property.
	*
      * <p>For a list of valid actions (values of the Action property) and
      * the properties each of these actions needs, see appendix A of the
      * readme file.
      *
      * @param props The Properties object.
	*/

	public void dispatch(Properties props) throws Exception {

		String action;

		// Set up the Transfer Engine
		init(props);
		//	 engine.setParserProperties(props);
		//	 engine.setDatabaseProperties(props);

		// Get the action
		action = props.getProperty(XMLDBMSProps.ACTION);
		if (action == null) {
			throw new IllegalArgumentException("Action property not specified.");
		}
		// Dispatch the action
		if (action.equals(XMLDBMSProps.STOREDOCUMENT)) {
			dispatchStoreDocument(props);
		} else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYSQL)) {
			dispatchRetrieveDocumentBySQL(props);
		} else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYKEY)) {
			dispatchRetrieveDocumentByKey(props);
		} else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYKEYS)) {
			dispatchRetrieveDocumentByKeys(props);
		} else {
			throw new IllegalArgumentException("Unknown value of Action property: " + action);
		}
	}

	/**
       * Initialize the Transfer object.
       *
       * <p>The initialization properties must include:</p>
       *
       * <ul>
       * <li>Driver, URL, User (optional), Password (optional) (if JDBC 1.0 is used)</li>
       * <li>DataSource, DBInitialContext, User (optional), Password (optional) (if
       *     JDBC 2.0 is used)</li>
       * <li>JDBCLevel</li>
       * <li>ParserUtilsClass</li>
       * <li>NameQualifierClass (if XML will be stored in the database)</li>
       * </ul>
       *
	 * @param props The initialization properties.
	 */

	public void init(Properties props) throws java.lang.Exception {

		engine.init(props);
	}

	/**
	 * Retrieves XML as a string using a table name and a key.
       *
       * @param props This must contain the MapFile property.
       * @param tableName Name of the root table
       * @param key The root key
       *
       * @return The XML document as a string
	 */

	public String retrieveXMLStringByKey(
		Properties props,
		String tableName,
		Object[] key)
		throws Exception {
		String mapFilename;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

		return engine.retrieveDocument(mapFilename, tableName, key);
	}

	/**
	 * Retrieves XML using a table name and a key and stores it in a file.
       *
       * @param props This must contain the MapFile property.
       * @param tableName Name of the root table
       * @param key The root key
       * @param xmlFilename Name of the file in which to store the XML
	 */
	public void retrieveXMLFileByKey(
		Properties props,
		String tableName,
		Object[] key,
		String xmlFilename)
		throws Exception {
		String mapFilename;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

		engine.retrieveDocument(mapFilename, xmlFilename, tableName, key);

	}

	/**
	 * Retrieves XML as a string using multiple table names and keys.
       *
       * @param props This must contain the MapFile property.
       * @param tableNames An array of pseudo-root tables
       * @param key An array of keys for the pseudo-root tables
       *
       * @return The XML document as a string
	 */
	public String retrieveXMLStringByKeys(
		Properties props,
		String[] tableNames,
		Object[][] keys)
		throws Exception {
		String mapFilename;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

		return engine.retrieveDocument(mapFilename, tableNames, keys);
	}

	/**
	 * Retrieves XML using multiple table names and keys and stores it in a file.
       *
       * @param props This must contain the MapFile property.
       * @param tableNames An array of pseudo-root tables
       * @param key An array of keys for the pseudo-root tables
       * @param xmlFilename Name of the file in which to store the XML
	 */
	public void retrieveXMLFileByKeys(
		Properties props,
		String[] tableNames,
		Object[][] keys,
		String xmlFilename)
		throws Exception {
		String mapFilename;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

		engine.retrieveDocument(mapFilename, xmlFilename, tableNames, keys);

	}

	/**
	 * Retrieves XML as a string using a select statement over the root table.
       *
       * <p>The map must have a class mapped to "Result Set".</p>
       *
       * @param props This must contain the MapFile property. It may contain
       *              the Select property as well as zero or more numbered
       *              Select properties (Select1, Select2, ...). These are
       *              pre-pended to the value of the sqlString argument. In
       *              this manner, the Select properties can be used to form
       *              a base SELECT statement and the sqlString argument can
       *              be used to add WHERE clauses, etc.
       * @param sqlString The SELECT statement or a part thereof.
       *
       * @return The XML document as a string
	 */
	public String retrieveXMLStringBySQL(Properties props, String sqlString)
		throws Exception {
		String mapFilename, select;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

		if (sqlString != null) {
			select = concatNumberedProps(XMLDBMSProps.SELECT, props, true) + sqlString;
		} else {
			select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);
		}

		return engine.retrieveDocument(mapFilename, select);
	}

	/**
	 * Retrieves XML using a select statement over the root table and stores it in a file.
       *
       * <p>The map must have a class mapped to "Result Set".</p>
       *
       * @param xmlFilename Name of the file in which to store the XML
       * @param props This must contain the MapFile property. It may contain
       *              the Select property as well as zero or more numbered
       *              Select properties (Select1, Select2, ...). These are
       *              pre-pended to the value of the sqlString argument. In
       *              this manner, the Select properties can be used to form
       *              a base SELECT statement and the sqlString argument can
       *              be used to add WHERE clauses, etc.
       * @param sqlString The SELECT statement or a part thereof.
       *
       * @return The XML document as a string
	 */
	public void retrieveXMLFileBySQL(
		Properties props,
		String xmlFilename,
		String sqlString)
		throws Exception {
		String mapFilename, select;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
		if (sqlString != null) {
			select = concatNumberedProps(XMLDBMSProps.SELECT, props, true) + sqlString;
		} else {
			select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);
		}

		engine.retrieveDocument(mapFilename, xmlFilename, select);

	}

	/**
	 * Store XML from a file.
       *
       * @param props This must contain the MapFile property. It may contain
       *              the CommitMode and KeyGeneratorClass properties. If it
       *              contains the KeyGeneratorClass property, it must also
       *              contain any initialization properties needed by that class.
       * @param xmlFilename Name of the file containing the XML
	 */
	public void storeXMLFile(Properties props, String xmlFilename)
		throws Exception {
		String mapFilename, keyGeneratorClass;
		int commitMode;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

            commitMode = getCommitMode(props);

		keyGeneratorClass = props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);

		engine.storeXMLFile(
			mapFilename,
			xmlFilename,
			commitMode,
			keyGeneratorClass,
			props);

	}

	/**
	 * Store XML from a string.
       *
       * @param props This must contain the MapFile property. It may contain
       *              the CommitMode and KeyGeneratorClass properties. If it
       *              contains the KeyGeneratorClass property, it must also
       *              contain any initialization properties needed by that class.
       * @param xmlString The XML string
	 */
	public void storeXMLString(Properties props, String xmlString)
		throws Exception {
		String mapFilename, keyGeneratorClass;
		int commitMode;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

            commitMode = getCommitMode(props);

		keyGeneratorClass = props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);

		engine.storeXMLString(
			mapFilename,
			xmlString,
			commitMode,
			keyGeneratorClass,
			props);
	}

	/**
	 * Store XML from an InputStream.
       *
       * @param props This must contain the MapFile property. It may contain
       *              the CommitMode and KeyGeneratorClass properties. If it
       *              contains the KeyGeneratorClass property, it must also
       *              contain any initialization properties needed by that class.
       * @param xmlString The InputString
	 */
	public void storeXMLStream(Properties props, InputStream xmlStream)
		throws Exception {
		String mapFilename, keyGeneratorClass;
		int commitMode;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);

            commitMode = getCommitMode(props);

		keyGeneratorClass = props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);

		engine.storeXMLInputStream(
			mapFilename,
			xmlStream,
			commitMode,
			keyGeneratorClass,
			props);
	}

	// ************************************************************************
	// Private methods
	// ************************************************************************

	/** 
	 * Gets the CommitMode for storing Documents
	 * Possible values are :
	 * COMMIT_AFTERINSERT
	 * COMMIT_AFTERDOCUMENT
       * COMMIT_NONE
	 */

	private int getCommitMode(Properties props) {
            String modeName;

            modeName = props.getProperty(XMLDBMSProps.COMMITMODE);

            if (modeName == null) return DOMToDBMS.COMMIT_AFTERDOCUMENT;

		if (modeName.equals(XMLDBMSProps.AFTERINSERT)) {
			return DOMToDBMS.COMMIT_AFTERINSERT;
		} else if (modeName.equals(XMLDBMSProps.AFTERDOCUMENT)) {
			return DOMToDBMS.COMMIT_AFTERDOCUMENT;
		} else if (modeName.equals(XMLDBMSProps.NONE)) {
			return DOMToDBMS.COMMIT_NONE;
		} else
			throw new IllegalArgumentException("Invalid commit mode value: " + modeName);
	}

	/** The Store Document method of Dispatch
	 */

	private void dispatchStoreDocument(Properties props) throws Exception {
		String mapFilename, xmlFilename, keyGeneratorClass;
		int commitMode;
		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
		xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);

            commitMode = getCommitMode(props);

		keyGeneratorClass = props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);

		engine.storeXMLFile(
			mapFilename,
			xmlFilename,
			commitMode,
			keyGeneratorClass,
			props);
	}

	/**
	 * The Retrieve by Key method for Dispatch
	 */

	private void dispatchRetrieveDocumentByKey(Properties props) throws Exception {
		String mapFilename, xmlFilename, table;
		String[] key;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
		xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
		table = props.getProperty(XMLDBMSProps.TABLE + String.valueOf(1));
		key = getNumberedProps(XMLDBMSProps.KEY, props);

		engine.retrieveDocument(mapFilename, xmlFilename, table, key);
	}

	/**
	 * The Retrieve by Keys method for Dispatch
	 */

	private void dispatchRetrieveDocumentByKeys(Properties props)
		throws Exception {
		String mapFilename, xmlFilename;
		String[] tables;
		String[][] keys;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
		xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
		tables = getNumberedProps(XMLDBMSProps.TABLE, props);
		keys = getDoubleNumberedProps(XMLDBMSProps.KEY, props);

		engine.retrieveDocument(mapFilename, xmlFilename, tables, keys);
	}

	/**
	 * The Retrieve by SQL Statement method for Dispatch
	 */

	private void dispatchRetrieveDocumentBySQL(Properties props) throws Exception {
		String mapFilename, xmlFilename, select;

		mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
		xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
		select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);

		engine.retrieveDocument(mapFilename, xmlFilename, select);
	}

}