// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Changed populateRow to fix bug when retrieving data from result sets.
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;


import de.tudarmstadt.ito.domutils.DocumentFactoryException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import de.tudarmstadt.ito.domutils.ParserUtils;/**
 * Transfers data from the database to a DOM tree.
 *
 * <P>DBMSToDOM transfers data from the database to a DOM tree according
 * to a particular Map. The caller must provide a DocumentFactory for the
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
 *    DBMSToDOM dbmsToDOM = new DBMSToDOM(map, new DF_Oracle());<BR />
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
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class DBMSToDOM
{
   // ************************************************************************
   // Private variables
   // ************************************************************************

   private Map             map = null;
   private Document        doc;
   private ParserUtils factory = null;
   private boolean         usePrefixes = false;
   private Parameters      parameters;

   // We don't actually need the field position, but SimpleDateFormat
   // requires this annoying object, so here it is.

   private FieldPosition   pos = new FieldPosition(DateFormat.YEAR_FIELD);

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String XMLNS = "xmlns:";
   private static String EMPTYSTRING = "";

   // ************************************************************************
   // Constructors
   // ************************************************************************

   /** Construct a new DBMSToDOM object. */
   public DBMSToDOM()
   {
   }   



   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
	* Get the current Map.
	*
	* @return The current Map.
	*/
   public Map getMap()
   {
	  return map;
   }   

   /**
	* Set the current Map.
	*
	* @param map The current Map.
	*/
   public void setMap(Map map)
   {
	  this.map = map;
   }   





   /**
	* State whether element and attribute names should be prefixed according
	* to the namespace prefixes in the Map.
	*
	* @param usePrefixes Whether to use prefixes.
	*/
   public void usePrefixes(boolean usePrefixes)
   {
	  this.usePrefixes = usePrefixes;
   }   

   /**
	* Construct a DOM Document from a result set.
	*
	* <P>In the simplest case, this method simply constructs a DOM Document
	* from the result set. Depending on the Map, data may be retrieved from
	* subordinate tables as well. Note that the Map must map an element type
	* to a table named "Result Set".</P>
	*
	* <P>If the result set contains more than one row, the Map must specify
	* an ignored root type; otherwise, a DOMException is thrown.</P>
	*
	* <P>This method closes the result set.</P>
	*
	* @param rs The result set.
	* @exception DocumentFactoryException Thrown if an error occurs creating an
	*  empty Document.
	* @exception DOMException Thrown if a DOM error occurs. One possible cause
	*  of this is that the result set contains more than one row and there was
	*  no ignored root element type.
	* @exception InvalidMapException Thrown if the Map is not set or more than
	*  one ignored root type is specified.
	* @exception SQLException Thrown if an error occurs retrieving data from the
	*  database.
	*/
   public Document retrieveDocument(ResultSet rs)
	  throws InvalidMapException, SQLException, DocumentFactoryException
   {
	  RootTableMap rootTableMap;

	  initialize();

	  rootTableMap = map.getRootTableMap("Result Set");
	  processRootResultSet(rootTableMap, rs, rootTableMap.orderColumn, new Order());

	  addXMLNSAttrs();
	  return doc;
   }   

   /**
	* Construct a DOM Document from the specified tables.
	*
	* <P>In the simplest case, this method simply constructs a DOM Document
	* from the specified tables and rows. Depending on the Map, data may be
	* retrieved from subordinate tables as well.</P>
	*
	* <P>If more than one row is retrieved from the specified tables, the
	* Map must specify an ignored root type; otherwise, a DOMException is
	* thrown.</P>
	*
	* @param tableNames The names of the tables from which to retrieve data.
	* @param keys The keys used to retrieve data. There must be the same
	*  number of keys as tables.
	* @exception DocumentFactoryException Thrown if an error occurs creating an
	*  empty Document.
	* @exception DOMException Thrown if a DOM error occurs. One possible cause
	*  of this is that more than one row was retrieved from the specified tables
	*  and there was no ignored root element type.
	* @exception InvalidMapException Thrown if the Map is not set or more than
	*  one ignored root type is specified.
	* @exception SQLException Thrown if an error occurs retrieving data from the
	*  database.
	*/
   public Document retrieveDocument(String[] tableNames, Object[][] keys)
	  throws InvalidMapException, SQLException, DocumentFactoryException
   {
	  Order order = new Order();

	  initialize();

	  for (int i = 0; i < tableNames.length; i++)
	  {
		 retrieveTableData(tableNames[i], keys[i], order);
	  }

	  addXMLNSAttrs();
	  return doc;
   }   

   /**
	* Construct a DOM Document from the specified table.
	*
	* <P>In the simplest case, this method simply constructs a DOM Document
	* from the row(s) in the specified table. Depending on the Map, data may
	* be retrieved from subordinate tables as well.</P>
	*
	* <P>If more than one row is retrieved from the specified table, the Map
	* must specify an ignored root type; otherwise, a DOMException is
	* thrown.</P>
	*
	* @param tableName The name of the table from which to retrieve data.
	* @param key The key used to retrieve data.
	* @exception DocumentFactoryException Thrown if an error occurs creating an
	*  empty Document.
	* @exception DOMException Thrown if a DOM error occurs. One possible cause
	*  of this is that more than one row retrieved from the specified table
	*  and there was no ignored root element type.
	* @exception InvalidMapException Thrown if the Map is not set or more than
	*  one ignored root type is specified.
	* @exception SQLException Thrown if an error occurs retrieving data from the
	*  database.
	*/
   public Document retrieveDocument(String tableName, Object[] key)
	  throws DOMException, InvalidMapException, SQLException, DocumentFactoryException
   {
	  initialize();
	  retrieveTableData(tableName, key, new Order());
	  addXMLNSAttrs();
	  return doc;
   }   

   /**
	* Construct a DOM Document according to the information in a DocumentInfo
	* object.
	*
	* <P>In the simplest case, this method simply constructs a DOM Document
	* from the row(s) specified in the DocumentInfo object. Depending on the
	* Map, data may be retrieved from subordinate tables as well.</P>
	*
	* <P>If more than one row is retrieved from the tables specified in the
	* DocumentInfo object, the Map must specify an ignored root type;
	* otherwise, a DOMException is thrown.</P>
	*
	* @param docInfo The DocumentInfo specifying which rows to retrieve.
	* @exception DocumentFactoryException Thrown if an error occurs creating an
	*  empty Document.
	* @exception DOMException Thrown if a DOM error occurs. One possible cause
	*  of this is that more than one row retrieved from the specified tables
	*  and there was no ignored root element type.
	* @exception InvalidMapException Thrown if the Map is not set or more than
	*  one ignored root type is specified.
	* @exception SQLException Thrown if an error occurs retrieving data from the
	*  database.
	*/
   public Document retrieveDocument(DocumentInfo docInfo)
	  throws DOMException, InvalidMapException, SQLException, DocumentFactoryException
   {
	  RootTableMap      rootTableMap;
	  TableMap          tableMap = null;
	  Column[]          valueCols = null;
	  Column            orderCol = null;
	  PreparedStatement select = null;
	  ResultSet         rs;
	  Order             order = new Order();

	  // Initialize the DBMSToDOM object.
	  initialize();

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
	  map.checkInSelectStmt(select);

	  // Add xmlns attributes to the root element and return the DOM Document.
	  addXMLNSAttrs();
	  return doc;
   }   

   // ************************************************************************
   // Result set processing methods
   // ************************************************************************

   void retrieveTableData(String tableName, Object[] key, Order order)
	  throws DOMException, InvalidMapException, SQLException, DocumentFactoryException
   {
	  RootTableMap      rootTableMap;
	  PreparedStatement select;
	  ResultSet         rs;

	  // Get the root table map for the table, construct a result set
	  // over it, and process the result set.

	  rootTableMap = map.getRootTableMap(tableName);
	  select = map.checkOutSelectStmt(rootTableMap.tableMap.table,
									  rootTableMap.candidateKey,
									  rootTableMap.orderColumn);
	  
	 
	  
	  parameters.setParameters(select, key, rootTableMap.candidateKey);
	  rs = select.executeQuery();
	  processRootResultSet(rootTableMap, rs, rootTableMap.orderColumn, order);
	  map.checkInSelectStmt(select);
   }            

   void processRootResultSet(RootTableMap rootTableMap, ResultSet rs, Column orderColumn, Order parentOrder)
	  throws DOMException, InvalidMapException, SQLException
   {
	  Node    parent;
	  Row     row = new Row(rootTableMap.tableMap.table);
//      boolean firstRow = true;

	  // Add the ignored root element, if any.
	  parent = addIgnoredRoot(rootTableMap);

	  // Process the root result set.
	  processClassResultSet(parent, rootTableMap.tableMap, rs, orderColumn, parentOrder);
   }   

   void processClassResultSet(Node parent, TableMap rsMap, ResultSet rs, Column orderColumn, Order parentOrder)
	  throws DOMException, SQLException, InvalidMapException
   {
	  // Process a result set created over a class table.

	  Row    row = new Row(rsMap.table);
	  Node   child;
	  Order  childOrder = new Order();
	  String elementType;

	  // We currently don't support pass-through elements. However, this will
	  // be the place to add them in the future.
	  // parent = addPassThroughElements(parent, rsMap);

	  while (rs.next())
	  {
		 // Cache the row data so we can access it randomly

		 // 5/29/00, Ronald Bourret
		 // Passed table to populateRow.

		 populateRow(rsMap.table, row, rs);

		 // Create an element node for the row and insert it into the
		 // parent node.

		 elementType = (usePrefixes) ? rsMap.prefixedElementType : rsMap.elementType;
		 child = (Node)doc.createElement(elementType);
		 parentOrder.insertChild(parent, child, getOrderValue(row, orderColumn));

		 // Process the columns in the row, then process the related tables
		 // for the row.

		 childOrder.clear();
		 processColumns(row, rsMap.columnMaps, child, childOrder);
		 processRelatedTables(row, rsMap, child, childOrder);
	  }
	  rs.close();
   }   

   void processColumns(Row row, ColumnMap[] columnMaps, Node parent, Order parentOrder)
   {
	  for (int i = 0; i < columnMaps.length; i++)
	  {
		 processColumn(row, columnMaps[i], parent, parentOrder);
	  }
   }   

   void processColumn(Row row, ColumnMap columnMap, Node parent, Order parentOrder)
   {
	  String dataValue, property;
	  int    orderValue;
	  Node   child, pcdata;

	  // Get the data value. If the data value is a null reference, then the
	  // corresponding column is NULL. In this case, we simply don't create
	  // the element/attribute/PCDATA.

	  dataValue = getDataValue(row, columnMap.column);
	  if (dataValue == null) return;

	  orderValue = getOrderValue(row, columnMap.orderColumn);

	  switch (columnMap.type)
	  {
		 case ColumnMap.TYPE_TOELEMENTTYPE:
			property = (usePrefixes) ? columnMap.prefixedProperty : columnMap.property;
			child = (Node)doc.createElement(property);
			parentOrder.insertChild(parent, child, orderValue);
			pcdata = NodeExpander.expandNode((Node)doc.createTextNode(dataValue));
			child.appendChild(pcdata);
			break;

		 case ColumnMap.TYPE_TOATTRIBUTE:
			// Set the attribute. Note that if the attribute is multi-valued, we
			// get the current attribute value first, then append the new value
			// to it. Because multi-valued attributes must be stored in a
			// property table, we don't need to worry about the order column --
			// the result set over the property table is already sorted.
			property = (usePrefixes) ? columnMap.prefixedProperty : columnMap.property;
			if (!columnMap.multiValued)
			{
			   ((Element)parent).setAttribute(property, dataValue);
			}
			else
			{
			   String s = ((Element)parent).getAttribute(property);
			   StringBuffer sb = new StringBuffer (s);
			   if (sb.length() != 0)
			   {
				  sb.append(" ");
			   }
			   sb.append(dataValue);
			   ((Element)parent).setAttribute(property, sb.toString());
			}
			break;

		 case ColumnMap.TYPE_TOPCDATA:
			pcdata = doc.createTextNode(dataValue);
			parentOrder.insertChild(parent, pcdata, orderValue);
			break;
	  }
   }   

   void processRelatedTables(Row row, TableMap rsMap, Node parent, Order parentOrder)
	  throws SQLException, InvalidMapException
   {
	  TableMap          relatedTableMap;
	  PreparedStatement select;
	  ResultSet         rs;

	  // Process the related tables
	  for (int i = 0; i < rsMap.relatedTables.length; i++)
	  {
		 relatedTableMap = rsMap.relatedTables[i];
		 select = map.checkOutSelectStmt(rsMap.table.number, i);
		 parameters.setParameters(select, row, rsMap.parentKeys[i]);
		 rs = select.executeQuery();

		 if (relatedTableMap.type == TableMap.TYPE_CLASSTABLE)
		 {
			processClassResultSet(parent, relatedTableMap, rs, rsMap.orderColumns[i], parentOrder);
		 }
		 else // if (relatedTableMap.type == TableMap.TYPE_PROPERTYTABLE)
		 {
			processPropResultSet(parent, relatedTableMap, rs, parentOrder);
		 }

		 rs.close();
		 map.checkInSelectStmt(select, rsMap.table.number, i);
	  }
   }   

   void processPropResultSet(Node parent, TableMap rsMap, ResultSet rs, Order parentOrder)
	  throws SQLException
   {
	  Node element;
	  Row  row = new Row(rsMap.table);

	  while (rs.next())
	  {
		 // 5/29/00, Ronald Bourret
		 // Passed table to populateRow.

		 populateRow(rsMap.table, row, rs);
		 processColumns(row, rsMap.columnMaps, parent, parentOrder);
	  }
	  rs.close();
   }   

   // ************************************************************************
   // Helper methods
   // ************************************************************************

   void initialize() throws InvalidMapException, DocumentFactoryException
   {
	  if (map == null) throw new InvalidMapException("Map not set.");
	  if (factory == null) throw new DocumentFactoryException("Document factory not set.");
	  doc = factory.createDocument();
	  parameters = new Parameters(map.dateFormatter, map.timeFormatter, map.timestampFormatter);
   }   

   Node addIgnoredRoot(RootTableMap rootMap)
	  throws InvalidMapException
   {
	  String ignoredRootType;
	  Node   ignoredRoot;

	  // If there is no ignored root element, simply return the current
	  // root element node.

	  if (rootMap.ignoredRootType == null) return (Node)doc;

	  // Check if there already is an ignored root element and, if so, that its
	  // name matches the ignored root type in the current map.

	  ignoredRootType = (usePrefixes) ? rootMap.prefixedIgnoredRootType: rootMap.ignoredRootType;
	  ignoredRoot = doc.getDocumentElement();
	  if (ignoredRoot == null)
	  {
		 ignoredRoot = (Node)doc.createElement(ignoredRootType);
		 doc.appendChild(ignoredRoot);
	  }
	  else if (ignoredRoot.getNodeName() != ignoredRootType)
	  {
		 throw new InvalidMapException("More than one ignored root element type specified: " + ignoredRoot.getNodeName() + " and " + ignoredRootType);
	  }

	  return ignoredRoot;
   }   

   void addXMLNSAttrs() throws DOMException
   {
	  Element root;

	  if ((!usePrefixes) || (map.prefixes == null)) return;

	  root = doc.getDocumentElement();
	  for (int i = 0; i < map.prefixes.length; i++)
	  {
		 root.setAttribute(XMLNS + map.prefixes[i], map.uris[i]);
	  }
   }   

   String getDataValue(Row row, Column column)
   {
	  Object value;

	  if (row.isNull(column)) return null;

	  value = row.getColumnValue(column);

	  // We want to format dates, times, and timestamps according to the
	  // formatting information, so we need to check the Object type. We
	  // check for String first, since this should have a high hit rate.
	  // Next we check for Date, Time, and Timestamp, which require
	  // special processing. For everything else, we call toString().

	  if (value instanceof String)
		 return (String)value;
	  else if (value instanceof Date)
		 return getFormattedDate(map.dateFormatter, (java.util.Date)value);
	  else if (value instanceof Time)
		 return getFormattedDate(map.timeFormatter, (java.util.Date)value);
	  else if (value instanceof Timestamp)
		 return getFormattedDate(map.timestampFormatter, (java.util.Date)value);
	  else
		 return value.toString();
   }   

   int getOrderValue(Row row, Column orderColumn)
   {
	  // Return -1 if the order column doesn't exist or contains a null
	  // value. Otherwise, get the order value.

	  if (orderColumn == null) return -1;
	  if (row.isNull(orderColumn)) return -1;
	  return ((Integer)row.getColumnValue(orderColumn)).intValue();
   }   

   void populateRow(Table table, Row row, ResultSet rs)
	  throws SQLException
   {
	  for (int i = 0; i < row.columnValues.length; i++)
	  {
		 // 5/59/00, Ronald Bourret
		 // Added table to parameter list, changed for loop to 0-based,
		 // and changed call to getObject to use Table.rsColumnNumbers.
		 // This is needed when the result set has more columns than are
		 // mapped. Note that columns in the result set are accessed in
		 // ascending order. See comments in Table and Column.
		 //
		 // Note that we use the Row.columnValues array here directly instead
		 // of calling row.setColumnValue. This is done for speed.

		 row.columnValues[i] = rs.getObject(table.rsColumnNumbers[i]);

		 if (rs.wasNull())
		 {
			if (map.emptyStringIsNull)
			{
			   // If empty strings are to be treated as NULLs, then
			   // return an empty string, not a null.

			   row.columnValues[i] = EMPTYSTRING;
			}
			else
			{
			   // This probably isn't necessary, but the JDBC spec
			   // doesn't state what is returned if the column value was
			   // NULL, even though most drivers probably return null.

			   row.columnValues[i] = null;
			}
		 }
	  }
   }   

   // ************************************************************************
   // Utility functions
   // ************************************************************************

   private String getFormattedDate(DateFormat formatter, java.util.Date date)
   {
	  StringBuffer s;

	  if (formatter instanceof SimpleDateFormat)
	  {
		 s = new StringBuffer();
		 return ((SimpleDateFormat)formatter).format(date, s, pos).toString();
	  }
	  else // if (formatter instanceof DateFormat)
	  {
		 return formatter.format(date);
	  }
   }   

   // ************************************************************************
   // Inner class
   // ************************************************************************

   class Order
   {
	  // This class contains information about the order in which the children
	  // of a given parent are stored. The information is stored as a simple
	  // linked list which contains the order value of and a pointer to each
	  // ordered child. Unordered children are stored beneath the parent after
	  // all ordered children. Note that adding an unordered child is fast,
	  // while adding ordered children is likely to be slow.

	  OrderNode start = null;
	  Node      firstUnorderedChild = null;

	  class OrderNode
	  {
		 int       orderValue;
		 Node      node;
		 OrderNode next;

		 OrderNode(int orderValue, Node node, OrderNode next)
		 {
			this.orderValue = orderValue;
			this.node = node;
			this.next = next;
		 }
	  }

	  void clear()
	  {
		 start = null;
		 firstUnorderedChild = null;
	  }

	  void insertChild(Node parent, Node child, int orderValue)
	  {
		 // Insert a child in the correct position in its parent. This code
		 // really ought to be rewritten to use a binary search.

		 OrderNode current, save, newOrderNode;

		 // If the child is not ordered, then save it as the last child. If this
		 // is the first unordered child, save it so we can place ordered
		 // children before it.

		 if (orderValue == -1)
		 {
			parent.appendChild(child);
			if (firstUnorderedChild == null)
			{
			   firstUnorderedChild = child;
			}
			return;
		 }

		 // Insert the child before the first node with a higher order value.
		 // This is efficient if the children are added in reverse order
		 // (highest order first), which is easy to do for children 
		 // corresponding to entire rows in class or columns in property tables
		 // because we can sort the table on a single column. It is very
		 // inefficient for children added in random order, such as those
		 // corresponding to columns in a class table, which are accessed from
		 // first column to last column.

		 current = start;
		 save = null;
		 while (current != null)
		 {
			if (orderValue > current.orderValue)
			{
			   save = current;
			   current = current.next;
			}
			else // if (orderValue <= current.orderValue)
			{
			   // Insert the child and update the linked list of order info.

			   parent.insertBefore(child, current.node);
			   newOrderNode = new OrderNode(orderValue, child, current);
			   if (save == null)
			   {
				  start = newOrderNode;
			   }
			   else
			   {
				  save.next = newOrderNode;
			   }
			   return;
			}
		 }

		 // If the order value is greater than the order values of all current
		 // children, insert the child after the ordered children and before the
		 // unordered children.

		 if (current == null)
		 {
			newOrderNode = new OrderNode(orderValue, child, current);
			parent.insertBefore(child, firstUnorderedChild);
			if (start == null)
			{
			   start = newOrderNode;
			}
			else
			{
			   save.next = newOrderNode;
			}
		 }
	  }
   }
   /**
	* Construct a new DBMSToDOM object and set the Map and
	* DocumentFactory objects.
	*/
   public DBMSToDOM(Map map, ParserUtils factory)
   {
	  this.map = map;
	  this.factory = factory;
   }         /**
	* Get the current DocumentFactory.
	*
	* @return The current DocumentFactory.
	*/
   public ParserUtils getDocumentFactory()
   {
	  return factory;
   }         /**
	* Set the current DocumentFactory.
	*
	* @param factory The current DocumentFactory.
	*/
   public void setDocumentFactory(ParserUtils factory)
   {
	  this.factory = factory;
   }      }