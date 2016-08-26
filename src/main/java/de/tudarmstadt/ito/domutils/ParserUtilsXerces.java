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

import java.io.ByteArrayOutputStream;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;

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
      throws ParserUtilsException
   {
	  return new SAXParser();

   }                                 

   /**
    * Open an XML file and create a DOM Document.
    *
    * @param xmlFilename The name of the XML file.
    *
    * @return An object that implements Document.
    */
   public  Document openDocument(String xmlFilename)
      throws ParserUtilsException
   {
	 DOMParser parser;
	 GetFileURL gfu = new GetFileURL();

       try
       {
	    // Instantiate the parser and set various options.
	    parser = new DOMParser();
	    parser.setFeature("http://xml.org/sax/features/namespaces", true);

	    //System.out.println("xmlFilename = " +xmlFilename);
	    // Parse the input file
	    //System.out.println("gfu = " +gfu.fullqual(xmlFilename));
          parser.parse(new InputSource(gfu.fullqual(xmlFilename)));
       }
       catch (Exception e)
       {
          throw new ParserUtilsException(e);
       }
	 //System.out.println("gfu = " +gfu.getFileURL(xmlFilename));
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
	 OutputFormat     format;
	 XMLSerializer    serial;

	 // Write the DOM tree to a file.
       try
       {
	    xmlFile = new FileOutputStream(xmlFilename);
	    format = new OutputFormat(doc);
	    format.setIndenting(true);
	    serial = new XMLSerializer((OutputStream)xmlFile, format);
	    serial.asDOMSerializer().serialize(doc);
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
		 return new org.apache.xerces.dom.DocumentImpl();
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
	 

       try
       {
	    // Instantiate the parser and set various options.
	    parser = new DOMParser();
	    parser.setFeature("http://xml.org/sax/features/namespaces", true);

	    // Parse the input file
	    parser.parse(new InputSource(InputStream));
       }
       catch (Exception e)
       {
          throw new ParserUtilsException(e);
       }
	 //System.out.println("gfu = " +gfu.getFileURL(xmlFilename));
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
	 

	ByteArrayOutputStream os = new ByteArrayOutputStream(); 
	OutputFormat outputFormat = new OutputFormat(doc); 
	//outputFormat.setPreserveSpace(true); 
	//outputFormat.setIndenting(true); 
	XMLSerializer serializer = new XMLSerializer(os, outputFormat); 
       try
       {
	    serializer.serialize(doc); 
       }
       catch (Exception e)
       {
          throw new ParserUtilsException(e);
       }
	return new String(os.toByteArray());

}
}