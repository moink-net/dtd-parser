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

public class ParserUtilsOracle2 implements ParserUtils
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
	* @return An object that implements Document.
	* @exception Exception An error occurred. Exception is used because
	*            the possible errors are different for each implementation.
	*/
   public Document openDocument(String xmlFilename) throws Exception
   {
	 DOMParser parser;
	 GetFileURL gfu = new GetFileURL();

	 // Instantiate the parser and set various options.
	 parser = new DOMParser();
	 parser.setValidationMode(false);
	 parser.showWarnings(true);

	 // Parse the input file
	 parser.parse(new InputSource(gfu.getFileURL(xmlFilename)));

	 // Return the DOM tree
	 return parser.getDocument();
   }                                                                  

   /**
	* Write a DOM Document to a file.
	*
	* @param doc The DOM Document.
	* @param xmlFilename The name of the XML file.
	* @exception Exception An error occurred. Exception is used because
	*            the possible errors are different for each implementation.
	*/
   public void writeDocument(Document doc, String xmlFilename) throws Exception
   {
	 FileOutputStream xmlFile;

	 // Write the DOM tree to a file.
	 xmlFile = new FileOutputStream(xmlFilename);
	 ((XMLDocument)doc).print((OutputStream)xmlFile);
	 xmlFile.close();
   }                     

   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   public Document createDocument() throws ParserUtilsException
   {
	  try
	  {
		 return new oracle.xml.parser.v2.XMLDocument();
	  }
	  catch (Exception e)
	  {
		 throw new ParserUtilsException(e.getMessage());
	  }
   }         

/**
 * Insert the method's description here.
 * Creation date: (10/04/01 12:30:01)
 * @return org.w3c.dom.Document
 * @param InputStream java.io.InputStream
 */
public Document openDocument(java.io.InputStream InputStream) {
	DOMParser parser;
	

	 // Instantiate the parser and set various options.
	 parser = new DOMParser();
	 parser.setValidationMode(false);
	 parser.showWarnings(true);

	 // Parse the input file
	 parser.parse(new InputSource(InputStream));

	 // Return the DOM tree
	 return parser.getDocument();
}
}