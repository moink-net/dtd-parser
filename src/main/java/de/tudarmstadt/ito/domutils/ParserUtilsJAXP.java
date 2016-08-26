package de.tudarmstadt.ito.domutils;

import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory; 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory; 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import de.tudarmstadt.ito.xmldbms.tools.GetFileURL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ParserUtilsJAXP extends ParserUtilsBase implements ParserUtils {
/**
 * Construct a ParserUtilsJAXP.
 */
public ParserUtilsJAXP() {
	super();
}

   /**
    * Get a SAX 1.0 Parser.
    *
    * @return An object that implements Parser.
    */
public Parser getSAXParser()
      throws ParserUtilsException
   {
	Parser parser = null;
	 try{  
		 SAXParserFactory factory = SAXParserFactory.newInstance();
		 // Turn on validation, and turn off namespaces
		 //factory.setValidating(true);
		 //factory.setNamespaceAware(false);
		 
		 
		 SAXParser sparser = factory.newSAXParser();
		 parser = (Parser)sparser.getParser();
		 
	  } 
	  catch (Exception e) {
           throw new ParserUtilsException(e);
        }
		return parser;    
	  
   }                                       
   
   
   /**
    * Write a DOM Document to a file.
    *
    * @param doc The DOM Document.
    * @param xmlFilename The name of the XML file.
    */
   public void writeDocument(Document doc, String xmlFilename) throws ParserUtilsException
   {
      String     output;
      FileWriter fw;

      output = serializeDocument(doc);
      try
      {
         fw = new FileWriter(xmlFilename);
         fw.write(output);
         fw.close();
      }
      catch (Exception e)
      {
         throw new ParserUtilsException(e);
      }
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
   	// Get a JAXP parser factory object
      try
      {
		GetFileURL gfu = new GetFileURL();
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		// Tell the factory what kind of parser we want 
		dbf.setValidating(false);
		
		// Use the factory to get a JAXP parser object
		javax.xml.parsers.DocumentBuilder parser = dbf.newDocumentBuilder();

		// Tell the parser how to handle errors.  Note that in the JAXP API,
		// DOM parsers rely on the SAX API for error handling
		parser.setErrorHandler(new org.xml.sax.ErrorHandler() {
				public void warning(SAXParseException e) {
					System.err.println("WARNING: " + e.getMessage());
				}
				public void error(SAXParseException e) {
					System.err.println("ERROR: " + e.getMessage());
				}
				public void fatalError(SAXParseException e)
					throws SAXException {
					System.err.println("FATAL: " + e.getMessage());
					throw e;   // re-throw the error
				}
			});

		// Finally, use the JAXP parser to parse the file.  This call returns
		// A Document object.  Now that we have this object, the rest of this
		// class uses the DOM API to work with it; JAXP is no longer required.
		org.w3c.dom.Document  document =    parser.parse(new InputSource(gfu.fullqual(xmlFilename)));
		
	 //System.out.println("gfu = " +gfu.getFileURL(xmlFilename));
	 // Return the DOM tree
	 return document;
		   //System.out.println("gfu = " +gfu.getFileURL(xmlFilename));
	    // Return the DOM tree
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
   try
   {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		// Tell the factory what kind of parser we want 
		dbf.setValidating(false);
		
		// Use the factory to get a JAXP parser object
		javax.xml.parsers.DocumentBuilder parser = dbf.newDocumentBuilder();

		// Tell the parser how to handle errors.  Note that in the JAXP API,
		// DOM parsers rely on the SAX API for error handling
		parser.setErrorHandler(new org.xml.sax.ErrorHandler() {
				public void warning(SAXParseException e) {
					System.err.println("WARNING: " + e.getMessage());
				}
				public void error(SAXParseException e) {
					System.err.println("ERROR: " + e.getMessage());
				}
				public void fatalError(SAXParseException e)
					throws SAXException {
					System.err.println("FATAL: " + e.getMessage());
					throw e;   // re-throw the error
				}
			});

		// Finally, use the JAXP parser to parse the file.  This call returns
		// A Document object.  Now that we have this object, the rest of this
		// class uses the DOM API to work with it; JAXP is no longer required.
		org.w3c.dom.Document  document =     parser.parse(new InputSource(InputStream));
		
	 //System.out.println("gfu = " +gfu.getFileURL(xmlFilename));
	 // Return the DOM tree
	 return document;
   }
   catch (Exception e)
   {
      throw new ParserUtilsException(e);
   }
		 
	 
}

   /**
    * Create an empty Document.
    *
    * @return The Document
    */
public Document createDocument()
   throws ParserUtilsException
{
  try
	  {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		// Tell the factory what kind of parser we want 
		dbf.setValidating(false);
		
		// Use the factory to get a JAXP parser object
		javax.xml.parsers.DocumentBuilder dbuild = dbf.newDocumentBuilder();

		
		  
		 return dbuild.newDocument();
	  }
	  catch (Exception e)
	  {
		 throw new ParserUtilsException(e);
	  }
   }               
}