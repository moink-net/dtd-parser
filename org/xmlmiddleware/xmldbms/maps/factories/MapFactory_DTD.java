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
// * Fix setRepeatInfo to calculate repeatability of elements.
// * Separate ID columns into PK and FK columns. Fixes circular reference problem.
// Changes from version 1.01: None
// Changes from version 1.1:
// * Updated for 2.0
// * Deleted createMapFromSchema

package org.xmlmiddleware.xmldbms.maps.factories;

import org.xmlmiddleware.db.*;
import org.xmlmiddleware.schemas.dtds.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;
import org.xmlmiddleware.xmlutils.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import org.xml.sax.*;

/**
 * Create an XMLDBMSMap from a DTD.
 *
 * <p>MapFactory_DTD constructs an XMLDBMSMap object from a DTD. Based on the
 * structure of the DTD, it predicts a set of tables and columns to which the
 * element types and attributes can be mapped, then constructs an XMLDBMSMap
 * that does this.</p>
 *
 * <p>MapFactory_DTD does not construct the tables and columns it uses. This
 * is the responsibility of the user. As a general rule, the caller will want
 * to serialize the generated map (with the MapSerializer utility), modify it
 * by hand as needed, then generate CREATE TABLE statements (with the DDLGenerator
 * utility) that can be used to create the tables in the database.</p>
 *
 * <p>MapFactory_DTD constructs a map roughly as follows. The term <i>complex element
 * type</i> refers to an element type that has attributes and/or child elements.
 * The term <i>simple element type</i> refers to an element type that has no attributes
 * and contains only PCDATA.</p>
 *
 * <ul>
 *
 * <li>Generates a table for each complex element type. The table is named the same
 * as the element type. It has a single primary key column with the name of the element
 * type plus "PK", a column for each singly-valued attribute, and a column for each
 * singly-occurring simple child. If the element type appears in the content model
 * of any other element types, it contains a foreign key column for the parent element
 * type.</li>
 *
 * <li>Generates a table for each multi-valued attribute (types IDREFS, NMTOKENS, and
 * ENTITIES). The table is named the same as the attribute. It has a single primary
 * key column with the name of the attribute plus "PK", a single foreign key column
 * with the name of the parent element type plus "FK", a data column with the
 * name of the attribute, and an optional order column.</li>
 *
 * <li>Generates a table for each multiply-occuring simple child. The table is named
 * the same as the child element type. Its columns parallel those in an attribute table.</li>
 *
 * <li>Generates a table for PCDATA in mixed content. The table is named the same as
 * the parent element type plus "PCDATA". Its columns parallel those in an attribute
 * table.</li>
 *
 * </ul>
 *
 * <p>The code modifies names and checks for name collisions as specified in the
 * DBNameChecker class. All key columns have type INTEGER and all data columns
 * have type VARCHAR(255). As a general rule, the data types for data columns need
 * to be changed to something more useful.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class MapFactory_DTD
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private XMLDBMSMap    map = null;
   private int           orderType = ORDER_NONE;
   private Connection    conn;
   private DBNameChecker checker = new DBNameChecker();
   private String        databaseName = DEFAULT, catalogName = null, schemaName = null;

   //**************************************************************************
   // Constants
   //**************************************************************************

   // 5/24/00 Ronald Bourret (bug found by Iztok Kucan)
   // Replace ID with PK and FK.

   private static final Object OBJ = new Object();
   private static String       DEFAULT = "Default",
                               ORDER = "Order",
                               PK = "PK",
                               FK = "FK",
                               PCDATA = "PCDATA",
                               HIGHLOW = "HighLow";

   /** The document is a DTD. */
   public static final int DTD_EXTERNAL = 0;

   /**
    * The document is an XML document which contains and/or references a DTD.
    */
   public static final int DTD_XMLDOCUMENT = 1;

   /** Do not generate order information (default). */
   public static final int ORDER_NONE = 0;

   /** Generate fixed order values. */
   public static final int ORDER_FIXED = 1;

   /** Generate order columns. */
   public static final int ORDER_COLUMNS = 2;

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
    * Set the database connection.
    *
    * <p>If set, the database connection is used to create database names that conform
    * to the rules of the specified database. Otherwise, database names will be constructed
    * according to a default set of rules. For details, see DBNameChecker.</p>
    *
    * @param conn The connection. May be null.
    * @exception SQLException Thrown if an error occurs retrieving metadata from
    *    the connection.
    * @see DBNameChecker
    */

   public void setConnection(Connection conn)
      throws SQLException
   {
      this.conn = conn;
      checker = (conn == null) ? new DBNameChecker() : new DBNameChecker(conn);
   }

   /**
    * Set how/if order is to be generated.
    *
    * <p>For more information, see the descriptions of the OrderColumn, TLOrderColumn,
    * and FixedOrder elements of the XML-DBMS mapping language.</p>
    *
    * @param orderType ORDER_NONE, ORDER_FIXED, or ORDER_COLUMNS.
    */
   public void setOrderType(int orderType)
   {
      if ((orderType == ORDER_NONE) || (orderType == ORDER_COLUMNS))
      {
         this.orderType = orderType;
      }
      else if (orderType == ORDER_FIXED)
      {
         throw new IllegalArgumentException("Generating fixed order not yet suppported.");
      }
      else
         throw new IllegalArgumentException("Invalid order type: " + orderType);
   }

   /**
    * Set the catalog and schema names to use in the map.
    *
    * @param databaseName The database name. If this is null, "DEFAULT" will be used.
    * @param catalogName The catalog name. May be null.
    * @param schemaName The schema name. May be null.
    */
   public void setDatabaseNames(String databaseName, String catalogName, String schemaName)
   {
      this.databaseName = (databaseName != null) ? databaseName : DEFAULT;
      this.catalogName = catalogName;
      this.schemaName = schemaName;
   }

   /**
    * Create a map from an InputSource representing an external DTD subset or
    * an XML document containing a DTD.
    *
    * @param src A SAX InputSource representing the document.
    * @param type DTD_EXTERNAL or DTD_XMLDOCUMENT.
    * @param namespaceURIs A Hashtable using prefixes as keys and namespace
    *    URIs as values. The prefixes correspond to those used in the DTD. May be null.
    * @return The XMLDBMSMap object.
    * @exception XMLMiddlewareException Thrown if a DTD or map error occurs.
    * @exception IOException Thrown if an IO exception occurs parsing the DTD.
    * @exception EOFException Thrown if an EOF exception occurs parsing the DTD.
    * @exception IOException Thrown if a system ID in the DTD is malformed.
    * @exception SQLException Thrown if an error occurs checking a table name
    *    against the database.
    */
   public XMLDBMSMap createMap(InputSource src, int type, Hashtable namespaceURIs)
     throws XMLMiddlewareException, SQLException, IOException, MalformedURLException, EOFException
   {
      DTDParser parser = new DTDParser();
      DTD       dtd;

      if (type == DTD_EXTERNAL)
      {
         dtd = parser.parseExternalSubset(src, namespaceURIs);
      }
      else if (type == DTD_XMLDOCUMENT)
      {
         dtd = parser.parseXMLDocument(src, namespaceURIs);
      }
      else
         throw new IllegalArgumentException("Invalid value for type: " + type);

      return createMap(dtd);
   }

   /**
    * Create a map from a DTD object.
    *
    * @param dtd The DTD object
    * @return The XMLDBMSMap.
    * @exception XMLMiddlewareException Thrown if a map error occurs.
    * @exception SQLException Thrown if an error occurs checking a table name
    *    against the database.
    */
   public XMLDBMSMap createMap(DTD dtd)
     throws XMLMiddlewareException, SQLException
   {
      MapInverter inverter;

      if (dtd == null)
         throw new IllegalArgumentException("The dtd argument must not be null.");

      // Initialize the global variables.

      map = new XMLDBMSMap();
      checker.startNewSession();

      // Create the XML-centric view of the map.

      processElementTypes(dtd);

      // Construct the database-centric view of the map.

      inverter = new MapInverter();
      inverter.createDatabaseView(map);

      // Return the map.

      return map;
   }

   //**************************************************************************
   // Private methods -- process element type definitions
   //**************************************************************************

   private void processElementTypes(DTD dtd)
      throws XMLMiddlewareException, SQLException
   {
      Enumeration e;

      e = dtd.elementTypes.elements();
      while (e.hasMoreElements())
      {
         processElementType((ElementType)e.nextElement());
      }
   }

   private void processElementType(ElementType elementType)
      throws XMLMiddlewareException, SQLException
   {
      ClassMap classMap;
      Table    classTable;

      checkNamespace(elementType.name);

      // Check if the element is treated as a class. If not, return and don't
      // process it now. Instead, we will process it when we encounter it in
      // each of its parents.

      if (!isClass(elementType)) return;

      // Create a class map for the element type. The ClassMap might already
      // have been created while handling a RelatedClassMap. If so, createClassMap
      // will return the existing ClassMap.

      classMap = map.createClassMap(elementType.name);

      // If the class table has not been created yet, create it now. (The table will
      // have been created already if the element type was already mapped as a
      // related class.)

      classTable = classMap.getTable();
      if (classTable == null)
      {
         classTable = createTable(elementType.name.getLocalName());
         classMap.setTable(classTable);
      }

      // Process the attributes, adding one property for each.

      processAttributes(classMap, elementType.attributes);

      // Process the content, adding properties and linking to child classes
      // as needed.

      switch (elementType.contentType)
      {
         case ElementType.CONTENT_ANY:
         case ElementType.CONTENT_MIXED:
            // Note that ANY is just a special case of MIXED -- the children
            // variable should contain all of the element types in the DTD.

            processMixedContent(classMap, elementType.children);
            break;

        case ElementType.CONTENT_ELEMENT:
           processElementContent(classMap, elementType.content, elementType.children);
           break;

        case ElementType.CONTENT_PCDATA:
           processPCDATAContent(classMap);
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

   private void processAttributes(ClassMap classMap, Hashtable attributes)
      throws XMLMiddlewareException, SQLException
   {
      Enumeration e;
      Attribute   attribute;

      // Add a property for each attribute of the element (if any).

      e = attributes.elements();
      while (e.hasMoreElements())
      {
         attribute = (Attribute)e.nextElement();
         checkNamespace(attribute.name);
         addAttrPropertyMap(classMap, attribute);
      }
   }

   //**************************************************************************
   // Private methods - process content
   //**************************************************************************

   private void processMixedContent(ClassMap classMap, Hashtable children)
      throws XMLMiddlewareException, SQLException
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
            // The last two arguments (repeatable and optional) are both
            // always true for element children in mixed content.

            addElementTypePropertyMap(classMap, child, true, true);
         }
      }
   }

   private void processPCDATAContent(ClassMap classMap)
      throws XMLMiddlewareException, SQLException
   {
      // This is the special case where the element type has attributes
      // but no child element types. In this case, we store its PCDATA
      // in the class table. (Hence, the second argument is false, meaning
      // that the PCDATA is single-valued.)

      addPCDATAPropertyMap(classMap, false);
   }

   private void processElementContent(ClassMap classMap, Group content, Hashtable children)
      throws XMLMiddlewareException, SQLException
   {
      Enumeration e;
      ElementType child;
      Hashtable   repeatInfo = new Hashtable(), optionInfo = new Hashtable();
      boolean     repeatable, optional;

      // Determine which element types-as-properties are repeatable. We
      // need this information to decide whether to map them to columns
      // in the current table or to a separate table.

      setRepeatInfo(repeatInfo, content, content.isRepeatable);

      // Determine which element types-as-properties are optional. If a property
      // is single-valued and optional, the corresponding column is nullable. If
      // a property is multi-valued, the corresponding column is not nullable, since
      // a missing value merely results in a missing row in a property table. See
      // also the notes in setOptionInfo about choice groups.

      optional = (!content.isRequired || (content.type == Particle.TYPE_CHOICE));
      setOptionInfo(optionInfo, content, optional);

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
            repeatable = ((Boolean)repeatInfo.get(child.name)).booleanValue();
            optional = ((Boolean)optionInfo.get(child.name)).booleanValue();
            addElementTypePropertyMap(classMap, child, repeatable, optional);
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

         if (particle.type == Particle.TYPE_ELEMENTTYPEREF)
         {
            child = ((Reference)particle).elementType;
            if (!isClass(child))
            {
               repeatInfo.put(child.name, new Boolean(repeatable));
            }
         }
         else // particle.type == Particle.TYPE_CHOICE || Particle.TYPE_SEQUENCE
         {
            setRepeatInfo(repeatInfo, (Group)particle, repeatable);
         }
      }
   }

   private void setOptionInfo(Hashtable optionInfo, Group content, boolean parentOptional)
   {
      Particle    particle;
      boolean     optional;
      ElementType child;

      for (int i = 0; i < content.members.size(); i++)
      {
         // Get the content particle and determine if it is optional.
         // A content particle is optional if it is optional or its
         // parent is optional.

         particle = (Particle)content.members.elementAt(i);
         optional = parentOptional || !particle.isRequired;

         // Process the content particle.
         //
         // If the content particle is a reference to an element type,
         // and the element type maps to a property, save information
         // about whether the property is optional. We need this
         // info later to decide whether to map the property to a
         // column in the class table or to a separate table.
         //
         // If the content particle is a reference to an element type
         // and the element type maps to a class, ignore the content
         // particle -- there is no information to be saved here.
         //
         // If the content particle is a group, process it recursively.

         if (particle.type == Particle.TYPE_ELEMENTTYPEREF)
         {
            child = ((Reference)particle).elementType;
            if (!isClass(child))
            {
               optionInfo.put(child.name, new Boolean(optional));
            }
         }
         else if (particle.type == Particle.TYPE_CHOICE)
         {
            // If the child particle is in a choice, then it is necessarily
            // optional. We fake out the recursively called method by saying
            // that the parent is optional, which forces the child to be
            // optional.

            setOptionInfo(optionInfo, (Group)particle, true);
         }
         else // if (particle.type == Particle.TYPE_SEQUENCE)
         {
            setOptionInfo(optionInfo, (Group)particle, optional);
         }
      }
   }

   //**************************************************************************
   // Private methods -- property maps
   //**************************************************************************

   private void addPropertyMap(ClassMap classMap, XMLName name, boolean multiValued, boolean optional, int type)
      throws XMLMiddlewareException, SQLException
   {
      XMLName     propName;
      PropertyMap propMap;
      Table       table;
      Column      column;
      LinkInfo    linkInfo;
      int         nullable;
      OrderInfo   orderInfo;

      // Create a new PropertyMap. We use a null name for PCDATA maps.

      propName = (type == PropertyMap.PCDATA) ? null : name;
      propMap = PropertyMap.create(propName, type);

      // Add the PropertyMap to the ClassMap.

      if (type == PropertyMap.ATTRIBUTE)
      {
         classMap.addAttributeMap(propMap);
      }
      else if (type == PropertyMap.ELEMENTTYPE)
      {
         classMap.addChildMap(propMap);
      }
      else // if (type == PropertyMap.PCDATA)
      {
         classMap.addPCDATAMap(propMap);
      }

      // If the property is multi-valued and an attribute, then it is (by definition)
      // a token list, since this is the only kind of multi-valued attribute. If the
      // property is multi-valued and not an attribute -- that is, it is PCDATA or an
      // element type -- it is a repeating child, not a token list.

      propMap.setIsTokenList(multiValued && (type == PropertyMap.ATTRIBUTE));

      // If the property is multi-valued, store it in a separate table. Otherwise,
      // store it in the class table.

      if (multiValued)
      {
         // Create a property table. Use the local part of the property name as
         // the table name. Next, create a LinkInfo object to link the property
         // table and the class table. Finally, set the property table in the PropertyMap.

         table = createTable(name.getLocalName());
         linkInfo = createLinkInfo(classMap.getTable(), table);
         propMap.setTable(table, linkInfo);
      }
      else
      {
         table = classMap.getTable();
      }

      // Add a column for the property. Use the local part of the property name as
      // the column name. Note that a property is nullable if it is single-valued and
      // optional. If it is multi-valued, it is non-nullable, since a missing value
      // simply results in a missing row in the property table.

      nullable = (!multiValued && optional) ? DatabaseMetaData.columnNullable :
                                              DatabaseMetaData.columnNoNulls;
      column = createColumn(table, name.getLocalName(), Types.VARCHAR, 255, nullable);
      propMap.setColumn(column);

      // Create order information in the map as requested.

      if (orderType == ORDER_COLUMNS)
      {
         // We create order columns for multi-valued attributes and for
         // element types and PCDATA.

         if (((type == PropertyMap.ATTRIBUTE) && multiValued) ||
             (type == PropertyMap.ELEMENTTYPE) ||
             (type == PropertyMap.PCDATA))
         {
            orderInfo = createOrderColumn(table, name.getLocalName());
            if (type == PropertyMap.ATTRIBUTE)
            {
               // If the property is an attribute, then we are creating an order
               // column for the token list, not the property itself.

               propMap.setTokenListOrderInfo(orderInfo);
            }
            else
            {
               propMap.setOrderInfo(orderInfo);
            }
         }
      }
      else if ((orderType == ORDER_FIXED) && (type == PropertyMap.ELEMENTTYPE))
      {
         // We create fixed order columns only for elements. Fixed order doesn't make
         // sense for text (it can go anywhere) or attributes (fixed order is equivalent
         // to no order).

         throw new XMLMiddlewareException("Not yet implemented. If you got here, it's a bug (1).");
      }
   }

   private void addAttrPropertyMap(ClassMap classMap, Attribute attribute)
      throws XMLMiddlewareException, SQLException
   {
      boolean multiValued, optional;

      multiValued = ((attribute.type == Attribute.TYPE_IDREFS) ||
                     (attribute.type == Attribute.TYPE_ENTITIES) ||
                     (attribute.type == Attribute.TYPE_NMTOKENS));
      optional = (attribute.required == Attribute.REQUIRED_OPTIONAL);

      addPropertyMap(classMap, attribute.name, multiValued, optional, PropertyMap.ATTRIBUTE);
   }

   private void addPCDATAPropertyMap(ClassMap classMap, boolean multiValued)
      throws XMLMiddlewareException, SQLException
   {
      XMLName name;

      name = XMLName.create(null, classMap.getElementTypeName().getLocalName() + PCDATA, null);

      // Note that the "optional" attribute is always set to true. This is because
      // PCDATA is always optional -- both in mixed content and in PCDATA-only content.

      addPropertyMap(classMap, name, multiValued, true, PropertyMap.PCDATA);
   }

   private void addElementTypePropertyMap(ClassMap classMap, ElementType elementType, boolean multiValued, boolean optional)
      throws XMLMiddlewareException, SQLException
   {
      addPropertyMap(classMap, elementType.name, multiValued, optional, PropertyMap.ELEMENTTYPE);
   }

   //**************************************************************************
   // Private methods -- related classes
   //**************************************************************************

   private void addRelatedClass(ClassMap classMap, ElementType child)
      throws XMLMiddlewareException, SQLException
   {
      RelatedClassMap relatedClassMap;
      ClassMap        childClassMap;
      Table           childTable;
      LinkInfo        linkInfo;
      OrderInfo       orderInfo;

      // Create a new RelatedClassMap.

      relatedClassMap = classMap.createRelatedClassMap(child.name);

      // Get/create the ClassMap for the child class. Note that this might
      // already have been created when we were processing element types; if this
      // is the case, createClassMap will return the existing ClassMap. Set
      // the ClassMap in the RelatedClassMap.

      childClassMap = map.createClassMap(child.name);
      relatedClassMap.setClassMap(childClassMap);

      // Get the child class table. If this is null (meaning we just created the
      // child ClassMap), then create the table now.

      childTable = childClassMap.getTable();
      if (childTable == null)
      {
         childTable = createTable(child.name.getLocalName());
         childClassMap.setTable(childTable);
      }

      // Link the class table to the related class table.

      linkInfo = createLinkInfo(classMap.getTable(), childTable);
      relatedClassMap.setLinkInfo(linkInfo);

      // Set the order info.

      if (orderType == ORDER_COLUMNS)
      {
         orderInfo = createOrderColumn(childTable, child.name.getLocalName());
         relatedClassMap.setOrderInfo(orderInfo);
      }
      else if (orderType == ORDER_FIXED)
      {
         throw new XMLMiddlewareException("Not yet implemented. If you got here, it's a bug (2).");
      }
   }

   //**************************************************************************
   // Private methods -- helpers
   //**************************************************************************

   private Table createTable(String name)
      throws XMLMiddlewareException, SQLException
   {
      Table    table;
      String   tableName, pkName;
      Key      pk;
      Column[] pkColumns = new Column[1];

      // Check the table name, then create the table. Note that we need
      // to be careful not to check the same table name twice, since that
      // would result in two different table names. Therefore, we have to
      // be careful only to call createTable when we know that we need to
      // create a new table.

      tableName = checker.checkTableName(catalogName, schemaName, name);
      table = map.createTable(databaseName, catalogName, schemaName, tableName);

      // Create the primary key.

      pkName = checker.checkConstraintName(tableName + PK);
      pk = table.createPrimaryKey(pkName);
      pkColumns[0] = createColumn(table, tableName + PK, Types.INTEGER, 0, DatabaseMetaData.columnNoNulls);
      pk.setColumns(pkColumns);
      pk.setKeyGeneration(Key.KEYGENERATOR, HIGHLOW);

      // Return the table.

      return table;
   }

   private Column createColumn(Table table, String name, int type, int length, int nullable)
      throws XMLMiddlewareException
   {
      String columnName;
      Column column;

      // Check the column name and create the column.

      columnName = checker.checkColumnName(catalogName, schemaName, table.getTableName(), name);
      column = table.createColumn(columnName);

      // Set the metadata.

      column.setType(type);
      if (JDBCTypes.typeIsBinary(type) || JDBCTypes.typeIsChar(type))
      {
         column.setLength(length);
      }
      column.setNullability(nullable);

      // Return the column.

      return column;
   }

   private LinkInfo createLinkInfo(Table pkTable, Table fkTable)
      throws XMLMiddlewareException
   {
      String   pkTableName, fkName;
      Key      pk, fk;
      Column[] fkColumns = new Column[1];

      // Create a link between the two tables. Both tables are assumed to
      // already have a primary key, but a foreign key needs to be constructed
      // in the foreign key table.

      // Get the name and primary key of the PK table.

      pkTableName = pkTable.getTableName();
      pk = pkTable.getPrimaryKey();

      // Create a foreign key in the FK table for the PK table.

      fkName = checker.checkConstraintName(pkTableName + FK);
      fk = fkTable.createForeignKey(fkName);
      fkColumns[0] = createColumn(fkTable, pkTableName + FK, Types.INTEGER, 0, DatabaseMetaData.columnNoNulls);
      fk.setColumns(fkColumns);
      fk.setRemoteKey(pkTable, pk);

      // Create and return a LinkInfo object to link the PK and FK tables. The
      // PK table is assumed to be the parent table.

      return LinkInfo.create(pk, fk);
   }

   private OrderInfo createOrderColumn(Table table, String baseName)
      throws XMLMiddlewareException
   {
      OrderInfo orderInfo;
      Column    orderColumn;

      // Create an order column and place it in an OrderInfo. Note that we always
      // generate order column values since we can't predict which properties contain
      // order values.

      orderInfo = OrderInfo.create();
      orderColumn = createColumn(table, baseName + ORDER, Types.INTEGER, 0, DatabaseMetaData.columnNoNulls);
      orderInfo.setOrderColumn(orderColumn);
      orderInfo.setGenerateOrder(true);

      // Return the new OrderInfo.

      return orderInfo;
   }

   private void checkNamespace(XMLName name)
      throws XMLMiddlewareException
   {
      String prefix;

      // Check if the prefix and URI have already been added. If not, add them
      // now. This assumes that the DTD will be constructed using a one-to-one
      // mapping between prefixes and URIs. This is true for DTDs constructed
      // from the DTD parser. It is not necessarily true for DTDs constructed by
      // hand. Any DTDs that violate this rule will throw an exception.

      prefix = name.getPrefix();
      if (prefix == null) return;
      if (map.getNamespaceURI(prefix) == null)
      {
         map.addNamespace(prefix, name.getURI());
      }
   }
}
