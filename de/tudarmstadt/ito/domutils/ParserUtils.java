// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.domutils;

import org.xml.sax.Parser;
import org.w3c.dom.Document;
 
/**
 * Interface for a class that generates implements parser-specific methods.
 *
 * <p>DOM and SAX do not cover all areas of functionality. This interface
 * encapsulates bootstrapping (creating a DOM Document and getting a SAX
 * parser) and serializing a DOM Document. Eventually, this will hopefully
 * go away through widespread implementation of JAXP and DOM level 3.</p>
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
   public Parser getSAXParser();   

   /**
	* Open an XML file and create a DOM Document.
	*
	* @param xmlFilename The name of the XML file.
	* @return An object that implements Document.
	* @exception Exception An error occurred. Exception is used because
	*            the possible errors are different for each implementation.
	*/
   public Document openDocument(String xmlFilename) throws Exception;   

   /**
	* Write a DOM Document to a file.
	*
	* @param doc The DOM Document.
	* @param xmlFilename The name of the XML file.
	* @exception Exception An error occurred. Exception is used because
	*            the possible errors are different for each implementation.
	*/
   public void writeDocument(Document doc, String xmlFilename) throws Exception;            

/**
 * Insert the method's description here.
 * Creation date: (29/06/01 15:45:11)
 * @return org.w3c.dom.Document
 */
public Document createDocument() throws DocumentFactoryException;   

/**
 * Insert the method's description here.
 * Creation date: (10/04/01 12:28:22)
 * @return org.w3c.dom.Document
 * @param InputStream java.io.InputStream
 */
public Document openDocument(java.io.InputStream InputStream) throws Exception;

/**
 * Insert the method's description here.
 * Creation date: (19/04/01 14:24:36)
 * @return java.lang.String
 * @param Doc org.w3c.dom.Document
 * @param xmlfile java.lang.String
 */
String returnString(Document toConvert) throws Exception;
}