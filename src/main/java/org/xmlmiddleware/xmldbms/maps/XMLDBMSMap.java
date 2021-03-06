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
// * Change declaration of byte array constants
// * Changed error messages in addColumnMetadata and checkColumnTypesSet
// * Fixed bug in addResultSetMetadata where column number not set
// * Quote table names in buildInsert/Select/CreateTableString
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmlutils.*;

import java.sql.*;
import java.text.*;
import java.util.*;

/**
 * Describes how an XML document is mapped to a database and vice versa;
 * <a href="../readme.htm#NotForUse">for limited use</a>.
 *
 * <h3>For people writing XML-DBMS applications</h3>
 *
 * <p>XMLDBMSMap objects describe how XML documents are mapped to databases and
 * vice versa. You can think of them as compiled versions of map documents,
 * although they can be created from other sources as well, such as a map
 * factory that creates an XMLDBMSMap object from a DTD.</p>
 *
 * <p>Applications that use XML-DBMS to transfer data treat XMLDBMSMap objects as
 * opaque objects. That is, they call a map factory to create an XMLDBMSMap object,
 * then pass it to a data transfer class such as DOMToDBMS or DBMSToDOM,
 * often without calling any methods on it.</p>
 *
 * <p>There are a variety of map utilities that can be used to modify or
 * perform operations with maps. Most of these are used by map factories.
 * However, some of them may be used by applications, such as to serialize a
 * map or initialize it with the metadata in a particular database.</p>
 * 
 * <p>For example, the following code creates a map from the sales.map
 * map document.</p>
 *
 * <pre>
 *    // Instantiate a new map factory from a database connection
 *    // and SAX parser.
 *    factory = new MapFactory_MapDocument(parser);<br />
 *
 *    // Create an XMLDBMSMap from sales.map.
 *    map = factory.createMap(new InputSource(new FileReader("sales.map")));
 * </pre>
 *
 * <h3>For people writing map factories and other internal XML-DBMS code</h3>
 *
 * <p>XMLDBMSMap objects are actually a graph of related objects. This graph can be fairly
 * complex and it is a good idea to familiarize yourself with all map objects before
 * trying to use them. (With a lower case m, map object refers to any of the objects in the graph,
 * such as an XMLDBMSMap, a ClassMap, or a ColumnMap.) One way to understand the graph is to
 * look at the private variables in each map class.</p>
 *
 * <p>The graph of objects contains two views of the mapping, one from the XML point
 * of view (ClassMaps) and one from the database point of view (ClassTableMaps and
 * PropertyTableMaps). This allows the map to be accessed based either on XML metadata
 * (element type and attribute names) or database metadata (table and column names).
 * The MapInverter utility constructs one view from the other.</p>
 *
 * <p>The following classes are in the XML-centric view of the map:</p>
 *
 * <pre>
 *    ClassMap
 *    PropertyMap
 *    RelatedClassMap
 *    InlineClassMap
 * </pre>
 *
 * The following classes are in the database-centric view of the map:</p>
 *
 * <pre>
 *    ClassTableMap
 *    ColumnMap
 *    PropertyTableMap
 *    RelatedClassTableMap
 * </pre>
 *
 * The following classes are shared by both views:</p>
 *
 * <pre>
 *    XMLDBMSMap
 *    Table
 *    Column
 *    Key
 *    LinkInfo
 *    OrderInfo
 * </pre>
 *
 * <p>The inheritance hierarchy of map classes is as follows. This makes clear how
 * how similar the XML-centric and database-centric views of the map are. For example,
 * PropertyMap (XML-centric), ColumnMap (database-centric), and PropertyTableMap
 * (database-centric) all inherit from PropertyMapBase, since all map properties.
 *
 * <pre>
 *    XMLMiddlewareException
 *
 *    MapBase
 *       XMLDBMSMap
 *
 *       ClassMapBase
 *          ClassMap
 *          InlineClassMap
 *       PropertyMapBase
 *          PropertyMap
 *          ColumnMap
 *          PropertyTableMap
 *       RelatedMapBase
 *          RelatedClassMap
 *          RelatedClassTableMap
 *       ClassTableMap
 *
 *       Table
 *       Column
 *       Key
 *
 *       LinkInfo
 *       OrderInfo
 * </pre>
 *
 * <p>Expressed as a tree, the graph looks roughly as follows. An ellipsis (...) is
 * used to indicate that the graph continues. In many cases, there are multiple paths
 * to the same object, which is not shown due to the tree representation. For example,
 * the Column in a PropertyMap can also be reached through the Table in the parent
 * ClassMap or the Table in the PropertyMap (if any).</p>
 *
 * <pre>
 *    XMLDBMSMap
 *       ClassMap (hashtable of)
 *          Table
 *             Column (hashtable of)
 *             Key (hashtables of)
 *                Column (Vector of)
 *          ClassMap... (for used class map, optional)
 *          ClassMap... (for base class, optional)
 *          PropertyMap (hashtables of)
 *             Table... (optional)
 *             Column
 *             LinkInfo (optional)
 *                Key...
 *                Key...
 *             OrderInfo (optional)
 *                Column (optional)
 *          RelatedClassMap (hashtable of)
 *             ClassMap...
 *             LinkInfo...
 *             OrderInfo... (optional)
 *          InlineClassMap (hashtable of)
 *             PropertyMap... (hashtable of)
 *             RelatedClassMap... (hashtable of)
 *             InlineClassMap... (hashtable of)
 *             OrderInfo... (optional)
 *       ClassTableMap (hashtable of)
 *          Table...
 *          Table... (for base class, optional)
 *          ColumnMap (hashtable of)
 *             Column
 *             OrderInfo... (optional)
 *          PropertyTableMap (hashtable of)
 *             Table...
 *             Column...
 *             LinkInfo...
 *             OrderInfo... (optional)
 *          RelatedClassTableMap (vector of)
 *             ClassTableMap...
 *             LinkInfo...
 *             OrderInfo... (optional)
 *          ElementInsertionMap...
 *             ColumnMap...
 *             PropertyTableMap...
 *             RelatedClassTableMap...
 *             ElementInsertionMap...
 *             OrderInfo... (optional)
 * </pre>
 *
 * <h4>Methods for Creating Map Objects</h4>
 *
 * <p>Map objects are created by factory methods. These occur in all map objects
 * except XMLDBMSMap, which has a simple constructor. For example, the following code
 * creates a LinkInfo object.</p>
 *
 * <pre>
 *    LinkInfo linkInfo = LinkInfo.create(parentKey, childKey);
 * </pre>
 *
 * <p>In many cases, factory methods can also be found on the parent object.
 * For example, the following code creates a ClassMap object from an XMLDBMSMap object:</p>
 *
 * <pre>
 *    classMap = map.createClassMap(null, "foo");
 * </pre>
 *
 * <p>When the factory method is called on the parent, the child object is
 * automatically added to the parent object. Furthermore, if a child object
 * with the same name already exists, that object will be returned instead of
 * creating a new object. Since objects are frequently used in multiple places
 * -- for example, the same ClassMap can appear in an XMLDBMSMap and a RelatedClassMap --
 * this is useful, as the alternative would be to first check if
 * the object exists, then create it if it doesn't. For example, the following
 * code is equivalent to calling createClassMap on an XMLDBMSMap object:</p>
 *
 * <pre>
 *    classMap = map.getClassMap(null, "foo");
 *    if (classMap == null)
 *    {
 *       classMap = ClassMap.create(null, "foo");
 *       map.addClassMap(classMap);
 *    }
 * </pre>
 *
 * <p>The primary difference between creating an object on its parent and adding
 * it to its parent is that an exception is thrown when attempting to add an object
 * that already exists on the parent. This is useful when a map factory wants to
 * make sure that the same object is not mapped or created twice. For example, the
 * following code throws an error if the table already has a column named "bar":</p>
 *
 * <pre>
 *   column = Column.create("bar");
 *   table.addColumn(column);
 * </pre>
 *
 * <h4>Map Object Properties</h4>
 *
 * <p>Properties in maps fall into three categories:</p>
 *
 * <ul>
 * <li>Fundamental properties are those that, once set, may not be changed,
 * such as the element type name in a ClassMap. These are passed as arguments
 * to class constructors.</li>
 *
 * <li>Collection properties are those for which more than one value is allowed,
 * such as a Column in a Table. These are stored in a Hashtable and indexed by name.</li>
 *
 * <li>Single-valued properties are those for which a single value is allowed,
 * such as a Table in a ClassMap.</li>
 * </ul>
 *
 * <h4>Methods for Manipulating Map Object Properties</h4>
 *
 * <p>Fundamental properties are set when the map object is created. They
 * cannot be modified, but can be retrieved with a getXxxx method. For example:</p>
 *
 * <pre>
 *    column = Column.create("foo");
 *    columnName = column.getName();
 * </pre>
 *
 * <p>Objects in collection properties are manipulated with methods similar
 * to the following:</p>
 *
 * <pre>
 *    getXxxx(String name)    // Returns an object with the specified name. Returns null if
 *                            // the value does not exist. Used by map utilities and data
 *                            // transfer classes.
 *
 *    getXxxxs()              // Returns an Enumeration or Hashtable over all objects of a
 *                            // given type. Used by map utilities and data transfer classes.
 *
 *    createXxxx(Xxxx xxx)    // Returns a single object with the specified name. The method
 *                            // first attempts to retrieve an existing object and, if one
 *                            // doesn't exist, creates a new one. Used only by map factories.
 *
 *    addXxxx(Xxxx xxxx)      // Adds a new object to the collection. Throws an error if
 *                            // the value already exists. Used only by map factories.
 *
 *    removeXxxx(String name) // Removes an object with the specified name. Throws an error
 *                            // if the value does not exist. Used only by map factories.
 *
 *    removeAllXxxxs()        // Removes all objects of a given type. Used by map factories.
 * </pre>
 *
 * <p>Single-valued properties are manipulated with methods similar to the following:</p>
 *
 * <pre>
 *    getXxxx()               // Returns the object value. Used by map factories and data
 *                            // transfer classes.
 *
 *    setXxxx(new value)      // Sets the object value, overriding the old value. In many cases,
 *                            // the argument can be null, which removes the property value.
 *                            // Used only by map factories.
 * </pre>
 *
 * <p>Note that all getXxxx methods are optimized to the extent possible. This is done
 * to minimize the time it takes to retrieve map information when transferring data and
 * usually means that the method is declared final and simply returns the value of a
 * class variable. This (hopefully) allows the method to be inlined by a clever Java
 * compiler. It also means that no error is returned when it is inappropriate to call
 * the method. This occurs when the state is invalid, such as during map construction,
 * or when the state is valid but technically does not allow the method to be called,
 * such as when OrderInfo.getFixedOrderValue() is called but an order column is used.
 * In such cases, the returned value is not guaranteed to be correct.</p>
 *
 * <h4>Map Object State</h4>
 *
 * <p>Some collection properties and some single-valued properties are required
 * to have values; others are not. Because map factories frequently need an object
 * before they have enough information to completely construct it -- for example, a
 * RelatedClassMap might be constructed before the ClassMap it points to is fully
 * constructed -- map objects can have invalid state.</p>
 *
 * <p>This is legal during map construction, but causes undefined behavior when the
 * map is used to transfer data. That is, the data transfer classes such as DBMSToDOM
 * and DOMToDBMS assume that the map objects are valid.</p>
 *
 * <p>Because of this, map factories must ensure that a map has valid state before
 * they return it. (The only exception to this rule is database metadata. A map
 * factory can return an XMLDBMSMap that has not been initialized with database metadata
 * and allow the application to do this. In this case, it is the application's
 * responsibility to ensure the map is in a valid state.)</p>
 *
 * <p>Map factories can check whether a map has valid state by calling
 * MapChecker.check(XMLDBMSMap). Whether this is necessary depends on the map factory.
 * Some map factories keep track of map state themselves, others call MapChecker to
 * check for them.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 */

public class XMLDBMSMap extends MapBase
{
   //**************************************************************************
   // Private variables
   //**************************************************************************

   //  Options

   private boolean   emptyStringIsNull = false;
   private Hashtable defaultFormatters = new Hashtable(); // Indexed by type
   private Hashtable namedFormatters = new Hashtable();   // Indexed by name
   private Hashtable classMaps = new Hashtable();         // Indexed by XMLName
   private Hashtable classTableMaps = new Hashtable();    // Indexed by table name
   private Hashtable tables = new Hashtable();            // Indexed by table name
   private Hashtable uris = new Hashtable();              // Indexed by prefix
   private Hashtable prefixes = new Hashtable();          // Indexed by URI

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new XMLDBMSMap. */
   public XMLDBMSMap()
   {
      resetDefaultFormatters();
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   //**************************************************************************
   // Empty strings
   //**************************************************************************

   /** 
    * Are empty strings treated as NULLs?
    *
    * @return Whether empty strings are treated as NULLs.
    */
   public final boolean emptyStringIsNull()
   {
      // Idea from Richard Sullivan
      return emptyStringIsNull;
   }

   /** 
    * Set whether empty strings are treated as NULLs.
    *
    * @param flag Whether empty strings are treated as NULLs.
    */
   public void setEmptyStringIsNull(boolean flag)
   {
      emptyStringIsNull = flag;
   }

   //**************************************************************************
   // Default formatting objects
   //**************************************************************************

   /**
    * Get the default formatting object for a type.
    *
    * <p>This method returns an object that implements the
    * org.xmlmiddleware.conversions.formatters.StringFormatter interface.</p>
    *
    * @param type The JDBC type. Must be a valid value from the java.sql.Types class.
    *
    * @return The formatting object. This is never null.
    */
   public final StringFormatter getDefaultFormatter(int type)
   {
      if (!JDBCTypes.typeIsValid(type))
         throw new IllegalArgumentException("Not a valid JDBC type: " + type);
      return (StringFormatter)defaultFormatters.get(new Integer(type));
   }

   /**
    * Get a Hashtable containing the default formatting objects hashed by JDBC type.
    *
    * @return The Hashtable.
    */
   public final Hashtable getDefaultFormatters()
   {
      return (Hashtable)defaultFormatters.clone();
   }

   /**
    * Set the default formatting object for a type.
    *
    * <p>The format object must be an object that implements the 
    * org.xmlmiddleware.conversions.formatters.StringFormatter interface.</p>
    *
    * @param type The JDBC type.
    * @param formatter The formatting object. Must not be null.
    */
   public void setDefaultFormatter(int type, StringFormatter formatter)
   {
      if (!JDBCTypes.typeIsValid(type))
         throw new IllegalArgumentException("Not a valid JDBC type: " + type);
      checkArgNull(formatter, ARG_FORMATTER);

      defaultFormatters.put(new Integer(type), formatter);
   }

   /**
    * Reset the default formatting objects to their initial state.
    */
   public void resetDefaultFormatters()
   {
      NoFormatter     noFormatter = new NoFormatter();
      CharFormatter   charFormatter = new CharFormatter();
      NumberFormatter numberFormatter = new NumberFormatter(NumberFormat.getNumberInstance());

      setDefaultFormatter(Types.BINARY, noFormatter);
      setDefaultFormatter(Types.VARBINARY, noFormatter);
      setDefaultFormatter(Types.LONGVARBINARY, noFormatter);

      setDefaultFormatter(Types.CHAR, charFormatter);
      setDefaultFormatter(Types.VARCHAR, charFormatter);
      setDefaultFormatter(Types.LONGVARCHAR, charFormatter);

      setDefaultFormatter(Types.DOUBLE, numberFormatter);
      setDefaultFormatter(Types.FLOAT, numberFormatter);
      setDefaultFormatter(Types.REAL, numberFormatter);
      setDefaultFormatter(Types.DECIMAL, numberFormatter);
      setDefaultFormatter(Types.NUMERIC, numberFormatter);
      setDefaultFormatter(Types.BIGINT, numberFormatter);
      setDefaultFormatter(Types.INTEGER, numberFormatter);
      setDefaultFormatter(Types.SMALLINT, numberFormatter);
      setDefaultFormatter(Types.TINYINT, numberFormatter);

      setDefaultFormatter(Types.BIT, new BooleanFormatter());

      setDefaultFormatter(Types.DATE, new DateFormatter(DateFormat.getDateInstance()));
      setDefaultFormatter(Types.TIME, new DateFormatter(DateFormat.getTimeInstance()));
      setDefaultFormatter(Types.TIMESTAMP, new DateFormatter(DateFormat.getDateTimeInstance()));
   }

   //**************************************************************************
   // Named formatting objects
   //**************************************************************************

   /**
    * Get a named formatting object.
    *
    * @param name The name of the the formatting object. Must not be null.
    *
    * @return The formatting object. May be null.
    */
   public final StringFormatter getNamedFormatter(String name)
   {
      return (StringFormatter)namedFormatters.get(name);
   }

   /**
    * Get a Hashtable containing all named formatting objects hashed by name.
    *
    * @return The Hashtable.
    */
   public final Hashtable getNamedFormatters()
   {
      return (Hashtable)namedFormatters.clone();
   }

   /**
    * Add a named formatting object.
    *
    * @param name The name. Must not be null.
    * @param formatter The formatting object. Must not be null.
    */
   public void addNamedFormatter(String name, StringFormatter formatter)
      throws XMLMiddlewareException
   {
      checkArgNull(name, ARG_NAME);
      checkArgNull(formatter, ARG_FORMATTER);
      if (namedFormatters.get(name) != null)
         throw new XMLMiddlewareException("Formatter with the name " + name + " already exists.");
      namedFormatters.put(name, formatter);
   }

   /**
    * Remove a named formatting object.
    *
    * @param name The name. Must not be null.
    */
   public void removeNamedFormatter(String name, StringFormatter formatter)
      throws XMLMiddlewareException
   {
      checkArgNull(name, ARG_NAME);
      if (namedFormatters.remove(name) == null)
         throw new XMLMiddlewareException("No formatter with the name " + name + " found.");
   }

   /**
    * Remove all named formatting objects.
    */
   public void removeAllNamedFormatters()
   {
      namedFormatters.clear();
   }

   //**************************************************************************
   // Namespaces
   //**************************************************************************

   /**
    * Get a namespace URI.
    *
    * @param prefix The namespace prefix.
    *
    * @return The namespace URI. Null if the prefix is not used.
    */
   public final String getNamespaceURI(String prefix)
   {
      checkArgNull(prefix, ARG_PREFIX);
      return (String)uris.get(prefix);
   }

   /**
    * Get a namespace prefix.
    *
    * @param uri The namespace URI.
    *
    * @return The namespace prefix. Null if the URI is not used.
    */
   public final String getNamespacePrefix(String uri)
   {
      checkArgNull(uri, ARG_URI);
      return (String)prefixes.get(uri);
   }

   /**
    * Get a Hashtable containing all namespace URIs hashed by prefix.
    *
    * @return The Hashtable. May be empty.
    */
   public final Hashtable getNamespaceURIs()
   {
      return (Hashtable)uris.clone();
   }

   /**
    * Get a Hashtable containing all namespace prefixes hashed by URI.
    *
    * @return The Hashtable. May be empty.
    */
   public final Hashtable getNamespacePrefixes()
   {
      return (Hashtable)prefixes.clone();
   }

   /**
    * Add a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    * @param uri The namespace URI.
    *
    * @exception XMLMiddlewareException Thrown if the prefix or URI is already used.
    */
   public void addNamespace(String prefix, String uri)
      throws XMLMiddlewareException
   {
      checkArgNull(prefix, ARG_PREFIX);
      checkArgNull(uri, ARG_URI);
      if (uris.get(prefix) != null)
         throw new XMLMiddlewareException("Prefix already used: " + prefix);
      if (prefixes.get(uri) != null)
         throw new XMLMiddlewareException("URI already used: " + uri);
      uris.put(prefix, uri);
      prefixes.put(uri, prefix);
   }

   /**
    * Remove a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    *
    * @exception XMLMiddlewareException Thrown if the prefix is not found.
    */
   public void removeNamespaceByPrefix(String prefix)
      throws XMLMiddlewareException
   {
      String uri;

      checkArgNull(prefix, ARG_PREFIX);

      uri = (String)uris.remove(prefix);
      if (uri == null)
         throw new XMLMiddlewareException("Prefix not found: " + prefix);
      prefixes.remove(uri);
   }

   /**
    * Remove a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    *
    * @exception XMLMiddlewareException Thrown if the prefix is not found.
    */
   public void removeNamespaceByURI(String uri)
      throws XMLMiddlewareException
   {
      String prefix;

      checkArgNull(uri, ARG_URI);

      prefix = (String)prefixes.remove(uri);
      if (prefix == null)
         throw new XMLMiddlewareException("URI not found: " + uri);
      uris.remove(prefix);
   }

   /**
    * Remove all namespace URIs.
    */
   public void removeNamespaces()
   {
      uris.clear();
      prefixes.clear();
   }

   //**************************************************************************
   // Class maps
   //**************************************************************************

   /**
    * Gets the ClassMap used by an element type.
    *
    * <p>If the ClassMap for the element type uses another ClassMap (which might
    * use yet another ClassMap, and so on), this method returns the last ClassMap
    * in the chain. This is the ClassMap actually used to transfer data.</p>
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    *
    * @return The ClassMap. Null if the element type is not mapped.
    */
   public final ClassMap getClassMap(String uri, String localName)
   {
      // create checks of localName is null.

      return getClassMap(XMLName.create(uri, localName));
   }

   /**
    * Gets the ClassMap used by an element type.
    *
    * <p>If the ClassMap for the element type uses another ClassMap (which might
    * use yet another ClassMap, and so on), this method returns the last ClassMap
    * in the chain. This is the ClassMap actually used to transfer data.</p>
    *
    * @param elementTypeName XMLName of the element type.
    *
    * @return The ClassMap. Null if the element type is not mapped.
    */
   public final ClassMap getClassMap(XMLName elementTypeName)
   {
      ClassMap classMap = null, useClassMap;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);

      // Navigate the chain of used ClassMaps and return the last ClassMap in
      // the chain. This is the actual ClassMap to be used. Note that this is
      // guaranteed not to generate an infinite loop since ClassMap.useClassMap()
      // checks for loops.

      useClassMap = (ClassMap)classMaps.get(elementTypeName);
      while (useClassMap != null)
      {
         classMap = useClassMap;
         useClassMap = classMap.getUsedClassMap();
      }
      return classMap;
   }

   /**
    * Gets an Enumeration of all class maps.
    *
    * @return The Enumeration. May be empty.
    */
   public final Enumeration getClassMaps()
   {
      return classMaps.elements();
   }

   /**
    * Create a ClassMap for an element type and add it to the XMLDBMSMap.
    *
    * <p>If the element type has already been mapped, returns the existing ClassMap.</p>
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    *
    * @return The ClassMap for the element type.
    */
   public ClassMap createClassMap(String uri, String localName)
   {
      // create checks of localName is null.

      return createClassMap(XMLName.create(uri, localName));
   }

   /**
    * Create a ClassMap for an element type and add it to the XMLDBMSMap.
    *
    * <p>If the element type has already been mapped, returns the existing ClassMap.</p>
    *
    * @param elementTypeName The element type name.
    *
    * @return The ClassMap for the element type.
    */
   public ClassMap createClassMap(XMLName elementTypeName)
   {
      ClassMap classMap;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      classMap = (ClassMap)classMaps.get(elementTypeName);
      if (classMap == null)
      {
         classMap = ClassMap.create(elementTypeName);
         classMaps.put(elementTypeName, classMap);
      }
      return classMap;
   }

   /**
    * Add a ClassMap for an element type.
    *
    * @param classMap ClassMap for the element type.
    * @exception XMLMiddlewareException Thrown if the element type has already been mapped.
    */
   public void addClassMap(ClassMap classMap)
      throws XMLMiddlewareException
   {
      XMLName elementTypeName;
      Object o;

      checkArgNull(classMap, ARG_CLASSMAP);

      elementTypeName = classMap.getElementTypeName();
      o = classMaps.get(elementTypeName);
      if (o != null)
         throw new XMLMiddlewareException("Element type " + elementTypeName.getUniversalName() + " already mapped.");
      classMaps.put(elementTypeName, classMap);
   }

   /**
    * Remove the ClassMap for an element type.
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    *
    * @exception XMLMiddlewareException Thrown if the element type has not been mapped.
    */
   public void removeClassMap(String uri, String localName)
      throws XMLMiddlewareException
   {
      // create checks if localName is null.

      removeClassMap(XMLName.create(uri, localName));
   }

   /**
    * Remove the ClassMap for an element type.
    *
    * @param elementTypeName XMLName of the element type.
    *
    * @exception XMLMiddlewareException Thrown if the element type has not been mapped.
    */
   public void removeClassMap(XMLName elementTypeName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      o = classMaps.remove(elementTypeName);
      if (o == null)
         throw new XMLMiddlewareException("Element type " + elementTypeName.getUniversalName() + " not mapped.");
   }

   /**
    * Remove the ClassMaps for all element types.
    */
   public void removeAllClassMaps()
   {
      classMaps.clear();
   }

   //**************************************************************************
   // Class table maps
   //**************************************************************************

   /**
    * Gets a ClassTableMap for a table.
    *
    * @param databaseName Name of the database. May be null.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The ClassTableMap. Null if the table is not mapped as a class table.
    */
   public final ClassTableMap getClassTableMap(String databaseName, String catalogName, String schemaName, String tableName)
   {
      return (ClassTableMap)classTableMaps.get(Table.getHashName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Gets a ClassTableMap for a table.
    *
    * @param table The Table
    *
    * @return The ClassTableMap. Null if the table is not mapped as a class table.
    */
   public final ClassTableMap getClassTableMap(Table table)
   {
      return (ClassTableMap)classTableMaps.get(table.getHashName());
   }

   /**
    * Gets an Enumeration of all class table maps.
    *
    * @return The Enumeration. May be empty.
    */
   public final Enumeration getClassTableMaps()
   {
      return classTableMaps.elements();
   }

   /**
    * Create a ClassTableMap for a table and add it to the XMLDBMSMap.
    *
    * <p>If the table has already been mapped as a class table, returns the
    * existing ClassTableMap.</p>
    *
    * @param table The Table being mapped.
    *
    * @return The ClassTableMap for the table.
    */
   public ClassTableMap createClassTableMap(Table table)
   {
      ClassTableMap classTableMap;
      String        name;

      checkArgNull(table, ARG_TABLE);
      name = table.getHashName();
      classTableMap = (ClassTableMap)classTableMaps.get(name);
      if (classTableMap == null)
      {
         classTableMap = ClassTableMap.create(table);
         classTableMaps.put(name, classTableMap);
      }
      return classTableMap;
   }

   /**
    * Add a ClassTableMap for a table.
    *
    * @param classTableMap ClassTableMap for the table. Must not be null.
    * @exception XMLMiddlewareException Thrown if the table has already been mapped.
    */
   public void addClassTableMap(ClassTableMap classTableMap)
      throws XMLMiddlewareException
   {
      String name;
      Object o;

      checkArgNull(classTableMap, ARG_CLASSTABLEMAP);
      name = classTableMap.getTable().getHashName();
      o = classTableMaps.get(name);
      if (o != null)
         throw new XMLMiddlewareException("Table already mapped: " + classTableMap.getTable().getUniversalName());
      classTableMaps.put(name, classTableMap);
   }

   /**
    * Remove the ClassTableMap for a table.
    *
    * @param databaseName Name of the database. May be null.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @exception XMLMiddlewareException Thrown if the table has not been mapped as a class table.
    */
   public void removeClassTableMap(String databaseName, String catalogName, String schemaName, String tableName)
      throws XMLMiddlewareException
   {
      Object o;

      o = classTableMaps.remove(Table.getHashName(databaseName, catalogName, schemaName, tableName));
      if (o == null)
         throw new XMLMiddlewareException("Table not mapped as a class table: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Remove the ClassTableMap for a table.
    *
    * @param table The Table
    *
    * @exception XMLMiddlewareException Thrown if the table has not been mapped as a class table.
    */
   public void removeClassTableMap(Table table)
      throws XMLMiddlewareException
   {
      Object o;

      o = classTableMaps.remove(table.getHashName());
      if (o == null)
         throw new XMLMiddlewareException("Table not mapped as a class table: " + table.getUniversalName());
   }

   /**
    * Remove the ClassTablesMaps for all tables.
    */
   public void removeAllClassTableMaps()
   {
      classTableMaps.clear();
   }

   //**************************************************************************
   // Tables
   //**************************************************************************

   /**
    * Gets a Table.
    *
    * @param databaseName Name of the database. May be null.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The Table. Null if the table does not exist.
    */
   public final Table getTable(String databaseName, String catalogName, String schemaName, String tableName)
   {
      return (Table)tables.get(Table.getHashName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Gets an Enumeration of all Tables.
    *
    * @return The Enumeration. May be empty.
    */
   public final Enumeration getTables()
   {
      return tables.elements();
   }

   /**
    * Create a Table and add it to the XMLDBMSMap.
    *
    * <p>If the table exists, returns the existing Table.</p>
    *
    * @param databaseName Name of the database. May be null.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The Table.
    */
   public Table createTable(String databaseName, String catalogName, String schemaName, String tableName)
   {
      Table  table;
      String name;
      Object o;

      name = Table.getHashName(databaseName, catalogName, schemaName, tableName);
      table = (Table)tables.get(name);
      if (table == null)
      {
         table = Table.create(databaseName, catalogName, schemaName, tableName);
         tables.put(name, table);
      }
      return table;
   }

   /**
    * Add a Table.
    *
    * @param table The Table.
    * @exception XMLMiddlewareException Thrown if the table already exists.
    */
   public void addTable(Table table)
      throws XMLMiddlewareException
   {
      Object o;
      String name;

      checkArgNull(table, ARG_TABLE);
      name = table.getHashName();
      o = tables.get(name);
      if (o != null)
         throw new XMLMiddlewareException("Table already exists: " + table.getUniversalName());
      tables.put(name, table);
   }

   /**
    * Remove a Table.
    *
    * <p>This method should be used carefully, as numerous other map objects
    * point to Tables. Those objects should be deleted before this method is called.</p>
    *
    * @param databaseName Name of the database. May be null.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @exception XMLMiddlewareException Thrown if the table does not exist.
    */
   public void removeTable(String databaseName, String catalogName, String schemaName, String tableName)
      throws XMLMiddlewareException
   {
      Object o;

      o = tables.remove(Table.getHashName(databaseName, catalogName, schemaName, tableName));
      if (o == null)
         throw new XMLMiddlewareException("Table does not exist: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Remove a Table.
    *
    * <p>This method should be used carefully, as numerous other map objects
    * point to Tables. Those objects should be deleted before this method is called.</p>
    *
    * @param table The Table
    *
    * @exception XMLMiddlewareException Thrown if the table does not exist.
    */
   public void removeTable(Table table)
      throws XMLMiddlewareException
   {
      Object o;

      o = tables.remove(table.getHashName());
      if (o == null)
         throw new XMLMiddlewareException("Table does not exist: " + table.getUniversalName());
   }

   /**
    * Remove all Tables.
    *
    * <p>This method should be used carefully, as numerous other map objects
    * point to Tables. Those objects should be deleted before this method is called.</p>
    */
   public void removeAllTables()
   {
      tables.clear();
   }

   // ********************************************************************
   // Table metadata
   // ********************************************************************

   /**
    * Checks whether metadata has been set for the columns in all tables.
    *
    * <p>If the returned value is non-null, it can be used to determine
    * the column for which metadata has not been set.</p>
    *
    * @return The first Table for which metadata has not been set or
    *    null if metadata has been set for all tables.
    */
   public Table checkMetadata()
   {
      Enumeration tables;
      Table       table;

      tables = getTables();
      while (tables.hasMoreElements())
      {
         table = (Table)tables.nextElement();
         if (table.checkMetadata() != null) return table;
      }
      return null;
   }
}
