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

import java.lang.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import java.util.StringTokenizer;
import java.sql.*;
import javax.sql.*;

import org.w3c.dom.*;

import org.xmlmiddleware.domutils.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.conversions.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.actions.*;
/**
 * Transfers data from the database to a DOM tree.
 *
 * <P>DOMToDBMS transfers data to the database from a DOM tree according
 * to a particular Map. The caller must provide a TransferInfo object
 * (which contains the map and database handlers specific to certain 
 * databases), a DOM Element or Document, and one or more actions to be
 * taken when storing this data in the database.</P>
 *
 * <P>For example, the following code transfers data from a DOM tree to 
 * the a generic database, using the specified map:</P>
 *
 * TODO: Make an example.
 *
 * TODO: Any more docs here
 * 
 * @author Sean Walter
 * @version 2.0
 */

public class DOMToDBMS
{
    // ************************************************************************
    // Private variables
    // ************************************************************************

    // The transfer info used for a session of processDocument
    private TransferInfo m_transInfo;

    // The commit mode (see processRoot)
    private int m_commitMode;

    // Stop on Database errors (see processClassRow)
    private boolean m_stopDBError;

    // Stored Exceptions and Warnings (see getExceptions and getWarnings)
    private SQLWarning m_sqlWarnings;
    private SQLException m_sqlExceptions;

    // Which actions to take for specific nodes
    private Actions m_actions;

    // Hash table of key generators
    private Hashtable m_keyGenerators;

    // Cached objects. These are here for efficiency (see getAllPropertyMaps)
    private Hashtable m_classAllProperties;

    private static final String DUMMY = "dummy";

    // ************************************************************************
    // Constructors
    // ************************************************************************

    /**
     * Create a DOMToDBMS object. 
     */
    public DOMToDBMS()
    {
        m_keyGenerators = new Hashtable();
        m_commitMode = DataHandler.COMMIT_AFTERSTATEMENT;
        m_stopDBError = true;

        m_classAllProperties = new Hashtable();
    }



    // ************************************************************************
    // Public methods
    // ************************************************************************

    /** 
     * Set the commit mode.
     *
     * @param commitMode The mode.
     */
    public void setCommitMode(int commitMode)
    {
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
     * Associate a KeyGenerator with a name for later use.
     *
     * @param name The name of the KeyGenenerator.
     * @param generator The KeyGenerator.
     */
    public void addKeyGenerator(String name, KeyGenerator generator)
    {
        m_keyGenerators.put(name, generator);
    }

    /**
     * Remove a previously associated KeyGenerator.
     *
     * @param name The name of the KeyGenenerator.
     */
    public void removeKeyGenerator(String name)
    {
        m_keyGenerators.remove(name);
    }

    /**
     * Clear all key generators.
     *
     * @param name The name of the KeyGenenerator.
     */
    public void removeAllKeyGenerators()
    {
        m_keyGenerators.clear();
    }


    /** 
     * Defines how SQLExceptions are handled. If set to false
     * then these are stored in a list (retrieved by getExceptions)
     * and processing of the document continues at the next class.
     * When set to true then processing stops at first exception.
     *
     * @param stopOnError Stop or not.
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

    /**
     * Store DOM tree in the database. Processing will begin at nodes mapped to classes
     * in the map.
     *
     * @param transInfo TransferInfo object containing map and DataHandler's.
     * @param doc The DOM document to process.
     * @param action Action to take on the document.
     */
    public DocumentInfo processDocument(TransferInfo transInfo, Document doc, int action)
        throws SQLException, MapException, KeyException, ConversionException
    {
        return processDocument(transInfo, doc.getDocumentElement(), action);
    }

    /**
     * Store DOM tree in the database. Processing will begin at nodes mapped to classes
     * in the map.
     *
     * @param transInfo TransferInfo object containing map and DataHandler's.
     * @param doc The DOM document to process.
     * @param action Actions to take on various elements of the document.
     */
    public DocumentInfo processDocument(TransferInfo transInfo, Document doc, Actions actions)
        throws SQLException, MapException, KeyException, ConversionException
    {
        return processDocument(transInfo, doc.getDocumentElement(), actions);
    }

    /**
     * Store DOM tree in the database. Processing will begin at nodes mapped to classes
     * in the map.
     *
     * @param transInfo TransferInfo object containing map and DataHandler's.
     * @param el Root of DOM tree to process.
     * @param action Action to take on the tree.
     */
    public DocumentInfo processDocument(TransferInfo transInfo, Element el, int action)
        throws SQLException, MapException, KeyException, ConversionException
    {
        Action act = new Action();
        act.setAction(action);

        Actions actions = new Actions(transInfo.map);
        actions.setDefaultAction(act); 

        return processDocument(transInfo, el, actions);
    }

    /**
     * Store DOM tree in the database. Processing will begin at nodes mapped to classes
     * in the map.
     *
     * @param transInfo TransferInfo object containing map and DataHandler's.
     * @param el Root of DOM tree to process.
     * @param action Actions to take on various elements of the tree.
     */
    public DocumentInfo processDocument(TransferInfo transInfo, Element el, Actions actions)
        throws SQLException, MapException, KeyException, ConversionException
    {
        DocumentInfo docInfo = new DocumentInfo();
   
        // TODO: Make this reentrant
        m_transInfo = transInfo;
        m_sqlExceptions = null;
        m_sqlWarnings = null;
        m_actions = actions;
     


        // Call startDocument here
        Enumeration e = transInfo.dataHandlers.elements(); 
        while(e.hasMoreElements())
            ((DataHandler)e.nextElement()).startDocument(m_commitMode);

        
        processRoot(docInfo, el, 1);


        // Call endDocument here
        e = transInfo.dataHandlers.elements(); 
        while(e.hasMoreElements())
            ((DataHandler)e.nextElement()).endDocument();
   

        // TODO: Do we have to do this?
        m_transInfo = null;

        return docInfo;
    }



    // ************************************************************************
    // Main Processing Methods
    // ************************************************************************

    /**
     * Processes the nodes from the root of the DOM Document that are not mapped.
     * The tree is searched until a node is found that is mapped.
     *
     * @param docInfo DocumentInfo to add root elements to.
     * @param el The node to recusively process.
     * @param orderInParent Position of this element in parent
     */
    private void processRoot(DocumentInfo docInfo, Element el, long orderInParent)
        throws SQLException, MapException, KeyException, ConversionException
    {
        // Check if the node is mapped
        ClassMap classMap = m_transInfo.map.getClassMap(el.getNamespaceURI(), el.getLocalName());

        if(classMap != null)
        {
            // Process the node
            Row row = processClassRow(null, classMap, null, el, orderInParent);

            if(row != null)
            {
                // Add this row to the DocumentInfo
                Table table = classMap.getTable();
                Key priKey = table.getPrimaryKey();
    
                // TODO: What is the ordercolumn?
                docInfo.addInfo(table, priKey.getColumns(), row.getColumnValues(priKey.getColumns())/*, null*/);
            }
        }

        // Otherwise treat like a pass through element
        else
        {
            NodeList children = el.getChildNodes();
            long childOrder = 1;

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
     * @param parentAction Action to inherit if this node does not specify own action.
     */
    private Row processClassRow(Row parentRow, ClassMap classMap, RelatedClassMap relMap, 
                                Element classNode, long orderInParent)
        throws SQLException, KeyException, MapException, ConversionException
    {
        // NOTE: This method is called from processRoot (relMap and parentRow 
        // will be null in this case) and processRow


        // Get the action for the node
        Action action = getActionFor(classNode);

        // A stack for all our children to be processed after insertion
	    Stack fkChildren = new Stack();

        Table table = classMap.getTable();

	    LinkInfo linkInfo = null;

        if(relMap != null)
            linkInfo = relMap.getLinkInfo();


        // Get the list of columns we are to use
        Vector useProps = getUseProps(classMap, action);

        // This creates a row object, clears it, and sets/generates keys
        Row classRow = createRow(table, parentRow, linkInfo, useProps);


        try
        {
            // Generate the order
            // TODO: How would we generate order on root elements?
            if(relMap != null)
	            generateOrder(classRow, relMap.getOrderInfo(), orderInParent);


            // Okay work on all the children. Children to be processed later
            // (due to key constraints) are put in the fkChildren stack.
	        processChildren(classRow, classMap, classNode, fkChildren, action, useProps);


            // Clean up row before storing
            finalizeRow(classRow, useProps, action);


            // Do the actual row insertion/update
	        storeRow(table, classRow, action.getAction());


            // Now delete all out-of-table properties
            clearNonTableProps(classRow, useProps, action);

            
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
                               long orderInParent, Action action)
        throws SQLException, MapException, KeyException, ConversionException
    {
        // NOTE: This method is called from processRow

        Table table = propMap.getTable();
        LinkInfo linkInfo = propMap.getLinkInfo();

        // Create the row object and set/generate keys
        Row propRow = createRow(table, parentRow, linkInfo, null);

        // Generate the order if necessary
	    generateOrder(propRow, propMap.getOrderInfo(), orderInParent);

        // Set the value
	    setPropertyColumn(propRow, propMap, propNode);
	  
        int act = action.getAction();

        // Note that these rows are always inserted . 
        if(act == Action.UPDATE || act == Action.UPDATEORINSERT)
            act = Action.INSERT;

        // Also note that Action.NONE falls through above

        // Do actual row insertion
        storeRow(table, propRow, act);

        return propRow;
    }


    /**
     * Creates row object and does set up on keys for that row. 
     */
    private Row createRow(Table table, Row parentRow, LinkInfo linkInfo, Vector useProps)
        throws KeyException, ConversionException
    {
        // NOTE: Called from processClassRow and processPropRow

        // KEY NOTE: Keys are generated and copied between rows 
        // in two places. That's createRow and processChildRow. Keys are 
        // generated for any key that has XMLDBMS key generation set. 
        // These can be UNIQUE or PRIMARY keys.
        //
        // createRow: 
        // - Generates primary keys in a table
        // - Generates child's unique keys in a relationship
        // - Copies parent's unique keys to child
        // 
        // processChildRow:
        // - Generates parent's unique keys in a relationship
        // - Copies child's unique keys to child



        // Create the actual row
	    Row row = new Row();


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

                // Key copied to parent row later (in processChildRow)
            }
        }


        return row;
    }


    /**
     * Called before row in stored. Sets missing fields to null
     */
    private void finalizeRow(Row classRow, Vector useProps, Action action)
        throws ConversionException
    {
       // Set to null all in-table property columns that aren't set yet
       for(int i = 0; i < useProps.size(); i++)
       {
            PropertyMap propMap = (PropertyMap)useProps.elementAt(i);
            if(propMap.getTable() == null)
            {
                if(!classRow.haveColumn(propMap.getColumn()))
                    classRow.setColumnValue(propMap.getColumn(), null);
            }
        }
    }


    /**
     * Clears out-of-table properties' rows.
     */
    private void clearNonTableProps(Row classRow, Vector useProps, Action action)
        throws SQLException, MapException, KeyException, ConversionException
    {
        // Only clear for UPDATE's
        if(action.getAction() == Action.UPDATE ||
           action.getAction() == Action.UPDATEORINSERT)
        {
            for(int i = 0; i < useProps.size(); i++)
            {
                PropertyMap propMap = (PropertyMap)useProps.elementAt(i);
    
                Table table = propMap.getTable();
                if(table != null)
                {
                    // NOTE: We assume here than the parent key in the link is unique. 
                    // This should be correct.

                    LinkInfo li = propMap.getLinkInfo();
    
                    // This should create the row and copy the keys from parent row
                    Row row = createRow(table, classRow, li, null);

                    // Delete it!
                    deleteRow(table, row, li.getChildKey(), true);
                }
            }
        }
    }


    /**
     * Processes rows that were left till later because of key constraints on
     * the parent table.
     */
    private void processFKNodes(Row parentRow, Stack fkNodes, Action action)
        throws SQLException, MapException, KeyException, ConversionException
    {
        // NOTE: Called from processClassRow and processPropRow

	    FKNode fkNode;

        // Note that instead of processing this stack LIFO we do FIFO

	    while(!fkNodes.empty())
	    {
		    fkNode = (FKNode)fkNodes.remove(0);
            processRow(parentRow, fkNode.map, fkNode.node, fkNode.orderInParent, action);
        }
    }   

    
    /** 
     * Process children of a class.
     */
    private void processChildren(Row parentRow, ClassMap parentMap, Node parentNode, 
                                 Stack fkChildren, Action action, Vector useProps)
        throws KeyException, SQLException, MapException, ConversionException
    {
        // NOTE: Called from processClassRow and called recursively for 
        // inline/wrapper classmaps

        Node child = DOMNormalizer.getFirstChild(parentNode);
        long childOrder = 1;

        while(child != null)
        {
	        Object childMap = null;

		    // Get the map for the node based on type
            switch(child.getNodeType())
            {
            case Node.TEXT_NODE:
			    childMap = parentMap.getPCDATAMap();
                break;
		    
            case Node.ELEMENT_NODE:
                childMap = parentMap.getChildMap(child.getNamespaceURI(), child.getLocalName());
                break;

		    }

            processChild(parentRow, parentMap, childMap, child, childOrder, 
                         fkChildren, action, useProps);


            // TODO: Should this be in the brace above? That is should
            // it only increment for mapped nodes. Currently copying 
            // behavior in v1.0
            childOrder++;

	        child = DOMNormalizer.getNextSibling(child);
	    }


        // Attributes get processed separately
        NamedNodeMap attrs = parentNode.getAttributes();
        if(attrs != null)
        {
            for(int i = 0; i < attrs.getLength(); i++)
            {
	            Object childMap = null;

                child = attrs.item(i);
                childMap = parentMap.getAttributeMap(child.getNamespaceURI(), child.getLocalName());

                processChild(parentRow, parentMap, childMap, child, childOrder, 
                             fkChildren, action, useProps);

                childOrder++;
            }
        }
    }


    /** 
     * This is the other half of processChildren. Called for every node that was mapped.
     */
    private void processChild(Row parentRow, ClassMap parentMap, Object childMap, Node childNode,
                              long childOrder, Stack fkChildren, Action action, Vector useProps)
        throws KeyException, SQLException, MapException, ConversionException
    {
        // NOTE: Called from processChildren

        if(childMap == null)
        {
            return;
        }
        else if(childMap instanceof InlineClassMap)
        {
            // Store the order of this inline element
            generateOrder(parentRow, ((InlineClassMap)childMap).getOrderInfo(), 
                          childOrder);
                    
            // For inline just recursively call this function with a 
            // new parentNode, so as to process the grand-children just 
            // like children
            processChildren(parentRow, parentMap, childNode, fkChildren, action, useProps);
        }
        else if(childMap instanceof PropertyMap)
        {
            // Only process if this property is being used (useProps)
            if(useProps.contains(childMap))

                // Properties first go through processProperty.
                // If they are rows then they go through to processChildRow
                processProperty(parentRow, (PropertyMap)childMap, childNode,
                                childOrder, fkChildren, action);
        }
        else if(childMap instanceof RelatedClassMap)
        {
            // Process the child node
            processChildRow(parentRow, childMap, ((RelatedClassMap)childMap).getLinkInfo(), 
                            childNode, childOrder, fkChildren, action);
        }
    }


    /**
     * Adds a property to the class, or if in it's own row sends it for 
     * processing in processChildRow.
     */
    private void processProperty(Row parentRow, PropertyMap propMap, Node propNode, long order, Stack fkNodes, Action action)
        throws KeyException, SQLException, MapException, ConversionException
    {
        // NOTE: Called from processChildren

        // Generate the order column
        generateOrder(parentRow, propMap.getOrderInfo(), order);

        if(propMap.isTokenList())
        {
   			// If the attribute is multi-valued, then process each value as a
    		// separate attribute. We construct fake attributes for this
	    	// purpose; the names of these attributes are unimportant, as we
		    // already have the AttributeMap. Order refers to the order of the
   			// value in the attribute, not order of the attribute in the
    		// element (attributes are unordered).

            StringTokenizer s = new StringTokenizer(getNodeValue(propNode, propMap.containsXML()), " ", false);

            long tokenOrder = 1;
            Document doc = propNode.getOwnerDocument();

            // Create a new property map and copy all needed attributes
            PropertyMap tokenMap = PropertyMap.create(DUMMY, DUMMY, propMap.getType());

            tokenMap.setTable(propMap.getTable(), propMap.getLinkInfo());
            tokenMap.setColumn(propMap.getColumn());
            tokenMap.setIsTokenList(false);

            if(propMap.getType() != PropertyMap.ATTRIBUTE)
                tokenMap.setOrderInfo(propMap.getTokenListOrderInfo());



			while(s.hasMoreElements())
			{
                Node tokenNode = null;

                switch(propNode.getNodeType())
                {
                // 
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
                };
             
                if(tokenNode != null)
                {
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
			    setPropertyColumn(parentRow, propMap, propNode);
            }

            else 
            {
                // Otherwise it's a property from another table, so send for other processing
                processChildRow(parentRow, propMap, propMap.getLinkInfo(), propNode, 
                                order, fkNodes, action);
	        }
        }
    }


    /**
     * General function for processing child rows. 
     */
    private void processChildRow(Row parentRow, Object map, LinkInfo linkInfo, Node node, 
                                 long orderInParent, Stack fkNodes, Action action)
        throws KeyException, SQLException, MapException, ConversionException
    {
        // NOTE: This method is called before parentRow has been inserted into the
        // database
        // Called from processChild and processProperty


        if(linkInfo.parentKeyIsUnique())
        {
            // Generate parent key if if necessary
            Key key = linkInfo.getChildKey();
            if(key.getKeyGeneration() == Key.XMLDBMS)
            {
                // Check if parent already generated (by another 
                // multi-valued property for instance)
                if(!parentRow.haveColumns(key.getColumns()))
                    generateKey(parentRow, key);
            }

            // Key copied to child row later (in createRow)

            // keep the node for later processing
            fkNodes.push(new FKNode(node, map, orderInParent));
        }
        else
        {
            // Otherwise the child key is unique 
            Row childRow = processRow(parentRow, map, node, orderInParent, action);
            
            if(childRow != null)
            {
                // In this case We must copy the key back to the parent
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
        throws SQLException, MapException, KeyException, ConversionException
    {
        // NOTE: Called from processChildRow and processFKNodes

        if(map instanceof PropertyMap)
        {
            return processPropRow(parentRow, (PropertyMap)map, node, orderInParent, action);
        }
        else // if (fkNode.map instanceof RelatedClassMap)
        {
            RelatedClassMap relMap = (RelatedClassMap)map;

            return processClassRow(parentRow, relMap.getClassMap(), relMap, (Element)node, 
                                   orderInParent);
        }
    }



    // ************************************************************************
    // Helper methods
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
    private Vector getAllPropertyMaps(ClassMap classMap)
    {
        // TODO: Test if caching this is worth it!
        if(m_classAllProperties.contains(classMap))
            return (Vector)m_classAllProperties.get(classMap);


        // Q: Could this be moved to ClassMap for efficiency?
        // A: Nope, it can't because ClassMap needs to be threadsafe. 
        Vector v = new Vector();
        PropertyMap propMap;

        // PCDATAMap
        propMap = classMap.getPCDATAMap();
        if(propMap != null)
            v.addElement(propMap);

        // Attributes
        Enumeration e = classMap.getAttributeMaps();
        while(e.hasMoreElements())
            v.addElement(e.nextElement());

        // Child Elements
        e = classMap.getChildMaps();
        while(e.hasMoreElements())
        {
            Object map = e.nextElement();
            if(map instanceof PropertyMap)
                v.addElement(map);
        }

        // Cache
        m_classAllProperties.put(classMap, v);

        return v;
    }


    /**
     * Gets the action for a given element.
     */
    private Action getActionFor(Element el)
        throws MapException
    {
        Action action = m_actions.getAction(el.getNamespaceURI(), el.getLocalName());

        if(action == null)
            action = m_actions.getDefaultAction();

        if(action == null)
            throw new MapException("No default action specified.");

        // TODO: When action: attributes are implemented put code here

        return action;
    }


    /**
     * Get (maybe serialized) value for a node.
     */
    private String getNodeValue(Node propNode, boolean containsXML)
    {
	    String s;

	    if(propNode.getNodeType() == Node.ELEMENT_NODE)
	    {
		    // If the property is stored in an element, then the property's
		    // value is the the element's contents, serialized as XML.

		    s = DOMNormalizer.serialize(propNode, true, containsXML);
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


    /** 
     * Set the value for column
     */
    private void setPropertyColumn(Row row, PropertyMap propMap, Node node)
        throws ConversionException
    {
        Column column = propMap.getColumn();

        // Get the value and format.
        StringFormatter formatter = column.getFormatter();
        Object val = formatter.parse(getNodeValue(node, propMap.containsXML()), column.getType());

        // Set in in the column
        row.setColumnValue(column, val);
    }
    

    /**
     * Copy the child key to parent row.
     */
    private void setParentKey(Row parentRow, Row childRow, LinkInfo l)
        throws KeyException, ConversionException
    {
        Column[] childCols = l.getChildKey().getColumns();

        // TODO: Better and more descriptive error message

        if(!childRow.haveColumns(childCols))
            throw new KeyException("Cannot copy child key. It's not set.");

	    parentRow.setColumnValues(l.getParentKey().getColumns(),
								  childRow.getColumnValues(childCols));
    }


    /**
     * Copy the parent key to a child row.
     */
    private void setChildKey(Row parentRow, Row childRow, LinkInfo l)
        throws KeyException, ConversionException
    {
        Column[] parentCols = l.getParentKey().getColumns();

        // TODO: Better and more descriptive error message

        if(!parentRow.haveColumns(parentCols))
            throw new KeyException("Cannot copy parent key. It's not set.");

	    childRow.setColumnValues(l.getChildKey().getColumns(),
							     parentRow.getColumnValues(parentCols));
    }


    /**
     * Use a KeyGenerator to generate a key.
     */
    private void generateKey(Row row, Key key)
        throws KeyException, ConversionException
    {
        KeyGenerator keyGen = (KeyGenerator)m_keyGenerators.get(key.getKeyGeneratorName());

        if(keyGen == null)
            throw new KeyException("Invalid key generator: " + key.getKeyGeneratorName());
    
        Column[] columns = key.getColumns();
        Object[] values = keyGen.generateKey();

        if(columns.length != values.length)
            throw new KeyException("Invalid number of columns generated key generator: " + key.getKeyGeneratorName());

        for(int i = 0; i < columns.length; i++)
        {
            // loop for each generated key column value
            row.setColumnValue(columns[i], ConvertObject.convertObject(values[i], columns[i].getType(), columns[i].getFormatter()));
        }
    }

    /** 
     * Put the order value in a row.
     */
    private void generateOrder(Row row, OrderInfo o, long orderValue)
        throws ConversionException
    {
	    if(o != null && !o.orderValueIsFixed() && o.generateOrder())
        {
            Column col = o.getOrderColumn();
            row.setColumnValue(col, ConvertObject.convertObject(new Long(orderValue), col.getType(), col.getFormatter()));
        }
    }   


    /**
     * Send a row to a DataHandler for processing.
     */
    private void storeRow(Table table, Row row, int action)
	    throws SQLException, MapException
    {
        
        boolean soft = false;

        // Get the database 
        // TODO: (What about the 'null' or default database?)
        DataHandler dataHandler = m_transInfo.getDataHandler(table.getDatabaseName());

        if(dataHandler == null)
            throw new MapException("Database '" + table.getDatabaseName() + "' not set.");

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
                throw new MapException("DELETE and SOFTDELETE actions cannot be used with DOMToDBMS.");

            default:
                throw new MapException("Invalid action in storeRow.");
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
     * Delete a row or rows.
     */
    private void deleteRow(Table table, Row row, Key key, boolean soft)
	    throws SQLException, MapException
    {
        // Get the database 
        // TODO: (What about the 'null' or default database?)
        DataHandler dataHandler = m_transInfo.getDataHandler(table.getDatabaseName());

        if(dataHandler == null)
            throw new MapException("Database '" + table.getDatabaseName() + "' not set.");

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
	    long    orderInParent;

	    FKNode (Node node, Object map, long orderInParent)
	    {
		    this.node = node;
		    this.map = map;
		    this.orderInParent = orderInParent;
	    }
    }
}


