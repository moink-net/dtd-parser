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
// * Modified for SAX 2

package org.xmlmiddleware.domutils;

import org.xml.sax.XMLReader;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * Interface for a class that generates implements parser-specific methods.
 *
 * <p>DOM and SAX do not cover all areas of functionality. This interface
 * encapsulates bootstrapping (creating a DOM Document and getting a SAX
 * parser) and serializing a DOM Document to both a string and a file.
 * Eventually, this should disappear through widespread implementation
 * of JAXP and DOM level 3.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 2.0
 */

public interface ParserUtils
{
   /**
    * Get a SAX 2.0 XMLReader.
    *
    * @return An object that implements XMLReader.
    */
   public XMLReader getXMLReader()
      throws ParserUtilsException;

   /**
    * Create an empty Document.
    *
    * @return The Document
    */
   public Document createDocument()
      throws ParserUtilsException;

   /**
    * Open an XML file and create a DOM Document.
    *
    * @param xmlFilename The name of the XML file.
    *
    * @return An object that implements Document.
    */
   public Document openDocument(String xmlFilename)
      throws ParserUtilsException;

   /**
    * Open an InputStream and create a DOM Document.
    *
    * @param inputStream The InputStream.
    *
    * @return An object that implements Document.
    */
   public Document openDocument(InputStream inputStream)
      throws ParserUtilsException;

   /**
    * Write a DOM Document to a file.
    *
    * @param doc The DOM Document.
    * @param xmlFilename The name of the XML file.
    */
   public void writeDocument(Document doc, String xmlFilename)
      throws ParserUtilsException;

   /**
    * Write a DOM Document to a String.
    *
    * @param doc The DOM Document.
    *
    * @return The XML string.
    */
   String writeDocument(Document doc)
      throws ParserUtilsException;
}
