// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Modified getAttrValue to return default attribute values.
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms.mapfactories;

import de.tudarmstadt.ito.utils.TokenList;
import de.tudarmstadt.ito.utils.NSName;
import de.tudarmstadt.ito.xmldbms.ClassMap;
import de.tudarmstadt.ito.xmldbms.ColumnMap;
import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import de.tudarmstadt.ito.xmldbms.Map;
import de.tudarmstadt.ito.xmldbms.PropertyMap;
import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Create a Map from a mapping document.
 *
 * <P>MapFactory_MapDocument assumes that the mapping document is valid. If
 * it is not, the class will either generate garbage or throw an error. One
 * way to guarantee that the document is valid is to pass a validating parser
 * to the map factory.</P>
 * 
 * <P>For example, the following code creates a map from the sales.map
 * mapping document.</P>
 * <PRE>
 *    // Instantiate a new map factory from a database connection
 *    // and a SAX parser.
 *    factory = new MapFactory_MapDocument(conn, parser);<BR />
 *
 *    // Create a Map from sales.map.
 *    map = factory.createMap(new InputSource(new FileReader("sales.map")));<BR />
 *
 *    // Pass the Map to DOMToDBMS.
 *    domToDBMS = new DOMToDBMS(map);
 * </PRE>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class MapFactory_MapDocument
   implements DocumentHandler
{
   // Constants
   //
   // The state gives context for elements that can occur inside more than
   // one element. For example, we need state to determine whether an
   // ElementType element occurs directly beneath a ClassMap, a PropertyMap,
   // a RelatedClass, an IgnoreRoot, or a PseudoRoot.
   // The state hierarchy is as follows:
   //
   //    STATE_CLASSMAP
   //       STATE_TOROOTTABLE
   //          STATE_CANDIDATEKEY
   //       STATE_TOCLASSTABLE
   //       STATE_PROPERTYMAP
   //          STATE_TOCOLUMN
   //          STATE_TOPROPERTYTABLE
   //             STATE_CANDIDATEKEY
   //             STATE_FOREIGNKEY
   //       STATE_RELATEDCLASS
   //          STATE_CANDIDATEKEY
   //          STATE_FOREIGNKEY
   //    STATE_IGNOREROOT
   //       STATE_PSEUDOROOT
   //          STATE_CANDIDATEKEY

   // State bitmaps
   private static final int STATE_NONE            = 0x0000;
   private static final int STATE_CLASSMAP        = 0x0001;
   private static final int STATE_TOROOTTABLE     = 0x0002;
   private static final int STATE_TOCLASSTABLE    = 0x0004;
   private static final int STATE_IGNOREROOT      = 0x0008;
   private static final int STATE_PROPERTYMAP     = 0x0010;
   private static final int STATE_TOCOLUMN        = 0x0020;
   private static final int STATE_TOPROPERTYTABLE = 0x0040;
   private static final int STATE_CANDIDATEKEY    = 0x0080;
   private static final int STATE_FOREIGNKEY      = 0x0100;
   private static final int STATE_RELATEDCLASS    = 0x0200;
   private static final int STATE_PSEUDOROOT      = 0x0400;

   // Constants for switch statements
   private static final int STATE_ROOT             = STATE_CLASSMAP |
													 STATE_TOROOTTABLE;
   private static final int STATE_ROOTCANDIDATE    = STATE_CLASSMAP |
													 STATE_TOROOTTABLE |
													 STATE_CANDIDATEKEY;
   private static final int STATE_CLASSTABLE       = STATE_CLASSMAP |
													 STATE_TOCLASSTABLE;
   private static final int STATE_PROP             = STATE_CLASSMAP |
													 STATE_PROPERTYMAP;
   private static final int STATE_PROPTOCOLUMN     = STATE_CLASSMAP |
													 STATE_PROPERTYMAP |
													 STATE_TOCOLUMN;
   private static final int STATE_PROPTOTABLE      = STATE_CLASSMAP |
													 STATE_PROPERTYMAP |
													 STATE_TOPROPERTYTABLE;
   private static final int STATE_PROPCANDIDATE    = STATE_CLASSMAP |
													 STATE_PROPTOTABLE |
													 STATE_CANDIDATEKEY;
   private static final int STATE_PROPFOREIGN      = STATE_CLASSMAP |
													 STATE_PROPTOTABLE |
													 STATE_FOREIGNKEY;
   private static final int STATE_RELATED          = STATE_CLASSMAP |
													 STATE_RELATEDCLASS;
   private static final int STATE_RELATEDCANDIDATE = STATE_CLASSMAP |
													 STATE_RELATEDCLASS |
													 STATE_CANDIDATEKEY;
   private static final int STATE_RELATEDFOREIGN   = STATE_CLASSMAP |
													 STATE_RELATEDCLASS |
													 STATE_FOREIGNKEY;
   private static final int STATE_PSEUDO           = STATE_IGNOREROOT |
													 STATE_PSEUDOROOT;
   private static final int STATE_PSEUDOCANDIDATE  = STATE_IGNOREROOT |
													 STATE_PSEUDOROOT |
													 STATE_CANDIDATEKEY;

   private static String FULL = "FULL",
						 LONG = "LONG",
						 MEDIUM = "MEDIUM",
						 SHORT = "SHORT";

   private static final int DATE = 1,
							TIME = 2,
							TIMESTAMP = 3;

   //**************************************************************************
   // Variables
   //**************************************************************************

   // Class variables
   private Connection          conn = null;
   private ResultSet           mappedResultSet = null;
   private Parser              parser = null;
   private TokenList           elementTokens;
   private int                 state;
   private TempMap             map;
   private TempClassMap        classMap;
   private TempPropertyMap     propMap;
   private TempRelatedClassMap rootClassMap, relatedMap;
   private Locale              locale;
   private String              datePattern, timePattern, timestampPattern;

   // Debugging variables
//   private int                 indent;
//   private boolean             debug = true;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new MapFactory_MapDocument. */
   public MapFactory_MapDocument()
   {
   }   

   /**
	* Construct a new MapFactory_MapDocument and set the database connection
	* and SAX parser.
	*/
   public MapFactory_MapDocument(Connection conn, Parser parser)
   {
	  this.conn = conn;
	  this.parser = parser;
   }   

   /**
	* Construct a new MapFactory_MapDocument and set the database connection,
	* SAX parser, and result set.
	*
	* <P>A result set is needed only when one of the tables in the mapping
	* document is named "Result Set". It is used to retrieve metadata about
	* the columns in the result set.</P>
	*/
   public MapFactory_MapDocument(Connection conn, ResultSet mappedResultSet, Parser parser)
   {
	  this.conn = conn;
	  this.mappedResultSet = mappedResultSet;
	  this.parser = parser;
   }   

   //**************************************************************************
   // Public methods -- map factory
   //**************************************************************************

   /**
	* Get the current Connection.
	*
	* @return The current Connection.
	*/
   public Connection getConnection()
   {
	  return conn;
   }   

   /**
	* Set the current Connection.
	*
	* @param conn The current Connection.
	*/
   public void setConnection(Connection conn)
   {
	  this.conn = conn;
   }   

   /**
	* Get the current ResultSet.
	*
	* @return The current ResultSet.
	*/
   public ResultSet getResultSet()
   {
	  return mappedResultSet;
   }   

   /**
	* Set the current ResultSet.
	*
	* @param mappedResultSet The current ResultSet.
	*/
   public void setResultSet(ResultSet mappedResultSet)
   {
	  this.mappedResultSet = mappedResultSet;
   }   

   /**
	* Get the current SAX Parser.
	*
	* @return The current SAX Parser.
	*/
   public Parser getParser()
   {
	  return parser;
   }   

   /**
	* Set the current SAX Parser.
	*
	* @param parser The current SAX Parser.
	*/
   public void setParser(Parser parser)
   {
	  this.parser = parser;
   }   

   /**
	* Set the connection, mapped result set (if any), and SAX parser, then
	* create a map from a mapping document.
	*
	* @param conn The current Connection.
	* @param mappedResultSet The current ResultSet. This should be null if
	*  the mapping document does not map an element type-as-class to the
	*  table named "Result Set".
	* @param parser The current SAX Parser.
	* @param src A SAX InputSource for the mapping document.
	* @exception InvalidMapException Thrown if the mapping document contained
	*  a mistake.
	* @exception SQLException Thrown if an error occurred retrieving column
	*  metadata from the database or result set.
	*/
   public Map createMap(Connection conn, ResultSet mappedResultSet, Parser parser, InputSource src)
	  throws InvalidMapException, SQLException
   {
	  setConnection(conn);
	  setResultSet(mappedResultSet);
	  setParser(parser);
	  return createMap(src);
   }   

   /**
	* Create a map from a mapping document.
	*
	* @param src A SAX InputSource for the mapping document.
	* @exception InvalidMapException Thrown if the mapping document contained
	*  a mistake.
	* @exception SQLException Thrown if an error occurred retrieving column
	*  metadata from the database or result set.
	*/
   public Map createMap(InputSource src)
	  throws InvalidMapException, SQLException
   {
	  Map realMap;

	  if ((conn == null) || (parser == null) || (src == null))
		 throw new InvalidMapException("You must set the connection, parser, and input source before creating a Map.");

	  initGlobals();

	  // Parse the map document.

	  try
	  {
		 parser.setDocumentHandler(this);
		 parser.parse(src);
	  }
	  catch (SAXException s)
	  {
		 // If an error occurs, check if it is an InvalidMapException
		 // returned by DocumentHandler by trying to cast it. If so, throw the
		 // InvalidMapException. If not, throw the message from the
		 // SAXException.

		 try
		 {
			throw (InvalidMapException)s.getException();
		 }
		 catch (Exception ex)
		 {
			throw new InvalidMapException("SaxException " +s.getMessage());
		 }
	  }
	  catch (IOException io)
	  {
		throw new InvalidMapException("IO Exception " +io.getMessage()); 
	  }

	  // Convert the temporary maps to real ones.

	  map.createTableMapsFromClassMaps();
	  realMap = map.createMapFromTemp();
	  realMap.setConnection(conn);
	  realMap.initColumnMetadata(mappedResultSet);

	  return realMap;
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
//      if (debug)
//      {
//         System.out.println("Document started.");
//      }

	  // Set up tokens for element and attribute names.
	  elementTokens = new TokenList(XMLDBMSConst.ELEMS, XMLDBMSConst.ELEM_TOKENS, XMLDBMSConst.ELEM_TOKEN_INVALID);
   }   

   /**
	* Implementation of endDocument in SAX' DocumentHandler interface.
	* This method is called by the SAX Parser and should not be called by
	* XML-DBMS programmers.
	*/
   public void endDocument() throws SAXException
   {
	  buildDateFormatters();

//      if (debug)
//      {
//         System.out.println("Document ended.");
//      }
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
//      if (debug)
//      {
//         indent();
//         System.out.println(name + " (start)");
//         indent += 3;
//      }

	  try
	  {
		 switch (elementTokens.getToken(name))
		 {
			case XMLDBMSConst.ELEM_TOKEN_ATTRIBUTE:
			   processAttribute(attrs);
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_CANDIDATEKEY:
			   processCandidateKey(attrs);
			   state = state | STATE_CANDIDATEKEY;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_CLASSMAP:
			   state = state | STATE_CLASSMAP;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_COLUMN:
			   processColumn(attrs);
			   break;

			case XMLDBMSConst.ELEM_TOKEN_ELEMENTTYPE:
			   processElementType(attrs);
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_EMPTYSTRINGISNULL:
			   processEmptyStringIsNull();
			   break;
			case XMLDBMSConst.ELEM_TOKEN_FOREIGNKEY:
			   state = state | STATE_FOREIGNKEY;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_IGNOREROOT:
			   state = state | STATE_IGNOREROOT;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_LOCALE:
			   processLocale(attrs);
			   break;

			case XMLDBMSConst.ELEM_TOKEN_NAMESPACE:
			   processNamespace(attrs);
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_ORDERCOLUMN:
			   processOrderColumn(attrs);
			   break;

			case XMLDBMSConst.ELEM_TOKEN_PASSTHROUGH:
			   throw new SAXException("Pass-through elements are not currently supported.");
   
			case XMLDBMSConst.ELEM_TOKEN_PATTERNS:
			   processPatterns(attrs);
			   break;

			case XMLDBMSConst.ELEM_TOKEN_PCDATA:
			   processPCDATA();
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_PROPERTYMAP:
			   processPropertyMap();
			   state = state | STATE_PROPERTYMAP;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_PSEUDOROOT:
			   processPseudoRoot();
			   state = state | STATE_PSEUDOROOT;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_RELATEDCLASS:
			   processRelatedClass(attrs);
			   state = state | STATE_RELATEDCLASS;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_TABLE:
			   processTable(attrs);
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_TOCLASSTABLE:
			   processToClassTable();
			   state = state | STATE_TOCLASSTABLE;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_TOCOLUMN:
			   processToColumn();
			   state = state | STATE_TOCOLUMN;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_TOPROPERTYTABLE:
			   processToPropertyTable(attrs);
			   state = state | STATE_TOPROPERTYTABLE;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_TOROOTTABLE:
			   processToRootTable();
			   state = state | STATE_TOROOTTABLE;
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_XMLTODBMS:
			   processXMLToDBMS(attrs);
			   break;
   
			case XMLDBMSConst.ELEM_TOKEN_DATETIMEFORMATS:
			case XMLDBMSConst.ELEM_TOKEN_MAPS:
			case XMLDBMSConst.ELEM_TOKEN_OPTIONS:
			   // No end processing for these element types
			   break;

			case XMLDBMSConst.ELEM_TOKEN_INVALID:
			   throw new InvalidMapException("Unrecognized XML-DBMS mapping language element type: " + name);
		 }
	  }
	  catch (InvalidMapException ex)
	  {
		 throw new SAXException(ex);
	  }
catch (Exception e)
{
   e.printStackTrace();
   throw new SAXException(e);
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
//      if (debug)
//      {
//         indent -= 3;
//         indent();
//         System.out.println(name + " (end)");
//      }

	  switch (elementTokens.getToken(name))
	  {
		 case XMLDBMSConst.ELEM_TOKEN_CANDIDATEKEY:
			state = state & (~STATE_CANDIDATEKEY);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_CLASSMAP:
			state = state & (~STATE_CLASSMAP);
			break;
   
		 case XMLDBMSConst.ELEM_TOKEN_FOREIGNKEY:
			state = state & (~STATE_FOREIGNKEY);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_IGNOREROOT:
			state = state & (~STATE_IGNOREROOT);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_PROPERTYMAP:
			state = state & (~STATE_PROPERTYMAP);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_PSEUDOROOT:
			state = state & (~STATE_PSEUDOROOT);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_RELATEDCLASS:
			state = state & (~STATE_RELATEDCLASS);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_TOCLASSTABLE:
			state = state & (~STATE_TOCLASSTABLE);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_TOCOLUMN:
			state = state & (~STATE_TOCOLUMN);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_TOPROPERTYTABLE:
			state = state & (~STATE_TOPROPERTYTABLE);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_TOROOTTABLE:
			state = state & (~STATE_TOROOTTABLE);
			break;

		 case XMLDBMSConst.ELEM_TOKEN_ATTRIBUTE:
		 case XMLDBMSConst.ELEM_TOKEN_COLUMN:
		 case XMLDBMSConst.ELEM_TOKEN_DATETIMEFORMATS:
		 case XMLDBMSConst.ELEM_TOKEN_ELEMENTTYPE:
		 case XMLDBMSConst.ELEM_TOKEN_EMPTYSTRINGISNULL:
		 case XMLDBMSConst.ELEM_TOKEN_LOCALE:
		 case XMLDBMSConst.ELEM_TOKEN_MAPS:
		 case XMLDBMSConst.ELEM_TOKEN_NAMESPACE:
		 case XMLDBMSConst.ELEM_TOKEN_OPTIONS:
		 case XMLDBMSConst.ELEM_TOKEN_ORDERCOLUMN:
		 case XMLDBMSConst.ELEM_TOKEN_PATTERNS:
		 case XMLDBMSConst.ELEM_TOKEN_PCDATA:
		 case XMLDBMSConst.ELEM_TOKEN_TABLE:
		 case XMLDBMSConst.ELEM_TOKEN_XMLTODBMS:
			// No end processing for these element types
			break;
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
	  // None of our elements have character content, so the document is
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
   // Element processing methods
   //**************************************************************************

   void processAttribute(AttributeList attrs) throws InvalidMapException
   {
	  String prefixedAttrName, attrValue;
	  NSName attrName;

	  prefixedAttrName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME, null);
	  attrName = map.getNSName(prefixedAttrName);
	  attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_MULTIVALUED, XMLDBMSConst.DEF_MULTIVALUED);
	  propMap.name = attrName;
	  propMap.multiValued = attrValue.equals(XMLDBMSConst.ENUM_YES);
	  classMap.addAttributePropertyMap(propMap);
   }   

   void processCandidateKey(AttributeList attrs)
   {
	  // Determine whether to generate the candidate key, update the flag that
	  // states whether any keys are generated (which implies whether a key
	  // generator is needed), and set the corresponding key generation flag.

	  boolean generate;
	  String  attrValue;

	  attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_GENERATE, null);
	  generate = attrValue.equals(XMLDBMSConst.ENUM_YES);
	  map.generateKeys = map.generateKeys || generate;

	  switch (state)
	  {
		 case STATE_ROOT:
			rootClassMap.linkInfo = new TempLinkInfo();
			rootClassMap.linkInfo.generateKey = generate;
			break;

		 case STATE_PROPTOTABLE:
			propMap.linkInfo.generateKey = generate;
			break;

		 case STATE_RELATED:
		 case STATE_PSEUDO:
			relatedMap.linkInfo.generateKey = generate;
			break;
	  }
   }   

   void processColumn(AttributeList attrs) throws InvalidMapException
   {
	  TempColumn column;
	  String     columnName;

	  columnName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME, null);

	  switch (state)
	  {
		 case STATE_ROOTCANDIDATE:
			column = classMap.table.getColumn(columnName);
			rootClassMap.linkInfo.childKey.addElement(column);
			break;

		 case STATE_PROPTOCOLUMN:
			column = classMap.table.mapPropertyColumn(columnName);
			propMap.column = column;
			break;

		 case STATE_PROPTOTABLE:
			column = propMap.table.mapPropertyColumn(columnName);
			propMap.column = column;
			break;

		 case STATE_PROPCANDIDATE:
			if (propMap.linkInfo.parentKeyIsCandidate)
			{
			   column = classMap.table.getColumn(columnName);
			   propMap.linkInfo.parentKey.addElement(column);
			}
			else
			{
			   column = propMap.table.getColumn(columnName);
			   propMap.linkInfo.childKey.addElement(column);
			}
			break;

		 case STATE_PROPFOREIGN:
			if (propMap.linkInfo.parentKeyIsCandidate)
			{
			   column = propMap.table.getColumn(columnName);
			   propMap.linkInfo.childKey.addElement(column);
			}
			else
			{
			   column = classMap.table.getColumn(columnName);
			   propMap.linkInfo.parentKey.addElement(column);
			}
			break;

		 case STATE_RELATEDCANDIDATE:
		 case STATE_PSEUDOCANDIDATE:
			if (relatedMap.linkInfo.parentKeyIsCandidate)
			{
			   column = classMap.table.getColumn(columnName);
			   relatedMap.linkInfo.parentKey.addElement(column);
			}
			else
			{
			   if (relatedMap.classMap.table == null)
			   {
				  relatedMap.classMap.table = map.getTempClassTable(relatedMap.classMap.name.qualified);
			   }
			   column = relatedMap.classMap.table.getColumn(columnName);
			   relatedMap.linkInfo.childKey.addElement(column);
			}
			break;

		 case STATE_RELATEDFOREIGN:
			if (relatedMap.linkInfo.parentKeyIsCandidate)
			{
			   if (relatedMap.classMap.table == null)
			   {
				  relatedMap.classMap.table = map.getTempClassTable(relatedMap.classMap.name.qualified);
			   }
			   column = relatedMap.classMap.table.getColumn(columnName);
			   relatedMap.linkInfo.childKey.addElement(column);
			}
			else
			{
			   column = classMap.table.getColumn(columnName);
			   relatedMap.linkInfo.parentKey.addElement(column);
			}
			break;
	  }
   }   

   void processElementType(AttributeList attrs) throws InvalidMapException
   {
	  String prefixedElementType;
	  NSName elementType;

	  prefixedElementType = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME, null);
	  elementType = map.getNSName(prefixedElementType);

	  switch (state)
	  {
		 case STATE_CLASSMAP:
			// Add a TempClassMap for the element type.

			classMap = map.addTempClassMap(elementType);
			break;

		 case STATE_PROP:
			// Add the property map to the list of elements-as-properties
			// for the class.

			propMap.name = elementType;
			classMap.addElementPropertyMap(propMap);
			break;

		 case STATE_RELATED:
		 case STATE_PSEUDO:
			// Set the TempClassMap for the related class and add it to
			// the list of related classes for the class.

			relatedMap.classMap = map.getTempClassMap(elementType);
			classMap.addRelatedClassMap(relatedMap);
			break;

		 case STATE_IGNOREROOT:
			// Add a TempClassMap for the element type, as well as a
			// TempRootClassMap.
			classMap = map.addTempClassMap(elementType);
			classMap.type = ClassMap.TYPE_IGNOREROOT;
			rootClassMap = map.addTempRootClassMap(classMap);
			break;
	  }
   }   

   void processEmptyStringIsNull()
   {
	 map.emptyStringIsNull = true;
   }   

   void processLocale(AttributeList attrs)
   {
	  String country, language;

	  country = getAttrValue(attrs, XMLDBMSConst.ATTR_COUNTRY, null);
	  language = getAttrValue(attrs, XMLDBMSConst.ATTR_LANGUAGE, null);
	  locale = new Locale(language, country);
   }   

   void processNamespace(AttributeList attrs) throws InvalidMapException
   {
	  String uri, prefix;

	  prefix = getAttrValue(attrs, XMLDBMSConst.ATTR_PREFIX, null);
	  uri = getAttrValue(attrs, XMLDBMSConst.ATTR_URI, null);
	  map.addNamespace(prefix, uri);
   }   

   void processOrderColumn(AttributeList attrs)
	  throws InvalidMapException
   {
	  String     columnName, attrValue;
	  boolean    generate;
	  TempColumn column;

	  columnName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME, null);
	  attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_GENERATE, null);
	  generate = attrValue.equals(XMLDBMSConst.ENUM_YES);

	  switch (state)
	  {
		 case STATE_ROOT:
			// Order column is in the root table.
			column = rootClassMap.classMap.table.getColumn(columnName);
			rootClassMap.orderInfo.orderColumn = column;
			rootClassMap.orderInfo.generateOrder = generate;
			break;

		 case STATE_PROP:
			if (propMap.type == PropertyMap.TYPE_TOCOLUMN)
			{
			   // Order column is parallel to the property column in the
			   // class table.
			   column = classMap.table.getColumn(columnName);
			   propMap.orderInfo.orderColumn = column;
			   propMap.orderInfo.generateOrder = generate;
			}
			else // if (propMap.type == PropertyMap.TYPE_TOPROPERTYTABLE)
			{
			   // Order column is in table of foreign key.
			   if (propMap.linkInfo.parentKeyIsCandidate)
			   {
				  column = propMap.table.getColumn(columnName);
			   }
			   else
			   {
				  column = classMap.table.getColumn(columnName);
			   }
			   propMap.orderInfo.orderColumn = column;
			   propMap.orderInfo.generateOrder = generate;
			}
			break;

		 case STATE_RELATED:
		 case STATE_PSEUDO:
			// Order column is in table of foreign key.
			if (relatedMap.linkInfo.parentKeyIsCandidate)
			{
			   if (relatedMap.classMap.table == null)
			   {
				  relatedMap.classMap.table = map.getTempClassTable(classMap.name.qualified);
			   }
			   column = relatedMap.classMap.table.getColumn(columnName);
			}
			else
			{
			   column = classMap.table.getColumn(columnName);
			}
			relatedMap.orderInfo.orderColumn = column;
			relatedMap.orderInfo.generateOrder = generate;
			break;
	  }
   }   

   void processPatterns(AttributeList attrs)
   {
	  String date, time, timestamp;

	  datePattern = getAttrValue(attrs, XMLDBMSConst.ATTR_DATE, null);
	  timePattern = getAttrValue(attrs, XMLDBMSConst.ATTR_TIME, null);
	  timestampPattern = getAttrValue(attrs, XMLDBMSConst.ATTR_TIMESTAMP, null);
   }   

   void processPCDATA() throws InvalidMapException
   {
	  // Add the property map and set its property to a null NSName.
	  classMap.addPCDATAPropertyMap(propMap);
	  propMap.name = new NSName();
   }   

   void processPropertyMap()
   {
	  propMap = new TempPropertyMap();
   }   

   void processPseudoRoot()
   {
	  // Pseudo roots are very closely related to related classes. The only
	  // real difference is that they have never have a parent key, since they
	  // are used only when the parent element is the root and is ignored.

	  relatedMap = new TempRelatedClassMap();
	  relatedMap.linkInfo.parentKeyIsCandidate = false;
   }   

   void processRelatedClass(AttributeList attrs)
   {
	  String attrValue;

	  attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_KEYINPARENTTABLE, null);
	  relatedMap = new TempRelatedClassMap();
	  relatedMap.linkInfo.parentKeyIsCandidate = attrValue.equals(XMLDBMSConst.ENUM_CANDIDATE);
   }   

   void processTable(AttributeList attrs)
	  throws InvalidMapException
   {
	  String tableName;

	  tableName = getAttrValue(attrs, XMLDBMSConst.ATTR_NAME, null);

	  switch (state)
	  {
		 case STATE_ROOT:
		 case STATE_CLASSTABLE:
			classMap.table = map.addTempClassTable(classMap.name.qualified, tableName);
			break;

		 case STATE_PROPTOTABLE:
			propMap.table = map.addTempPropertyTable(tableName);
			break;
	  }
   }   

   void processToClassTable()
   {
	  classMap.type = ClassMap.TYPE_TOCLASSTABLE;
   }   

   void processToColumn()
   {
	  propMap.type = PropertyMap.TYPE_TOCOLUMN;
   }   

   void processToPropertyTable(AttributeList attrs)
   {
	  String attrValue;

	  attrValue = getAttrValue(attrs, XMLDBMSConst.ATTR_KEYINPARENTTABLE, null);
	  propMap.type = PropertyMap.TYPE_TOPROPERTYTABLE;
	  propMap.linkInfo = new TempLinkInfo();
	  propMap.linkInfo.parentKeyIsCandidate = attrValue.equals(XMLDBMSConst.ENUM_CANDIDATE);
   }   

   void processToRootTable()
   {
	  // Add a new TempRootClassMap for the element type and set the parent key
	  // type for it.
	  rootClassMap = map.addTempRootClassMap(classMap);
	  rootClassMap.linkInfo = new TempLinkInfo();
	  rootClassMap.linkInfo.parentKeyIsCandidate = false;

	  // Set the class map type as TOROOTTABLE
	  classMap.type = ClassMap.TYPE_TOROOTTABLE;
   }   

   void processXMLToDBMS(AttributeList attrs) throws InvalidMapException
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
		 throw new InvalidMapException("Unsupported XML-DBMS version: " + version);
   }   

   //**************************************************************************
   // Private methods -- miscellaneous
   //**************************************************************************

   private void initGlobals()
   {
	  // Initialize global variables.

	  state = STATE_NONE;
	  map = new TempMap();
	  locale = null;
	  datePattern = null;
	  timePattern = null;
	  timestampPattern = null;
//      indent = 0;
   }   

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

	  attrValue = attrs.getValue(name);

	  // On null or zero-length attribute value, return the default.
	  //
	  // The check for zero-length attributes is a workaround for
	  // parsers that incorrectly return an empty string instead of
	  // a null when the attribute is not found. This is a safe check
	  // to make as all mapping language attributes, if present, must
	  // have a non-zero length.

	  if (attrValue == null) return defaultValue;
	  else if (attrValue.length() == 0) return defaultValue;
	  else return attrValue;
   }   

   private void buildDateFormatters()
   {
	  map.dateFormatter = buildDateFormatter(locale, datePattern, DATE);
	  map.timeFormatter = buildDateFormatter(locale, timePattern, TIME);
	  map.timestampFormatter = buildDateFormatter(locale, timestampPattern, TIMESTAMP);
   }   

   private DateFormat buildDateFormatter(Locale locale, String pattern, int type)
   {
	  int style;

	  if (pattern == null)
	  {
		 switch (type)
		 {
			case DATE:
			   return DateFormat.getDateInstance();

			case TIME:
			   return DateFormat.getTimeInstance();

			case TIMESTAMP:
			   return DateFormat.getDateTimeInstance();
		 }
	  }

	  style = getStyle(pattern);
	  if (style != DateFormat.DEFAULT)
	  {
		 switch (type)
		 {
			case DATE:
			   return (locale == null) ? DateFormat.getDateInstance(style) :
										 DateFormat.getDateInstance(style, locale);

			case TIME:
			   return (locale == null) ? DateFormat.getTimeInstance(style) :
										 DateFormat.getTimeInstance(style, locale);

			case TIMESTAMP:
			   return (locale == null) ? DateFormat.getDateTimeInstance(style, style) :
										 DateFormat.getDateTimeInstance(style, style, locale);
		 }
	  }
	  return (locale == null) ? new SimpleDateFormat(pattern) :
								new SimpleDateFormat(pattern, locale);
   }   

   private int getStyle(String pattern)
   {
	  if (pattern.equals(FULL))
	  {
		 return DateFormat.FULL;
	  }
	  else if (pattern.equals(LONG))
	  {
		 return DateFormat.LONG;
	  }
	  else if (pattern.equals(MEDIUM))
	  {
		 return DateFormat.MEDIUM;
	  }
	  else if (pattern.equals(SHORT))
	  {
		 return DateFormat.SHORT;
	  }
	  else
	  {
		 return DateFormat.DEFAULT;
	  }
   }   

   //**************************************************************************
   // Private methods -- debugging
   //**************************************************************************

//   private void indent()
//   {
//      for (int i = 0; i < indent; i++)
//      {
//         System.out.print(" ");
//      }
//   }
//**************************************************************************
   // Private methods -- debugging
   //**************************************************************************

//   private void indent()
//   {
//      for (int i = 0; i < indent; i++)
//      {
//         System.out.print(" ");
//      }
//   }
}