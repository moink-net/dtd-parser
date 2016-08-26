// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Fix setRepeatInfo to calculate repeatability of elements.
// * Separate ID columns into PK and FK columns. Fixes circular
//   reference problem.
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import de.tudarmstadt.ito.utils.NSName;
import de.tudarmstadt.ito.schemas.dtd.*;
import de.tudarmstadt.ito.schemas.converters.DDMLToDTD;
import de.tudarmstadt.ito.schemas.converters.SubsetToDTD;
import de.tudarmstadt.ito.xmldbms.ClassMap;
import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import de.tudarmstadt.ito.xmldbms.Map;
import de.tudarmstadt.ito.xmldbms.PropertyMap;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * Create a Map from a DTD object.
 *
 * <p>MapFactory_DTD constructs tables and columns in which the element types
 * and attributes described in the DTD object can be stored, then creates
 * a Map that maps the element types and attributes to these tables and
 * columns. The resulting Map cannot be used immediately with DBMSToDOM or
 * DOMToDBMS because no connection has been set on it. Furthermore, it is
 * possible that the tables referred to by the map don't yet exist. However,
 * it can be serialized as a mapping document or used to generate CREATE
 * TABLE statements.</p>
 *
 * <P>For example, the following code creates a map from the DTD document.dtd,
 * creates the tables, sets the connection, and then transfers data to the
 * database:</P>
 *
 * <pre>
 *    // Instantiate a new map factory and create a map.
 *    factory = new MapFactory_DTD();
 *    map = factory.createMapFromDTD(src, MapFactory_DTD.DTD_EXTERNAL, true, null);<br />
 *
 *   // Create the tables used by the map. Note that this function calls
 *   // Map.getCreateTableStrings(), then executes each string in a
 *   // JDBC Statement.
 *   createTables(map);<br />
 *
 *   // Set the database connection, then transfer the data to the database.
 *   map.setConnection(conn);
 *   domToDBMS = new DOMToDBMS(map);
 *   domToDBMS.storeDocument(doc);
 * </pre>
 *
 * <p>MapFactory_DTD constructs tables and columns roughly as follows. Note
 * that these tables and columns are not actually created in the database;
 * to do this, the application must retrieve CREATE TABLE strings from
 * the resulting Map and execute these in JDBC statements. The reason for
 * this is that applications will commonly want to change the table structure
 * predicted by MapFactory_DTD before actually creating tables or simply
 * use this factory as a tool for creating Maps, which can be serialized
 * with the Map.serialize() method.</p>
 *
 * <ul>
 *
 * <li>For each element type that has attributes or child elements, a table
 * is generated. In this table are a primary key (PK) column, one column for each
 * single-valued attribute, one column for each singly-occurring child
 * element type that contains only PCDATA and has no attributes, an (optional)
 * order column for each child element type column, and one foreign key (FK)
 * column for each parent element type. If the element type has attributes and
 * PCDATA but no child element types, then there is also a column for its
 * PCDATA. Note that the PK column appears only if needed to link to
 * a child table or if the element type is a potential root element type.</li>
 *
 * <li>If an attribute is multi-valued (IDREFS, NMTOKENS, or ENTITIES), it
 * is stored in a separate table, with an element type FK column, a value
 * column, and an (optional) order column.</li>
 *
 * <li>If a child element type that contains only PCDATA and has no attributes
 * can occur multiple times in its parent, it is stored in a separate table,
 * with a parent element type FK column, a value column, and an
 * (optional) order column.</li>
 *
 * <li>Except as noted above, PCDATA is stored in a separate table with an
 * element type FK column, a value column, and an (optional) order column.</li>
 *
 * <li>The code also guesses at what the legal root element types are. An
 * element type is considered to be a root if it has no parents. If all
 * element types have parents, then all element types are made legal roots.</li>
 *
 * </ul>
 *
 * <p><b>WARNING!</b> This code does not check for name collisions. A number
 * of name collisions are possible. The possible table and column names are
 * listed below. If these clash with each other, the result is missing tables
 * and/or columns. If these clash with existing table names, the result is
 * the inability to correctly transfer data to the database. You should also
 * remember that table and column names in many databases are case insensitive,
 * so collisions due to case folding are possible.</p>
 *
 * <pre>
 *    Table name: element type name
 *    Column names:
 *       element type name + "PK"
 *       element type name + "PCDATA"
 *       element type name + "PCDATA" + "Order"
 *       parent element type name + "FK"
 *       child element type name + "FK"
 *       child element type name + "Order"
 *       attribute name
 *
 *    Table name: attribute name
 *    Column names:
 *       element type name + "FK"
 *       attribute name
 *       attribute name + "Order"
 *
 *    Table name: element type name + "PCDATA"
 *    Column names:
 *       element type name + "FK"
 *       element type name + "PCDATA"
 *       element type name + "PCDATA" + "Order"
 * </pre>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class MapFactory_DTD
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private TempMap    map = null;
   private DTD        dtd;
   private boolean    storeOrder;

   //**************************************************************************
   // Constants
   //**************************************************************************

   // 5/24/00 Ronald Bourret (bug found by Iztok Kucan)
   // Replace ID with PK and FK.

   private static final Object OBJ = new Object();
   private static String       ORDER = "Order",
							   PK = "PK",
							   FK = "FK",
							   PCDATA = "PCDATA";
   private static final int    PROPERTY_ATTRIBUTE = 0,
							   PROPERTY_ELEMENTTYPE = 1,
							   PROPERTY_PCDATA = 2;

   /** Schema document uses DCD (not supported). */
   public static final int SCHEMA_DCD = 0;

   /** Schema document uses DDML. */
   public static final int SCHEMA_DDML = 1;

   /** Schema document uses SOX (not supported). */
   public static final int SCHEMA_SOX = 2;

   /** Schema document uses W3C XML Schemas (not supported). */
   public static final int SCHEMA_W3C = 3;

   /** Schema document uses XML-Data (not supported). */
   public static final int SCHEMA_XMLDATA = 4;

   /** DTD is an external subset. */
   public static final int DTD_EXTERNAL = 0;

   /**
	* The DTD is in an XML document, as an internal subset, reference
	* to an external subset, or both.
	*/
   public static final int DTD_XMLDOCUMENT = 1;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new MapFactory_DTD. */
   public MapFactory_DTD()
   {
   }      

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Create a map from an InputSource representing an external DTD subset or
	* an XML document containing a DTD.
	*
	* @param filename The name of the schema file.
	* @param type DTD_EXTERNAL or DTD_XMLDOCUMENT.
	* @param storeOrder Whether the map should store order information in
	*    the database.
	* @param namespaceURIs A Hashtable using prefixes as keys and namespace
	*    URIs as values. May be null.
	* @return The Map object.
	* @exception InvalidMapException Thrown if a mapping error occurs.
	* @exception DTDException Thrown if a DTD error is found.
	* @exception EOFException Thrown if EOF is reached prematurely.
	* @exception MalformedURLException Thrown if a system ID is malformed.
	* @exception IOException Thrown if an I/O error occurs.
	*/
   public Map createMapFromDTD(InputSource src, int type, boolean storeOrder, Hashtable namespaceURIs)
	  throws InvalidMapException, DTDException, EOFException, MalformedURLException, IOException
   {
	  SubsetToDTD subsetToDTD = new SubsetToDTD();
	  DTD         dtd;

	  switch (type)
	  {
		 case DTD_EXTERNAL:
			dtd = subsetToDTD.convertExternalSubset(src, namespaceURIs);
			break;

		 case DTD_XMLDOCUMENT:
			dtd = subsetToDTD.convertDocument(src, namespaceURIs);
			break;

		 default:
			throw new IllegalArgumentException("Invalid type: " + type);
	  }

	  return createMap(dtd, storeOrder);
   }      


   /**
	* Create a map from an InputSource representing a schema.
	*
	* <p>Currently, only DDML files are supported.</p>
	*
	* @param filename The name of the schema file.
	* @param type The schema type: SCHEMA_DCD, SCHEMA_DDML, SCHEMA_SOX,
	*    SCHEMA_XMLDATA, SCHEMA_W3C.
	* @param storeOrder Whether the map should store order information in
	*    the database.
	* @param parser A SAX Parser to parse the InputSource.
	* @return The Map object.
	* @exception InvalidMapException Thrown if a mapping error occurs.
	* @exception SAXException Thrown if a SAX error occurs.
	* @exception IOException Thrown if an I/O error occurs.
	*/
   public Map createMapFromSchema(InputSource src, int type, boolean storeOrder, Parser parser)
	  throws InvalidMapException, SAXException, IOException
   {
	  DTD       dtd = null;
	  DDMLToDTD ddmlToDTD;

	  switch (type)
	  {
		 case SCHEMA_DDML:
			ddmlToDTD = new DDMLToDTD(parser);
			dtd = ddmlToDTD.convert(parser, src);
			break;

		 default:
			throw new IllegalArgumentException("Invalid or unsupported type: " + type);
	  }
	  return createMap(dtd, storeOrder);
   }      

   /**
	* Create a map from a DTD object.
	*
	* @param dtd A DTD object from which to create the Map.
	* @param storeOrder Whether the map should store order information in
	*    the database.
	* @return The Map object.
	* @exception InvalidMapException Thrown if a map error occurs.
	*/
   public Map createMap(DTD dtd, boolean storeOrder)
	  throws InvalidMapException
   {
	  if (dtd == null)
		 throw new IllegalArgumentException("The dtd argument must be non-null.");

	  this.storeOrder = storeOrder;
	  this.dtd = dtd;
	  map = new TempMap();

	  processElementTypes();
	  addRootClassMaps();

	  // Convert the temporary maps to real ones.

	  map.createTableMapsFromClassMaps();
	  return map.createMapFromTemp();
   }      

   //**************************************************************************
   // Private methods -- process element type definitions
   //**************************************************************************

   private void processElementTypes()
	  throws InvalidMapException
   {
	  Enumeration e;

	  e = dtd.elementTypes.elements();
	  while (e.hasMoreElements())
	  {
		 processElementType((ElementType)e.nextElement());
		 
	  }	  

	  
   }                     

   private void processElementType(ElementType elementType)
	  throws InvalidMapException
   {
	  TempClassMap classMap;
	  TempColumn   column;

	  // Check if the element is treated as a class. If not, return and don't
	  // process it now. Instead, we will process it when we encounter it in
	  // each of its parents.

	  if (!isClass(elementType)) return;

	  // Add a class map for the element type.
	  classMap = map.addTempClassMap(elementType.name);

	  // 5/24/00 Ronald Bourret
	  // Deleted commented-out code that added a key column. Rewrote comment.

	  // Add a table for the element type, using the element type name as the table
	  // name. Note that no key column is added at this time. This is because there
	  // is no need for a key column if the table is not a root table and does not have child
	  // property tables or child element tables. Therefore, if the key column is needed,
	  // it is added in addPropertyMap, addRelatedClass, or addRootClassMap
	  // by the call to TempTable.getColumn(classMap.name.local + PK).

	  classMap.type = ClassMap.TYPE_TOCLASSTABLE;
	  classMap.table = map.addTempClassTable(elementType.name.qualified, elementType.name.local);

	  // Process the attributes, adding one property for each.
	  processAttributes(classMap, elementType.attributes);

	  // Process the content, adding properties and linking to child classes
	  // as needed.

	  switch (elementType.contentType)
	  {
		 case ElementType.CONTENT_MIXED:
			processMixedContent(classMap, elementType.children);
			break;

		 case ElementType.CONTENT_ELEMENT:
			processElementContent(classMap, elementType.content, elementType.children);
			break;

		 case ElementType.CONTENT_PCDATA:
			processPCDATAContent(classMap);
			break;

		 case ElementType.CONTENT_ANY:
			// Note that ANY is just a special case of MIXED -- the children
			// variable should contain all of the element types in the DTD.
			processMixedContent(classMap, elementType.children);
			break;

		 case ElementType.CONTENT_EMPTY:
			// No content to process.
			break;
	  }
   }               

   private boolean isClass(ElementType elementType)
   {
	  // If an element type has any attributes or child elements, it is
	  // treated as a class. Otherwise, it is treated as a property.

	  // BUG! This code actually misses a special case. If an element type
	  // has no children, no attributes, and no parents, it needs to be
	  // mapped as a class. However, the corresponding XML document is:
	  //
	  //    <?xml version="1.0"?>
	  //    <!DOCTYPE [<!ELEMENT foo EMPTY>]>
	  //    <foo/>
	  //
	  // which really isn't worth worrying about...

	  return (!elementType.children.isEmpty() ||
			  !elementType.attributes.isEmpty());
   }      

   //**************************************************************************
   // Private methods - process attribute definitions
   //**************************************************************************

   private void processAttributes(TempClassMap classMap, Hashtable attributes)
	  throws InvalidMapException
   {
	  Enumeration e;
	  Attribute   attribute;

	  // Add a property for each attribute of the element (if any).
	  e = attributes.elements();
	  while (e.hasMoreElements())
	  {
		 attribute = (Attribute)e.nextElement();
		 addAttrPropertyMap(classMap, attribute);
	  }
   }      

   //**************************************************************************
   // Private methods - process content
   //**************************************************************************

   private void processMixedContent(TempClassMap classMap, Hashtable children)
	  throws InvalidMapException
   {
	  Enumeration e;
	  ElementType child;

	  // Add a table to contain the PCDATA for the element.

	  addPCDATAPropertyMap(classMap, true);

	  // Process the children. If a child is mapped as a class, add a link
	  // to the table for that child. If a child is mapped as a property,
	  // add a property table for that child.

	  e = children.elements();
	  while (e.hasMoreElements())
	  {
		 child = (ElementType)e.nextElement();
		 if (isClass(child))
		 {
			addRelatedClass(classMap, child);
		 }
		 else
		 {
			addElementTypePropertyMap(classMap, child, true);
		 }
	  }
   }      

   private void processPCDATAContent(TempClassMap classMap)
	  throws InvalidMapException
   {
	  // This is the special case where the element type has attributes
	  // but no child element types. In this case, we store its PCDATA
	  // in the class table. (Hence, the second argument is false, meaning
	  // that the PCDATA is single-valued.)

	  addPCDATAPropertyMap(classMap, false);
   }      

   private void processElementContent(TempClassMap classMap, Group content, Hashtable children)
	  throws InvalidMapException
   {
	  Enumeration e;
	  ElementType child;
	  Hashtable   repeatInfo = new Hashtable();
	  boolean     repeatable;

	  // Determine which element types-as-properties are repeatable. We
	  // need this information to decide whether to map them to columns
	  // in the current table or to a separate table.

	  setRepeatInfo(repeatInfo, content, content.isRepeatable);

	  // Process the children and either add related classes or properties
	  // for them. This is similar to the code in processMixedContent, except
	  // that we retrieve information about whether the child is repeatable
	  // from the repeatInfo hashtable. In processMixedContent, the child
	  // is always repeatable.

	  e = children.elements();
	  while (e.hasMoreElements())
	  {
		 child = (ElementType)e.nextElement();
		 if (isClass(child))
		 {
			addRelatedClass(classMap, child);
		 }
		 else
		 {
			repeatable = ((Boolean)repeatInfo.get(child.name.qualified)).booleanValue();
			addElementTypePropertyMap(classMap, child, repeatable);
		 }
	  }
   }      

   private void setRepeatInfo(Hashtable repeatInfo, Group content, boolean parentRepeatable)
   {
	  Particle    particle;
	  boolean     repeatable;
	  ElementType child;

	  for (int i = 0; i < content.members.size(); i++)
	  {
		 // Get the content particle and determine if it is repeatable.
		 // A content particle is repeatable if it is repeatable or its
		 // parent is repeatable.

		 // 5/18/00, Iztok Kucan
		 // Change "&&" to "||" when setting "repeatable" so the code
		 // actually does what the above comment says.

		 particle = (Particle)content.members.elementAt(i);
		 repeatable = parentRepeatable || particle.isRepeatable;

		 // Process the content particle.
		 //
		 // If the content particle is a reference to an element type,
		 // and the element type maps to a property, save information
		 // about whether the property is repeatable. We need this
		 // info later to decide whether to map the property to a
		 // column in the class table or to a separate table.
		 //
		 // If the content particle is a reference to an element type
		 // and the element type maps to a class, ignore the content
		 // particle -- there is no information to be saved here.
		 //
		 // If the content particle is a group, process it recursively.

		 if (particle.type == Particle.PARTICLE_ELEMENTTYPEREF)
		 {
			child = ((Reference)particle).elementType;
			if (!isClass(child))
			{
			   repeatInfo.put(child.name.qualified, new Boolean(repeatable));
			}
		 }
		 else // particle.type == Particle.PARTICLE_CHOICE || _SEQUENCE
		 {
			setRepeatInfo(repeatInfo, (Group)particle, repeatable);
		 }
	  }
   }      

   //**************************************************************************
   // Private methods -- property maps
   //**************************************************************************

   private void addPropertyMap(TempClassMap classMap, NSName name, boolean multiValued, int type)
	  throws InvalidMapException
   {
	  TempPropertyMap propMap;
	  TempTable       table;
	  TempColumn      foreignKeyColumn, primaryKeyColumn;

	  // Create a new TempPropertyMap and add it to the TempClassMap.
	  propMap = new TempPropertyMap();
	  propMap.name = name;
	  if (type == PROPERTY_ATTRIBUTE)
	  {
		 classMap.addAttributePropertyMap(propMap);
	  }
	  else if (type == PROPERTY_ELEMENTTYPE)
	  {
		 classMap.addElementPropertyMap(propMap);
	  }
	  else // if (type == PROPERTY_PCDATA)
	  {
		 classMap.addPCDATAPropertyMap(propMap);
	  }

	  // If the property is multi-valued, store it in a separate table.
	  // Otherwise, store it in the class table.

	  propMap.multiValued = multiValued;
	  if (propMap.multiValued)
	  {
		 // Add the property table. Use the property name as the table name.

		 propMap.type = PropertyMap.TYPE_TOPROPERTYTABLE;
		 propMap.table = map.addTempPropertyTable(name.local);
		 table = propMap.table;

		 // Add a foreign key column to the property table and get the primary
		 // key column from the class table.

		 // 5/24/00 Ronald Bourret
		 // Replace ID columns with PK and FK columns.

		 foreignKeyColumn = propMap.table.getColumn(classMap.name.local + FK);
		 foreignKeyColumn.type = Types.INTEGER;
		 primaryKeyColumn = classMap.table.getColumn(classMap.name.local + PK);
		 primaryKeyColumn.type = Types.INTEGER;

		 // Link the class table to the property table. Note that we use the
		 // key column from the class table to do the link.

		 propMap.linkInfo = new TempLinkInfo();
		 propMap.linkInfo.generateKey = true;
		 propMap.linkInfo.parentKeyIsCandidate = true;
		 propMap.linkInfo.parentKey.addElement(primaryKeyColumn);
		 propMap.linkInfo.childKey.addElement(foreignKeyColumn);
	  }
	  else
	  {
		 propMap.type = PropertyMap.TYPE_TOCOLUMN;
		 table = classMap.table;
	  }

	  // Add a column for the property. Use the property name as the
	  // column name.

	  propMap.column = table.mapPropertyColumn(name.local);
	  propMap.column.type = Types.VARCHAR;
	  propMap.column.length = 255;

	  // Store order information if requested. For attributes, we only
	  // store the order information if the attribute is multi-valued.

	  if ((storeOrder) && (multiValued || (type != PROPERTY_ATTRIBUTE)))
	  {
		 propMap.orderInfo.generateOrder = true;
		 propMap.orderInfo.orderColumn = table.getColumn(name.local + ORDER);
		 propMap.orderInfo.orderColumn.type = Types.INTEGER;
	  }
   }      

   private void addAttrPropertyMap(TempClassMap classMap, Attribute attribute)
	  throws InvalidMapException
   {
	  boolean multiValued;

	  multiValued = ((attribute.type == Attribute.TYPE_IDREFS) ||
					 (attribute.type == Attribute.TYPE_ENTITIES) ||
					 (attribute.type == Attribute.TYPE_NMTOKENS));

	  addPropertyMap(classMap, attribute.name, multiValued, PROPERTY_ATTRIBUTE);
   }      

   private void addPCDATAPropertyMap(TempClassMap classMap, boolean multiValued)
	  throws InvalidMapException
   {
	  NSName name;

	  name = new NSName(classMap.name.local + PCDATA, null, null);

	  addPropertyMap(classMap, name, multiValued, PROPERTY_PCDATA);
   }      

   private void addElementTypePropertyMap(TempClassMap classMap, ElementType elementType, boolean multiValued)
	  throws InvalidMapException
   {
	  addPropertyMap(classMap, elementType.name, multiValued, PROPERTY_ELEMENTTYPE);
   }      

   //**************************************************************************
   // Private methods -- related classes
   //**************************************************************************

   private void addRelatedClass(TempClassMap classMap, ElementType child)
	  throws InvalidMapException
   {
	  TempRelatedClassMap relatedClassMap;
	  TempColumn          foreignKeyColumn, primaryKeyColumn;

	  // Create a new TempRelatedClassMap.
	  relatedClassMap = new TempRelatedClassMap();

	  // Get the TempClassMap for the child class. If its table is null,
	  // then add the table so we can add columns to it.

	  relatedClassMap.classMap = map.getTempClassMap(child.name);
	  if (relatedClassMap.classMap.table == null)
	  {
		 relatedClassMap.classMap.table = map.getTempClassTable(child.name.qualified);
	  }

	  // 5/24/00 Ronald Bourret
	  // Replace ID columns with PK and FK columns.

	  // Get the primary and foreign key columns.
	  foreignKeyColumn = relatedClassMap.classMap.table.getColumn(classMap.name.local + FK);
	  foreignKeyColumn.type = Types.INTEGER;
	  primaryKeyColumn = classMap.table.getColumn(classMap.name.local + PK);
	  primaryKeyColumn.type = Types.INTEGER;

	  // Set the link info.
	  relatedClassMap.linkInfo = new TempLinkInfo();
	  relatedClassMap.linkInfo.generateKey = true;
	  relatedClassMap.linkInfo.parentKeyIsCandidate = true;
	  relatedClassMap.linkInfo.parentKey.addElement(primaryKeyColumn);
	  relatedClassMap.linkInfo.childKey.addElement(foreignKeyColumn);

	  // Set the order info.
	  if (storeOrder)
	  {
		 relatedClassMap.orderInfo.generateOrder = true;
		 relatedClassMap.orderInfo.orderColumn = relatedClassMap.classMap.table.getColumn(child.name.local + ORDER);
		 relatedClassMap.orderInfo.orderColumn.type = Types.INTEGER;
	  }

	  // Add the related class map to the class map.

	  classMap.addRelatedClassMap(relatedClassMap);
   }      

   //**************************************************************************
   // Private methods -- process element type definitions
   //**************************************************************************

   private void addRootClassMaps()
   {
	  Enumeration e;
	  ElementType elementType;
	  boolean     rootAdded = false;

	  // Since the DTD does not specify what the root element type is, we
	  // guess that it is any type that does not have a parent. Admittedly
	  // a lousy guess, but the alternative is all element types. Note that
	  // if any element types have a content model of ANY, then all element
	  // type have parents.

	  e = dtd.elementTypes.elements();
	  while (e.hasMoreElements())
	  {
		 elementType = (ElementType)e.nextElement();
		 if (elementType.parents.isEmpty() && isClass(elementType))
		 {
			addRootClassMap(elementType);
			rootAdded = true;
		 }
	  }

	  // If all element types have parents, then assume all classes are roots.

	  if (!rootAdded)
	  {
		 e = dtd.elementTypes.elements();
		 while (e.hasMoreElements())
		 {
			elementType = (ElementType)e.nextElement();
			if (isClass(elementType))
			{
			   addRootClassMap(elementType);
			}
		 }
	  }
   }      

   private void addRootClassMap(ElementType elementType)
   {
	  TempClassMap     classMap;
	  TempRootClassMap rootClassMap;
	  TempColumn       primaryKeyColumn;

	  // Get the class map for the element type and change its type
	  // to TYPE_TOROOTTABLE.

	  classMap = map.getTempClassMap(elementType.name);
	  classMap.type = ClassMap.TYPE_TOROOTTABLE;

	  // Add the root class map.

	  rootClassMap = map.addTempRootClassMap(classMap);

	  // Add the link info.

	  // 5/24/00 Ronald Bourret
	  // Replace ID column with PK column.

	  primaryKeyColumn = classMap.table.getColumn(classMap.name.local + PK);
	  primaryKeyColumn.type = Types.INTEGER;

	  rootClassMap.linkInfo = new TempLinkInfo();
	  rootClassMap.linkInfo.generateKey = true;
	  rootClassMap.linkInfo.parentKeyIsCandidate = false;
	  rootClassMap.linkInfo.childKey.addElement(primaryKeyColumn);

	  // Set the order info.
	  if (storeOrder)
	  {
		 rootClassMap.orderInfo.generateOrder = true;
		 rootClassMap.orderInfo.orderColumn = classMap.table.getColumn(classMap.name.local + ORDER);
		 rootClassMap.orderInfo.orderColumn.type = Types.INTEGER;
	  }
   }      
}