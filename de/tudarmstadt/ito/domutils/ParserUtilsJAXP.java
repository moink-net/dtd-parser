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
import de.tudarmstadt.ito.xmldbms.tools.StringStore;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ParserUtilsJAXP implements ParserUtils {
/**
 * ParserUtilsJAXP constructor comment.
 */
public ParserUtilsJAXP() {
	super();
}

public Parser getSAXParser()
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
	  catch (ParserConfigurationException e) {
	  System.out.println("The underlying parser does not support " +
							   " the requested features.");
 
	  }catch (Throwable err) {
	   err.printStackTrace ();
	  } 

		return parser;    
	  
   }                                       
   
   
   public void writeDocument(Document doc, String xmlFilename) throws Exception
   {
   
	   try
	   {
   
		 
		 Element firstRoot = doc.getDocumentElement();
		 NodeList nl = firstRoot.getChildNodes();
		 for (int i = 0; i < nl.getLength(); i++) 
		 {
		   if(i == 0)
		   {
					
			 Node nchild = nl.item(i);
			 Node n = nchild.getParentNode();
			 String output = traverse(n);
			// System.out.println(output);
			
		   // file store method had to be called to write the string into the file
		     StringStore.store(xmlFilename,output);
			 

		     
		   }
				
		}// end of for loop


		}catch (/* Unexpected */ Exception e) {
		  e.printStackTrace(); 
		}
		
	}
 
 
 

	public static String traverse(Node node) {
	StringBuffer buf = new StringBuffer();
	Node currentNode = node;

	while (currentNode != null) {
	  visit(currentNode, buf);

	  // Move down to first child
	  Node nextNode = currentNode.getFirstChild();
	  if (nextNode != null) {
		currentNode = nextNode;
		continue;
		}

	  // No child nodes, so walk tree
	  while (currentNode != null) {
		revisit(currentNode, buf)  ;
		// do end-of-node processing, if any

		// Move to sibling if possible.
		nextNode = currentNode.getNextSibling();
		if (nextNode != null) {
		  currentNode = nextNode;
		  break;
		  }

	   // Move up
	   if (currentNode == node)
		 currentNode = null;
	   else
		 currentNode = currentNode.getParentNode();
	   }
	}

	return buf.toString();
  }                        

  public static void visit(Node node, StringBuffer buf)
  {
	  int type = node.getNodeType();
	  //Node.ELEMENT, Node.TEXT, Node.ATTRIBUTE
	  switch (type)
	  {
		 case Node.ELEMENT_NODE:
			buf.append("<");
			buf.append(node.getNodeName());
			processAttributes(node, buf);
			buf.append(">");
			break;

		 case Node.TEXT_NODE:
		 //?? Bug -- need to check here to escape <'s
			buf.append(node.getNodeValue());
			break;

		 default:
			break;
	  }
   }                                    

   public static void processAttributes(Node elem, StringBuffer buf)
   {
	  NamedNodeMap attrs = elem.getAttributes();

	  for (int i = 0; i < attrs.getLength(); i++)
	  {
		 Node attr = attrs.item(i);
		 buf.append(" '");
//       ?? Bug -- need to check here to espace single quotes
		 buf.append(attr.getNodeValue());
		 buf.append("'");
	  }
   }                                      

   public static void revisit(Node node, StringBuffer buf)
   {
	  int type = node.getNodeType();
	  switch (type)
	  {
		 case Node.ELEMENT_NODE:
			buf.append("</");
			buf.append(node.getNodeName());
			buf.append(">");
			break;

		 default:
			break;
	  }
   }                                         
   
   
   public Document openDocument(String xmlFilename) throws Exception
   {
   	// Get a JAXP parser factory object
   
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

   public Document openDocument(java.io.InputStream InputStream) throws Exception {
	 
	 

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

   public String returnString(Document toConvert)throws Exception {
	 

	 // Write the DOM tree to a file.
	  
	    String output = null;
	 	Element firstRoot = toConvert.getDocumentElement();
		 NodeList nl = firstRoot.getChildNodes();
		 for (int i = 0; i < nl.getLength(); i++) 
		 {
		  // Node n01 = nl.item(i);
		  // String snode = n01.getNodeName();	
	      // System.out.println(snode+"and the loop"+i);
		 
		 if(i == 0){
		  		
		 Node nchild = nl.item(i);
		 Node n = nchild.getParentNode();
		 output = traverse(n);
		 
		           }
				
		 }// end of for loop

		 return output;

}

public Document createDocument() throws ParserUtilsException
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
		 throw new ParserUtilsException(e.getMessage());
	  }
   }               
}