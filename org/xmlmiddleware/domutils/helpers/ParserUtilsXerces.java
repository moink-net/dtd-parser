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

package org.xmlmiddleware.domutils.helpers;

import org.xmlmiddleware.domutils.ParserUtils;
import org.xmlmiddleware.domutils.ParserUtilsException;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

// Imports for the Xerces parser

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

/**
 * Implements ParserUtils for the Xerces parser.
 *
 * @author Adam Flinton
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
    * Get a SAX 2.0 XMLReader.
    *
    * @return An object that implements XMLReader.
    */
   public XMLReader getXMLReader()
      throws ParserUtilsException
   {
      return new SAXParser();
   }

   /**
    * Create an empty Document.
    *
    * @return The Document
    */
   public Document createDocument() throws ParserUtilsException
   {
      try
      {
         return new org.apache.xerces.dom.DocumentImpl();
      }
      catch (Exception e)
      {
         throw new ParserUtilsException(e);
      }
   }

   /**
    * Open an XML file and create a DOM Document.
    *
    * @param xmlFilename The name of the XML file.
    *
    * @return An object that implements Document.
    */
   public Document openDocument(String xmlFilename)
      throws ParserUtilsException
   {
      try
      {
         return openDocument(new FileInputStream(xmlFilename));
      }
      catch (Exception e)
      {
         throw new ParserUtilsException(e);
      }
   }

   /**
    * Open an InputStream and create a DOM Document.
    *
    * @param inputStream The InputStream.
    *
    * @return An object that implements Document.
    */
   public Document openDocument(InputStream stream)
      throws ParserUtilsException
   {
      DOMParser parser;

      try
      {
         // Instantiate the parser and set various options.
         parser = new DOMParser();
         parser.setFeature("http://xml.org/sax/features/namespaces", true);

         // Parse the input file
         parser.parse(new InputSource(stream));
      }
      catch (Exception e)
      {
         throw new ParserUtilsException(e);
      }

      // Return the DOM tree
      return parser.getDocument();
   }

   /**
    * Write a DOM Document to a file.
    *
    * @param doc The DOM Document.
    * @param xmlFilename The name of the XML file.
    * @param encoding The output encoding to use. If this is null, the default
    *    encoding is used.
    */
   public void writeDocument(Document doc, String xmlFilename, String encoding)
      throws ParserUtilsException
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
         throw new ParserUtilsException(e);
      }
   }

   /**
    * Write a DOM Document to a String.
    *
    * @param doc The DOM Document.
    *
    * @return The XML string.
    */
   public String writeDocument(Document doc)
      throws ParserUtilsException
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
         throw new ParserUtilsException(e);
      }
      return stream.toString();
   }
}