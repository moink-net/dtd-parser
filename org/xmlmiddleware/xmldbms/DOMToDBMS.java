
// Author: Sean Walter

package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Stack;
import java.util.StringTokenizer;
import java.sql.*;
import javax.sql.*;

import org.w3c.dom.*;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.domutils.*;

public class DOMToDBMS
{
    // The transfer info used for a session of storeDocument
    // TODO: We may have to remove this and make this entire thing
    // reentrant
    private TransferInfo m_transInfo;

    // The commit mode
    private int m_commitMode;

    // Stop on Database errors
    private boolean m_stopDBError;
    
    private SQLWarning m_sqlWarnings;
    private SQLException m_sqlExceptions;

    // Which actions to take for specific nodes
    private Actions m_actions;

    // Hash table of key generators
    private Hashtable m_keyGenerators;




    /**
     * Create a DOMToDBMS object. 
     * Defaults:
     * Commit mode: COMMIT_AFTERINSERT.
     * Stop on DB Errors: true.
     */
    public DOMToDBMS()
    {
        m_keyGenerators = new Hashtable();
        m_commitMode = COMMIT_AFTERINSERT;
        m_stopDBError = true;
    }



    /** 
     * Set the commit mode.
     * @param commitMode The mode.
     */
    public void setCommitMode(int commitMode)
    {
        m_commitMode = commitMode;
    }

    /** 
     * Get the commit mode.
     * @return The mode.
     */
    public int getCommitMode()
    {
        return m_commitMode;
    }



    /**
     * Associate a KeyGenerator with a name for later use.
     * @param name The name of the KeyGenenerator.
     * @param generator The KeyGenerator.
     */
    public void addKeyGenerator(String name, KeyGenerator generator)
    {
        m_keyGenerators.put(name, generator);
    }

    /**
     * Remove a previously associated KeyGenerator.
     * @param name The name of the KeyGenenerator.
     */
    public void removeKeyGenerator(String name)
    {
        m_keyGenerators.remove(name);
    }

    /**
     * Clear all key generators.
     * @param name The name of the KeyGenenerator.
     */
    public void removeAllKeyGenerators()
    {
        m_keyGenerators.clear();
    }



    /** 
     * Defines how SQLExceptions are handled. If set to false
     * then these are stored in a list (retrieved by getExceptions)
     * and processing of the document continues.
     *
     * When set to true then processing stops at first exception.
     */
    public void setDBErrorHandling(boolean stopOnError)
    {
        m_stopDBError = stopOnError;
    }

    /**
     * Returns a chain of all SQLWarnings issued during process
     * of the last document.
     */
    public SQLWarning getWarnings()
    {
        return m_sqlWarnings;
    }

    /** 
     * Returns a chain of all SQLExceptions issued during processing
     * of the last document. setDBErrorHandling must be set to false
     * for them to gather here.
     */
    public SQLException getExceptions()
    {
        return m_sqlExceptions;
    }


    public DocumentInfo processDocument(TransferInfo transInfo, Document doc, int action)
        throws SQLException, MapException, KeyException
    {
        return processDocument(transInfo, doc.getDocumentElement(), action);
    }

    processDocument(TransferInfo transInfo, Document doc, Actions actions)
        throws SQLException, MapException, KeyException
    {
        return processDocument(transInfo, doc.getDocumentElement(), actions);
    }

    public DocumentInfo processDocument(TransferInfo transInfo, Element el, int action)
        throws SQLException, MapException, KeyException
    {
        Actions actions = new Actions();
        actions.setDefault(action);

        return processDocument(transInfo, el, actions);
    }

    public DocumentInfo processDocument(TransferInfo transInfo, Element el, Actions actions)
        throws SQLException, MapException, KeyException
    {
        DocumentInfo docInfo = new DocumentInfo();
   
        // TODO: Make this reentrant
        m_transInfo = transInfo;
        m_sqlExceptions = null;
        m_sqlWarnings = null;
        m_actions = actions;
     


        // Call startDocument here
        Enumeration e = transInfo.dbActions.elements(); 
        while(e.hasMoreElements())
            ((DBAction)e.nextElement()).startDocument(m_commitMode);

        
        processRoot(docInfo, el, 1);


        // Call endDocument here
        Enumeration e = transInfo.dbActions.elements(); 
        while(e.hasMoreElements())
            ((DBAction)e.nextElement()).endDocument();
   

        // TODO: Do we have to do this?
        m_transInfo = null;

        return docInfo;
    }



    /**
     * Processes the nodes from the root of the DOM Document that are not mapped.
     * The tree is searched until a node is found that is mapped.
     *
     * @param el The node to recusively process.
     */
    private void processRoot(DocumentInfo docInfo, Element el, int orderInParent)
        throws SQLException, MapException, KeyException
    {
        // Get the default action (see note in processClassRow)
        // TODO: getDefault method not implemented
        Action action = m_actions.getDefault();

        // Check if the node is mapped
        ClassMap classMap = m_transInfo.map.getClassMap(el.getNamespaceURI(), el.getLocalName());

        if(classMap != null)
        {
            // Process the node
            Row row = processClassRow(null, classMap, null, (Node)el, orderInParent, action);

            // Add this row to the DocumentInfo
            Table table = classMap.getTable()
            Key priKey = table.getPrimaryKey();

            // TODO: What is the ordercolumn?
            docInfo.addInfo(table, priKey, row.getColumnValues(priKey.getColumns()), null);
        }

        // Otherwise treat like a pass through element
        else
        {
            NodeList children = el.getChildNodes();
            int childOrder = 1;

            for(int i = 0; i < children.getLength(); i++)
            {
                // Process any child elements, attributes and text are ignored at 
                // this point.
                if(children.item(i).getNodeType() == Node.ELEMENT_NODE)
                {
                    processRoot(docInfo, (Element)children.item(i), childOrder);
                    childOrder++;
                }
            }
        }
    }



    /**
     * This method creates and inserts a row in a class table. As part of the
     * process, it also processes all children of the node.
     * 
     * @param parentRow Parent class' row. May be null if root.
     * @param classMap Class to process.
     * @param relMap Relation between this class and it's parent. May be null if root.
     * @param classNode The node with data for this class.
     * @param orderInParent The order of this node within it's parent.
     */
    private Row processClassRow(Row parentRow, ClassMap classMap, RelatedClassMap relMap, 
                                Element classNode, int orderInParent, Action parentAction)
        throws SQLException, KeyException, MapException
    {
        // NOTE: This method is called from processRoot (relMap and parentRow 
        // will be null in this case) and processRow


        // ACTION NOTE: Actions work like so:
        // 
        // - This method receives the action of the parent class. For 
        // root classes this is the default action passed in to processDocument
        // - We see if m_actions or an action:Action attribute contains an action 
        // to override it with
        // - All children are processed with this action


        // A stack for all our children to be processed after insertion
	    Stack fkChildren = new Stack();

        Table table = classMap.getTable();

	    LinkInfo linkInfo = null;

        if(relMap != null)
            linkInfo = relMap.getLinkInfo();

        try
        {

            // This creates a row object and sets/generates keys
            Row classRow = createRow(table, parentRow, linkInfo);

            // Generate the order
            // TODO: How would we generate order on root elements?
            if(relMap != null)
	            generateOrder(classRow, relMap.getOrderInfo(), orderInParent);

            // Get the action for the node
            Action action = getActionFor(classNode);
            if(action == null)
            {
                // Copy the parent action. This is because
                // we don't want to move over UpdateProperties

                action = new Action(XMLName.create(classNode.getNamespaceURI(), 
                                                   classNode.getLocalName()), classMap);
                action.setAction(parentActon.getAction)
            }

            // Okay work on all the children. Children to be processed later
            // (due to key constraints) are put in the fkChildren stack.
	        processChildren(classRow, classMap, classNode, fkChildren, action);

            // Do the actual row insertion/update
	        storeRow(table, classRow, action);

            // Process children left till later
            processFKNodes(classRow, fkChildren, action);

        }
        catch(SQLException e)
        {
            // Note above that we skip all processing of dependent 
            // if this class row errors for any reason. 

            // TODO: Check how we handle if one of the children that this
            // row is dependent on errors. Do we handle this or just let
            // it blow with two errors (one for the child and then one here)?

            if(m_stopDBError)
                throw e;
            else
                pushException(e);
        }


        return classRow;
    }   

    /**
     * This method creates and inserts a row in a property table. 
     */
    private Row processPropRow(Row parentRow, PropertyMap propMap, Node propNode, 
                               int orderInParent, Action action)
        throws SQLException, MapException, KeyException
    {
        // NOTE: This method is called from processRow

        Table table = propMap.getTable();
        LinkInfo linkInfo = propMap.getLinkInfo();

        // Create the row object and set/generate keys
        Row propRow = createRow(table, parentRow, linkInfo);

        // Generate the order if necessary
	    generateOrder(propRow, propMap.getOrderInfo(), orderInParent);

        // Set the value
	    setPropertyColumn(propRow, propMap.getColumn(), propNode);
	  
        // Do actual row insertion/update

        // TODO: These properties need special care here
        // Implement DELETE/INSERT instead of UPDATE!
        storeRow(table, propRow, action);

        return propRow;
    }

    /**
     * Creates row object and does set up on keys for that row. 
     */
    private Row createRow(Table table, Row parentRow, LinkInfo linkInfo)
        throws KeyException
    {
        // NOTE: Called from processClassRow and processPropRow

        // KEY NOTE: Keys are generated and copied between rows 
        // in two places. That's createRow and processChild. Keys are 
        // generated for any key that has XMLDBMS key generation set. 
        // These can be UNIQUE or PRIMARY keys.
        //
        // createRow: 
        // - Generates primary keys in a table
        // - Generates child's unique keys in a relationship
        // - Copies parent's unique keys to child
        // 
        // processChild:
        // - Generates parent's unique keys in a relationship
        // - Copies child's unique keys to child



        // Create the actual row
	    Row row = new Row(table);


        // Check the primary keys on the table 
        Key priKey = table.getPrimaryKey();
        if(priKey != null)
        {
            // and generate if necessary
            if(priKey.getKeyGeneration() == Key.XMLDBMS)
                generateKey(row, priKey);
        }


        // If row has a parent ...
        if(parentRow != null && linkInfo != null)
        {
            // ... if that parent has the unique key ...
            if(linkInfo.parentKeyIsUnique())
            {
                // ... then copy the parent key to this row ...
                setChildKey(parentRow, row, linkInfo);
            }
            else
            {
                // ... otherwise generate it if necessary
                Key key = linkInfo.getChildKey();
                if(key.getKeyGeneration() == Key.XMLDBMS)
                    generateKey(row, key);

                // Key copied to parent row later (in processChild)
            }
        }


        return row;
    }


    /**
     * Processes rows that were left till later because of key constraints on
     * the parent table.
     */
    private void processFKNodes(Row parentRow, Stack fkNodes, Action action)
        throws SQLException, MapException, KeyException
    {
        // NOTE: Called from processClassRow and processPropRow

	    FKNode fkNode;

	    while(!fkNodes.empty())
	    {
		    fkNode = (FKNode)fkNodes.pop();
            processRow(parentRow, fkNode.map, fkNode.node, fkNode.orderInParent, action);
	    }
    }   

    
    /** 
     * Process children of a class.
     */
    private void processChildren(Row parentRow, ClassMap parentMap, Node parentNode, 
                                 Stack fkChildren, Action action)
        throws KeyException, SQLException, MapException
    {
        // NOTE: Called from processClassRow and called recursively for 
        // inline/wrapper classmaps

	    NodeList children = parentNode.getChildNodes();
        int childOrder = 1;

        for(int i = 0; i < children.getLength(); i++)
        {
	        Object childMap = null;
            Node child = children.item(i);

		    // Get the map for the node based on type
            switch(child.getNodeType())
            {
            case Node.TEXT_NODE:
			    childMap = parentMap.getPCDATAMap();
                break;
		    
            case Node.ELEMENT_NODE:
                childMap = parentMap.getChildMap(child.getNamespaceURI(), child.getLocalName());
                break;

            case Node.ATTRIBUTE_NODE:
                childMap = parentMap.getAttributeMap(m_nameQual.getQualifiedName(child));
                break;
		    }


            // If the child has been mapped, then process it. Otherwise, ignore.
		    if(childMap != null)
		    {
			    if(childMap instanceof InlineClassMap)
                {
                    // Store the order of this inline element
                    generateOrder(parentRow, ((InlineClassMap)childMap).getOrderInfo(), 
                                  childOrder);
                    
                    // For inline just recursively call this function with a 
                    // new parentNode, so as to process the grand-children just 
                    // like children
                    processChildren(parentRow, parentMap, child, fkChildren, action);
                }
                else if(childMap instanceof PropertyMap)
                {
                    // Properties first go through processProperty.
                    // If they are rows then they go through to processChild
                    processProperty(parentRow, (PropertyMap)childMap, child,
                                    childOrder, fkChildren, action);
                }
                else if(childMap instanceof RelatedClassMap)
                {
                    // Process the child node
                    processChild(parentRow, childMap, ((RelatedClassMap)childMap).getLinkInfo(), 
                                 child, childOrder, fkChildren, action);
                }

                childOrder++;
		    }
	    }
    }


    private static final String DUMMY = "dummy";

    /**
     * Adds a property to the class, or if in it's own row sends it for 
     * processing in processChild.
     */
    private void processProperty(Row parentRow, PropertyMap propMap, Node propNode, int order, Stack fkNodes, Action action)
        throws KeyException, SQLException, MapException
    {
        // NOTE: Called from processChildren

        // Generate the order column
        generateOrder(parentRow, propMap.getOrderInfo(), orderInParent);

        if(propMap.isTokenList())
        {
   			// If the attribute is multi-valued, then process each value as a
    		// separate attribute. We construct fake attributes for this
	    	// purpose; the names of these attributes are unimportant, as we
		    // already have the AttributeMap. Order refers to the order of the
   			// value in the attribute, not order of the attribute in the
    		// element (attributes are unordered).

            StringTokenizer s = new StringTokenizer(getNodeValue(propNode), " ", false);

            int tokenOrder = 1;
            Document doc = propNode.getOwnerDocument();

            // Create a new property map and copy all needed attributes
            PropertyMap tokenMap = PropertyMap.create(DUMMY, DUMMY, propMap.getType());

            tokenMap.setTable(propMap.getTable(), propMap.getLinkInfo());
            tokenMap.setColumn(propMap.getColumn());
            tokenMap.setOrderInfo(propMap.getTokenListOrderInfo());
            tokenMap.setIsTokenList(false);


			while(s.hasMoreElements())
			{
                Node tokenNode = null;

                switch(propNode.getNodeType())
                {
                case Node.ELEMENT_NODE:
                    tokenNode = doc.createElement(DUMMY);
                    break;
                case Node.ATTRIBUTE_NODE:
                    tokenNode = doc.createAttribute(DUMMY);
                    break;
                case Node.TEXT_NODE:
                    tokenNode = doc.createTextNode("");
                    break;
                };
             
                if(node != null)
                {
                    tokenNode.setNodeValue(s.nextToken());

                    // Now call ourselves with this property (no endless loop possible 
                    // because we clear the tokenList flag above)
                    processProperty(parentRow, tokenMap, tokenNode, tokenOrder, 
                                    fkNodes, action);

                    tokenOrder++;
                }
            }
        }


        // Non token properties go here
        else
        {
            // If mapped to the current table
            if(propMap.getTable() == null)
            {
                // And insert the value
			    setPropertyColumn(parentRow, propMap.getColumn(), propNode);
            }

            else 
            {
                // Otherwise it's a property from another table, so send for other processing
                processChild(parentRow, propMap, propMap.getLinkInfo(), propNode, 
                             orderInParent, fkNodes, action);
	        }
        }
    }


    /**
     * General function for processing child rows. 
     */
    private void processChild(Row parentRow, Object map, LinkInfo linkInfo, Node node, 
                              int orderInParent, Stack fkNodes, Action action)
        throws KeyException, SQLException, MapException
    {
        // NOTE: This method is called before parentRow has been inserted into the
        // database
        // Called from processChildren and processProperty


        if(linkInfo.parentKeyIsUnique())
        {
            // Generate parent key if if necessary
            Key key = linkInfo.getChildKey();
            if(key.getKeyGeneration() == Key.XMLDBMS)
                generateKey(row, key);

            // Key copied to child row later (in createRow)

            // keep the node for later processing
            fkNodes.push(new FKNode(node, map, orderInParent));
        }
        else
        {
            // Otherwise the child key is unique and we must copy the key
            // back to the parentRow. So go ahead and process

            Row childRow = processRow(parentRow, map, node, orderInParent, action);
            setParentKey(parentRow, childRow, linkInfo);
        }
    }

    
    /**
     * When it's actually time for a row to get inserted this sends it to the 
     * appropriate location.
     */
    private Row processRow(Row parentRow, Object map, Node node, int orderInParent, 
                           Action action)
        throws SQLException, MapException, KeyException
    {
        // NOTE: Called from processChild and processFKNodes

        if(map instanceof PropertyMap)
			return processPropRow(parentRow, (PropertyMap)map, node, orderInParent, action);

        else // if (fkNode.map instanceof RelatedClassMap)
        {
            RelatedClassMap relMap = (RelatedClassMap)map;

            // The action here is used as the parentAction
    	    return processClassRow(parentRow, relMap.getClassMap(), relMap, node, 
                                   orderInParent, action);
        }
    }





    public static Action getActionFor(Element el)
        throws MapException
    {
        // TODO: When action: attributes are implemented put code here

        return m_actions.getAction(el.getNamespaceURI(), el.getLocalName());
    }



    private String getNodeValue(Node propNode)
    {
	    String s;

	    if(propNode.getNodeType() == Node.ELEMENT_NODE)
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

	    if(m_transInfo.map.emptyStringIsNull() && s.length() == 0)
			s = null;
        
        return s;
    }


    private void setPropertyColumn(Row row, Column column, Node node)
    {
        StringFormatter formatter = column.getFormatter();
        String val = getNodeValue(node);
        row.setColumnValue(column, formatter.parse(val, column.getType()));
    }
    

    private void setParentKey(Row parentRow, Row childRow, LinkInfo l)
    {
	    parentRow.setColumnValues(l.getParentKey().getColumns(),
								  childRow.getColumnValues(l.getChildKey().getColumns()));
    }

    private void setChildKey(Row parentRow, Row childRow, LinkInfo l)
    {
	    childRow.setColumnValues(l.getChildKey().getColumns(),
							     parentRow.getColumnValues(l.getParentKey().getColumns()));
    }


    private void generateKey(Row row, Key key)
        throws KeyException
    {
        KeyGenerator keyGen = (KeyGenerator)m_keyGenerators.get(key.getKeyGeneratorName());

        if(keyGen == null)
            throw new KeyException("Invalid key generator: " + key.getKeyGeneratorName());
    
        Columns[] columns = key.getColumns();
        Object[] values = keyGen.generateKey();

        if(columns.length != values.length)
            throw new KeyException("Invalid number of columns generated key generator: " + key.getKeyGeneratorName());

        for(int i = 0; i < columns.length; i++)
        {
            // loop for each generated key column value
            row.setColumnValue(columns[i], ConvertJDBCObject.toObject(values[i], columns[i].getType()));
        }
    }

    private void generateOrder(Row row, OrderInfo o, int orderInParent)
    {
	    if(o != null && !o.orderValueIsFixed() && o.generateOrder())
            row.setColumnValue(column, ConvertJDBCObject.toObject(new Integer(orderValue), column.getType());
    }   


    private void storeRow(Table table, Row row, Action action)
	    throws SQLException, MapException
    {
        
        int act = action.getAction();
        boolean soft = false;

        // Get the database 
        // TODO: (What about the 'null' or default database?)
        DBAction dbAct = m_transInfo.getDBAction(table.getDatabaseName());

        if(dbAct == null)
            throw new MapException("Database '" + table.getDatabaseName() + "' not set.");

        try
        {
            switch(act)
            {
            case Action.NONE:
                break;

            case Action.SOFTINSERT:
                soft = true;
            case Action.INSERT:
                dbAct.insert(table, row);
                break;

            case Action.UPDATEORINSERT:
                dbAct.updateOrInsert(table, row);
                break;

            case Action.UPDATE:
                dbAct.update(table, row);
                break;

            case Action.SOFTDELETE:
                soft = true;
            case Action.DELETE:
                dbAct.delete(table, row);
                break;
        }
        catch(SQLException e)
        {
            if(soft)
                pushWarning(new SQLWarning(e.getMessage(), e.getSQLState());
            else
                throw e;
        }
            
    }


    public void pushException(SQLException e)
    {
        // TODO: What order do we chain the exceptions?

        if(m_sqlExceptions == null)
            m_sqlExceptions = e;
        else
            m_sqlExceptions.setNextException(e);
    }


    public void pushWarning(SQLWarning w)
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


