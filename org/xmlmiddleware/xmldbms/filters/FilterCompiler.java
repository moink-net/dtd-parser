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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.xmldbms.filters;

import org.xmlmiddleware.utils.TokenList;
import org.xmlmiddleware.utils.XMLName;
import org.xmlmiddleware.xmldbms.maps.Map;

import java.io.IOException;
import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Compiles a filter document into a FilterSet object.
 *
 * <p>Filter documents specify the following items:</p>
 *
 * <ul>
 * <li><b>Wrapper elements</b> These elements wrap the result.</li>
 * <li><b>Root filters</b> These specify the conditions for retrieving
 * data from the root table(s).</li>
 * <li><b>Result set info</b> Specifies the class table to which the result
 * set corresponds.</li>
 * <li><b>Table filters</b> Specifies the conditions used to retrieve data
 * from the child tables of a class table.</li>
 * </ul>
 *
 * <p>For more information, see filters.dtd.</p>
 *
 * <p>FilterCompiler assumes that the filter document is valid. If
 * it is not, the class will either generate garbage or throw an exception. One
 * way to guarantee that the document is valid is to pass a validating parser
 * to the filter compiler.</p>
 * 
 * <p>For example, the following code creates a FilterSet object from the
 * SalesFilter.ftr filter document.</p>
 *
 * <pre>
 *    // Instantiate a new filter compiler and set the XMLReader.
 *    compiler = new FilterCompiler(xmlReader);<br />
 *
 *    // Compile SalesFilter.ftr into a FilterSet object.
 *    filterSet = compiler.compile(new InputSource(new FileReader("SalesFilter.ftr")));<br />
 * </pre>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class FilterCompiler implements ContentHandler
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private XMLReader        xmlReader;
   private TokenList        elementTokens;
   private FilterSet        filterSet;
   private RootFilter       rootFilter;
   private FilterBase       filterBase;
   private TableFilter      tableFilter;
   private FilterConditions relatedTableFilter, rootFilterConditions;
   private String           parentKeyName, childKeyName;
   private int              state;

   // Debugging variables
   private int       indent = 0;
   private boolean   debug = false;

   //*********************************************************************
   // Constants
   //*********************************************************************

   // The state gives context for elements that can occur inside more than
   // one element or that have constraints that cannot be expressed in the
   // DTD. The following table shows which states affect the processing of
   // which element types:
   //
   // State                    Affected element types
   // ------------------       -----------------------------------------------------
   // STATE_ROOTFILTER         Table, Where
   // STATE_TABLEFILTER        Table
   // STATE_RELATEDTABLEFILTER Table, Where

   private static final int STATE_INITIAL = 0;
   private static final int STATE_ROOTFILTER = 1;
   private static final int STATE_TABLEFILTER = 2;
   private static final int STATE_RELATEDTABLEFILTER = 3;

   private static String NAMESPACES = "http://xml.org/sax/features/namespaces";

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new FilterCompiler and set the XMLReader (parser).
    *
    * @param xmlReader The XMLReader
    */
   public FilterCompiler(XMLReader xmlReader)
      throws SAXException
   {
      if (xmlReader == null)
         throw new IllegalArgumentException("xmlReader argument must not be null.");
      this.xmlReader = xmlReader;
      this.xmlReader.setContentHandler(this);
      this.xmlReader.setFeature(NAMESPACES, true);
      initTokens();
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   /**
    * Compile a filter document into a FilterSet object.
    *
    * @param src A SAX InputSource for the filter document.
    * @param map The map to which the filters apply.
    * @return The FilterSet object
    * @exception SAXException Thrown if the filter document contains an error.
    * @exception IOException Thrown if an IO error occurs.
    */
   public FilterSet compile(Map map, InputSource src)
      throws SAXException, IOException
   {
      // Check the arguments.

      if ((src == null) || (map == null))
         throw new IllegalArgumentException("map and src arguments must not be null.");


      // Initialize global variables.

      state = STATE_INITIAL;
      filterSet = new FilterSet(map);

      // Parse the filter document.

      xmlReader.parse(src);

      // Return the FilterSet object.

      return filterSet;
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
   }

   /** For internal use only. */
   public void endDocument() throws SAXException
   {
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

      if (!uri.equals(FilterConst.URI_FILTERSV2))
         throw new SAXException("Unrecognized namespace URI for filter language: " + uri);

      switch (elementTokens.getToken(localName))
      {
         case FilterConst.ELEM_TOKEN_FILTERSET:
            processFilterSet(attrs);
            break;

         case FilterConst.ELEM_TOKEN_NAMESPACE:
            processNamespace(attrs);
            break;

         case FilterConst.ELEM_TOKEN_RELATEDTABLEFILTER:
            state = STATE_RELATEDTABLEFILTER;
            processRelatedTableFilter(attrs);
            break;

         case FilterConst.ELEM_TOKEN_RESULTSETINFO:
            processResultSetInfo(attrs);
            break;

         case FilterConst.ELEM_TOKEN_ROOTFILTER:
            state = STATE_ROOTFILTER;
            processRootFilter();
            break;

         case FilterConst.ELEM_TOKEN_TABLE:
            processTable(attrs);
            break;

         case FilterConst.ELEM_TOKEN_TABLEFILTER:
            state = STATE_TABLEFILTER;
            break;

         case FilterConst.ELEM_TOKEN_WHERE:
            processWhere(attrs);
            break;

         case FilterConst.ELEM_TOKEN_WRAPPER:
            processWrapper(attrs);
            break;

         case FilterConst.ELEM_TOKEN_FILTER:
         case FilterConst.ELEM_TOKEN_FILTERS:
         case FilterConst.ELEM_TOKEN_OPTIONS:
            // do nothing
            break;

         case FilterConst.ELEM_TOKEN_INVALID:
            throw new SAXException("Unrecognized filter language element type: " + localName);
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

      if (!uri.equals(FilterConst.URI_FILTERSV2))
         throw new SAXException("Unrecognized namespace URI for filter language: " + uri);

      switch (elementTokens.getToken(localName))
      {

         case FilterConst.ELEM_TOKEN_RELATEDTABLEFILTER:
            state = STATE_TABLEFILTER;
            break;

         case FilterConst.ELEM_TOKEN_ROOTFILTER:
            state = STATE_INITIAL;
            break;

         case FilterConst.ELEM_TOKEN_TABLEFILTER:
            state = STATE_INITIAL;
            break;

         case FilterConst.ELEM_TOKEN_FILTER:
         case FilterConst.ELEM_TOKEN_FILTERS:
         case FilterConst.ELEM_TOKEN_FILTERSET:
         case FilterConst.ELEM_TOKEN_NAMESPACE:
         case FilterConst.ELEM_TOKEN_OPTIONS:
         case FilterConst.ELEM_TOKEN_RESULTSETINFO:
         case FilterConst.ELEM_TOKEN_TABLE:
         case FilterConst.ELEM_TOKEN_WHERE:
         case FilterConst.ELEM_TOKEN_WRAPPER:
            // Nothing to do.
            break;

         case FilterConst.ELEM_TOKEN_INVALID:
            throw new SAXException("Unrecognized filter language element type: " + localName);
      }
   }

   /** For internal use only. */
   public void characters (char ch[], int start, int length)
     throws SAXException
   {
      // No filter language elements have character content, so the document is
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
   // Private methods -- element processing methods, in alphabetical order
   //**************************************************************************

   private void processFilterSet(Attributes attrs)
      throws SAXException
   {
      String version;

      // Check the version number. Since we can't count on a validating
      // parser, if the attribute doesn't exist, we assume the version
      // is correct.

      version = getAttrValue(attrs, FilterConst.ATTR_VERSION, FilterConst.DEF_VERSION);
      if (!version.equals(FilterConst.DEF_VERSION))
         throw new SAXException("Unsupported filter language version: " + version);
   }

   private void processNamespace(Attributes attrs)
   {
      String uri, prefix;

      prefix = getAttrValue(attrs, FilterConst.ATTR_PREFIX);
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
      uri = getAttrValue(attrs, FilterConst.ATTR_URI);

      filterSet.addNamespace(prefix, uri);
   }

   private void processRelatedTableFilter(Attributes attrs)
   {
      parentKeyName = getAttrValue(attrs, FilterConst.ATTR_PARENTKEY);
      childKeyName = getAttrValue(attrs, FilterConst.ATTR_CHILDKEY);
   }

   private void processResultSetInfo(Attributes attrs)
      throws SAXException
   {
      String          databaseName, catalogName, schemaName, tableName;
      ResultSetFilter resultSetFilter;

      databaseName = getAttrValue(attrs, FilterConst.ATTR_DATABASE, FilterConst.DEF_DATABASE);
      catalogName = getAttrValue(attrs, FilterConst.ATTR_CATALOG);
      schemaName = getAttrValue(attrs, FilterConst.ATTR_SCHEMA);
      tableName = getAttrValue(attrs, FilterConst.ATTR_TABLE);

      try
      {
         resultSetFilter = filterSet.createResultSetFilter();
         resultSetFilter.setTable(databaseName, catalogName, schemaName, tableName);
         filterBase = resultSetFilter;
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }

   private void processRootFilter()
   {
      try
      {
         rootFilter = filterSet.createRootFilter();
         filterBase = rootFilter;
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }

   private void processTable(Attributes attrs)
      throws SAXException
   {
      String databaseName, catalogName, schemaName, tableName;

      databaseName = getAttrValue(attrs, FilterConst.ATTR_DATABASE, FilterConst.DEF_DATABASE);
      catalogName = getAttrValue(attrs, FilterConst.ATTR_CATALOG);
      schemaName = getAttrValue(attrs, FilterConst.ATTR_SCHEMA);
      tableName = getAttrValue(attrs, FilterConst.ATTR_NAME);

      try
      {
         switch (state)
         {
            case STATE_RELATEDTABLEFILTER:
               relatedTableFilter = tableFilter.createRelatedTableFilter(databaseName, catalogName, schemaName, tableName, parentKeyName, childKeyName);
               break;

            case STATE_ROOTFILTER:
               rootFilterConditions = rootFilter.createRootFilterConditions(databaseName, catalogName, schemaName, tableName);
               break;

            case STATE_TABLEFILTER:
               tableFilter = filterBase.createTableFilter(databaseName, catalogName, schemaName, tableName);
               break;

            default:
               throw new SAXException("Programming error. Invalid state.");
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new SAXException(e.getMessage());
      }
   }

   private void processWhere(Attributes attrs)
      throws SAXException
   {
      String condition;

      condition = getAttrValue(attrs, FilterConst.ATTR_CONDITION);

      switch (state)
      {
         case STATE_RELATEDTABLEFILTER:
            relatedTableFilter.addCondition(condition);
            break;

         case STATE_ROOTFILTER:
            rootFilterConditions.addCondition(condition);
            break;

         default:
            throw new SAXException("Programming error. Invalid state.");
      }
   }

   private void processWrapper(Attributes attrs)
      throws SAXException
   {
      String  qualifiedName;
      XMLName wrapperName;

      // Get the qualified element type name and convert it to an XMLName.

      qualifiedName = getAttrValue(attrs, FilterConst.ATTR_NAME);

      wrapperName = XMLName.create(qualifiedName, filterSet.getNamespaceURIs());
      filterSet.addWrapperName(wrapperName, 0);
   }

   //**************************************************************************
   // Private methods -- miscellaneous
   //**************************************************************************

   private void initTokens()
   {
      // Set up tokens for element names and enumerated values.

      elementTokens = new TokenList(FilterConst.ELEMS, FilterConst.ELEM_TOKENS, FilterConst.ELEM_TOKEN_INVALID);
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
      String attrValue;

      // Get the attribute value.

      attrValue = getAttrValue(attrs, name);

      // On null attribute value, return the default.

      return (attrValue == null) ? defaultValue : attrValue;
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
}