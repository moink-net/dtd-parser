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

package org.xmlmiddleware.xmldbms;

import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.filters.*;
import org.xmlmiddleware.xmldbms.keygenerators.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.actions.*;
import org.xmlmiddleware.xmlutils.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.w3c.dom.*;

/**
 * Transfers data from the database to a DOM tree.
 *
 * <p>DOMToDBMS transfers data from a DOM tree to the database according
 * to a particular XMLDBMSMap and Actions object. The caller must provide a
 * TransferInfo object, which contains the map and database handlers,
 * a DOM Element or Document, and one or more actions to be taken when
 * storing this data in the database.</p>
 *
 * <p>For example, the following code transfers data from orders.xml according
 * to the map document orders.map and the action document orders.act:</p>
 *
 * <pre>
 *   // Create the XMLDBMSMap object with a user-defined function.
 *   <br />
 *   map = createMap("orders.map");
 *   <br />
 *   // Create the Actions object with a user-defined function.
 *   <br />
 *   actions = createActions(map, "orders.act");
 *   <br />
 *   // Create a new DOMToDBMS object.
 *   <br />
 *   domToDBMS = new DOMToDBMS();
 *   <br />
 *   // Create a data source and data handler for our database, then
 *   // bundle these into a TransferInfo object.
 *   <br />
 *   ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:xmldbms");
 *   handler = new GenericHandler(ds, null, null);
 *   ti = new TransferInfo(map, null, handler);
 *   <br />
 *   // Open the document and call storeDocument to transfer the data.
 *   <br />
 *   utils = new ParserUtilsXerces();
 *   doc = utils.openDocument(new InputSource(new FileInputStream("orders.xml")));
 *   domToDBMS.storeDocument(ti, doc, actions);
 * </pre>
 *
 * <p>DOMToDBMS stores data starting with the first element it finds that is mapped
 * as a class. It continues processing along a given branch until it finds an
 * element that is not mapped. It then continues with the sibling of that element.
 * Thus, DOMToDBMS stores data from one or more contiguous fragments of the DOM tree.</p>
 *
 * @author Sean Walter
 * @version 2.0
 */

public class DOMToDBMS
{
   // ************************************************************************
   // Private variables
   // ************************************************************************

   // The transfer info used for a session of storeDocument

   private TransferInfo m_transInfo;

   // The commit mode (see processRoot)

   private int m_commitMode;

   // Whether to build/return a FilterSet identifying the stored document

   private boolean m_returnFilterSet;

   // Stop on Database errors (see processClassRow)

   private boolean m_stopOnException;

   // Stored Exceptions and Warnings (see getExceptions and getWarnings)

   private SQLWarning m_sqlWarnings;
   private SQLException m_sqlExceptions;

   // Which actions to take for specific nodes

   private Actions m_actions;

   // Hash table of key generators

   private Hashtable m_keyGenerators;

   // Cached objects. These are here for efficiency (see getAllPropertyMaps)

   private Hashtable m_classAllProperties;

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static final String DUMMY = "dummy";
   private static final String AND = "AND ";

   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
    * Create a DOMToDBMS object.
    *
    * @return The DOMToDBMS object.
    */
   public DOMToDBMS()
   {
      m_keyGenerators = new Hashtable();
      m_commitMode = DataHandler.COMMIT_AFTERSTATEMENT;
      m_returnFilterSet = false;
      m_stopOnException = true;

      m_classAllProperties = new Hashtable();
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Set the commit mode.
    *
    * <p>The commit mode must be one of DataHandler.COMMIT_AFTERSTATEMENT,
    * COMMIT_AFTERDOCUMENT, COMMIT_NONE, or COMMIT_NOTRANSACTIONS. The default
    * is COMMIT_AFTERSTATEMENT.</p>
    *
    * @param commitMode The commit mode.
    */
   public void setCommitMode(int commitMode)
   {
      if ((commitMode != DataHandler.COMMIT_AFTERSTATEMENT) &&
         (commitMode != DataHandler.COMMIT_AFTERDOCUMENT) &&
         (commitMode != DataHandler.COMMIT_NONE) &&
         (commitMode != DataHandler.COMMIT_NOTRANSACTIONS))
         throw new IllegalArgumentException("Invalid commit mode: " + commitMode);

      m_commitMode = commitMode;
   }

   /**
    * Get the commit mode.
    *
    * @return The mode.
    */
   public int getCommitMode()
   {
      return m_commitMode;
   }

   /**
    * Sets whether storeDocument returns a FilterSet or a null.
    *
    * <p>storeDocument can return a FilterSet describing the document it
    * processes. This can be used at a later point in time to retrieve the
    * document. Whether such a FilterSet is useful depends on the application.
    * As a general rule, it is not useful for data-centric applications. For these
    * applications, XML is usually a data transport and documents have no persistent identity.
    * It is useful for document-centric applications, for which documents do
    * have a persistent identity.</p>
    *
    * <p>By default, storeDocument returns a null. This saves some processing
    * time, although the amount saved is likely to be noticeable only for very
    * large documents.</p>
    *
    * @param value Whether storeDocument returns a FilterSet
    */
   public void setFilterSetReturned(boolean value)
   {
      m_returnFilterSet = value;
   }

   /**
    * Whether storeDocument returns a FilterSet or a null.
    *
    * <p>For more information, see setFilterSetReturned.</p>
    *
    * @return Whether storeDocument returns a FilterSet
    */
   public boolean isFilterSetReturned()
   {
      return m_returnFilterSet;
   }

   /**
    * Add a key generator by name
    *
    * <p>Applications call this method only if the map requires
    * XML-DBMS to generate primary keys. (This does not include
    * database-generated keys.)</p>
    *
    * @param name The logical name of the key generator.
    * @param generator An object that implements the KeyGenerator interface
    */
   public void addKeyGenerator(String name, KeyGenerator generator)
   {
      m_keyGenerators.put(name, generator);
   }

   /**
    * Remove a key generator.
    *
    * @param name The logical name of the key genenerator.
    */
   public void removeKeyGenerator(String name)
   {
      m_keyGenerators.remove(name);
   }

   /**
    * Remove all key generators.
    */
   public void removeAllKeyGenerators()
   {
      m_keyGenerators.clear();
   }

   /**
    * Defines how SQLExceptions are handled.
    *
    * <p>When a database exception occurs, one of two things happens. If this
    * is set to false, then the exception is added to the list returned
    * by getExceptions and processing continues at the next class element.
    * If this is set to true, then processing stops and the transaction (if any)
    * is rolled back.</p>
    *
    * <p>By default, processing stops on exceptions.</p>
    *
    * @param stopOnException Stop or not.
    */
   public void stopOnException(boolean stopOnException)
   {
      m_stopOnException = stopOnException;
   }

   /**
    * Returns a chain of all SQLWarnings generated while processing
    * the last document.
    *
    * @return The chain. Null if there were no SQLWarnings.
    */
   public SQLWarning getWarnings()
   {
      return m_sqlWarnings;
   }

   /**
    * Returns a chain of all SQLExceptions generated while processing
    * the last document.
    *
    * @return The chain. Null if there were no SQLExceptions or stopOnException
    *   is set to true.
    */
   public SQLException getExceptions()
   {
      return m_sqlExceptions;
   }

   /**
    * Store a DOM tree in the database using a single action.
    *
    * @param transInfo TransferInfo object containing the map and DataHandlers.
    * @param doc The DOM document to store.
    * @param action Action to take on the document.
    * @return Null or a FilterSet describing the stored data. See setFilterSetReturned().
    */
   public FilterSet storeDocument(TransferInfo transInfo, Document doc, int action)
      throws SQLException, XMLMiddlewareException
   {
      return storeDocument(transInfo, doc.getDocumentElement(), action);
   }

   /**
    * Store a DOM tree in the database using a set of actions.
    *
    * @param transInfo TransferInfo object containing the map and DataHandlers.
    * @param doc The DOM document to store.
    * @param action Actions to take on various elements of the document.
    * @return Null or a FilterSet describing the stored data. See setFilterSetReturned().
    */
   public FilterSet storeDocument(TransferInfo transInfo, Document doc, Actions actions)
      throws SQLException, XMLMiddlewareException
   {
      return storeDocument(transInfo, doc.getDocumentElement(), actions);
   }

   /**
    * Store part of a document in the database using a single action.
    *
    * @param transInfo TransferInfo object containing the map and DataHandlers.
    * @param el Element defining the part of the document to store.
    * @param action Action to take on the tree.
    * @return Null or a FilterSet describing the stored data. See setFilterSetReturned().
    */
   public FilterSet storeDocument(TransferInfo transInfo, Element el, int action)
      throws SQLException, XMLMiddlewareException
   {
      Action act = new Action();
      act.setAction(action);

      Actions actions = new Actions(transInfo.getMap());
      actions.setDefaultAction(act);

      return storeDocument(transInfo, el, actions);
   }

   /**
    * Store part of a document in the database using a set of actions.
    *
    * @param transInfo TransferInfo object containing the map and DataHandlers.
    * @param element Element defining the part of the document to store.
    * @param action Actions to take on various elements of the tree.
    * @return Null or a FilterSet describing the stored data. See setFilterSetReturned().
    */
   public FilterSet storeDocument(TransferInfo transInfo, Element element, Actions actions)
      throws SQLException, XMLMiddlewareException
   {
      FilterSet filterSet = (m_returnFilterSet) ? new FilterSet(transInfo.getMap()) : null;

      // TODO: Make this reentrant

      m_transInfo = transInfo;
      m_sqlExceptions = null;
      m_sqlWarnings = null;
      m_actions = actions;

      // Call startDocument here

      Enumeration e = transInfo.getDataHandlers();
      while(e.hasMoreElements())
      {
         ((DataHandler)e.nextElement()).startDocument(m_commitMode);
      }

      // Process the document

      try
      {
         processRoot(filterSet, element, 1);
      }
      catch (Exception ex)
      {
         // If an exception occurs, notify the DataHandlers so they
         // can roll back the current transaction.

         e = transInfo.getDataHandlers();
         while(e.hasMoreElements())
         {
            ((DataHandler)e.nextElement()).recoverFromException();
         }

         // Rethrow the exception so the calling code knows about it.

         if (ex instanceof SQLException)
            throw (SQLException)ex;
         else if (ex instanceof XMLMiddlewareException)
            throw (XMLMiddlewareException)ex;
      }

      // Call endDocument here

      e = transInfo.getDataHandlers();
      while(e.hasMoreElements())
      {
         ((DataHandler)e.nextElement()).endDocument();
      }

      // Set the TransferInfo and Actions objects to null so we don't hold
      // any unnecessary references, such as to connection objects, etc.

      m_transInfo = null;
      m_actions = null;

      // Return the FilterSet.

      return filterSet;
   }

   // ************************************************************************
   // Main Processing Methods
   // ************************************************************************

   // The processing flow is as follows. Note that property tables and columns
   // eventually result in leaf nodes, while related classes result in branch nodes.
   //
   //            ----------
   //            |        |
   //            |    not mapped
   //            v        |
   //  START --> processRoot --
   //            |
   //         mapped
   //            |
   //            v
   //  ------> processClassRow ---------------------------------------
   // |          |                 |                    |
   // |          v                 v                    v
   // |   --> processChildren       storeRow            processFKNodes
   // |   | (attrs, elems, text)     (class row)                |
   // |   |       |                                     |
   // |   |       v                                     |
   // |   |   processChild                                 |
   // |   |   |   |    |                                 |
   // |   | inline  |   related                              |
   // |   |   |  prop   |                                 |
   // |   ----    |     ------------------------               |
   // |          |                       |              |
   // |          v        prop table        v              |
   // |   --> processProperty ------------> processRowChild         |
   // |  |     |      |               |         |         |
   // |  |   multi   single         PK in parent   PK in child   |
   // |  |   value   value              |         |         |
   // |  |     |      |               v         v         |
   // |   ------      |              add to    processRow <----
   // |             v             FK nodes    |      |
   // |        setPropertyColumn              related  prop
   // |                                    |      |
   //  ------------------------------------------------      v
   //                                        processPropRow
   //                                            |
   //                                            v
   //                                          storeRow
   //                                       (prop table row)

   /**
    * Processes the nodes from the root of the DOM Document that are not mapped.
    * The tree is searched until a node is found that is mapped.
    *
    * @param filterSet FilterSet to add root elements to.
    * @param el The node to recusively process.
    * @param orderInParent Position of this element in parent
    */
   private void processRoot(FilterSet filterSet, Element el, long orderInParent)
      throws SQLException, XMLMiddlewareException
   {
      // Check if the node is mapped as a class

      ClassMap classMap = m_transInfo.getMap().getClassMap(el.getNamespaceURI(), el.getLocalName());

      if(classMap != null)
      {
         // Process the node

         Row row = processClassRow(null, classMap, null, el, orderInParent);

         if ((m_returnFilterSet) && (row != null))
         {
            // Add this row to the FilterSet

            Table table = classMap.getTable();
            Key priKey = table.getPrimaryKey();
            RootFilter rootFilter = filterSet.createRootFilter();
            FilterConditions rootConditions = rootFilter.createRootFilterConditions(table);
            String where = buildCondition(priKey.getColumns(),
                                   row.getColumnValues(priKey.getColumns()));
            rootConditions.addCondition(where);
         }
      }
      else
      {
         // If the element is not mapped as a class element, ignore it
         // and process its children

         NodeList children = el.getChildNodes();
         long childOrder = 1;

         for(int i = 0; i < children.getLength(); i++)
         {
            // Process any child elements. Attributes and text are ignored at
            // this point.

            if(children.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
               processRoot(filterSet, (Element)children.item(i), childOrder);
               childOrder++;
            }
         }
      }
   }

   /**
    * This method creates and inserts a row in a class table. As part of the
    * process, it also processes all children of the node.
    *
    * @param parentRow Parent class' row. Null for the root element.
    * @param classMap Class to process.
    * @param relMap Relation between this class and its parent. Null for the root element.
    * @param classNode The node with data for this class.
    * @param orderInParent The order of this node within its parent.
    */
   private Row processClassRow(Row parentRow, ClassMap classMap, RelatedClassMap relMap,
                        Element classNode, long orderInParent)
      throws SQLException, XMLMiddlewareException
   {
      // This method is called from processRoot and from processRow. When
      // it is called from processRoot, relMap and parentRow are null.

      Action   action;
      Vector   fkChildren = new Vector();
      Vector   useProps;
      Table   table;
      LinkInfo linkInfo;
      Row     classRow;

      // Get the action for the node

      action = getActionFor(classNode);

      // Get the list of columns we will insert or update in the database

      useProps = getUseProps(classMap, action);

      // Create a new Row object and set/generate the keys

      table = classMap.getTable();
      linkInfo = (relMap == null) ? null : relMap.getLinkInfo();
      classRow = createRow(table, parentRow, linkInfo);

      try
      {
         // Generate the order column value, if any.
         // TODO: How would we generate order on root elements?

         if (relMap != null)
            generateOrder(classRow, relMap.getOrderInfo(), orderInParent);

         // Process the children. This does four things:
         //
         // 1) For properties (PCDATA, attributes, and property elements) stored in
         //   the class table, adds property values to the class row.
         // 2) For properties stored in property tables, adds property nodes to fkChildren.
         //   Note that rows in property tables always have the FK.
         // 3) For related class rows when the PK is in the child class, inserts the row.
         // 4) For related class elements when the FK is in the child class, adds the
         //   element to fkChildren.
         //
         // Steps (2), (3), and (4) are done because referential integrity constraints
         // require PK rows to be inserted before FK rows. Because the PK can be in
         // either the parent or child class, we need to handle both cases.

         processChildren(classRow, classMap, classNode, fkChildren, action, useProps);

         // If any fields in the table haven't been set, set them to NULL

         setMissingFieldsToNull(classRow, useProps);

         // Insert or update the class row.

         storeRow(table, classRow, action.getAction());

         // If we are doing an update and we are using property tables,
         // delete the rows from these tables. This is because updating
         // a multi-valued property means deleting all the existing rows
         // and adding the new rows. Note that we don't add the new rows
         // until we call processFKNodes. This is because rows in property
         // tables always contain the FK.

         deletePropTableRows(classRow, useProps, action);

         // Process children stored in child tables for which the child
         // row contains the FK.

         processFKNodes(classRow, fkChildren, action);
      }
      catch(SQLException e)
      {
         // Note that if an error occurs while inserting/updating the class row,
         // we won't process any children that depend on the class row -- that
         // is, children that use an FK to link to the class row.

         // TODO: Check how we handle if one of the children that this
         // row is dependent on errors. Do we handle this or just let
         // it blow with two errors (one for the child and then one here)?

         if(m_stopOnException)
            throw e;
         else
            pushException(e);
      }

      return classRow;
   }

   /**
    * Process the children of a class element. This includes the attributes.
    */
   private void processChildren(Row parentRow, ClassMapBase parentMap, Node parentNode,
                         Vector fkChildren, Action action, Vector useProps)
      throws SQLException, XMLMiddlewareException
   {
      // This method is called from processClassRow. It is also called recursively
      // from processChild for inline class maps.

      Node   childNode;
      long   childOrder = 1;
      Object childMap;

      // Get the first child. DOMNormalizer resolves entity reference children
      // and concatenates all adjacent PCDATA children. Thus, we only get complete
      // element and PCDATA children.

      childNode = DOMNormalizer.getFirstChild(parentNode);

      // Process the child elements and PCDATA.

      while(childNode != null)
      {
         childMap = null;

         // Get the map for the node based on type

         switch(childNode.getNodeType())
         {
            case Node.TEXT_NODE:
               childMap = parentMap.getPCDATAMap();
               break;

            case Node.ELEMENT_NODE:
               childMap = parentMap.getChildMap(childNode.getNamespaceURI(),
                                        childNode.getLocalName());
               break;

         }

         // Process the child.

         processChild(parentRow, childMap, childNode, childOrder,
                   fkChildren, action, useProps);

         // Increment the count of the child in its parent.

         childOrder++;

         // Get the next PCDATA or element child.

         childNode = DOMNormalizer.getNextSibling(childNode);
      }

      // Process the attributes

      NamedNodeMap attrs = parentNode.getAttributes();
      if(attrs != null)
      {
         for(int i = 0; i < attrs.getLength(); i++)
         {
            // Get the attribute, get its map, and process it.

            childNode = attrs.item(i);
            childMap = parentMap.getAttributeMap(childNode.getNamespaceURI(),
                                        childNode.getLocalName());
            processChild(parentRow, childMap, childNode, 0,
                      fkChildren, action, useProps);
         }
      }
   }

   /**
    * This is the other half of processChildren. It is called for every node that is mapped.
    */
   private void processChild(Row parentRow, Object childMap, Node childNode, long childOrder,
                       Vector fkChildren, Action action, Vector useProps)
      throws SQLException, XMLMiddlewareException
   {
      if(childMap == null)
      {
         return;
      }
      else if(childMap instanceof PropertyMap)
      {
         // Only process the property if it is being used (useProps). When
         // inserting, all mapped properties are used. When updating, only the
         // properties explicitly specified in the Actions object are used.

         if(useProps.contains(childMap))
         {
            // Call processProperty for the property. For properties in
            // the class table, this calls setPropertyColumn. For properties
            // in property tables, this calls processRowChild.

            processProperty(parentRow, (PropertyMap)childMap, childNode,
                        childOrder, fkChildren, action);
         }
      }
      else if(childMap instanceof RelatedClassMap)
      {
         // Process the related class

         processRowChild(parentRow, childMap, ((RelatedClassMap)childMap).getLinkInfo(),
                     childNode, childOrder, fkChildren, action);
      }
      else if(childMap instanceof InlineClassMap)
      {
         // Generate the order column value of the inline element, if any.

         generateOrder(parentRow, ((InlineClassMap)childMap).getOrderInfo(), childOrder);

         // If the element is inlined, just recursively call processChildren with
         // this node as the parent node. Since the parent row is unchanged, the
         // children of this element will be processed as if they were children
         // of the class element (which owns the parent row).

         processChildren(parentRow, (InlineClassMap)childMap, childNode, fkChildren, action, useProps);
      }
   }

   /**
    * Adds the property value to the class row or (in the case of properties
    * stored in a separate property table) calls processRowChild.
    */
   private void processProperty(Row parentRow, PropertyMap propMap, Node propNode, long order, Vector fkNodes, Action action)
      throws SQLException, XMLMiddlewareException
   {
      // NOTE: Called from processChild

      // Generate the order column value, if any.

      generateOrder(parentRow, propMap.getOrderInfo(), order);

      if(propMap.isTokenList())
      {
         // If the property is a token list of properties, the process each value
         // as a separate property. To do this, we construct a fake property map
         // (in which isTokenList() returns false) and fake property nodes. Note
         // that the names of the fake nodes are unimportant since we already have
         // a property map.

         // Create a StringTokenizer over the property value.

         StringTokenizer s = new StringTokenizer(getNodeValue(propNode, propMap.containsXML()), " \n\r\t", false);

         // Start the token order at 1. This is because order here refers to the
         // order of the token in the list, not the order of the property in its
         // parent.

         long tokenOrder = 1;

         // Get the parent document. We need this to create new nodes.

         Document doc = propNode.getOwnerDocument();

         // Create a new property map and copy all needed attributes. Note that
         // we set the token list property to false (since each token will be
         // passed recursively to processProperty as a normal property) and that
         // we use the token list order info (which tells us how to store the order
         // of the token in the list) rather than the normal order info (which
         // tells us how to store the order of the token list in its parent).

         PropertyMap tokenMap = PropertyMap.create(DUMMY, DUMMY, propMap.getType());

         tokenMap.setTable(propMap.getTable(), propMap.getLinkInfo());
         tokenMap.setColumn(propMap.getColumn());
         tokenMap.setIsTokenList(false);
         tokenMap.setOrderInfo(propMap.getTokenListOrderInfo());

         while(s.hasMoreElements())
         {
            Node tokenNode = null;

            switch(propNode.getNodeType())
            {
               case Node.ELEMENT_NODE:
                  tokenNode = doc.createElement(DUMMY);
                  tokenNode.appendChild(doc.createTextNode(s.nextToken()));
                  break;

               case Node.TEXT_NODE:
                  tokenNode = doc.createTextNode(s.nextToken());
                  break;

               case Node.ATTRIBUTE_NODE:
                  tokenNode = doc.createAttribute(DUMMY);
                  tokenNode.setNodeValue(s.nextToken());
                  break;
            }

            if(tokenNode != null)
            {
               // Now we recursively call processProperty with the property node
               // constructed for the token. Note that this node will be processed
               // by the other clause in the if-then-else statement since the
               // property map we constructed has the tokenList flag set to false.
               // This removes any possibility of an infinite loop.

               processProperty(parentRow, tokenMap, tokenNode, tokenOrder,
                           fkNodes, action);

               // Increment the token order.

               tokenOrder++;
            }
         }
      }
      else
      {
         // This clause processes properties that are single values -- that is,
         // properties that are not token lists.

         if(propMap.getTable() == null)
         {
            // If the property is stored in the class table, set the value now.

            setPropertyColumn(parentRow, propMap, propNode);
         }
         else
         {
            // Otherwise, the property is stored in a separate property table. In
            // this case, pass it to processRowChild for processing.

            processRowChild(parentRow, propMap, propMap.getLinkInfo(), propNode,
                        order, fkNodes, action);
         }
      }
   }

   /**
    * General function for processing children that are stored in separate rows --
    * that is, in child tables. These can be related class tables or property tables.
    */
   private void processRowChild(Row parentRow, Object map, LinkInfo linkInfo, Node node,
                         long orderInParent, Vector fkNodes, Action action)
      throws SQLException, XMLMiddlewareException
   {
      // This method is called for nodes that are stored in child rows. It is
      // called before the parent row has been inserted into the database. Thus,
      // if the child row has the unique key, the node is processed immediately and
      // the child row inserted now. If the child row has the foreign key, the node
      // is stored for later processing by processFKNodes, which is called after
      // the parent row is inserted.

      // Called from processChild and processProperty

      if(linkInfo.parentKeyIsUnique())
      {
         // If the parent row has the unique key, store the child node and
         // related information for later processing by processFKNodes.

         fkNodes.addElement(new FKNode(node, map, orderInParent));
      }
      else
      {
         // If the child row has the unique key, process the node now and copy
         // the unique key from the child row to the parent row.

         Row childRow = processRow(parentRow, map, node, orderInParent, action);
         if(childRow != null)
         {
            setParentKey(parentRow, childRow, linkInfo);
         }
      }
   }

   /**
    * When it's actually time for a row to get inserted this sends it to the
    * appropriate location.
    */
   private Row processRow(Row parentRow, Object map, Node node, long orderInParent,
                     Action action)
      throws SQLException, XMLMiddlewareException
   {
      // NOTE: Called from processRowChild and processFKNodes

      if(map instanceof PropertyMap)
      {
         // If the map is a property map, call processPropRow.

         return processPropRow(parentRow, (PropertyMap)map, node, orderInParent, action);
      }
      else // if (fkNode.map instanceof RelatedClassMap)
      {
         // If the map is related class map, call processClassRow with the corresponding
         // class map. Note that we can safely cast the node as an Element in this case
         // because only element nodes can be mapped using RelatedClassMap.

         RelatedClassMap relMap = (RelatedClassMap)map;

         return processClassRow(parentRow, relMap.getClassMap(), relMap, (Element)node,
                           orderInParent);
      }
   }

   /**
    * This method creates and inserts a row in a property table.
    */
   private Row processPropRow(Row parentRow, PropertyMap propMap, Node propNode,
                        long orderInParent, Action action)
      throws SQLException, XMLMiddlewareException
   {
      // NOTE: This method is called from processRow

      // Create the row object and set/generate keys

      Table table = propMap.getTable();
      LinkInfo linkInfo = propMap.getLinkInfo();
      Row propRow = createRow(table, parentRow, linkInfo);

      // Generate the order column value, if any.

      generateOrder(propRow, propMap.getOrderInfo(), orderInParent);

      // Set the property value

      setPropertyColumn(propRow, propMap, propNode);

      // Get the action. If it is UPDATE or UPDATEORINSERT, change it to
      // INSERT. This is because "updating" a property that is stored in
      // a property table means deleting the old row (or rows) for the
      // property and inserting the new row (or rows). The rows are deleted
      // by a call to deletePropTableRows from processClassRow. That call
      // always occurs before this call.

      // Note that Action.NONE falls through this check.

      int act = action.getAction();
      if((act == Action.UPDATE) || (act == Action.UPDATEORINSERT))
         act = Action.INSERT;

      // Insert the row in the property table and return it to the calling method.

      storeRow(table, propRow, act);
      return propRow;
   }

   /**
    * Process nodes that were left until later because the parent table
    * contained the primary key.
    */
   private void processFKNodes(Row parentRow, Vector fkNodes, Action action)
      throws SQLException, XMLMiddlewareException
   {
      // NOTE: Called from processClassRow

      FKNode fkNode;

      for (int i = 0; i < fkNodes.size(); i++)
      {
         // For each node, call processRow. This does one of two things. For
         // nodes stored in property tables, it calls processPropRow. For nodes
         // stored in related class tables, it calls processClassRow.

         fkNode = (FKNode)fkNodes.elementAt(i);
         processRow(parentRow, fkNode.map, fkNode.node, fkNode.orderInParent, action);
      }
   }

   // ************************************************************************
   // Row/column processing methods
   // ************************************************************************

   /**
    * Creates a Row object and creates / copies keys for that row.
    */
   private Row createRow(Table table, Row parentRow, LinkInfo linkInfo)
      throws XMLMiddlewareException
   {
      // NOTE: Called from processClassRow and processPropRow

      // KEYS: Primary and unique keys can be created in one of three ways:
      //
      // o Copied from the DOM tree. Occurs when processProperty calls setPropertyColumn.
      // o Generated by a key generator. Occurs in createRow.
      // o Generated by the database. Occurs when storeRow calls DataHandler.insert.
      //
      // Foreign keys are created by copying them from the row containing the primary
      // or unique key. If the primary/unique key is in the parent row, these are
      // copied to the child in createRow. If the primary/unique key is in the child
      // row, these are copied to the parent in processChild. (processChild first
      // creates the child row, which creates the keys by one of the above methods.)

      // Create the actual row

      Row row = new Row();

      // Generate the primary key (if necessary)

      Key priKey = table.getPrimaryKey();
      if(priKey != null)
      {
         if(priKey.getKeyGeneration() == Key.KEYGENERATOR)
            generateKey(row, priKey);
      }

/*
      // Generate the unique keys (if necessary). Note that this code is not used
      // at the moment, but may be used in the future.

      Enumeration e = table.getUniqueKeys();
      while (e.hasMoreElements())
      {
         Key uniqueKey = (Key)e.nextElement();
         if (uniqueKey.getKeyGeneration() == Key.KEYGENERATOR)
            generateKey(row, uniqueKey);
      }
*/

      // If the row has a parent and the primary / unique key is in the
      // parent, copy it to the child. Note that parentRow and linkInfo
      // should both be null (when the row is in the root table) or both
      // be non-null (when the row is in any other table).

      if(parentRow != null && linkInfo != null)
      {
         if(linkInfo.parentKeyIsUnique())
         {
            setChildKey(parentRow, row, linkInfo);
         }
      }

      return row;
   }

   /**
    * Called before row is stored. Sets missing fields to null.
    */
   private void setMissingFieldsToNull(Row classRow, Vector useProps)
      throws XMLMiddlewareException
   {
      // This method is called after all the children (including attributes)
      // of a class element have been processed. If any properties in which
      // we are interested (useProps) have not yet been set -- that is, they
      // weren't in the XML document -- set them to null now. Note that this
      // only applies to properties stored in the class table; it doesn't
      // apply to properties stored in property tables.

      for(int i = 0; i < useProps.size(); i++)
      {
         PropertyMap propMap = (PropertyMap)useProps.elementAt(i);
         if(propMap.getTable() == null)
         {
            if(!classRow.isColumnSet(propMap.getColumn()))
               classRow.setColumnValue(propMap.getColumn(), null);
         }
      }
   }

   /**
    * Get the value for a node.
    */
   private String getNodeValue(Node propNode, boolean containsXML)
   {
      String s;

      if(propNode.getNodeType() == Node.ELEMENT_NODE)
      {
         // If the property is an element, then the property's value is the
         // element's contents, serialized as XML. If containsXML is true,
         // then <'s and &'s are escaped. This means the XML will be parseable
         // as XML when it is retrieved, and is usually used for storing
         // mixed content. If containsXML is false, the <'s and &'s are serialized
         // literally. The result is not parseable as XML, and this is usually
         // used for storing PCDATA-only content that might contain <'s and &'s.

         s = DOMNormalizer.serialize(propNode, true, containsXML);
      }
      else // if (propNode.getNodeType() == Node.TEXT_NODE, Node.ATTRIBUTE_NODE)
      {
         // If the property is stored in an attribute or text node, then
         // the property's value is the node's value. Since it is simple text,
         // there is no need to call DOMNormalizer.serialize().

         s = propNode.getNodeValue();
      }

      // If empty strings are treated as NULLs, then check the length of
      // the property value and, if it is 0, set the value to null, which
      // is later interpreted as NULL.

      if(m_transInfo.getMap().emptyStringIsNull() && s.length() == 0)
         s = null;

      return s;
   }

   /**
    * Set the value for column
    */
   private void setPropertyColumn(Row row, PropertyMap propMap, Node node)
      throws XMLMiddlewareException
   {
      Column column = propMap.getColumn();

      // Get the node value (a string) and parse it with the column's StringFormatter.
      // The result is an Object of the type corresponding to the column's SQL type.

      StringFormatter formatter = column.getFormatter();
      Object val = formatter.parse(getNodeValue(node, propMap.containsXML()), column.getType());

      // Set the column value to the parsed value.

      row.setColumnValue(column, val);
   }

   /**
    * Generate the order column value, if any.
    */
   private void generateOrder(Row row, OrderInfo o, long orderValue)
      throws XMLMiddlewareException
   {
      // If the property is ordered, the order is stored in an order column,
      // and XML-DBMS generates the order, the store the current order value
      // in the order column. We use ConvertObject to convert the order value
      // from a Long to whatever type is used by the order column.

      if((o != null) && (!o.orderValueIsFixed()) && (o.generateOrder()))
      {
         Column col = o.getOrderColumn();
         Object val = ConvertObject.convertObject(new Long(orderValue), col.getType(), col.getFormatter());
         row.setColumnValue(col, val);
      }
   }

   // ************************************************************************
   // Helper methods -- general
   // ************************************************************************

   /**
    * Get the changeable properties for a ClassMap/Action combination.
    */
   private Vector getUseProps(ClassMap classMap, Action action)
   {
      Vector useProps = null;

      if(action.getAction() == Action.UPDATE)
         useProps = action.getUpdatePropertyMaps();

      if(useProps == null)
         useProps = getAllPropertyMaps(classMap);

      return useProps;
   }

   /**
    * Gets all properties for a ClassMap.
    */
   private Vector getAllPropertyMaps(ClassMapBase classMapBase)
   {
      // TODO: Test if caching this is worth it!
      if(m_classAllProperties.contains(classMapBase))
         return (Vector)m_classAllProperties.get(classMapBase);

      // Q: Could this be moved to ClassMap for efficiency?
      // A: Nope, it can't because ClassMap needs to be threadsafe.
      Vector v = new Vector();
      PropertyMap propMap;

      // PCDATAMap
      propMap = classMapBase.getPCDATAMap();
      if(propMap != null)
         v.addElement(propMap);

      // Attributes
      Enumeration e = classMapBase.getAttributeMaps();
      while(e.hasMoreElements())
         v.addElement(e.nextElement());

      // Child Elements
      e = classMapBase.getChildMaps();
      while(e.hasMoreElements())
      {
         Object map = e.nextElement();
         if(map instanceof PropertyMap)
            v.addElement(map);
         else if (map instanceof InlineClassMap)
         {
            Vector v1 = getAllPropertyMaps((InlineClassMap)map);
            for (int i = 0; i < v1.size(); i++)
            {
              v.addElement(v1.elementAt(i));
            }
         }
      }

      // Cache
      m_classAllProperties.put(classMapBase, v);

      return v;
   }

   /**
    * Gets the action for a given element.
    */
   private Action getActionFor(Element el)
      throws XMLMiddlewareException
   {
      // See if there is an action for the specified element. If so, use it. If
      // not, use the default. If there is no default, throw an exception.

      Action action = m_actions.getAction(el.getNamespaceURI(), el.getLocalName());

      if(action == null)
         action = m_actions.getDefaultAction();

      if(action == null)
         throw new XMLMiddlewareException("No default action specified.");

      // TODO: When action: attributes are implemented put code here

      return action;
   }

   /**
    * Builds a set of expressions of the form "Column=value AND ..."
    */
   private String buildCondition(Column[] columns, Object[] values)
      throws XMLMiddlewareException
   {
      String      value;
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < columns.length; i++)
      {
         if (i != 0) sb.append(AND);
         sb.append(columns[i].getName());
         sb.append('=');
         value = SQLLiterals.buildLiteral(columns[i].getType(), values[i], columns[i].getFormatter());
         sb.append(value);
         sb.append(' ');
      }
      return sb.toString();
   }

   // ************************************************************************
   // Helper methods -- keys
   // ************************************************************************

   /**
    * Copy the child key to parent row.
    */
   private void setParentKey(Row parentRow, Row childRow, LinkInfo l)
      throws XMLMiddlewareException
   {
      Column[] childCols = l.getChildKey().getColumns();

      // We shouldn't ever hit this, but it might be possible if a
      // database-generated key value isn't generated.

      if(!childRow.areColumnsSet(childCols))
         throw new XMLMiddlewareException("Internal error. The child key is not set yet.");

      parentRow.setColumnValues(l.getParentKey().getColumns(),
                          childRow.getColumnValues(childCols));
   }

   /**
    * Copy the parent key to a child row.
    */
   private void setChildKey(Row parentRow, Row childRow, LinkInfo l)
      throws XMLMiddlewareException
   {
      Column[] parentCols = l.getParentKey().getColumns();

      // We shouldn't ever hit this, but it might be possible if a
      // database-generated key value isn't generated.

      if(!parentRow.areColumnsSet(parentCols))
         throw new XMLMiddlewareException("Internal error. The parent key is not set yet.");

      childRow.setColumnValues(l.getChildKey().getColumns(),
                         parentRow.getColumnValues(parentCols));
   }

   /**
    * Use a KeyGenerator to generate a key.
    */
   private void generateKey(Row row, Key key)
      throws XMLMiddlewareException
   {
      KeyGenerator keyGen = (KeyGenerator)m_keyGenerators.get(key.getKeyGeneratorName());

      if(keyGen == null)
         throw new XMLMiddlewareException("No KeyGenerator added for the key generator named " + key.getKeyGeneratorName());

      Column[] columns = key.getColumns();
      Object[] values = keyGen.generateKey();

      if(columns.length != values.length)
         throw new XMLMiddlewareException("Invalid number of columns generated by key generator: " + key.getKeyGeneratorName());

      for(int i = 0; i < columns.length; i++)
      {
         // Convert each generated key column value to the appropriate type for
         // the actual column, then set the column in the row.

         Object value = ConvertObject.convertObject(values[i], columns[i].getType(), columns[i].getFormatter());
         row.setColumnValue(columns[i], value);
      }
   }

   // ************************************************************************
   // Helper methods -- database
   // ************************************************************************

   /**
    * Send a row to a DataHandler for processing.
    */
   private void storeRow(Table table, Row row, int action)
      throws SQLException, XMLMiddlewareException
   {

      boolean soft = false;

      // Get the database
      // TODO: (What about the 'null' or default database?)
      DataHandler dataHandler = m_transInfo.getDataHandler(table.getDatabaseName());

      if(dataHandler == null)
         throw new XMLMiddlewareException("DataHandler not set for the database named " + table.getDatabaseName());

      try
      {
         switch(action)
         {
            case Action.NONE:
               break;

            case Action.SOFTINSERT:
               soft = true;
            case Action.INSERT:
               dataHandler.insert(table, row);
               break;

            case Action.UPDATEORINSERT:
               dataHandler.updateOrInsert(table, row);
               break;

            case Action.UPDATE:
               // NOTE: We only set the appropriate columns in the row anyway
               // so there's no need to pass in a set of columns to update
               dataHandler.update(table, row, null);
               break;

            case Action.SOFTDELETE:
            case Action.DELETE:
               throw new XMLMiddlewareException("DELETE and SOFTDELETE actions cannot be used with DOMToDBMS.");

            default:
               throw new XMLMiddlewareException("Internal error. Invalid action in storeRow.");
         };
      }
      catch(SQLException e)
      {
         if(soft)
            pushWarning(new SQLWarning(e.getMessage(), e.getSQLState()));
         else
            throw e;
      }
   }

   /**
    * Deletes rows from a property table for a particular property.
    */
   private void deletePropTableRows(Row classRow, Vector useProps, Action action)
      throws SQLException, XMLMiddlewareException
   {
      // When a property (usually multi-valued) is stored in a property table,
      // "updating" the property means deleting all the old rows and inserting
      // the new rows.

      if(action.getAction() == Action.UPDATE ||
         action.getAction() == Action.UPDATEORINSERT)
      {
         for(int i = 0; i < useProps.size(); i++)
         {
            // For each property stored in a property table, delete any
            // existing rows from the property table.

            PropertyMap propMap = (PropertyMap)useProps.elementAt(i);

            Table propTable = propMap.getTable();
            if(propTable != null)
            {
               // Note that for the link between class tables and property tables,
               // the unique key is always in the class table.

               LinkInfo li = propMap.getLinkInfo();

               // Create a fake property table row. This contains the key from
               // the parent (class table) row, so it can be used to delete
               // the child (property table) row or rows.

               Row propRow = createRow(propTable, classRow, li);

               // Delete the row from the property table. Note that we set the
               // soft parameter to true, since all we care about is making sure
               // that there are no rows for this key in the property table -- it
               // doesn't matter if any previously existed or not.

               deleteRow(propTable, propRow, li.getChildKey(), true);
            }
         }
      }
   }

   /**
    * Delete a row or rows.
    */
   private void deleteRow(Table table, Row row, Key key, boolean soft)
      throws SQLException, XMLMiddlewareException
   {
      // Get the DataHandler for the database
      // TODO: (What about the 'null' or default database?)

      DataHandler dataHandler = m_transInfo.getDataHandler(table.getDatabaseName());
      if(dataHandler == null)
         throw new XMLMiddlewareException("Database '" + table.getDatabaseName() + "' not set.");

      // Delete the row or rows

      try
      {
         dataHandler.delete(table, row, key);
      }
      catch(SQLException e)
      {
         if(soft)
            pushWarning(new SQLWarning(e.getMessage(), e.getSQLState()));
         else
            throw e;
      }
   }

   // ************************************************************************
   // Helper methods -- exception handling
   // ************************************************************************

   /**
    * Adds an SQLException to the chain (returned from getExceptions)
    */
   private void pushException(SQLException e)
   {
      // TODO: What order do we chain the exceptions?

      if(m_sqlExceptions == null)
         m_sqlExceptions = e;
      else
         m_sqlExceptions.setNextException(e);
   }

   /**
    * Adds an SQLWarning to the chain (returned from getWarnings)
    */
   private void pushWarning(SQLWarning w)
   {
      // TODO: What order do we chain the warnings?

      if(m_sqlWarnings == null)
         m_sqlWarnings = w;
      else
         m_sqlWarnings.setNextWarning(w);
   }

   // ************************************************************************
   // Inner classes
   // ************************************************************************

   // Class used to save nodes for later processing. These nodes are mapped
   // to tables in which the candidate key of the parent's table is used as a
   // foreign key in the node's table. Such rows must be processed after the
   // parent row has been inserted for two reasons:
   //
   // a) An integrity constraint may link the foreign key in the node
   //   table to the candidate key in the parent table.
   //
   // b) We don't know when the candidate key in the parent is set -- it could
   //   be generated or it could be retrieved from other properties, which
   //   might not be encountered until after the saved node is encountered.

   class FKNode
   {
      Node   node;
      Object map;
      long   orderInParent;

      FKNode (Node node, Object map, long orderInParent)
      {
         this.node = node;
         this.map = map;
         this.orderInParent = orderInParent;
      }
   }
}
