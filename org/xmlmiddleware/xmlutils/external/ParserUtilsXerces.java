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
// Changes from version 1.01: New in 1.1
// Changes from version 1.1:
// * Moved to xmlutils.helpers package
// * Uses 2.0 ParserUtils

package org.xmlmiddleware.xmlutils.external;

import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmlutils.*;

import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.*;

// Imports for the Xerces parser

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

/**
 * Implements ParserUtils for the Xerces parser.
 *
 * <p>Supports any(?) version of Xerces that supports JAXP.</p>
 *
 * @author Adam Flinton
 * @author Paul Gubbay
 * @version 2.0
 */

public class ParserUtilsXerces implements ParserUtils
{
   // ***********************************************************************
   // Constructors
   // ***********************************************************************

   /**
   * Construct a ParserUtilsXerces object.
   */
   public ParserUtilsXerces()
   {
   }

   // ***********************************************************************
   // Public methods
   // ***********************************************************************

   /**
    * Return an object that wraps an XMLReader.  The XMLReader is wrapped
    * within the SAXParser.
    *
    * @return  XMLReader wrapped within a SAXParser
    * @exception  XMLMiddlewareException thrown when the SAXParserFactory encounters
    *          a problem creating a new SAXParser
    */
   public XMLReader getXMLReader()
      throws XMLMiddlewareException
   {
      // Instantiate a SAXParser using the SAXParserFactory.
      // This process ensures compatability across multiple version of Xerces

      SAXParserFactory factory = SAXParserFactory.newInstance();
      try
      {
         // Return the XMLReader that is wrapped by the SAXParser
         return factory.newSAXParser().getXMLReader();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Get a DOMImplementation object.
    *
    * @return The DOMImplementation object
    * @exception XMLMiddlewareException Thrown if an error occurs instantiating
    *    the DOMImplementation.
    */
   public DOMImplementation getDOMImplementation()
      throws XMLMiddlewareException
   {
      try
      {
         return new DOMImplementationImpl();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Open an InputSource and create a DOM Document
    *
    * @param   src SAX InputSource to be parsed into a Document
    * @return  Document containing a DOM representation of InputSource
    * @exception  XMLMiddlewareException Thrown if there is a problem parsing the
    *          InputSource
    */
   public Document openDocument(InputSource src)
      throws XMLMiddlewareException
   {
      // Instantiate a DocumentBuilder parser using the DocumentBuilderFactory.
      // This process ensures compatability across multiple version of Xerces

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      try
      {
         DocumentBuilder builder = factory.newDocumentBuilder();
         // Return the DOM tree
         return builder.parse(src);
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Write a DOM Document to a file.
    *
    * @param doc The DOM Document.
    * @param xmlFilename The name of the XML file.
    * @param encoding The output encoding to use. If this is null, the default
    *    encoding is used.
    * @exception XMLMiddlewareException Thrown if an error occurs writing the
    *    DOM Document.
    */
   public void writeDocument(Document doc, String xmlFilename, String encoding)
      throws XMLMiddlewareException
   {
      FileOutputStream stream;
      OutputFormat     outputFormat;
      XMLSerializer    serializer;

      // Write the DOM tree to a file.
      try
      {
         outputFormat = new OutputFormat(doc);
         if (encoding != null) outputFormat.setEncoding(encoding);
         outputFormat.setIndenting(true);

         stream = new FileOutputStream(xmlFilename);

         serializer = new XMLSerializer(stream, outputFormat);
         serializer.asDOMSerializer().serialize(doc);

         stream.close();
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Write a DOM Document to a String.
    *
    * @param doc The DOM Document.
    *
    * @return The XML string.
    * @exception XMLMiddlewareException Thrown if an error occurs writing the
    *    DOM Document.
    */
   public String writeDocument(Document doc)
      throws XMLMiddlewareException
   {
      ByteArrayOutputStream stream;
      OutputFormat          outputFormat;
      XMLSerializer         serializer;

      outputFormat = new OutputFormat(doc);
      //outputFormat.setIndenting(true);
      stream = new ByteArrayOutputStream();
      serializer = new XMLSerializer(stream, outputFormat);

      try
      {
         serializer.serialize(doc);
      }
      catch (Exception e)
      {
         throw new XMLMiddlewareException(e);
      }
      return stream.toString();
   }
}