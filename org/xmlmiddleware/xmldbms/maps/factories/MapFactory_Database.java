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
// Changes from version 1.1: New in version 2.0

package org.xmlmiddleware.xmldbms.maps.factories;

import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;
import org.xmlmiddleware.xmlutils.*;

import java.sql.*;
import java.util.*;

/**
 * Create an XMLDBMSMap from a database.
 *
 * <p>Through primary key / foreign key references, the tables in a database
 * form a graph. MapFactory_Database follows this graph and creates an XMLDBMSMap. Tables
 * are mapped to elements with complex types and columns may be mapped either to
 * attributes or to elements containing only PCDATA. The calling application
 * specifies which tables to include in the map in any of a number of ways.</p>
 *
 * <p>For example, the following code creates a map starting with the SalesOrders table.</p>
 *
 * <pre>
 *    // Instantiate a new map factory and set the JDBC connection.
 *    factory = new MapFactory_Database(conn);<br />
 *
 *    // Create an XMLDBMSMap based on the graph including the SalesOrders table.
 *    map = factory.createMap("SalesOrders");<br />
 * </pre>
 *
 * <p>It is important to understand that an XMLDBMSMap is a directed graph. This is different
 * from the tables in a database, which form an undirected graph. In particular, a primary
 * key / foreign key link can be traversed in either direction; the hierarchy of an XML
 * document forces you to choose one of those directions. That is, you can choose to
 * traverse from primary key to foreign key (in which case, the primary key table is the
 * parent) or from foreign key to primary key (in which case the foreign key table is
 * the parent).</p>
 *
 * <p>Which direction the XMLDBMSMap constructed by this class uses depends on the tables you
 * choose as root table. For example, suppose we have the following graph of tables:</p>
 *
 * <pre>
 *             SalesOrders
 *             /         \
 *          Lines     Customers
 *            |
 *          Parts
 * </pre>
 *
 * <p>If you choose SalesOrders as the root table, then your XML documents will look
 * like this:</p>
 *
 * <pre>
 *    &lt;SalesOrder&gt;
 *       &lt;Customer>
 *          ...
 *       &lt;/Customer>
 *       &lt;Line>
 *          ...
 *          &lt;Part>
 *             ...
 *          &lt;/Part>
 *       &lt;/Line>
 *       ...
 *    &lt;/SalesOrder>
 * </pre>
 *
 * <p>On the other hand, if you choose Customers as the root table, your XML documents
 * will look like this:</p>
 * 
 * <pre>
 *    &lt;Customer&gt;
 *       &lt;SalesOrder>
 *          &lt;Line>
 *             ...
 *             &lt;Part>
 *                ...
 *             &lt;/Part>
 *          &lt;/Line>
 *          ...
 *       &lt;/SalesOrder>
 *       ...
 *    &lt;/Customer>
 * </pre>
 *
 * <p>You have several different choices on how to construct the XMLDBMSMap. You can specify
 * whether to follow primary key links (that is, links where the primary key is in the
 * current table) and whether to following foreign key links (that is, links where the
 * foreign key is in the current table). For example, following only primary key links
 * in the first example above would have resulted in only the SalesOrders and Lines tables
 * being included. Following only foreign key links would have resulted in only the
 * SalesOrders and Customers tables being included.</p>
 *
 * <p>You can also specify multiple root tables. This is useful when a graph has multiple
 * leaf tables that you want to use as roots, but must be used carefully. In particular,
 * if you start at one root and the graph leads back to another root (which is usually the
 * case), the second root won't be processed. This can be avoided only by not following
 * primary key links or foreign key links. For example, suppose you have the following
 * graph, in which both the Students and Teachers tables both have primary keys:</p>
 *
 * <pre>
 *     Students     Teachers
 *             \   /
 *            Courses
 * </pre>
 *
 * <p>If you follow both primary and foreign key links and input both Teachers and Students
 * as root tables (in that order), then the map can only generate the following XML document,
 * since the Student table is processed by following the links from Teachers to Courses to
 * Students:</p>
 *
 * <pre>
 *    &lt;Teacher>
 *       &lt;Course>
 *          &lt;Student>
 *             ...
 *          &lt;/Student>
 *          ...
 *       &lt;/Course>
 *       ...
 *   &lt;/Teacher>
 * </pre>
 *
 * <p>On the other hand, if you only follow primary key links, then the factory does not
 * follow the link from Courses back to Students and you can create either of the following
 * XML documents:</p>
 *
 * <pre>
 *    &lt;Teacher>         &lt;Student>
 *       &lt;Course>          &lt;Course>
 *          ...               ...
 *       &lt;/Course>         &lt;/Course>
 *       ...               ...
 *   &lt;/Teacher>         &lt;/Student>
 * </pre>
 *
 * <p>The last option is that you can "trim" a graph by specifying "stop" tables. These are
 * tables that are not included in the XMLDBMSMap. For example, in the first example above, if you
 * only wanted to create an XML document with SalesOrders, Lines, and Parts, you could
 * specify Customers as a stop table.</p>
 *
 * <p>A word of final warning. No matter what options you choose, you should expect to
 * modify your map by hand after it is generated. It is very easy to find examples of
 * useful maps that cannot be generated from these criteria, and adding new criteria is not a
 * high priority. In such cases, the best thing to do is to generate a map with too many
 * tables, then remove and rearrange ClassMap and RelatedClass elements to get the map
 * you want.</p>
 * 
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class MapFactory_Database
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   // General class variables
   private Connection[] connections = null;
   private String[]     databaseNames = null,
                        namespaceURIs = null;
   private boolean      useElementTypes = true,
                        followPrimaryKeys = true, followForeignKeys = true;
   private XMLDBMSMap   map = null;
   private Hashtable    processedTables = new Hashtable(),
                        elementTypeNames = new Hashtable(),
                        conns = new Hashtable(),
                        metas = new Hashtable(),
                        uris = new Hashtable(),
                        attributeHashes = new Hashtable();

   // Static variables
   static private final Object    o = new Object();
   static private final Hashtable emptyHashtable = new Hashtable();

   // Constants
   private static final String DEFAULT = "Default";
   private static final String PERIOD = ".";
   private static final String CONNECTIONS = "connections";
   private static final String DATABASENAME = "database name";
   private static final String CATALOGNAME = "catalog name";
   private static final String SCHEMANAME = "schema name";
   private static final String TABLENAME = "table name";
   private static final String ARRAY = "s array";
   private static final String STOP = "Stop ";
   private static final String ROOT = "root ";

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new MapFactory_Database.
    */
   public MapFactory_Database()
   {
   }

   /**
    * Construct a new MapFactory_Database and set the connection.
    *
    * <p>The database name is set to "Default".</p>
    *
    * @param conn The database connection.
    */
   public MapFactory_Database(Connection conn)
   {
      connections = new Connection[1];
      connections[0] = conn;
      databaseNames = new String[1];
   }

   /**
    * Construct a new MapFactory_Database and set the connections, database names,
    * and namespace URIs.
    *
    * @param databaseNames An array of names for the databases in connections and URIs
    *    in namespaceURIs.
    * @param connections An array of database connections.
    * @param namespaceURIs An array of namespace URIs. Entries may be null.
    */
   public MapFactory_Database(String[] databaseNames, Connection[] connections, String[] namespaceURIs)
   {
      this.databaseNames = databaseNames;
      this.connections = connections;
      this.namespaceURIs = namespaceURIs;
   }

   //**************************************************************************
   // Public methods -- setting variables
   //**************************************************************************

   /**
    * Whether to generate child elements or attributes from columns.
    *
    * @return True if child elements are generated from columns; false if
    *    attributes are generated.
    */
   public final boolean areColumnsElementTypes()
   {
      return useElementTypes;
   }

   /**
    * Set whether to generate child elements or attributes from columns.
    *
    * @param useElementTypes True if child elements are generated from columns; false
    *    if attributes are generated.
    */
   public void columnsAreElementTypes(boolean useElementTypes)
   {
      this.useElementTypes = useElementTypes;
   }

   /**
    * Set the database connection to use.
    *
    * <p>The database name is set to "Default" and no namespace URI is used.</p>
    *
    * @param conn The database connection.
    */
   public void setConnection(Connection conn)
   {
      connections = new Connection[1];
      connections[0] = conn;
      databaseNames = new String[1];
      databaseNames[0] = DEFAULT;
      namespaceURIs = new String[1];
      namespaceURIs[0] = null;
   }

   /**
    * Set the database connections to and namespace URIs to use.
    *
    * @param databaseNames An array of names for the databases in connections.
    * @param connections An array of database connections.
    * @param namespaceURIs An array of namespace URIs. Entries may be null.
    */
   public void setConnections(String[] databaseNames, Connection[] connections, String[] namespaceURIs)
   {
      this.databaseNames = databaseNames;
      this.connections = connections;
      this.namespaceURIs = namespaceURIs;
   }

   /**
    * Whether primary key links are followed.
    *
    * @return Whether primary key links are followed.
    */
   public boolean followPrimaryKeys()
   {
      return followPrimaryKeys;
   }

   /**
    * Set whether primary key links are followed.
    *
    * @param followPrimaryKeys Whether primary key links are followed.
    */
   public void followPrimaryKeys(boolean followPrimaryKeys)
   {
      this.followPrimaryKeys = followPrimaryKeys;
   }

   /**
    * Whether foreign key links are followed.
    *
    * @return Whether foreign key links are followed.
    */
   public boolean followForeignKeys()
   {
      return followForeignKeys;
   }

   /**
    * Set whether foreign key links are followed.
    *
    * @param followForeignKeys Whether foreign key links are followed.
    */
   public void followForeignKeys(boolean followForeignKeys)
   {
      this.followForeignKeys = followForeignKeys;
   }

   //**************************************************************************
   // Public methods -- creating maps
   //**************************************************************************

   /**
    * Create a map from a single root table.
    *
    * <p>The map factory will follow the graph from the root table until it ends.</p>
    *
    * <p>All tables are assumed to be in a single database named "Default".</p>
    *
    * @param rootCatalogName Name of the root catalog. May be null.
    * @param rootSchemaName Name of the root schema. May be null.
    * @param rootTableName Name of the root table.
    *
    * @return The XMLDBMSMap.
    * @exception SQLException An error occurred accessing the database.
    * @exception XMLMiddlewareException An error occurred building the map.
    */
   public XMLDBMSMap createMap(String rootCatalogName, String rootSchemaName, String rootTableName)
      throws SQLException, XMLMiddlewareException
   {
      // Check the arguments and state.

      checkNull(rootTableName, ROOT + TABLENAME);
      initGlobalVariables();

      // Create and return the map.

      processTable(DEFAULT, rootCatalogName, rootSchemaName, rootTableName, emptyHashtable);
      invertMap();
      return map;
   }

   /**
    * Create a map from a set of root tables.
    *
    * <p>The map factory will follow the graph from each root table until it ends.</p>
    *
    * @param rootDatabaseNames Names of the root databases. If any entries are null, the
    *    name "Default" is used.
    * @param rootCatalogNames Names of the root catalogs. Entries may be null.
    * @param rootSchemaNames Names of the root schemas. Entries may be null.
    * @param rootTableNames Names of the root tables.
    *
    * @return The XMLDBMSMap.
    * @exception SQLException An error occurred accessing the database.
    * @exception XMLMiddlewareException An error occurred building the map.
    */
   public XMLDBMSMap createMap(String[] rootDatabaseNames, String[] rootCatalogNames, String[] rootSchemaNames, String[] rootTableNames)
      throws SQLException, XMLMiddlewareException
   {
      checkArray(rootTableNames, 0, true, null, TABLENAME);
      checkArray(rootDatabaseNames, rootTableNames.length, false, DEFAULT, ROOT + DATABASENAME);
      checkArray(rootCatalogNames, rootTableNames.length, false, null, CATALOGNAME);
      checkArray(rootSchemaNames, rootTableNames.length, false, null, SCHEMANAME);

      initGlobalVariables();

      // Create and return the map.

      processTables(rootDatabaseNames, rootCatalogNames, rootSchemaNames, rootTableNames, emptyHashtable);
      invertMap();
      return map;
   }

   /**
    * Create a map from a set of root tables, stopping at designated "stop" tables.
    *
    * <p>The map factory will follow the graph from each root table until it hits one of the
    * designated stop tables or until the graph ends. Note that stop tables are not
    * included in the map.</p>
    *
    * <p>The number of stop tables is almost always different from the number of
    * root tables since stop tables are not directly related to root tables.</p>
    *
    * @param rootDatabaseNames Names of the root databases. If any entries are null, the
    *    name "Default" is used.
    * @param rootCatalogNames Names of the root catalogs. Entries may be null.
    * @param rootSchemaNames Names of the root schemas. Entries may be null.
    * @param tableNames Names of the root tables.
    * @param stopDatabaseNames Names of the stop databases. If any entries are null, the
    *    name "Default" is used.
    * @param stopCatalogNames Names of the stop catalogs. Entries may be null.
    * @param stopSchemaNames Names of the stop schemas. Entries may be null.
    * @param stopTableNames Names of the stop tables.
    *
    * @return The XMLDBMSMap.
    * @exception SQLException An error occurred accessing the database.
    * @exception XMLMiddlewareException An error occurred building the map.
    */
   public XMLDBMSMap createMap(String[] rootDatabaseNames, String[] rootCatalogNames, String[] rootSchemaNames, String[] rootTableNames, String[] stopDatabaseNames, String[] stopCatalogNames, String[] stopSchemaNames, String[] stopTableNames)
      throws SQLException, XMLMiddlewareException
   {
      String    name;
      Hashtable stopTables = new Hashtable();

      checkArray(rootTableNames, 0, true, null, ROOT + TABLENAME);
      checkArray(rootDatabaseNames, rootTableNames.length, false, DEFAULT, ROOT + DATABASENAME);
      checkArray(rootCatalogNames, rootTableNames.length, false, null, ROOT + CATALOGNAME);
      checkArray(rootSchemaNames, rootTableNames.length, false, null, ROOT + SCHEMANAME);

      checkArray(stopTableNames, 0, true, null, STOP + TABLENAME);
      checkArray(stopDatabaseNames, stopTableNames.length, false, DEFAULT, STOP + DATABASENAME);
      checkArray(stopCatalogNames, stopTableNames.length, false, null, STOP + CATALOGNAME);
      checkArray(stopSchemaNames, stopTableNames.length, false, null, STOP + SCHEMANAME);

      initGlobalVariables();

      // Create a hashtable of stop table names.

      for (int i = 0; i < stopTableNames.length; i++)
      {
         name = Table.getHashName(stopDatabaseNames[i], stopCatalogNames[i], stopSchemaNames[i], stopTableNames[i]);
         stopTables.put(name, o);
      }

      // Create and return the map.

      processTables(rootDatabaseNames, rootCatalogNames, rootSchemaNames, rootTableNames, stopTables);
      invertMap();
      return map;
   }

   /**
    * Create a map for all tables in a catalog or catalogs.
    *
    * <p>The map factory will follow the graph from each table in each root catalog
    * until it ends.</p>
    *
    * @param rootDatabaseNames Names of the root databases. If any entries are null, the
    *    name "Default" is used.
    * @param rootCatalogNames Names of the root catalogs.
    *
    * @return The XMLDBMSMap.
    * @exception SQLException An error occurred accessing the database.
    * @exception XMLMiddlewareException An error occurred building the map.
    */
   public XMLDBMSMap createMap(String[] rootDatabaseNames, String[] rootCatalogNames)
      throws SQLException, XMLMiddlewareException
   {
      checkArray(rootCatalogNames, 0, true, null, ROOT + CATALOGNAME);
      checkArray(rootDatabaseNames, rootCatalogNames.length, false, DEFAULT, ROOT + DATABASENAME);

      initGlobalVariables();

      // Create and return the map.

      processCatalogs(rootDatabaseNames, rootCatalogNames);
      invertMap();
      return map;
   }

   /**
    * Create a map for all tables in a schema or schemas.
    *
    * <p>The map factory will follow the graph from each table in each root schema
    * until it ends.</p>
    *
    * @param rootDatabaseNames Names of the root databases. If any entries are null, the
    *    name "Default" is used.
    * @param rootCatalogNames Names of the root catalogs. Entries may be null.
    * @param rootSchemaNames Names of the root schemas.
    *
    * @return The XMLDBMSMap.
    * @exception SQLException An error occurred accessing the database.
    * @exception XMLMiddlewareException An error occurred building the map.
    */
   public XMLDBMSMap createMap(String[] rootDatabaseNames, String[] rootCatalogNames, String rootSchemaNames[])
      throws SQLException, XMLMiddlewareException
   {
      checkArray(rootSchemaNames, 0, true, null, ROOT + SCHEMANAME);
      checkArray(rootDatabaseNames, rootSchemaNames.length, false, DEFAULT, ROOT + DATABASENAME);
      checkArray(rootCatalogNames, rootSchemaNames.length, false, null, ROOT + CATALOGNAME);

      initGlobalVariables();

      // Create and return the map.

      processSchemas(rootDatabaseNames, rootCatalogNames, rootSchemaNames);
      invertMap();
      return map;
   }

   //**************************************************************************
   // Private methods -- iterators over catalogs, schemas, and tables
   //**************************************************************************

   private void processCatalogs(String[] databaseNames, String[] catalogNames)
      throws SQLException, XMLMiddlewareException
   {
      for (int i = 0; i < databaseNames.length; i++)
      {
         processCatalogOrSchema(databaseNames[i], catalogNames[i], null);
      }
   }

   private void processSchemas(String[] databaseNames, String[] catalogNames, String[] schemaNames)
      throws SQLException, XMLMiddlewareException
   {
      for (int i = 0; i < databaseNames.length; i++)
      {
         processCatalogOrSchema(databaseNames[i], catalogNames[i], schemaNames[i]);
      }
   }

   private void processTables(String[] databaseNames, String[] catalogNames, String[] schemaNames, String[] tableNames, Hashtable stopTables)
      throws SQLException, XMLMiddlewareException
   {
      for (int i = 0; i < databaseNames.length; i++)
      {
         processTable(databaseNames[i], catalogNames[i], schemaNames[i], tableNames[i], stopTables);
      }
   }

   private void processCatalogOrSchema(String databaseName, String catalogName, String schemaName)
      throws SQLException, XMLMiddlewareException
   {
      DatabaseMetaData meta;
      ResultSet        rs;
      Vector           catalogNames = new Vector(),
                       schemaNames = new Vector(),
                       tableNames = new Vector();

      // Get a DatabaseMetaData object for the connection used by the database and
      // escape the _ and % characters in the schema name, as these are treated as wildcards.

      // It is not clear whether to set null catalog and schema names to null or an
      // empty string. For the moment, set these to an empty string. See the catalog
      // methods in DatabaseMetaData (getTables, getColumns, etc.) for details.

      meta = getDatabaseMetaData(databaseName);
//      if (catalogName == null) catalogName = "";
//      if (schemaName == null) schemaName = "";
      schemaName = escapeDBName(meta, schemaName);

      // Get the catalog, schema, and table names and cache them.

      rs = meta.getTables(catalogName, schemaName, null, null);
      while (rs.next())
      {
         catalogNames.addElement(rs.getString(1));
         schemaNames.addElement(rs.getString(2));
         tableNames.addElement(rs.getString(3));
      }

      // Close the result set before processing individual tables. This ensures
      // that only one result set is open at a time, thereby avoiding problems
      // with databases that can have only one open result set on a connection.

      rs.close();

      // Process the tables.

      for (int i = 0; i < schemaNames.size(); i++)
      {
         processTable(databaseName, (String)catalogNames.elementAt(i), (String)schemaNames.elementAt(i), (String)tableNames.elementAt(i), emptyHashtable);
      }
   }

   //**************************************************************************
   // Private methods -- create maps
   //**************************************************************************

   private void processTable(String databaseName, String catalogName, String schemaName, String tableName, Hashtable stopTables)
      throws SQLException, XMLMiddlewareException
   {
      DatabaseMetaData meta;
      Table            table;
      ClassTableMap    classTableMap;
      XMLName          elementTypeName;
      Vector           remoteTables = new Vector(), remoteLinkInfos = new Vector();

      // Create a new Table. We use XMLDBMSMap.createTable because the Table might already
      // have been created. This happens when a table that we have already processed
      // points to this table. 

      table = map.createTable(databaseName, catalogName, schemaName, tableName);

      // Check if the table has already been processed. (This is possible due to
      // cycles in the database graph.) If so, just return. Note that just because
      // the table exists doesn't mean it has been processed (been through this
      // method). Tables are created in this method and in getForeignKeys.

      if (processedTables.get(table) != null) return;
      processedTables.put(table, o);

      // Create a new ClassTableMap. We use Table.createClassTableMap because
      // the ClassTableMap could have been created already. This happens when
      // an already-processed table pointed to this table -- the ClassTableMap
      // is created for inclusion in the RelatedClassTableMap.

      classTableMap = map.createClassTableMap(table);

      // Get a DatabaseMetaData object for the connection used by the database and
      // escape the _ and % characters in the schema name, as these are treated as wildcards.

      // It is not clear whether to set null catalog and schema names to null or an
      // empty string. For the moment, set these to an empty string. See the catalog
      // methods in DatabaseMetaData (getTables, getColumns, etc.) for details.

      meta = getDatabaseMetaData(databaseName);
//      if (catalogName == null) catalogName = "";
//      if (schemaName == null) schemaName = "";
      schemaName = escapeDBName(meta, schemaName);

      // Get the primary key and add it to the table. Note that some of this
      // information is duplicated in getForeignKeys(), but not all of it. In
      // particular, leaf tables reached by following a primary key will not
      // return primary key information through getForeignKeys().

      getPrimaryKey(meta, table, catalogName, schemaName, tableName);

      // If the element type name has not been set, create it and set it now.
      // This will already be set if the ClassTableMap was created when following
      // a primary or foreign key.

      if (classTableMap.getElementTypeName() == null)
      {
         elementTypeName = getElementTypeName(table.getDatabaseName(), table.getCatalogName(), table.getSchemaName(), table.getTableName());
         classTableMap.setElementTypeName(elementTypeName);
      }

      // If we are following foreign keys, build the foreign keys in the current
      // table. We need to do this because the foreign key columns won't be included
      // in the element type for the current table. Instead, they will be included in
      // the element type for the child table, where they are primary key columns.
      //
      // Note that foreign keys pointing to stop tables will not be built, so the columns
      // that point to those tables will be included in the element type for this table.

      if (followForeignKeys)
      {
         getForeignKeys(meta, databaseName, catalogName, schemaName, tableName, remoteTables, remoteLinkInfos, false, stopTables);
      }

      // Process the columns, then set the result set indexes.

      processColumns(meta, classTableMap, table, databaseName, catalogName, schemaName, tableName);
      setResultSetIndexes(table);

      // If we are following primary keys, get the tables to which the primary keys
      // are exported and the LinkInfos needed to join those tables to the current table.

      if (followPrimaryKeys)
      {
         getForeignKeys(meta, databaseName, catalogName, schemaName, tableName, remoteTables, remoteLinkInfos, true, stopTables);
      }

      // Link the remote tables to the current table, then process the remote tables.
      // We do this in two steps because it gives a slightly better unfolding of the
      // graph into a hierarchy in the case of cycles. Specifically, we check for cycles
      // by testing if a link has already been used. If both primary keys and foreign
      // keys are being followed, a link can be approached from either direction.
      //
      // By adding all links before processing any remote tables, we provide the
      // shortest possible path to the remote table from the current table. Otherwise,
      // the depth-first recursive nature of the code would loop around and approach the
      // link from the other direction, creating a longer path.

      linkRemoteTables(classTableMap, remoteTables, remoteLinkInfos);
      processRemoteTables(remoteTables, stopTables);
   }

   private void processColumns(DatabaseMetaData meta, ClassTableMap classTableMap, Table table, String databaseName, String catalogName, String schemaName, String tableName)
      throws SQLException
   {
      Enumeration foreignKeys;
      Key         foreignKey;
      Column[]    fkColumns;
      Hashtable   fkColumnNames = new Hashtable();
      ResultSet   rs;
      String      columnName;
      Column      column;
      int         type;
      int         len;

      // If we are converting to attributes, build a hashtable to hold the attribute names.

      if (!useElementTypes)
      {
         attributeHashes.put(classTableMap.getElementTypeName().getUniversalName(), new Hashtable());
      }

      // Build a hashtable of the foreign key column names. These columns won't
      // be mapped, since they will be mapped on the element of the table to which
      // the foreign key points.

      foreignKeys = table.getForeignKeys();
      while (foreignKeys.hasMoreElements())
      {
         foreignKey = (Key)foreignKeys.nextElement();
         fkColumns = foreignKey.getColumns();
         for (int i = 0; i < fkColumns.length; i++)
         {
            fkColumnNames.put(fkColumns[i].getName(), o);
         }
      }

      // Get a result set of columns in the current table. Note that we escape
      // any % or _ characters in the table name, since these are treated as
      // wild card characters.

      rs = meta.getColumns(catalogName, schemaName, escapeDBName(meta, tableName), null);

      while (rs.next())
      {
         // Get the column name and create a Column object. We use Table.createColumn()
         // since the Column might have been created during key processing.

         columnName = rs.getString(4);
         column = table.createColumn(columnName);

         // Set the various column metadata.

         type = JDBCTypes.convertDateTimeType(rs.getShort(5));
         column.setType(type);
         len = rs.getInt(7);
         if (JDBCTypes.typeIsChar(type) || JDBCTypes.typeIsBinary(type))
         {
            column.setLength(len);
         }
         column.setNullability(rs.getInt(11));
         column.setFormatter(map.getDefaultFormatter(type));

         // If the column is not part of a foreign key, create a ColumnMap for it.

         if (fkColumnNames.get(columnName) == null)
         {
            createColumnMap(classTableMap, table, column);
         }
      }

      // Close the result set.

      rs.close();
   }

   private void setResultSetIndexes(Table table)
   {
      Enumeration columns;
      Column      column;

      // Set the column numbers from 1 to n.

      columns = table.getColumns();
      for (int i = 1; i <= table.getNumberOfColumns(); i++)
      {
         column = (Column)columns.nextElement();
         column.setResultSetIndex(i);
      }
   }

   private void createColumnMap(ClassTableMap classTableMap, Table table, Column column)
   {
      ColumnMap columnMap;
      XMLName   xmlName;
      String    parentElementTypeName;
      int       type;

      // Create a ColumnMap for the column.

      columnMap = classTableMap.createColumnMap(column);

      // Set the element or attribute name and type.

      type = (useElementTypes) ? ColumnMap.ELEMENTTYPE : ColumnMap.ATTRIBUTE;
      parentElementTypeName = classTableMap.getElementTypeName().getUniversalName();
      xmlName = getXMLName(table.getDatabaseName(), table.getCatalogName(), table.getSchemaName(), table.getTableName(), column.getName(), parentElementTypeName, type);
      columnMap.setXMLName(xmlName, type);
   }

   private void linkRemoteTables(ClassTableMap classTableMap, Vector remoteTables, Vector linkInfos)
      throws SQLException, XMLMiddlewareException
   {
      LinkInfo             linkInfo;
      Table                remoteTable;
      ClassTableMap        remoteCTM;
      RelatedClassTableMap relatedClassTableMap;
      XMLName              elementTypeName;

      for (int i = 0; i < remoteTables.size(); i++)
      {
         // Get the next LinkInfo and remote table, then create a ClassTableMap for
         // the remote table. We use XMLDBMSMap.createClassTableMap because the table might
         // already have been created.

         linkInfo = (LinkInfo)linkInfos.elementAt(i);
         remoteTable = (Table)remoteTables.elementAt(i);
         remoteCTM = map.createClassTableMap(remoteTable);

         // Check if the remote table is already linked to the current table with the
         // keys in the current link. If so, then don't create a RelatedClassTableMap
         // for the link. Doing so will cause an infinite loop in DBMSToDOM, where the
         // parent row gets the child row gets the parent row gets the child row...

         if (linkExists(classTableMap.getTable(), linkInfo, remoteCTM)) continue;

         // Create a RelatedClassTableMap for the remote table, add it to the
         // ClassTableMap, and set its LinkInfo.

         relatedClassTableMap = RelatedClassTableMap.create(remoteCTM);
         classTableMap.addRelatedClassTableMap(relatedClassTableMap);
         relatedClassTableMap.setLinkInfo(linkInfo);

         // Get the element type name from the ClassTableMap for the remote table
         // and set this on the RelatedClassTableMap. (If it hasn't been set on
         // the ClassTableMap for the remote table, set it now.)

         elementTypeName = remoteCTM.getElementTypeName();
         if (elementTypeName == null)
         {
            elementTypeName = getElementTypeName(remoteTable.getDatabaseName(), remoteTable.getCatalogName(), remoteTable.getSchemaName(), remoteTable.getTableName());
            remoteCTM.setElementTypeName(elementTypeName);
         }
         relatedClassTableMap.setElementTypeName(elementTypeName);
      }
   }

   private boolean linkExists(Table localTable, LinkInfo localLinkInfo, ClassTableMap remoteCTM)
   {
      Enumeration          remoteRCTMs;
      RelatedClassTableMap remoteRCTM;
      LinkInfo             remoteLinkInfo;

      // Get a list of RelatedClassTableMaps on the remote table that point to the
      // current table. Note that there can be more than one. For example, the remote
      // table might have multiple foreign keys pointing to the current table.

      remoteRCTMs = remoteCTM.getRelatedClassTableMap(localTable.getDatabaseName(), localTable.getCatalogName(), localTable.getSchemaName(), localTable.getTableName());

      while (remoteRCTMs.hasMoreElements())
      {
         // Check each RelatedClassTableMap to see if its LinkInfo uses the same
         // primary and foreign keys as the LinkInfo we are currently processing.
         // If so, return that the link already exists. Note that we can compare
         // actual objects because the use of Table.createXxxxxKey ensures that
         // no two objects represent the same key.

         remoteRCTM = (RelatedClassTableMap)remoteRCTMs.nextElement();
         remoteLinkInfo = remoteRCTM.getLinkInfo();
         if (remoteLinkInfo.getParentKey().equals(localLinkInfo.getChildKey()) &&
             remoteLinkInfo.getChildKey().equals(localLinkInfo.getParentKey()))
         {
            return true;
         }
      }
      return false;
   }

   private void processRemoteTables(Vector remoteTables, Hashtable stopTables)
      throws SQLException, XMLMiddlewareException
   {
      Table remoteTable;

      for (int i = 0; i < remoteTables.size(); i++)
      {
         // Get the next remote table and process it.

         remoteTable = (Table)remoteTables.elementAt(i);
         processTable(remoteTable.getDatabaseName(), remoteTable.getCatalogName(), remoteTable.getSchemaName(), remoteTable.getTableName(), stopTables);
      }
   }

   //**************************************************************************
   // Private methods -- get database metadata
   //**************************************************************************

   private void getPrimaryKey(DatabaseMetaData meta, Table table, String catalogName, String schemaName, String tableName)
      throws SQLException
   {
      ResultSet rs;
      String    pkColumnName, pkName;
      short     keySeq;
      int       iKeySeq;
      Key       primaryKey = null;
      Column[]  pkColumns;
      Vector    pkColumnNames = new Vector(), keySeqs = new Vector();

      // Get the result set containing the primary key columns.

      rs = meta.getPrimaryKeys(catalogName, schemaName, tableName);
      while (rs.next())
      {
         // Retrieve the column names and sequence in which they occur
         // in the key. Cache these for later processing.

         pkColumnName = rs.getString(4);
         keySeq = rs.getShort(5);
         pkColumnNames.addElement(pkColumnName);
         keySeqs.addElement(new Integer(keySeq));

         // When processing the first column in the key, get the primary
         // key name and create a Key for the primary key.

         if (keySeq == 1)
         {
            pkName = rs.getString(6);
            if (rs.wasNull()) pkName = null;
            primaryKey = table.createPrimaryKey(pkName);
         }
      }

      // Close the result set.

      rs.close();

      // If a primary key object was created, allocate an array for the primary
      // key columns and place each column in its correct position in the array,
      // then set the array of columns in the Key. (No primary key object is created
      // if the table does not have a primary key.)

      if (primaryKey != null)
      {
         pkColumns = new Column[pkColumnNames.size()];
         for (int i = 0; i < pkColumnNames.size(); i++)
         {
            iKeySeq = ((Integer)keySeqs.elementAt(i)).intValue();
            pkColumnName = (String)pkColumnNames.elementAt(i);
            pkColumns[iKeySeq - 1] = table.createColumn(pkColumnName);
         }
         primaryKey.setColumns(pkColumns);
      }
   }

   private void getForeignKeys(DatabaseMetaData meta, String databaseName, String catalogName, String schemaName, String tableName, Vector remoteTables, Vector linkInfos, boolean getExportedKeys, Hashtable stopTables)
      throws SQLException
   {
      // This method creates the primary and foreign keys in the current table,
      // as well as any tables linked to the current table and the keys in those
      // tables used to do the linkage. It returns a list of the remote tables and
      // the LinkInfos need to connect to those tables. These are processed later.

      ResultSet rs;
      String    pkCatalogName, pkSchemaName, pkTableName, pkColumnName, pkName,
                fkCatalogName, fkSchemaName, fkTableName, fkColumnName, fkName,
                remoteTableName;
      short     keySeq;
      Vector    pkColumnNames = new Vector(), fkColumnNames = new Vector();
      Key       primaryKey = null, foreignKey = null;
      Table     pkTable = null, fkTable = null;
      LinkInfo  linkInfo;

      // Get the result set of imported or exported keys.
      //
      // getExportedKeys tells us whether we are getting keys that are exported from
      // this table (that is, foreign keys in other tables that match the primary key in
      // this table), or keys that are imported into this table (that is, foreign keys in
      // this table that match the primary key in another table).
      //
      // In the first case, the pkTables are all the current table and the primaryKeys
      // are all the primary key of the current table. In the second case, the fkTables
      // are all the current table and the foreignKeys are the foreign keys in the
      // current table.

      if (getExportedKeys)
      {
         rs = meta.getExportedKeys(catalogName, schemaName, tableName);
      }
      else
      {
         rs = meta.getImportedKeys(catalogName, schemaName, tableName);
      }

      // Process the result set.

      while (rs.next())
      {
         // Get the primary key info.

         pkCatalogName = rs.getString(1);
         if (rs.wasNull()) pkCatalogName = null;
         pkSchemaName = rs.getString(2);
         if (rs.wasNull()) pkSchemaName = null;
         pkTableName = rs.getString(3);
         pkColumnName = rs.getString(4);

         // Get the foreign key info.

         fkCatalogName = rs.getString(5);
         if (rs.wasNull()) fkCatalogName = null;
         fkSchemaName = rs.getString(6);
         if (rs.wasNull()) fkSchemaName = null;
         fkTableName = rs.getString(7);
         fkColumnName = rs.getString(8);

         // Get the name of the remote table and check if it is is a stop table.
         // If so, don't process it.

         if (getExportedKeys)
         {
            remoteTableName = Table.getHashName(databaseName, fkCatalogName, fkSchemaName, fkTableName);
         }
         else
         {
            remoteTableName = Table.getHashName(databaseName, pkCatalogName, pkSchemaName, pkTableName);
         }
         if (stopTables.get(remoteTableName) != null) continue;

         // Get the key sequence number.

         keySeq = rs.getShort(9);

         // Get the key names, if any.

         fkName = rs.getString(12);
         if (rs.wasNull()) fkName = null;
         pkName = rs.getString(13);
         if (rs.wasNull()) pkName = null;

         // Each key is sorted by key sequence number, so when the key sequence
         // number is 1, finish processing the previous keys (if any) and create
         // new keys.

         if (keySeq == 1)
         {
            if (foreignKey != null)
            {
               // Set the key columns in the previous keys.

               setKeyColumnArray(fkTable, fkColumnNames, foreignKey);
               setKeyColumnArray(pkTable, pkColumnNames, primaryKey);
            }

            // Create the primary key table and the primary key. We use XMLDBMSMap.createTable
            // because the table might already have been built. For example, another
            // table that we have already processed might have pointed to it, or we
            // are processing exported keys, in which case the primary key table is
            // always the current table.

            pkTable = map.createTable(databaseName, pkCatalogName, pkSchemaName, pkTableName);
            primaryKey = pkTable.createPrimaryKey(pkName);

            // Build the foreign key table and foreign key. Also set the remote table
            // and key on the foreign key.

            fkTable = map.createTable(databaseName, fkCatalogName, fkSchemaName, fkTableName);
            foreignKey = fkTable.createForeignKey(fkName);
            foreignKey.setRemoteKey(pkTable, primaryKey);

            // Save the remote table. If we are getting exported keys (that is, keys
            // related to the current table's primary key), this is the foreign key
            // table. If we are getting imported keys (that is, keys related to foreign
            // keys in the current table), this is the primary key table.
            //
            // At the same time, create the LinkInfo used to connect the two tables
            // and add it to the list. Note that the current table is always the parent
            // table (the first argument in LinkInfo.create()).

            if (getExportedKeys)
            {
               remoteTables.addElement(fkTable);
               linkInfo = LinkInfo.create(primaryKey, foreignKey);
            }
            else
            {
               remoteTables.addElement(pkTable);
               linkInfo = LinkInfo.create(foreignKey, primaryKey);
            }
            linkInfos.addElement(linkInfo);

            // Clear the lists of key column names.

            fkColumnNames.removeAllElements();
            pkColumnNames.removeAllElements();
         }

         // Add the current column name to the list of column names for the current keys.

         fkColumnNames.addElement(fkColumnName);
         pkColumnNames.addElement(pkColumnName);
      }

      // Close the result set.

      rs.close();

      // If a foreign key object was created, set the key columns in the last
      // keys processed. (No foreign key object is created if the table does not
      // export its primary key or import any foreign keys.)

      if (foreignKey != null)
      {
         setKeyColumnArray(fkTable, fkColumnNames, foreignKey);
         setKeyColumnArray(pkTable, pkColumnNames, primaryKey);
      }
   }

   private void setKeyColumnArray(Table table, Vector columnNames, Key key)
   {
      // This method just converts a Vector of column names into an array
      // of Columns, then sets this array on the key.

      Column[] columnArray;
      Column   column;

      columnArray = new Column[columnNames.size()];
      for (int i = 0; i < columnArray.length; i++)
      {
         column = table.createColumn((String)columnNames.elementAt(i));
         columnArray[i] = column;
      }
      key.setColumns(columnArray);
   }

   //**************************************************************************
   // Private methods -- utilities
   //**************************************************************************

   private DatabaseMetaData getDatabaseMetaData(String databaseName)
      throws SQLException
   {
      DatabaseMetaData meta;
      Connection       conn;

      // Get/create a DatabaseMetaData object for the current database.

      meta = (DatabaseMetaData)metas.get(databaseName);
      if (meta == null)
      {
         conn = (Connection)conns.get(databaseName);
         meta = conn.getMetaData();
         metas.put(databaseName, meta);
      }
      return meta;
   }

   private void invertMap()
      throws XMLMiddlewareException
   {
      MapInverter inverter;

      // Create the XML-centric view of the map.

      inverter = new MapInverter();
      inverter.createXMLView(map);
   }

   private String escapeDBName(DatabaseMetaData meta, String name)
      throws SQLException
   {
      String       escapeChar;
      StringBuffer escapedName = new StringBuffer();
      char[]       buf;

      // Just return if name is null.

      if (name == null) return null;

      // Get the character used to escape wildcard characters.

      escapeChar = meta.getSearchStringEscape();

      // Read through the name and escape any wildcard characters.

      buf = new char[name.length()];
      name.getChars(0, name.length(), buf, 0);
      for (int i = 0; i < buf.length; i++)
      {
         switch (buf[i])
         {
            case '%':
            case '_':
               escapedName.append(escapeChar);
               // WARNING! Note that we fall through to the default!

            default:
               escapedName.append(buf[i]);
               break;
         }
      }

      // Return the escaped string.

      return escapedName.toString();
   }

   //**************************************************************************
   // Private methods -- name conversion
   //**************************************************************************

   private XMLName getXMLName(String databaseName, String catalogName, String schemaName, String tableName, String columnName, String parentElementTypeName, int type)
   {
      String[] names = new String[5];

      names[0] = columnName;
      names[1] = tableName;
      names[2] = schemaName;
      names[3] = catalogName;
      names[4] = databaseName;

      if (type == ColumnMap.ELEMENTTYPE)
      {
         return getXMLName(names, elementTypeNames);
      }
      else // if (type == ColumnMap.ATTRIBUTE)
      {
         return getXMLName(names, (Hashtable)attributeHashes.get(parentElementTypeName));
      }
   }

   private XMLName getElementTypeName(String databaseName, String catalogName, String schemaName, String tableName)
   {
      String[] names = new String[4];

      names[0] = tableName;
      names[1] = schemaName;
      names[2] = catalogName;
      names[3] = databaseName;
      return getXMLName(names, elementTypeNames);
   }

   private XMLName getXMLName(String[] names, Hashtable uniqueNames)
   {
      // This method converts a table name to an element type name. It's very
      // poorly designed, as it knows the contents of the names array.

      XMLName xmlName;
      String  saveLocalName, localName, uri;

      // Convert the first name in the array (table or column name) to an XML 1.0 Name.

      saveLocalName = convertDBNameToLocalName(names[0]);

      // Construct an XMLName and see if it collides with an existing element
      // type name. If not, we are done.

      uri = (String)uris.get(names[names.length - 1]);
      xmlName = XMLName.create(uri, saveLocalName);
      if (uniqueNames.get(xmlName.getUniversalName()) == null) return xmlName;

      // If we have a collision, try prepending the other names in the list to
      // see if we can construct a unique name.

      for (int i = 1; i < names.length; i++)
      {
         localName = convertDBNameToLocalName(names[i]) + PERIOD + xmlName.getLocalName();
         xmlName = XMLName.create(uri, localName);
         if (uniqueNames.get(xmlName.getUniversalName()) == null) return xmlName;
      }

      // If we still have a collision, go back to the converted table or column name
      // and append 2, 3, 4, ... If we still have a collision, say mean things to the user.

      for (int i = 2; i < Integer.MAX_VALUE; i++)
      {
         localName = saveLocalName + String.valueOf(i);
         xmlName = XMLName.create(uri, localName);
         if (uniqueNames.get(xmlName.getUniversalName()) == null) return xmlName;
      }

      throw new IllegalStateException("If you get this exception, there is either a bug in XML-DBMS or the state of your database is ludicrous. See the code in MapFactory_Database.getXMLName.");
   }

   private String convertDBNameToLocalName(String dbName)
   {
      // BUG! This method needs to check that the dbName is a legal XML Name
      // and replace any illegal characters. To implement, break out the code
      // from the DTD parser that checks for legal characters, then add code
      // to replace illegal characters.

      return dbName;
   }

   //**************************************************************************
   // Private methods -- initialization and checking
   //**************************************************************************

   private void initGlobalVariables()
   {
      checkState();
      setDefault(databaseNames, DEFAULT);
      map = new XMLDBMSMap();
      processedTables.clear();
      elementTypeNames.clear();
      attributeHashes.clear();
      metas.clear();
      buildHashtable(conns, databaseNames, connections);
      buildHashtable(uris, databaseNames, namespaceURIs);
   }

   private void checkState()
   {
      if (connections == null)
         throw new IllegalStateException("You must set at least one connection.");
      if (connections.length == 0)
         throw new IllegalStateException("You must set at least one connection.");
      if (databaseNames == null)
         throw new IllegalStateException("You must set at least one database name.");
      if (connections.length != databaseNames.length)
         throw new IllegalStateException("The number of connections and database names must be the same.");
      if (connections.length != namespaceURIs.length)
         throw new IllegalStateException("The number of namespace URIs and database names must be the same.");
      checkNullEntries(connections, CONNECTIONS);
   }

   private void buildHashtable(Hashtable hash, Object[] keys, Object[] values)
   {
      hash.clear();
      for (int i = 0; i < keys.length; i++)
      {
         if (values[i] != null)
         {
            hash.put(keys[i], values[i]);
         }
      }
   }

   private void checkArray(Object[] array, int length, boolean checkNullEntries, Object defaultValue, String arrayName)
   {
      arrayName = arrayName + ARRAY;
      checkNull(array, arrayName);
      if (length != 0) checkLength(array, length, arrayName);
      if (checkNullEntries) checkNullEntries(array, arrayName);
      if (defaultValue != null) setDefault(array, defaultValue);
   }

   private void checkNull(Object arg, String argName)
   {
      if (arg == null)
         throw new IllegalArgumentException(argName + " must not be null.");
   }

   private void checkNull(Object[] array, String arrayName)
   {
      if (array == null)
         throw new IllegalArgumentException(arrayName + " must not be null.");
   }

   private void checkLength(Object[] array, int length, String arrayName)
   {
      if (array.length != length)
         throw new IllegalArgumentException(arrayName + " must have a length of " + String.valueOf(length));
   }

   private void checkNullEntries(Object[] array, String objectNames)
   {
      for (int i = 0; i < array.length; i++)
      {
         if (array[i] == null)
            throw new IllegalStateException("All " + objectNames + " must be non-null.");
      }
   }

   private void setDefault(Object[] array, Object defaultValue)
   {
      for (int i = 0; i < array.length; i++)
      {
         if (array[i] == null) array[i] = defaultValue;
      }
   }
}
