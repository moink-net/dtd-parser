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
// Changes from version 1.0:
// * Changed populateRow to fix bug when retrieving data from result sets.
// Changes from version 1.01:
// * Replaced DocumentFactory with ParserUtils.
// Changes from version 1.1:
// * Heavily updated for new map objects / DTD features

package org.xmlmiddleware.xmldbms;

import org.xmlmiddleware.conversions.StringFormatter;
import org.xmlmiddleware.conversions.ConversionException;
import org.xmlmiddleware.domutils.FragmentBuilder;
import org.xmlmiddleware.domutils.ParserUtils;
import org.xmlmiddleware.domutils.ParserUtilsException;
import org.xmlmiddleware.utils.XMLName;
import org.xmlmiddleware.xmldbms.maps.ClassTableMap;
import org.xmlmiddleware.xmldbms.maps.Column;
import org.xmlmiddleware.xmldbms.maps.ColumnMap;
import org.xmlmiddleware.xmldbms.maps.ElementInsertionList;
import org.xmlmiddleware.xmldbms.maps.ElementInsertionMap;
import org.xmlmiddleware.xmldbms.maps.Key;
import org.xmlmiddleware.xmldbms.maps.LinkInfo;
import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.MapException;
import org.xmlmiddleware.xmldbms.maps.OrderInfo;
import org.xmlmiddleware.xmldbms.maps.PropertyMapBase;
import org.xmlmiddleware.xmldbms.maps.PropertyTableMap;
import org.xmlmiddleware.xmldbms.maps.RelatedClassTableMap;
import org.xmlmiddleware.xmldbms.maps.Table;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Transfers data from the database to a DOM tree.
 *
 * <P>DBMSToDOM transfers data from the database to a DOM tree according
 * to a particular Map. The caller must provide a ParserUtils object for the
 * DOM implementation they are using (many are available in
 * de.tudarmstadt.ito.domutils), a Map object, and information about how
 * to retrieve the data. The latter can be one or more table names and key
 * values, a DocumentInfo object, or a result set.</P>
 *
 * <P>For example, the following code transfers data for sales order number 123
 * from the Sales table to a DOM tree using Oracle's DOM implementation:</P>
 *
 * <PRE>
 *    // Use a user-defined function to create a map.
 *    Map map = createMap("sales.map", conn);<BR />
 *
 *    // Create a new DBMSToDOM object.
 *    DBMSToDOM dbmsToDOM = new DBMSToDOM(map, new ParserUtilsXerces());<BR />
 *
 *    // Create a key and retrieve the data.
 *    Object[] key = {new Integer(123)};
 *    Document doc = dbmsToDOM.retrieveDocument("Sales", key);
 * </PRE>
 *
 * <P>Currently, no DOM implementations allow the namespace of an element or
 * attribute to be set. Therefore, the caller can choose whether element and
 * attribute names are prefixed according to the namespace prefixes in the
 * Map. This is useful if the DOM tree will be serialized as an XML document.
 * It might cause problems if the DOM tree is to be used directly, as the DOM
 * implementation will not correctly return the base name, the namespace URI,
 * or the qualified name. That is, it will return the prefixed name as the
 * base name and qualified name and null as the namespace URI. By default,
 * namespace prefixes are not used.</P>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class DBMSToDOM
{
   // ************************************************************************
   // Private variables
   // ************************************************************************

   private Map          map;
   private Document     doc;

   // 8/01 Adam Flinton
   // Replaced DocumentFactory with ParserUtils.

   private ParserUtils     utils;
   private FragmentBuilder fragmentBuilder;
   private TransferInfo    transferInfo;

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String EMPTYSTRING = "";
   private static String SPACE = " ";
   private static String FAKESTARTTAG = "<fake>";
   private static String FAKEENDTAG = "</fake>";

   private static final XMLName PCDATA = XMLName.create(null, "#PCDATA");

   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
    * Construct a new DBMSToDOM object.
    *
    * @param utils A ParserUtils object.
    */
   // 8/01 Adam Flinton
   // Replaced DocumentFactory with ParserUtils.
   public DBMSToDOM(ParserUtils utils)
      throws SAXException, ParserUtilsException
   {
      this.utils = utils;
      fragmentBuilder = new FragmentBuilder(utils.getXMLReader());
   }         

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Construct a DOM Document starting with data from the specified table.
    *
    * <p>Data will be retrieved from other subordinate tables according to the map.
    * If more than one row is retrieved from the specified table, the list of
    * wrapper element names must contain at least one entry.</p>
    *
    * @param transferInfo A TransferInfo object containing a Map and at least
    *    one DataSource.
    * @param databaseName The name of the database in which the table resides. If this
    *    is null, "Default" is used.
    * @param catalogName The name of the catalog in which the table resides. May be null.
    * @param schemaName The name of the schema in which the table resides. May be null.
    * @param tableName The name of the table from which to retrieve data.
    * @param key The key used to retrieve data. This array will contain as many
    *    values are there are columns in the key. If you want to retrieve multiple
    *    rows from the same table using different keys, use the forms of retrieveDocument
    *    that accept an array of keys.
    * @param wrapperURIs A list of URIs for element types in which to wrap the retrieved
    *    data. May be null. If this is not null, it must have the same number of values
    *    as wrapperNames.
    * @param wrapperNames A list of qualified names of the element types in which to
    *    wrap the retrieved data. May be null.
    */
   public Document retrieveDocument(TransferInfo transferInfo, String databaseName, String catalogName, String schemaName, String tableName, Object[] key, String[] wrapperURIs, String[] wrapperNames)
      throws ParserUtilsException, SQLException, MapException
   {
      OrderedNode rootNode;

      // Set things up.

      initGlobals(transferInfo);
      rootNode = initDoc(wrapperURIs, wrapperNames);

      // Build and return the Document.

      retrieveRootTableData(rootNode, databaseName, catalogName, schemaName, tableName, key);
      return doc;
   }

   /**
    * Construct a DOM Document starting with multiple rows of data from the specified table.
    *
    * <p>Data will be retrieved from other subordinate tables according to the map.
    * If more than one row is retrieved from the specified table, the list of
    * wrapper element names must contain at least one entry.</p>
    *
    * @param transferInfo A TransferInfo object containing a Map and at least
    *    one DataSource.
    * @param databaseName The name of the database in which the table resides. If this
    *    is null, "Default" is used.
    * @param catalogName The name of the catalog in which the table resides. May be null.
    * @param schemaName The name of the schema in which the table resides. May be null.
    * @param tableName The name of the table from which to retrieve data.
    * @param key The keys used to retrieve data. The primary index
    *    distinguishes keys; the secondary index distinguishes column values in a key.
    * @param wrapperURIs A list of URIs for element types in which to wrap the retrieved
    *    data. May be null. If this is not null, it must have the same number of values
    *    as wrapperNames.
    * @param wrapperNames A list of qualified names of the element types in which to
    *    wrap the retrieved data. May be null.
    */
   public Document retrieveDocument(TransferInfo transferInfo, String databaseName, String catalogName, String schemaName, String tableName, Object[][] key, String[] wrapperURIs, String[] wrapperNames)
      throws ParserUtilsException, SQLException, MapException
   {
      OrderedNode rootNode;

      // Set things up.

      initGlobals(transferInfo);
      rootNode = initDoc(wrapperURIs, wrapperNames);

      // Build and return the Document.

      for (int i = 0; i < key.length; i++)
      {
         retrieveRootTableData(rootNode, databaseName, catalogName, schemaName, tableName, key[i]);
      }
      return doc;
   }

   /**
    * Construct a DOM Document starting with data from the specified tables.
    *
    * <p>Data will be retrieved from other subordinate tables according to the map.
    * If more than one row is retrieved from the specified tables, the list of
    * wrapper element names must contain at least one entry.</p>
    *
    * @param transferInfo A TransferInfo object containing a Map and at least
    *    one DataSource.
    * @param databaseNames The names of the databases in which the tables reside. If the
    *    array is null, "Default" is used for all tables. If an individual value is null,
    *    "Default" is used for that value.
    * @param catalogNames The names of the catalogs in which the tables reside. The array
    *    or entries in the array may be null.
    * @param schemaNames The names of the schemas in which the tables reside. The array or
    *    entries in the array may be null.
    * @param tableNames The names of the tables from which to retrieve data.
    * @param key The keys used to retrieve data from each table. The primary index
    *    distinguishes keys; the secondary index distinguishes column values in a key.
    * @param wrapperURIs A list of URIs for element types in which to wrap the retrieved
    *    data. May be null. If this is not null, it must have the same number of values
    *    as wrapperNames.
    * @param wrapperNames A list of qualified names of the element types in which to
    *    wrap the retrieved data. May be null.
    */
   public Document retrieveDocument(TransferInfo transferInfo, String[] databaseNames, String[] catalogNames, String[] schemaNames, String[] tableNames, Object[][] keys, String[] wrapperURIs, String[] wrapperNames)
      throws ParserUtilsException, SQLException, MapException
   {
      OrderedNode rootNode;
      String      databaseName, catalogName, schemaName;

      // Set things up.

      initGlobals(transferInfo);
      rootNode = initDoc(wrapperURIs, wrapperNames);

      // Build and return the Document.

      for (int i = 0; i < tableNames.length; i++)
      {
         databaseName = (databaseNames == null) ? null : databaseNames[i];
         catalogName = (catalogNames == null) ? null : catalogNames[i];
         schemaName = (schemaNames == null) ? null : schemaNames[i];
         retrieveRootTableData(rootNode, databaseName, catalogName, schemaName, tableNames[i], keys[i]);
      }
      return doc;
   }

   /**
    * Construct a DOM Document starting with data from the result set.
    *
    * <p>This method retrieves all rows in the result set, but does not close it.</p>
    *
    * <p>Data will be retrieved from other subordinate tables according to the map.
    * If more than one row is retrieved from the result set, the list of
    * wrapper element names must contain at least one entry.</p>
    *
    * @param transferInfo A TransferInfo object containing a Map and at least
    *    one DataSource.
    * @param rs The result set.
    * @param rsDatabaseName The name of the database used to map the result set. If this
    *    is null, "Default" is used.
    * @param rsCatalogName The name of the catalog used to map the result set. May be null.
    * @param rsSchemaName The name of the schema used to map the result set. May be null.
    * @param rsTableName The name of the table used to map the result set.
    * @param wrapperURIs A list of URIs for element types in which to wrap the retrieved
    *    data. May be null. If this is not null, it must have the same number of values
    *    as wrapperNames.
    * @param wrapperNames A list of qualified names of the element types in which to
    *    wrap the retrieved data. May be null.
    */
   public Document retrieveDocument(TransferInfo transferInfo, ResultSet rs, String rsDatabaseName, String rsCatalogName, String rsSchemaName, String rsTableName, String[] wrapperURIs, String[] wrapperNames)
      throws ParserUtilsException, SQLException, MapException
   {
      OrderedNode   rootNode;
      ClassTableMap rootTableMap;

      // Set things up.

      initGlobals(transferInfo);
      rootNode = initDoc(wrapperURIs, wrapperNames);
      rootTableMap = map.getClassTableMap(rsDatabaseName, rsCatalogName, rsSchemaName, rsTableName);

      // Build and return the Document.

      processClassResultSet(rootNode, rs, rootTableMap.getElementTypeName(), null, rootTableMap);
      return doc;
   }

   /**
    * Construct a DOM Document starting with data specified by a DocumentInfo object.
    *
    * <p>Data will be retrieved from other subordinate tables according to the map.
    * If the DocumentInfo object specifies more than one row, the list of
    * wrapper element names must contain at least one entry.</p>
    *
    * @param transferInfo A TransferInfo object containing a Map and at least
    *    one DataSource.
    * @param docInfo The DocumentInfo specifying which rows to retrieve.
    * @param wrapperURIs A list of URIs for element types in which to wrap the retrieved
    *    data. May be null. If this is not null, it must have the same number of values
    *    as wrapperNames.
    * @param wrapperNames A list of qualified names of the element types in which to
    *    wrap the retrieved data. May be null.
    */
/*   public Document retrieveDocument(TransferInfo transferInfo, DocumentInfo docInfo, String[] wrapperURIs, String[] wrapperNames)
      throws ParserUtilsException, SQLException, MapException
   {
     RootTableMap      rootTableMap;
     TableMap          tableMap = null;
     Column[]          valueCols = null;
     Column            orderCol = null;
     PreparedStatement select = null;
     ResultSet         rs;
     Order             order = new Order();

      // Set things up.

      initGlobals(transferInfo);
      rootNode = initDoc(wrapperURIs, wrapperNames);

      // Process the entries in the DocumentInfo object.
      for (int i = 0; i < docInfo.size(); i++)
      {
         // Get the RootTableMap for the next table in the DocumentInfo. If it
         // is different from the previous table, build a new SELECT statement.

         rootTableMap = map.getRootTableMap(docInfo.getTableName(i));
         if (tableMap != rootTableMap.tableMap)
         {
            tableMap = rootTableMap.tableMap;
            valueCols = tableMap.table.getColumns(docInfo.getKeyColumnNames(i));
            orderCol = tableMap.table.getColumn(docInfo.getOrderColumnName(i));
            if (select != null)
            {
               map.checkInSelectStmt(select);
            }
            select = map.checkOutSelectStmt(tableMap.table, valueCols, orderCol);
          }

          // Set the parameters in the SELECT statement to the key values,
          // then execute the SELECT statement and process the result set.

          parameters.setParameters(select, docInfo.getKey(i), valueCols);
          rs = select.executeQuery();
          processRootResultSet(rootTableMap, rs, orderCol, order);
      }

      return doc;
   }
*/

/*
   public void setDBErrorHandling??
   public SQLException getSQLExceptions??
   public SQLException getSQLWarnings??
*/
   // ************************************************************************
   // Helper methods for getting started
   // ************************************************************************

   private void retrieveRootTableData(OrderedNode rootNode, String databaseName, String catalogName, String schemaName, String tableName, Object[] key)
      throws ParserUtilsException, SQLException, MapException
   {
      ClassTableMap rootTableMap;
      Table         rootTable;
      DataHandler   dataHandler;
      ResultSet     rs;

      // Get the map for the table, construct a result set
      // over it, and process the result set.

      rootTableMap = map.getClassTableMap(databaseName, catalogName, schemaName, tableName);
      rootTable = rootTableMap.getTable();

      dataHandler = transferInfo.getDataHandler(rootTable.getDatabaseName());

      rs = dataHandler.select(rootTable, key, null);
      processClassResultSet(rootNode, rs, rootTableMap.getElementTypeName(), null, rootTableMap);
      rs.close();
   }

   // ************************************************************************
   // Main processing methods
   // ************************************************************************

   // The processing flow is as follows. Note that property tables and columns
   // eventually result in leaf nodes, while related classes result in branch nodes.
   //
   //                                1.                 2.
   //      --->processClassResultSet --> processColumns --> processColumn
   //      |        3. |                                          ^
   //      |           v                                          |
   //    5.|   processRelatedTables--------                       |
   //      |        4. |                   |                      |
   //      |           v                6. |                   8. |
   //      |---processRelatedClassTable    |                      |
   //                                      |                      |
   //                                      v          7.          |
   //                            processPropertyTable --> processPropResultSet

   private void processClassResultSet(OrderedNode parentNode, ResultSet rs, XMLName classElementName, OrderInfo classElementOrder, ClassTableMap classTableMap)
      throws SQLException, MapException
   {
      Row         classRow;
      Node        realClassNode;
      OrderedNode classNode;
      long        orderValue;
      boolean     ascending;

      // Create a new row.

      classRow = new Row();

      // Process the result set.

      while (rs.next())
      {
         // Cache the row data so we can access it randomly

         populateRow(classTableMap.getTable(), classRow, rs);

         // Create an element node for the row, get the order information, and
         // insert the node into the parent node. An OrderedNode is returned.

         realClassNode = doc.createElementNS(classElementName.getURI(),
                                             classElementName.getQualifiedName());
         orderValue = getOrderValue(classRow, classElementOrder);
         ascending = getAscending(classElementOrder);
         classNode = parentNode.insertChild(classElementName, realClassNode, orderValue, ascending);

         // Process the columns in the row, then process the related tables
         // for the row.

         processColumns(classNode, classRow, classTableMap.getColumnMaps());
         processRelatedTables(classNode, classRow, classTableMap);

         // We are done processing this class. Clear the children in the ordered DOM
         // tree. (This doesn't affect the real DOM tree.)

         classNode.clearChildren();
      }
   }

   private void processColumns(OrderedNode classNode, Row classRow, Enumeration columnMaps)
      throws SQLException, MapException
   {
      ColumnMap   columnMap;
      OrderedNode parentNode;

      while (columnMaps.hasMoreElements())
      {
         // Get the next column map, add any inlined elements, and process the column.
         //
         // Inlined elements occur between the class (table) node and the property
         // (column) node. Column values are actually added as nodes of the lowest
         // level inlined element. If there are no inlined elements, the class node
         // is used.

         columnMap = (ColumnMap)columnMaps.nextElement();
         parentNode = addInlinedElements(classNode, classRow, columnMap.getElementInsertionList());
         processColumn(parentNode, classRow, columnMap);
      }
   }

   private void processColumn(OrderedNode parentNode, Row classRow, PropertyMapBase propMapBase)
      throws SQLException, MapException
   {
      // We use PropertyMapBase because this method is called from processColumns
      // using a ColumnMap and from processPropertyResultSet using a PropertyTableMap.
      // PropertyMapBase is the base class of both.

      String  value;
      long    orderValue;
      boolean ascending;
      XMLName name;

      // Get the data value. If the data value is a null reference, then the
      // corresponding column is NULL. In this case, we simply don't create
      // the element/attribute/PCDATA.

      value = getStringValue(classRow, propMapBase.getColumn());
      if (value == null) return;

      // Get the order value and whether the node is sorted in ascending or
      // descending order.

      orderValue = getOrderValue(classRow, propMapBase.getOrderInfo());
      ascending = getAscending(propMapBase.getOrderInfo());

      name = propMapBase.getXMLName();

      switch (propMapBase.getType())
      {
         case PropertyMapBase.ELEMENTTYPE:
            if (propMapBase.isTokenList())
            {
               addTokenToElement(parentNode, name, value, orderValue, ascending);
            }
            else
            {
               addElement(parentNode, name, value, orderValue, ascending, propMapBase.containsXML());
            }
            break;

         case PropertyMapBase.ATTRIBUTE:
            if (propMapBase.isTokenList())
            {
               addTokenToAttribute(parentNode, name, value);
            }
            else
            {
               addAttribute(parentNode, name, value);
            }
            break;

         case PropertyMapBase.PCDATA:
            if (propMapBase.isTokenList())
            {
               addTokenToPCDATA(parentNode, value, orderValue, ascending);
            }
            else
            {
               addPCDATA(parentNode, value, orderValue, ascending);
            }
            break;

         case PropertyMapBase.UNKNOWN:
            throw new MapException("Column is not mapped to an element type, attribute, or PCDATA: " + propMapBase.getColumn().getName());
      }
   }

   private void processRelatedTables(OrderedNode classNode, Row classRow, ClassTableMap classTableMap)
      throws SQLException, MapException
   {
      Enumeration          relatedClassTableMaps, propTableMaps;
      RelatedClassTableMap relatedClassTableMap;
      PropertyTableMap     propTableMap;

      // Process the related class tables.

      relatedClassTableMaps = classTableMap.getRelatedClassTableMaps();
      while (relatedClassTableMaps.hasMoreElements())
      {
         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.nextElement();
         processRelatedClassTable(classNode, classRow, relatedClassTableMap);
      }

      // Process the property tables.

      propTableMaps = classTableMap.getPropertyTableMaps();
      while (propTableMaps.hasMoreElements())
      {
         propTableMap = (PropertyTableMap)propTableMaps.nextElement();
         processPropertyTable(classNode, classRow, propTableMap);
      }
   }

   private void processRelatedClassTable(OrderedNode classNode, Row classRow, RelatedClassTableMap relatedClassTableMap)
      throws SQLException, MapException
   {
      OrderedNode   parentNode;
      ClassTableMap classTableMap;
      Table         table;
      Column[]      keyColumns;
      Object[]      keyValues;
      DataHandler   dataHandler;
      OrderInfo     orderInfo;
      ResultSet     rs;

      // Add any inlined elements between the class element and the elements
      // in the related class.

      parentNode = addInlinedElements(classNode, classRow, relatedClassTableMap.getElementInsertionList());

      // Get the key

      keyColumns = relatedClassTableMap.getLinkInfo().getParentKey().getColumns();
      keyValues = classRow.getColumnValues(keyColumns);

      // Get the DataHandler used by the table

      classTableMap = relatedClassTableMap.getClassTableMap();
      table = classTableMap.getTable();
      dataHandler = transferInfo.getDataHandler(table.getDatabaseName());

      // Get the result set over the related class table and process it.

      orderInfo = relatedClassTableMap.getOrderInfo();
      rs = dataHandler.select(table, keyValues, orderInfo);
      processClassResultSet(classNode,
                            rs,
                            relatedClassTableMap.getElementTypeName(),
                            orderInfo,
                            classTableMap);
      rs.close();
   }

   private void processPropertyTable(OrderedNode classNode, Row classRow, PropertyTableMap propTableMap)
      throws SQLException, MapException
   {
      OrderedNode parentNode;
      Table       table;
      Column[]    keyColumns;
      Object[]    keyValues;
      DataHandler dataHandler;
      OrderInfo   rsOrderInfo;
      ResultSet   rs;

      // Add any inlined elements between the class element and the property elements.

      parentNode = addInlinedElements(classNode, classRow, propTableMap.getElementInsertionList());

      // Get the key

      keyColumns = propTableMap.getLinkInfo().getParentKey().getColumns();
      keyValues = classRow.getColumnValues(keyColumns);

      // Get the DataHandler used by the table

      table = propTableMap.getTable();
      dataHandler = transferInfo.getDataHandler(table.getDatabaseName());

      // Get the result set over the property table and process it. Note that
      // how we sort the result set depends on whether we are processing a list
      // of token values or properties. If we are processing a token list, then
      // the normal OrderInfo gives us the position of the token-list element or
      // PCDATA in its parent. This value is retrieved by processColumn.

      rsOrderInfo = (propTableMap.isTokenList()) ? propTableMap.getTokenListOrderInfo() :
                                                   propTableMap.getOrderInfo();
      rs = dataHandler.select(table, keyValues, rsOrderInfo);
      processPropResultSet(parentNode, rs, propTableMap);
      rs.close();
   }

   private void processPropResultSet(OrderedNode parentNode, ResultSet rs, PropertyTableMap propTableMap)
      throws SQLException, MapException
   {
      Row  row = new Row();

      while (rs.next())
      {
         populateRow(propTableMap.getTable(), row, rs);
         processColumn(parentNode, row, propTableMap);
      }
   }

   // ************************************************************************
   // Helper methods -- general
   // ************************************************************************

   private void initGlobals(TransferInfo transferInfo)
   {
      // Set up the global variables

      this.transferInfo = transferInfo;
      map = transferInfo.getMap();
   }

   private OrderedNode initDoc(String[] wrapperURIs, String[] wrapperNames)
      throws ParserUtilsException
   {
      Node realRoot;

      // Create a new document.

      doc = utils.createDocument();

      // Add the wrapper elements, if any, and return an ordered node that will
      // serve as the root for the retrieved data.

      realRoot = addWrapperElements(wrapperURIs, wrapperNames);
      return new OrderedNode(realRoot, OrderInfo.UNORDERED, null);
   }

   // ************************************************************************
   // Helper methods -- inlined elements
   // ************************************************************************

   private Node addWrapperElements(String[] wrapperURIs, String[] wrapperNames)
   {
      NodeList wrapperList;
      Node     wrapper, newWrapper;
      String   uri;
      int      i;

      // If there are no wrapper element names, just return the document node.

      if (wrapperNames == null) return doc;

      // Check if the wrapper elements have already been added. If so, return
      // the lowest level wrapper. Note that we only need to get the first child
      // of each node, since the wrapper elements are a chain of single nodes.

      uri = (wrapperURIs == null) ? null : wrapperURIs[0];
      wrapperList = doc.getElementsByTagNameNS(uri, wrapperNames[0]);
      if (wrapperList.getLength() != 0)
      {
         wrapper = wrapperList.item(0);
         for (i = 1; i < wrapperNames.length; i++)
         {
            wrapper = wrapper.getFirstChild();
         }
         return wrapper;
      }

      // Add the wrapper elements.

      wrapper = doc;
      for (i = 0; i < wrapperNames.length; i++)
      {
         uri = (wrapperURIs == null) ? null : wrapperURIs[i];
         newWrapper = doc.createElementNS(uri, wrapperNames[i]);
         wrapper.appendChild(newWrapper);
         wrapper = newWrapper;
      }

      // Return the lowest level wrapper element.

      return wrapper;
   }

   private OrderedNode addInlinedElements(OrderedNode parentNode, Row row, ElementInsertionList list)
   {
      ElementInsertionMap insertionMap;

      if (list == null) return parentNode;

      for (int i = 0; i < list.size(); i++)
      {
         insertionMap = list.getElementInsertionMap(i);
         parentNode = addInlinedElement(parentNode, row, insertionMap);
      }

      return parentNode;
   }

   private OrderedNode addInlinedElement(OrderedNode parentNode, Row row, ElementInsertionMap insertionMap)
   {
      XMLName     elementTypeName;
      OrderedNode element;
      Node        realElement;
      OrderInfo   orderInfo;
      long        orderValue;
      boolean     ascending;

      elementTypeName = insertionMap.getElementTypeName();

      // Check if an element with this name has already been added to the
      // list. Because of the way maps and the DOM tree are constructed, we are
      // guaranteed that there is at most one element with this name.

      element = parentNode.getUniqueChild(elementTypeName);

      // If there is no element with this name, add one.

      if (element == null)
      {
         // Create the DOM node.

         realElement = doc.createElementNS(elementTypeName.getURI(),
                                           elementTypeName.getQualifiedName());

         // Get the ordering information and add the DOM node to the ordered DOM tree.

         orderInfo = insertionMap.getOrderInfo();
         orderValue = getOrderValue(row, orderInfo);
         ascending = getAscending(orderInfo);
         element = parentNode.insertChild(elementTypeName, realElement, orderValue, ascending);
      }

      // Return the OrderedNode element.

      return element;
   }

   // ************************************************************************
   // Helper methods -- adding data values
   // ************************************************************************

   private void addTokenToElement(OrderedNode parentNode, XMLName elementTypeName, String token, long orderValue, boolean ascending)
      throws SQLException
   {
      OrderedNode element;
      Text        realText;

      // Get the element node. Note that the rules for token list elements require
      // that there be at most one token list element with a given name in a group of
      // siblings.

      element = parentNode.getUniqueChild(elementTypeName);
      if (element == null)
      {
         // If the node doesn't exist, add it.

         addElement(parentNode, elementTypeName, token, orderValue, ascending, false);
      }
      else
      {
         // Get the text child of the element node. Since the element node contains only
         // one piece of text and we inserted that, the text child is the first (only) child.

         realText = (Text)element.realNode.getFirstChild();
         realText.appendData(SPACE);
         realText.appendData(token);
      }
   }

   private void addElement(OrderedNode parentNode, XMLName elementTypeName, String value, long orderValue, boolean ascending, boolean containsXML)
      throws SQLException
   {
      Element          realElement;
      Text             realText;
      DocumentFragment fragment;

      // Create a new element and add it to the real and ordered DOM trees.

      realElement = doc.createElementNS(elementTypeName.getURI(),
                                        elementTypeName.getQualifiedName());
      parentNode.insertChild(elementTypeName, realElement, orderValue, ascending);

      // If the column contains XML markup, wrap it in a fake start tag and parse it to
      // get a document fragment, which we then insert into the element. We need a wrapper
      // tag in case the column value is not well-formed, but don't care what it is since
      // appendChild ignores it.
      //
      // If the column does not contain XML markup, just add a text child to the element.

      if (containsXML)
      {
         try
         {
            fragment = fragmentBuilder.parse(doc, FAKESTARTTAG + value + FAKEENDTAG);
         }
         catch (Exception e)
         {
            throw new SQLException("[XML-DBMS]: Exception parsing column value: " + e.getMessage());
         }
         realElement.appendChild(fragment);
      }
      else
      {
         realText = doc.createTextNode(value);
         realElement.appendChild(realText);
      }
   }

   private void addTokenToAttribute(OrderedNode parentNode, XMLName attrName, String token)
   {
      Element realElement;
      Attr    realAttr;
      Text    realText;

      // Get the attribute to which to append the value. If it doesn't exist, create
      // it. Otherwise, append the new token to the existing value.

      realElement = (Element)(parentNode.realNode);
      realAttr = realElement.getAttributeNodeNS(attrName.getURI(), attrName.getLocalName());
      if (realAttr == null)
      {
         addAttribute(parentNode, attrName, token);
      }
      else
      {
         realText = (Text)realAttr.getFirstChild();
         realText.appendData(SPACE);
         realText.appendData(token);
      }
   }

   private void addAttribute(OrderedNode parentNode, XMLName attrName, String value)
   {
      Element realElement;
      Attr    realAttr;
      Text    realText;

      // Add a new attribute.

      realAttr = doc.createAttributeNS(attrName.getURI(), attrName.getQualifiedName());
      realText = doc.createTextNode(value);
      realAttr.appendChild(realText);

      realElement = (Element)(parentNode.realNode);
      realElement.setAttributeNodeNS(realAttr);
   }

   private void addTokenToPCDATA(OrderedNode parentNode, String token, long orderValue, boolean ascending)
   {
      OrderedNode text;

      // Get the PCDATA node. Note that the rules for token list PCDATA requires
      // that there be at most one token list PCDATA in a group of siblings.

      text = parentNode.getUniqueChild(PCDATA);
      if (text == null)
      {
         addPCDATA(parentNode, token, orderValue, ascending);
      }
      else
      {
         ((Text)text.realNode).appendData(SPACE);
         ((Text)text.realNode).appendData(token);
      }
   }

   private void addPCDATA(OrderedNode parentNode, String value, long orderValue, boolean ascending)
   {
      Text realText;

      realText = doc.createTextNode(value);
      parentNode.insertChild(PCDATA, realText, orderValue, ascending);
   }

   // ************************************************************************
   // Helper methods -- rows
   // ************************************************************************

   private String getStringValue(Row row, Column column)
      throws SQLException
   {
      Object          value;
      StringFormatter formatter;

      // Get the column value and check if it is null;

      value = row.getColumnValue(column);
      if (value == null) return null;

      // Format the column value with the column's formatter.

      formatter = column.getFormatter();
      try
      {
         return formatter.format(value);
      }
      catch (ConversionException e)
      {
         throw new SQLException("[XML-DBMS] Conversion error: " + e.getMessage());
      }
   }

   private boolean getAscending(OrderInfo orderInfo)
   {
      if (orderInfo == null) return false;
      return orderInfo.isAscending();
   }

   private long getOrderValue(Row row, OrderInfo orderInfo)
   {
      Object orderValue;

      // Return OrderInfo.UNORDERED if the node is not ordered or the
      // order column contains a null value. Otherwise, get the order value.

      if (orderInfo == null)
      {
         return OrderInfo.UNORDERED;
      }
      else if (orderInfo.orderValueIsFixed())
      {
         return orderInfo.getFixedOrderValue();
      }
      else
      {
         orderValue = row.getColumnValue(orderInfo.getOrderColumn());
         if (orderValue == null) return OrderInfo.UNORDERED;

         // 8/13/01, from Bryan Pendleton
         // Cast the returned value as Number, not Integer. This is necessary
         // because Oracle doesn't directly support the INTEGER SQL type and
         // returns integers as BigDecimals.

         return ((Number)orderValue).longValue();
      }
   }

   private void populateRow(Table table, Row row, ResultSet rs)
      throws SQLException
   {
      Column[] rsColumns;
      Object   o;

      row.clear();

      rsColumns = table.getResultSetColumns();
      for (int i = 0; i < rsColumns.length; i++)
      {
         // We use Table.getResultSetColumns() since this:
         // (a) Retrieves only the necessary columns (the result set might have more), and
         // (b) Retrieves the columns in ascending order, which is needed for interoperability.

         o = rs.getObject(rsColumns[i].getResultSetIndex());
         row.setColumnValue(rsColumns[i], o);

         if (rs.wasNull())
         {
            if (map.emptyStringIsNull())
            {
               // If empty strings are to be treated as NULLs, then
               // return an empty string, not a null.

               row.setColumnValue(rsColumns[i], EMPTYSTRING);
            }
            else
            {
               // This probably isn't necessary, but the JDBC spec
               // doesn't state what is returned if the column value was
               // NULL, even though most drivers probably return null.

               row.setColumnValue(rsColumns[i], null);
            }
         }
      }
   }

   // ************************************************************************
   // Inner class
   // ************************************************************************

   class OrderedNode
   {
      // This class wraps a DOM node, adding an order value. The order value gives
      // the order of the node in its parent. This allows us to construct an ordered
      // DOM tree parallel to the actual DOM tree. That is, the new DOM tree
      // contains node-by-node pointers to the real DOM tree, but also contains
      // order values at each node. Thus, we can insert a new node into both the
      // ordered tree and the real tree at a particular position, based on the
      // order value.
      //
      // In addition to a pointer to the real DOM node and an order value, each
      // OrderedNode contains a pointer to its next sibling and pointers to its
      // first ordered child and first unordered child. Unordered children appear
      // in the real DOM tree after the ordered children.
      //
      // When a new node is added, the code checks whether it is ordered or not.
      // If so, a linear search of the ordered nodes is done and the node is
      // inserted at the correct position in both the ordered and real DOM trees.
      // If not, it is simply appended to the end of the list of children in both trees.

      Node        realNode;
      long        orderValue;
      OrderedNode previousSibling;

      OrderedNode lastOrderedChild = null;
      OrderedNode firstUnorderedChild = null;

      Hashtable   children = new Hashtable();

      OrderedNode(Node realNode, long orderValue, OrderedNode previousSibling)
      {
         this.orderValue = orderValue;
         this.realNode = realNode;
         this.previousSibling = previousSibling;
      }

      void clearChildren()
      {
         // Use this method to prune the ordered tree when we're done with
         // a particular branch. This releases the ordered nodes in that
         // branch, thereby saving memory. It doesn't touch the real tree.

         lastOrderedChild = null;
         firstUnorderedChild = null;
         children.clear();
      }

      OrderedNode getUniqueChild(XMLName name)
      {
         // WARNING! This method is designed to be called only for inlined
         // elements and token list elements. The reason for this is that it
         // assumes there is at most one child element with a given name.
         // This assumption is true for inlined elements and token list elemnts,
         // but not for other elements.

         return (OrderedNode)children.get(name);
      }

      OrderedNode insertChild(XMLName name, Node realChild, long orderValue, boolean ascending)
      {
         // Insert a child in the correct position in both DOM trees and return
         // the OrderedNode. This code should(?) be rewritten to use a binary search.

         if (orderValue == OrderInfo.UNORDERED)
         {
            return insertUnorderedChild(name, realChild);
         }
         else
         {
            // If we are inserting children in descending order, flip the order value;

            if (!ascending) orderValue = -1 * orderValue;

            return insertOrderedChild(name, realChild, orderValue);
         }
      }

      private OrderedNode insertOrderedChild(XMLName name, Node realChild, long orderValue)
      {
         OrderedNode currentChild, savedChild, newChild;
         Node        nextRealNode;

         // Find the first ordered child with an order value that is greater
         // than the order value of the current search. We search in reverse
         // order because this is most efficient when children are passed to
         // us in ascending order. This will be true for the children that
         // correspond to rows in a table, which we sort in ascending order.
         // It will not be true for children that correspond to columns in a
         // class table, which will be passed to us in random order. Because
         // of the latter case, we should rewrite this code to use a binary search.
         //
         // (When we are processing descending order, the rows are sorted in
         // descending order, so multiplying the order value by -1 has the
         // effect of receiving rows in ascending order.)

         currentChild = lastOrderedChild;
         savedChild = firstUnorderedChild;
         while (currentChild != null)
         {
            if (orderValue < currentChild.orderValue)
            {
               // If the order value is less than the current order value,
               // continue searching.

               savedChild = currentChild;
               currentChild = currentChild.previousSibling;
            }
            else // if (orderValue >= currentChild.orderValue)
            {
               break;
            }
         }

         // Insert the real child in the DOM tree before the real node corresponding
         // to the child with the next higher order value. If there are no nodes
         // with a higher order value (as is the case when nodes are passed to us
         // in ascending order), insert the real child before the first unordered
         // child. If there are no unordered children, the effect is to append the
         // child to the end of the list. That is, insertBefore(node, null) is the
         // same as appendChild(node).

         nextRealNode = (savedChild == null) ? null : savedChild.realNode;
         realNode.insertBefore(realChild, nextRealNode);

         // Create a new node in the ordered DOM tree and insert it into the list
         // of ordered DOM children.

         newChild = new OrderedNode(realChild, orderValue, currentChild);
         if ((savedChild != null) && (savedChild != firstUnorderedChild))
         {
            savedChild.previousSibling = newChild;
         }
         if (lastOrderedChild == null)
         {
            lastOrderedChild = newChild;
         }

         // Add the child to the list of children. We only care about this for
         // inlined elements and token list elements, both of which are guaranteed
         // to have unique names in their parent.

         children.put(name, newChild);

         // Return the OrderedNode for the new child.

         return newChild;
      }

      private OrderedNode insertUnorderedChild(XMLName name, Node realChild)
      {
         OrderedNode newChild;

         // If the child is not ordered, then append it to the end of the list.

         realNode.appendChild(realChild);
         newChild = new OrderedNode(realChild, OrderInfo.UNORDERED, null);

         // Check if any unordered nodes have been added yet and, if not, set
         // firstUnorderedNode so we can add any ordered nodes before this.

         if (firstUnorderedChild == null)
         {
            firstUnorderedChild = newChild;
         }
         children.put(name, newChild);
         return newChild;
      }
   }
}