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
// * Modified getAttrValue to return default attribute values.
// Changes from version 1.01:
// * Update for version 2.0 DTD.
// * Delete connection and result set information.
// * Simplified API.

package org.xmlmiddleware.xmldbms.maps.factories;

import org.xmlmiddleware.db.*;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;
import org.xmlmiddleware.xmlutils.*;

import org.xml.sax.*;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

/**
 * Compile a map document into an XMLDBMSMap object.
 *
 * <p>MapCompiler assumes that the mapping document is valid. If
 * it is not, the class will either generate garbage or throw an exception. One
 * way to guarantee that the document is valid is to pass a validating parser
 * to the map compiler.</p>
 * 
 * <p>For example, the following code creates a map from the sales.map
 * mapping document.</p>
 *
 * <pre>
 *    // Instantiate a new map compiler and set the XMLReader.
 *    compiler = new MapCompiler(xmlReader);<br />
 *
 *    // Compile sales.map into an XMLDBMSMap object.
 *    map = compiler.compile(new InputSource(new FileReader("sales.map")));<br />
 * </pre>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class MapCompiler
   implements ContentHandler
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   // The state gives context for elements that can occur inside more than
   // one element. For example, we need state to determine whether an
   // <ElementType> element occurs directly beneath a <ClassMap>, <PropertyMap>,
   // <RelatedClass>, or <InlineMap> element.
   //
   // The state hierarchy is as follows:
   //
   //    STATE_NONE
   //       STATE_KEY
   //       STATE_CLASSMAP
   //          STATE_USEBASETABLE
   //          STATE_PROPERTYMAP
   //             STATE_TOPROPERTYTABLE
   //          STATE_RELATEDCLASS
   //          STATE_INLINECLASS
   //             STATE_PROPERTYMAP
   //             STATE_RELATEDCLASS
   //             STATE_INLINECLASS...
   //
   // The following table shows which states affect the processing of which element types:
   //
   // State                  Affected element types
   // --------------         -----------------------------------------------------
   // STATE_KEY              UseUniqueKey
   // STATE_CLASSMAP         ElementType, PropertyMap, InlineMap, RelatedClass, UseClassMap
   // STATE_USEBASETABLE     UseUniqueKey, UseForeignKey
   // STATE_PROPERTYMAP      ElementType, OrderColumn, FixedOrder
   // STATE_RELATEDCLASS     ElementType, OrderColumn, FixedOrder, UseUniqueKey,
   //                        UseForeignKey, UseClassMap
   // STATE_INLINECLASS      ElementType, PropertyMap, InlineMap, RelatedClass,
   //                        OrderColumn, FixedOrder
   // STATE_TOPROPERTYTABLE  UseUniqueKey, UseForeignKey


   // State integers (for switch statements)
   private static final int iSTATE_NONE            = 0;
   private static final int iSTATE_KEY             = 1;
   private static final int iSTATE_CLASSMAP        = 2;
   private static final int iSTATE_USEBASETABLE    = 3;
   private static final int iSTATE_PROPERTYMAP     = 4;
   private static final int iSTATE_RELATEDCLASS    = 5;
   private static final int iSTATE_INLINECLASS     = 6;
   private static final int iSTATE_TOPROPERTYTABLE = 7;

   // State objects (for state variable and stateStack)
   private static final Integer STATE_NONE            = new Integer(iSTATE_NONE);
   private static final Integer STATE_KEY             = new Integer(iSTATE_KEY);
   private static final Integer STATE_CLASSMAP        = new Integer(iSTATE_CLASSMAP);
   private static final Integer STATE_USEBASETABLE    = new Integer(iSTATE_USEBASETABLE);
   private static final Integer STATE_PROPERTYMAP     = new Integer(iSTATE_PROPERTYMAP);
   private static final Integer STATE_RELATEDCLASS    = new Integer(iSTATE_RELATEDCLASS);
   private static final Integer STATE_INLINECLASS     = new Integer(iSTATE_INLINECLASS);
   private static final Integer STATE_TOPROPERTYTABLE = new Integer(iSTATE_TOPROPERTYTABLE);

   private static String NAMESPACES = "http://xml.org/sax/features/namespaces";

   //**************************************************************************
   // Variables
   //**************************************************************************

   // General class variables
   private XMLReader       xmlReader = null;
   private TokenList       elementTokens, enumTokens;
   private Integer         state;
   private Stack           stateStack = new Stack();
   private Stack           fkWrapperStack = new Stack();
   private Stack           baseTableWrapperStack = new Stack();
   private Stack           rcmWrapperStack = new Stack();
   private Stack           inlineClassMapStack = new Stack();
   private Vector          keyColumns = new Vector();
   private Hashtable       propTables = new Hashtable();
   private Hashtable       classTables = new Hashtable();
   private Hashtable       formatTypes = new Hashtable();

   // State variables -- map
   private XMLDBMSMap       map;

   // State variables -- databases
   private String           databaseName, catalogName, schemaName;
   private Table            table;
   private Key              key;

   // State variables -- options
   private Locale           locale;
   private String           pattern, formatName, defaultForTypes;
   private int              dateStyle, timeStyle;

   // State variables -- class maps
   private XMLName          elementTypeName;
   private ClassMap         classMap;

   // State variables -- property maps
   private PropertyMap      propMap;
   private Table            propertyTable;
   private boolean          parentKeyIsUnique, isTokenList, containsXML;
   private Key              uniqueKey;

   // State variables -- base class tables
   private BaseTableWrapper baseTableWrapper;

   // State variables -- related class maps
   private RCMWrapper       rcmWrapper;

   // State variables -- inline class maps
   private InlineClassMap   inlineClassMap;

   // Debugging variables
   private int              indent = 0;
   private boolean          debug = false;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new MapCompiler and set the SAX XMLReader (parser).
    */
   public MapCompiler(XMLReader xmlReader)
      throws SAXException
   {
      if (xmlReader == null)
         throw new IllegalArgumentException("xmlReader argument must not be null.");
      this.xmlReader = xmlReader;
      this.xmlReader.setContentHandler(this);
      this.xmlReader.setFeature(NAMESPACES, true);
      init();
   }

   //**************************************************************************
   // Public methods -- map compiler
   //**************************************************************************

   /**
    * Compile a map document into an XMLDBMSMap object.
    *
    * @param src A SAX InputSource for the mapping document.
    * @return The XMLDBMSMap object
    * @exception XMLMiddlewareException Thrown if the mapping document contains an error.
    */
   public XMLDBMSMap compile(InputSource src)
      throws XMLMiddlewareException
   {
      Exception e;
      MapInverter inverter;

      // Check the arguments.

      if (src == null)
         throw new IllegalArgumentException("src argument must not be null.");

      // Parse the map document. Rethrow any exceptions as XMLMiddlewareExceptions.

      try
      {
         xmlReader.parse(src);
      }
      catch (SAXException s)
      {
         // Get the embedded Exception (if any) and check if it's a XMLMiddlewareException.
         e = s.getException();
         if (e != null)
         {
            if (e instanceof XMLMiddlewareException)
               throw (XMLMiddlewareException)e;
            else
               throw new XMLMiddlewareException(e);
         }
         else
            throw new XMLMiddlewareException(s);
      }
      catch (IOException io)
      {
         throw new XMLMiddlewareException(io);
      }

      // Create the database-centric view of the map.

      inverter = new MapInverter();
      inverter.createDatabaseView(map);

      // Return the XMLDBMSMap object.

      return map;
   }

   //**************************************************************************
   // org.xml.sax.ContentHandler methods
   //**************************************************************************

   /** For internal use only. */
   public void startDocument () throws SAXException
   {
      if (debug)
      {
         System.out.println("Document started.");
      }

      // Initialize global variables.
      state = STATE_NONE;
      stateStack.removeAllElements();
      fkWrapperStack.removeAllElements();
      baseTableWrapperStack.removeAllElements();
      rcmWrapperStack.removeAllElements();
      inlineClassMapStack.removeAllElements();
      propTables.clear();
      classTables.clear();
      formatTypes.clear();

      map = new XMLDBMSMap();
      inlineClassMap = null;
   }

   /** For internal use only. */
   public void endDocument() throws SAXException
   {
      try
      {
        resolveRCMWrappers();
        resolveBaseTableWrappers();
      }
      catch (XMLMiddlewareException m)
      {
         throw new SAXException(m);
      }

      if (debug)
      {
         System.out.println("Document ended.");
      }
   }

   /** For internal use only. */
   public void startElement (String uri, String localName, String qName, Attributes attrs)
     throws SAXException
   {
      
      // Debugging code.
      if (debug)
      {
         indent();
         System.out.println(localName + " (start)");
         indent += 3;
      }

      try
      {
         if (!uri.equals(MapConst.URI_XMLDBMSV2))
            throw new XMLMiddlewareException("Unrecognized namespace URI: " + uri);

         switch (elementTokens.getToken(localName))
         {
            case MapConst.ELEM_TOKEN_ATTRIBUTE:
               processAttribute(attrs);
               break;

            case MapConst.ELEM_TOKEN_CATALOG:
               processCatalog(attrs);
               break;

            case MapConst.ELEM_TOKEN_CLASSMAP:
               // Just set the state. We create the ClassMap in processElementType().
               stateStack.push(state);
               state = STATE_CLASSMAP;
               break;

            case MapConst.ELEM_TOKEN_COLUMN:
               processColumn(attrs);
               break;

            case MapConst.ELEM_TOKEN_DATABASE:
               processDatabase(attrs);
               break;

            case MapConst.ELEM_TOKEN_DATABASES:
               // Nothing to do.
               break;

            case MapConst.ELEM_TOKEN_DATEFORMAT:
               processDateFormatStart(attrs, true, false);
               break;

            case MapConst.ELEM_TOKEN_DATETIMEFORMAT:
               processDateFormatStart(attrs, true, true);
               break;

            case MapConst.ELEM_TOKEN_DECIMALFORMAT:
               processDecimalFormatStart(attrs);
               break;

            case MapConst.ELEM_TOKEN_ELEMENTTYPE:
               processElementType(attrs);
               break;

            case MapConst.ELEM_TOKEN_EMPTYSTRINGISNULL:
               processEmptyStringIsNull();
               break;

            case MapConst.ELEM_TOKEN_EXTENDS:
               processExtends(attrs);
               break;

            case MapConst.ELEM_TOKEN_FIXEDORDER:
               processFixedOrder(attrs);
               break;

            case MapConst.ELEM_TOKEN_FOREIGNKEY:
               stateStack.push(state);
               state = STATE_KEY;
               processForeignKey(attrs);
               break;

            case MapConst.ELEM_TOKEN_FORMATCLASS:
               processFormatClass(attrs);
               break;

            case MapConst.ELEM_TOKEN_INLINEMAP:
               // Set the state and push the current inlineClassMap onto the stack. We
               // set the new inlineClassMap when we process the <ElementType> element.
               stateStack.push(state);
               state = STATE_INLINECLASS;
               inlineClassMapStack.push(inlineClassMap);
               break;

            case MapConst.ELEM_TOKEN_LOCALE:
               processLocale(attrs);
               break;

            case MapConst.ELEM_TOKEN_NAMESPACE:
               processNamespace(attrs);
               break;

            case MapConst.ELEM_TOKEN_NUMBERFORMAT:
               processFormatStart(attrs);
               break;

            case MapConst.ELEM_TOKEN_ORDERCOLUMN:
               processOrderColumn(attrs, false);
               break;

            case MapConst.ELEM_TOKEN_PCDATA:
               processPCDATA();
               break;

            case MapConst.ELEM_TOKEN_PRIMARYKEY:
               stateStack.push(state);
               state = STATE_KEY;
               processPrimaryKey(attrs);
               break;

            case MapConst.ELEM_TOKEN_PROPERTYMAP:
               processPropertyMap(attrs);
               stateStack.push(state);
               state = STATE_PROPERTYMAP;
               break;

            case MapConst.ELEM_TOKEN_RELATEDCLASS:
               // Set the state. We create the relatedClassMap when we encounter
               // the <ElementType> element.
               stateStack.push(state);
               state = STATE_RELATEDCLASS;
               processRelatedClass(attrs);
               break;

            case MapConst.ELEM_TOKEN_SCHEMA:
               processSchema(attrs);
               break;

            case MapConst.ELEM_TOKEN_SIMPLEDATEFORMAT:
               processSimpleDateFormatStart(attrs);
               break;

            case MapConst.ELEM_TOKEN_TABLE:
               processTableStart(attrs);
               break;

            case MapConst.ELEM_TOKEN_TIMEFORMAT:
               processDateFormatStart(attrs, false, true);
               break;

            case MapConst.ELEM_TOKEN_TLORDERCOLUMN:
               processOrderColumn(attrs, true);
               break;

            case MapConst.ELEM_TOKEN_TOCLASSTABLE:
               processToClassTable(attrs);
               break;

            case MapConst.ELEM_TOKEN_TOCOLUMN:
               processToColumn(attrs);
               break;

            case MapConst.ELEM_TOKEN_TOPROPERTYTABLE:
               processToPropertyTable(attrs);
               stateStack.push(state);
               state = STATE_TOPROPERTYTABLE;
               break;

            case MapConst.ELEM_TOKEN_UNIQUEKEY:
               stateStack.push(state);
               state = STATE_KEY;
               processUniqueKey(attrs);
               break;

            case MapConst.ELEM_TOKEN_USEBASETABLE:
               processUseBaseTable(attrs);
               stateStack.push(state);
               state = STATE_USEBASETABLE;
               break;

            case MapConst.ELEM_TOKEN_USECLASSMAP:
               processUseClassMap(attrs);
               break;

            case MapConst.ELEM_TOKEN_USECOLUMN:
               processUseColumn(attrs);
               break;

            case MapConst.ELEM_TOKEN_USEFOREIGNKEY:
               processUseForeignKey(attrs);
               break;

            case MapConst.ELEM_TOKEN_USETABLE:
               processUseTable(attrs);
               break;

            case MapConst.ELEM_TOKEN_USEUNIQUEKEY:
               processUseUniqueKey(attrs);
               break;

            case MapConst.ELEM_TOKEN_XMLTODBMS:
               processXMLToDBMS(attrs);
               break;

            case MapConst.ELEM_TOKEN_MAPS:
            case MapConst.ELEM_TOKEN_OPTIONS:
               // Nothing to do.
               break;

            case MapConst.ELEM_TOKEN_INVALID:
               throw new XMLMiddlewareException("Unrecognized XML-DBMS mapping language element type: " + localName);
         }
      }
      catch (XMLMiddlewareException m)
      {
         throw new SAXException(m);
      }
   }

   /** For internal use only. */
   public void endElement (String uri, String localName, String qName) throws SAXException
   {
      // Debugging code.
      if (debug)
      {
         indent -= 3;
         indent();
         System.out.println(localName + " (end)");
      }

      try
      {
         if (!uri.equals(MapConst.URI_XMLDBMSV2))
            throw new XMLMiddlewareException("Unrecognized namespace URI: " + uri);

         switch (elementTokens.getToken(localName))
         {
            case MapConst.ELEM_TOKEN_DATABASES:
               resolveFKWrappers();
               break;

            case MapConst.ELEM_TOKEN_DATEFORMAT:
               processDateFormatEnd();
               break;

            case MapConst.ELEM_TOKEN_DATETIMEFORMAT:
               processDateTimeFormatEnd();
               break;

            case MapConst.ELEM_TOKEN_DECIMALFORMAT:
               processDecimalFormatEnd();
               break;

            case MapConst.ELEM_TOKEN_FOREIGNKEY:
               processKeyEnd();
               state = (Integer)stateStack.pop();
               break;

            case MapConst.ELEM_TOKEN_INLINEMAP:
               state = (Integer)stateStack.pop();
               inlineClassMap = (InlineClassMap)inlineClassMapStack.pop();
               break;

            case MapConst.ELEM_TOKEN_NUMBERFORMAT:
               processNumberFormatEnd();
               break;

            case MapConst.ELEM_TOKEN_PRIMARYKEY:
               processKeyEnd();
               state = (Integer)stateStack.pop();
               break;

            case MapConst.ELEM_TOKEN_SIMPLEDATEFORMAT:
               processSimpleDateFormatEnd();
               break;

            case MapConst.ELEM_TOKEN_TABLE:
               processTableEnd();
               break;

            case MapConst.ELEM_TOKEN_TIMEFORMAT:
               processTimeFormatEnd();
               break;

            case MapConst.ELEM_TOKEN_UNIQUEKEY:
               processKeyEnd();
               state = (Integer)stateStack.pop();
               break;

            case MapConst.ELEM_TOKEN_CLASSMAP:
            case MapConst.ELEM_TOKEN_PROPERTYMAP:
            case MapConst.ELEM_TOKEN_RELATEDCLASS:
            case MapConst.ELEM_TOKEN_TOPROPERTYTABLE:
            case MapConst.ELEM_TOKEN_USEBASETABLE:
               // Only need to pop the state.
               state = (Integer)stateStack.pop();
               break;

            case MapConst.ELEM_TOKEN_ATTRIBUTE:
            case MapConst.ELEM_TOKEN_CATALOG:
            case MapConst.ELEM_TOKEN_COLUMN:
            case MapConst.ELEM_TOKEN_DATABASE:
            case MapConst.ELEM_TOKEN_ELEMENTTYPE:
            case MapConst.ELEM_TOKEN_EMPTYSTRINGISNULL:
            case MapConst.ELEM_TOKEN_EXTENDS:
            case MapConst.ELEM_TOKEN_FIXEDORDER:
            case MapConst.ELEM_TOKEN_FORMATCLASS:
            case MapConst.ELEM_TOKEN_LOCALE:
            case MapConst.ELEM_TOKEN_MAPS:
            case MapConst.ELEM_TOKEN_NAMESPACE:
            case MapConst.ELEM_TOKEN_OPTIONS:
            case MapConst.ELEM_TOKEN_ORDERCOLUMN:
            case MapConst.ELEM_TOKEN_PCDATA:
            case MapConst.ELEM_TOKEN_SCHEMA:
            case MapConst.ELEM_TOKEN_TLORDERCOLUMN:
            case MapConst.ELEM_TOKEN_TOCLASSTABLE:
            case MapConst.ELEM_TOKEN_TOCOLUMN:
            case MapConst.ELEM_TOKEN_USECLASSMAP:
            case MapConst.ELEM_TOKEN_USECOLUMN:
            case MapConst.ELEM_TOKEN_USEFOREIGNKEY:
            case MapConst.ELEM_TOKEN_USETABLE:
            case MapConst.ELEM_TOKEN_USEUNIQUEKEY:
            case MapConst.ELEM_TOKEN_XMLTODBMS:
               // Nothing to do.
               break;

            case MapConst.ELEM_TOKEN_INVALID:
               throw new XMLMiddlewareException("Unrecognized XML-DBMS mapping language element type: " + localName);
         }
      }
      catch (XMLMiddlewareException m)
      {
         throw new SAXException(m);
      }
   }

   /** For internal use only. */
   public void characters (char ch[], int start, int length)
     throws SAXException
   {
      // No XML-DBMS elements have character content, so the document is
      // invalid if any is found (other than whitespace). Since we assume
      // that the document is valid, don't throw an error.
   }
   
   /** For internal use only. */
   public void ignorableWhitespace (char ch[], int start, int length)
      throws SAXException
   {
   }

   /** For internal use only. */
   public void processingInstruction (String target, String data)
      throws SAXException
   {
   }

   /** For internal use only. */
   public void startPrefixMapping(String prefix, String uri)
      throws SAXException
   {
   }

   /** For internal use only. */
   public void endPrefixMapping(String prefix)
      throws SAXException
   {
   }

   /** For internal use only. */
   public void setDocumentLocator (Locator locator)
   {
   }

   /** For internal use only. */
   public void skippedEntity(String name)
      throws SAXException
   {
   }

   //**************************************************************************
   // Element processing methods -- in alphabetical order
   //**************************************************************************

   private void processAttribute(Attributes attrs)
      throws XMLMiddlewareException
   {
      String  attrValue;
      XMLName xmlName;

      // Check if the user attempted to specify that the attribute contained
      // XML markup and throw an error if this is true.

      if (containsXML)
         throw new XMLMiddlewareException("The ContainsXML attribute may not be set to 'Yes' for PropertyMaps that map attributes.");

      // Get the attribute's name and create an XMLName, then create an
      // attribute map.

      attrValue = getAttrValue(attrs, MapConst.ATTR_NAME);
      xmlName = XMLName.create(attrValue, map.getNamespaceURIs());
      propMap = PropertyMap.create(xmlName, PropertyMap.ATTRIBUTE);

      // Check whether the <PropertyMap> element is inside a <ClassMap> or an
      // <InlineMap> element and add it accordingly. This throws an error if the
      // attribute has already been mapped.

      switch (((Integer)stateStack.peek()).intValue())
      {
         case iSTATE_CLASSMAP:
            classMap.addAttributeMap(propMap);
            break;

         case iSTATE_INLINECLASS:
            inlineClassMap.addAttributeMap(propMap);
            break;
      }

      // Set whether the attribute value is a token list.

      propMap.setIsTokenList(isTokenList);
   }

   private void processCatalog(Attributes attrs)
   {
      catalogName = getAttrValue(attrs, MapConst.ATTR_NAME);
   }

   private void processColumn(Attributes attrs)
      throws XMLMiddlewareException
   {
      Column          column;
      String          columnName, attrValue;
      int             type = Types.NULL, nullability;
      StringFormatter formatter;

      // Create the column and add it to the table. This throws an error if
      // the column already exists.

      columnName = getAttrValue(attrs, MapConst.ATTR_NAME);
      column = Column.create(columnName);
      table.addColumn(column);

      // Get the data type, if any.

      attrValue = getAttrValue(attrs, MapConst.ATTR_DATATYPE);
      if (attrValue != null)
      {
         type = JDBCTypes.getType(attrValue);
         if (type == Types.NULL)
            throw new XMLMiddlewareException("Invalid data type: " + attrValue);
         column.setType(type);
      }

      // Get the column length, if any.

      attrValue = getAttrValue(attrs, MapConst.ATTR_LENGTH);
      if (attrValue != null)
      {
         column.setLength(parseInt(attrValue));
      }

      // Get the column precision, if any.

      attrValue = getAttrValue(attrs, MapConst.ATTR_PRECISION);
      if (attrValue != null)
      {
         column.setPrecision(parseInt(attrValue));
      }

      // Get the column scale, if any.

      attrValue = getAttrValue(attrs, MapConst.ATTR_SCALE);
      if (attrValue != null)
      {
         column.setScale(parseInt(attrValue));
      }

      // Get the nullability, if any. We translate here between the token
      // values and the DatabaseMetaData values.

      attrValue = getAttrValue(attrs, MapConst.ATTR_NULLABLE);
      if (attrValue != null)
      {
         nullability = enumTokens.getToken(attrValue);
         switch (nullability)
         {
            case MapConst.ENUM_TOKEN_YES:
               nullability = DatabaseMetaData.columnNullable;
               break;

            case MapConst.ENUM_TOKEN_NO:
               nullability = DatabaseMetaData.columnNoNulls;
               break;

            case MapConst.ENUM_TOKEN_UNKNOWN:
               nullability = DatabaseMetaData.columnNullableUnknown;
               break;

            default:
               throw new XMLMiddlewareException("Invalid nullability value: " + attrValue);
         }
         column.setNullability(nullability);
      }

      // Get the named column format if any.

      attrValue = getAttrValue(attrs, MapConst.ATTR_FORMAT);
      if (attrValue != null)
      {
         formatter = map.getNamedFormatter(attrValue);
         if (formatter == null)
            throw new XMLMiddlewareException("Column " + columnName + " uses the named format " + attrValue + ". The format was not declared.");
         column.setFormatter(formatter);
      }
      else if (type != Types.NULL)
      {
         column.setFormatter(map.getDefaultFormatter(type));
      }
   }

   private void processDatabase(Attributes attrs)
   {
      databaseName = getAttrValue(attrs, MapConst.ATTR_NAME, MapConst.DEF_DATABASENAME);
   }

   private void processDateFormatEnd()
      throws XMLMiddlewareException
   {
      DateFormat      df;
      StringFormatter formatter;

      df = (locale == null) ? DateFormat.getDateInstance(dateStyle) :
                              DateFormat.getDateInstance(dateStyle, locale);
      formatter = new DateFormatter(df);

      // Add the formatting object to the hashtable of named formatting
      // objects and/or the list of default formatting objects.

      addNamedFormatter(formatName, formatter);
      addDefaultFormatter(defaultForTypes, formatter);
   }

   private void processDateFormatStart(Attributes attrs, boolean getDateStyle, boolean getTimeStyle)
      throws XMLMiddlewareException
   {
      processFormatStart(attrs);
      if (getDateStyle)
      {
         dateStyle = enumTokens.getToken(getAttrValue(attrs, MapConst.ATTR_DATESTYLE));
      }
      if (getTimeStyle)
      {
         timeStyle = enumTokens.getToken(getAttrValue(attrs, MapConst.ATTR_TIMESTYLE));
      }
   }

   private void processDateTimeFormatEnd()
      throws XMLMiddlewareException
   {
      DateFormat      df;
      StringFormatter formatter;

      df = (locale == null) ? DateFormat.getDateTimeInstance(dateStyle, timeStyle) :
                              DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
      formatter = new DateFormatter(df);

      // Add the formatting object to the hashtable of named formatting
      // objects and/or the list of default formatting objects.

      addNamedFormatter(formatName, formatter);
      addDefaultFormatter(defaultForTypes, formatter);
   }

   private void processDecimalFormatStart(Attributes attrs)
      throws XMLMiddlewareException
   {
      processFormatStart(attrs);

      pattern = getAttrValue(attrs, MapConst.ATTR_PATTERN);
   }

   private void processDecimalFormatEnd()
      throws XMLMiddlewareException
   {
      NumberFormat    nf;
      StringFormatter formatter;

      if (locale != null)
      {
         try
         {
            nf = NumberFormat.getInstance(locale);
            ((DecimalFormat)nf).applyLocalizedPattern(pattern);
         }
         catch (ClassCastException c)
         {
            throw new XMLMiddlewareException("No DecimalFormat object available for locale with country " + locale.getCountry() + " and language " + locale.getLanguage() + ".");
         }
      }
      else
      {
         nf = new DecimalFormat(pattern);
      }
      formatter = new NumberFormatter(nf);

      // Add the formatting object to the hashtable of named formatting
      // objects and/or the list of default formatting objects.

      addNamedFormatter(formatName, formatter);
      addDefaultFormatter(defaultForTypes, formatter);
   }

   private void processElementType(Attributes attrs)
      throws XMLMiddlewareException
   {
      String         qualifiedName;
      InlineClassMap parentInlineClassMap;

      // Get the qualified element type name and convert it to an XMLName.

      qualifiedName = getAttrValue(attrs, MapConst.ATTR_NAME);

      elementTypeName = XMLName.create(qualifiedName, map.getNamespaceURIs());

      switch (state.intValue())
      {
         case iSTATE_CLASSMAP:
            // Create the ClassMap using the element type name. Note that we use
            // map.createClassMap() because the ClassMap might already have been
            // created, such as when processing a <RelatedMap> element.

            classMap = map.createClassMap(elementTypeName);
            break;

         case iSTATE_PROPERTYMAP:
            // Create a new PropertyMap. Set whether it is a token list and contains XML.

            propMap = PropertyMap.create(elementTypeName, PropertyMap.ELEMENTTYPE);
            propMap.setIsTokenList(isTokenList);
            propMap.setContainsXML(containsXML);

            // Check whether the <PropertyMap> element is inside a <ClassMap> or an
            // <InlineMap> element and add it accordingly. This throws an error if
            // the element type has already been mapped.

            switch (((Integer)stateStack.peek()).intValue())
            {
               case iSTATE_CLASSMAP:
                  classMap.addChildMap(propMap);
                  break;

               case iSTATE_INLINECLASS:
                  inlineClassMap.addChildMap(propMap);
                  break;
            }
            break;

         case iSTATE_RELATEDCLASS:
            // Create a new RelatedClassMap.

            rcmWrapper.relatedClassMap = RelatedClassMap.create(elementTypeName);

            // Add the RelatedClassMap. Where we add this depends on whether the
            // <RelatedClass> element is in a <ClassMap> element or an <InlineMap>
            // element. Peek at the previous state to find out. This throws an exception
            // if the child element type has already been mapped for the class.

            switch (((Integer)stateStack.peek()).intValue())
            {
               case iSTATE_CLASSMAP:
                  classMap.addChildMap(rcmWrapper.relatedClassMap);
                  break;

               case iSTATE_INLINECLASS:
                  inlineClassMap.addChildMap(rcmWrapper.relatedClassMap);
                  break;
            }

            // There are two possibilities for when the ClassMap will be set:
            //
            // 1) The related element type is mapped with <RelatedClass>/<UseClassMap>.
            //    In this case, we will set the ClassMap in processUseClassMap().
            //
            // 2) The related element type is mapped just with <RelatedClass>. In
            //    this case, we will set the ClassMap in processUniqueKey(), which
            //    is the next element in the document.

            break;

         case iSTATE_INLINECLASS:
            // Create a new InlineClassMap.

            inlineClassMap = InlineClassMap.create(elementTypeName);

            // Check whether the <InlineMap> element is inside a <ClassMap> element or
            // another <InlineMap> element and add it accordingly. This throws an
            // exception if the element type has already been mapped.

            switch (((Integer)stateStack.peek()).intValue())
            {
               case iSTATE_CLASSMAP:
                  classMap.addChildMap(inlineClassMap);
                  break;

               case iSTATE_INLINECLASS:
                  // Add the new InlineClassMap to the parent InlineClassMap, which
                  // is already on the top of the stack.

                  parentInlineClassMap = (InlineClassMap)inlineClassMapStack.peek();
                  parentInlineClassMap.addChildMap(inlineClassMap);
                  break;
            }
            break;
      }
   }

   private void processEmptyStringIsNull()
   {
      map.setEmptyStringIsNull(true);
   }

   private void processExtends(Attributes attrs)
      throws XMLMiddlewareException
   {
      throw new XMLMiddlewareException("Extends keyword not yet implemented.");
/*
      String   qualifiedName;
      ClassMap baseClassMap;

      // Get the qualified name of the base class map's element type, get/create
      // the class map for that element type, and set the base class map on the
      // current class map.

      qualifiedName = getAttrValue(attrs, MapConst.ATTR_ELEMENTTYPE);
      baseClassMap = map.createClassMap(XMLName.create(qualifiedName, map.getNamespaceURIs()));
      classMap.setBaseClassMap(baseClassMap);
*/
   }

   private void processFixedOrder(Attributes attrs)
      throws XMLMiddlewareException
   {
      OrderInfo orderInfo;
      String    attrValue;

      // If we're mapping a property and the property is an attribute, return an error.

      if (state.intValue() == iSTATE_PROPERTYMAP)
      {
         if (propMap.getType() == PropertyMap.ATTRIBUTE)
            throw new XMLMiddlewareException("A PropertyMap for an attribute may not contain a FixedOrder element. (If the attribute value is a token list, it may contain an TLOrderColumn element.)");
      }

      // Create an OrderInfo object.

      orderInfo = createOrderInfo(attrs, false);

      // Set the fixed order value.

      attrValue = getAttrValue(attrs, MapConst.ATTR_VALUE);
      orderInfo.setFixedOrderValue(parseInt(attrValue));
   }

   private void processForeignKey(Attributes attrs)
      throws XMLMiddlewareException
   {
      String    name;
      FKWrapper fkWrapper;

      // Initialize the keyColumns Vector.

      keyColumns.removeAllElements();

      // Create a foreign key and add it to the table; throws an exception if
      // the table already has a foreign key with this name.

      name = getAttrValue(attrs, MapConst.ATTR_NAME);
      key = Key.createForeignKey(name);
      table.addForeignKey(key);

      // Create a new foreign key wrapper and add it to the stack. See processUseTable
      // for details.

      fkWrapper = new FKWrapper();
      fkWrapperStack.push(fkWrapper);
      fkWrapper.foreignKey = key;
   }

   private void processFormatClass(Attributes attrs)
      throws XMLMiddlewareException
   {
      String          className;
      StringFormatter formatter;

      // Get the common attributes.

      processFormatStart(attrs);

      // Instantiate an object of the custom formatting class.

      className = getAttrValue(attrs, MapConst.ATTR_CLASS);
      try
      {
         formatter = (StringFormatter)Class.forName(className).newInstance();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }

      // Add the formatting object to the hashtable of named formatting
      // objects and/or the list of default formatting objects.

      addNamedFormatter(formatName, formatter);
      addDefaultFormatter(defaultForTypes, formatter);
   }

   private void processFormatStart(Attributes attrs)
      throws XMLMiddlewareException
   {
      // Get the attribute values and check that the Name or DefaultForTypes
      // or both attributes are present.

      formatName = getAttrValue(attrs, MapConst.ATTR_NAME);
      defaultForTypes = getAttrValue(attrs, MapConst.ATTR_DEFAULTFORTYPES);
      if ((formatName == null) && (defaultForTypes == null))
         throw new XMLMiddlewareException("At least one of the attributes Name and DefaultForTypes must be present on a formatting element.");

      locale = null;
   }

   private void processKeyEnd()
   {
      Column[] columns;

      columns = new Column[keyColumns.size()];
      for (int i = 0; i < columns.length; i++)
      {
         columns[i] = (Column)keyColumns.elementAt(i);
      }
      key.setColumns(columns);
   }

   private void processLocale(Attributes attrs)
      throws XMLMiddlewareException
   {
      String country, language;

      country = getAttrValue(attrs, MapConst.ATTR_COUNTRY);
      language = getAttrValue(attrs, MapConst.ATTR_LANGUAGE);
      locale = new Locale(language, country);
   }

   private void processNamespace(Attributes attrs)
      throws XMLMiddlewareException
   {
      String uri, prefix;

      prefix = getAttrValue(attrs, MapConst.ATTR_PREFIX);
      if (prefix == null)
      {
         // This is a somewhat annoying predicament. In getAttrValue, we set the
         // attribute value to null if it is an empty string. This is because some
         // parsers don't follow the SAX spec and incorrectly return an empty string
         // when the attribute is not found. However, an empty string is a legal value
         // for the prefix. Therefore, if prefix is null, we assume an empty value was
         // actually retrieved. We assume that the prefix attribute was present, since
         // it is required by the DTD and we have undefined behavior if the document
         // is not valid. Long term, we need to simply fail with parsers that are bogus...

         prefix = "";
      }
      uri = getAttrValue(attrs, MapConst.ATTR_URI);

      map.addNamespace(prefix, uri);
   }

   private void processNumberFormatEnd()
      throws XMLMiddlewareException
   {
      NumberFormat    nf;
      StringFormatter formatter;

      nf = NumberFormat.getInstance(locale);
      formatter = new NumberFormatter(nf);

      // Add the formatting object to the hashtable of named formatting
      // objects and/or the list of default formatting objects.

      addNamedFormatter(formatName, formatter);
      addDefaultFormatter(defaultForTypes, formatter);
   }

   private void processOrderColumn(Attributes attrs, boolean isTokenList)
      throws XMLMiddlewareException
   {
      // This method processes both OrderColumn and TLOrderColumn. In the latter
      // case, isTokenList is true. This is passed to processOrder, which then
      // calls setTokenListOrderInfo instead of setOrderInfo.

      OrderInfo orderInfo;
      String    name;
      boolean   generate;
      Column    column;
      LinkInfo  linkInfo;
      Table     orderColumnTable = null;

      // Create an OrderInfo object

      orderInfo = createOrderInfo(attrs, isTokenList);

      // Get the name of the order column and whether to generate it

      name = getAttrValue(attrs, MapConst.ATTR_NAME);
      generate = isYes(getAttrValue(attrs, MapConst.ATTR_GENERATE, MapConst.DEF_GENERATE));

      // Get the table that the order column is in.

      switch (state.intValue())
      {
         case iSTATE_PROPERTYMAP:
            if (isTokenList)
            {
               if (!propMap.isTokenList())
                  throw new XMLMiddlewareException("A PropertyMap cannot contain a TLOrderColumn element unless the TokenList attribute is set to 'Yes'.");
            }
            else if (propMap.getType() == PropertyMap.ATTRIBUTE)
               throw new XMLMiddlewareException("A PropertyMap for an attribute may not contain an OrderColumn element. (If the attribute value is a token list, it may contain a TLOrderColumn element.)");

            // If the LinkInfo is null, the order column is in the class table.
            // If the LinkInfo is not null, the order column is in the table
            // containing the foreign key. Note that the parent table is the
            // ClassMap table.

            linkInfo = propMap.getLinkInfo();
            if (linkInfo == null)
            {
               orderColumnTable = classMap.getTable();
            }
            else if (linkInfo.parentKeyIsUnique())
            {
               orderColumnTable = propMap.getTable();
            }
            else
            {
               orderColumnTable = classMap.getTable();
            }
            break;

         case iSTATE_RELATEDCLASS:
            // We may not know the name of the related class table yet, so
            // just save the name of the order column and whether it is
            // generated. We will process it later in resolveRCMWrappers().

            rcmWrapper.orderColumnName = name;
            rcmWrapper.generateOrder = generate;
            return;

         case iSTATE_INLINECLASS:
            // Since inlined elements always are stored in the class table,
            // the table is always the table in the ClassMap. Note that this
            // is true even if the <InlineMap> elements are nested.

            orderColumnTable = classMap.getTable();
            break;
      }

      // Get the column from the table and add it to the OrderInfo. It must
      // already have been declared or an exception is thrown.

      column = orderColumnTable.getColumn(name);
      if (column == null)
         throw new XMLMiddlewareException("Order column " + name + " not found in table " + orderColumnTable.getUniversalName());
      orderInfo.setOrderColumn(column);

      // Set whether order is generated.

      orderInfo.setGenerateOrder(generate);
   }

   private void processPCDATA()
      throws XMLMiddlewareException
   {
      // Check if the user attempted to specify that the PCDATA contained
      // XML markup and throw an error if this is true.

      if (containsXML)
         throw new XMLMiddlewareException("The ContainsXML attribute may not be set to 'Yes' for PropertyMaps that map PCDATA.");

      // Create a new PropertyMap and set whether it contains a token list.

      propMap = PropertyMap.create(null, PropertyMap.PCDATA);
      propMap.setIsTokenList(isTokenList);

      // Check whether the <PropertyMap> element is inside a <ClassMap> or an
      // <InlineMap> element and add it accordingly. This throws an error if the
      // PCDATA has already been mapped.

      switch (((Integer)stateStack.peek()).intValue())
      {
         case iSTATE_CLASSMAP:
            classMap.addPCDATAMap(propMap);
            break;

         case iSTATE_INLINECLASS:
            inlineClassMap.addPCDATAMap(propMap);
            break;
      }
   }

   private void processPrimaryKey(Attributes attrs)
      throws XMLMiddlewareException
   {
      String name, keyGenerator;
      int    generate;

      // Initialize the keyColumns Vector.

      keyColumns.removeAllElements();

      // Create a primary key and add it to the table; throws an exception if
      // the table already has a primary key.

      name = getAttrValue(attrs, MapConst.ATTR_NAME, MapConst.VALUE_PRIMARYKEY);
      key = Key.createPrimaryKey(name);
      table.addPrimaryKey(key);

      // Set the key generation.

      keyGenerator = getAttrValue(attrs, MapConst.ATTR_KEYGENERATOR);
      if (keyGenerator == null)
      {
         generate = Key.DOCUMENT;
      }
      else if (keyGenerator.equals(MapConst.VALUE_DATABASE))
      {
         generate = Key.DATABASE;
         keyGenerator = null;
      }
      else
      {
         generate = Key.XMLDBMS;
      }
      key.setKeyGeneration(generate, keyGenerator);
   }

   private void processPropertyMap(Attributes attrs)
   {
      String attrValue;

      // All we do here is save whether the property contains a token list and the
      // column to which it is mapped contains XML. We actually create the propMap
      // when we encounter the <ElementType>, <Attribute>, or <PCDATA> element.

      attrValue = getAttrValue(attrs, MapConst.ATTR_TOKENLIST, MapConst.DEF_TOKENLIST);
      isTokenList = isYes(attrValue);

      attrValue = getAttrValue(attrs, MapConst.ATTR_CONTAINSXML, MapConst.DEF_CONTAINSXML);
      containsXML = isYes(attrValue);
   }

   private void processRelatedClass(Attributes attrs)
   {
      // We can't process <RelatedClass> elements immediately because it is possible
      // that the corresponding <ClassMap> element might not have been processed yet.
      // In this case, we don't know the name of the related class table and therefore
      // can't get the necessary keys and order column. Therefore, we create a wrapper
      // object to store the information and add it to the stack of wrapper objects. These
      // will all be processed with resolveRCMWrappers() at the end of the document.

      rcmWrapper = new RCMWrapper();
      rcmWrapperStack.push(rcmWrapper);
      rcmWrapper.parentClassMap = classMap;
      rcmWrapper.parentKeyIsUnique = getParentKeyIsUnique(attrs, MapConst.ATTR_KEYINPARENTTABLE);
   }

   private void processSchema(Attributes attrs)
   {
      schemaName = getAttrValue(attrs, MapConst.ATTR_NAME);
   }

   private void processSimpleDateFormatEnd()
      throws XMLMiddlewareException
   {
      DateFormat      df;
      StringFormatter formatter;

      df = (locale == null) ? new SimpleDateFormat(pattern) :
                              new SimpleDateFormat(pattern, locale);
      formatter = new DateFormatter(df);

      // Add the formatting object to the hashtable of named formatting
      // objects and/or the list of default formatting objects.

      addNamedFormatter(formatName, formatter);
      addDefaultFormatter(defaultForTypes, formatter);
   }

   private void processSimpleDateFormatStart(Attributes attrs)
      throws XMLMiddlewareException
   {
      processFormatStart(attrs);
      pattern = getAttrValue(attrs, MapConst.ATTR_PATTERN);
   }

   private void processTableStart(Attributes attrs)
      throws XMLMiddlewareException
   {
      String tableName;

      // Get the table name, create a new table, and add it to the XMLDBMSMap. We use
      // XMLDBMSMap.addTable because it will throw an exception if the table has already
      // been created.

      tableName = getAttrValue(attrs, MapConst.ATTR_NAME);
      table = Table.create(databaseName, catalogName, schemaName, tableName);
      map.addTable(table);
   }

   private void processTableEnd()
      throws XMLMiddlewareException
   {
      Enumeration columns;
      Column      column;

      // Set the column numbers from 1 to n.

      columns = table.getColumns();
      for (int i = 1; i <= table.getNumberOfColumns(); i++)
      {
         column = (Column)columns.nextElement();
         column.setResultSetIndex(i);
      }
   }

   private void processTimeFormatEnd()
      throws XMLMiddlewareException
   {
      DateFormat      df;
      StringFormatter formatter;

      df = (locale == null) ? DateFormat.getTimeInstance(timeStyle) :
                              DateFormat.getTimeInstance(timeStyle, locale);
      formatter = new DateFormatter(df);

      // Add the formatting object to the hashtable of named formatting
      // objects and/or the list of default formatting objects.

      addNamedFormatter(formatName, formatter);
      addDefaultFormatter(defaultForTypes, formatter);
   }

   private void processToClassTable(Attributes attrs)
      throws XMLMiddlewareException
   {
      Table  classTable;
      String hashName;

      // Get the class table.

      classTable = getTable(attrs);

      // Check that it is not already mapped and add it to the list of mapped tables.

      hashName = classTable.getHashName();
      if (propTables.get(hashName) != null)
         throw new XMLMiddlewareException("Table already mapped as a property table: " + classTable.getUniversalName());
      else if (classTables.get(hashName) != null)
         throw new XMLMiddlewareException("Table already mapped as a class table: " + classTable.getUniversalName());
      classTables.put(hashName, classTable);

      // Set the table in the ClassMap.

      classMap.setTable(classTable);
   }

   private void processToColumn(Attributes attrs)
      throws XMLMiddlewareException
   {
      String name;
      Table  propTable;
      Column column;

      // Get the table in which the property column exists. This is either the
      // property table (if there is one) or the class table.
      //
      // Note that it doesn't matter if the <PropertyMap> element is in a <ClassMap>
      // element or an <InlineMap> element, since <InlineMap>s don't change the class table.

      propTable = propMap.getTable();
      if (propTable == null)
      {
         propTable = classMap.getTable();
      }

      // Get the name of the column, then get the column, then set the column in
      // the PropertyMap.

      name = getAttrValue(attrs, MapConst.ATTR_NAME);
      column = propTable.getColumn(name);
      if (column == null)
         throw new XMLMiddlewareException("Property column " + name + " not found in table " + propTable.getUniversalName());

      propMap.setColumn(column);
   }

   private void processToPropertyTable(Attributes attrs)
      throws XMLMiddlewareException
   {
      String hashName;

      // Get the property table. We will set the property table in
      // processUseForeignKey, since we will then have all the necessary information.
      // Note that the unique key is always in the parent (class) table.

      propertyTable = getTable(attrs);
      parentKeyIsUnique = true;
//      parentKeyIsUnique = getParentKeyIsUnique(attrs, MapConst.ATTR_KEYINPARENTTABLE);

      // Check that the table is not already mapped and add it to the list of mapped tables.

      hashName = propertyTable.getHashName();
      if (propTables.get(hashName) != null)
         throw new XMLMiddlewareException("Table already mapped as a property table: " + propertyTable.getUniversalName());
      else if (classTables.get(hashName) != null)
         throw new XMLMiddlewareException("Table already mapped as a class table: " + propertyTable.getUniversalName());
      propTables.put(hashName, propertyTable);
   }

   private void processUniqueKey(Attributes attrs)
      throws XMLMiddlewareException
   {
      String name;

      // Initialize the keyColumns Vector.

      keyColumns.removeAllElements();

      // Create a unique key and add it to the table; throws an exception if
      // the table already has a unique key with this name.

      name = getAttrValue(attrs, MapConst.ATTR_NAME);
      key = Key.createUniqueKey(name);
      table.addUniqueKey(key);
   }

   private void processUseBaseTable(Attributes attrs)
   {
      // We can't process <UseBaseTable> elements immediately because it is possible
      // that the <ClassMap> element of the base class might not have been processed yet.
      // In this case, we don't know the name of the base class table and therefore
      // can't get the necessary keys. Therefore, we create a wrapper object to store
      // the information and add it to the stack of wrapper objects. These will all
      // be processed with resolveBaseTableWrappers() at the end of the document.

      baseTableWrapper = new BaseTableWrapper();
      baseTableWrapperStack.push(baseTableWrapper);
      baseTableWrapper.extendedClassMap = classMap;
      baseTableWrapper.baseKeyIsUnique = getParentKeyIsUnique(attrs, MapConst.ATTR_KEYINBASETABLE);
   }

   private void processUseClassMap(Attributes attrs)
      throws XMLMiddlewareException
   {
      String   useQualifiedName;
      XMLName  useXMLName;
      ClassMap useClassMap;

      // 1) Get the name of the element type whose ClassMap is to be used. This
      //    is stored in the ElementType attribute of the <UseClassMap> element.

      useQualifiedName = getAttrValue(attrs, MapConst.ATTR_ELEMENTTYPE);
      useXMLName = XMLName.create(useQualifiedName, map.getNamespaceURIs());

      // 2) Get/create the ClassMap for the used element type. We use createClassMap
      //    here because it is not known whether the ClassMap already exists.

      useClassMap = map.createClassMap(useXMLName);

      // 3) Set the used ClassMap.

      switch (state.intValue())
      {
         case iSTATE_CLASSMAP:
            classMap.useClassMap(useClassMap);
            break;

         case iSTATE_RELATEDCLASS:
            rcmWrapper.relatedClassMap.setClassMap(useClassMap);
            break;
      }
   }

   private void processUseColumn(Attributes attrs)
      throws XMLMiddlewareException
   {
      String name;
      Column column;

      // Get the name of the column to use in the key.

      name = getAttrValue(attrs, MapConst.ATTR_NAME);

      // Get the key column. This is in the table for the ClassMap being
      // declared. Note that the column must have been declared or an error
      // is thrown.

      column = table.getColumn(name);
      if (column == null)
          throw new XMLMiddlewareException("Key column " + name + " not found in table " + table.getUniversalName());
      keyColumns.addElement(column);
   }

   private void processUseForeignKey(Attributes attrs)
      throws XMLMiddlewareException
   {
      String   name;
      Table    foreignKeyTable, uniqueKeyTable;
      Key      foreignKey;
      LinkInfo linkInfo;

      name = getAttrValue(attrs, MapConst.ATTR_NAME);

      switch (state.intValue())
      {
         case iSTATE_TOPROPERTYTABLE:
            // Get the unique and foreign key tables. The unique key comes from
            // processUseUniqueKey().

            foreignKeyTable = (parentKeyIsUnique) ? propertyTable : classMap.getTable();
            uniqueKeyTable = (parentKeyIsUnique) ? classMap.getTable() : propertyTable;
            foreignKey = getForeignKey(foreignKeyTable, name);

            // Check that the table and unique key referenced by the foreign key are
            // the table and unique key used in the <RelatedClass> element.

            checkUniqueKey(MapConst.ELEM_TOPROPERTYTABLE, foreignKeyTable, foreignKey, uniqueKeyTable, uniqueKey);

            // Create a LinkInfo object from the two keys.

            linkInfo = (parentKeyIsUnique) ? LinkInfo.create(uniqueKey, foreignKey) :
                                             LinkInfo.create(foreignKey, uniqueKey);

            // Add the property table and LinkInfo to the property map.

            propMap.setTable(propertyTable, linkInfo);
            break;

         case iSTATE_RELATEDCLASS:
            // We might not know the table of the related class yet, so all we can
            // do is save the name of the foreign key. This will be processed later
            // in resolveRCMWrappers().

            rcmWrapper.foreignKeyName = name;
            break;

         case iSTATE_USEBASETABLE:
            // We might not know the table of the base class yet, so all we can
            // do is save the name of the foreign key. This will be processed later
            // in resolveBaseTableWrappers().

            baseTableWrapper.foreignKeyName = name;
            break;
      }
   }

   private void processUseTable(Attributes attrs)
   {
      FKWrapper fkWrapper;

      // Create a new foreign key wrapper and push it on the stack. We can't
      // process the remote table/key used by the foreign key now because they
      // might not have been created yet. The foreign key pointers will be resolved
      // at the end of the <Databases> element.

      fkWrapper = (FKWrapper)fkWrapperStack.peek();

      // Get the database, catalog, schema, and table names.

      fkWrapper.remoteDatabaseName = getAttrValue(attrs, MapConst.ATTR_DATABASE, MapConst.DEF_DATABASENAME);
      fkWrapper.remoteCatalogName = getAttrValue(attrs, MapConst.ATTR_CATALOG);
      fkWrapper.remoteSchemaName = getAttrValue(attrs, MapConst.ATTR_SCHEMA);
      fkWrapper.remoteTableName = getAttrValue(attrs, MapConst.ATTR_NAME);
   }

   private void processUseUniqueKey(Attributes attrs)
      throws XMLMiddlewareException
   {
      String    name;
      Table     uniqueKeyTable;
      Key       foreignKey;
      FKWrapper fkWrapper;

      name = getAttrValue(attrs, MapConst.ATTR_NAME);

      switch (state.intValue())
      {
         case iSTATE_KEY:
            // Store the name of the unique key used by the foreign key. See
            // processUseTable for details.

            fkWrapper = (FKWrapper)fkWrapperStack.peek();
            fkWrapper.remoteKeyName = name;
            break;

         case iSTATE_TOPROPERTYTABLE:
            // Get the unique key and save it. We will create a LinkInfo from
            // it and add it to the PropertyMap in processUseForeignKey(), since
            // we will then have all the information we need.

            uniqueKeyTable = (parentKeyIsUnique) ? classMap.getTable() : propertyTable;
            uniqueKey = getUniqueKey(uniqueKeyTable, name);
            break;

         case iSTATE_RELATEDCLASS:
            // We might not know the table of the related class yet, so all we can
            // do is save the name of the unique key. This will be processed later
            // in resolveRCMWrappers().

            rcmWrapper.uniqueKeyName = name;

            // We also need to do something totally unrelated to unique keys here.
            // If the RelatedClassMap does not use another ClassMap -- that is, it
            // was not mapped with a <UseClassMap> element -- then the related ClassMap
            // has not yet been set. Check whether it has been set and, if not, set it.
            // In this case, we use the ClassMap for the current element type. We use
            // createClassMap to get this ClassMap since we don't know if it has been
            // created yet.

            if (rcmWrapper.relatedClassMap.getClassMap() == null)
            {
               rcmWrapper.relatedClassMap.setClassMap(map.createClassMap(elementTypeName));
            }
            break;

         case iSTATE_USEBASETABLE:
            // We might not know the table of the base class yet, so all we can
            // do is save the name of the unique key. This will be processed later
            // in resolveBaseTableWrappers().

            baseTableWrapper.uniqueKeyName = name;
            break;
      }
   }

   private void processXMLToDBMS(Attributes attrs)
      throws XMLMiddlewareException
   {
      String version;

      // Check the version number. Since we can't count on a validating
      // parser, if the attribute doesn't exist, we assume the version
      // is correct.

      // 5/24/00 Ronald Bourret
      // getAttrValue now returns the default if no version attribute
      // was specified, so remove check for version == null

      version = getAttrValue(attrs, MapConst.ATTR_VERSION, MapConst.DEF_VERSION);
      if (!version.equals(MapConst.DEF_VERSION))
         throw new XMLMiddlewareException("Unsupported XML-DBMS version: " + version);
   }

   //**************************************************************************
   // Private methods -- miscellaneous
   //**************************************************************************

   private void init()
   {
      // Set up tokens for element names and enumerated values.

      elementTokens = new TokenList(MapConst.ELEMS, MapConst.ELEM_TOKENS, MapConst.ELEM_TOKEN_INVALID);
      enumTokens = new TokenList(MapConst.ENUMS, MapConst.ENUM_TOKENS, MapConst.ENUM_TOKEN_INVALID);
   }

   private OrderInfo createOrderInfo(Attributes attrs, boolean isTokenList)
   {
      OrderInfo orderInfo;
      String    ascending;

      // Create a new OrderInfo object and add it to the appropriate parent object.

      orderInfo = OrderInfo.create();
      switch (state.intValue())
      {
         case iSTATE_PROPERTYMAP:
            if (isTokenList)
            {
               propMap.setTokenListOrderInfo(orderInfo);
            }
            else
            {
               propMap.setOrderInfo(orderInfo);
            }
            break;

         case iSTATE_RELATEDCLASS:
            rcmWrapper.relatedClassMap.setOrderInfo(orderInfo);
            break;

         case iSTATE_INLINECLASS:
            inlineClassMap.setOrderInfo(orderInfo);
            break;
      }

      // Set whether the order is ascending or descending.

      ascending = getAttrValue(attrs, MapConst.ATTR_DIRECTION, MapConst.DEF_DIRECTION);
      orderInfo.setIsAscending(ascending.equals(MapConst.ENUM_ASCENDING));

      // Return the OrderInfo object

      return orderInfo;
   }

   private void addNamedFormatter(String formatName, StringFormatter formatter)
      throws XMLMiddlewareException
   {
      if (formatName == null) return;
      map.addNamedFormatter(formatName, formatter);
   }

   private void addDefaultFormatter(String defaultForTypes, StringFormatter formatter)
      throws XMLMiddlewareException
   {
      StringTokenizer tokenizer;
      String          typeName;
      int             type;

      if (defaultForTypes == null) return;
      tokenizer = new StringTokenizer(defaultForTypes, " \n\r\t", false);
      while (tokenizer.hasMoreTokens())
      {
         // Get the type name and type indicator. Check that the type name is valid and
         // that no default formatter has already been set for the type.

         typeName = tokenizer.nextToken();
         type = JDBCTypes.getType(typeName);
         if (type == Types.NULL)
            throw new XMLMiddlewareException("Invalid JDBC type name: " + typeName);
         if (formatTypes.get(typeName) != null)
            throw new XMLMiddlewareException("Default format already set for type: " + typeName);
         formatTypes.put(typeName, typeName);

         // Set the default formatter

         map.setDefaultFormatter(type, formatter);
      }
   }

   private void resolveFKWrappers()
      throws XMLMiddlewareException
   {
      while (!fkWrapperStack.empty())
      {
         resolveFKWrapper((FKWrapper)fkWrapperStack.pop());
      }
   }

   private void resolveFKWrapper(FKWrapper fkWrapper)
      throws XMLMiddlewareException
   {
      Table     remoteTable;
      Key       remoteKey;

      remoteTable = map.getTable(fkWrapper.remoteDatabaseName, fkWrapper.remoteCatalogName, fkWrapper.remoteSchemaName, fkWrapper.remoteTableName);
      if (remoteTable == null)
         throw new XMLMiddlewareException("Table not found: " + Table.getUniversalName(fkWrapper.remoteDatabaseName, fkWrapper.remoteCatalogName, fkWrapper.remoteSchemaName, fkWrapper.remoteTableName) + ". Referenced by foreign key: " + fkWrapper.foreignKey.getName());

      remoteKey = getUniqueKey(remoteTable, fkWrapper.remoteKeyName);

      fkWrapper.foreignKey.setRemoteKey(remoteTable, remoteKey);
   }

   private void resolveRCMWrappers()
      throws XMLMiddlewareException
   {
      while (!rcmWrapperStack.empty())
      {
         resolveRCMWrapper((RCMWrapper)rcmWrapperStack.pop());
      }
   }

   private void resolveRCMWrapper(RCMWrapper rcmWrapper)
      throws XMLMiddlewareException
   {
      Table     parentTable, childTable, uniqueKeyTable, foreignKeyTable, orderTable;
      Key       uniqueKey, foreignKey;
      Column    orderColumn;
      OrderInfo orderInfo;
      LinkInfo  linkInfo;

      // Get the parent and child tables. The parent table can't be null in a
      // valid document. This is because it comes from the <ClassMap> element,
      // which is required to have a <ToClassTable> child. However, the child
      // table can be null. This occurs if the child is mapped as a related class
      // but never mapped as a class.

      parentTable = rcmWrapper.parentClassMap.getTable();
      childTable = rcmWrapper.relatedClassMap.getClassMap().getTable();
      if (childTable == null)
         throw new XMLMiddlewareException("Element type " + rcmWrapper.relatedClassMap.getClassMap().getElementTypeName().getUniversalName() + " mapped as a related class but never mapped as a class.");

      // Get the parent and child keys. This is the usual confusing stuff about, "Um, the
      // parent key is unique so we use the unique key name with the parent table and the
      // foreign key name with the child table."

      uniqueKeyTable = (rcmWrapper.parentKeyIsUnique) ? parentTable : childTable;
      uniqueKey = getUniqueKey(uniqueKeyTable, rcmWrapper.uniqueKeyName);

      foreignKeyTable = (rcmWrapper.parentKeyIsUnique) ? childTable : parentTable;
      foreignKey = getForeignKey(foreignKeyTable, rcmWrapper.foreignKeyName);

      // Check that the table and unique key referenced by the foreign key are
      // the table and unique key used in the <RelatedClass> element.

      checkUniqueKey(MapConst.ELEM_RELATEDCLASS, foreignKeyTable, foreignKey, uniqueKeyTable, uniqueKey);

      // Create a new LinkInfo object and add it to the RelatedClassMap.

      linkInfo = (rcmWrapper.parentKeyIsUnique) ? LinkInfo.create(uniqueKey, foreignKey) :
                                                  LinkInfo.create(foreignKey, uniqueKey);
      rcmWrapper.relatedClassMap.setLinkInfo(linkInfo);

      // Set the column in the OrderInfo object if the order column name
      // is not null.

      if (rcmWrapper.orderColumnName != null)
      {
         // The order column is in the table containing the foreign key.

         if (rcmWrapper.parentKeyIsUnique)
         {
            orderTable = rcmWrapper.relatedClassMap.getClassMap().getTable();
         }
         else
         {
            orderTable = rcmWrapper.parentClassMap.getTable();
         }

         orderColumn = orderTable.getColumn(rcmWrapper.orderColumnName);
         if (orderColumn == null)
            throw new XMLMiddlewareException("Order column " + rcmWrapper.orderColumnName + " not found in table " + orderTable.getUniversalName() + ".");

         orderInfo = rcmWrapper.relatedClassMap.getOrderInfo();
         orderInfo.setOrderColumn(orderColumn);
         orderInfo.setGenerateOrder(rcmWrapper.generateOrder);
      }
   }

   private void resolveBaseTableWrappers()
      throws XMLMiddlewareException
   {
      while (!baseTableWrapperStack.empty())
      {
         resolveBaseTableWrapper((BaseTableWrapper)baseTableWrapperStack.pop());
      }
   }

   private void resolveBaseTableWrapper(BaseTableWrapper baseTableWrapper)
      throws XMLMiddlewareException
   {
      Table     extendedTable, baseTable, uniqueKeyTable, foreignKeyTable;
      Key       uniqueKey, foreignKey;
      LinkInfo  baseLinkInfo;

      // Get the tables of the extended class and the base class. The extended class table
      // can't be null in a valid document. This is because it comes from the <ClassMap>
      // element, which is required to have a <ToClassTable> child. However, the base class
      // table can be null. This occurs if the child is mapped as a base class
      // but never mapped as a class.

      extendedTable = baseTableWrapper.extendedClassMap.getTable();
      baseTable = baseTableWrapper.extendedClassMap.getBaseClassMap().getTable();
      if (baseTable == null)
         throw new XMLMiddlewareException("Element type " + baseTableWrapper.extendedClassMap.getBaseClassMap().getElementTypeName().getUniversalName() + " mapped as a base class but never mapped as a class.");

      // Get the extended and base keys. This is the usual confusing stuff about, "Um,
      // the extended class table key is unique so we use the unique key name with
      // the extended class table and the foreign key name with the base class table."

      uniqueKeyTable = (baseTableWrapper.baseKeyIsUnique) ? baseTable : extendedTable;
      uniqueKey = getUniqueKey(uniqueKeyTable, baseTableWrapper.uniqueKeyName);

      foreignKeyTable = (baseTableWrapper.baseKeyIsUnique) ? extendedTable : baseTable;
      foreignKey = getForeignKey(foreignKeyTable, baseTableWrapper.foreignKeyName);

      // Check that the table and unique key referenced by the foreign key are
      // the table and unique key used in the <RelatedClass> element.

      checkUniqueKey(MapConst.ELEM_USEBASETABLE, foreignKeyTable, foreignKey, uniqueKeyTable, uniqueKey);

      // Create a new LinkInfo object and add it to the ClassMap.

      baseLinkInfo = (baseTableWrapper.baseKeyIsUnique) ?
                                                  LinkInfo.create(uniqueKey, foreignKey) :
                                                  LinkInfo.create(foreignKey, uniqueKey);
      baseTableWrapper.extendedClassMap.setBaseLinkInfo(baseLinkInfo);
   }

   //**************************************************************************
   // Private methods -- attribute processing
   //**************************************************************************

   private String getAttrValue(Attributes attrs, String name)
   {
      String value;

      value = attrs.getValue(name);

      // Work-around for parsers (Oracle) that incorrectly return an empty
      // string instead of a null when the attribute is not found.

      if (value != null)
      {
         if (value.length() == 0)
         {
            value = null;
         }
      }

      return value;
   }

   private String getAttrValue(Attributes attrs, String name, String defaultValue)
   {
      // 5/18/00, Ronald Bourret, Richard Sullivan
      // Added defaultValue to parameter list and returned it when no
      // attribute is found. Necessary because parsers are not required to
      // return default attribute values stored in external DTDs. Thus, we
      // need to supply the defaults here.

      String attrValue;

      // Get the attribute value.

      attrValue = getAttrValue(attrs, name);

      // On null attribute value, return the default.

      return (attrValue == null) ? defaultValue : attrValue;
   }

   private boolean getParentKeyIsUnique(Attributes attrs, String attrName)
   {
      String attrValue;

      attrValue = getAttrValue(attrs, attrName);
      return attrValue.equals(MapConst.ENUM_UNIQUE);
   }

   private Table getTable(Attributes attrs)
      throws XMLMiddlewareException
   {
      String databaseName, catalogName, schemaName, tableName;
      Table  table;

      databaseName = getAttrValue(attrs, MapConst.ATTR_DATABASE, MapConst.DEF_DATABASENAME);
      catalogName = getAttrValue(attrs, MapConst.ATTR_CATALOG);
      schemaName = getAttrValue(attrs, MapConst.ATTR_SCHEMA);
      tableName = getAttrValue(attrs, MapConst.ATTR_NAME);

      table = map.getTable(databaseName, catalogName, schemaName, tableName);
      if (table == null)
         throw new XMLMiddlewareException("Table not found: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
      return table;
   }

   private boolean isYes(String yesNo)
   {
      return yesNo.equals(MapConst.ENUM_YES);
   }

   private int parseInt(String string)
      throws XMLMiddlewareException
   {
      int i;

      try
      {
         i = Integer.parseInt(string);
      }
      catch (NumberFormatException n)
      {
         throw new XMLMiddlewareException("Invalid integer value: " + string + ". " + n.getMessage());
      }
      return i;
   }

   //**************************************************************************
   // Private methods -- get stuff
   //**************************************************************************

   private Key getUniqueKey(Table uniqueKeyTable, String uniqueKeyName)
      throws XMLMiddlewareException
   {
      Key uniqueKey;

      // Get the primary key.

      uniqueKey = uniqueKeyTable.getPrimaryKey();

      // Check if the uniqueKeyName is the name of the primary key. If so,
      // use it. If not, check if there is a unique key with the specified
      // name.

      if (!uniqueKeyName.equals(uniqueKey.getName()))
      {
         uniqueKey = uniqueKeyTable.getUniqueKey(uniqueKeyName);
         if (uniqueKey == null)
            throw new XMLMiddlewareException("Unique or primary key with the name " + uniqueKeyName + " not found in table " + uniqueKeyTable.getUniversalName());
      }
      return uniqueKey;
   }

   private Key getForeignKey(Table foreignKeyTable, String foreignKeyName)
      throws XMLMiddlewareException
   {
      Key foreignKey;

      foreignKey = foreignKeyTable.getForeignKey(foreignKeyName);
      if (foreignKey == null)
         throw new XMLMiddlewareException("Foreign key " + foreignKeyName + " not found in table " + foreignKeyTable.getUniversalName());
      return foreignKey;
   }

   private void checkUniqueKey(String tag, Table foreignKeyTable, Key foreignKey, Table uniqueKeyTable, Key uniqueKey)
      throws XMLMiddlewareException
   {
      Table fkUniqueKeyTable;
      Key   fkUniqueKey;

      fkUniqueKeyTable = foreignKey.getRemoteTable();
      if (!fkUniqueKeyTable.equals(uniqueKeyTable))
         throw new XMLMiddlewareException("<" + tag + ">" + " uses foreign key " + foreignKey.getName() + " on table " + foreignKeyTable.getUniversalName() + ". This references table " + fkUniqueKeyTable.getUniversalName() + " but <UseUniqueKey> uses table " + uniqueKeyTable.getUniversalName());

      fkUniqueKey = foreignKey.getRemoteKey();
      if (!fkUniqueKey.equals(uniqueKey))
         throw new XMLMiddlewareException("<" + tag + ">" + " uses foreign key " + foreignKey.getName() + " on table " + foreignKeyTable.getUniversalName() + ". This references unique key " + fkUniqueKey.getName() + " on table " + fkUniqueKeyTable.getUniversalName() + " but <UseUniqueKey> uses key " + uniqueKey.getName() + " on table " + uniqueKeyTable.getUniversalName());
   }

   //**************************************************************************
   // Private methods -- debugging
   //**************************************************************************

   private void indent()
   {
      for (int i = 0; i < indent; i++)
      {
         System.out.print(" ");
      }
   }

   //**************************************************************************
   // Inner class: FKWrapper
   //**************************************************************************

   class FKWrapper
   {
      // Stores information about the table and unique key that a foreign key
      // points to. We process these at the end of the databases, since that is
      // the only time we are sure that all the tables and unique keys have been
      // created.

      Key    foreignKey = null;
      String remoteDatabaseName = null;
      String remoteCatalogName = null;
      String remoteSchemaName = null;
      String remoteTableName = null;
      String remoteKeyName = null;

      FKWrapper()
      {
      }
   }

   //**************************************************************************
   // Inner class: RCMWrapper
   //**************************************************************************

   class RCMWrapper
   {
      // Stores information about a related class. We process these at the end
      // of the document, since that is the only time we are sure that all the
      // corresponding ClassMaps have been processed.

      ClassMap        parentClassMap = null;
      RelatedClassMap relatedClassMap = null;
      boolean         parentKeyIsUnique = false;
      String          uniqueKeyName = null;
      String          foreignKeyName = null;
      String          orderColumnName = null;
      boolean         generateOrder = false;

      RCMWrapper()
      {
      }
   }

   //**************************************************************************
   // Inner class: BaseTableWrapper
   //**************************************************************************

   class BaseTableWrapper
   {
      // Stores information about a base table. We process these at the end
      // of the document, since that is the only time we are sure that all the
      // corresponding ClassMaps have been processed.

      ClassMap extendedClassMap = null;
      boolean  baseKeyIsUnique = false;
      String   uniqueKeyName = null;
      String   foreignKeyName = null;

      BaseTableWrapper()
      {
      }
   }

}
