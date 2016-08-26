// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.domutils;

import org.xml.sax.Parser;
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
 * @version 1.1
 */

public interface ParserUtils
{
   /**
    * Get a SAX 1.0 Parser.
    *
    * @return An object that implements Parser.
    */
   public Parser getSAXParser()
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
