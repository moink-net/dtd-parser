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

// Imports for the Sun parser
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.XmlDocumentBuilder;

import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;

/**
 * Implements ParserUtils for the Sun parser.
 *
 * @author Adam Flinton
 * @version 1.1
 */

public class ParserUtilsSun implements ParserUtils
{
   // ***********************************************************************
   // Constructors
   // ***********************************************************************

   /**
	* Construct a ParserUtilsSun object.
	*/
   public ParserUtilsSun()
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
	 return new com.sun.xml.parser.Parser();
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
	 com.sun.xml.parser.Parser parser;
	 XmlDocumentBuilder        docBuilder;
	 GetFileURL gfu = new GetFileURL();
	 // Instantiate the parser and set various options.
	 parser = new com.sun.xml.parser.Parser();
	 docBuilder =  new XmlDocumentBuilder();
	 parser.setDocumentHandler(docBuilder);

	 // Parse the input file
	 parser.parse(new InputSource(gfu.getFileURL(xmlFilename)));

	 // Return the DOM tree
	 return docBuilder.getDocument();
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
	 ((XmlDocument)doc).write((OutputStream)xmlFile);
	 xmlFile.close();
   }                     

   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   public Document createDocument() throws ParserUtilsException
   {
	  try
	  {
		 return new com.sun.xml.tree.XmlDocument();
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
		 com.sun.xml.parser.Parser parser;
	 XmlDocumentBuilder        docBuilder;
	 // Instantiate the parser and set various options.
	 parser = new com.sun.xml.parser.Parser();
	 docBuilder =  new XmlDocumentBuilder();
	 parser.setDocumentHandler(docBuilder);

	 // Parse the input file
	 parser.parse(new InputSource(InputStream));

	 // Return the DOM tree
	 return docBuilder.getDocument();
}
}