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
// This software was originally developed at the Technical University
// of Darmstadt, Germany.

// Version 2.0
// Changes from version 1.0:
// * Change declaration of byte array constants
// * Changed error messages in addColumnMetadata and checkColumnTypesSet
// * Fixed bug in addResultSetMetadata where column number not set
// * Quote table names in buildInsert/Select/CreateTableString
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;

import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Describes how an XML document is mapped to a database and vice versa;
 * <a href="../readme.htm#NotForUse">for limited use</a>.
 *
 * <h3>For people writing XML-DBMS applications</h3>
 *
 * <p>Map objects describe how XML documents are mapped to databases and
 * vice versa. You can think of them as compiled versions of map documents,
 * although they can be created from other sources as well, such as a map
 * factory that creates a Map object from a DTD.</p>
 *
 * <p>Applications that use XML-DBMS to transfer data treat Map objects as
 * opaque objects. That is, they call a map factory to create a Map object,
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
 *    // Create a Map from sales.map.
 *    map = factory.createMap(new InputSource(new FileReader("sales.map")));
 * </pre>
 *
 * <h3>For people writing map factories and other internal XML-DBMS code</h3>
 *
 * <p>Map objects are actually a graph of related objects. This graph can be fairly
 * complex and it is a good idea to familiarize yourself with all map objects before
 * trying to use them. (With a capital M, Map object refers to an object of the Map
 * class. With a lower case m, map object refers to any of the objects in the graph,
 * such as a Map, a ClassMap, or a ColumnMap.) One way to understand the graph is to
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
 *    Map
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
 *    MapException
 *
 *    MapBase
 *       Map
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
 *    Map
 *       ClassMap (hashtable of)
 *          Table
 *             Column (hashtable of)
 *             Key (hashtables of)
 *                Column (array of)
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
 * except Map, which has a simple constructor. For example, the following code
 * creates a LinkInfo object.</p>
 *
 * <pre>
 *    LinkInfo linkInfo = LinkInfo.create(parentKey, childKey);
 * </pre>
 *
 * <p>In many cases, factory methods can also be found on the parent object.
 * For example, the following code creates a ClassMap object from a Map object:</p>
 *
 * <pre>
 *    classMap = map.createClassMap(null, "foo");
 * </pre>
 *
 * <p>When the factory method is called on the parent, the child object is
 * automatically added to the parent object. Furthermore, if a child object
 * with the same name already exists, that object will be returned instead of
 * creating a new object. Since objects are frequently used in multiple places
 * -- for example, the same ClassMap can appear in a Map and a RelatedClassMap --
 * this is useful, as the alternative would be to first check if
 * the object exists, then create it if it doesn't. For example, the following
 * code is equivalent to calling createClassMap on a Map object:</p>
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
 * factory can return a Map that has not been initialized with database metadata
 * and allow the application to do this. In this case, it is the application's
 * responsibility to ensure the map is in a valid state.)</p>
 *
 * <p>Map factories can check whether a map has valid state by calling
 * MapChecker.check(Map). Whether this is necessary depends on the map factory.
 * Some map factories keep track of map state themselves, others call MapChecker to
 * check for them.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 */

public class Map extends MapBase
{
   //**************************************************************************
   // Private variables
   //**************************************************************************

   //  Options

   private boolean   emptyStringIsNull = false;
   private Hashtable dateFormats = new Hashtable();     // Indexed by format name
   private Hashtable timeFormats = new Hashtable();     // Indexed by format name
   private Hashtable datetimeFormats = new Hashtable(); // Indexed by format name
   private Hashtable numberFormats = new Hashtable();   // Indexed by format name
   private Hashtable classMaps = new Hashtable();       // Indexed by universal name
   private Hashtable classTableMaps = new Hashtable();  // Indexed by table name
   private Hashtable tables = new Hashtable();          // Indexed by table name
   private Hashtable uris = new Hashtable();            // Indexed by prefix
   private Hashtable prefixes = new Hashtable();        // Indexed by URI

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new Map. */
   public Map()
   {
      initFormats();
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
   // Default formats
   //**************************************************************************

   /**
    * Set the default formats for columns.
    *
    * <p>This method sets the format for all columns for which the format has not
    * explicitly been set. It must be called after the data type has been set for
    * each column.</p>
    *
    */
/*
   public void setDefaultFormats()
   {
      Enumeration tables, columns;
      Table       table;
      Column      column;
      int         type;

      tables = getTables();
      while (tables.hasMoreElements())
      {
         table = tables.nextElement()
         columns = table.getColumns();
         while (columns.hasMoreElements())
         {
            column = columns.nextElement();
            if (column.useDefaultFormat())
            {
               type = column.getType();
               if (type == Types.NULL)
                  throw new IllegalStateException("Type not set for column: " + column.getName());
               switch (type)
               {
                  case Types.DATE:
                     column.setDefaultFormat(getDefaultDateFormat());
                     break;

                  case Types.TIME:
                     column.setDefaultFormat(getDefaultTimeFormat());
                     break;

                  case Types.DATETIME:
                     column.setDefaultFormat(getDefaultDateTimeFormat());
                     break;

                  case Types.BIGINT:
                  case Types.BIT:
                  case Types.DECIMAL:
                  case Types.DOUBLE:
                  case Types.FLOAT:
                  case Types.INTEGER:
                  case Types.NUMERIC:
                  case Types.REAL:
                  case Types.SMALLINT:
                  case Types.TINYINT:
                     column.setDefaultFormat(getDefaultNumberFormat());
                     break;

                  default:
                     break;
               }
            }
         }
      }
   }
*/

   //**************************************************************************
   // Date formats
   //**************************************************************************

   /**
    * Get a date format by name.
    *
    * @return The date format.
    */
   public final DateFormat getDateFormat(String name)
   {
      return (DateFormat)getFormat(dateFormats, name);
   }

   /**
    * Get the default date format.
    *
    * @return The default date format.
    */
   public final DateFormat getDefaultDateFormat()
   {
      return (DateFormat)getDefaultFormat(dateFormats, DEFAULTDATE);
   }

   /**
    * Get a Hashtable containing all date formats hashed by name.
    *
    * @return The Hashtable.
    */
   public final Hashtable getDateFormats()
   {
      return (Hashtable)dateFormats.clone();
   }

   /**
    * Add a date format.
    *
    * <p>Use the name "DefaultDate" to override the default date format.
    * Default formats must be explicitly enabled by calling setDefaultFormats().</p>
    *
    * @param name Format name.
    * @param format The date format.
    * @exception MapException Thrown if the format already exists
    *    (except for the default date format, which may be overridden).
    */
   public void addDateFormat(String name, DateFormat format)
      throws MapException
   {
      addFormat(dateFormats, name, format);
   }

   /**
    * Remove a date format.
    *
    * @param name Format name.
    * @exception MapException Thrown if the format is not found.
    */
   public void removeDateFormat(String name)
      throws MapException
   {
      removeFormat(dateFormats, name);
   }

   /**
    * Remove all date formats except the default format.
    */
   public void removeAllDateFormats()
   {
      removeAllFormats(dateFormats);
   }

   //**************************************************************************
   // Time formats
   //**************************************************************************

   /**
    * Get a time format by name.
    *
    * @return The time format.
    * @exception MapException Thrown if the format is not found.
    */
   public final DateFormat getTimeFormat(String name)
      throws MapException
   {
      return (DateFormat)getFormat(timeFormats, name);
   }

   /**
    * Get the default time format.
    *
    * @return The default time format.
    */
   public final DateFormat getDefaultTimeFormat()
   {
      return (DateFormat)getDefaultFormat(timeFormats, DEFAULTTIME);
   }

   /**
    * Get a Hashtable containing all time formats hashed by name.
    *
    * @return The Hashtable.
    */
   public final Hashtable getTimeFormats()
   {
      return (Hashtable)timeFormats.clone();
   }

   /**
    * Add a time format.
    *
    * <p>Use the name "DefaultTime" to override the default time format.
    * Default formats must be explicitly enabled by calling setDefaultFormats().</p>
    *
    * @param name Format name.
    * @param format The time format.
    * @exception MapException Thrown if the format already exists
    *    (except for the default time format, which may be overridden).
    */
   public void addTimeFormat(String name, DateFormat format)
      throws MapException
   {
      addFormat(timeFormats, name, format);
   }

   /**
    * Remove a time format.
    *
    * <p>The default format may not be removed.</p>
    *
    * @param name Format name.
    * @exception MapException Thrown if the format is not found.
    */
   public void removeTimeFormat(String name)
      throws MapException
   {
      removeFormat(timeFormats, name);
   }

   /**
    * Remove all time formats except the default format.
    */
   public void removeAllTimeFormats()
   {
      removeAllFormats(timeFormats);
   }

   //**************************************************************************
   // Datetime formats
   //**************************************************************************

   /**
    * Get a datetime format by name.
    *
    * @return The datetime format.
    */
   public final DateFormat getDateTimeFormat(String name)
   {
      return (DateFormat)getFormat(datetimeFormats, name);
   }

   /**
    * Get the default datetime format.
    *
    * @return The default datetime format.
    */
   public final DateFormat getDefaultDateTimeFormat()
   {
      return (DateFormat)getDefaultFormat(datetimeFormats, DEFAULTDATETIME);
   }

   /**
    * Get a Hashtable containing all datetime formats hashed by name.
    *
    * @return The Hashtable.
    */
   public final Hashtable getDateTimeFormats()
   {
      return (Hashtable)datetimeFormats.clone();
   }

   /**
    * Add a datetime format.
    *
    * <p>Use the name "DefaultDateTime" to override the default datetime format.
    * Default formats must be explicitly enabled by calling setDefaultFormats().</p>
    *
    * @param name Format name.
    * @param datetimeFormat The datetime format.
    * @exception MapException Thrown if the format already exists
    *    (except for the default datetime format, which may be overridden).
    */
   public void addDateTimeFormat(String name, DateFormat format)
      throws MapException
   {
      addFormat(datetimeFormats, name, format);
   }

   /**
    * Remove a datetime format.
    *
    * <p>The default format may not be removed.</p>
    *
    * @param name Format name.
    * @exception MapException Thrown if the format is not found.
    */
   public void removeDateTimeFormat(String name)
      throws MapException
   {
      removeFormat(datetimeFormats, name);
   }

   /**
    * Remove all datetime formats except the default format.
    */
   public void removeAllDateTimeFormats()
   {
      removeAllFormats(datetimeFormats);
   }

   //**************************************************************************
   // Number formats
   //**************************************************************************

   /**
    * Get a number format by name.
    *
    * @return The number format.
    */
   public final NumberFormat getNumberFormat(String name)
   {
      return (NumberFormat)getFormat(numberFormats, name);
   }

   /**
    * Get the default number format.
    *
    * @return The default number format.
    */
   public final NumberFormat getDefaultNumberFormat()
   {
      return (NumberFormat)getDefaultFormat(numberFormats, DEFAULTNUMBER);
   }

   /**
    * Get a Hashtable containing all number formats hashed by name.
    *
    * @return The Hashtable.
    */
   public final Hashtable getNumberFormats()
   {
      return (Hashtable)numberFormats.clone();
   }

   /**
    * Add a number format.
    *
    * <p>Use the name "DefaultNumber" to override the default number format.
    * Default formats must be explicitly enabled by calling setDefaultFormats().</p>
    *
    * @param name Format name.
    * @param numberFormat The number format.
    * @exception MapException Thrown if the format already exists
    *    (except for the default number format, which may be overridden).
    */
   public void addNumberFormat(String name, NumberFormat format)
      throws MapException
   {
      addFormat(numberFormats, name, format);
   }

   /**
    * Remove a number format.
    *
    * <p>The default format may not be removed.</p>
    *
    * @param name Format name.
    * @exception MapException Thrown if the format is not found.
    */
   public void removeNumberFormat(String name)
      throws MapException
   {
      removeFormat(numberFormats, name);
   }

   /**
    * Remove all number formats except the default format.
    */
   public void removeAllNumberFormats()
   {
      removeAllFormats(numberFormats);
   }

   //**************************************************************************
   // Namespaces
   //**************************************************************************

   /**
    * Get a namespace URI.
    *
    * @param prefix The namespace prefix.
    *
    * @return The namespace URI.
    * @exception MapException Thrown if the prefix is not found.
    */
   public final String getNamespaceURI(String prefix)
      throws MapException
   {
      String uri;
      checkArgNull(prefix, ARG_PREFIX);
      uri = (String)uris.get(prefix);
      if (uri == null)
         throw new MapException("Prefix not found: " + prefix);
      return uri;
   }

   /**
    * Get a namespace prefix.
    *
    * @param uri The namespace URI.
    *
    * @return The namespace prefix.
    * @exception MapException Thrown if the URI is not found.
    */
   public final String getNamespacePrefix(String uri)
      throws MapException
   {
      String prefix;
      checkArgNull(uri, ARG_URI);
      prefix = (String)prefixes.get(uri);
      if (prefix == null)
         throw new MapException("URI not found: " + uri);
      return prefix;
   }

   /**
    * Get a Hashtable containing all namespace URIs hashed by prefix.
    *
    * @return The Hashtable.
    */
   public final Hashtable getNamespaceURIs()
   {
      return (Hashtable)uris.clone();
   }

   /**
    * Get a Hashtable containing all namespace prefixes hashed by URI.
    *
    * @return The Hashtable.
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
    * @exception MapException Thrown if the prefix or URI is already used.
    */
   public void addNamespace(String prefix, String uri)
      throws MapException
   {
      checkArgNull(prefix, ARG_PREFIX);
      checkArgNull(uri, ARG_URI);
      if (uris.get(prefix) != null)
         throw new MapException("Prefix already used: " + prefix);
      if (prefixes.get(uri) != null)
         throw new MapException("URI already used: " + uri);
      uris.put(prefix, uri);
      prefixes.put(uri, prefix);
   }

   /**
    * Remove a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    *
    * @exception MapException Thrown if the prefix is not found.
    */
   public void removeNamespaceByPrefix(String prefix)
      throws MapException
   {
      String uri;

      checkArgNull(prefix, ARG_PREFIX);

      uri = (String)uris.remove(prefix);
      if (uri == null)
         throw new MapException("Prefix not found: " + prefix);
      prefixes.remove(uri);
   }

   /**
    * Remove a namespace prefix and URI.
    *
    * @param prefix The namespace prefix.
    *
    * @exception MapException Thrown if the prefix is not found.
    */
   public void removeNamespaceByURI(String uri)
      throws MapException
   {
      String prefix;

      checkArgNull(uri, ARG_URI);

      prefix = (String)prefixes.remove(uri);
      if (prefix == null)
         throw new MapException("URI not found: " + uri);
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
      return getClassMap(XMLName.getUniversalName(uri, localName));
   }

   /**
    * Gets the ClassMap used by an element type.
    *
    * <p>If the ClassMap for the element type uses another ClassMap (which might
    * use yet another ClassMap, and so on), this method returns the last ClassMap
    * in the chain. This is the ClassMap actually used to transfer data.</p>
    *
    * @param universalName Universal name of the element type.
    *
    * @return The ClassMap. Null if the element type is not mapped.
    */
   public final ClassMap getClassMap(String universalName)
   {
      ClassMap classMap = null, useClassMap;

      // Navigate the chain of used ClassMaps and return the last ClassMap in
      // the chain. This is the actual ClassMap to be used. Note that this is
      // guaranteed not to generate an infinite loop since ClassMap.useClassMap()
      // checks for loops.

      useClassMap = (ClassMap)classMaps.get(universalName);
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
    * Create a ClassMap for an element type and add it to the Map.
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
      return createClassMap(XMLName.create(uri, localName));
   }

   /**
    * Create a ClassMap for an element type and add it to the Map.
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
      String   universalName;

      universalName = elementTypeName.getUniversalName();
      classMap = (ClassMap)classMaps.get(universalName);
      if (classMap == null)
      {
         classMap = ClassMap.create(elementTypeName);
         classMaps.put(universalName, classMap);
      }
      return classMap;
   }

   /**
    * Add a ClassMap for an element type.
    *
    * @param classMap ClassMap for the element type.
    * @exception MapException Thrown if the element type has already been mapped.
    */
   public void addClassMap(ClassMap classMap)
      throws MapException
   {
      String universalName;
      Object o;

      checkArgNull(classMap, ARG_CLASSMAP);

      universalName = classMap.getElementTypeName().getUniversalName();
      o = classMaps.get(universalName);
      if (o != null)
         throw new MapException("Element type " + universalName + " already mapped.");
      classMaps.put(universalName, classMap);
   }

   /**
    * Remove the ClassMap for an element type.
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    *
    * @exception MapException Thrown if the element type has not been mapped.
    */
   public void removeClassMap(String uri, String localName)
      throws MapException
   {
      // getUniversalName checks if localName is null.

      removeClassMap(XMLName.getUniversalName(uri, localName));
   }

   /**
    * Remove the ClassMap for an element type.
    *
    * @param universalName Universal name of the element type.
    *
    * @exception MapException Thrown if the element type has not been mapped.
    */
   public void removeClassMap(String universalName)
      throws MapException
   {
      Object o;

      checkArgNull(universalName, ARG_UNIVERSALNAME);
      o = classMaps.remove(universalName);
      if (o == null)
         throw new MapException("Element type " + universalName + " not mapped.");
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
      return (ClassTableMap)classTableMaps.get(Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
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
    * Create a ClassTableMap for a table and add it to the Map.
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
      name = table.getUniversalName();
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
    * @exception MapException Thrown if the table has already been mapped.
    */
   public void addClassTableMap(ClassTableMap classTableMap)
      throws MapException
   {
      String name;
      Object o;

      checkArgNull(classTableMap, ARG_CLASSTABLEMAP);
      name = classTableMap.getTable().getUniversalName();
      o = classTableMaps.get(name);
      if (o != null)
         throw new MapException("Table already mapped: " + name);
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
    * @exception MapException Thrown if the table has not been mapped as a class table.
    */
   public void removeClassTableMap(String databaseName, String catalogName, String schemaName, String tableName)
      throws MapException
   {
      ClassTableMap classTableMap;
      String name;

      name = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);
      classTableMap = (ClassTableMap)classTableMaps.remove(name);
      if (classTableMap == null)
         throw new MapException("Table not mapped as a class table: " + name);
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
      return (Table)tables.get(Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
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
    * Create a Table and add it to the Map.
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
      Object           o;

      name = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);
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
    * @exception MapException Thrown if the table already exists.
    */
   public void addTable(Table table)
      throws MapException
   {
      Object o;
      String name;

      checkArgNull(table, ARG_TABLE);
      name = table.getUniversalName();
      o = tables.get(name);
      if (o != null)
         throw new MapException("Table already exists: " + name);
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
    * @exception MapException Thrown if the table does not exist.
    */
   public void removeTable(String databaseName, String catalogName, String schemaName, String tableName)
      throws MapException
   {
      Object o;
      String name;

      name = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);
      o = tables.remove(name);
      if (o == null)
         throw new MapException("Table does not exist: " + name);
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

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void initFormats()
   {
      dateFormats.put(DEFAULT, DateFormat.getDateInstance());
      timeFormats.put(DEFAULT, DateFormat.getTimeInstance());
      datetimeFormats.put(DEFAULT, DateFormat.getDateTimeInstance());
      numberFormats.put(DEFAULT, NumberFormat.getInstance());
   }

   private final Object getFormat(Hashtable hash, String name)
   {
      Object o;

      checkArgNull(name, ARG_NAME);
      o = hash.get(name);
      return o;
   }

   private final Object getDefaultFormat(Hashtable hash, String defaultName)
   {
      Object o;

      // First try to get the user-specified default (DefaultDate, etc.).
      // If this does not exist, use the system default.

      o = hash.get(defaultName);
      if (o == null)
      {
         o = hash.get(DEFAULT);
      }
      return o;
   }

   private void addFormat(Hashtable hash, String name, Format format)
      throws MapException
   {
      Object o;

      checkArgNull(name, ARG_NAME);
      checkArgNull(format, ARG_FORMAT);

      if (name.equals(DEFAULT))
         throw new IllegalArgumentException("The name \"" + DEFAULT + "\" is reserved and may not be used.");

      // Check if the format name is already used.
      o = hash.get(name);
      if (o != null)
         throw new MapException("Format already exists: " + name);
      hash.put(name, format);
   }

   private void removeFormat(Hashtable hash, String name)
      throws MapException
   {
      Object o;

      checkArgNull(name, ARG_NAME);
      if (name.equals(DEFAULT))
         throw new IllegalArgumentException("The name \"" + DEFAULT + "\" is reserved and may not be used.");

      o = hash.remove(name);
      if (o == null)
         throw new MapException("Format not found: " + name);
   }

   private void removeAllFormats(Hashtable hash)
   {
      Object o;

      o = hash.get(DEFAULT);
      hash.clear();
      hash.put(DEFAULT, o);
   }
}
