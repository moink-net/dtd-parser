// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.domutils;

import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.w3c.dom.Document;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

// Imports for the Oracle version 2 parser
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.SAXParser;
import oracle.xml.parser.v2.XMLDocument;

import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;

/**
 * Implements ParserUtils for the Oracle parser, version 2.
 *
 * @author Adam Flinton
 * @version 1.1
 */

public class ParserUtilsOracle2 extends ParserUtilsBase implements ParserUtils
{
   // ***********************************************************************
   // Constructors
   // ***********************************************************************

   /**
	* Construct a ParserUtilsOracle2 object.
	*/
   public ParserUtilsOracle2()
   {
   }      

   // ***********************************************************************
   // Public methods
   // ***********************************************************************

   /**
	* Get a SAX 1.0 Parser.
	*
	* @return An object that implements Parser.
	*/
   public Parser getSAXParser()
      throws ParserUtilsException
   {
	 SAXParser parser;

	 // Instantiate the parser and set various options
	 parser = new SAXParser();
	 parser.setValidationMode(true);
	 return parser;
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
	 DOMParser parser;
	 GetFileURL gfu = new GetFileURL();

	 // Instantiate the parser and set various options.
	 parser = new DOMParser();
	 parser.setValidationMode(false);
	 parser.showWarnings(true);

	 // Parse the input file
       try
       {
          String[] filename = new String[1];
          filename[0] = xmlFilename;
	    parser.parse(new InputSource(gfu.getFile(filename)));
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
    */
   public void writeDocument(Document doc, String xmlFilename)
      throws ParserUtilsException
   {
	 FileOutputStream xmlFile;

	 // Write the DOM tree to a file.
       try
       {
          xmlFile = new FileOutputStream(xmlFilename);
	    ((XMLDocument)doc).print((OutputStream)xmlFile);
          xmlFile.close();
       }
       catch (Exception e)
       {
          throw new ParserUtilsException(e);
       }
   }                     

   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Create an empty Document.
    *
    * @return The Document
    */
   public Document createDocument() throws ParserUtilsException
   {
	  try
	  {
		 return new oracle.xml.parser.v2.XMLDocument();
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
public Document openDocument(java.io.InputStream InputStream)
      throws ParserUtilsException
 {
	DOMParser parser;
	

	 // Instantiate the parser and set various options.
	 parser = new DOMParser();
	 parser.setValidationMode(false);
	 parser.showWarnings(true);

	 // Parse the input file
       try
       {
	    parser.parse(new InputSource(InputStream));
       }
       catch (Exception e)
       {
          throw new ParserUtilsException(e);
       }

	 // Return the DOM tree
	 return parser.getDocument();
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
      return serializeDocument(doc);
   }

}