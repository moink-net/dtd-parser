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
// Changes from version 1.0: None
// Changes from version 1.01: None
// Changes from version 1.1:
// * Changed name and wrote parse method. (This previously just threw an exception.)

package org.xmlmiddleware.domutils;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * Creates a DocumentFragment from a string whose value is well-formed XML.
 *
 * <p>The root element of the XML is ignored; its children become the children
 * of the DocumentFragment.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class FragmentBuilder implements ContentHandler
{
   // ************************************************************************
   // Class variables
   // ************************************************************************

   XMLReader        xmlReader;
   Document         doc;
   DocumentFragment fragment;
   Node             current;

   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String NAMESPACES = "http://xml.org/sax/features/namespaces";
   private static String PREFIXES = "http://xml.org/sax/features/namespace-prefixes"; 

   // ************************************************************************
   // Constructors
   // ************************************************************************

   public FragmentBuilder(XMLReader xmlReader)
      throws SAXNotRecognizedException, SAXNotSupportedException
   {
      this.xmlReader = xmlReader;
      xmlReader.setContentHandler(this);
      xmlReader.setFeature(NAMESPACES, true);
      xmlReader.setFeature(PREFIXES, true);
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   public DocumentFragment parse(Document doc, String xml)
      throws SAXException, IOException
   {
      this.doc = doc;
      fragment = doc.createDocumentFragment();
      current = fragment;
      xmlReader.parse(new InputSource(new StringReader(xml)));
      return fragment;
   }

   // ************************************************************************
   // Public methods -- SAX
   // ************************************************************************

   public void startDocument () throws SAXException
   {
   }

   public void endDocument() throws SAXException
   {
   }

   public void startElement (String uri, String localName, String qName, Attributes attrs)
      throws SAXException
   {
      Element element;
      String  attrURI, attrQName, attrValue;

      // Since we are constructing a fragment, ignore the root element.

      if (current == fragment) return;

      // Construct an element.

      element = doc.createElementNS(uri, qName);
      current.appendChild(element);
      current = element;

      // Process the attributes.

      for (int i = 0; i < attrs.getLength(); i++)
      {
         attrURI = attrs.getURI(i);
         attrQName = attrs.getQName(i);
         attrValue = attrs.getValue(i);
         element.setAttributeNS(attrURI, attrQName, attrValue);
      }
   }

   public void endElement (String uri, String localName, String qName) throws SAXException
   {
      current = current.getParentNode();
   }

   public void characters (char ch[], int start, int length)
      throws SAXException
   {
      Text text;

      text = doc.createTextNode(new String(ch, start, length));
      current.appendChild(text);
   }
   
   public void ignorableWhitespace (char ch[], int start, int length)
      throws SAXException
   {
      characters(ch, start, length);
   }

   public void processingInstruction (String target, String data)
      throws SAXException
   {
      ProcessingInstruction pi;

      pi = doc.createProcessingInstruction(target, data);
      current.appendChild(pi);
   }

   public void startPrefixMapping(String prefix, String uri)
      throws SAXException
   {
   }

   public void endPrefixMapping(String prefix)
      throws SAXException
   {
   }

   public void setDocumentLocator (Locator locator)
   {
   }

   public void skippedEntity(String name)
      throws SAXException
   {
   }
}