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

// Imports for the Xerces parser

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
import java.io.ByteArrayOutputStream;

/**
 * Implements ParserUtils for the Xerces parser.
 *
 * @author Adam Flinton
 * @version 1.1
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
	* Get a SAX 1.0 Parser.
	*
	* @return An object that implements Parser.
	*/
   public Parser getSAXParser()
   {
	  return new SAXParser();

   }                                 

   /**
	* Open an XML file and create a DOM Document.
	*
	* @param xmlFilename The name of the XML file.
	* @return An object that implements Document.
	* @exception Exception An error occurred. Exception is used because
	*            the possible errors are different for each implementation.
	*/
   public  Document openDocument(String xmlFilename) throws Exception
   {
	 DOMParser parser;
	 GetFileURL gfu = new GetFileURL();

	 // Instantiate the parser and set various options.
	 parser = new DOMParser();
	 parser.setFeature("http://xml.org/sax/features/namespaces", true);

	 System.out.println("xmlFilename = " +xmlFilename);
	 // Parse the input file
	 System.out.println("gfu = " +gfu.fullqual(xmlFilename));
	 parser.parse(new InputSource(gfu.fullqual(xmlFilename)));
	 //System.out.println("gfu = " +gfu.getFileURL(xmlFilename));
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
	 OutputFormat     format;
	 XMLSerializer    serial;

	 // Write the DOM tree to a file.
	 xmlFile = new FileOutputStream(xmlFilename);
	 format = new OutputFormat(doc);
	 format.setIndenting(true);
	 serial = new XMLSerializer((OutputStream)xmlFile, format);
	 serial.asDOMSerializer().serialize(doc);
	 xmlFile.close();
   }                        

   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   public Document createDocument() throws ParserUtilsException
   {
	  try
	  {
		 return new org.apache.xerces.dom.DocumentImpl();
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
public Document openDocument(java.io.InputStream InputStream) throws Exception {
	 DOMParser parser;
	 

	 // Instantiate the parser and set various options.
	 parser = new DOMParser();
	 parser.setFeature("http://xml.org/sax/features/namespaces", true);

	 // Parse the input file
	 parser.parse(new InputSource(InputStream));
	 //System.out.println("gfu = " +gfu.getFileURL(xmlFilename));
	 // Return the DOM tree
	 return parser.getDocument();
}

/**
 * Insert the method's description here.
 * Creation date: (19/04/01 15:22:10)
 * @return java.lang.String
 * @param toConvert org.w3c.dom.Document
 */
public String returnString(Document toConvert)throws Exception {
	 

	ByteArrayOutputStream os = new ByteArrayOutputStream(); 
	OutputFormat outputFormat = new OutputFormat(toConvert); 
	//outputFormat.setPreserveSpace(true); 
	//outputFormat.setIndenting(true); 
	XMLSerializer serializer = new XMLSerializer(os, outputFormat); 
	serializer.serialize(toConvert); 
	return new String(os.toByteArray());

}
}