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
// Changes from version 1.01:
// * Changed to use Writer instead of OutputStream
// * Cleaned up public methods.
// * Modified for new map objects / DTD.

package org.xmlmiddleware.xmldbms.maps.utils;

import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.factories.*;
import org.xmlmiddleware.xmlutils.*;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

/**
 * Serializes an XMLDBMSMap object to a character stream.
 *
 * <p>If you want to use a specific encoding, the Writer must be an OutputStreamWriter
 * or a subclass of an OutputStreamWriter. For example, you might use the following
 * code to write a map file with the Shift_JIS encoding:</p>
 *
 * <pre>
 *    // Construct the FileOutputStream.
 *    OutputStream out = new FileOutputStream("sales.map");
 *    <br />
 *    // Construct the OutputStreamWriter with the Shift_JIS encoding. This may
 *    // throw an UnsupportedEncodingException.
 *    Writer writer = new OutputStreamWriter(out, "Shift_JIS");
 *    <br />
 *    // Construct the MapSerializer.
 *    MapSerializer serializer = new MapSerializer(writer);
 *    <br />
 *    // Serialize the map.
 *    serializer.serialize(map);
 *    <br />
 *    // Close the file.
 *    writer.close();
 * </pre>
 *
 * <p>If you want to use the default encoding, you can just use a FileWriter. However,
 * no encoding declaration will be written in the XML declaration. For example:</p>
 *
 * <pre>
 *    // Construct a new FileWriter.
 *    Writer writer = new FileWriter("sales.map");
 *    <br />
 *    // Construct the MapSerializer.
 *    MapSerializer serializer = new MapSerializer(writer);
 *    <br />
 *    // Serialize the map.
 *    serializer.serialize(map);
 *    <br />
 *    // Close the file.
 *    writer.close();
 * </pre>
 *
 * <p><b>WARNING:</b> MapSerializer cannot generate DateFormat, TimeFormat,
 * DateTimeFormat, and NumberFormat elements. This is because the
 * underlying formatting objects do not contain enough information to
 * construct these elements. MapSerializer can generate SimpleDateFormat,
 * DecimalFormat, and FormatClass elements, but cannot generate Locale
 * children of SimpleDateFormat and DecimalFormat elements, again due to
 * lack of information. However, it can generate localized patterns.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class MapSerializer extends XMLWriter
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private static String XMLDBMS2DTD = "xmldbms2.dtd";
   private static String SPACE = " ";

   //**************************************************************************
   // Variables
   //**************************************************************************

   private XMLDBMSMap map;
   private Hashtable  uris = null;
   private Hashtable  prefixes = null;
   private Hashtable  defaultFormatters = new Hashtable();
   private Hashtable  formatterNames = new Hashtable();

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new MapSerializer. */
   public MapSerializer()
   {
      allocateAttrs(6);
   }

   /**
    * Construct a new MapSerializer and set the Writer.
    *
    * @param writer The writer. The writer must implement the write(String,int,int)
    *    and write(int) methods.
    */
   public MapSerializer(Writer writer)
   {
      super(writer);
      allocateAttrs(6);
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Use the specified prefixes.
    *
    * @param prefixes An array of namespace prefixes. If this is null, then
    *    serialize() uses the prefixes in the XMLDBMSMap.
    * @param uris An array of namespace URIs corresponding to the prefixes in
    *    the prefixes argument.
   **/
   public void usePrefixes(String[] prefixes, String[] uris)
   {
      if (prefixes == null)
      {
         this.prefixes = null;
         this.uris = null;
      }
      else
      {
         if (prefixes.length != uris.length)
            throw new IllegalArgumentException("prefixes and uris arrays must be same length.");
         this.prefixes = new Hashtable(prefixes.length);
         this.uris = new Hashtable(prefixes.length);
         for (int i = 0; i < prefixes.length; i++)
         {
            this.prefixes.put(uris[i], prefixes[i]);
            this.uris.put(prefixes[i], uris[i]);
         }
      }
   }

   /**
    * Serialize an XMLDBMSMap using the XML-DBMS mapping language.
    *
    * <p>No system or public ID is written in the DOCTYPE statement.</p>
    *
    * @param map The XMLDBMSMap.
    * @exception IOException Thrown if an I/O exception occurs.
    * @exception XMLMiddlewareException Thrown if a prefix was not found for a URI
    *    or a map error occurs.
    */
   public void serialize(XMLDBMSMap map)
      throws IOException, XMLMiddlewareException
   {
      this.map = map;
      serialize(null, null);
   }

   /**
    * Serialize an XMLDBMSMap using the XML-DBMS mapping language.
    *
    * @param map The XMLDBMSMap.
    * @param systemID System ID of the DTD. If this is null, "xmldbms2.dtd" is used.
    * @param publicID Public ID of the DTD. May be null.
    * @exception IOException Thrown if an I/O exception occurs.
    * @exception XMLMiddlewareException Thrown if a prefix was not found for a URI
    *    or a map error occurs.
    */
   public void serialize(XMLDBMSMap map, String systemID, String publicID)
      throws IOException, XMLMiddlewareException
   {
      this.map = map;
      if (systemID == null)
      {
         systemID = XMLDBMS2DTD;
      }
      serialize(systemID, publicID);
   }

   //**************************************************************************
   // Private methods - serialize()
   //**************************************************************************

   private void serialize(String systemID, String publicID)
      throws IOException, XMLMiddlewareException
   {
      writeMapStart(systemID, publicID);
      writeOptions();
      writeDatabases();
      writeMaps();
      writeMapEnd();
   }

   //**************************************************************************
   // Private methods - map serialization (in alphabetical order)
   //**************************************************************************

   private void writeAttribute(XMLName attributeName)
      throws IOException, XMLMiddlewareException
   {
      attrs[0] = MapConst.ATTR_NAME;
      values[0] = getQualifiedName(attributeName);
      writeElementStart(MapConst.ELEM_ATTRIBUTE, 1, true);
   }

   private void writeClassMap(ClassMap classMap)
      throws IOException, XMLMiddlewareException
   {
      ClassMap baseClassMap, useClassMap;
      Table    table;
      int      count;

      // Start the <ClassMap> element.

      writeElementStart(MapConst.ELEM_CLASSMAP, 0, false);

      // Write the <ElementType> element.

      writeElementType(classMap.getElementTypeName());

      // Check whether the ClassMap uses a different ClassMap. If so, write
      // a <UseClassMap> element. If not, write the rest of the <ClassMap> element.

      useClassMap = classMap.getUsedClassMap();
      if (useClassMap != null)
      {
         writeUseClassMap(useClassMap);
      }
      else
      {
         // Write the <Extends> element if a base class map exists.

         writeExtends(classMap);

         // Write the <ToClassTable> element.

         table = classMap.getTable();
         count = setTableNameAttributes(table);
         writeElementStart(MapConst.ELEM_TOCLASSTABLE, count, true);

         // Write the property maps, inline class maps, and related class maps.

         writePropertyInlineRelatedMaps(classMap);
      }

      // End the <ClassMap> element.

      writeElementEnd(MapConst.ELEM_CLASSMAP);
   }

   private void writeClassMaps()
      throws IOException, XMLMiddlewareException
   {
      Enumeration classMaps;
      ClassMap    classMap;

      classMaps = map.getClassMaps();
      while (classMaps.hasMoreElements())
      {
         classMap = (ClassMap)classMaps.nextElement();
         writeClassMap(classMap);
      }
   }

   private void writeColumns(Table table)
      throws IOException
   {
      Enumeration columns;
      Column      column;

      columns = table.getColumns();
      while (columns.hasMoreElements())
      {
         column = (Column)columns.nextElement();
         writeColumn(column);
      }
   }

   private void writeColumn(Column column)
      throws IOException
   {
      int             count = 0, type, length, precision, scale, nullability;
      StringFormatter formatter;
      String          formatName;

      attrs[count] = MapConst.ATTR_NAME;
      values[count++] = column.getName();

      type = column.getType();
      if (type != Types.NULL)
      {
         attrs[count] = MapConst.ATTR_DATATYPE;
         values[count++] = JDBCTypes.getName(column.getType());
      }

      if (JDBCTypes.typeIsChar(type) || JDBCTypes.typeIsBinary(type))
      {
         length = column.getLength();
         if (length != -1)
         {
            attrs[count] = MapConst.ATTR_LENGTH;
            values[count++] = String.valueOf(length);
         }
      }

      if ((type == Types.DECIMAL) || (type == Types.NUMERIC))
      {
         precision = column.getPrecision();
         if (precision != -1)
         {
            attrs[count] = MapConst.ATTR_PRECISION;
            values[count++] = String.valueOf(precision);
         }

         scale = column.getScale();
         if (scale != Integer.MIN_VALUE)
         {
            attrs[count] = MapConst.ATTR_SCALE;
            values[count++] = String.valueOf(scale);
         }
      }

      nullability = column.getNullability();
      if (nullability == DatabaseMetaData.columnNullable)
      {
         attrs[count] = MapConst.ATTR_NULLABLE;
         values[count++] = MapConst.ENUM_YES;
      }
      else if (nullability == DatabaseMetaData.columnNoNulls)
      {
         attrs[count] = MapConst.ATTR_NULLABLE;
         values[count++] = MapConst.ENUM_NO;
      }

      formatter = column.getFormatter();
      if (formatter != null)
      {
         formatName = (String)formatterNames.get(formatter);
         if (formatName != null)
         {
            attrs[count] = MapConst.ATTR_FORMAT;
            values[count++] = formatName;
         }
      }

      writeElementStart(MapConst.ELEM_COLUMN, count, true);
   }

   private void writeDatabases()
      throws IOException
   {
      Enumeration tableEnum;
      Table       table;
      Vector      hashNames = new Vector(), tables = new Vector();
      String[]    hashNamesArray;
      Table[]     tablesArray;
      String      currDatabaseName = null, newDatabaseName = null,
                  currCatalogName = null, newCatalogName = null,
                  currSchemaName = null, newSchemaName = null;
      boolean     databaseChange, catalogChange, schemaChange;
      int         count;

      // Start the Databases element.

      writeElementStart(MapConst.ELEM_DATABASES, 0, false);

      // Get the tables.

      tableEnum = map.getTables();
      while (tableEnum.hasMoreElements())
      {
         table = (Table)tableEnum.nextElement();
         hashNames.addElement(table.getHashName());
         tables.addElement(table);
      }

      // Sort the tables by hash name.

      hashNamesArray = new String[hashNames.size()];
      hashNames.copyInto(hashNamesArray);
      tablesArray = new Table[tables.size()];
      tables.copyInto(tablesArray);
      Sort.sort(hashNamesArray, tablesArray);

      // Process the tables, grouping them by database, catalog, and schema.

      for (int i = 0; i < tablesArray.length; i++)
      {
         // Get the names for the next table.

         table = tablesArray[i];
         newDatabaseName = table.getDatabaseName();
         newCatalogName = table.getCatalogName();
         newSchemaName = table.getSchemaName();

         // Figure out which names have changed.

         databaseChange = false;
         catalogChange = false;
         schemaChange = false;

         if (nameChanged(newDatabaseName, currDatabaseName) || (i == 0))
         {
            databaseChange = true;
            catalogChange = true;
            schemaChange = true;
         }
         else if (nameChanged(newCatalogName, currCatalogName))
         {
            catalogChange = true;
            schemaChange = true;
         }
         else if (nameChanged(newSchemaName, currSchemaName))
         {
            schemaChange = true;
         }

         // If this isn't the first table, close existing elements as needed.

         if (i != 0)
         {
            if (schemaChange)
            {
               writeElementEnd(MapConst.ELEM_SCHEMA);
            }
            if (catalogChange)
            {
               writeElementEnd(MapConst.ELEM_CATALOG);
            }
            if (databaseChange)
            {
               writeElementEnd(MapConst.ELEM_DATABASE);
            }
         }

         // Start new elements as needed.

         if (databaseChange)
         {
            attrs[0] = MapConst.ATTR_NAME;
            values[0] = newDatabaseName;
            writeElementStart(MapConst.ELEM_DATABASE, 1, false);
            currDatabaseName = newDatabaseName;
         }

         if (catalogChange)
         {
            count = 0;
            if (newCatalogName != null)
            {
               attrs[count] = MapConst.ATTR_NAME;
               values[count++] = newCatalogName;
            }
            writeElementStart(MapConst.ELEM_CATALOG, count, false);
            currCatalogName = newCatalogName;
         }

         if (schemaChange)
         {
            count = 0;
            if (newSchemaName != null)
            {
               attrs[count] = MapConst.ATTR_NAME;
               values[count++] = newSchemaName;
            }
            writeElementStart(MapConst.ELEM_SCHEMA, count, false);
            currSchemaName = newSchemaName;
         }

         writeTable(table);
      }

      // Close the Schema, Catalog, Database, and Databases elements

      writeElementEnd(MapConst.ELEM_SCHEMA);
      writeElementEnd(MapConst.ELEM_CATALOG);
      writeElementEnd(MapConst.ELEM_DATABASE);
      writeElementEnd(MapConst.ELEM_DATABASES);
   }

   private void writeDefaultFormatElements()
      throws IOException
   {
      Enumeration     formatters;
      StringFormatter formatter;
      Vector          typeVector;
      String          name;

      // Write the default formatting objects. Remember to check if any of
      // these is also a named format object, in which case we also write
      // the name.

      formatters = defaultFormatters.keys();
      while (formatters.hasMoreElements())
      {
         formatter = (StringFormatter)formatters.nextElement();
         typeVector = (Vector)defaultFormatters.get(formatter);
         name = (String)formatterNames.get(formatter);
         writeFormatElement(formatter, typeVector, name);
      }
   }

   private void writeElementType(XMLName elementTypeName)
      throws IOException, XMLMiddlewareException
   {
      attrs[0] = MapConst.ATTR_NAME;
      values[0] = getQualifiedName(elementTypeName);
      writeElementStart(MapConst.ELEM_ELEMENTTYPE, 1, true);
   }

   private void writeEmptyStringIsNull()
      throws IOException
   {
      if (map.emptyStringIsNull())
      {
         writeElementStart(MapConst.ELEM_EMPTYSTRINGISNULL, 0, true);
      }
   }

   private void writeExtends(ClassMap classMap)
      throws IOException, XMLMiddlewareException
   {
      ClassMap baseClassMap;
      LinkInfo baseLinkInfo;

      baseClassMap = classMap.getBaseClassMap();
      if (baseClassMap == null) return;

      attrs[0] = MapConst.ATTR_ELEMENTTYPE;
      values[0] = getQualifiedName(baseClassMap.getElementTypeName());
      writeElementStart(MapConst.ELEM_EXTENDS, 1, false);

      baseLinkInfo = classMap.getBaseLinkInfo();
      if (baseLinkInfo == null) return;

      attrs[0] = MapConst.ATTR_KEYINBASETABLE;
      values[0] = (baseLinkInfo.parentKeyIsUnique()) ? MapConst.ENUM_UNIQUE : MapConst.ENUM_FOREIGN;
      writeElementStart(MapConst.ELEM_USEBASETABLE, 1, false);

      writeLinkInfo(baseLinkInfo);

      writeElementEnd(MapConst.ELEM_USEBASETABLE);
      writeElementEnd(MapConst.ELEM_EXTENDS);
   }

   private void writeFormatElement(StringFormatter formatter, Vector types, String name)
      throws IOException
   {
      int          count = 0, type;
      StringBuffer sb;
      DateFormat   df;
      NumberFormat nf;

      if (types != null)
      {
         sb = new StringBuffer();
         for (int i = 0; i < types.size(); i++)
         {
            if (i != 0) sb.append(SPACE);
            type = ((Integer)types.elementAt(i)).intValue();
            sb.append(JDBCTypes.getName(type));
         }
         attrs[count] = MapConst.ATTR_DEFAULTFORTYPES;
         values[count++] = sb.toString();
      }

      if (name != null)
      {
         attrs[count] = MapConst.ATTR_NAME;
         values[count++] = name;
      }

      if (formatter instanceof NumberFormatter)
      {
         // If the formatter is an instance of the NumberFormatter class,
         // get the underlying NumberFormat object. If this is a DecimalFormat
         // object, then we can serialize it.

         nf = ((NumberFormatter)formatter).getNumberFormat();
         if (nf instanceof DecimalFormat)
         {
            attrs[count] = MapConst.ATTR_PATTERN;
            values[count++] = ((DecimalFormat)nf).toLocalizedPattern();
            writeElementStart(MapConst.ELEM_DECIMALFORMAT, count, true);
         }
      }
      else if (formatter instanceof DateFormatter)
      {
         // If the formatter is an instance of the DateFormatter class, get
         // the underlying DateFormat object. If this is a SimpleDateFormat
         // object, then we can serialize it.

         df = ((DateFormatter)formatter).getDateFormat();
         if (df instanceof SimpleDateFormat)
         {
            attrs[count] = MapConst.ATTR_PATTERN;
            values[count++] = ((SimpleDateFormat)df).toLocalizedPattern();
            writeElementStart(MapConst.ELEM_SIMPLEDATEFORMAT, count, true);
         }
      }
      else
      {
         // Otherwise, the formatter is an instance of a custom formatting
         // class. In this case, just serialize the class name. Note that this
         // includes the helper classes Base64Formatter, BooleanFormatter, and
         // CharFormatter.

         attrs[count] = MapConst.ATTR_CLASS;
         values[count++] = formatter.getClass().getName();
         writeElementStart(MapConst.ELEM_FORMATCLASS, count, true);
      }
   }

   private void writeFormatElements()
      throws IOException
   {
      buildDefaultFormatters();
      buildFormatterNames();
      writeDefaultFormatElements();
      writeNamedFormatElements();
   }

   private void writeInlineClassMap(InlineClassMap inlineClassMap)
      throws IOException, XMLMiddlewareException
   {
      writeElementStart(MapConst.ELEM_INLINEMAP, 0, false);
      writeElementType(inlineClassMap.getElementTypeName());
      writeOrderInfo(inlineClassMap.getOrderInfo(), false);
      writePropertyInlineRelatedMaps(inlineClassMap);
      writeElementEnd(MapConst.ELEM_INLINEMAP);
   }

   private void writeKey(Key key, String elementTypeName, boolean isForeignKey)
      throws IOException
   {
      Table remoteTable;
      int   count;

      attrs[0] = MapConst.ATTR_NAME;
      values[0] = key.getName();
      writeElementStart(elementTypeName, 1, false);
      if (isForeignKey)
      {
         // Write the <UseTable> element.

         remoteTable = key.getRemoteTable();
         count = setTableNameAttributes(remoteTable);
         writeElementStart(MapConst.ELEM_USETABLE, count, true);

         // Write the <UseUniqueKey> element.

         attrs[0] = MapConst.ATTR_NAME;
         values[0] = key.getRemoteKey().getName();
         writeElementStart(MapConst.ELEM_USEUNIQUEKEY, 1, true);
      }
      writeUseColumns(key.getColumns());
      writeElementEnd(elementTypeName);
   }

   private void writeKeys(Enumeration keys, String elementTypeName, boolean isForeignKey)
      throws IOException
   {
      Key key;
      while (keys.hasMoreElements())
      {
         key = (Key)keys.nextElement();
         writeKey(key, elementTypeName, isForeignKey);
      }
   }

   private void writeLinkInfo(LinkInfo linkInfo)
      throws IOException
   {
      String parentKeyName, childKeyName;

      // Get the names of the parent and child keys.

      parentKeyName = linkInfo.getParentKey().getName();
      childKeyName = linkInfo.getChildKey().getName();

      // Write the <UseUniqueKey> element.

      attrs[0] = MapConst.ATTR_NAME;
      values[0] = linkInfo.parentKeyIsUnique() ? parentKeyName : childKeyName;
      writeElementStart(MapConst.ELEM_USEUNIQUEKEY, 1, true);

      // Write the <UseForeignKey> element.

      values[0] = linkInfo.parentKeyIsUnique() ? childKeyName : parentKeyName;
      writeElementStart(MapConst.ELEM_USEFOREIGNKEY, 1, true);
   }

   private void writeMapEnd()
      throws IOException
   {
      writeElementEnd(MapConst.ELEM_XMLTODBMS);
   }

   private void writeMaps()
      throws IOException, XMLMiddlewareException
   {
      writeElementStart(MapConst.ELEM_MAPS, 0, false);
      writeClassMaps();
      writeElementEnd(MapConst.ELEM_MAPS);
   }

   private void writeMapStart(String systemID, String publicID)
      throws IOException
   {
      attrs[0] = MapConst.ATTR_VERSION;
      values[0] = MapConst.DEF_VERSION;
      attrs[1] = "xmlns";
      values[1] = MapConst.URI_XMLDBMSV2;

      writeXMLDecl();
      writeDOCTYPE(MapConst.ELEM_XMLTODBMS, systemID, publicID);
      writeElementStart(MapConst.ELEM_XMLTODBMS, 2, false);
   }

   private void writeNamedFormatElements()
      throws IOException
   {
      Enumeration     formatters;
      StringFormatter formatter;
      String          name;

      // Write the named format elements. Note that we don't write any
      // named format objects that are also default format objects. This
      // is because they have already been written with writeDefaultFormatElements().

      formatters = formatterNames.keys();
      while (formatters.hasMoreElements())
      {
         formatter = (StringFormatter)formatters.nextElement();
         if (defaultFormatters.get(formatter) != null) continue;
         name = (String)formatterNames.get(formatter);
         writeFormatElement(formatter, null, name);
      }
   }

   private void writeNamespaces()
      throws IOException
   {
      Hashtable   uris;
      Enumeration prefixes;
      String      prefix;

      // If the application specified a particular set of prefixes with
      // setPrefixes(), use these. Otherwise, use the prefixes and URIs in the XMLDBMSMap.

      uris = this.uris;
      if (uris == null)
      {
         uris = map.getNamespaceURIs();
      }

      // Write out the namespaces.

      prefixes = uris.keys();
      while (prefixes.hasMoreElements())
      {
         prefix = (String)prefixes.nextElement();
         attrs[0] = MapConst.ATTR_PREFIX;
         values[0] = prefix;
         attrs[1] = MapConst.ATTR_URI;
         values[1] = (String)uris.get(prefix);
         writeElementStart(MapConst.ELEM_NAMESPACE, 2, true);
      }
   }

   private void writeOptions()
      throws IOException
   {
      writeElementStart(MapConst.ELEM_OPTIONS, 0, false);
      writeEmptyStringIsNull();
      writeFormatElements();
      writeNamespaces();
      writeElementEnd(MapConst.ELEM_OPTIONS);
   }

   private void writeOrderInfo(OrderInfo orderInfo, boolean isTokenList)
      throws IOException
   {
      String name;

      if (orderInfo == null) return;

      attrs[0] = MapConst.ATTR_DIRECTION;
      values[0] = (orderInfo.isAscending()) ? MapConst.ENUM_ASCENDING : MapConst.ENUM_DESCENDING;

      if (orderInfo.orderValueIsFixed())
      {
         attrs[1] = MapConst.ATTR_VALUE;
         values[1] = String.valueOf(orderInfo.getFixedOrderValue());
         writeElementStart(MapConst.ELEM_FIXEDORDER, 2, true);
      }
      else
      {
         attrs[1] = MapConst.ATTR_NAME;
         values[1] = orderInfo.getOrderColumn().getName();
         attrs[2] = MapConst.ATTR_GENERATE;
         values[2] = orderInfo.generateOrder() ? MapConst.ENUM_YES : MapConst.ENUM_NO;
         name = (isTokenList) ? MapConst.ELEM_TLORDERCOLUMN : MapConst.ELEM_ORDERCOLUMN;
         writeElementStart(name, 3, true);
      }
   }

   private void writePrimaryKey(Key key)
      throws IOException
   {
      String keyGeneratorName = null;
      int    count = 0, keyGeneration;

      if (key != null)
      {
         keyGeneration = key.getKeyGeneration();
         if (keyGeneration == Key.DATABASE)
         {
            keyGeneratorName = MapConst.VALUE_DATABASE;
         }
         else if (keyGeneration == Key.KEYGENERATOR)
         {
            keyGeneratorName = key.getKeyGeneratorName();
         }

         if (keyGeneratorName != null)
         {
            attrs[count] = MapConst.ATTR_KEYGENERATOR;
            values[count++] = keyGeneratorName;
         }
         writeElementStart(MapConst.ELEM_PRIMARYKEY, count, false);
         writeUseColumns(key.getColumns());
         writeElementEnd(MapConst.ELEM_PRIMARYKEY);
      }
   }

   private void writePropertyInlineRelatedMaps(ClassMapBase base)
      throws IOException, XMLMiddlewareException
   {
      Enumeration enum;
      Object      o;
      Stack       relatedClassMaps = new Stack(),
                  inlineClassMaps = new Stack();

      // Writes out all property maps, inline class maps, and related class maps

      // First write property maps for attributes, if any.

      enum = base.getAttributeMaps();
      while (enum.hasMoreElements())
      {
         writePropertyMap((PropertyMap)enum.nextElement());
      }

      // Next write property maps for PCDATA, if any.

      writePropertyMap(base.getPCDATAMap());

      // Finally write maps for child elements, if any. Note that these are a
      // mixture of property maps, inline class maps, and related class maps. We
      // write the property maps first, caching the inline class maps and related
      // class maps for later processing.

      enum = base.getChildMaps();
      while (enum.hasMoreElements())
      {
         o = enum.nextElement();
         if (o instanceof PropertyMap)
         {
            writePropertyMap((PropertyMap)o);
         }
         else if (o instanceof RelatedClassMap)
         {
            relatedClassMaps.push(o);
         }
         else // if (o instanceof InlineClassMap)
         {
            inlineClassMaps.push(o);
         }
      }

      // Write the cached inline class maps

      while (!inlineClassMaps.empty())
      {
         writeInlineClassMap((InlineClassMap)inlineClassMaps.pop());
      }

      // Write the cached related class maps

      while (!relatedClassMaps.empty())
      {
         writeRelatedClassMap((RelatedClassMap)relatedClassMaps.pop());
      }
   }

   private void writePropertyMap(PropertyMap propMap)
      throws IOException, XMLMiddlewareException
   {
      int count = 0;

      // If the property map is null, just return.

      if (propMap == null) return;

      // Start the <PropertyMap> element.

      attrs[count] = MapConst.ATTR_TOKENLIST;
      values[count++] = propMap.isTokenList() ? MapConst.ENUM_YES : MapConst.ENUM_NO;
      if (propMap.getType() == PropertyMap.ELEMENTTYPE)
      {
         attrs[count] = MapConst.ATTR_CONTAINSXML;
         values[count++] = propMap.containsXML() ? MapConst.ENUM_YES : MapConst.ENUM_NO;
      }
      writeElementStart(MapConst.ELEM_PROPERTYMAP, count, false);

      // Write the <Attribute>, <PCDATA>, or <ElementType> element.

      switch (propMap.getType())
      {
         case PropertyMap.ATTRIBUTE:
            writeAttribute(propMap.getXMLName());
            break;

         case PropertyMap.PCDATA:
            writeElementStart(MapConst.ELEM_PCDATA, 0, true);
            break;

         case PropertyMap.ELEMENTTYPE:
            writeElementType(propMap.getXMLName());
            break;
      }

      // Write the <ToPropertyTable> (if any), <ToColumn>, and <Order> (if any) elements.

      writeToPropertyTable(propMap);
      writeToColumn(propMap.getColumn());
      writeOrderInfo(propMap.getOrderInfo(), false);
      if (propMap.isTokenList())
      {
         writeOrderInfo(propMap.getTokenListOrderInfo(), true);
      }

      // End the <PropertyMap> element.

      writeElementEnd(MapConst.ELEM_PROPERTYMAP);
   }

   private void writeRelatedClassMap(RelatedClassMap relatedClassMap)
      throws IOException, XMLMiddlewareException
   {
      LinkInfo linkInfo;
      XMLName  mappedElementTypeName, usedElementTypeName;
      ClassMap classMap;
      String   classElementTypeName;

      linkInfo = relatedClassMap.getLinkInfo();

      // Start the <RelatedClass> element.

      attrs[0] = MapConst.ATTR_KEYINPARENTTABLE;
      values[0] = (linkInfo.parentKeyIsUnique()) ? MapConst.ENUM_UNIQUE : MapConst.ENUM_FOREIGN;
      writeElementStart(MapConst.ELEM_RELATEDCLASS, 1, false);

      // Get the name of the element type in the RelatedClassMap and create an
      // <ElementType> element for it.

      mappedElementTypeName = relatedClassMap.getElementTypeName();
      writeElementType(mappedElementTypeName);

      // Compare the element type being mapped with the element type in the class
      // map actually used. If these are different, write a <UseClassMap> element.

      classMap = relatedClassMap.getClassMap();
      usedElementTypeName = classMap.getElementTypeName();
      if (!usedElementTypeName.getUniversalName().equals(mappedElementTypeName.getUniversalName()))
      {
         writeUseClassMap(classMap);
      }

      // Write the <UseUniqueKey> and <UseForeignKey> elements, then the <Order>
      // element, if any.

      writeLinkInfo(linkInfo);
      writeOrderInfo(relatedClassMap.getOrderInfo(), false);

      // End the <RelatedClass> element.

      writeElementEnd(MapConst.ELEM_RELATEDCLASS);
   }

   private void writeTable(Table table)
      throws IOException
   {
      // Start the Table element

      attrs[0] = MapConst.ATTR_NAME;
      values[0] = table.getTableName();
      writeElementStart(MapConst.ELEM_TABLE, 1, false);

      // Write the children of the Table element

      writeColumns(table);
      writePrimaryKey(table.getPrimaryKey());
      writeKeys(table.getUniqueKeys(), MapConst.ELEM_UNIQUEKEY, false);
      writeKeys(table.getForeignKeys(), MapConst.ELEM_FOREIGNKEY, true);

      // End the Table element

      writeElementEnd(MapConst.ELEM_TABLE);
   }

   private void writeToColumn(Column column)
      throws IOException
   {
      attrs[0] = MapConst.ATTR_NAME;
      values[0] = column.getName();
      writeElementStart(MapConst.ELEM_TOCOLUMN, 1, true);
   }

   private void writeToPropertyTable(PropertyMap propMap)
      throws IOException
   {
      Table    table;
      int      count;
      LinkInfo linkInfo;

      // Get the property table. If there isn't one, just return.

      table = propMap.getTable();
      if (table == null) return;

      // Set the table name attributes and the KeyInParentTable attribute, then
      // start the <ToPropertyTable> element.

      count = setTableNameAttributes(table);
      linkInfo = propMap.getLinkInfo();
      attrs[count] = MapConst.ATTR_KEYINPARENTTABLE;
      values[count++] = (linkInfo.parentKeyIsUnique()) ?
                        MapConst.ENUM_UNIQUE : MapConst.ENUM_FOREIGN;
      writeElementStart(MapConst.ELEM_TOPROPERTYTABLE, count, false);

      // Write the <UseUniqueKey> and <UseForeignKey> elements.

      writeLinkInfo(linkInfo);

      // End the <ToPropertyTable> element.

      writeElementEnd(MapConst.ELEM_TOPROPERTYTABLE);
   }

   private void writeUseClassMap(ClassMap classMap)
      throws IOException, XMLMiddlewareException
   {
      attrs[0] = MapConst.ATTR_ELEMENTTYPE;
      values[0] = getQualifiedName(classMap.getElementTypeName());
      writeElementStart(MapConst.ELEM_USECLASSMAP, 1, true);
   }

   private void writeUseColumn(Column column)
      throws IOException
   {
      attrs[0] = MapConst.ATTR_NAME;
      values[0] = column.getName();
      writeElementStart(MapConst.ELEM_USECOLUMN, 1, true);
   }

   private void writeUseColumns(Column[] columns)
      throws IOException
   {
      for (int i = 0; i < columns.length; i++)
      {
         writeUseColumn(columns[i]);
      }
   }

   //**************************************************************************
   // Private methods - utilities
   //**************************************************************************

   private int setTableNameAttributes(Table table)
   {
      String name;
      int    count = 0;

      name = table.getDatabaseName();
      if (!name.equals(MapConst.DEF_DATABASENAME))
      {
         attrs[count] = MapConst.ATTR_DATABASE;
         values[count++] = name;
      }
      name = table.getCatalogName();
      if (name != null)
      {
         attrs[count] = MapConst.ATTR_CATALOG;
         values[count++] = name;
      }
      name = table.getSchemaName();
      if (name != null)
      {
         attrs[count] = MapConst.ATTR_SCHEMA;
         values[count++] = name;
      }
      attrs[count] = MapConst.ATTR_NAME;
      values[count++] = table.getTableName();

      return count;
   }

   private String getQualifiedName(XMLName xmlName)
   {
      // Constructs a qualified name from an xmlName, using the prefixes
      // passed to the MapSerializer, if any.

      if (prefixes != null)
      {
         // If the calling application set prefixes explicitly, use them. Throws
         // an exception if no prefix is found for the URI in the universal name.

         return XMLName.getQualifiedName(xmlName.getUniversalName(), prefixes);
      }
      else
      {
         // If the calling application did not set prefixes, use the prefixes
         // in the map. Throws an exception if prefixes are needed but not set.

         return xmlName.getQualifiedName();
      }
   }

   private boolean nameChanged(String newName, String oldName)
   {
      if (newName == null)
      {
         return (oldName != null);
      }
      return !newName.equals(oldName);
   }

   //**************************************************************************
   // Private methods - formatting stuff
   //**************************************************************************

   private void buildDefaultFormatters()
   {
      Hashtable       formatsByType;
      StringFormatter formatter;
      Enumeration     types;
      Object          type;
      Vector          typeVector;

      // Initialize the defaultFormatters Hashtable.

      defaultFormatters.clear();

      // Get the Hashtable of default formatting objects. These are hashed
      // using JDBC type as a key.

      formatsByType = map.getDefaultFormatters();
      types = formatsByType.keys();

      // Invert the hashtable so that the formatting object is the key.
      // Since more than one JDBC type might use the same formatting
      // object, the stored element is a Vector of JDBC types.

      while (types.hasMoreElements())
      {
         // Get the next type and the corresponding formatting object.
         // Check if the format object can be serialized in a map
         // document. This is possible only for certain classes of
         // formatting objects.

         type = types.nextElement();
         formatter = (StringFormatter)formatsByType.get(type);
         if (formatterSerializable(formatter))
         {
            // Check if the formatting object has already been used as a key.
            // If so, use the existing type Vector. If not, allocate a new type
            // Vector and store it in the Hashtable.

            typeVector = (Vector)defaultFormatters.get(formatter);
            if (typeVector == null)
            {
               typeVector = new Vector();
               defaultFormatters.put(formatter, typeVector);
            }

            // Add the type to the type Vector.

            typeVector.addElement(type);
         }
      }
   }

   private void buildFormatterNames()
   {
      Enumeration     names;
      String          name;
      StringFormatter formatter;

      // Initialize the formatterNames Hashtable.

      formatterNames.clear();

      // Get a list of formatters that have names. Construct a hashtable
      // of those named formatters that are serializable.

      names = map.getNamedFormatters().keys();
      while (names.hasMoreElements())
      {
         name = (String)names.nextElement();
         formatter = map.getNamedFormatter(name);
         if (formatterSerializable(formatter))
         {
            formatterNames.put(formatter, name);
         }
      }
   }

   private boolean formatterSerializable(StringFormatter formatter)
   {
      DateFormat   df;
      NumberFormat nf;

      // Not all formatting objects can be used to construct formatting
      // elements in the map document. This is because some format classes
      // do not expose enough information to construct a corresponding
      // formatting element.
      
      // If the formatting object is an instance of DateFormatter, get the
      // underlying DateFormat object. If this is an instance of SimpleDateFormat,
      // then we can serialize it, although we cannot serialize the locale. If
      // not, we can't.

      if (formatter instanceof DateFormatter)
      {
         df = ((DateFormatter)formatter).getDateFormat();
         return (df instanceof SimpleDateFormat);
      }

      // If the formatting object is an instance of NumberFormatter, get the
      // underlying NumberFormat object. If this is an instance of DecimalFormat,
      // then we can serialize it. If not, we can't.

      if (formatter instanceof NumberFormatter)
      {
         nf = ((NumberFormatter)formatter).getNumberFormat();
         return (nf instanceof DecimalFormat);
      }

      // Otherwise, the formatting object is a custom formatting class, which
      // we can serialize because we can get its name.

      return true;
   }
}
