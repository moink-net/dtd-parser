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
// * Modified getAttrValue to return default attribute values.
// Changes from version 1.01:
// * Update for version 2.0 DTD.
// * Delete connection and result set information.
// * Simplified API.

package org.xmlmiddleware.xmldbms.maps.factories;

import org.xmlmiddleware.utils.JDBCTypes;
import org.xmlmiddleware.utils.TokenList;
import org.xmlmiddleware.utils.XMLName;

import org.xmlmiddleware.xmldbms.maps.ClassMap;
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

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

/**
 * Create a Map from a map document.
 *
 * <p>MapFactory_MapDocument assumes that the mapping document is valid. If
 * it is not, the class will either generate garbage or throw an exception. One
 * way to guarantee that the document is valid is to pass a validating parser
 * to the map factory.</p>
 * 
 * <p>For example, the following code creates a map from the sales.map
 * mapping document.</p>
 *
 * <pre>
 *    // Instantiate a new map factory from a SAX parser.
 *    factory = new MapFactory_MapDocument(parser);<br />
 *
 *    // Create a Map from sales.map.
 *    map = factory.createMap(new InputSource(new FileReader("sales.map")));<br />
 * </pre>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class MapFactory_MapDocument
   implements DocumentHandler
{
   // Constants
   //
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
   //          STATE_PROPERTYMAP
   //             STATE_TOPROPERTYTABLE
   //             STATE_ORDER
   //          STATE_RELATEDCLASS
   //             STATE_ORDER
   //          STATE_INLINECLASS
   //             STATE_PROPERTYMAP
   //             STATE_RELATEDCLASS
   //             STATE_INLINECLASS...
   //             STATE_ORDER
   //
   // The following table shows which states affect the processing of which element types:
   //
   // State                  Affected element types
   // --------------         -----------------------------------------------------
   // STATE_KEY              UseColumn
   // STATE_CLASSMAP         ElementType, PropertyMap, InlineMap, RelatedClass, UseClassMap
   // STATE_PROPERTYMAP      ElementType, Order
   // STATE_RELATEDCLASS     ElementType, Order, UseUniqueKey, UseForeignKey, UseClassMap
   // STATE_INLINECLASS      ElementType, PropertyMap, InlineMap, RelatedClass, Order
   // STATE_TOPROPERTYTABLE  UseUniqueKey, UseForeignKey
   // STATE_ORDER            UseColumn


   // State integers (for switch statements)
   private static final int iSTATE_NONE            = 0;
   private static final int iSTATE_KEY             = 1;
   private static final int iSTATE_CLASSMAP        = 2;
   private static final int iSTATE_PROPERTYMAP     = 3;
   private static final int iSTATE_RELATEDCLASS    = 4;
   private static final int iSTATE_INLINECLASS     = 5;
   private static final int iSTATE_TOPROPERTYTABLE = 6;
   private static final int iSTATE_ORDER           = 7;

   // State objects (for state variable and stateStack)
   private static final Integer STATE_NONE            = new Integer(iSTATE_NONE);
   private static final Integer STATE_KEY             = new Integer(iSTATE_KEY);
   private static final Integer STATE_CLASSMAP        = new Integer(iSTATE_CLASSMAP);
   private static final Integer STATE_PROPERTYMAP     = new Integer(iSTATE_PROPERTYMAP);
   private static final Integer STATE_RELATEDCLASS    = new Integer(iSTATE_RELATEDCLASS);
   private static final Integer STATE_INLINECLASS     = new Integer(iSTATE_INLINECLASS);
   private static final Integer STATE_TOPROPERTYTABLE = new Integer(iSTATE_TOPROPERTYTABLE);
   private static final Integer STATE_ORDER           = new Integer(iSTATE_ORDER);

   // Date/time format type
   private static final int DATE = 0;
   private static final int TIME = 1;
   private static final int DATETIME = 2;

   //**************************************************************************
   // Variables
   //**************************************************************************

   // General class variables
   private Parser          parser = null;
   private TokenList       elementTokens, enumTokens;
   private Integer         state;
   private Stack           stateStack = new Stack();
   private Stack           rcmWrapperStack = new Stack();
   private Stack           inlineClassMapStack = new Stack();
   private Vector          keyColumns = new Vector();

   // State variables -- map
   private Map             map;

   // State variables -- databases
   private String          databaseName, catalogName, schemaName;
   private boolean         quoteIdentifiers;
   private Table           table;
   private Key             key;

   // State variables -- options
   private Locale          locale;
   private String          pattern, formatName;

   // State variables -- class maps
   private XMLName         elementTypeName;
   private ClassMap        classMap;

   // State variables -- property maps
   private PropertyMap     propMap;
   private Table           propertyTable;
   private boolean         parentKeyIsUnique;
   private Key             uniqueKey;

   // State variables -- related class maps
   private RCMWrapper      rcmWrapper;

   // State variables -- inline class maps
   private InlineClassMap  inlineClassMap;

   // State variables -- property, related class, and inline class maps
   private OrderInfo       orderInfo;

   // Debugging variables
   private int                 indent;
   private boolean             debug = false;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new MapFactory_MapDocument. */
   public MapFactory_MapDocument()
   {
      init();
   }

   /**
    * Construct a new MapFactory_MapDocument and set the SAX parser.
    */
   public MapFactory_MapDocument(Parser parser)
   {
      this.parser = parser;
      init();
   }

   //**************************************************************************
   // Public methods -- map factory
   //**************************************************************************

   /**
    * Get the SAX Parser.
    *
    * @return The SAX Parser.
    */
   public final Parser getParser()
   {
      return parser;
   }

   /**
    * Set the SAX Parser.
    *
    * @param parser The SAX Parser.
    */
   public void setParser(Parser parser)
   {
      this.parser = parser;
   }

   /**
    * Create a map from a mapping document.
    *
    * <p>You must set the parser before calling this method.</p>
    *
    * @param src A SAX InputSource for the mapping document.
    * @exception MapException Thrown if the mapping document contains an error.
    */
   public Map createMap(InputSource src)
      throws MapException
   {
      Exception e;

      // Check the arguments and state.

      if (src == null)
         throw new IllegalArgumentException("src argument must not be null.");
      if (parser == null)
         throw new IllegalStateException("Parser must be set before calling createMap.");

      // Parse the map document. Rethrow any exceptions as MapExceptions.

      try
      {
         parser.setDocumentHandler(this);
         parser.parse(src);
      }
      catch (SAXException s)
      {
         // Get the embedded Exception (if any) and check if it's a MapException.
         e = s.getException();
         if (e instanceof MapException)
            throw (MapException)e;
         else
            throw new MapException("SAX exception: " + s.getMessage());
      }
      catch (IOException io)
      {
         throw new MapException("IO exception: " + io.getMessage());
      }

      // Return the Map object.

      return map;
   }

   //**************************************************************************
   // org.xml.sax.DocumentHandler methods
   //**************************************************************************

   /**
    * Implementation of setDocumentLocator in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void setDocumentLocator (Locator locator)
   {
   }

   /**
    * Implementation of startDocument in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void startDocument () throws SAXException
   {
      if (debug)
      {
         System.out.println("Document started.");
      }

      // Initialize global variables.
      state = STATE_NONE;
      map = new Map();
      locale = null;
      pattern = null;
      formatName = null;
      databaseName = null;
      catalogName = null;
      schemaName = null;
      table = null;
      key = null;
      classMap = null;
      orderInfo = null;
      propMap = null;
      inlineClassMap = null;
//      indent = 0;

   }

   /**
    * Implementation of endDocument in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void endDocument() throws SAXException
   {
      try
      {
        resolveRCMWrappers();
        // MapInverter.createDBView(map);
      }
      catch (MapException m)
      {
         throw new SAXException(m);
      }

      if (debug)
      {
         System.out.println("Document ended.");
      }
   }

   /**
    * Implementation of startElement in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void startElement (String name, AttributeList attrs)
     throws SAXException
   {
      
      // Debugging code.
      if (debug)
      {
         indent();
         System.out.println(name + " (start)");
         indent += 3;
      }

      try
      {
         switch (elementTokens.getToken(name))
         {
            case XMLDBMSConst.ELEM_TOKEN_ATTRIBUTE:
               processAttribute(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_CATALOG:
               processCatalog(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_CLASSMAP:
               // Just set the state. We create the ClassMap in processElementType().
               stateStack.push(state);
               state = STATE_CLASSMAP;
               break;

            case XMLDBMSConst.ELEM_TOKEN_COLUMN:
               processColumn(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_DATABASE:
               processDatabase(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_DATABASES:
               // Nothing to do.
               break;

            case XMLDBMSConst.ELEM_TOKEN_DATEFORMAT:
               processFormatStart(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_DATETIMEFORMAT:
               processFormatStart(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_ELEMENTTYPE:
               processElementType(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_EMPTYSTRINGISNULL:
               processEmptyStringIsNull();
               break;

            case XMLDBMSConst.ELEM_TOKEN_EXTENDS:
               processExtends(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_FIXEDORDER:
               processFixedOrder(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_FOREIGNKEY:
               stateStack.push(state);
               state = STATE_KEY;
               processForeignKey(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_GENERATE:
               processGenerate();
               break;

            case XMLDBMSConst.ELEM_TOKEN_INLINEMAP:
               // Set the state and push the current inlineClassMap onto the stack. We
               // set the new inlineClassMap when we process the <ElementType> element.
               stateStack.push(state);
               state = STATE_INLINECLASS;
               inlineClassMapStack.push(inlineClassMap);
               break;

            case XMLDBMSConst.ELEM_TOKEN_LOCALE:
               processLocale(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_NAMESPACE:
               processNamespace(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_NUMBERFORMAT:
               processFormatStart(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_ORDER:
               processOrder(attrs);
               stateStack.push(state);
               state = STATE_ORDER;
               break;

            case XMLDBMSConst.ELEM_TOKEN_PATTERN:
               processPattern(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_PCDATA:
               processPCDATA();
               break;

            case XMLDBMSConst.ELEM_TOKEN_PRIMARYKEY:
               stateStack.push(state);
               state = STATE_KEY;
               processPrimaryKey(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_PROPERTYMAP:
               // Just set the state. We create the propMap when we encounter
               // the <ElementType>, <Attribute>, or <PCDATA> element.
               stateStack.push(state);
               state = STATE_PROPERTYMAP;
               break;

            case XMLDBMSConst.ELEM_TOKEN_RELATEDCLASS:
               // Set the state. We create the relatedClassMap when we encounter
               // the <ElementType> element.
               stateStack.push(state);
               state = STATE_RELATEDCLASS;
               processRelatedClass(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_SCHEMA:
               processSchema(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_TABLE:
               processTable(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_TIMEFORMAT:
               processFormatStart(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_TOCLASSTABLE:
               processToClassTable(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_TOCOLUMN:
               processToColumn(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_TOPROPERTYTABLE:
               processToPropertyTable(attrs);
               stateStack.push(state);
               state = STATE_TOPROPERTYTABLE;
               break;

            case XMLDBMSConst.ELEM_TOKEN_UNIQUEKEY:
               stateStack.push(state);
               state = STATE_KEY;
               processUniqueKey(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_USECLASSMAP:
               processUseClassMap(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_USECOLUMN:
               processUseColumn(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_USEFOREIGNKEY:
               processUseForeignKey(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_USEUNIQUEKEY:
               processUseUniqueKey(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_XMLTODBMS:
               processXMLToDBMS(attrs);
               break;

            case XMLDBMSConst.ELEM_TOKEN_MAPS:
            case XMLDBMSConst.ELEM_TOKEN_OPTIONS:
               // Nothing to do.
               break;

            case XMLDBMSConst.ELEM_TOKEN_INVALID:
               throw new MapException("Unrecognized XML-DBMS mapping language element type: " + name);
         }
      }
      catch (MapException m)
      {
         throw new SAXException(m);
      }
   }

   /**
    * Implementation of endElement in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void endElement (String name) throws SAXException
   {
      int    token;
      
      // Debugging code.
      if (debug)
      {
         indent -= 3;
         indent();
         System.out.println(name + " (end)");
      }

      try
      {
         switch (elementTokens.getToken(name))
         {
            case XMLDBMSConst.ELEM_TOKEN_DATEFORMAT:
               processDateFormat();
               break;

            case XMLDBMSConst.ELEM_TOKEN_DATETIMEFORMAT:
               processDateTimeFormat();
               break;

            case XMLDBMSConst.ELEM_TOKEN_FOREIGNKEY:
               processKeyEnd();
               state = (Integer)stateStack.pop();
               break;

            case XMLDBMSConst.ELEM_TOKEN_INLINEMAP:
               state = (Integer)stateStack.pop();
               inlineClassMap = (InlineClassMap)inlineClassMapStack.pop();
               break;

            case XMLDBMSConst.ELEM_TOKEN_NUMBERFORMAT:
               processNumberFormat();
               break;

            case XMLDBMSConst.ELEM_TOKEN_PRIMARYKEY:
               processKeyEnd();
               state = (Integer)stateStack.pop();
               break;

            case XMLDBMSConst.ELEM_TOKEN_TIMEFORMAT:
               processTimeFormat();
               break;

            case XMLDBMSConst.ELEM_TOKEN_UNIQUEKEY:
               processKeyEnd();
               state = (Integer)stateStack.pop();
               break;

            case XMLDBMSConst.ELEM_TOKEN_CLASSMAP:
            case XMLDBMSConst.ELEM_TOKEN_ORDER:
            case XMLDBMSConst.ELEM_TOKEN_PROPERTYMAP:
            case XMLDBMSConst.ELEM_TOKEN_RELATEDCLASS:
            case XMLDBMSConst.ELEM_TOKEN_TOPROPERTYTABLE:
               // Only need to pop the state.
               state = (Integer)stateStack.pop();
               break;

            case XMLDBMSConst.ELEM_TOKEN_ATTRIBUTE:
            case XMLDBMSConst.ELEM_TOKEN_CATALOG:
            case XMLDBMSConst.ELEM_TOKEN_COLUMN:
            case XMLDBMSConst.ELEM_TOKEN_DATABASE:
            case XMLDBMSConst.ELEM_TOKEN_DATABASES:
            case XMLDBMSConst.ELEM_TOKEN_ELEMENTTYPE:
            case XMLDBMSConst.ELEM_TOKEN_EMPTYSTRINGISNULL:
            case XMLDBMSConst.ELEM_TOKEN_EXTENDS:
            case XMLDBMSConst.ELEM_TOKEN_FIXEDORDER:
            case XMLDBMSConst.ELEM_TOKEN_GENERATE:
            case XMLDBMSConst.ELEM_TOKEN_LOCALE:
            case XMLDBMSConst.ELEM_TOKEN_MAPS:
            case XMLDBMSConst.ELEM_TOKEN_NAMESPACE:
            case XMLDBMSConst.ELEM_TOKEN_OPTIONS:
            case XMLDBMSConst.ELEM_TOKEN_PATTERN:
            case XMLDBMSConst.ELEM_TOKEN_PCDATA:
            case XMLDBMSConst.ELEM_TOKEN_SCHEMA:
            case XMLDBMSConst.ELEM_TOKEN_TABLE:
            case XMLDBMSConst.ELEM_TOKEN_TOCLASSTABLE:
            case XMLDBMSConst.ELEM_TOKEN_TOCOLUMN:
            case XMLDBMSConst.ELEM_TOKEN_USECLASSMAP:
            case XMLDBMSConst.ELEM_TOKEN_USECOLUMN:
            case XMLDBMSConst.ELEM_TOKEN_USEFOREIGNKEY:
            case XMLDBMSConst.ELEM_TOKEN_USEUNIQUEKEY:
            case XMLDBMSConst.ELEM_TOKEN_XMLTODBMS:
               // Nothing to do.
               break;

            case XMLDBMSConst.ELEM_TOKEN_INVALID:
               throw new MapException("Unrecognized XML-DBMS mapping language element type: " + name);
         }
      }
      catch (MapException m)
      {
         throw new SAXException(m);
      }
   }

   /**
    * Implementation of characters in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void characters (char ch[], int start, int length)
     throws SAXException
   {
      // No XML-DBMS elements have character content, so the document is
      // invalid if any is found (other than whitespace). Since we assume
      // that the document is valid, don't throw an error.
   }
   
   /**
    * Implementation of ignorableWhitespace in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void ignorableWhitespace (char ch[], int start, int length)
      throws SAXException
   {
   }

   /**
    * Implementation of processingInstruction in SAX' DocumentHandler interface.
    * This method is called by the SAX Parser and should not be called by
    * XML-DBMS programmers.
    */
   public void processingInstruction (String target, String data)
      throws SAXException
   {
   }

   //**************************************************************************
   // Element processing methods -- in alphabetical order
   //**************************************************************************

   private void processAttribute(AttributeList attrs)
      throws MapException
   {
      String  attrValue;
      XMLName xmlName;

      // Get the attribute's name and create an XMLName, then create an
      // attribute map.

      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
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

      // Set whether the attribute is multi-valued.

      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_MULTIVALUED, XMLDBMSConst.DEF_MULTIVALUED);
      propMap.setAttributeIsMultiValued(isYes(attrValue));
   }

   private void processCatalog(AttributeList attrs)
   {
      catalogName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
   }

   private void processColumn(AttributeList attrs)
      throws MapException
   {
      Column column;
      String columnName, attrValue;
      int    type, length, nullability;

      // Create the column and add it to the table. This throws an error if
      // the column already exists.

      columnName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
      column = Column.create(columnName);
      table.addColumn(column);

      // Get the data type, if any.

      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_DATATYPE);
      if (attrValue != null)
      {
         type = JDBCTypes.getType(attrValue);
         if (type == Types.NULL)
            throw new MapException("Invalid data type: " + attrValue);
         column.setType(type);
      }

      // Get the column length, if any.

      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_LENGTH);
      if (attrValue != null)
      {
         try
         {
            length = Integer.parseInt(attrValue);
         }
         catch (NumberFormatException n)
         {
            throw new MapException("Invalid length: " + attrValue + ". " + n.getMessage());
         }
         column.setLength(length);
      }

      // Get the nullability, if any. We translate here between the token
      // values and the DatabaseMetaData values.

      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_NULLABLE);
      if (attrValue != null)
      {
         nullability = enumTokens.getToken(attrValue);
         switch (nullability)
         {
            case XMLDBMSConst.ENUM_TOKEN_YES:
               nullability = DatabaseMetaData.columnNullable;
               break;

            case XMLDBMSConst.ENUM_TOKEN_NO:
               nullability = DatabaseMetaData.columnNoNulls;
               break;

            case XMLDBMSConst.ENUM_TOKEN_UNKNOWN:
               nullability = DatabaseMetaData.columnNullableUnknown;
               break;

            default:
               throw new MapException("Invalid nullability value: " + attrValue);
         }
         column.setNullability(nullability);
      }

      // Get the column format if any.

/*
?? DTD bug here. Since we don't necessarily know the column type, we don't know whether to look through the date, time, timestamp, or number formats. If we combine all formats into a single hashtable, we can't distinguish betwen date, time, and timestamp formats, which is a problem when serializing the DTD. If we leave them separate, we have to search four different hashtables (a) to find a format and (b) every time we add a format.

One solution is to store the format names in each column, then later have Map.resolveFormats() get the correct format for each name, including getting default formats.
*/
      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_FORMAT);
      if (attrValue != null)
      {
         throw new MapException("Bug. Column formats not implemented yet.");
      }
   }

   private void processDatabase(AttributeList attrs)
   {
      databaseName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME, XMLDBMSConst.DEF_DATABASENAME);
      quoteIdentifiers = isYes(getAttrValue(attrs, XMLDBMSConst.ATTR_QUOTEIDENTIFIERS, XMLDBMSConst.DEF_QUOTEIDENTIFIERS));
   }

   private void processDateFormat()
      throws MapException
   {
      map.addDateFormat(formatName, getDateFormat(DATE));
   }

   private void processDateTimeFormat()
      throws MapException
   {
      map.addDateTimeFormat(formatName, getDateFormat(DATETIME));
   }

   private void processElementType(AttributeList attrs)
      throws MapException
   {
      String         qualifiedName;
      InlineClassMap parentInlineClassMap;

      // Get the qualified element type name and convert it to an XMLName.

      qualifiedName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);

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
            // Create a new PropertyMap.

            propMap = PropertyMap.create(elementTypeName, PropertyMap.ELEMENTTYPE);

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

   private void processExtends(AttributeList attrs)
   {
      String   qualifiedName;
      ClassMap baseClassMap;
      boolean  useBaseTable;

      // Get the qualified name of the base class map's element type, get/create
      // the class map for that element type, and set the base class map on the
      // current class map.

      qualifiedName = getAttrValue(attrs, XMLDBMSConst.ATTR_ELEMENTTYPE);
      baseClassMap = map.createClassMap(XMLName.create(qualifiedName, map.getNamespaceURIs()));
      classMap.setBaseClassMap(baseClassMap);

      // Get whether to use the base table and set it accordingly.

      useBaseTable = isYes(getAttrValue(attrs, XMLDBMSConst.ATTR_USEBASETABLE));
      classMap.setUseBaseTable(useBaseTable);
   }

   private void processFixedOrder(AttributeList attrs)
      throws MapException
   {
      String attrValue;
      int    orderValue;

      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_VALUE);

      try
      {
         orderValue = Integer.parseInt(attrValue);
      }
      catch (NumberFormatException n)
      {
         throw new MapException("Invalid fixed order value: " + attrValue + ". " + n.getMessage());
      }
      orderInfo.setFixedOrderValue(orderValue);
   }

   private void processForeignKey(AttributeList attrs)
      throws MapException
   {
      String name;

      // Initialize the keyColumns Vector.

      keyColumns.removeAllElements();

      // Create a foreign key and add it to the table; throws an exception if
      // the table already has a foreign key with this name.

      name = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
      key = Key.createForeignKey(name);
      table.addForeignKey(key);
   }

   private void processFormatStart(AttributeList attrs)
   {
      formatName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
      locale = null;
      pattern = null;
   }

   private void processGenerate()
   {
      if (((Integer)stateStack.peek()).intValue() == iSTATE_RELATEDCLASS)
      {
         // We can't set whether order is generated now because we haven't
         // yet set the column (see processOrderColumn()). Therefore, save
         // this for processing with resolveRCMWrappers().

         rcmWrapper.generateOrder = true;
      }
      else // parent state is STATE_INLINECLASS or STATE_PROPERTYMAP
      {
         orderInfo.setGenerateOrder(true);
      }
   }

   private void processKeyColumn(String name)
      throws MapException
   {
       // Process <UseColumn> when it is inside a <PrimaryKey>, <UniqueKey>,
       // or <ForeignKey> element.

       Column column;

      // Get the key column. This is in the table for the ClassMap being
      // declared. Note that the column must have been declared or an error
      // is thrown.

      column = table.getColumn(name);
      if (column == null)
          throw new MapException("Order column " + name + " not found in table " + table.getUniversalName());
      keyColumns.addElement(column);
   }

   private void processKeyEnd()
   {
      Column[] columns;
      int      size;

      size = keyColumns.size();

      columns = new Column[size];
      for (int i = 0; i < size; i++)
      {
         columns[i] = (Column)keyColumns.elementAt(i);
      }
      key.setColumns(columns);
   }

   private void processLocale(AttributeList attrs)
      throws MapException
   {
      String country, language;

      country = getAttrValue(attrs, XMLDBMSConst.ATTR_COUNTRY);
      language = getAttrValue(attrs, XMLDBMSConst.ATTR_LANGUAGE);
      locale = new Locale(language, country);
   }

   private void processNamespace(AttributeList attrs)
      throws MapException
   {
      String uri, prefix;

      prefix = getAttrValue(attrs, XMLDBMSConst.ATTR_PREFIX);
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
      uri = getAttrValue(attrs, XMLDBMSConst.ATTR_URI);

      map.addNamespace(prefix, uri);
   }

   private void processNumberFormat()
      throws MapException
   {
      NumberFormat numberFormat;

      if (pattern != null)
      {
         numberFormat = new DecimalFormat(pattern);
      }
      else // if (locale != null)
      {
         numberFormat = NumberFormat.getInstance(locale);
      }
      map.addNumberFormat(formatName, numberFormat);
   }

   private void processOrder(AttributeList attrs)
   {
      boolean ascending;

      // Create a new OrderInfo object and add it to the appropriate parent object.

      orderInfo = OrderInfo.create();
      switch (state.intValue())
      {
         case iSTATE_PROPERTYMAP:
            propMap.setOrderInfo(orderInfo);
            break;

         case iSTATE_RELATEDCLASS:
            rcmWrapper.relatedClassMap.setOrderInfo(orderInfo);
            break;

         case iSTATE_INLINECLASS:
            inlineClassMap.setOrderInfo(orderInfo);
            break;
      }

      // Set whether the order is ascending or descending.

      ascending = getAttrValue(attrs, XMLDBMSConst.ATTR_DIRECTION, XMLDBMSConst.DEF_DIRECTION).equals(XMLDBMSConst.ENUM_ASCENDING);
      orderInfo.setIsAscending(ascending);
   }

   private void processOrderColumn(String name)
      throws MapException
   {
      // Process <UseColumn> when it is inside an <Order> element.

      Column   column;
      LinkInfo linkInfo;
      Table    orderColumnTable = null;
     
      // Get the table that the order column is in. Note that we peek at the
      // previous state, as which table we use depends on whether the <Order>
      // element appears in a <PropertyMap>, <RelatedClass>, or <InlineMap>.

      switch (((Integer)stateStack.peek()).intValue())
      {
         case iSTATE_PROPERTYMAP:
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
            // just save the name of the order column and process it later
            // in resolveRCMWrappers().

            rcmWrapper.orderColumnName = name;
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
         throw new MapException("Order column " + name + " not found in table " + orderColumnTable.getUniversalName());
      orderInfo.setOrderColumn(column);
   }

   private void processPattern(AttributeList attrs)
   {
      pattern = getAttrValue(attrs, XMLDBMSConst.ATTR_VALUE);
   }

   private void processPCDATA()
      throws MapException
   {
      // Create a new PropertyMap.

      propMap = PropertyMap.create(null, PropertyMap.PCDATA);

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

   private void processPrimaryKey(AttributeList attrs)
      throws MapException
   {
      String keyGenerator;
      int    generate;

      // Initialize the keyColumns Vector.

      keyColumns.removeAllElements();

      // Create a primary key and add it to the table; throws an exception if
      // the table already has a primary key.

      key = Key.createPrimaryKey();
      table.addPrimaryKey(key);

      // Set the key generation.

      keyGenerator = getAttrValue(attrs, XMLDBMSConst.ATTR_KEYGENERATOR);
      if (keyGenerator == null)
      {
         generate = Key.DOCUMENT;
      }
      else if (keyGenerator.equals(XMLDBMSConst.VALUE_DATABASE))
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

   private void processRelatedClass(AttributeList attrs)
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
      rcmWrapper.parentKeyIsUnique = getParentKeyIsUnique(attrs);
   }

   private void processSchema(AttributeList attrs)
   {
      schemaName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
   }

   private void processTable(AttributeList attrs)
      throws MapException
   {
      String tableName;

      // Get the table name, create a new table, and add it to the Map. Throws
      // an exception if the table has already been created.

      tableName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
      table = Table.create(databaseName, catalogName, schemaName, tableName);
      map.addTable(table);
   }

   private void processTimeFormat()
      throws MapException
   {
      map.addTimeFormat(formatName, getDateFormat(TIME));
   }

   private void processToClassTable(AttributeList attrs)
      throws MapException
   {
      classMap.setTable(getTable(attrs));
   }

   private void processToColumn(AttributeList attrs)
      throws MapException
   {
      String name;
      Table  propertyTable;
      Column column;

      // Get the table in which the property column exists. This is either the
      // property table (if there is one) or the class table.
      //
      // Note that it doesn't matter if the <PropertyMap> element is in a <ClassMap>
      // element or an <InlineMap> element, since <InlineMap>s don't change the class table.

      propertyTable = propMap.getTable();
      if (propertyTable == null)
      {
         propertyTable = classMap.getTable();
      }

      // Get the name of the column, then get the column, then set the column in
      // the PropertyMap.

      name = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
      column = propertyTable.getColumn(name);
      if (column == null)
         throw new MapException("Property column " + name + " not found in table " + propertyTable.getUniversalName());

      propMap.setColumn(column);
   }

   private void processToPropertyTable(AttributeList attrs)
      throws MapException
   {
      // Save the property table and the location of the unique key. We will
      // set the property table in processUseForeignKey, since we will then
      // have all the necessary information.

      propertyTable = getTable(attrs);
      parentKeyIsUnique = getParentKeyIsUnique(attrs);
   }

   private void processUniqueKey(AttributeList attrs)
      throws MapException
   {
      String name;

      // Initialize the keyColumns Vector.

      keyColumns.removeAllElements();

      // Create a unique key and add it to the table; throws an exception if
      // the table already has a unique key with this name.

      name = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);
      key = Key.createUniqueKey(name);
      table.addUniqueKey(key);
   }

   private void processUseClassMap(AttributeList attrs)
      throws MapException
   {
      String   useQualifiedName;
      XMLName  useXMLName;
      ClassMap useClassMap;

      // 1) Get the name of the element type whose ClassMap is to be used. This
      //    is stored in the ElementType attribute of the <UseClassMap> element.

      useQualifiedName = getAttrValue(attrs, XMLDBMSConst.ATTR_ELEMENTTYPE);
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

   private void processUseColumn(AttributeList attrs)
      throws MapException
   {
      String name;

      name = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);

      // If the <UseColumn> element occurs inside a <PrimaryKey>, <UniqueKey>,
      // or <ForeignKey> element, process it with processKeyColumn(). If it
      // occurs inside an <Order> element, process it with processOrderColumn().

      switch (state.intValue())
      {
         case iSTATE_KEY:
            processKeyColumn(name);
            break;

         case iSTATE_ORDER:
            processOrderColumn(name);
            break;
      }
   }

   private void processUseForeignKey(AttributeList attrs)
      throws MapException
   {
      String   name;
      Table    foreignKeyTable;
      Key      foreignKey;
      LinkInfo linkInfo;

      name = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);

      switch (state.intValue())
      {
         case iSTATE_TOPROPERTYTABLE:
            // Get the foreign key and create a LinkInfo object from this and
            // the key from processUseUniqueKey().

            foreignKeyTable = (parentKeyIsUnique) ? propertyTable : classMap.getTable();
            foreignKey = getForeignKey(foreignKeyTable, name);
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
      }
   }

   private void processUseUniqueKey(AttributeList attrs)
      throws MapException
   {
      String name;
      Table  uniqueKeyTable;

      name = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);

      switch (state.intValue())
      {
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
      }
   }

   private void processXMLToDBMS(AttributeList attrs)
      throws MapException
   {
      String version;

      // Check the version number. Since we can't count on a validating
      // parser, if the attribute doesn't exist, we assume the version
      // is correct.

      // 5/24/00 Ronald Bourret
      // getAttrValue now returns the default if no version attribute
      // was specified, so remove check for version == null

      version = getAttrValue(attrs, XMLDBMSConst.ATTR_VERSION, XMLDBMSConst.DEF_VERSION);
      if (!version.equals(XMLDBMSConst.DEF_VERSION))
         throw new MapException("Unsupported XML-DBMS version: " + version);
   }

   //**************************************************************************
   // Private methods -- miscellaneous
   //**************************************************************************

   private void init()
   {
      // Set up tokens for element names and enumerated values.

      elementTokens = new TokenList(XMLDBMSConst.ELEMS, XMLDBMSConst.ELEM_TOKENS, XMLDBMSConst.ELEM_TOKEN_INVALID);
      enumTokens = new TokenList(XMLDBMSConst.ENUMS, XMLDBMSConst.ENUM_TOKENS, XMLDBMSConst.ENUM_TOKEN_INVALID);
   }

   private void resolveRCMWrappers()
      throws MapException
   {
      while (!rcmWrapperStack.empty())
      {
         resolveRCMWrapper((RCMWrapper)rcmWrapperStack.pop());
      }
   }

   private void resolveRCMWrapper(RCMWrapper rcmWrapper)
      throws MapException
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
         throw new MapException("Element type " + rcmWrapper.relatedClassMap.getClassMap().getElementTypeName().getUniversalName() + " mapped as a related class but never mapped as a class.");

      // Get the parent and child keys. This is the usual confusing stuff about, "Um, the
      // parent key is unique so we use the unique key name with the parent table and the
      // foreign key name with the child table."

      uniqueKeyTable = (rcmWrapper.parentKeyIsUnique) ? parentTable : childTable;
      uniqueKey = getUniqueKey(uniqueKeyTable, rcmWrapper.uniqueKeyName);

      foreignKeyTable = (rcmWrapper.parentKeyIsUnique) ? childTable : parentTable;
      foreignKey = getForeignKey(foreignKeyTable, rcmWrapper.foreignKeyName);

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
            throw new MapException("Order column " + rcmWrapper.orderColumnName + " not found in table " + orderTable.getUniversalName() + ".");

         orderInfo = rcmWrapper.relatedClassMap.getOrderInfo();
         orderInfo.setOrderColumn(orderColumn);
         orderInfo.setGenerateOrder(rcmWrapper.generateOrder);
      }
   }

   //**************************************************************************
   // Private methods -- attribute processing
   //**************************************************************************

   private String getAttrValue(AttributeList attrs, String name)
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

   private String getAttrValue(AttributeList attrs, String name, String defaultValue)
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

   private boolean getParentKeyIsUnique(AttributeList attrs)
   {
      String attrValue;

      attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_KEYINPARENTTABLE);
      return attrValue.equals(XMLDBMSConst.ENUM_UNIQUE);
   }

   private Table getTable(AttributeList attrs)
      throws MapException
   {
      String databaseName, catalogName, schemaName, tableName;
      Table  table;

      databaseName = getAttrValue(attrs, XMLDBMSConst.ATTR_DATABASE, XMLDBMSConst.DEF_DATABASENAME);
      catalogName = getAttrValue(attrs, XMLDBMSConst.ATTR_CATALOG);
      schemaName = getAttrValue(attrs, XMLDBMSConst.ATTR_SCHEMA);
      tableName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME);

      table = map.getTable(databaseName, catalogName, schemaName, tableName);
      if (table == null)
         throw new MapException("Table not found: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
      return table;
   }

   private boolean isYes(String yesNo)
   {
      return yesNo.equals(XMLDBMSConst.ENUM_YES);
   }

   //**************************************************************************
   // Private methods -- get stuff
   //**************************************************************************

   private Key getUniqueKey(Table uniqueKeyTable, String uniqueKeyName)
      throws MapException
   {
      Key uniqueKey;

      if (uniqueKeyName.equals(XMLDBMSConst.VALUE_PRIMARYKEY))
      {
         uniqueKey = uniqueKeyTable.getPrimaryKey();
         if (uniqueKey == null)
            throw new MapException("Primary key not found in table " + uniqueKeyTable.getUniversalName());
      }
      else
      {
         uniqueKey = uniqueKeyTable.getUniqueKey(uniqueKeyName);
         if (uniqueKey == null)
            throw new MapException("Unique key " + uniqueKeyName + " not found in table " + uniqueKeyTable.getUniversalName());
      }
      return uniqueKey;
   }

   private Key getForeignKey(Table foreignKeyTable, String foreignKeyName)
      throws MapException
   {
      Key foreignKey;

      foreignKey = foreignKeyTable.getForeignKey(foreignKeyName);
      if (foreignKey == null)
         throw new MapException("Foreign key " + foreignKeyName + " not found in table " + foreignKeyTable.getUniversalName());
      return foreignKey;
   }

   private DateFormat getDateFormat(int type)
   {
      int style;

      // Convert the pattern to a style.

      style = enumTokens.getToken(pattern);

      if (style == XMLDBMSConst.ENUM_TOKEN_INVALID)
      {
         // The pattern was not "FULL", "LONG", "MEDIUM", or "SHORT", so it
         // must be a SimpleDateFormat pattern like "MM-dd-yy". Use this to
         // build a SimpleDateFormat.

         return (locale == null) ? new SimpleDateFormat(pattern) :
                                   new SimpleDateFormat(pattern, locale);
      }
      else
      {
         // The pattern was "FULL", "LONG", "MEDIUM", or "SHORT", so use this
         // to build a DateFormat.

         switch (type)
         {
            case DATE:
               return (locale == null) ? DateFormat.getDateInstance(style) :
                                         DateFormat.getDateInstance(style, locale);

            case TIME:
               return (locale == null) ? DateFormat.getTimeInstance(style) :
                                         DateFormat.getTimeInstance(style, locale);

            case DATETIME:
               return (locale == null) ? DateFormat.getDateTimeInstance(style, style) :
                                         DateFormat.getDateTimeInstance(style, style, locale);

            default:
               // Shouldn't ever hit this, but it keeps the compiler happy...
               return null;
         }
      }
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

}