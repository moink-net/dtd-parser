// No copyright, no warranty; use as you will.
// Written by Adam Flinton, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.domutils;

import org.w3c.dom.Document;
import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;

/**
 * Implements ParserUtils for the IBM parser.
 *
 * @author Adam Flinton
 * @version 1.1
 */

public class ParserUtilsIBM implements ParserUtils
{
   // ***********************************************************************
   // Constructors
   // ***********************************************************************

   /**
	* Construct a ParserUtilsIBM object.
	*/
   public ParserUtilsIBM()
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
	   GetFileURL gfu = new GetFileURL();
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
   }   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   public Document createDocument() throws DocumentFactoryException
   {
	  try
	  {
		 return new com.ibm.xml.parser.TXDocument();
	  }
	  catch (Exception e)
	  {
		 throw new DocumentFactoryException(e.getMessage());
	  }
   }      

/**
 * Insert the method's description here.
 * Creation date: (10/04/01 12:30:01)
 * @return org.w3c.dom.Document
 * @param InputStream java.io.InputStream
 */
public Document openDocument(java.io.InputStream InputStream) {
	return null;
}
}