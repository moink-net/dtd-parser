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
// Changes from version 1.01:
// * Changed to use Writer instead of OutputStream
// * Cleaned up public methods.
// * Modified for new map objects / DTD.

package org.xmlmiddleware.xmldbms.maps.utils;

import org.xmlmiddleware.utils.XMLName;
import org.xmlmiddleware.utils.XMLWriter;
import org.xmlmiddleware.utils.JDBCTypes;

import org.xmlmiddleware.xmldbms.maps.ClassMap;
import org.xmlmiddleware.xmldbms.maps.ClassMapBase;
import org.xmlmiddleware.xmldbms.maps.Column;
import org.xmlmiddleware.xmldbms.maps.InlineClassMap;
import org.xmlmiddleware.xmldbms.maps.Key;
import org.xmlmiddleware.xmldbms.maps.LinkInfo;
import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.MapException;
import org.xmlmiddleware.xmldbms.maps.OrderInfo;
import org.xmlmiddleware.xmldbms.maps.PropertyMap;
import org.xmlmiddleware.xmldbms.maps.RelatedClassMap;
import org.xmlmiddleware.xmldbms.maps.Table;

import org.xmlmiddleware.xmldbms.maps.factories.XMLDBMSConst;

import java.io.IOException;
import java.io.Writer;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * Serializes a Map object to a character stream.
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
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class MapSerializer extends XMLWriter
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private static String XMLDBMS2DTD = "xmldbms2.dtd";

   //**************************************************************************
   // Variables
   //**************************************************************************

   private Map       map;
   private Hashtable uris = null;
   private Hashtable prefixes = null;

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
    *    serialize() uses the prefixes in the Map.
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
    * Serialize a Map using the XML-DBMS mapping language.
    *
    * <p>No system or public ID is written in the DOCTYPE statement.</p>
    *
??    * <p>Note that serialize cannot currently create the Locale element
    * or the values FULL, LONG, MEDIUM, or SHORT in the Date, Time, and
    * Timestamp attributes of the Patterns element.</p>
    *
    * @param map The Map.
    * @exception IOException Thrown if an I/O exception occurs.
    */
   public void serialize(Map map)
      throws IOException, MapException
   {
      this.map = map;
      serialize(null, null);
   }

   /**
    * Serialize a Map using the XML-DBMS mapping language.
    *
??    * <p>Note that serialize cannot currently create the Locale element
    * or the values FULL, LONG, MEDIUM, or SHORT in the Date, Time, and
    * Timestamp attributes of the Patterns element.</p>
    *
    * @param map The Map.
    * @param systemID System ID of the DTD. If this is null, "xmldbms2.dtd" is used.
    * @param publicID Public ID of the DTD. May be null.
    * @exception IOException Thrown if an I/O exception occurs.
    * @exception MapException Thrown if a prefix was not found for a URI.
    */
   public void serialize(Map map, String systemID, String publicID)
      throws IOException, MapException
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
      throws IOException, MapException
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

   private void writeAttribute(XMLName attributeName, boolean multiValued)
      throws IOException, MapException
   {
      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = getQualifiedName(attributeName);
      attrs[1] = XMLDBMSConst.ATTR_MULTIVALUED;
      values[1] = (multiValued) ? XMLDBMSConst.ENUM_YES : XMLDBMSConst.ENUM_NO;
      writeElementStart(XMLDBMSConst.ELEM_ATTRIBUTE, 1, true);
   }

   private void writeClassMap(ClassMap classMap)
      throws IOException, MapException
   {
      ClassMap baseClassMap, useClassMap;
      Table    table;
      int      count;

      // Start the <ClassMap> element.

      writeElementStart(XMLDBMSConst.ELEM_CLASSMAP, 0, false);

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
         writeElementStart(XMLDBMSConst.ELEM_TOCLASSTABLE, count, true);

         // Write the property maps, inline class maps, and related class maps.

         writePropertyInlineRelatedMaps(classMap);
      }

      // End the <ClassMap> element.

      writeElementEnd(XMLDBMSConst.ELEM_CLASSMAP);
   }

   private void writeClassMaps()
      throws IOException, MapException
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
      int    count = 0, type, length, precision, scale, nullability;
      Format format;

      attrs[count] = XMLDBMSConst.ATTR_NAME;
      values[count++] = column.getName();

      type = column.getType();
      if (type != Types.NULL)
      {
         attrs[count] = XMLDBMSConst.ATTR_DATATYPE;
         values[count++] = JDBCTypes.getName(column.getType());
      }

      if (JDBCTypes.typeIsChar(type) || JDBCTypes.typeIsBinary(type))
      {
         length = column.getLength();
         if (length != -1)
         {
            attrs[count] = XMLDBMSConst.ATTR_LENGTH;
            values[count++] = String.valueOf(length);
         }
      }

      if ((type == Types.DECIMAL) || (type == Types.NUMERIC))
      {
         precision = column.getPrecision();
         if (precision != -1)
         {
            attrs[count] = XMLDBMSConst.ATTR_PRECISION;
            values[count++] = String.valueOf(precision);
         }

         scale = column.getScale();
         if (scale != Integer.MIN_VALUE)
         {
            attrs[count] = XMLDBMSConst.ATTR_SCALE;
            values[count++] = String.valueOf(scale);
         }
      }

      nullability = column.getNullability();
      if (nullability == DatabaseMetaData.columnNullable)
      {
         attrs[count] = XMLDBMSConst.ATTR_NULLABLE;
         values[count++] = XMLDBMSConst.ENUM_YES;
      }
      else if (nullability == DatabaseMetaData.columnNoNulls)
      {
         attrs[count] = XMLDBMSConst.ATTR_NULLABLE;
         values[count++] = XMLDBMSConst.ENUM_NO;
      }

      format = column.getFormat();
      if (format != null)
      {
         throw new IllegalStateException("Column format not supported.");
      }

      writeElementStart(XMLDBMSConst.ELEM_COLUMN, count, true);
   }

   private void writeDatabases()
      throws IOException
   {
      Enumeration tableEnum;
      Table       table;
      Vector      universalNames = new Vector(), tables = new Vector();
      String[]    universalNamesArray;
      Table[]     tablesArray;
      String      currDatabaseName = null, newDatabaseName = null,
                  currCatalogName = null, newCatalogName = null,
                  currSchemaName = null, newSchemaName = null;
      boolean     databaseChange, catalogChange, schemaChange;
      int         count;

      // Start the Databases element.

      writeElementStart(XMLDBMSConst.ELEM_DATABASES, 0, false);

      // Get the tables. Note that this is probably
      // broken in some extremely abusive naming cases...

      tableEnum = map.getTables();
      while (tableEnum.hasMoreElements())
      {
         table = (Table)tableEnum.nextElement();
         universalNames.addElement(table.getUniversalName());
         tables.addElement(table);
      }

      // Sort the tables by universal table name.

      universalNamesArray = new String[universalNames.size()];
      universalNames.copyInto(universalNamesArray);
      tablesArray = new Table[tables.size()];
      tables.copyInto(tablesArray);
      sort(universalNamesArray, tablesArray);

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
               writeElementEnd(XMLDBMSConst.ELEM_SCHEMA);
            }
            if (catalogChange)
            {
               writeElementEnd(XMLDBMSConst.ELEM_CATALOG);
            }
            if (databaseChange)
            {
               writeElementEnd(XMLDBMSConst.ELEM_DATABASE);
            }
         }

         // Start new elements as needed.

         if (databaseChange)
         {
            attrs[0] = XMLDBMSConst.ATTR_NAME;
            values[0] = newDatabaseName;
            attrs[1] = XMLDBMSConst.ATTR_QUOTEIDENTIFIERS;
            values[1] = (table.quoteIdentifiers()) ? XMLDBMSConst.ENUM_YES : XMLDBMSConst.ENUM_NO;
            writeElementStart(XMLDBMSConst.ELEM_DATABASE, 2, false);
            currDatabaseName = newDatabaseName;
         }

         if (catalogChange)
         {
            count = 0;
            if (newCatalogName != null)
            {
               attrs[count] = XMLDBMSConst.ATTR_NAME;
               values[count++] = newCatalogName;
            }
            writeElementStart(XMLDBMSConst.ELEM_CATALOG, count, false);
            currCatalogName = newCatalogName;
         }

         if (schemaChange)
         {
            count = 0;
            if (newSchemaName != null)
            {
               attrs[count] = XMLDBMSConst.ATTR_NAME;
               values[count++] = newSchemaName;
            }
            writeElementStart(XMLDBMSConst.ELEM_SCHEMA, count, false);
            currSchemaName = newSchemaName;
         }

         writeTable(table);
      }

      // Close the Schema, Catalog, Database, and Databases elements

      writeElementEnd(XMLDBMSConst.ELEM_SCHEMA);
      writeElementEnd(XMLDBMSConst.ELEM_CATALOG);
      writeElementEnd(XMLDBMSConst.ELEM_DATABASE);
      writeElementEnd(XMLDBMSConst.ELEM_DATABASES);
   }

   private void writeDateTimeFormats(Hashtable hash, String elementName)
      throws IOException
   {
      // Bug! DateFormat does not return the information we need to reconstruct
      // the <DateFormat> element. The best we can do is check if the DateFormat
      // is a SimpleDateFormat and, if so, use the pattern from that.

      Enumeration formatNames;
      String      formatName;
      Object      format;

      formatNames = hash.keys();

      while (formatNames.hasMoreElements())
      {
         formatName = (String)formatNames.nextElement();
         if (!formatName.equals("Default"))
         {
            format = hash.get(formatName);
            if (format instanceof SimpleDateFormat)
            {
               writeFormatElement(elementName, formatName, (SimpleDateFormat)format);
            }
         }
      }
   }

   private void writeElementType(XMLName elementTypeName)
      throws IOException, MapException
   {
      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = getQualifiedName(elementTypeName);
      writeElementStart(XMLDBMSConst.ELEM_ELEMENTTYPE, 1, true);
   }

   private void writeEmptyStringIsNull()
      throws IOException
   {
      if (map.emptyStringIsNull())
      {
         writeElementStart(XMLDBMSConst.ELEM_EMPTYSTRINGISNULL, 0, true);
      }
   }

   private void writeExtends(ClassMap classMap)
      throws IOException, MapException
   {
      ClassMap baseClassMap;
      LinkInfo baseLinkInfo;

      baseClassMap = classMap.getBaseClassMap();
      if (baseClassMap == null) return;

      attrs[0] = XMLDBMSConst.ATTR_ELEMENTTYPE;
      values[0] = getQualifiedName(baseClassMap.getElementTypeName());
      writeElementStart(XMLDBMSConst.ELEM_EXTENDS, 1, false);

      baseLinkInfo = classMap.getBaseLinkInfo();
      if (baseLinkInfo == null) return;

      attrs[0] = XMLDBMSConst.ATTR_KEYINBASETABLE;
      values[0] = (baseLinkInfo.parentKeyIsUnique()) ? XMLDBMSConst.ENUM_UNIQUE : XMLDBMSConst.ENUM_FOREIGN;
      writeElementStart(XMLDBMSConst.ELEM_USEBASETABLE, 1, false);

      writeLinkInfo(baseLinkInfo);

      writeElementEnd(XMLDBMSConst.ELEM_USEBASETABLE);
      writeElementEnd(XMLDBMSConst.ELEM_EXTENDS);
   }

   private void writeFormatElement(String elementName, String formatName, SimpleDateFormat format)
      throws IOException
   {
      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = formatName;
      writeElementStart(elementName, 1, false);
      attrs[0] = XMLDBMSConst.ATTR_VALUE;
      values[0] = format.toPattern();
      writeElementStart(XMLDBMSConst.ELEM_PATTERN, 1, true);
      writeElementEnd(elementName);
   }

   private void writeInlineClassMap(InlineClassMap inlineClassMap)
      throws IOException, MapException
   {
      writeElementStart(XMLDBMSConst.ELEM_INLINEMAP, 0, false);
      writeElementType(inlineClassMap.getElementTypeName());
      writePropertyInlineRelatedMaps(inlineClassMap);
      writeOrder(inlineClassMap.getOrderInfo());
      writeElementEnd(XMLDBMSConst.ELEM_INLINEMAP);
   }

   private void writeKey(Key key, String elementTypeName, boolean isForeignKey)
      throws IOException
   {
      Table remoteTable;
      int   count;

      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = key.getName();
      writeElementStart(elementTypeName, 1, false);
      if (isForeignKey)
      {
         // Write the <UseTable> element.

         remoteTable = key.getRemoteTable();
         count = setTableNameAttributes(remoteTable);
         writeElementStart(XMLDBMSConst.ELEM_USETABLE, count, true);

         // Write the <UseUniqueKey> element.

         attrs[0] = XMLDBMSConst.ATTR_NAME;
         values[0] = key.getRemoteKey().getName();
         writeElementStart(XMLDBMSConst.ELEM_USEUNIQUEKEY, 1, true);
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

      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = linkInfo.parentKeyIsUnique() ? parentKeyName : childKeyName;
      writeElementStart(XMLDBMSConst.ELEM_USEUNIQUEKEY, 1, true);

      // Write the <UseForeignKey> element.

      values[0] = linkInfo.parentKeyIsUnique() ? childKeyName : parentKeyName;
      writeElementStart(XMLDBMSConst.ELEM_USEFOREIGNKEY, 1, true);
   }


   private void writeMapEnd()
      throws IOException
   {
      writeElementEnd(XMLDBMSConst.ELEM_XMLTODBMS);
   }

   private void writeMaps()
      throws IOException, MapException
   {
      writeElementStart(XMLDBMSConst.ELEM_MAPS, 0, false);
      writeClassMaps();
      writeElementEnd(XMLDBMSConst.ELEM_MAPS);
   }

   private void writeMapStart(String systemID, String publicID)
      throws IOException
   {
      attrs[0] = XMLDBMSConst.ATTR_VERSION;
      values[0] = XMLDBMSConst.DEF_VERSION;
      attrs[1] = "xmlns";
      values[1] = XMLDBMSConst.URI_XMLDBMSV2;

      writeXMLDecl();
      writeDOCTYPE(XMLDBMSConst.ELEM_XMLTODBMS, systemID, publicID);
      writeElementStart(XMLDBMSConst.ELEM_XMLTODBMS, 2, false);
   }

   private void writeNamespaces()
      throws IOException
   {
      Hashtable   uris;
      Enumeration prefixes;
      String      prefix;

      // If the application specified a particular set of prefixes with
      // setPrefixes(), use these. Otherwise, use the prefixes and URIs in the Map.

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
         attrs[0] = XMLDBMSConst.ATTR_PREFIX;
         values[0] = prefix;
         attrs[1] = XMLDBMSConst.ATTR_URI;
         values[1] = (String)uris.get(prefix);
         writeElementStart(XMLDBMSConst.ELEM_NAMESPACE, 2, true);
      }
   }

   private void writeNumberFormats()
      throws IOException
   {
      Hashtable     numberFormats;
      Enumeration   formatNames;
      String        formatName;
      DecimalFormat format;

      numberFormats = map.getNumberFormats();
      formatNames = numberFormats.keys();

      while (formatNames.hasMoreElements())
      {
         formatName = (String)formatNames.nextElement();
         if (!formatName.equals("Default"))
         {
            format = (DecimalFormat)numberFormats.get(formatName);
            attrs[0] = XMLDBMSConst.ATTR_NAME;
            values[0] = formatName;
            writeElementStart(XMLDBMSConst.ELEM_NUMBERFORMAT, 1, false);
            attrs[0] = XMLDBMSConst.ATTR_VALUE;
            values[0] = format.toPattern();
            writeElementStart(XMLDBMSConst.ELEM_PATTERN, 1, true);
            writeElementEnd(XMLDBMSConst.ELEM_NUMBERFORMAT);
         }
      }
   }

   private void writeOptions()
      throws IOException
   {
      writeElementStart(XMLDBMSConst.ELEM_OPTIONS, 0, false);
      writeEmptyStringIsNull();
      writeDateTimeFormats(map.getDateFormats(), XMLDBMSConst.ELEM_DATEFORMAT);
      writeDateTimeFormats(map.getTimeFormats(), XMLDBMSConst.ELEM_TIMEFORMAT);
      writeDateTimeFormats(map.getDateTimeFormats(), XMLDBMSConst.ELEM_DATETIMEFORMAT);
      writeNumberFormats();
      writeNamespaces();
      writeElementEnd(XMLDBMSConst.ELEM_OPTIONS);
   }

   private void writeOrder(OrderInfo orderInfo)
      throws IOException
   {
      if (orderInfo == null) return;

      attrs[0] = XMLDBMSConst.ATTR_DIRECTION;
      values[0] = (orderInfo.isAscending()) ? XMLDBMSConst.ENUM_ASCENDING : XMLDBMSConst.ENUM_DESCENDING;
      writeElementStart(XMLDBMSConst.ELEM_ORDER, 1, false);

      if (orderInfo.orderValueIsFixed())
      {
         attrs[0] = XMLDBMSConst.ATTR_VALUE;
         values[0] = String.valueOf(orderInfo.getFixedOrderValue());
         writeElementStart(XMLDBMSConst.ELEM_FIXEDORDER, 1, true);
      }
      else
      {
         writeUseColumn(orderInfo.getOrderColumn());
         if (orderInfo.generateOrder())
         {
            writeElementStart(XMLDBMSConst.ELEM_GENERATE, 0, true);
         }
      }

      writeElementEnd(XMLDBMSConst.ELEM_ORDER);
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
            keyGeneratorName = XMLDBMSConst.VALUE_DATABASE;
         }
         else if (keyGeneration == Key.XMLDBMS)
         {
            keyGeneratorName = key.getKeyGeneratorName();
         }

         if (keyGeneratorName != null)
         {
            attrs[count] = XMLDBMSConst.ATTR_KEYGENERATOR;
            values[count++] = keyGeneratorName;
         }
         writeElementStart(XMLDBMSConst.ELEM_PRIMARYKEY, count, false);
         writeUseColumns(key.getColumns());
         writeElementEnd(XMLDBMSConst.ELEM_PRIMARYKEY);
      }
   }

   private void writePropertyInlineRelatedMaps(ClassMapBase base)
      throws IOException, MapException
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
      throws IOException, MapException
   {
      // If the property map is null, just return.

      if (propMap == null) return;

      // Start the <PropertyMap> element.

      writeElementStart(XMLDBMSConst.ELEM_PROPERTYMAP, 0, false);

      // Write the <Attribute>, <PCDATA>, or <ElementType> element.

      switch (propMap.getType())
      {
         case PropertyMap.ATTRIBUTE:
            writeAttribute(propMap.getXMLName(), propMap.attributeIsMultiValued());
            break;

         case PropertyMap.PCDATA:
            writeElementStart(XMLDBMSConst.ELEM_PCDATA, 0, true);
            break;

         case PropertyMap.ELEMENTTYPE:
            writeElementType(propMap.getXMLName());
            break;
      }

      // Write the <ToPropertyTable> (if any), <ToColumn>, and <Order> (if any) elements.

      writeToPropertyTable(propMap);
      writeToColumn(propMap.getColumn());
      writeOrder(propMap.getOrderInfo());

      // End the <PropertyMap> element.

      writeElementEnd(XMLDBMSConst.ELEM_PROPERTYMAP);
   }

   private void writeRelatedClassMap(RelatedClassMap relatedClassMap)
      throws IOException, MapException
   {
      LinkInfo linkInfo;
      XMLName  mappedElementTypeName, usedElementTypeName;
      ClassMap classMap;
      String   classElementTypeName;

      linkInfo = relatedClassMap.getLinkInfo();

      // Start the <RelatedClass> element.

      attrs[0] = XMLDBMSConst.ATTR_KEYINPARENTTABLE;
      values[0] = (linkInfo.parentKeyIsUnique()) ? XMLDBMSConst.ENUM_UNIQUE : XMLDBMSConst.ENUM_FOREIGN;
      writeElementStart(XMLDBMSConst.ELEM_RELATEDCLASS, 1, false);

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
      writeOrder(relatedClassMap.getOrderInfo());

      // End the <RelatedClass> element.

      writeElementEnd(XMLDBMSConst.ELEM_RELATEDCLASS);
   }

   private void writeTable(Table table)
      throws IOException
   {
      // Start the Table element

      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = table.getTableName();
      writeElementStart(XMLDBMSConst.ELEM_TABLE, 1, false);

      // Write the children of the Table element

      writeColumns(table);
      writePrimaryKey(table.getPrimaryKey());
      writeKeys(table.getUniqueKeys(), XMLDBMSConst.ELEM_UNIQUEKEY, false);
      writeKeys(table.getForeignKeys(), XMLDBMSConst.ELEM_FOREIGNKEY, true);

      // End the Table element

      writeElementEnd(XMLDBMSConst.ELEM_TABLE);
   }

   private void writeToColumn(Column column)
      throws IOException
   {
      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = column.getName();
      writeElementStart(XMLDBMSConst.ELEM_TOCOLUMN, 1, true);
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
      attrs[count] = XMLDBMSConst.ATTR_KEYINPARENTTABLE;
      values[count++] = (linkInfo.parentKeyIsUnique()) ?
                        XMLDBMSConst.ENUM_UNIQUE : XMLDBMSConst.ENUM_FOREIGN;
      writeElementStart(XMLDBMSConst.ELEM_TOPROPERTYTABLE, count, false);

      // Write the <UseUniqueKey> and <UseForeignKey> elements.

      writeLinkInfo(linkInfo);

      // End the <ToPropertyTable> element.

      writeElementEnd(XMLDBMSConst.ELEM_TOPROPERTYTABLE);
   }

   private void writeUseClassMap(ClassMap classMap)
      throws IOException, MapException
   {
      attrs[0] = XMLDBMSConst.ATTR_ELEMENTTYPE;
      values[0] = getQualifiedName(classMap.getElementTypeName());
      writeElementStart(XMLDBMSConst.ELEM_USECLASSMAP, 1, true);
   }

   private void writeUseColumn(Column column)
      throws IOException
   {
      attrs[0] = XMLDBMSConst.ATTR_NAME;
      values[0] = column.getName();
      writeElementStart(XMLDBMSConst.ELEM_USECOLUMN, 1, true);
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
      if (!name.equals(XMLDBMSConst.DEF_DATABASENAME))
      {
         attrs[count] = XMLDBMSConst.ATTR_DATABASE;
         values[count++] = name;
      }
      name = table.getCatalogName();
      if (name != null)
      {
         attrs[count] = XMLDBMSConst.ATTR_CATALOG;
         values[count++] = name;
      }
      name = table.getSchemaName();
      if (name != null)
      {
         attrs[count] = XMLDBMSConst.ATTR_SCHEMA;
         values[count++] = name;
      }
      attrs[count] = XMLDBMSConst.ATTR_NAME;
      values[count++] = table.getTableName();

      return count;
   }

   private String getQualifiedName(XMLName xmlName)
      throws MapException
   {
      // Constructs a qualified name from an xmlName, using the prefixes
      // passed to the MapSerializer, if any.

      try
      {
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
      catch (Exception e)
      {
         throw new MapException(e.getMessage());
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

   private void sort(String[] keys, Object[] values)
   {
      String s;
      Object o;

      // Sort a Vector of values by the specified (String) keys.

      for (int i = 0; i < keys.length; i++)
      {
         for (int j = i + 1; j < keys.length; j++)
         { 
            if (keys[i].compareTo(keys[j]) > 0)
            {
               s = keys[i];
               o = values[i];
               keys[i] = keys[j];
               values[i] = values[j];
               keys[j] = s;
               values[j] = o;
            }
         }
      }
   }
}
