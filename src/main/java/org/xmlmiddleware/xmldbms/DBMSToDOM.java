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

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.filters.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmlutils.*;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Transfers data from the database to a DOM tree.
 *
 * <p>DBMSToDOM transfers data from the database to a DOM tree according
 * to a particular XMLDBMSMap and FilterSet. The map provides the general structural
 * mapping from the database to XML; the filters specify exactly which data
 * are to be transferred. In addition, DBMSToDOM can transfer data from
 * application-constructed result sets to XML.</p>
 *
 * <p>For example, the following code transfers data according to the map
 * document orders.map and the filter document ordersbynumber.ftr, which
 * uses sales order number as a parameter:</p>
 *
 * <pre>
 *    // Create the XMLDBMSMap object with a user-defined function.
 *    <br />
 *    map = createMap("orders.map");
 *    <br />
 *    // Create the FilterSet object with a user-defined function.
 *    <br />
 *    filterSet = createFilterSet(map, "ordersbynumber.ftr");
 *    <br />
 *    // Create a new DBMSToDOM object that uses the Xerces parser.
 *    <br />
 *    dbmsToDOM = new DBMSToDOM(new ParserUtilsXerces());
 *    <br />
 *    // Create a data source and build a database-enabled map.
 *    <br />
 *    ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:xmldbms");
 *    dbMap = new DBEnabledMap(map, null, ds, "ron", "passwd");
 *    <br />
 *    // Build the parameters hashtable.
 *    <br />
 *    params = new Hashtable();
 *    params.put("$Number", "123");
 *    <br />
 *    // Call retrieveDocument to transfer the data.
 *    <br />
 *    doc = dbmsToDOM.retrieveDocument(dbMap, filterSet, params, null);
 * </pre>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class DBMSToDOM
{
   // ************************************************************************
   // Private variables
   // ************************************************************************

   // Per-execution globals

   private XMLDBMSMap   map;
   private FilterBase   filterBase;
   private Document     doc;
   private DBEnabledMap dbMap;

   // Truly global globals

   // 8/01 Adam Flinton
   // Replaced DocumentFactory with ParserUtils.

   private ParserUtils     utils;
   private FragmentBuilder fragmentBuilder;
   private String          publicID;
   private String          systemID;

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String EMPTYSTRING = "";
   private static String SPACE = " ";
   private static String FAKESTARTTAG = "<fake>";
   private static String FAKEENDTAG = "</fake>";
   private static String XMLNS = "xmlns:";
   private static String XMLNSURI = "http://www.w3.org/2000/xmlns/";

   private static final XMLName PCDATA = XMLName.create(null, "#PCDATA");

   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
    * Construct a new DBMSToDOM object.
    *
    * @param utils A ParserUtils object.
    * @exception SAXException Thrown if the XMLReader does not support namespaces.
    * @exception XMLMiddlewareException Thrown if an error occurs instantiating the XMLReader.
    */
   // 8/01 Adam Flinton
   // Replaced DocumentFactory with ParserUtils.
   public DBMSToDOM(ParserUtils utils)
      throws SAXException, XMLMiddlewareException
   {
      this.utils = utils;
      fragmentBuilder = new FragmentBuilder(utils.getXMLReader(false));
   }         

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Set the system and public IDs to use in the DOCTYPE statement.
    *
    * @param systemID The system ID. Non-null if the public ID is non-null. Otherwise,
    *    may be null.
    * @param publicID The public ID. May be null.
    */
   public void setDTDInfo(String systemID, String publicID)
   {
      if ((publicID != null) && (systemID == null))
         throw new IllegalArgumentException("If the public ID is non-null, the system ID must also be non-null.");
      this.systemID = systemID;
      this.publicID = publicID;
   }

   /**
    * Retrieve a document based on the specified filters.
    *
    * <p>The filter set must contain at least one root filter.</p>
    *
    * @param dbMap A database-enabled map.
    * @param filterSet The filter set specifying the data to retrieve.
    * @param params A Hashtable containing the names (keys) and values (elements) of
    *    any parameters used in the filters. Null if there are no parameters.
    * @param rootNode The Node to which the retrieved document is to be added. If this is
    *    null, retrieveDocument will create a new Document.
    * @return The document. If rootNode is not null, this is the owner Document.
    *    Otherwise, it is a new Document.
    * @exception SQLException Thrown if a database error occurs retrieving data
    * @exception XMLMiddlewareException Thrown if an XML-DBMS error occurs retrieving data.
    */
   public Document retrieveDocument(DBEnabledMap dbMap, FilterSet filterSet, Hashtable params, Node rootNode)
      throws SQLException, XMLMiddlewareException
   {
      Hashtable resultSets = new Hashtable();

      return retrieveDocument(dbMap, resultSets, filterSet, params, rootNode);
   }

   /**
    * Retrieve a document based on a result set.
    *
    * <p>The filter set must contain exactly one result set filter.</p>
    *
    * @param dbMap A database-enabled map.
    * @param rs The result set. The filter for the result set must use the name "Default".
    * @param filterSet The filter set specifying the data to retrieve.
    * @param params A Hashtable containing the names (keys) and values (elements) of
    *    any parameters used in the filters. Null if there are no parameters.
    * @param rootNode The Node to which the retrieved document is to be added. If this is
    *    null, retrieveDocument will create a new Document.
    * @return The document. If rootNode is not null, this is the owner Document.
    *    Otherwise, it is a new Document.
    * @exception SQLException Thrown if a database error occurs retrieving data
    * @exception XMLMiddlewareException Thrown if an XML-DBMS error occurs retrieving data.
    */
   public Document retrieveDocument(DBEnabledMap dbMap, ResultSet rs, FilterSet filterSet, Hashtable params, Node rootNode)
      throws SQLException, XMLMiddlewareException
   {
      Hashtable resultSets = new Hashtable();

      resultSets.put("Default", rs);
      return retrieveDocument(dbMap, resultSets, filterSet, params, rootNode);
   }

   /**
    * Retrieve a document based on the specified result sets.
    *
    * <p>The filter set must contain at least one root filter.</p>
    *
    * @param dbMap A database-enabled map.
    * @param resultSets An hashtable containing result sets keyed by result set name.
    *    There must be one result set for each result set filter in the filter set.
    * @param filterSet The filter set specifying the data to retrieve.
    * @param params A Hashtable containing the names (keys) and values (elements) of
    *    any parameters used in the filters. Null if there are no parameters.
    * @param rootNode The Node to which the retrieved document is to be added. If this is
    *    null, retrieveDocument will create a new Document.
    * @return The document. If rootNode is not null, this is the owner Document.
    *    Otherwise, it is a new Document.
    * @exception SQLException Thrown if a database error occurs retrieving data
    * @exception XMLMiddlewareException Thrown if an XML-DBMS error occurs retrieving data.
    */
   public Document retrieveDocument(DBEnabledMap dbMap, Hashtable resultSets, FilterSet filterSet, Hashtable params, Node rootNode)
      throws SQLException, XMLMiddlewareException
   {
      OrderedNode     orderedRootNode;
      Vector          filters;

      // Initialize the per-execution global variables.

      initGlobals(dbMap);

      // Check that we have at least one filter

      filters = filterSet.getFilters();
      if (filters.size() < 1)
         throw new IllegalArgumentException("You must specify at least one filter.");

      // Set the filter parameters.

      filterSet.setFilterParameters(params);

      // Get an OrderedNode over the Node to which elements will be added.

      orderedRootNode = getOrderedRootNode(rootNode, filterSet);

      // Retrieve the data and add it to the root node.

      retrieveData(filters, resultSets, orderedRootNode);

      // Add namespace declarations to the children of the real root node. We add these
      // to the children instead of the root for two reasons. First, the root might be
      // a Document node. Second, if the root is a wrapper element, we don't want to
      // collide with the wrapper element namespaces.

      addNamespaceDeclsToChildren(orderedRootNode.realNode, map.getNamespaceURIs());

      // Reset the per-execution global variables.

      resetGlobals();

      // Return the document.

      return doc;
   }

/*
   public void setDBErrorHandling??
   public SQLException getSQLExceptions??
   public SQLException getSQLWarnings??
*/
   // ************************************************************************
   // Helper methods for getting started
   // ************************************************************************

   private void retrieveData(Vector filters, Hashtable resultSets, OrderedNode orderedRootNode)
      throws SQLException, XMLMiddlewareException
   {
      Object          filter;
      ResultSetFilter rsFilter;
      String          rsName;
      ResultSet       rs;

      // Retrieve the data.

      for (int i = 0; i < filters.size(); i++)
      {
         // Get the next filter and set the filterBase global. filterBase
         // parallels map in that it contains filters for all class tables.

         filter = filters.elementAt(i);
         filterBase = (FilterBase)filter;
         if (filter instanceof RootFilter)
         {
            retrieveRootTableData(orderedRootNode, (RootFilter)filter);
         }
         else // if (filter instanceof ResultSetFilter)
         {
            rsFilter = (ResultSetFilter)filter;
            rsName = rsFilter.getResultSetName();
            rs = (ResultSet)resultSets.get(rsName);
            if (rs == null)
               throw new IllegalArgumentException("No result set found for the result set name: " + rsName);
            retrieveResultSetData(orderedRootNode, rsFilter, rs);
         }
      }
   }

   private void retrieveRootTableData(OrderedNode rootNode, RootFilter rootFilter)
      throws SQLException, XMLMiddlewareException
   {
      Table            rootTable;
      ClassTableMap    rootTableMap;
      FilterConditions rootConditions;
      DataHandler      dataHandler;
      String           where;
      Vector           columns, params;
      ResultSet        rs;

      // Get the root table and the map for it.

      rootConditions = rootFilter.getRootFilterConditions();
      rootTable = rootConditions.getTable();
      rootTableMap = map.getClassTableMap(rootTable);

      // Get a DataHandler

      dataHandler = dbMap.getDataHandler(rootTable.getDatabaseName());
      where = rootConditions.getWhereCondition();
      columns = rootConditions.getColumns();
      params = rootConditions.getParameterValues();

      // Construct a result set based on the filter conditions and process it.

      rs = dataHandler.select(rootTable, null, null, where, columns, params, null);
      processClassResultSet(rootNode, rs, rootTableMap.getElementTypeName(), null, rootTableMap);
      rs.close();
   }

   private void retrieveResultSetData(OrderedNode rootNode, ResultSetFilter rsFilter, ResultSet rs)
      throws SQLException, XMLMiddlewareException
   {
      ClassTableMap rootTableMap;

      rootTableMap = map.getClassTableMap(rsFilter.getDatabaseName(), rsFilter.getCatalogName(), rsFilter.getSchemaName(), rsFilter.getTableName());
      processClassResultSet(rootNode, rs, rootTableMap.getElementTypeName(), null, rootTableMap);
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
      throws SQLException, XMLMiddlewareException
   {
      Row         classRow;
      Table       table;
      Node        realClassNode;
      OrderedNode classNode;
      long        orderValue;
      boolean     ascending;
      TableFilter classTableFilter;

      // Create a new row.

      classRow = new Row();
      table = classTableMap.getTable();

      // Process the result set.

      while (rs.next())
      {
         // Cache the row data so we can access it randomly

         classRow.removeAllColumnValues();
         classRow.setColumnValues(rs, table, map.emptyStringIsNull());

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

         classTableFilter = filterBase.getTableFilter(table.getDatabaseName(), table.getCatalogName(), table.getSchemaName(), table.getTableName());
         processRelatedTables(classNode, classRow, classTableMap, classTableFilter);

         // We are done processing this class. Clear the children in the ordered DOM
         // tree. (This doesn't affect the real DOM tree.)

         classNode.clearChildren();
      }
   }

   private void processColumns(OrderedNode classNode, Row classRow, Enumeration columnMaps)
      throws SQLException, XMLMiddlewareException
   {
      ColumnMap   columnMap;
      OrderedNode parentNode;

      while (columnMaps.hasMoreElements())
      {
         // Get the next column map, add any inlined elements, and process the column.
         // Note that we do not process columns for which the type is Types.NULL. These
         // occur when a result set is passed to retrieveDocument and the map maps more
         // columns not found in the result set.
         //
         // Inlined elements occur between the class (table) node and the property
         // (column) node. Column values are actually added as nodes of the lowest
         // level inlined element. If there are no inlined elements, the class node
         // is used.

         columnMap = (ColumnMap)columnMaps.nextElement();
         if (columnMap.getColumn().getType() == Types.NULL) continue;
         parentNode = addInlinedElements(classNode, classRow, columnMap.getElementInsertionList());
         processColumn(parentNode, classRow, columnMap);
      }
   }

   private void processColumn(OrderedNode parentNode, Row classRow, PropertyMapBase propMapBase)
      throws SQLException, XMLMiddlewareException
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
            throw new XMLMiddlewareException("Column is not mapped to an element type, attribute, or PCDATA: " + propMapBase.getColumn().getName());
      }
   }

   private void processRelatedTables(OrderedNode classNode, Row classRow, ClassTableMap classTableMap, TableFilter classTableFilter)
      throws SQLException, XMLMiddlewareException
   {
      Enumeration          relatedClassTableMaps, propTableMaps;
      RelatedClassTableMap relatedClassTableMap;
      PropertyTableMap     propTableMap;
      RelatedTableFilter   relatedTableFilter;

      // Process the related class tables.

      relatedClassTableMaps = classTableMap.getRelatedClassTableMaps();
      while (relatedClassTableMaps.hasMoreElements())
      {
         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.nextElement();
         relatedTableFilter = (classTableFilter == null) ?
                               null :
                               classTableFilter.getRelatedTableFilter(relatedClassTableMap);
         processRelatedClassTable(classNode, classRow, relatedClassTableMap, relatedTableFilter);
      }

      // Process the property tables.

      propTableMaps = classTableMap.getPropertyTableMaps();
      while (propTableMaps.hasMoreElements())
      {
         propTableMap = (PropertyTableMap)propTableMaps.nextElement();
         relatedTableFilter = (classTableFilter == null) ?
                               null :
                               classTableFilter.getRelatedTableFilter(propTableMap);
         processPropertyTable(classNode, classRow, propTableMap, relatedTableFilter);
      }
   }

   private void processRelatedClassTable(OrderedNode classNode, Row classRow, RelatedClassTableMap relatedClassTableMap, RelatedTableFilter relatedTableFilter)
      throws SQLException, XMLMiddlewareException
   {
      OrderedNode   parentNode;
      ClassTableMap childClassTableMap;
      Table         childTable;
      LinkInfo      linkInfo;
      Vector        keyValue;
      Key           childKey;
      DataHandler   dataHandler;
      OrderInfo     orderInfo;
      String        where = null;
      Vector        columns = null;
      Vector        params = null;
      ResultSet     rs;

      // Add any inlined elements between the class element and the elements
      // in the related class.

      parentNode = addInlinedElements(classNode, classRow, relatedClassTableMap.getElementInsertionList());

      // Get the key values

      linkInfo = relatedClassTableMap.getLinkInfo();
      keyValue = classRow.getColumnValues(linkInfo.getParentKey().getColumns());
      childKey = linkInfo.getChildKey();

      // Get the DataHandler used by the table

      childClassTableMap = relatedClassTableMap.getClassTableMap();
      childTable = childClassTableMap.getTable();
      dataHandler = dbMap.getDataHandler(childTable.getDatabaseName());

      // Get the result set over the related class table and process it.

      if (relatedTableFilter != null)
      {
         where = relatedTableFilter.getWhereCondition();
         columns = relatedTableFilter.getColumns();
         params = relatedTableFilter.getParameterValues();
      }
      orderInfo = relatedClassTableMap.getOrderInfo();

      rs = dataHandler.select(childTable, childKey, keyValue, where, columns, params, orderInfo);
      processClassResultSet(parentNode,
                            rs,
                            relatedClassTableMap.getElementTypeName(),
                            orderInfo,
                            childClassTableMap);
      rs.close();
   }

   private void processPropertyTable(OrderedNode classNode, Row classRow, PropertyTableMap propTableMap, RelatedTableFilter relatedTableFilter)
      throws SQLException, XMLMiddlewareException
   {
      OrderedNode parentNode;
      Table       propTable;
      LinkInfo    linkInfo;
      Vector      keyValue;
      Key         propTableKey;
      DataHandler dataHandler;
      OrderInfo   rsOrderInfo;
      String      where = null;
      Vector      columns = null;
      Vector      params = null;
      ResultSet   rs;

      // Add any inlined elements between the class element and the property elements.

      parentNode = addInlinedElements(classNode, classRow, propTableMap.getElementInsertionList());

      // Get the key

      linkInfo = propTableMap.getLinkInfo();
      keyValue = classRow.getColumnValues(linkInfo.getParentKey().getColumns());
      propTableKey = linkInfo.getChildKey();

      // Get the DataHandler used by the table

      propTable = propTableMap.getTable();
      dataHandler = dbMap.getDataHandler(propTable.getDatabaseName());

      // Get the result set over the property table and process it. Note that
      // how we sort the result set depends on whether we are processing a list
      // of token values or properties. If we are processing a token list, then
      // the normal OrderInfo gives us the position of the token-list element or
      // PCDATA in its parent. This value is retrieved by processColumn.

      if (relatedTableFilter != null)
      {
         where = relatedTableFilter.getWhereCondition();
         columns = relatedTableFilter.getColumns();
         params = relatedTableFilter.getParameterValues();
      }
      rsOrderInfo = (propTableMap.isTokenList()) ? propTableMap.getTokenListOrderInfo() :
                                                   propTableMap.getOrderInfo();

      rs = dataHandler.select(propTable, propTableKey, keyValue, where, columns, params, rsOrderInfo);

      processPropResultSet(parentNode, rs, propTableMap);
      rs.close();
   }

   private void processPropResultSet(OrderedNode parentNode, ResultSet rs, PropertyTableMap propTableMap)
      throws SQLException, XMLMiddlewareException
   {
      Row  row = new Row();

      while (rs.next())
      {
         row.removeAllColumnValues();
         row.setColumnValues(rs, propTableMap.getTable(), map.emptyStringIsNull());
         processColumn(parentNode, row, propTableMap);
      }
   }

   // ************************************************************************
   // Helper methods -- initialization
   // ************************************************************************

   private void initGlobals(DBEnabledMap dbMap)
   {
      // Set up the global variables

      this.dbMap = dbMap;
      map = dbMap.getMap();
   }

   private void resetGlobals()
   {
      // Set the per-execution global variables to null so we don't hold
      // any unnecessary references.

      dbMap = null;
      map = null;
      filterBase = null;
   }

   private OrderedNode getOrderedRootNode(Node rootNode, FilterSet filterSet)
      throws XMLMiddlewareException
   {
      Vector          wrapperNames, filters;
      Node            realRootNode = null;
      Object          filter;
      ResultSetFilter rsFilter;
      Table           rootTable;
      ClassTableMap   rootTableMap;

      wrapperNames = filterSet.getWrapperNames();

      // There are four cases:
      //
      // Case 1: The root node is null and there are wrappers:
      //    Document created?   Yes
      //    Root element type:  Top-level wrapper
      //    Returned node:      Bottom-level wrapper
      //
      // Case 2: The root node is null and there are no wrappers:
      //    Document created?   Yes
      //    Root element type:  Type to which the root filter or result set is mapped
      //    Returned node:      Document node
      //
      // Case 3: The root node is not null and there are wrappers:
      //    Document created?   No
      //    Root element type:  Already exists
      //    Returned node:      Bottom-level wrapper
      //
      // Case 4: The root node is not null and there are no wrappers:
      //    Document created?   No
      //    Root element type:  Already exists
      //    Returned node:      Root node
      //
      // The returned node is the node to which DBMSToDOM will append new children.

      if (rootNode == null)
      {
         if (!wrapperNames.isEmpty())
         {
            // Case 1: The root node is null and there are wrappers

            createDocument((XMLName)wrapperNames.elementAt(0));
            realRootNode = addWrapperElements(doc, wrapperNames, filterSet.getNamespaceURIs());
         }
         else
         {
            // Case 2: The root node is null and there are no wrappers

            filters = filterSet.getFilters();
            if (filters.size() > 1)
               throw new IllegalArgumentException("There is more than one root filter and no wrapper elements.");
            filter = filters.elementAt(0);
            if (filter instanceof RootFilter)
            {
               rootTable = ((RootFilter)filter).getRootFilterConditions().getTable();
               rootTableMap = map.getClassTableMap(rootTable);
            }
            else // if (filter instanceof ResultSetFilter)
            {
               rsFilter = (ResultSetFilter)filter;
               rootTableMap = map.getClassTableMap(rsFilter.getDatabaseName(), rsFilter.getCatalogName(), rsFilter.getSchemaName(), rsFilter.getTableName());
            }
            createDocument(rootTableMap.getElementTypeName());
            realRootNode = doc;
         }
      }
      else // if (rootNode != null)
      {
         doc = rootNode.getOwnerDocument();
         if (!wrapperNames.isEmpty())
         {
            // Case 3: The root node is not null and there are wrappers

            realRootNode = addWrapperElements(rootNode, wrapperNames, filterSet.getNamespaceURIs());
         }
         else
         {
            // Case 4: The root node is not null and there are no wrappers

            realRootNode = rootNode;
         }
      }
      return new OrderedNode(realRootNode, OrderInfo.UNORDERED, null);
   }

   private void createDocument(XMLName rootName)
      throws XMLMiddlewareException
   {
      DOMImplementation domImpl;
      DocumentType      docType;
      Element           root;

      domImpl = utils.getDOMImplementation();
      docType = domImpl.createDocumentType(rootName.getQualifiedName(), publicID, systemID);
      doc = domImpl.createDocument(rootName.getURI(), rootName.getQualifiedName(), docType);

      // DOMImplementation.createDocument creates the root element. This screws up
      // a lot of recursive code, so get rid of it. It is added later.

      root = doc.getDocumentElement();
      doc.removeChild(root);
   }

   private Node addWrapperElements(Node rootNode, Vector wrapperNames, Hashtable namespaceURIs)
   {
      Node    wrapper, newWrapper, topWrapper;
      XMLName wrapperName;
      int     i;

      if (wrapperNames.size() == 0) return rootNode;

      wrapper = rootNode;
      for (i = 0; i < wrapperNames.size(); i++)
      {
         wrapperName = (XMLName)wrapperNames.elementAt(i);
         newWrapper = doc.createElementNS(wrapperName.getURI(), wrapperName.getQualifiedName());
         wrapper.appendChild(newWrapper);
         wrapper = newWrapper;
      }

      // Add namespace declarations to the top wrapper element. Since we appended
      // wrapper elements to the end of the children of the root node, we need to
      // get the last child of the root node, which is the first wrapper element
      // in the chain.

      addNamespaceDecls((Element)rootNode.getLastChild(), namespaceURIs);

      // Return the lowest level wrapper element.

      return wrapper;
   }

   private void addNamespaceDeclsToChildren(Node node, Hashtable namespaceURIs)
   {
      Node child;

      child = node.getFirstChild();
      while (child != null)
      {
// BUG! If the application passes a root node, that root node has children, and
// the map uses namespaces, this code will add namespace declarations to the element
// children of the root node. To solve this, OrderedNode needs to keep track of
// the first child that we add to the root node and start adding namespaces from
// there...

         if (child instanceof Element)
         {
            addNamespaceDecls((Element)child, namespaceURIs);
         }
         child = child.getNextSibling();
      }
   }

   private void addNamespaceDecls(Element element, Hashtable namespaceURIs)
   {
      Enumeration prefixes;
      String      prefix, uri;

      // Loop through the namespaces prefix/URI pairs and add xlmns
      // attributes for them.

      prefixes = namespaceURIs.keys();
      while (prefixes.hasMoreElements())
      {
         prefix = (String)prefixes.nextElement();
         uri = (String)namespaceURIs.get(prefix);
         element.setAttributeNS(XMLNSURI, XMLNS + prefix, uri);
      }
   }

   // ************************************************************************
   // Helper methods -- inlined elements
   // ************************************************************************

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
      catch (XMLMiddlewareException e)
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
         // Cast the returned value as a Number. This is necessary because
         // Oracle doesn't directly support the INTEGER SQL type and
         // returns integers as BigDecimals.

         return ((Number)orderValue).longValue();
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
         if (savedChild == firstUnorderedChild)
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
