// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0:
// * Change declaration of byte array constants
// * Change getQuote to use byte constants instead of char values
// Changes from version 1.01: None

package de.tudarmstadt.ito.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility methods for writing an XML document to an OutputStream.
 *
 * <p>This is used as the base class for classes such as Map, which need to
 * serialize themselves as XML.</p>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

public class XMLOutputStream
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   // 5/24/00 Phil Friedman, Ronald Bourret
   // 1) Initialize the following byte array constants from Strings, not char arrays,
   //    which require an explicit cast to byte.
   // 2) Add byte constants for single and double quotes.

   static String XMLDECLSTARTStr = "<?xml version='1.0'",
				 XMLDECLENDStr = " ?>",
				 ENCODINGStr = " encoding=",
				 STANDALONEStr = " standalone=",
				 YESStr = "yes",
				 NOStr = "no",
				 DOCTYPESTARTStr = "<!DOCTYPE ",
				 DOCTYPEENDStr = " >",
				 SYSTEMIDStr = " SYSTEM ",
				 PUBLICIDStr = " PUBLIC ",
				 AMPStr = "&amp;",
				 LTStr = "&lt;",
				 GTStr = "&gt;",
				 APOSStr = "&apos;",
				 QUOTStr = "&quot;";
   static final byte[] XMLDECLSTART = XMLDECLSTARTStr.getBytes(),
					   XMLDECLEND = XMLDECLENDStr.getBytes(),
					   ENCODING = ENCODINGStr.getBytes(),
					   STANDALONE = STANDALONEStr.getBytes(),
					   YES = YESStr.getBytes(),
					   NO = NOStr.getBytes(),
					   DOCTYPESTART = DOCTYPESTARTStr.getBytes(),
					   DOCTYPEEND = DOCTYPEENDStr.getBytes(),
					   SYSTEMID = SYSTEMIDStr.getBytes(),
					   PUBLICID = PUBLICIDStr.getBytes(),
					   AMP = AMPStr.getBytes(),
					   LT = LTStr.getBytes(),
					   GT = GTStr.getBytes(),
					   APOS = APOSStr.getBytes(),
					   QUOT = QUOTStr.getBytes(),
					   RETURN = System.getProperty("line.separator").getBytes();
   private static final byte DOUBLEQUOTE = (byte)'"',
							 SINGLEQUOTE = (byte)'\'';

   //**************************************************************************
   // Variables
   //**************************************************************************

   /** Array for storing attribute names. */
   public byte[][] attrs;

   /** Array for storing attribute values. */
   public byte[][] values;

   OutputStream  out = null;
   boolean       pretty = false, charsWritten = false;
   int           indent = 0, saveIndent = 0, increment = 3;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new XMLOutputStream. */
   public XMLOutputStream()
   {
   }   

   /** Construct a new XMLOutputStream and set the OutputStream. */
   public XMLOutputStream(OutputStream out)
   {
	  this.out = out;
   }   

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Set the OutputStream.
	*
	* @param out The output stream.
	*/
   public void setOutputStream(OutputStream out)
   {
	  this.out = out;
   }   

   /**
	* Set the pretty printing options.
	*
	* @param pretty Whether to perform pretty printing.
	* @param increment The number of spaces by which to indent nested
	*  child elements in their parent. If this is less then 0, it is
	*  set to 3.
	*/
   public void setPrettyPrinting(boolean pretty, int increment)
   {
	  this.pretty = pretty;
	  this.increment = (increment < 0) ? 3 : increment;
   }   

   /**
	* Write the XML declaration.
	*
	* @param encoding The encoding to use. May be null.
	*/
   public void writeXMLDecl(byte[] encoding)
	  throws IOException
   {
	  out.write(XMLDECLSTART);
	  if (encoding != null) writeEncoding(encoding);
	  out.write(XMLDECLEND);
	  if (pretty) out.write(RETURN);
   }   

   /**
	* Write the XML declaration with a standalone declaration.
	*
	* @param encoding The encoding to use. May be null.
	* @param standalone Whether the standalone declaration is yes or no.
	*/
   public void writeXMLDecl(byte[] encoding, boolean standalone)
	  throws IOException
   {
	  out.write(XMLDECLSTART);
	  if (encoding != null) writeEncoding(encoding);
	  out.write(STANDALONE);
	  writeQuotedValue(standalone ? YES : NO);
	  out.write(XMLDECLEND);
	  if (pretty) out.write(RETURN);
   }   

   /**
	* Write the DOCTYPE statement. Internal subsets are not supported.
	*
	* @param root The root element type.
	* @param systemID The system ID of the external subset.
	* @param publicID The public ID of the external subset. May be null.
	*/
   public void writeDOCTYPE(byte[] root, byte[] systemID, byte[] publicID)
	  throws IOException
   {
	  out.write(DOCTYPESTART);
	  out.write(root);
	  if (publicID != null)
	  {
		 out.write(PUBLICID);
		 writeQuotedValue(publicID);
		 out.write(' ');
	  }
	  if (systemID != null)
	  {
		 if (publicID == null)
		 {
			out.write(SYSTEMID);
		 }
		 writeQuotedValue(systemID);
	  }
	  out.write(DOCTYPEEND);
	  if (pretty) out.write(RETURN);
   }   

   /**
	* Write an element start tag.
	*
	* @param name The name of the element type.
	* @param attrs The attribute names. May be null.
	* @param values The attribute values. Must match attrs in number and order.
	*  May be null.
	* @param empty Whether the element is empty. 
	*/
   public void writeElementStart(byte[] name, byte[][] attrs, byte[][] values, boolean empty)
	  throws IOException
   {
	  if (!charsWritten && pretty)
	  {
		 out.write(RETURN);
		 indent();
	  }
	  out.write('<');
	  out.write(name);
	  if (attrs != null)
	  {
		 for (int i = 0; i < attrs.length; i++)
		 {
			if (attrs[i] == null) break;
			out.write(' ');
			out.write(attrs[i]);
			out.write('=');
			writeAttribute(values[i]);
		 }
	  }
	  if (empty)
	  {
		 out.write('/');
	  }
	  else
	  {
		 indent += increment;
	  }
	  out.write('>');
   }   

   /**
	* Write an element end tag.
	*
	* @param name The name of the element type.
	*/
   public void writeElementEnd(byte[] name)
	  throws IOException
   {
	  indent -= increment;
	  if (!charsWritten && pretty)
	  {
		 out.write(RETURN);
		 indent();
	  }
	  out.write('<');
	  out.write('/');
	  out.write(name);
	  out.write('>');
	  if (indent == saveIndent)
	  {
		 // See note in writeCharacters().
		 charsWritten = false;
	  }
	  out.flush();
   }   

   /**
	* Write PCDATA.
	* @param characters The PCDATA. 
	*/
   public void writeCharacters(byte[] characters)
	  throws IOException
   {
	  writeCharacters(characters, 0, characters.length);
   }   

   /**
	* Write PCDATA starting at a given position
	* @param characters The PCDATA.
	* @param start The start position.
	* @param length The number of characters to write.
	*/
   public void writeCharacters(byte[] characters, int start, int length)
	  throws IOException
   {
	  // Write out the characters, flag that characters have been written, and
	  // save the indent level. We save the indent level because we assume that
	  // once we are inside an element with mixed content, we no longer want to
	  // perform any indents. We use the indent level as a flag to tell us when
	  // we leave that mixed element.

	  characters(characters, start, length);
	  charsWritten = true;
	  saveIndent = indent;
   }   

   /**
	* Allocate the attrs and values arrays. Generally, subclasses
	* allocate arrays large enough to handle the maximum number of
	* attributes found on any element type. Before each call to
	* writeElementStart, they then call initAttrs, which initializes
	* the attributes to nulls; writeElementStart stops writing attributes
	* the first time it encounters a value of the attrs variable that is null.
	*
	* @param size The array size.
	*/
   public void allocateAttrs(int size)
   {
	  attrs = new byte[size][];
	  values = new byte[size][];
   }   

   public void initAttrs()
   {
	  if (attrs == null) return;
	  for (int i = 0; i < attrs.length; i++)
	  {
		 attrs[i] = null;
		 values[i] = null;
	  }
   }   

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void writeEncoding(byte[] encoding) throws IOException
   {
	  out.write(ENCODING);
	  writeQuotedValue(encoding);
   }   

   private void writeAttribute(byte[] value) throws IOException
   {
	  byte quote;

	  quote = getQuote(value);
	  out.write(quote);
	  characters(value, 0, value.length);
	  out.write(quote);
   }   

   private void writeQuotedValue(byte[] value) throws IOException
   {
	  byte quote;

	  quote = getQuote(value);
	  out.write(quote);
	  out.write(value);
	  out.write(quote);
   }   

   private byte getQuote(byte[] value)
   {
	  // 5/24/00 Phil Friedman, Ronald Bourret
	  // Use byte constants instead of character values.

	  for (int i = 0; i < value.length; i++)
	  {
		 if (value[i] == DOUBLEQUOTE) return SINGLEQUOTE;
	  }
	  return DOUBLEQUOTE;
   }   

   private void characters(byte[] ch, int start, int length)
	  throws IOException
   {
	  int save;

	  save = start;
	  for (int i = start; i < start + length; i++)
	  {
		 switch(ch[i])
		 {
			case '&':
			case '<':
			case '>':
			case '\'':
			case '"':
			   if (save < i)
			   {
				  out.write(ch, save, i - save);
			   }
			   save = i + 1;

			   switch(ch[i])
			   {
				  case '&':
					 out.write(AMP);
					 break;
			
				  case '<':
					 out.write(LT);
					 break;
			
				  case '>':
					 out.write(GT);
					 break;
			
				  case '\'':
					 out.write(APOS);
					 break;
			
				  case '"':
					 out.write(QUOT);
					 break;
			   }
			
			default:
			   break;
		 }
	  }
	  
	  if (save < start + length)
	  {
		 out.write(ch, save, start + length - save);
	  }
   }   

   private void indent() throws IOException
   {
	  for (int i = 0; i < indent; i++)
	  {
		 out.write(' ');
	  }
   }   
}