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

public class ParserUtilsSun extends ParserUtilsBase implements ParserUtils
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
      throws ParserUtilsException
   {
	 return new com.sun.xml.parser.Parser();
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
	 com.sun.xml.parser.Parser parser;
	 XmlDocumentBuilder        docBuilder;
	 GetFileURL gfu = new GetFileURL();
	 // Instantiate the parser and set various options.
	 parser = new com.sun.xml.parser.Parser();
	 docBuilder =  new XmlDocumentBuilder();
	 parser.setDocumentHandler(docBuilder);

	 // Parse the input file
       try
       {
          parser.parse(new InputSource(gfu.getFileURL(xmlFilename)));
       }
       catch (Exception e)
       {
          throw new ParserUtilsException(e);
       }

	 // Return the DOM tree
	 return docBuilder.getDocument();
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
	    ((XmlDocument)doc).write((OutputStream)xmlFile);
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
		 return new com.sun.xml.tree.XmlDocument();
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
		 com.sun.xml.parser.Parser parser;
	 XmlDocumentBuilder        docBuilder;
	 // Instantiate the parser and set various options.
	 parser = new com.sun.xml.parser.Parser();
	 docBuilder =  new XmlDocumentBuilder();
	 parser.setDocumentHandler(docBuilder);

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
	 return docBuilder.getDocument();
}

   /**
    * Write a DOM Document to a String.
    *
    * @param doc The DOM Document.
    *
    * @return The XML string.
    */
   String writeDocument(Document doc)
      throws ParserUtilsException
   {
      return serializeDocument(doc);
   }
}