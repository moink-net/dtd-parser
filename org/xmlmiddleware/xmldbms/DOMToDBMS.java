
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
    public static final int COMMIT_AFTERINSERT = 1;
    public static final int COMMIT_AFTERDOCUMENT = 2;
    public static final int COMMIT_NONE = 3;
    public static final int COMMIT_NOTRANSACTIONS = 4;


    // TODO: we have a mode variable here for update/insert 

    private Map m_map;
    private Document m_document;

    private NameQualifier m_nameQual;

    // Hash table of DataInfo objects (see end)
    private Hashtable m_dataSources;

    // Hash table of key generators
    private Hashtable m_keyGenerators;

    private int m_commitMode;




    public DOMToDBMS()
    {
        m_dataSources = new Hashtable();
        m_nameQual = new NameQualifierImpl();

        // TODO: What's the default commit mode?
        m_commitMode = COMMIT_AFTERINSERT;
    }

    public DOMToDBMS(NameQualifier nq)
    {
        m_dataSources = new Hashtable();
        m_nameQual = nq;

        // TODO: What's the default commit mode?
        m_commitMode = COMMIT_AFTERINSERT;
    }
    

    public void setCommitMode(int mode)
    {
        // TODO: Should we check the value?
        m_commitMode = mode;
    }

    public int getCommitMode()
    {
        return m_commitMode;
    }



    /**
     * Adds a data source
     *
     * @param databaseName Name used in map for this database.
     * @param dataSource A DataSource object to retrieve connections from.
     * @param user User to connect to the database as.
     * @param password Password to connect to the database with.
     */
    public void addDataSource(String databaseName, DataSource dataSource, 
                         String user, String password)
        throws SQLException
    {
        m_dataSources.put(databaseName, new DataInfo(dataSource, user, password));
    }

    /**
     * Remove a set of data base info previously added.
     * 
     * @param databaseName Named used in map for this database.
     */
    public void removeDataSource(String databaseName)
    {
        m_dataSources.remove(databaseName);
    }

    /**
     * Clear all database connection info previously added.
     *
     */
    public void removeAllDataSources()
    {
        m_dataSources.clear();
    }


    public void addKeyGenerator(String name, KeyGenerator generator)
    {
        m_keyGenerators.put(name, generator);
    }

    public void removeKeyGenerator(String name)
    {
        m_keyGenerators.remove(name);
    }

    public void removeAllKeyGenerators()
    {
        m_keyGenerators.clear();
    }



    // TODO: Needs to return a DocumentInfo
    public /*DocumentInfo*/ void storeDocument(Map map, Element el)
        throws SQLException, MapException, KeyException
    {
        // TODO: Make this reentrant by passing map around
        m_map = map;

        processRoot(el, 1);

        // TODO: Do we have to do this?
        m_map = null;


        // TODO: what do we do about COMMIT_NONE?

        if(m_commitMode == COMMIT_AFTERDOCUMENT)
        {
            Enumeration datas = m_dataSources.elements();

            while(datas.hasMoreElements())
            {
                DataInfo data = (DataInfo)datas.nextElement();
                if(data.used)
                    data.conn.commit();
            }
        }
                    
        //TODO: Do the return value DocumentInfo thingy
    }


    /**
     * Processes the nodes from the root of the DOM Document that are not mapped.
     * The tree is searched until a node is found that is mapped.
     *
     * @param el The node to recusively process.
     */
    private void processRoot(Element el, int orderInParent)
        throws SQLException, MapException, KeyException
    {
        // Check if the node is mapped
        ClassMap classMap = m_map.getClassMap(m_nameQual.getQualifiedName((Node)el));

        if(classMap != null)
        {
            // Process the node
            processClassRow(null, classMap, null, (Node)el, orderInParent);

            // TODO: get the row and set up a document info
            // TODO: We need to be able to handle a document info with 
            // multiple root nodes
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
                    processRoot((Element)children.item(i), childOrder);
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
                                Node classNode, int orderInParent)
        throws SQLException, KeyException, MapException
    {
        // NOTE: This method is called from processRoot (relMap and parentRow 
        // will be null in this case) and processRow

        // A stack for all our children to be processed after insertion
	    Stack fkChildren = new Stack();


        Table table = classMap.getTable();

	    LinkInfo linkInfo = null;

        if(relMap != null)
            linkInfo = relMap.getLinkInfo();


        // This creates a row object and sets/generates keys
        Row classRow = createRow(table, parentRow, linkInfo);

        // Generate the order
        // TODO: How would we generate order on root elements?
        if(relMap != null)
	        generateOrder(classRow, relMap.getOrderInfo(), orderInParent);

        // Okay work on all the children. Children to be processed later
        // (due to key constraints) are put in the fkChildren stack.
	    processChildren(classRow, classMap, classNode, fkChildren);

        // Do the actual row insertion/update
	    insertRow(table, classRow);

        // Process children left till later
        processFKNodes(classRow, fkChildren);


        return classRow;
    }   

    /**
     * This method creates and inserts a row in a property table. 
     */
    private Row processPropRow(Row parentRow, PropertyMap propMap, Node propNode, int orderInParent)
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
        insertRow(table, propRow);

        return propRow;
    }

    /**
     * Creates row object and does set up on keys for that row. 
     */
    private Row createRow(Table table, Row parentRow, LinkInfo linkInfo)
        throws KeyException
    {
        // NOTE: Called from processClassRow and processPropRow

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
            if(linkInfo.parentKeyIsUnique())
            {
                // Copy the parent key to this row
                setChildKey(parentRow, row, linkInfo);
            }
            else
            {
                // Generate a key, (it's copied back to the parent in processChild)
                Key childKey = linkInfo.getChildKey();
                if(childKey.getKeyGeneration() == Key.XMLDBMS)
                    generateKey(row, childKey);
            }
        }

        return row;
    }


    /**
     * Processes rows that were left till later because of key constraints on
     * the parent table.
     */
    private void processFKNodes(Row parentRow, Stack fkNodes)
        throws SQLException, MapException, KeyException
    {
        // NOTE: Called from processClassRow and processPropRow

	    FKNode fkNode;

	    while(!fkNodes.empty())
	    {
		    fkNode = (FKNode)fkNodes.pop();
            processRow(parentRow, fkNode.map, fkNode.node, fkNode.orderInParent);
	    }
    }   

    
    /** 
     * Process children of a class.
     */
    private void processChildren(Row parentRow, ClassMap parentMap, Node parentNode, Stack fkChildren)
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
                childMap = parentMap.getChildMap(m_nameQual.getQualifiedName(child));
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
                    // For inline just recursively call this function with a 
                    // new parentNode, so as to process the grand-children just 
                    // like children

                    // TODO: Work out order in this case!
                    processChildren(parentRow, parentMap, child, fkChildren);
                }
                else if(childMap instanceof PropertyMap)
                {
                    // Properties first go through processProperty.
                    // If they are rows then they go through to processChild
                    processProperty(parentRow, (PropertyMap)childMap, child,
                                    childOrder, fkChildren);
                }
                else if(childMap instanceof RelatedClassMap)
                {
                    // Process the child node
                    processChild(parentRow, childMap, ((RelatedClassMap)childMap).getLinkInfo(), 
                                 child, childOrder, fkChildren);
                }

                childOrder++;
		    }
	    }
    }


    /**
     * Preprocess a property. May split it if multi-valued.
     */
    private void processProperty(Row parentRow, PropertyMap propMap, Node propNode, int order, Stack fkNodes)
        throws KeyException, SQLException, MapException
    {
        // NOTE: Called from processChildren

        if(propMap.isTokenList())
        {
   			// If the attribute is multi-valued, then process each value as a
    		// separate attribute. We construct fake attributes for this
	    	// purpose; the names of these attributes are unimportant, as we
		    // already have the AttributeMap. Order refers to the order of the
   			// value in the attribute, not order of the attribute in the
    		// element (attributes are unordered).

            StringTokenizer s = new StringTokenizer(getNodeValue(propNode), " ", false);

            int attrOrder = 1;

			while(s.hasMoreElements())
			{
                Node node = null;
                Document doc = propNode.getOwnerDocument();

                switch(propNode.getNodeType())
                {
                case Node.ELEMENT_NODE:
                    node = doc.createElement("fake");
                    break;
                case Node.ATTRIBUTE_NODE:
                    node = doc.createAttribute("fake");
                    break;
                case Node.TEXT_NODE:
                    node = doc.createTextNode("");
                    break;
                };

                // TODO: Token list order
                
                if(node != null)
                {
                    node.setNodeValue(s.nextToken());
                    processSingleProperty(parentRow, propMap, node, attrOrder, fkNodes);
                    attrOrder++;
                }
            }
        }
        else
        {
            // If not multivalued then just process it.
            processSingleProperty(parentRow, propMap, propNode, order, fkNodes);
        }
    }
            
    
    /**
     * Adds a property to the class, or if in it's own row sends it for 
     * processing in processChild.
     */
    private void processSingleProperty(Row parentRow, PropertyMap propMap, Node propNode, 
                               int orderInParent, Stack fkNodes)
        throws KeyException, SQLException, MapException
    {
        // NOTE: Called from processProperty

        // If mapped to the current table
        if(propMap.getTable() == null)
        {
            // Generate the order column
            generateOrder(parentRow, propMap.getOrderInfo(), orderInParent);

            // And insert the value
			setPropertyColumn(parentRow, propMap.getColumn(), propNode);
        }

        else 
        {
            // Otherwise it's a property from another table, so send for other processing
            processChild(parentRow, propMap, propMap.getLinkInfo(), propNode, orderInParent, fkNodes);
	    }
    }


    /**
     * General function for processing child rows. 
     */
    private void processChild(Row parentRow, Object map, LinkInfo linkInfo, Node node, 
                              int orderInParent, Stack fkNodes)
        throws KeyException, SQLException, MapException
    {
        // NOTE: This method is called before parentRow has been inserted into the
        // database
        // Called from processChildren and processSingleProperty


        if(linkInfo.parentKeyIsUnique())
        {
            // If the parent key is the unique key then (possibly) generate it
            Key parentKey = linkInfo.getParentKey();
   
            if(parentKey.getKeyGeneration() == Key.XMLDBMS)
                generateKey(parentRow, parentKey);

            
            // and keep the node for later processing
            fkNodes.push(new FKNode(node, map, orderInParent));
        }
        else
        {
            // Otherwise the child key is unique and we must copy the key
            // back to the parentRow. So go ahead and process

            Row childRow = processRow(parentRow, map, node, orderInParent);
            setParentKey(parentRow, childRow, linkInfo);
        }
    }

    
    /**
     * When it's actually time for a row to get inserted this sends it to the 
     * appropriate location.
     */
    private Row processRow(Row parentRow, Object map, Node node, int orderInParent)
        throws SQLException, MapException, KeyException
    {
        // NOTE: Called from processChild and processFKNodes

        if(map instanceof PropertyMap)
			return processPropRow(parentRow, (PropertyMap)map, node, orderInParent);

        else // if (fkNode.map instanceof RelatedClassMap)
        {
            RelatedClassMap relMap = (RelatedClassMap)map;
    	    return processClassRow(parentRow, relMap.getClassMap(), relMap, node, orderInParent);
        }
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

	    if(m_map.emptyStringIsNull() && s.length() == 0)
			s = null;
        
        return s;
    }


    private void setPropertyColumn(Row propRow, Column propColumn, Node propNode)
    {
	    // Set the property's value in the row.
        String s = getNodeValue(propNode);

	    propRow.setColumnValue(propColumn, s);
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

        // TODO: throw exception for any non-existant key generators
        // TODO: what about invalid number of columns returned

        row.setColumnValues(key.getColumns(), keyGen.generateKey());
    }

    private void generateOrder(Row row, OrderInfo o, int orderInParent)
    {
	    if(o != null && !o.orderValueIsFixed() && o.generateOrder())
	    {
		    row.setColumnValue(o.getOrderColumn(), new Integer(orderInParent));
	    }
    }   


    private void insertRow(Table table, Row row)
	    throws SQLException, MapException
    {
        // TODO: Portions of this function need to be abstracted into 
        // DB specific classes

	    PreparedStatement p;


        // Get the database 
        // TODO: (What about the 'null' or default database?)
        DataInfo data = (DataInfo)m_dataSources.get(table.getDatabaseName());

        if(data == null)
            throw new MapException("Database '" + table.getDatabaseName() + "' not set.");

        String sql = data.strings.getInsert(table);
        PreparedStatement stmt = data.conn.prepareStatement(sql);

	    // parameters.setParameters(m_map, stmt, row, table.getColumns());

	    stmt.executeUpdate();

        // TODO: Work this out!
	    if(m_commitMode == COMMIT_AFTERINSERT)
            data.conn.commit();

	    stmt.close();

        data.used = true;
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

    // We only need one connection so we don't have 
    // to keep the login info around
    class DataInfo
    {
        DataSource dataSource;
        SQLStrings strings;
        Connection conn;
        boolean used;           // Used for commits. 

        DataInfo(DataSource ds, String user, String password)
            throws SQLException
        {
            dataSource = ds;
            conn = dataSource.getConnection(user, password);
            strings = new SQLStrings(conn);
            used = false;
        }
    }
}


