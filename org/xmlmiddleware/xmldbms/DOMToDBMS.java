>>>>> WARNING: WORK IN PROGRESS WILL NOT COMPILE <<<<<

// Author: Sean Walter

package org.xmlmiddleware.xmldbms;

import java.lang.*;
import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import org.xmlmiddleware.xmldbms.maps.*;

public class DOMToDBMS
{
    public static final int COMMIT_AFTERINSERT = 1
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
        m_dataSources = new HashTable();
        m_nameQual = new NameQualifierImpl();

        // TODO: What's the default commit mode?
        m_commitMode = COMMIT_AFTERINSERT;
    }

    public DOMToDBMS(NameQualifier nq)
    {
        m_dataSources = new HashTable();
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



    /**
     * Adds a data source
     *
     * @param databaseName Name used in map for this database.
     * @param dataSource A DataSource object to retrieve connections from.
     * @param user User to connect to the database as.
     * @param password Password to connect to the database with.
     */
    public addDataSource(String databaseName, DataSource dataSource, 
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
    public removeDataSource(String databaseName)
    {
        m_dataSources.remove(databaseName)
    }

    /**
     * Clear all database connection info previously added.
     *
     */
    public removeAllDataSources()
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


    public DocumentInfo storeDocument(Map map, Element el)
        throws ????
    {
        m_map = map
        m_document = el.getOwnerDocument();

        processRoot(el);

        // TODO: Do we have to do this?
        m_map = null;
        m_document = null;
  
        //TODO: Do the return value thingy
        return null;
    }


    /**
     * Processes the nodes from the root of the DOM Document that are not mapped.
     * The tree is searched until a node is found that is mapped.
     *
     * @param el The node to recusively process.
     */
    private processRoot(Element el)
    {
        // Check if the node is mapped
        ClassMap classMap = m_map.getClassMap(m_nameQual.getQualifiedName(el));

        if(classMap != null)
        {
            // TODO: Check the order stuff
            processClassRow(null, classMap, null, node, 0)

            // TODO: get the row and set up a document info
        }

        // Otherwise treat like a pass through element
        else
        {
            NodeList children = node.getChildNodes();
            for(int i = 0; i < children.getLength(); i++)
            {
                // Process any child elements, attributes and text are ignored at 
                // this point.
                if(children.item(i).getNodeType() == Node.ELEMENT_NODE)
                    processRoot((Element)children.item(i));
            }
        }
    }


    private void preprocessChildRow(Row classRow, Row parentRow, LinkInfo linkInfo)
    {
        // Do the key here
        // TODO: Check if we should even process here if Key.getType != Key.XMLDBMS

        // If parent's key is the dominant key
        linkInfo = relMap.getLinkInfo();
        if(linkInfo.parentKeyIsUnique())
        {
            // Get the key from the parent
            setChildKey(parentRow, classRow, linkInfo);
        }
        else
        {
            // Generate the key
            if(key.getType == Key.XMLDBMS)
                generateKey(classRow, linkInfo.getChildKey());
        }
    }

    private void postprocessChildRow(Row classRow, Row parentRow, LinkInfo linkInfo)
    {
        // TODO: Do we need to check parentKey.getType
        if(!linkInfo.parentKeyIsUnique())
            setParentKey(parentRow, classRow, linkInfo);
    }
        

    /**
     * Processes a mapped class.
     *
     * @param parentRow Parent class' row. May be null if root.
     * @param classMap Class to process.
     * @param relMap Relation between this class and it's parent. May be null if root.
     * @param classNode The node with data for this class.
     * @param orderInParent The order of this node within it's parent.
     */
    Row processClassRow(Row parentRow, ClassMap classMap, RelatedClassMap relMap, Node classNode, int orderInParent)
	    throws ????
    {
	    // This method creates and inserts a row in a class table. As part of the
	    // process, it also processes all children of the node.

	    Stack fkChildren = new Stack();
	    Row classRow = new Row(classMap.getTable());

        // This is used for the initial key set and then for propogation to
        // the parent later as well
	    LinkInfo linkInfo = null;


         // If the root
        if(parentRow == null || relmap == null)
        {
            Key key = classMap.getTable().getPrimaryKey();
            if(key.getType == Key.XMLDBMS)
                generateKey(classRow, key);
        }
        else
        {
            linkInfo = relMap.getLinkInfo();
            preprocessChildRow(classRow, parentRow, linkInfo);
        }

        // TODO: Do and understand order
	    // generateOrder(classRow, rcm.orderInfo, orderInParent);

	    processChildren(classRow, classMap, classNode, fkChildren);

	    insertRow(classMap.getTable(), classRow);

        processFKNodes(classRow, fkChildren);


        // Okay now any keys should have been set so we can update parent
        if(linkInfo != null)
            postprocessChildRow(classRow, parentRow, linkInfo);

        return classRow;
    }   

    Row processPropRow(Row parentRow, PropertyMap propMap, Node propNode, int orderInParent)
	    throws ???
    {
	    // This method creates and inserts a row in a property table. If the
	    // key used to link the row to its parent is a candidate key in this
	    // table, it is generated if necessary. Otherwise, the candidate key
	    // from the parent is set in this table as a foreign key.

	    Row propRow = new Row(propMap.getTable());

        preprocessChildRow(propRow, parentRow, propMap.getLinkInfo());

        // TODO: Do and understand order
	    // generateOrder(propRow, propMap.orderInfo, orderInParent);

	    setPropertyColumn(propRow, propMap.column, propNode);
	  
        insertRow(propMap.getTable(), propRow);

        postprocessChildRow(propRow, parentRow, propMap.getLinkInfo());
	  
        return propRow;
    }


    void processFKNodes(Row parentRow, Stack fkNodes)
	    throws ???
    {
	    // This method creates and inserts a row in a class or property table.
	    // The candidate key used to link the row to its parent is in the
	    // parent's table.

	    FKNode fkNode;

	    while (!fkNodes.empty())
	    {
		    fkNode = (FKNode)fkNodes.pop();
		
		    if(fkNode.map instanceof PropertyMap)
		    {
                PropertyMap propMap = (PropertyMap)fkNode.map;
			    processPropRow(parentRow, propMap, fkNode.node, fkNode.orderInParent);
		    }
		    else // if (fkNode.map instanceof RelatedClassMap)
		    {
                RelatedClassMap relMap = (RelatedClassMap)fkNode.map;
			    processClassRow(parentRow, relMap.getClassMap(), relMap, fkNode.node, fkNode.orderInParent);
		    }
	    }
    }   

    void processChildren(Row parentRow, ClassMap parentMap, Node parentNode, Stack fkChildren)
	    throws ???
    {
	    // Process the children of a class node.

	    NodeList children = parentNode.getChildNodes();

        for(int i = 0; i < children.getLength(); i++)
        {
	        Object childMap;
            Node child = children.item(i);


		    // that it is either a text or element node.

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
			    if(childMap instanceof PropertyMap)
			    {
			        processProperty(parentRow, (PropertyMap)childMap,
							        child, i, fkChildren);
			    }
			    else if(childMap instanceof RelatedClassMap)
			    {  
			        processRelatedClass(parentRow, (RelatedClassMap)childMap,
								        child, i, fkChildren);
			    }
                else if(childMap instanceof InlineClassMap)
                {
                    // Just recursively call this function with a new parentNode
                    processChildren(parentRow, parentMap, child, fkChildren);
                }
		    }
	    }
    }

    void processProperty(Row parentRow, PropertyMap propMap, Node propNode, int order, Stack fkNodes)
        throws ???
    {
        if(propMap.isMultiValued())
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
                Node node;

                switch(propNode.getNodeType())
                {
                case Node.ELEMENT_NODE:
                    node = m_document.createElement("fake");
                    break;
                case Node.ATTRIBUTE_NODE:
                    node = m_document.createAttribute("fake");
                    break;
                case Node.TEXT_NODE:
                    node = m_document.createTextNode("");
                    break;
                };

                node.setNodeValue(s.nextToken());
                processSingleProperty(elementRow, attrMap, node, attrOrder, fkAttrs);
                attrOrder++;
            }
        }
        else
        {
            // If not multivalued then just process it.
            processSingleProperty(parentRow, attrMap, propNode, order, fkAttrs);
        }
    }
            
    
       

    void processSingleProperty(Row parentRow, PropertyMap propMap, Node propNode, 
                               int orderInParent, Stack fkNodes)
	    throws ????
    {
        // If mapped to the current table
        if(propMap.getTable() == null)
        {
            // TODO: Do this order stuff when we understand it
            // generateOrder(parentRow, propMap.orderInfo, orderInParent);
			setPropertyColumn(parentRow, propMap.column, propNode);
        }

        // Otherwise it's a property from another table, so link it
        else 
        {
            LinkInfo linkInfo = propMap.getLinkInfo();
			if(linkInfo.parentKeyIsUnique())
			{
			    // If the key linking the class table to the property table is
			    // a candidate key in the class table and a foreign key in the
			    // property table, generate that key now and save the node
			    // for later processing (see FKNode).

			    generateKey(parentRow, linkInfo.getParentKey());
			    fkNodes.push(new FKNode(propNode, propMap, orderInParent));
			}
			else
			{
			    // If the key linking the class table to the property table is
			    // a candidate key in the property table and a foreign key in the
			    // class table, create the row now, which sets the foreign key in
			    // the parent (class) table.

			    createPropRow(parentRow, propMap, propNode, orderInParent);
			}
	    }
    }

    void processRelatedClass(Row parentRow, RelatedClassMap relMap, Node classNode, int orderInParent, Stack fkNodes)
	    throws ???
    {
        Linkinfo linkinfo = relMap.getLinkInfo();
		if(linkinfo.parentKeyIsUnique())
		{
		    // If the key linking the class table to the related class table
		    // is a candidate key in the class table and a foreign key in the
		    // related class table, generate that key now and save the node
		    // for later processing (see FKNode).

		    generateKey(parentRow, linkinfo.getParentKey());
		    fkNodes.push(new FKNode(classNode, relMap, orderInParent));
		}
		else
		{
		    // If the key linking the class table to the related class table
		    // is a candidate key in the related class table and a foreign
			// key in the class table, create the row now, which sets the
			// foreign key in the parent (class) table.

			processClassRow(parentRow, relMap.getClassMap(), relMap, classNode, orderInParent);
		}
    }


    String getNodeValue(Node propNode)
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

	    if(map.emptyStringIsNull() && s.length() == 0)
			s = null;
        
        return s;
    }


    void setPropertyColumn(Row propRow, Column propColumn, Node propNode)
    {
	    // Set the property's value in the row.
        String s = getNodeValue(propNode);

        // Do the XMLFormatter here
        Object format = propColumn.getFormatObject();

        Object value = null;

        if(format == null)
        {
            value = s;
        }
        else if(format instanceof DateFormat)
        {
            value = ((DateFormat)format).parse(s);
        }
        else if(format instanceof NumberFormat)
        {
            value = ((NumberFormat)format).parse(s);
        }
        else if(format instanceof XMLFormatter)
        {
            value = ((XMLFormatter)format).parse(s);
        }

	    propRow.setColumnValue(propColumn, value);
    }
    

    void setParentKey(Row parentRow, Row childRow, LinkInfo l)
    {
	    parentRow.setColumnValues(l.getParentKey().getColumns(),
								  childRow.getColumnValues(l.getChildKey().getColumns()));
    }

    void setChildKey(Row parentRow, Row childRow, LinkInfo l)
    {
	    childRow.setColumnValues(l.getChildKey().getColumns(),
							     parentRow.getColumnValues(l.getParentKey().getColumns()));
    }


    private void generateKey(Row row, Key key)
    {
        KeyGenerator keyGen = m_keyGenerators.get(key.getKeyGeneratorName());

        // TODO: throw exception for any non-existant key generators
        // TODO: what about invalid number of columns returned

        row.setColumnValues(key.getColumns(), keyGen.generateKey());
    }


    void insertRow(Table table, Row row)
	    throws SQLException, MapException
    {
	    PreparedStatement p;


        // Get the database 
        // TODO: (What about the 'null' or default database?)
        DataInfo data = m_dataSources.get(table.getDatabaseName());

        if(data == null)
            throw MapException("Database '" + table.getDatabaseName() + "' not set.");

        String sql = data.strings.getInsert(table);
        PreparedStatement stmt = data.conn.prepareStatement(sql);

	    parameters.setParameters(m_map, stmt, row, table.getColumns());

	    stmt.executeUpdate();

        // TODO: Work this out!
	    if(commitMode == COMMIT_AFTERINSERT)
            conn.commit();

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
        }
    }
}
                    



        
        