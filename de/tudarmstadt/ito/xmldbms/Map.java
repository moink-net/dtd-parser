// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Change declaration of byte array constants
// * Changed error messages in addColumnMetadata and checkColumnTypesSet
// * Fixed bug in addResultSetMetadata where column number not set
// * Quote table names in buildInsert/Select/CreateTableString
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;


import de.tudarmstadt.ito.xmldbms.mapfactories.XMLDBMSConst;
import de.tudarmstadt.ito.utils.NSName;
import de.tudarmstadt.ito.utils.XMLOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

import java.util.StringTokenizer;

/**
 * Describes how an XML document is mapped to a database and vice versa;
 * <A HREF="../readme.html#NotForUse">for limited use</A>.
 *
 * <P>Map objects describe how XML documents are mapped to databases and
 * vice versa. They are created by map factories, which can be found in
 * de.tudarmstadt.ito.xmldbms.mapfactories. Map objects are opaque. That is,
 * programmers create them with map factories and pass them to DOMToDBMS and
 * DBMSToDOM objects, but never instantiate them directly or call methods on
 * them.</P>
 * 
 * <P>For example, the following code creates a map from the sales.map
 * mapping document.</P>
 * <PRE>
 *    // Instantiate a new map factory from a database connection
 *    // and SAX parser.
 *    factory = new MapFactory_MapDocument(conn, parser);<BR />
 *
 *    // Create a Map from sales.map.
 *    map = factory.createMap(new InputSource(new FileReader("sales.map")));<BR />
 *
 *    // Pass the Map to DOMToDBMS.
 *    DOMToDBMS = new DOMToDBMS(map);
 * </PRE>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class Map extends XMLOutputStream
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private static String INSERT      = "INSERT INTO ";
   private static String VALUES      = " VALUES (";
   private static String PARAM       = "?";
   private static String COMMA       = ", ";
   private static String COMMAPARAM  = ", ?";
   private static String CLOSEPAREN  = ")";
   private static String CREATETABLE = "CREATE TABLE ";
   private static String OPENPAREN   = " (";
   private static String SPACE       = " ";
   private static String SELECT      = "SELECT ";
   private static String FROM        = " FROM ";
   private static String WHERE       = " WHERE ";
   private static String ORDERBY     = " ORDER BY ";
   private static String DESC        = " DESC";
   private static String AND         = " AND ";
   private static String EQUALSPARAM = " = ?";
   private static String PERIOD = ".";
   private static String RESULTSET = "Result Set";


   //**************************************************************************
   // Variables
   //**************************************************************************

   // Data type name variables.
   String bigintName,
		  binaryName,
		  bitName,
		  charName,
		  dateName,
		  decimalName,
		  doubleName,
		  floatName,
		  integerName,
		  longvarbinaryName,
		  longvarcharName,
		  numericName,
		  realName,
		  smallintName,
		  timeName,
		  timestampName,
		  tinyintName,
		  varbinaryName,
		  varcharName;

   // Database variables
   Connection conn = null;
   ResultSet  mappedResultSet = null;
   String[]   insertStrings, createStrings;
   String[][] selectStrings;
   Stack[]    insertStacks;
   Stack[][]  selectStacks;
   int        activeStmts = 0, maxActiveStmts;
   String     quote = null;
   Random     rnd = new Random();
   boolean    emptyStringIsNull = false,    // Are empty strings treated as
											// NULLs? Idea from Richard Sullivan
			  preparedSurviveCommit = true; // Do prepared statements survive
											// commits or do they need to
											// be reprepared?

   // Mapping variables
   Table[]    tables;
   TableMap[] tableMaps;
   Hashtable  rootClassMaps;         // Keyed by element type name
   Hashtable  classMaps;             // Keyed by element type name
   Hashtable  rootTableMaps;         // Keyed by table name
   String[]   prefixes, uris;
   boolean    generateKeys = false;  // Are any keys generated?

   // Other variables.
   DateFormat dateFormatter,
			  timeFormatter,
			  timestampFormatter;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new Map. */
   public Map(Table[]    tables,
			  TableMap[] tableMaps,
			  Hashtable  rootTableMaps,
			  Hashtable  classMaps,
			  Hashtable  rootClassMaps,
			  String[]   prefixes,
			  String[]   uris,
			  boolean    generateKeys,
			  boolean    emptyStringIsNull,
			  DateFormat dateFormatter,
			  DateFormat timeFormatter,
			  DateFormat timestampFormatter)
   {
	  this.tables = tables;
	  this.tableMaps = tableMaps;
	  this.rootTableMaps = rootTableMaps;
	  this.classMaps = classMaps;
	  this.rootClassMaps = rootClassMaps;
	  this.prefixes = prefixes;
	  this.uris = uris;
	  this.generateKeys = generateKeys;
	  this.emptyStringIsNull = emptyStringIsNull;
	  this.dateFormatter = dateFormatter;
	  this.timeFormatter = timeFormatter;
	  this.timestampFormatter = timestampFormatter;
   }   

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Initialize the Map to use a particular Connection; usually called only
	* by map factories.
	*
	* <p>This method sets the Connection used by the Map and retrieves
	* various database metadata used both when transferring data and when
	* when constructing SQL statements. Until this method is called, the
	* Map cannot be used to transfer data or get CREATE TABLE strings. It
	* is legal to call the serialize() method before this method is called.</p>
	*
	* <p>Generally, this method is called only by map factories that
	* connect to the database, such as MapFactory_MapDocument. It can be
	* called by applications, such as to use the same Map with different
	* database, but such use is likely to be rare. If an application calls
	* this method, they usually call initTableMetadata() directly afterwards
	* to initialize the Map according the the table and column metadata of
	* the new connection.</p>
	*
	* @exception SQLException Thrown if a problem occurs while retrieving
	*  the database metadata.
	*/
   public void setConnection(Connection conn)
	  throws SQLException
   {
	  this.conn = conn;
	  getDatabaseMetadata();
   }   

   /**
	* Initialize the column metadata for the current Connection.
	*
	* <p>This method initializes the column metadata for the
	* current Connection. This metadata is used both when transferring
	* data and when when constructing SQL statements. Until the metadata
	* is set, either directly or by calling this method, the Map cannot
	* be used to transfer data or get CREATE TABLE strings.</p>
	*
	* <p>Generally, this method is called only by map factories that
	* connect to the database, such as MapFactory_MapDocument. It is not
	* called by map factories that generate their own column metadata,
	* such as MapFactory_DTD, which predicts data types and lengths.</p>
	*
	* @param mappedResultSet The result set from which to retrieve metadata
	*  if the Map maps an element type to a table named "Result Set".
	*  Ignored (and may be null) if no such table is named.
	* @exception SQLException Thrown if a problem occurs while retrieving
	*  the metadata.
	* @exception InvalidMapException Thrown if a column is not found.
	* @see DBMSToDOM#retrieveDocument(ResultSet)
	*/
   public void initColumnMetadata(ResultSet mappedResultSet)
	  throws SQLException, InvalidMapException
   {
	  if (conn == null)
		 throw new IllegalStateException("Connection not set");
	  this.mappedResultSet = mappedResultSet;
	  getTableMetadata();
   }   

   /**
	* Serialize a Map in the XML-DBMS mapping language to an OutputStream.
	*
	* <p>Note that serialize cannot currently create the Locale element
	* or the values FULL, LONG, MEDIUM, or SHORT in the Date, Time, and
	* Timestamp attributes of the Patterns element.</p>
	*
	* @param out The OutputStream.
	* @exception IOException Thrown if an I/O exception occurs.
	*/
   public void serialize(OutputStream out, boolean pretty, int indent)
	  throws IOException
   {
	  setOutputStream(out);
	  setPrettyPrinting(pretty, indent);
	  allocateAttrs(3);
	  writeMapStart();
	  writeOptions();
	  writeMaps();
	  writeMapEnd();
   }   

   /**
	* Close any open prepared statements.
	*
	* <p>By default, Maps maintain INSERT and DELETE statements in a
	* prepared state for later use, such as when the same Map is used
	* to transfer data from multiple documents. The statements are
	* closed only when the application calls this method, the Map
	* object is deleted (such as by the garbage collector), or the database
	* closes them when committing a transaction.</p>
	*
	* <p>Applications can use this method to close statements and
	* release JDBC and database resources earlier than when it might
	* be done by the garbage collector. Because this method does not
	* close the Connection, the Map can be reused, although it must
	* reprepare statements as needed.<p>
	*
	* For example, consider an application that maintains a pool of
	* Maps, one for each type of XML document for which it transfers
	* data. If each Map is used infrequently, the application might
	* choose to free database resources by calling closeStatements()
	* after each use of a given Map. Although the Map must reprepare
	* statements each time it is used, this might result in better
	* overall performance.</p>
	* 
	* <p>closeStatement() is called by Map.finalize().</p>
	*
	* @exception SQLException Thrown if a problem occurs while closing
	* statements.
	*/
   public void closeStatements() throws SQLException
   {
	  // Idea from Richard Sullivan.
	  int               i, j;
	  PreparedStatement p;

	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // Close all prepared INSERT statements.
	  if (insertStacks != null)
	  {
		 for (i = 0; i < insertStacks.length; i++)
		 {
			while (!insertStacks[i].empty())
			{
			   p = (PreparedStatement)insertStacks[i].pop();
			   p.close();
			}
		 }
	  }

	  // Close all prepared SELECT statements.
	  if (selectStacks != null)
	  {
		 for (i = 0; i < selectStacks.length; i++)
		 {
			for (j = 0; j < selectStacks[i].length; j++)
			{
			   while (!selectStacks[i][j].empty())
			   {
				  p = (PreparedStatement)selectStacks[i][j].pop();
				  p.close();
			   }
			}
		 }
	  }
   }   

   /**
	* Return an array of CREATE TABLE statements.
	*
	* <p>This method returns an array of CREATE TABLE statements in the
	* form of strings. Generally, the calling application will edit these
	* strings before using them.</p>
	*
	* <p>WARNING! These strings currently use hard-coded data type names;
	* in the future, this method should retrieve data type names from the
	* database.</p>
	*
	* @return The array of CREATE TABLE statements.
	* @exception InvalidMapException The Map contained an invalid data type.
	* @exception SQLException An error occurred retrieving metadata from the
	*  database.
	*/
   public String[] getCreateTableStrings()
	  throws InvalidMapException, SQLException
   {
	  if (createStrings == null)
	  {
		 buildCreateTableStrings();
	  }
	  return createStrings;
   }   

   //**************************************************************************
   // Protected methods
   //**************************************************************************

   protected void finalize() throws Throwable
   {
	  // Idea from Richard Sullivan.
	  if (conn != null)
	  {
		 closeStatements();
	  }
   }   

   //**************************************************************************
   // Package methods
   //**************************************************************************

   void commit() throws SQLException
   {
	  // This method is here because the Map needs to know whether to close
	  // prepared statements that don't survive the commit.
	  //
	  // BUG! There are actually a couple of bugs here, but the way the
	  // package is currently written, these will never be hit:
	  //
	  // First, if any SELECT statements are checked out at the time of
	  // the commit and preparedSurviveCommit is false, the statements
	  // presumably don't survive, but will be added back to selectStacks
	  // when they are checked in. It is possible that JDBC and/or the
	  // database takes care of this and simply reprepares the statement
	  // when it is next executed. Note that we never hit this bug because
	  // DBMSToDOM, which is the only code that checks out SELECT statements,
	  // never calls commit().
	  //
	  // Second, if any result sets are open at the time of the commit,
	  // we don't check if these are automatically closed. Again, we never
	  // hit this bug because DBMSToDOM does not call commit. Note also that
	  // DBMSToDOM *requires* multiple result sets to be open if it returns
	  // data from more than one table, so calling commit in the middle of
	  // it when this closed result sets would have disastrous results.

	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  conn.commit();
	  if (!preparedSurviveCommit)
	  {
		 // If prepared statements don't survive the commit, discard them now.
		 closeStatements();
	  }
   }   

   void setAutoCommit(boolean autoCommit) throws SQLException
   {
	  // This is here only so we can fully isolate the Connection
	  // within the Map.

	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  conn.setAutoCommit(autoCommit);
   }   

   RootClassMap getRootClassMap(String rootElementName)
   {
	  return (RootClassMap)rootClassMaps.get(rootElementName);
   }   

   RootTableMap getRootTableMap(String rootTable) throws InvalidMapException
   {
	  RootTableMap rootTableMap;
	  //System.out.println("RootTable = " + rootTable);

	  rootTableMap = (RootTableMap)rootTableMaps.get(rootTable);
	  if (rootTableMap.tableMap.elementType == null)
		 throw new InvalidMapException("Table not mapped as a root table: " + rootTable);
	  return rootTableMap;
   }               

   PreparedStatement checkOutInsertStmt(Table table) throws SQLException
   {
	  PreparedStatement p;
	  int               rand;

	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // If the insert strings have not yet been built, build them now.
	  if (insertStrings == null)
	  {
		 buildInsertStrings();
	  }

	  checkMaxActiveStmts();

	  // If a prepared INSERT statement is available, use it.

	  if (!insertStacks[table.number].empty())
	  {
		 return (PreparedStatement)insertStacks[table.number].pop();
	  }

	  // Since no prepared statement is available, try to create a new one. If
	  // this fails, assumes that the reason is a limit on the number of
	  // prepared statements, close an existing (unused) statement, and try
	  // again. If this fails, or if there are no unused statements to close,
	  // throw an error.

	  try
	  {
		 return conn.prepareStatement(insertStrings[table.number]);
	  }
	  catch (SQLException se)
	  {
		 if (closeInsertStmt())
		 {
			return conn.prepareStatement(insertStrings[table.number]);
		 }
		 else if (closeSelectStmt())
		 {
			return conn.prepareStatement(insertStrings[table.number]);
		 }
		 else
		 {
			throw se;
		 }
	  }
   }   

   void checkInInsertStmt(PreparedStatement p, Table table)
   {
	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // Decrement the number of active (checked-out) statements and push
	  // the statement on the stack.
	  activeStmts --;
	  insertStacks[table.number].push(p);
   }   

   PreparedStatement checkOutSelectStmt(int tableNum, int subtableNum)
	  throws SQLException, InvalidMapException
   {
	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // If the select strings have not yet been built, build them now.
	  if (selectStrings == null)
	  {
		 buildSelectStrings();
	  }

	  checkMaxActiveStmts();

	  // If a prepared SELECT statement is available, use it.

	  if (!selectStacks[tableNum][subtableNum].empty())
	  {
		 return (PreparedStatement)selectStacks[tableNum][subtableNum].pop();
	  }

	  // Since no prepared statement is available, try to create a new one. If
	  // this fails, assumes that the reason is a limit on the number of
	  // prepared statements, close an existing (unused) statement, and try
	  // again. If this fails, or if there are no unused statements to close,
	  // throw an error.

	  try
	  {
		 return conn.prepareStatement(selectStrings[tableNum][subtableNum]);
	  }
	  catch (SQLException se)
	  {
		 if (closeSelectStmt())
		 {
			return conn.prepareStatement(selectStrings[tableNum][subtableNum]);
		 }
		 else if (closeInsertStmt())
		 {
			return conn.prepareStatement(selectStrings[tableNum][subtableNum]);
		 }
		 else
		 {
			throw se;
		 }
	  }
   }   

   PreparedStatement checkOutSelectStmt(Table t,
										Column[] whereColumns,
										Column   orderbyColumn)
	  throws SQLException
   {
	  String selectString;

	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // Check the number of currently active statements.

	  checkMaxActiveStmts();

	  // Create the SELECT string.

	  selectString = buildSelectString(t, whereColumns, orderbyColumn);

	  // Try to prepare the statement. If this fails, assumes that the reason
	  // is a limit on the number of prepared statements, close an existing
	  // (unused) statement, and try again. If this fails, or if there are no
	  // unused statements to close, throw an error.

	  try
	  {
		 return conn.prepareStatement(selectString);
	  }
	  catch (SQLException se)
	  {
		 if (closeSelectStmt())
		 {
			return conn.prepareStatement(selectString);
		 }
		 else if (closeInsertStmt())
		 {
			return conn.prepareStatement(selectString);
		 }
		 else
		 {
			throw se;
		 }
	  }
   }   

   void checkInSelectStmt(PreparedStatement p, int tableNum, int subtableNum)
	  throws SQLException
   {
	  // This function does not work as expected. In particular, the driver
	  // I am using (the JDBC-ODBC bridge with the MS Access driver) fails
	  // to correctly close the result set. Thus, when I try to reexecute a
	  // prepared SELECT statement, I get an error from the JDBC-ODBC bridge
	  // stating that I have an "Invalid state for getResultSet". It is not
	  // clear whether this is the fault of the JDBC-ODBC bridge or the MS
	  // Access driver, but it is certainly invalid behavior.
	  //
	  // If your driver correctly supports prepared statements, uncomment
	  // the line that pushes the prepared statement on the Stack and comment
	  // out the line that closes the prepared statement. You should see a
	  // significant gain in performance. To test if your driver supports
	  // valid behavior for prepared statements, run the following code:
	  //
	  //    Connection conn;
	  //    PreparedStatement p;
	  //    ResultSet rs;
	  //
	  //    Class.forName(<your driver here>);
	  //    conn = DriverManager.getConnection(<your url here>);
	  //
	  //    p = conn.prepareStatement("SELECT * from <your table name here>");
	  //    rs = p.executeQuery();
	  //    rs.close();
	  //    rs = p.executeQuery();
	  //
	  // The second call to executeQuery() should succeed for drivers supporting
	  // valid behavior and fail for drivers that don't.

	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // Decrement the number of active (checked-out) statements and push
	  // the statement on the stack OR close the prepared statement.

	  activeStmts --;
//      selectStacks[tableNum][subtableNum].push(p);
	  p.close();
   }   

   void checkInSelectStmt(PreparedStatement p) throws SQLException
   {
	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // Decrement the number of active (checked-out) statements and close
	  // the statement. Note that this method is used for one-time SELECT
	  // statements, so we don't keep them around -- we just want to make sure
	  // the count of active statements is decremented.
	  activeStmts --;
	  p.close();
   }   

   String getCreateTableString(Table table)
	  throws InvalidMapException, SQLException
   {
	  if (conn == null)
		 throw new IllegalStateException("Connection not set.");

	  // Return a CREATE TABLE string. Note that we do not return this as a
	  // PreparedStatement, since such strings will generally only be used
	  // once. Thus, the user will probably use them with their own Statement.

	  // If the create table strings have not yet been built, build them now.
	  if (createStrings == null)
	  {
		 buildCreateTableStrings();
	  }

	  return createStrings[table.number];
   }   

   //**************************************************************************
   // Private methods - build INSERT statements
   //**************************************************************************

   private void buildInsertStrings() throws SQLException
   {
	  setQuote();
	  insertStrings = new String[tables.length];
	  insertStacks = new Stack[tables.length];
	  for (int i = 0; i < tables.length; i++)
	  {
		 insertStrings[i] = buildInsertString(tables[i]);
		 insertStacks[i] = new Stack();
	  }
   }   

   private String buildInsertString(Table t) throws SQLException
   {
	  int              i;
	  StringBuffer     istr;

	  istr = new StringBuffer(1000);

	  // Create the INSERT statement.
	  // 6/9/00, Ruben Lainez, Ronald Bourret
	  // Use the identifier quote character for the table name.
	  
	  istr.append(INSERT);
	//  istr.append(quote);
	  istr.append(t.name);
	//  istr.append(quote);

	  istr.append(OPENPAREN);
	  for (i = 0; i < t.columns.length; i++)
	  {
		 addColumnName(istr, t.columns[i], (i != 0));
	  }
	  istr.append(CLOSEPAREN);
	  
	  istr.append(VALUES);
	  istr.append(PARAM);
	  for (i = 1; i < t.columns.length; i++)
	  {
		 istr.append(COMMAPARAM);
	  }
	  istr.append(CLOSEPAREN);
	  
	  return istr.toString();
   }      
   
   //**************************************************************************
   // Private methods - build CREATE TABLE statements
   //**************************************************************************

   private void buildCreateTableStrings()
	  throws InvalidMapException, SQLException
   {
	  initDataTypeNames();
	  createStrings = new String[tables.length];
	  for (int i = 0; i < tables.length; i++)
	  {
		 createStrings[i] = buildCreateTableString(tables[i]);
	  }
   }   

   private String buildCreateTableString(Table t)
	  throws InvalidMapException, SQLException
   {
	  StringBuffer cstr = new StringBuffer(1000);
	  setQuote();
	  
	  // 6/9/00, Ruben Lainez, Ronald Bourret
	  // Use the identifier quote character for the table name.
	  
	  cstr.append(CREATETABLE);
	  cstr.append(quote);
	  cstr.append(t.name);
	  cstr.append(quote);
	  cstr.append(OPENPAREN);
	  
	  // Add column definitions.
	  
	  for (int i = 0; i < t.columns.length; i++)
	  {
		 addColumnDef(cstr, t.columns[i], (i != 0));
	  }
	  
	  cstr.append(CLOSEPAREN);
	  
	  return cstr.toString();
   }   
   
   private void addColumnDef(StringBuffer cstr, Column column, boolean comma)
	  throws InvalidMapException
   {
	  addColumnName(cstr, column, comma);
	  cstr.append(SPACE);
	  cstr.append(getTypeName(column.type, column.length));
   }   

   private String getTypeName(int type, int length)
	  throws InvalidMapException
   {
	  String name = null;

	  switch (type)
	  {
		 case Types.BIGINT:
			name = bigintName;
			break;

		 case Types.BINARY:
			name = binaryName + length + CLOSEPAREN;
			break;

		 case Types.BIT:
			name = bitName;
			break;

		 case Types.CHAR:
			name = charName + length + CLOSEPAREN;
			break;

		 case Types.DATE:
			name = dateName;
			break;

		 case Types.DECIMAL:
			// BUG! This needs scale and precision parameters.
			name = decimalName;
			break;

		 case Types.DOUBLE:
			name = doubleName;
			break;

		 case Types.FLOAT:
			name = floatName;
			break;

		 case Types.INTEGER:
			name = integerName;
			break;

		 case Types.LONGVARBINARY:
			name = longvarbinaryName + length + CLOSEPAREN;
			break;

		 case Types.LONGVARCHAR:
			name = longvarcharName + length + CLOSEPAREN;
			break;

		 case Types.NUMERIC:
			// BUG! This needs scale and precision parameters.
			name = numericName;
			break;

		 case Types.REAL:
			name = realName;
			break;

		 case Types.SMALLINT:
			name = smallintName;
			break;

		 case Types.TIME:
			name = timeName;
			break;

		 case Types.TIMESTAMP:
			name = timestampName;
			break;

		 case Types.TINYINT:
			name = tinyintName;
			break;

		 case Types.VARBINARY:
			name = varbinaryName + length + CLOSEPAREN;
			break;

		 case Types.VARCHAR:
			name = varcharName + length + CLOSEPAREN;
			break;

	  }
	  if (name == null)
		 throw new InvalidMapException("Unsupported data type: " + type);

	  return name;
   }   

   private void initDataTypeNames()
   {
	  // Many (most?) of these names won't work due to differences in naming
	  // between databases. In the future, these should all be initialized to
	  // null and then as many as possible set from the result set returned by
	  // DatabaseMetaData.getTypeInfo().

	  bigintName = "BIGINT";
	  binaryName = "BINARY(";
	  bitName = "BIT";
	  charName = "CHAR(";
	  dateName = "DATE";
	  decimalName = "DECIMAL";
	  doubleName = "DOUBLE PRECISION";
	  floatName = "FLOAT";
	  integerName = "INTEGER";
	  longvarbinaryName = "LONGVARBINARY(";
	  longvarcharName = "LONGVARCHAR(";
	  numericName = "NUMERIC";
	  realName = "REAL";
	  smallintName = "SMALLINT";
	  timeName = "TIME";
	  timestampName = "TIMESTAMP";
	  tinyintName = "TINYINT";
	  varbinaryName = "VARBINARY(";
	  varcharName = "VARCHAR(";
   }   

   //**************************************************************************
   // Private methods - build SELECT statements
   //**************************************************************************

   private void buildSelectStrings() throws InvalidMapException, SQLException
   {
	  selectStrings = new String[tableMaps.length][];
	  selectStacks = new Stack[tableMaps.length][];

	  for (int i = 0; i < tableMaps.length; i++)
	  {
		 selectStrings[i] = new String[tableMaps[i].relatedTables.length];
		 selectStacks[i] = new Stack[tableMaps[i].relatedTables.length];
		 for (int j = 0; j < tableMaps[i].relatedTables.length; j++)
		 {
			selectStrings[i][j] = buildSelectString(tableMaps[i], j);
			selectStacks[i][j] = new Stack();
		 }
	  }
   }   

   private String buildSelectString(TableMap tm, int relatedTable)
	  throws InvalidMapException, SQLException
   {
	  if ((tm.orderColumns[relatedTable] != null) &&
		  (!tm.parentKeyIsCandidate[relatedTable]))
		 throw new InvalidMapException("BUG! DBMS => XML data transfer not supported when: a) the candidate key in the relationship linking two element types is stored in the table of the child element type, and b) order information about the child element type is stored in the database.");

	  // BUG!!! The order column stuff doesn't work when the parent key is
	  // a foreign key.  In fact, the entire Row object falls apart.  The
	  // problem is that in this case, the order column is in the parent table,
	  // which thus needs to be joined to the child table, which means that the
	  // result set is no longer shaped like a single table -- the assumption
	  // on which Row (and probably a lot of other code) is built.

	  return buildSelectString(tm.relatedTables[relatedTable].table, tm.childKeys[relatedTable], null);
   }   

   private String buildSelectString(Table t,
									Column[] whereColumns,
									Column   orderbyColumn)
	  throws SQLException
   {
	  StringBuffer     sstr;

	  sstr = new StringBuffer(1000);
	  setQuote();

	  sstr.append(SELECT);
	  
	  // Add column names.

	  for (int i = 0; i < t.columns.length; i++)
	  {
		 addColumnName(sstr, t.columns[i], (i != 0));
	  }

	  // Add table name.
	  // 6/9/00, Ruben Lainez, Ronald Bourret
	  // Use the identifier quote character for the table name.
	  
	  sstr.append(FROM);
	 // sstr.append(quote);
	  sstr.append(t.name);
	 // sstr.append(quote);
	  
	  // Add WHERE clause.

	  if (whereColumns != null)
	  {
		 sstr.append(WHERE);
		 
		 for (int i = 0; i < whereColumns.length; i++)
		 {
			addColumnRestriction(sstr, whereColumns[i], (i != 0));
		 }
	  }

	  // Add ORDER BY clause. We sort in descending order because this
	  // gives us better performance in some cases. For more details,
	  // see DBMSToDOM.Order.insertChild, which really ought to be
	  // rewritten to use a binary search.
	  
	  if (orderbyColumn != null)
	  {
		 sstr.append(ORDERBY);
		 addColumnName(sstr, orderbyColumn, false);
		 sstr.append(DESC);
	  }
	  
	  return sstr.toString();
   }      
   
   private void addColumnName(StringBuffer str, Column column, boolean comma)
   {
	  if (comma)
	  {
		 str.append(COMMA);
	  }
	  str.append(quote);
	  str.append(column.name);
	  str.append(quote);
   }   

   private void addColumnRestriction(StringBuffer sstr, Column column, boolean and)
   {
	  if (and)
	  {
		 sstr.append(AND);
	  }
	  addColumnName(sstr, column, false);
	  sstr.append(EQUALSPARAM);
   }   

   void setQuote() throws SQLException
   {
	  if (quote == null)
	  {
		 quote = conn.getMetaData().getIdentifierQuoteString();
	  }
   }   

   //**************************************************************************
   // Private methods - serialize map
   //**************************************************************************

   // Serialization constants

   // 5/24/00 Phil Friedman, Ronald Bourret
   // 1) Initialize the following byte array constant from a String, not a char array,
   //    which requires an explicit cast to byte.
   // 2) Declare the byte constant to be final.

   static String XMLDBMSDTDStr = "xmldbms.dtd";
   static byte[] XMLDBMSDTD = XMLDBMSDTDStr.getBytes();

   // Serialization methods (in alphabetical order)

   private void writeAttribute(NSName name)
	  throws IOException
   {
	  initAttrs();
	  attrs[0] = XMLDBMSConst.ATTR_NAME.getBytes();
	  values[0] = name.prefixed.getBytes();
	  writeElementStart(XMLDBMSConst.ELEM_ATTRIBUTE.getBytes(), attrs, values, true);
   }   

   private void writeClassMap(ClassMap classMap)
	  throws IOException
   {
	  // Start the ClassMap element.
	  writeElementStart(XMLDBMSConst.ELEM_CLASSMAP.getBytes(), null, null, false);

	  // Write the ElementType element.
	  writeElementType(classMap.name);

	  // Write the ToRootTable or ToClassTable element.
	  if (classMap.type == ClassMap.TYPE_TOROOTTABLE)
	  {
		 writeToRootTable(classMap);
	  }
	  else // if (classMap.type == ClassMap.TYPE_TOCLASSTABLE)
	  {
		 writeToClassTable(classMap.table);
	  }

	  // Write the PropertyMap elements.
	  writePropertyMaps(classMap);

	  // Write the RelatedClass elements.
	  writeRelatedClassMaps(XMLDBMSConst.ELEM_RELATEDCLASS.getBytes(), classMap.subElementTypeMaps);

	  // Write the PassThrough elements.
	  // (NOT SUPPORTED.)

	  // End the ClassMap element.
	  writeElementEnd(XMLDBMSConst.ELEM_CLASSMAP.getBytes());
   }   

   private void writeClassMaps()
	  throws IOException
   {
	  Enumeration e;
	  ClassMap    classMap;

	  e = classMaps.elements();
	  while (e.hasMoreElements())
	  {
		 classMap = (ClassMap)e.nextElement();
		 if ((classMap.type == ClassMap.TYPE_TOCLASSTABLE) ||
			 (classMap.type == ClassMap.TYPE_TOROOTTABLE))
		 {
			writeClassMap(classMap);
		 }
	  }
   }   

   private void writeColumn(Column column)
	  throws IOException
   {
	  initAttrs();
	  attrs[0] = XMLDBMSConst.ATTR_NAME.getBytes();
	  values[0] = column.name.getBytes();
	  writeElementStart(XMLDBMSConst.ELEM_COLUMN.getBytes(), attrs, values, true);
   }   

   private void writeDateTimeFormat(DateFormat date, DateFormat time, DateFormat timestamp)
	  throws IOException
   {
	  int count = 0;

	  initAttrs();
	  if (date instanceof SimpleDateFormat)
	  {
		 attrs[count] = XMLDBMSConst.ATTR_DATE.getBytes();
		 values[count] = ((SimpleDateFormat)date).toPattern().getBytes();
		 count++;
	  }

	  if (time instanceof SimpleDateFormat)
	  {
		 attrs[count] = XMLDBMSConst.ATTR_TIME.getBytes();
		 values[count] = ((SimpleDateFormat)time).toPattern().getBytes();
		 count++;
	  }

	  if (timestamp instanceof SimpleDateFormat)
	  {
		 attrs[count] = XMLDBMSConst.ATTR_TIMESTAMP.getBytes();
		 values[count] = ((SimpleDateFormat)timestamp).toPattern().getBytes();
	  }

	  writeElementStart(XMLDBMSConst.ELEM_PATTERNS.getBytes(), attrs, values, true);
   }   

   private void writeDateTimeFormats()
	  throws IOException
   {
	  // Bug! We can't really return all the information we need here to
	  // reconstruct the formatters. The best we can do is see if they
	  // are SimpleDateFormats and, if so, return the pattern. In the
	  // future, we might want to retain this information from the mapping
	  // document, but right now, I don't think it's worth it.

	  if (!((dateFormatter instanceof SimpleDateFormat) ||
			(timeFormatter instanceof SimpleDateFormat) ||
			(timestampFormatter instanceof SimpleDateFormat))) return;

	  writeElementStart(XMLDBMSConst.ELEM_DATETIMEFORMATS.getBytes(), null, null, false);
	  writeDateTimeFormat(dateFormatter, timeFormatter, timestampFormatter);
	  writeElementEnd(XMLDBMSConst.ELEM_DATETIMEFORMATS.getBytes());
   }   

   private void writeElementType(NSName name)
	  throws IOException
   {
	  initAttrs();
	  attrs[0] = XMLDBMSConst.ATTR_NAME.getBytes();
	  values[0] = name.prefixed.getBytes();
	  writeElementStart(XMLDBMSConst.ELEM_ELEMENTTYPE.getBytes(), attrs, values, true);
   }   

   private void writeEmptyStringIsNull()
	  throws IOException
   {
	  if (emptyStringIsNull)
	  {
		 writeElementStart(XMLDBMSConst.ELEM_EMPTYSTRINGISNULL.getBytes(), null, null, true);
	  }
   }   

   private void writeIgnoreRoot(RootClassMap rootClassMap)
	  throws IOException
   {
	  writeElementStart(XMLDBMSConst.ELEM_IGNOREROOT.getBytes(), null, null, false);
	  writeElementType(rootClassMap.classMap.name);
	  writeRelatedClassMaps(XMLDBMSConst.ELEM_PSEUDOROOT.getBytes(), rootClassMap.classMap.subElementTypeMaps);
	  writeElementEnd(XMLDBMSConst.ELEM_IGNOREROOT.getBytes());
   }   

   private void writeIgnoreRoots()
	  throws IOException
   {
	  Enumeration  e;
	  RootClassMap rootClassMap;

	  e = rootClassMaps.elements();
	  while (e.hasMoreElements())
	  {
		 rootClassMap = (RootClassMap)e.nextElement();
		 if (rootClassMap.classMap.type != ClassMap.TYPE_IGNOREROOT) continue;
		 writeIgnoreRoot(rootClassMap);
	  }
   }   

   private void writeKey(LinkInfo linkInfo, boolean candidate)
	  throws IOException
   {
	  byte[]   elementTypeName;
	  Column[] key = null;

	  initAttrs();

	  // Get the correct key from the LinkInfo.

	  if (candidate)
	  {
		 key = (linkInfo.parentKeyIsCandidate) ? linkInfo.parentKey : linkInfo.childKey;
	  }
	  else
	  {
		 key = (linkInfo.parentKeyIsCandidate) ? linkInfo.childKey : linkInfo.parentKey;
	  }

	  // If the key is null, don't write it out. (Null keys are legal
	  // in a number of places, such as RootClassMaps.)

	  if (key == null) return;

	  // For candidate keys, set the Generate attribute.

	  if (candidate)
	  {
		 attrs[0] = XMLDBMSConst.ATTR_GENERATE.getBytes();
		 values[0] = linkInfo.generateKey ? XMLDBMSConst.ENUM_YES.getBytes() : XMLDBMSConst.ENUM_NO.getBytes();
	  }

	  // Start the CandidateKey or ForeignKey element.
	  elementTypeName = (candidate) ? XMLDBMSConst.ELEM_CANDIDATEKEY.getBytes() : XMLDBMSConst.ELEM_FOREIGNKEY.getBytes();
	  writeElementStart(elementTypeName, attrs, values, false);

	  // Write the Column elements.
	  for (int i = 0; i < key.length; i++)
	  {
		 writeColumn(key[i]);
	  }

	  // End the CandidateKey or ForeignKey element.
	  writeElementEnd(elementTypeName);
   }   

   private void writeMapEnd()
	  throws IOException
   {
	  writeElementEnd(XMLDBMSConst.ELEM_XMLTODBMS.getBytes());
   }   

   private void writeMaps()
	  throws IOException
   {
	  writeElementStart(XMLDBMSConst.ELEM_MAPS.getBytes(), null, null, false);
	  writeIgnoreRoots();
	  writeClassMaps();
	  writeElementEnd(XMLDBMSConst.ELEM_MAPS.getBytes());
   }   

   private void writeMapStart()
	  throws IOException
   {
	  initAttrs();
	  attrs[0] = XMLDBMSConst.ATTR_VERSION.getBytes();
	  values[0] = XMLDBMSConst.DEF_VERSION.getBytes();

	  writeXMLDecl(null);
	  writeDOCTYPE(XMLDBMSConst.ELEM_XMLTODBMS.getBytes(), XMLDBMSDTD, null);
	  writeElementStart(XMLDBMSConst.ELEM_XMLTODBMS.getBytes(), attrs, values, false);
   }   

   private void writeNamespaces()
	  throws IOException
   {
	  initAttrs();
	  if (prefixes == null) return;
	  for (int i = 0; i < prefixes.length; i++)
	  {
		 attrs[0] = XMLDBMSConst.ATTR_PREFIX.getBytes();
		 attrs[1] = XMLDBMSConst.ATTR_URI.getBytes();
		 values[0] = prefixes[i].getBytes();
		 values[1] = uris[i].getBytes();
		 writeElementStart(XMLDBMSConst.ELEM_NAMESPACE.getBytes(), attrs, values, true);
	  }
   }   

   private void writeOptions()
	  throws IOException
   {
	  writeElementStart(XMLDBMSConst.ELEM_OPTIONS.getBytes(), null, null, false);
	  writeEmptyStringIsNull();
	  writeDateTimeFormats();
	  writeNamespaces();
	  writeElementEnd(XMLDBMSConst.ELEM_OPTIONS.getBytes());
   }   

   private void writeOrderColumn(OrderInfo orderInfo)
	  throws IOException
   {
	  initAttrs();

	  if (orderInfo.orderColumn == null) return;

	  attrs[0] = XMLDBMSConst.ATTR_NAME.getBytes();
	  attrs[1] = XMLDBMSConst.ATTR_GENERATE.getBytes();
	  values[0] = orderInfo.orderColumn.name.getBytes();
	  values[1] = (orderInfo.generateOrder) ? XMLDBMSConst.ENUM_YES.getBytes() : XMLDBMSConst.ENUM_NO.getBytes();
	  writeElementStart(XMLDBMSConst.ELEM_ORDERCOLUMN.getBytes(), attrs, values, true);
   }   

   private void writePropertyMap(PropertyMap propMap, int type)
	  throws IOException
   {
	  writeElementStart(XMLDBMSConst.ELEM_PROPERTYMAP.getBytes(), null, null, false);

	  switch (type)
	  {
		 case ColumnMap.TYPE_TOATTRIBUTE:
			writeAttribute(propMap.name);
			break;

		 case ColumnMap.TYPE_TOPCDATA:
			writeElementStart(XMLDBMSConst.ELEM_PCDATA.getBytes(), null, null, true);
			break;

		 case ColumnMap.TYPE_TOELEMENTTYPE:
			writeElementType(propMap.name);
			break;
	  }

	  if (propMap.type == PropertyMap.TYPE_TOCOLUMN)
	  {
		 writeToColumn(propMap.column);
	  }
	  else // if (propMap.type == PropertyMap.TYPE_TOPROPERTYTABLE)
	  {
		 writeToPropertyTable(propMap);
	  }

	  writeOrderColumn(propMap.orderInfo);

	  writeElementEnd(XMLDBMSConst.ELEM_PROPERTYMAP.getBytes());
   }   

   private void writePropertyMaps(ClassMap classMap)
	  throws IOException
   {
	  Enumeration e;
	  PropertyMap propMap;
	  Object      temp;

	  // Write the PropertyMaps for attributes (if any).
	  e = classMap.attributeMaps.elements();
	  while (e.hasMoreElements())
	  {
		 propMap = (PropertyMap)e.nextElement();
		 writePropertyMap(propMap, ColumnMap.TYPE_TOATTRIBUTE);
	  }

	  // Write the PropertyMap for PCDATA (if any).
	  if (classMap.pcdataMap != null)
	  {
		 writePropertyMap(classMap.pcdataMap, ColumnMap.TYPE_TOPCDATA);
	  }

	  // Write the PropertyMaps for element types-as-properties (if any).
	  e = classMap.subElementTypeMaps.elements();
	  while (e.hasMoreElements())
	  {
		 temp = e.nextElement();
		 if (temp instanceof PropertyMap)
		 {
			propMap = (PropertyMap)temp;
			writePropertyMap(propMap, ColumnMap.TYPE_TOELEMENTTYPE);
		 }
	  }
   }   

   private void writeRelatedClassMap(byte[] elementTypeName, RelatedClassMap relatedClassMap)
	  throws IOException
   {
	  Column[] key;

	  // Note that we use the same code to write both PseudoRoot elements and
	  // RelatedClass elements. Thus, we need to pass in the actual element type
	  // name and check to see whether we need the KeyInParentTable attribute
	  // on the element and whether a parent key even exists.

	  initAttrs();

	  // Start the PseudoRoot or RelatedClass element. Note that the
	  // RelatedClass element has an attribute (KeyInParentTable).

	  if (relatedClassMap.linkInfo.parentKey != null)
	  {
		 attrs[0] = XMLDBMSConst.ATTR_KEYINPARENTTABLE.getBytes();
		 values[0] = (relatedClassMap.linkInfo.parentKeyIsCandidate) ?
					 XMLDBMSConst.ENUM_CANDIDATE.getBytes() : XMLDBMSConst.ENUM_FOREIGN.getBytes();
	  }
	  writeElementStart(elementTypeName, attrs, values, false);

	  // Write the ElementType element.

	  writeElementType(relatedClassMap.classMap.name);

	  // Write the CandidateKey element (if any). This is optional on the
	  // PseudoRoot element.

	  writeKey(relatedClassMap.linkInfo, true);

	  // Write the ForeignKey element (if any). This is optional on the
	  // PseudoRoot element.

	  writeKey(relatedClassMap.linkInfo, false);

	  // Write the OrderColumn element (if any). This is optional on both the
	  // PseudoRoot and RelatedClass elements.

	  writeOrderColumn(relatedClassMap.orderInfo);

	  // End the PseudoRoot or RelatedClass element.
	  writeElementEnd(elementTypeName);
   }   

   private void writeRelatedClassMaps(byte[] elementTypeName, Hashtable subElementTypeMaps)
	  throws IOException
   {
	  Enumeration     e;
	  RelatedClassMap relatedClassMap;
	  Object          temp;

	  e = subElementTypeMaps.elements();
	  while (e.hasMoreElements())
	  {
		 temp = e.nextElement();
		 if (temp instanceof RelatedClassMap)
		 {
			relatedClassMap = (RelatedClassMap)temp;
			writeRelatedClassMap(elementTypeName, relatedClassMap);
		 }
	  }
   }   

   private void writeTable(Table table)
	  throws IOException
   {
	  initAttrs();
	  attrs[0] = XMLDBMSConst.ATTR_NAME.getBytes();
	  values[0] = table.name.getBytes();
	  writeElementStart(XMLDBMSConst.ELEM_TABLE.getBytes(), attrs, values, true);
   }   

   private void writeToClassTable(Table table)
	  throws IOException
   {
	  writeElementStart(XMLDBMSConst.ELEM_TOCLASSTABLE.getBytes(), null, null, false);
	  writeTable(table);
	  writeElementEnd(XMLDBMSConst.ELEM_TOCLASSTABLE.getBytes());
   }   

   private void writeToColumn(Column column)
	  throws IOException
   {
	  writeElementStart(XMLDBMSConst.ELEM_TOCOLUMN.getBytes(), null, null, false);
	  writeColumn(column);
	  writeElementEnd(XMLDBMSConst.ELEM_TOCOLUMN.getBytes());
   }   

   private void writeToPropertyTable(PropertyMap propMap)
	  throws IOException
   {
	  initAttrs();
	  attrs[0] = XMLDBMSConst.ATTR_KEYINPARENTTABLE.getBytes();
	  values[0] = (propMap.linkInfo.parentKeyIsCandidate) ?
				  XMLDBMSConst.ENUM_CANDIDATE.getBytes() : XMLDBMSConst.ENUM_FOREIGN.getBytes();
	  writeElementStart(XMLDBMSConst.ELEM_TOPROPERTYTABLE.getBytes(), attrs, values, false);
	  writeTable(propMap.table);
	  writeKey(propMap.linkInfo, true);
	  writeKey(propMap.linkInfo, false);
	  writeColumn(propMap.column);
	  writeElementEnd(XMLDBMSConst.ELEM_TOPROPERTYTABLE.getBytes());
	  
   }   

   private void writeToRootTable(ClassMap classMap)
	  throws IOException
   {
	  RootClassMap rootClassMap;

	  // Get the RootClassMap for the class.
	  rootClassMap = getRootClassMap(classMap.name.qualified);

	  // Start the ToRootTable element.
	  writeElementStart(XMLDBMSConst.ELEM_TOROOTTABLE.getBytes(), null, null, false);

	  // Write the Table element.
	  writeTable(classMap.table);

	  // Write the CandidateKey element (if any).

	  writeKey(rootClassMap.linkInfo, true);

	  // Write the OrderColumn element (if any).

	  writeOrderColumn(rootClassMap.orderInfo);

	  // End the ToRootTable element.
	  writeElementEnd(XMLDBMSConst.ELEM_TOROOTTABLE.getBytes());
   }   

   //**************************************************************************
   // Private methods - check-in / check-out utilities
   //**************************************************************************

   private int getRandomInt(int max)
   {
	  // Returns 0-based number less than or equal to max.
	  int r;

	  r = rnd.nextInt();
	  if (r < 0) r *= -1;
	  r = r % (max + 1);
	  return r;
   }   

   private void checkMaxActiveStmts() throws SQLException
   {
	  // Check if the number of active statements is already maxed out. If not,
	  // increment the number of currently active statements. Note that any
	  // checked out statement is assumed to be active.

	  if (activeStmts == maxActiveStmts)
		 throw new SQLException("Maximum number of active statements exceeded.");
	  activeStmts++;
   }   

   private boolean closeSelectStmt() throws SQLException
   {
	  PreparedStatement p;
	  int               rand, index;

	  if (selectStrings == null) return false;

	  // To decide what prepared statement to close, we simply
	  // start at a random place in the array and delete the
	  // first statement we encounter. This is less sophisticated
	  // than keeping usage statistics and deleting the least-used
	  // statement, but much easier to implement.

	  rand = getRandomInt(selectStrings.length);
	  for (int i = 0; i < selectStrings.length; i++)
	  {
		 index = (i + rand) % rand;
		 for (int j = 0; j < selectStrings[i].length; j++)
		 {
			if (!selectStacks[index][j].empty())
			{
			   p = (PreparedStatement)selectStacks[index][j].pop();
			   p.close();
			   return true;
			}
		 }
	  }
	  return false;
   }   

   private boolean closeInsertStmt() throws SQLException
   {
	  PreparedStatement p;
	  int               rand;

	  if (insertStrings == null) return false;

	  // To decide what prepared statement to close, we simply
	  // start at a random place in the array and delete the
	  // first statement we encounter. This is less sophisticated
	  // than keeping usage statistics and deleting the least-used
	  // statement, but much easier to implement.

	  rand = getRandomInt(insertStrings.length);
	  for (int i = 0; i < insertStrings.length; i++)
	  {
		 if (!insertStacks[(i + rand) % rand].empty())
		 {
			p = (PreparedStatement)insertStacks[(i + rand) % rand].pop();
			p.close();
			return true;
		 }
	  }
	  return false;
   }   

   //**************************************************************************
   // Private methods - initialization
   //**************************************************************************

   private void getDatabaseMetadata()
	  throws SQLException
   {
	  DatabaseMetaData meta;

	  meta = conn.getMetaData();

	  // Get the maximum number of active statements. If this is 0, which means
	  // that the number is unbounded or unknown, set it to the maximum value
	  // of an int.

	  maxActiveStmts = meta.getMaxStatements();
	  if (maxActiveStmts == 0)
	  {
		 maxActiveStmts = Integer.MAX_VALUE;
	  }

	  preparedSurviveCommit = meta.supportsOpenStatementsAcrossCommit();
   }   

   private void getTableMetadata()
	  throws SQLException, InvalidMapException
   {
	  DatabaseMetaData meta;

	  meta = conn.getMetaData();

	  for (int i = 0; i < tables.length; i++)
	  {
		 // Check if the table really represents a result set. If so, get
		 // the column metadata directly from the result set. If not, get the
		 // column metadata from the DatabaseMetaData object.

		 if (tables[i].name.equals(RESULTSET))
		 {
			addResultSetMetadata(tables[i]);
		 }
		 else
		 {
			addColumnMetadata(meta, tables[i]);
		 }
	  }

   }   

   private void addResultSetMetadata(Table table)
	  throws SQLException, InvalidMapException
   {
	  ResultSetMetaData meta;
	  Column            column;
	  int               rowObjectIndex = -1;

	  if (mappedResultSet == null)
		 throw new InvalidMapException("You must provide a result set when mapping an element type-as-class to a result set.");

	  meta = mappedResultSet.getMetaData();

	  for (int i = 1; i <= meta.getColumnCount(); i++)
	  {
		 // Get the name of the next column in the result set. If the column
		 // isn't mapped -- that is, it is not in table -- then ignore it.

		 try
		 {
			column = table.getColumn(meta.getColumnName(i));
		 }
		 catch (InvalidMapException ime)
		 {
			continue;
		 }

		 // Set the type and length.

		 column.type = meta.getColumnType(i);
		 fixDateTimeType(column);
		 column.length = meta.getColumnDisplaySize(i);

		 // 5/29/00, Roland Stengel, Ronald Bourret
		 // Increment the index of the Row.columnValues array and:
		 //
		 // a) Store the column number for this index in Table.rsColumnNumbers.
		 //    This is used when populating the Row in DBMSToDOM.populateRow.
		 //    Note that the order in which result set column numbers are
		 //    stored means that columns will be accessed in ascending order.
		 //
		 // b) Store the index in Column.rowObjectIndex. This is used to
		 //    get and set data in the Row object.

		 rowObjectIndex++;
		 table.rsColumnNumbers[rowObjectIndex] = i;
		 column.rowObjectIndex = rowObjectIndex;
	  }

	  // Check that the type variable was set for all columns.

	  checkColumnTypesSet(table);
   }   

   private void addColumnMetadata(DatabaseMetaData meta, Table table)
	  throws SQLException, InvalidMapException
   {
	  String     catalog = null,
				 schema = null,
				 tableName = table.name;
				 //System.out.println("TABLE= "+table.name);
	  
	  ResultSet  rs;
	  Column     column;
	  boolean    tableFound = false;
	  String     columnName,catsep;

	  // Get the catalog, schema, and table names. Note that we check first to
	  // see if catalogs and schemas are even supported.


	  catsep = meta.getCatalogSeparator();
	  
		if (catsep == null)
		{catsep = ".";}
		
		if (catsep.equals(""))
		{catsep = ".";}
		
	  
		  StringTokenizer st = new StringTokenizer(table.name,catsep);
		  int i;
		  i = st.countTokens();
		  //System.out.println("Number of tokens = " +i);
		  if (i==2 && meta.getSchemaTerm().length() != 0)
		  //if 2 assume schema & table but check to see if db supports schema's
		  { schema = st.nextToken();
			  tableName = st.nextToken();
		  }
		  if (i==3 && meta.getSchemaTerm().length() != 0 && meta.getCatalogTerm().length() != 0)
		  //if 3 assume catalog & schema & table but check to see if db supports schema's & catalogs
		  // & wether the catalog is @ the front or the back
		  { if (meta.isCatalogAtStart())
			  {
			  catalog = st.nextToken();
			  schema = st.nextToken();
			  tableName = st.nextToken();
			  }
			  else
			  {
			  schema = st.nextToken();
			  tableName = st.nextToken();
			  catalog = st.nextToken();
			  }
		  }

//		  	   System.out.println("Catalog = "+catalog);	
//			   System.out.println("Schema = "+schema);
//			   System.out.println("tableName = "+tableName);			
		 
	  

	  // Get the column metadata result set and process it. Column 4 is column
	  // name, column 5 is data type, and column 7 is length in characters.


	  rs = meta.getColumns(catalog, schema, tableName, null);


	  while (rs.next())
	  {
		 tableFound = true;

		 // Get the next row of metadata and get the column name. If the column
		 // isn't mapped, continue to the following row.

		 try
		 {
			column = table.getColumn(rs.getString(4));


		 }
		 catch (InvalidMapException ime)
		 {
			continue;
		 }

		 // Set the type and length.



		 column.type = (int)rs.getShort(5);
		 fixDateTimeType(column);
		 column.length = rs.getInt(7);
	  }

	  // Close the result set.
	  rs.close();

	  // If the table was not found, throw an error.

	  // 5/19/00, Ronald Bourret
	  // Added comments about checking case to error message.
	  //
	  // A common problem is that users use a different case in the map document
	  // than is used to store an identifier in the database. This is because
	  // databases commonly case-fold unquoted identifiers in CREATE TABLE
	  // statements before storing them. For example, the identifier Foo in
	  // "CREATE TABLE Foo ..." might be stored as FOO. If the user uses Foo in
	  // the map document, it is not found because the database uses FOO.
	  //
	  // Unfortunately, there is no easy technical solution to this problem, in
	  // spite of the fact that JDBC provides information about how identifiers
	  // are stored in the database. The problem is that the map document does
	  // not support quoted identifiers. Thus, Foo could refer to the unquoted
	  // identifier Foo (which might need to be case-folded to FOO before
	  // comparison) or the quoted identifier "Foo" (which might not need to be
	  // case-folded).
	  //
	  // Although we could support quoted identifiers in the map document,
	  // (a) this is not backwards compatible, and (b) this is more complex than
	  // simply requiring users to use the exact case.

	  if (!tableFound)
		 throw new InvalidMapException("Table not found: " + table.name +
			". Check that the table exists, that its name is spelled correctly, " +
			"and that the case used in the map document exactly matches the " +
			"case used in the database. This might be different from the case " +
			"you used when creating the table.");

	  // Check that the type variable was set for all columns.

	  checkColumnTypesSet(table);
   }                                                                           

   private void fixDateTimeType(Column column)
   {
	  // Check the column type. Note that the numbers for the date/time data
	  // types changed between ODBC 2.0 and ODBC 3.0 and that JDBC uses the
	  // 3.0 numbers. Thus, if we get the 2.0 numbers (the ODBC Driver Manager
	  // or the ODBC-JDBC bridge hasn't converted them for us), we need to
	  // convert them ourselves.

	  switch (column.type)
	  {
		 case 9:
			column.type = Types.DATE;
			break;

		 case 10:
			column.type = Types.TIME;
			break;

		 case 11:
			column.type = Types.TIMESTAMP;
			break;

		 default:
			break;
	  }
   }   

   private void checkColumnTypesSet(Table table) throws InvalidMapException
   {
	  // 5/19/00, Ronald Bourret
	  // Added comments about checking case to error message. See discussion
	  // in addColumnMetaData.

	  for (int i = 0; i < table.columns.length; i++)
	  {
		 if (table.columns[i].type == Types.NULL)
			throw new InvalidMapException("Column " + table.columns[i].name +
			   " not found in table " + table.name + ". Check that the column " +
			   "exists, that its name is spelled correctly, and that the case " +
			   "used in the map document exactly matches the case used in the " +
			   "database. This might be different from the case you used when " +
			   "creating the column.");
	  }
   }   
}