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

package org.xmlmiddleware.xmldbms.actions;

import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmlutils.*;

import java.io.*;
import java.util.*;

import org.xml.sax.*;

/**
 * Compiles an action document into an Actions object.
 *
 * <p>Action documents specify a default action for a document, as well
 * as per-class (per element type) actions. For more information, see
 * actions.dtd.</p>
 *
 * <p>ActionCompiler assumes that the action document is valid. If
 * it is not, the class will either generate garbage or throw an exception. One
 * way to guarantee that the document is valid is to pass a validating parser
 * to the action compiler.</p>
 * 
 * <p>For example, the following code creates an Actions object from the
 * sales.act action document.</p>
 *
 * <pre>
 *    // Instantiate a new action compiler and set the XMLReader.
 *    compiler = new ActionCompiler(xmlReader);<br />
 *    <br />
 *    // Compile sales.act into an Actions object.
 *    actions = compiler.compile(map, new InputSource(new FileReader("sales.act")));<br />
 * </pre>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ActionCompiler implements ContentHandler
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private XMLReader xmlReader;
   private TokenList elementTokens;
   private Actions   actions;
   private Action    action;
   private boolean   stateDefaultAction, stateUpdate;
   private Hashtable uris = new Hashtable(); // Indexed by prefix

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
   // State                  Affected element types
   // ------------------     -----------------------------------------------------
   // stateDefaultAction     ElementType, Attribute, PCDATA
   // stateUpdate            ElementType

   private static String NAMESPACES = "http://xml.org/sax/features/namespaces";

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new ActionCompiler and set the XMLReader (parser).
    *
    * @param xmlReader The XMLReader
    * @exception SAXException Thrown if the XMLReader doesn't support namespaces.
    */
   public ActionCompiler(XMLReader xmlReader)
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
    * Compile an action document into an Actions object.
    *
    * @param map The map to which the actions apply.
    * @param src A SAX InputSource for the action document.
    * @return The Actions object
    * @exception XMLMiddlewareException Thrown if the action document contains an error.
    */
   public Actions compile(XMLDBMSMap map, InputSource src)
      throws XMLMiddlewareException
   {
      Exception e;

      // Check the arguments.

      if ((src == null) || (map == null))
         throw new IllegalArgumentException("map and src arguments must not be null.");


      // Initialize global variables.

      stateDefaultAction = false;
      stateUpdate = false;
      uris.clear();
      actions = new Actions(map);

      // Parse the actions document.

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

      // Return the Actions object.

      return actions;
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

      if (!uri.equals(ActionConst.URI_ACTIONSV2))
         throw new SAXException("Unrecognized namespace URI for action language: " + uri);

      try
      {
         switch (elementTokens.getToken(localName))
         {
            case ActionConst.ELEM_TOKEN_ACTION:
               // do nothing
               break;

            case ActionConst.ELEM_TOKEN_ACTIONS:
               processActions(attrs);
               break;

            case ActionConst.ELEM_TOKEN_ALL:
               throw new SAXException("<All> not yet implemented.");
//               break;

            case ActionConst.ELEM_TOKEN_ATTRIBUTE:
               processAttribute(attrs);
               break;

            case ActionConst.ELEM_TOKEN_DEFAULTACTION:
               processDefaultAction();
               stateDefaultAction = true;
               break;

            case ActionConst.ELEM_TOKEN_DELETE:
               processAction(Action.DELETE);
               break;

            case ActionConst.ELEM_TOKEN_ELEMENTTYPE:
               processElementType(attrs);
               break;

            case ActionConst.ELEM_TOKEN_INSERT:
               processAction(Action.INSERT);
               break;

            case ActionConst.ELEM_TOKEN_NAMESPACE:
               processNamespace(attrs);
               break;

            case ActionConst.ELEM_TOKEN_NONE:
               processAction(Action.NONE);
               break;

            case ActionConst.ELEM_TOKEN_OPTIONS:
               // do nothing
               break;

            case ActionConst.ELEM_TOKEN_PCDATA:
               processUpdateProperty(null, PropertyMap.PCDATA);
               break;

            case ActionConst.ELEM_TOKEN_SOFTDELETE:
               processAction(Action.SOFTDELETE);
               break;

            case ActionConst.ELEM_TOKEN_SOFTINSERT:
               processAction(Action.SOFTINSERT);
               break;

            case ActionConst.ELEM_TOKEN_UPDATE:
               processAction(Action.UPDATE);
               stateUpdate = true;
               break;

            case ActionConst.ELEM_TOKEN_UPDATEORINSERT:
               processAction(Action.UPDATEORINSERT);
               break;

            case ActionConst.ELEM_TOKEN_INVALID:
               throw new XMLMiddlewareException("Unrecognized action language element type: " + localName);
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

      if (!uri.equals(ActionConst.URI_ACTIONSV2))
         throw new SAXException("Unrecognized namespace URI for action language: " + uri);

      try
      {
         switch (elementTokens.getToken(localName))
         {
            case ActionConst.ELEM_TOKEN_DEFAULTACTION:
               stateDefaultAction = false;
               break;

            case ActionConst.ELEM_TOKEN_UPDATE:
               stateUpdate = false;
               break;

            case ActionConst.ELEM_TOKEN_ACTION:
            case ActionConst.ELEM_TOKEN_ACTIONS:
            case ActionConst.ELEM_TOKEN_ALL:
            case ActionConst.ELEM_TOKEN_ATTRIBUTE:
            case ActionConst.ELEM_TOKEN_DELETE:
            case ActionConst.ELEM_TOKEN_ELEMENTTYPE:
            case ActionConst.ELEM_TOKEN_INSERT:
            case ActionConst.ELEM_TOKEN_NAMESPACE:
            case ActionConst.ELEM_TOKEN_NONE:
            case ActionConst.ELEM_TOKEN_OPTIONS:
            case ActionConst.ELEM_TOKEN_PCDATA:
            case ActionConst.ELEM_TOKEN_SOFTDELETE:
            case ActionConst.ELEM_TOKEN_SOFTINSERT:
            case ActionConst.ELEM_TOKEN_UPDATEORINSERT:
               // Nothing to do.
               break;

            case ActionConst.ELEM_TOKEN_INVALID:
               throw new XMLMiddlewareException("Unrecognized action language element type: " + localName);
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
      // No action language elements have character content, so the document is
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

   private void processAction(int actionType)
   {
      action.setAction(actionType);
   }

   private void processActions(Attributes attrs)
      throws XMLMiddlewareException
   {
      String version;

      // Check the version number. Since we can't count on a validating
      // parser, if the attribute doesn't exist, we assume the version
      // is correct.

      version = getAttrValue(attrs, ActionConst.ATTR_VERSION, ActionConst.DEF_VERSION);
      if (!version.equals(ActionConst.DEF_VERSION))
         throw new XMLMiddlewareException("Unsupported action language version: " + version);
   }

   private void processAttribute(Attributes attrs)
      throws XMLMiddlewareException
   {
      String qname;

      qname = getAttrValue(attrs, ActionConst.ATTR_NAME);
      processUpdateProperty(XMLName.create(qname, uris), PropertyMap.ATTRIBUTE);
   }

   private void processDefaultAction()
   {
      action = new Action();
      actions.setDefaultAction(action);
   }

   private void processElementType(Attributes attrs)
      throws XMLMiddlewareException
   {
      String qname;

      qname = getAttrValue(attrs, ActionConst.ATTR_NAME);

      if (!stateUpdate)
      {
         // The <ElementType> element is inside an <Action> element

         action = actions.createAction(XMLName.create(qname, uris));
      }
      else
      {
         processUpdateProperty(XMLName.create(qname, uris), PropertyMap.ELEMENTTYPE);
      }
   }

   private void processNamespace(Attributes attrs)
   {
      String uri, prefix;

      prefix = getAttrValue(attrs, ActionConst.ATTR_PREFIX);
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
      uri = getAttrValue(attrs, ActionConst.ATTR_URI);

      uris.put(prefix, uri);
   }

   private void processUpdateProperty(XMLName propName, int type)
      throws XMLMiddlewareException
   {
      // The <ElementType>, <Attribute>, or <PCDATA> element is inside
      // an <Update> element.

      if (stateDefaultAction)
         throw new XMLMiddlewareException("When <Update> is inside <DefaultAction>, the only valid child is <All />.");

      action.setUpdateProperty(propName, type);
   }


   //**************************************************************************
   // Private methods -- miscellaneous
   //**************************************************************************

   private void initTokens()
   {
      // Set up tokens for element names and enumerated values.

      elementTokens = new TokenList(ActionConst.ELEMS, ActionConst.ELEM_TOKENS, ActionConst.ELEM_TOKEN_INVALID);
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
