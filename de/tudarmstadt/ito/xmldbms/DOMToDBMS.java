// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Bug fixed in processAttributes to store attrs in property tables.
// * Non-root elements can be mapped to root tables (processRelatedClass).
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

import de.tudarmstadt.ito.domutils.NameQualifier;
import de.tudarmstadt.ito.domutils.NameQualifierImpl;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Stack;
import java.util.StringTokenizer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Transfers data from a DOM tree to the database.
 *
 * <P>DOMToDBMS transfers data from a DOM tree to the database according
 * to a particular Map. The caller must always provide a Map object, a
 * KeyGenerator (if any keys are to be generated), and a NameQualifier
 * (if the XML document uses namespaces). It returns a DocumentInfo with
 * information about the tables and keys needed to retrieve the data.</P>
 *
 * <P>For example, the following code transfers data from the "sales_in.xml"
 * document to the database. It does not use a KeyGenerator, but uses the
 * NameQualifier for Oracle's implementation of the DOM:</P>
 *
 * <PRE>
 *    // Use a user-defined function to create a map.
 *    Map map = createMap("sales.map", conn1);<BR />
 *
 *    // Use a user-defined function to create a DOM tree over sales_in.xml
 *    doc = openDocument("sales_in.xml");<BR />
 *
 *    // Create a new DOMToDBMS object and store the data.
 *    domToDBMS = new DOMToDBMS(map, null, new NQ_Oracle());
 *    domToDBMS.storeDocument(doc);
 * </PRE>
 *
 * <P>The default KeyGenerator is (KeyGeneratorImpl) can be found in
 * de.tudarmstadt.ito.xmldbms.helpers. Many NameQualifiers can be found
 * in de.tudarmstadt.ito.domutils.</P>
 *
 * <P><B>WARNING!</B> DOMToDBMS modifies the DOM tree in the process of
 * transferring data to the database.</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class DOMToDBMS
{

   // ************************************************************************
   // Private variables
   // ************************************************************************

   private Map           map = null;
   private KeyGenerator  keyGenerator = null;
   private NameQualifier qualifier = null;
   private Document      doc = null;
   private int           commitMode = COMMIT_AFTERINSERT;
   private Parameters    parameters;

   // ************************************************************************
   // Constants
   // ************************************************************************

   /** Call commit after each INSERT statement is executed (default). */
   public static final int COMMIT_AFTERINSERT = 1;

   /** Call commit after the entire document has been stored. */
   public static final int COMMIT_AFTERDOCUMENT = 2;

   /**
	* Don't call commit. In this case, the application must call commit,
	* such as when the data is part of a larger transaction.
	*/
   public static final int COMMIT_NONE = 3; // Idea from Richard Sullivan.


   // ************************************************************************
   // Constructors
   // ************************************************************************

   /** Construct a DOMToDBMS object. */
   public DOMToDBMS()
   {
   }   

   /** Construct a DOMToDBMS object and set the Map object and commit mode. */
   public DOMToDBMS(Map map)
   {
	  this.map = map;
   }   

   /**
	* Construct a DOMToDBMS object and set the Map, KeyGenerator, and
	* NameQualifier objects.
	*/
   public DOMToDBMS(Map map, KeyGenerator keyGenerator, NameQualifier qualifier)
   {
	  this.map = map;
	  this.keyGenerator = keyGenerator;
	  this.qualifier = qualifier;
   }   

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
	* Get the current commit mode.
	*
	* @return The current commit mode.
	*/
   public int getCommitMode()
   {
	  return commitMode;
   }   

   /**
	* Set the current commit mode.
	*
	* @param commitMode COMMIT_AFTERINSERT, COMMIT_AFTERDOCUMENT, or COMMIT_NONE.
	*/
   public void setCommitMode(int commitMode)
   {
	  if ((commitMode == COMMIT_AFTERINSERT) ||
		  (commitMode == COMMIT_AFTERDOCUMENT) ||
		  (commitMode == COMMIT_NONE))
	  {
		 this.commitMode = commitMode;
	  }
	  else
	  {
		 throw new IllegalArgumentException("Invalid commit mode value: " + commitMode);
	  }
   }   

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
	* @param map The Map.
	*/
   public void setMap(Map map)
   {
	  this.map = map;
   }   

   /**
	* Get the current KeyGenerator.
	*
	* @return The current KeyGenerator.
	*/
   public KeyGenerator getKeyGenerator()
   {
	  return keyGenerator;
   }   

   /**
	* Set the current KeyGenerator.
	*
	* @param keyGenerator The KeyGenerator.
	*/
   public void setKeyGenerator(KeyGenerator keyGenerator)
   {
	  this.keyGenerator = keyGenerator;
   }   

   /**
	* Get the current NameQualifier.
	*
	* @return The current NameQualifier.
	*/
   public NameQualifier getNameQualifier()
   {
	  return qualifier;
   }   

   /**
	* Set the current NameQualifier.
	*
	* @param qualifier The NameQualifier.
	*/
   public void setNameQualifier(NameQualifier qualifier)
   {
	  this.qualifier = qualifier;
   }   

   /**
	* Store the data from a DOM tree in the database.
	*
	* @param doc The DOM tree.
	* @return A DocumentInfo object containing information that can be used
	*  to retrieve the data at a later time.
	* @exception SQLException Thrown if an error occurs storing data in the
	*  database.
	* @exception InvalidMapException Thrown if the Map contains invalid or
	*  incomplete information.
	* @exception KeyException Thrown if an error occurs generating a key or
	*  if keys are to be generated and no KeyGenerator has been specified.
	*/
   public DocumentInfo storeDocument(Document doc)
	  throws SQLException, InvalidMapException, KeyException
   {
	  DocumentInfo docInfo;

	  // Check that we have a map, a key generator (if needed), and a
	  // namespace name qualifier.

	  if (map == null) throw new InvalidMapException("Map not set.");
	  if ((map.generateKeys) && (keyGenerator == null))
		 throw new KeyException("Key generator not set.");

	  initGlobals();

	  // Set the global doc variable.
	  this.doc = doc;

	  // Set auto-commit, process the document, and commit the transaction
	  map.setAutoCommit(commitMode == COMMIT_AFTERINSERT);
	  docInfo = processRoot(doc.getDocumentElement());
	  if (commitMode == COMMIT_AFTERDOCUMENT)
	  {
		 map.commit();
	  }

	  return docInfo;
   }   

   // ************************************************************************
   // Tree processing methods
   // ************************************************************************

   DocumentInfo processRoot(Element root)
	  throws SQLException, InvalidMapException, KeyException
   {
	  RootClassMap rootMap;
	  DocumentInfo docInfo;

	  rootMap = map.getRootClassMap(qualifier.getQualifiedName(root));
	  if (rootMap == null)
		 throw new InvalidMapException ("Root element not mapped to root table or ignored: " + root.getNodeName());

	  docInfo = new DocumentInfo();

// BUG! The childKey can be null, so in both cases, we need to do something.
// The options are not returning any document info at all and using the entire
// row as a key.
	  switch (rootMap.classMap.type)
	  {
		 case ClassMap.TYPE_TOROOTTABLE:
			processRootElement(docInfo, (RelatedClassMap)rootMap, root, 1);
			break;

		 case ClassMap.TYPE_IGNOREROOT:
			// Process the children of the ignored root; these must be
			// Elements, not Text, and must be mapped as TOCLASSTABLE.
			RelatedClassMap childMap;
			int             childOrder = 1;
			Node            child = LogicalNodeUtils.getFirstChild(root);

			while (child != null)
			{
			   childMap = (RelatedClassMap)rootMap.classMap.getElementTypeMap(
											qualifier.getQualifiedName(child));
			   if (childMap != null)
			   {
				  if (childMap.classMap.type != ClassMap.TYPE_TOCLASSTABLE)
					 throw new InvalidMapException("If the root element is ignored, any mapped children must be mapped to class tables. " + child.getNodeName() + " is not.");

				  processRootElement(docInfo, childMap, child, childOrder);
			   }
			   child = getNextChild(child);
			   childOrder++;
			}
			break;

		 default:
			// The root must be mapped as TOROOTTABLE or IGNOREROOT
			throw new InvalidMapException("Root element must be mapped to a root table or ignored. " + root.getNodeName() + " is not.");
	  }
	  return docInfo;
   }   

   void processRootElement(DocumentInfo docInfo, RelatedClassMap relatedClassMap, Node root, int orderInParent)
	  throws KeyException, SQLException, InvalidMapException
   {
	  Column[] keyColumns = null;
	  Object[] key = null;
	  Row      row;

	  row = createClassRow(null, relatedClassMap, root, orderInParent);
	  if (relatedClassMap.linkInfo != null)
	  {
		 keyColumns = relatedClassMap.linkInfo.childKey;
		 key = row.getColumnValues(keyColumns);
	  }
	  docInfo.addInfo(relatedClassMap.classMap.table, keyColumns, key, relatedClassMap.orderInfo.orderColumn);
   }   

   Row createClassRow(Row parentRow, RelatedClassMap rcm, Node classNode, int orderInParent)
	  throws KeyException, SQLException, InvalidMapException
   {
	  // This method creates and inserts a row in a class table. As part of the
	  // process, it also processes all children of the node.

	  Row   classRow;

	  Stack fkChildren = new Stack();
	  classRow = new Row (rcm.classMap.table);
	  if (rcm.linkInfo.parentKeyIsCandidate)
	  {
		 // If the candidate key linking this class to its parent class is
		 // in the parent's table, set that key in the child row now. Otherwise,
		 // generate the candidate key in the current row.

		 setChildKey(parentRow, classRow, rcm.linkInfo);
	  }
	  else
	  {
		 generateChildKey(classRow, rcm.linkInfo);
	  }

	  // BUG! Notice that the order is always assumed to be in the child
	  // class table. The mapping language supports placing it in either
	  // the parent or child tables, but the code does not -- for more
	  // information, see the bug file. (When this bug is fixed, care
	  // must be taken with the root element. In this case, the order
	  // column is always in the "child" (root) table, regardless of what
	  // parentKeyIsCandidate says.)

	  // 5/5/00, Ivana Tzenova
	  // Added fkChildren parameter to processAttributes call. See
	  // processAttributes for details.

	  generateOrder(classRow, rcm.orderInfo, orderInParent);
	  processAttributes(classRow, rcm.classMap, classNode, fkChildren);
	  processChildren(classRow, rcm.classMap, classNode, fkChildren);
	  insertRow(rcm.classMap.table, classRow);
	  processFKNodes(classRow, fkChildren);
	  return classRow;
   }   

   Row createPropRow(Row parentRow, PropertyMap propMap, Node propNode, int orderInParent)
	  throws KeyException, SQLException
   {
	  // This method creates and inserts a row in a property table. If the
	  // key used to link the row to its parent is a candidate key in this
	  // table, it is generated if necessary. Otherwise, the candidate key
	  // from the parent is set in this table as a foreign key.

	  Row propRow = new Row(propMap.table);

	  if (propMap.linkInfo.parentKeyIsCandidate)
	  {
		 // If the candidate key linking this class to its parent class is
		 // in the parent's table, set that key in the child row now. Otherwise,
		 // generate the candidate key in the current row.

		 setChildKey(parentRow, propRow, propMap.linkInfo);
	  }
	  else
	  {
		 generateChildKey(propRow, propMap.linkInfo);
	  }

	  // BUG! Notice that the order is always assumed to be in the property
	  // table. The mapping language supports placing it in either the
	  // parent or child tables, but the code does not -- for more
	  // information, see the bug file.

	  generateOrder(propRow, propMap.orderInfo, orderInParent);
	  setPropertyColumn(propRow, propMap.column, propNode);
	  insertRow(propMap.table, propRow);
	  return propRow;
   }   

   void processFKNodes(Row parentRow, Stack fkNodes)
	  throws KeyException, SQLException, InvalidMapException
   {
	  // This method creates and inserts a row in a class or property table.
	  // The candidate key used to link the row to its parent is in the
	  // parent's table.

	  FKNode fkNode;

	  while (!fkNodes.empty())
	  {
		 fkNode = (FKNode)fkNodes.pop();
		
		 if (fkNode.map instanceof PropertyMap)
		 {
			createPropRow(parentRow, (PropertyMap)fkNode.map, fkNode.node, fkNode.orderInParent);
		 }
		 else // if (fkNode.map instanceof RelatedClassMap)
		 {
			createClassRow(parentRow, (RelatedClassMap)fkNode.map, fkNode.node, fkNode.orderInParent);
		 }
	  }
   }   

   void processChildren(Row parentRow, ClassMap parentMap, Node parentNode, Stack fkChildren)
	  throws KeyException, InvalidMapException, SQLException
   {
	  // Process the children of a class node.

	  Object childMap;
	  Node   child = LogicalNodeUtils.getFirstChild(parentNode);
	  int    childOrder = 1;

	  while (child != null)
	  {
		 // Get the child map. LogicalNodeUtils.getFirstChild() guarantees
		 // that it is either a text or element node.

		 if (child.getNodeType() == Node.TEXT_NODE)
		 {
			childMap = parentMap.getPCDATAMap();
		 }
		 else // if (child.getNodeType() == Node.ELEMENT_NODE)
		 {
			childMap = parentMap.getElementTypeMap(qualifier.getQualifiedName(child));
		 }

		 // If the child has been mapped, then process it. Otherwise, ignore.

		 if (childMap != null)
		 {
			if (childMap instanceof PropertyMap)
			{
			   processProperty(parentRow, (PropertyMap)childMap,
							   child, childOrder, fkChildren);
			}
			else // if (childMap instanceof RelatedClassMap)
			{
			   processRelatedClass(parentRow, (RelatedClassMap)childMap,
								   child, childOrder, fkChildren);
			}
			// PASSTHROUGH! When we support pass-through elements, we will
			// need to check if the child has been mapped as pass-through.
		 }
		 child = getNextChild(child);
		 childOrder++;
	  }
   }   

   void processProperty(Row parentRow, PropertyMap propMap, Node propNode, int orderInParent, Stack fkNodes)
	  throws KeyException, SQLException
   {
	  switch (propMap.type)
	  {
		 case PropertyMap.TYPE_TOCOLUMN:
			generateOrder(parentRow, propMap.orderInfo, orderInParent);
			setPropertyColumn(parentRow, propMap.column, propNode);
			break;

		 case PropertyMap.TYPE_TOPROPERTYTABLE:
			if (propMap.linkInfo.parentKeyIsCandidate)
			{
			   // If the key linking the class table to the property table is
			   // a candidate key in the class table and a foreign key in the
			   // property table, generate that key now and save the node
			   // for later processing (see FKNode).

			   generateParentKey(parentRow, propMap.linkInfo);
			   fkNodes.push(new FKNode(propNode, propMap, orderInParent));
			}
			else
			{
			   // If the key linking the class table to the property table is
			   // a candidate key in the property table and a foreign key in the
			   // class table, create the row now, then set the foreign key in
			   // the parent (class) table.

			   Row propRow = createPropRow(null, propMap, propNode, orderInParent);
			   setParentKey(parentRow, propRow, propMap.linkInfo);
			}
			break;
	  }
   }   

   void processRelatedClass(Row parentRow, RelatedClassMap rcm, Node classNode, int orderInParent, Stack fkNodes)
	  throws KeyException, InvalidMapException, SQLException
   {
	  switch (rcm.classMap.type)
	  {
		 // 6/9/00, Ronald Bourret
		 // Treat _TOROOTTABLE the same as _TOCLASSTABLE. Previously, an error
		 // was returned if a non-root element was mapped to a root table. To
		 // see why this is not an error, consider the trivial case when a
		 // root element can contain itself as a child. A non-trivial case is
		 // when a single map document is used by multiple documents that
		 // share the same DTD and mapping but have different root element types.

		 case ClassMap.TYPE_TOCLASSTABLE:
		 case ClassMap.TYPE_TOROOTTABLE:
			if (rcm.linkInfo.parentKeyIsCandidate)
			{
			   // If the key linking the class table to the related class table
			   // is a candidate key in the class table and a foreign key in the
			   // related class table, generate that key now and save the node
			   // for later processing (see FKNode).

			   generateParentKey(parentRow, rcm.linkInfo);
			   fkNodes.push(new FKNode(classNode, rcm, orderInParent));
			}
			else
			{
			   // If the key linking the class table to the related class table
			   // is a candidate key in the related class table and a foreign
			   // key in the class table, create the row now, then set the
			   // foreign key in the parent (class) table.

			   Row classRow = createClassRow(null, rcm, classNode, orderInParent);
			   setParentKey(parentRow, classRow, rcm.linkInfo);
			}
			break;

		 case ClassMap.TYPE_IGNOREROOT:
			throw new InvalidMapException("Non-root element ignored: " + classNode.getNodeName());

		 case ClassMap.TYPE_PASSTHROUGH:
			throw new InvalidMapException("Pass-through not implemented yet: " + classNode.getNodeName());
	  }
   }   

   void setPropertyColumn(Row propRow, Column propColumn, Node propNode)
   {
	  String s;

	  if (propNode.getNodeType() == Node.ELEMENT_NODE)
	  {
		 // If the property is stored in an element, then the property's
		 // value is the the element's contents, serialized as XML.

		 s = LogicalNodeSerializer.serializeNode(propNode, true);
	  }
	  else // if (propNode.getNodeType() == Node.TEXT_NODE, Node.ATTRIBUTE_NODE)
	  {
		 // If the property is stored in an attribute or text node, then
		 // the property's value is the node's value.

		 s = propNode.getNodeValue();
	  }

	  // If empty strings are treated as NULLs, then check the length of
	  // the property value and, if it is 0, set the value to null, which
	  // is later interpreted as NULL.

	  if (map.emptyStringIsNull)
	  {
		 if (s.length() == 0)
		 {
			s = null;
		 }
	  }

	  // Set the property's value in the row.

	  propRow.setColumnValue(propColumn, s);
   }   

   void processAttributes(Row elementRow, ClassMap classMap, Node elementNode, Stack fkAttrs)
	  throws SQLException, InvalidMapException, KeyException
   {
	  // 5/5/00, Ivana Tzenova
	  // Added fkAttrs parameter and replaced savedAttrs in calls to
	  // processProperty with fkAttrs. This is because attributes stored in a
	  // property table were not being saved. The problem was that, although
	  // FKNodes for these rows were pushed onto the savedAttrs Stack in
	  // processProperty, savedAttrs was never processed.

	  int          i;
	  int          attrOrder;
	  NamedNodeMap attrs;
	  Attr         attr;
	  PropertyMap  attrMap;

	  if (elementNode.getNodeType() != Node.ELEMENT_NODE) return;
	  if ((attrs = elementNode.getAttributes()) == null) return;

	  for (i = 0; i < attrs.getLength(); i++)
	  {
		 attr = (Attr)attrs.item(i);
		 attrMap = classMap.getAttributeMap(qualifier.getQualifiedName(attr));
		 if (attrMap == null) continue;

		 attrOrder = 1;

		 // In the future, check here for unparsed entity attributes.

		 if (attrMap.multiValued)
		 {
			// If the attribute is multi-valued, then process each value as a
			// separate attribute. We construct fake attributes for this
			// purpose; the names of these attributes are unimportant, as we
			// already have the AttributeMap. Order refers to the order of the
			// value in the attribute, not order of the attribute in the
			// element (attributes are unordered).

			StringTokenizer s = new StringTokenizer(attr.getNodeValue(), " ", false);
			Attr            fake;

			while (s.hasMoreElements())
			{
			   fake = doc.createAttribute("fake");
			   fake.setNodeValue(s.nextToken());
			   processProperty(elementRow, attrMap, fake, attrOrder, fkAttrs);
			   attrOrder++;
			}
		 }
		 else
		 {
			processProperty(elementRow, attrMap, attr, attrOrder, fkAttrs);
		 }
	  }
   }   

   // ************************************************************************
   // Key setting methods
   // ************************************************************************

   void generateChildKey(Row childRow, LinkInfo l)
	  throws KeyException
   {
	  if (l.generateKey)
	  {
		 childRow.setColumnValues(l.childKey, keyGenerator.generateKey());
	  }
   }   

   void generateParentKey(Row parentRow, LinkInfo l) throws KeyException
   {
	  // Generate the candidate key in the parent's table if: (a) it is
	  // supposed to be generated, and (b) it has not already been generated.
	  // The latter condition is necessary because the parent table may be
	  // linked with the same key to multiple child tables, so the key might
	  // have already been set when processing a different child. This code
	  // assumes that no key columns in the parent are nullable, so a null in
	  // any column indicates that the key has not been generated.

	  if (l.generateKey)
	  {
		 if (parentRow.anyNull(l.parentKey))
		 {
			parentRow.setColumnValues(l.parentKey, keyGenerator.generateKey());
		 }
	  }
   }   

   void setParentKey(Row parentRow, Row childRow, LinkInfo l)
   {
	  parentRow.setColumnValues(l.parentKey,
								childRow.getColumnValues(l.childKey));
   }   

   void setChildKey(Row parentRow, Row childRow, LinkInfo l)
   {
	  childRow.setColumnValues(l.childKey,
							   parentRow.getColumnValues(l.parentKey));
   }   

   // ************************************************************************
   // Row and database methods
   // ************************************************************************

   void generateOrder(Row row, OrderInfo o, int orderInParent)
   {
	  // OrderInfo.generateOrder is false if OrderInfo.orderColumn is null.
	  if (o.generateOrder)
	  {
		 row.setColumnValue(o.orderColumn, new Integer(orderInParent));
	  }
   }   

   void insertRow(Table table, Row row)
	  throws SQLException
   {
	  PreparedStatement p;

	  p = map.checkOutInsertStmt(table);
	  parameters.setParameters(p, row, table.columns);
	  p.executeUpdate();

	  if (commitMode == COMMIT_AFTERINSERT)
	  {
		  map.commit();
	  }
	  map.checkInInsertStmt(p, table);
   }   

   Node getNextChild(Node child)
   {
	  // In the future, this will have to deal with passed-through nodes, where
	  // the next node is the first child of the passed-through node. Currently,
	  // we can just get the next logical sibling of the node.

	  return LogicalNodeUtils.getNextSibling(child);
   }   

   // ************************************************************************
   // General methods
   // ************************************************************************

   private void initGlobals()
   {
	  if (qualifier == null)
	  {
		 qualifier = new NameQualifierImpl();
	  }

	  parameters = new Parameters(map.dateFormatter, map.timeFormatter, map.timestampFormatter);
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
   //    table to the candidate key in the parent table.
   //
   // b) We don't know when the candidate key in the parent is set -- it could
   //    be generated or it could be retrieved from other properties, which
   //    might node be encountered until after the saved node is encountered.

   class FKNode
   {
	  Node   node;
	  Object map;
	  int    orderInParent;

	  FKNode (Node node, Object map, int orderInParent)
	  {
		 this.node = node;
		 this.map = map;
		 this.orderInParent = orderInParent;
	  }
   }
}