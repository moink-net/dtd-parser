// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// This software was originally developed at the Technical University
// of Darmstadt, Germany.

// Version 2.0
// Changes from version 1.0:
// * Change declaration of byte array constants
// * Change getQuote to use byte constants instead of char values
// Changes from version 1.01:
// * Changed to use Writer instead of OutputStream
// * General cleanup

package org.xmlmiddleware.utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Utility methods for writing an XML document to a Writer.
 *
 * <p>This is used as the base class for classes such as MapSerializer, which
 * write things out as XML.</p>
 *
 * <p>If you want to use a specific encoding, the Writer must be an OutputStreamWriter
 * or a subclass of an OutputStreamWriter. For example, you might use the following
 * code to write to the list.xml file with the Shift_JIS encoding:</p>
 *
 * <pre>
 *    // Construct the FileOutputStream.
 *    OutputStream out = new FileOutputStream("list.xml");
 *    <br />
 *    // Construct the OutputStreamWriter with the Shift_JIS encoding. This may
 *    // throw an UnsupportedEncodingException.
 *    Writer writer = new OutputStreamWriter(out, "Shift_JIS");
 *    <br />
 *    // Construct the XMLWriter.
 *    XMLWriter xmlWriter = new XMLWriter(writer);
 *    <br />
 *    // Write to the XMLWriter.
 *    ...
 *    <br />
 *    // Close the file.
 *    writer.close();
 * </pre>
 *
 * <p>If you want to use the default encoding, you can just use a FileWriter. However,
 * no encoding declaration will be written in the XML declaration. For example:</p>
 *
 * <pre>
 *    // Construct a new FileWriter.
 *    Writer writer = new FileWriter("list.xml");
 *    <br />
 *    // Construct the XMLWriter.
 *    XMLWriter xmlWriter = new XMLWriter(writer);
 *    <br />
 *    // Write to the XMLWriter.
 *    ...
 *    <br />
 *    // Close the file.
 *    writer.close();
 * </pre>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class XMLWriter
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private static String XMLDECLSTART = "<?xml version='1.0'",
                         XMLDECLEND   = " ?>",
                         ENCODING     = " encoding=",
                         STANDALONE   = " standalone=",
                         YES          = "yes",
                         NO           = "no",
                         DOCTYPESTART = "<!DOCTYPE ",
                         DOCTYPEEND   = " >",
                         SYSTEMID     = " SYSTEM ",
                         PUBLICID     = " PUBLIC ",
                         AMPENTITY    = "&amp;",
                         LTENTITY     = "&lt;",
                         GTENTITY     = "&gt;",
                         APOSENTITY   = "&apos;",
                         QUOTENTITY   = "&quot;",
                         RETURN       = System.getProperty("line.separator");

   private static final int lXMLDECLSTART = XMLDECLSTART.length(),
                            lXMLDECLEND   = XMLDECLEND.length(),
                            lENCODING     = ENCODING.length(),
                            lSTANDALONE   = STANDALONE.length(),
                            lYES          = YES.length(),
                            lNO           = NO.length(),
                            lDOCTYPESTART = DOCTYPESTART.length(),
                            lDOCTYPEEND   = DOCTYPEEND.length(),
                            lSYSTEMID     = SYSTEMID.length(),
                            lPUBLICID     = PUBLICID.length(),
                            lAMPENTITY    = AMPENTITY.length(),
                            lLTENTITY     = LTENTITY.length(),
                            lGTENTITY     = GTENTITY.length(),
                            lAPOSENTITY   = APOSENTITY.length(),
                            lQUOTENTITY   = QUOTENTITY.length(),
                            lRETURN       = RETURN.length();

   private static final char QUOT   = '"',
                             APOS   = '\'',
                             LT     = '<',
                             GT     = '>',
                             AMP    = '&',
                             SPACE  = ' ',
                             EQUALS = '=',
                             SLASH  = '/'
;

   //**************************************************************************
   // Variables
   //**************************************************************************

   /** Array for storing attribute names. Used by subclasses. */
   public String[] attrs;

   /** Array for storing attribute values. Used by subclasses. */
   public String[] values;

   private Writer  writer = null;
   private boolean pretty = false, charsWritten = false;
   private int     indent = 0, saveIndent = 0, increment = 3;

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /** Construct a new XMLWriter. */
   public XMLWriter()
   {
   }

   /**
    * Construct a new XMLWriter and set the Writer.
    *
    * @param writer The writer. The writer must implement the write(String,int,int)
    *    and write(int) methods.
    */
   public XMLWriter(Writer writer)
   {
      this.writer = writer;
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Get the Writer.
    *
    * @return The writer.
    */
   public Writer getWriter()
   {
      return writer;
   }

   /**
    * Set the Writer.
    *
    * @param writer The writer. The writer must implement the write(String,int,int)
    *    and write(int) methods
    */
   public void setWriter(Writer writer)
   {
      this.writer = writer;
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
    * <p>An encoding declaration will be written only if the Writer is an
    * OutputStreamWriter or a subclass of OutputStreamWriter.</p>
    */
   public void writeXMLDecl()
      throws IOException
   {
      writer.write(XMLDECLSTART, 0, lXMLDECLSTART);
      writeEncoding();
      writer.write(XMLDECLEND, 0, lXMLDECLEND);
      if (pretty) writer.write(RETURN);
   }

   /**
    * Write the XML declaration with a standalone declaration.
    *
    * <p>An encoding declaration will be written only if the Writer is an
    * OutputStreamWriter or a subclass of OutputStreamWriter.</p>
    *
    * @param standalone Whether the standalone declaration is yes or no.
    */
   public void writeXMLDecl(boolean standalone)
      throws IOException
   {
      writer.write(XMLDECLSTART, 0, lXMLDECLSTART);
      writeEncoding();
      writer.write(STANDALONE, 0, lSTANDALONE);
      writeQuotedValue(standalone ? YES : NO);
      writer.write(XMLDECLEND, 0, lXMLDECLEND);
      if (pretty) writer.write(RETURN, 0, lRETURN);
   }

   /**
    * Write the DOCTYPE statement.
    *
    * <p>Internal subsets are not supported.</p>
    *
    * @param root The root element type.
    * @param systemID The system ID of the external subset. May be null.
    * @param publicID The public ID of the external subset. May be null.
    */
   public void writeDOCTYPE(String root, String systemID, String publicID)
      throws IOException
   {
      writer.write(DOCTYPESTART, 0, lDOCTYPESTART);
      writer.write(root, 0, root.length());
      if (publicID != null)
      {
         writer.write(PUBLICID, 0, lPUBLICID);
         writeQuotedValue(publicID);
         writer.write(SPACE);
      }
      if (systemID != null)
      {
         if (publicID == null)
         {
            writer.write(SYSTEMID, 0, lSYSTEMID);
         }
         writeQuotedValue(systemID);
      }
      writer.write(DOCTYPEEND, 0, lDOCTYPEEND);
      if (pretty) writer.write(RETURN);
   }

   /**
    * Write an element start tag.
    *
    * @param name The name of the element type.
    * @param attrs The number of entries in the attrs and values arrays. May be 0.
    * @param empty Whether the element is empty. 
    */
   public void writeElementStart(String name, int numAttrs, boolean empty)
      throws IOException
   {
      if (!charsWritten && pretty)
      {
         writer.write(RETURN, 0, lRETURN);
         indent();
      }
      writer.write(LT);
      writer.write(name, 0, name.length());
      if (attrs != null)
      {
         for (int i = 0; i < numAttrs; i++)
         {
            writer.write(SPACE);
            writer.write(attrs[i], 0, attrs[i].length());
            writer.write(EQUALS);
            writeAttribute(values[i]);
         }
      }
      if (empty)
      {
         writer.write(SLASH);
      }
      else
      {
         indent += increment;
      }
      writer.write(GT);
   }

   /**
    * Write an element end tag.
    *
    * @param name The name of the element type.
    */
   public void writeElementEnd(String name)
      throws IOException
   {
      indent -= increment;
      if (!charsWritten && pretty)
      {
         writer.write(RETURN);
         indent();
      }
      writer.write(LT);
      writer.write(SLASH);
      writer.write(name, 0, name.length());
      writer.write(GT);
      if (indent == saveIndent)
      {
         // See note in writeCharacters().
         charsWritten = false;
      }
      writer.flush();
   }

   /**
    * Write PCDATA.
    * @param characters The PCDATA. 
    */
   public void writeCharacters(String characters)
      throws IOException
   {
      writeCharacters(characters.toCharArray(), 0, characters.length());
   }

   /**
    * Write PCDATA starting at a given position
    * @param characters The PCDATA.
    * @param start The start position.
    * @param length The number of characters to write.
    */
   public void writeCharacters(char[] characters, int start, int length)
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
    * attributes found on any element type.
    *
    * @param size The array size.
    */
   public void allocateAttrs(int size)
   {
      attrs = new String[size];
      values = new String[size];
   }

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void writeEncoding() throws IOException
   {
      String encoding;

      if (writer instanceof OutputStreamWriter)
      {
         encoding = ((OutputStreamWriter)writer).getEncoding();
         if (encoding != null)
         {
            writer.write(ENCODING, 0, lENCODING);
            writeQuotedValue(encoding);
         }
      }
   }

   private void writeAttribute(String value) throws IOException
   {
      writer.write(QUOT);
      characters(value.toCharArray(), 0, value.length());
      writer.write(QUOT);
   }

   private void writeQuotedValue(String value) throws IOException
   {
      char quote;

      quote = getQuote(value);
      writer.write(quote);
      writer.write(value, 0, value.length());
      writer.write(quote);
   }

   private char getQuote(String value)
   {
      char[] chars = value.toCharArray();

      for (int i = 0; i < chars.length; i++)
      {
         if (chars[i] == QUOT) return APOS;
      }
      return QUOT;
   }

   private void characters(char[] chars, int start, int length)
      throws IOException
   {
      int save;

      save = start;
      for (int i = start; i < start + length; i++)
      {
         switch(chars[i])
         {
            case AMP:
            case LT:
            case GT:
            case APOS:
            case QUOT:
               // When we encounter a character that needs to be escaped as
               // an entity, write out any characters that haven't been written,
               // adjust the save point, and write out the entity reference.

               if (save < i)
               {
                  writer.write(chars, save, i - save);
               }
               save = i + 1;

               switch(chars[i])
               {
                  case AMP:
                     writer.write(AMPENTITY);
                     break;
            
                  case LT:
                     writer.write(LTENTITY);
                     break;
            
                  case GT:
                     writer.write(GTENTITY);
                     break;
            
                  case APOS:
                     writer.write(APOSENTITY);
                     break;
            
                  case QUOT:
                     writer.write(QUOTENTITY);
                     break;
               }
            
            default:
               break;
         }
      }

      // If there are any characters that haven't yet been written, write
      // them out now.
      
      if (save < start + length)
      {
         writer.write(chars, save, start + length - save);
      }
   }

   private void indent() throws IOException
   {
      for (int i = 0; i < indent; i++)
      {
         writer.write(SPACE);
      }
   }
}
