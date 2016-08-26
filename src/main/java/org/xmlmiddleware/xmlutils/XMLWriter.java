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
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.0:
// * Change declaration of byte array constants
// * Change getQuote to use byte constants instead of char values
// Changes from version 1.01:
// * Changed to use Writer instead of OutputStream
// * Moved to xmlutils package
// * General cleanup

package org.xmlmiddleware.xmlutils;

import java.io.*;

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
 * <p><b>WARNING:</b> This class does not check that you write a valid XML document.
 * For example, you could write two DOCTYPE statements or improperly nested elements.</p>
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

   private static final char QUOT   = '"',
                             APOS   = '\'',
                             LT     = '<',
                             GT     = '>',
                             AMP    = '&',
                             SPACE  = ' ',
                             EQUALS = '=',
                             SLASH  = '/';

   //**************************************************************************
   // Variables
   //**************************************************************************

   /** Array for storing attribute names. Used by subclasses. */
   public String[] attrs;

   /** Array for storing attribute values. Used by subclasses. */
   public String[] values;

   private Writer  writer = null;
   private boolean pretty = false, charsWritten = false;
   private int     saveIndent = -1, increment = 3, indent = -1 * increment;

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
    * <p>This method should not be called after you have started writing the document.</p>
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
    * <p>This method should not be called after you have started writing the document.</p>
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
      this.indent = -1 * this.increment;
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
      writer.write(XMLDECLSTART);
      writeEncoding();
      writer.write(XMLDECLEND);
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
      writer.write(XMLDECLSTART);
      writeEncoding();
      writer.write(STANDALONE);
      writeQuotedValue(standalone ? YES : NO);
      writer.write(XMLDECLEND);
      if (pretty) writer.write(RETURN);
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
      writer.write(DOCTYPESTART);
      writer.write(root);
      if (publicID != null)
      {
         writer.write(PUBLICID);
         writeQuotedValue(publicID);
         writer.write(SPACE);
      }
      if (systemID != null)
      {
         if (publicID == null)
         {
            writer.write(SYSTEMID);
         }
         writeQuotedValue(systemID);
      }
      writer.write(DOCTYPEEND);
      if (pretty) writer.write(RETURN);
   }

   /**
    * Write an element start tag.
    *
    * @param name The name of the element type.
    * @param attrs The number of entries in the attrs and values arrays. May be 0.
    *    These entries must be non-null.
    * @param empty Whether the element is empty. 
    */
   public void writeElementStart(String name, int numAttrs, boolean empty)
      throws IOException
   {
      // Increment the indent value.

      indent += increment;

      // If we are starting a new element, it is not in mixed content
      // (charsWritten equals false), and we are doing pretty-printing,
      // then indent the element.

      if (!charsWritten && pretty)
      {
         writer.write(RETURN);
         indent();
      }

      // Write the element start tag, including any attributes.

      writer.write(LT);
      writer.write(name);
      if (attrs != null)
      {
         for (int i = 0; i < numAttrs; i++)
         {
            writer.write(SPACE);
            writer.write(attrs[i]);
            writer.write(EQUALS);
            writeAttributeValue(values[i]);
         }
      }

      // If this is an empty element, close it and decrement the indent level.

      if (empty)
      {
         writer.write(SLASH);
         indent -= increment;
      }

      // Finish the start tag.

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
      if (charsWritten)
      {
         // If the element being ended contained text or mixed content, check if
         // it was the outermost element that did so. If so, reset charsWritten
         // and saveIndent. Resetting charsWritten allows us to start printing indents
         // again. Resetting saveIndent allows us to flag the start of the next
         // element that contains character or mixed content.

         if (saveIndent == indent)
         {
            // See notes in writeCharacters().
            charsWritten = false;
            saveIndent = -1;
         }
      }
      else if (pretty)
      {
         // If the element being ended had element or empty content and we are
         // pretty printing, then start a new line and indent.

         writer.write(RETURN);
         indent();
      }

      // Decrement the indent level.

      indent -= increment;

      // Write the element end tag and flush the writer.

      writer.write(LT);
      writer.write(SLASH);
      writer.write(name);
      writer.write(GT);
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
      // Write out the characters.

      characters(characters, start, length);

      // Flag that characters have been written. When we are inside an element in
      // which characters have been written and we start a new element, we will
      // not perform any indentation. This is because the the new element is in
      // mixed content and indentation would be wrong. For example, we don't indent
      // before the <b> element in the following:
      //
      //    <p>We indent the paragraph element <b>but not the bold element</b>.</p>
      //
      // We also won't perform indentation before ending the element. For example,
      // we don't indent before </price> in the following:
      //
      //    <price>12.34</price>

      charsWritten = true;

      // If the saved indent level is -1, then we have just started an element with
      // character or mixed content. Save the indent level of this element so we
      // can determine when we end it. After we end this element, we will start
      // indenting again. Note that if we are already inside an element with character
      // or mixed content (saveIndent is not -1), we don't overwrite saveIndent.
      // (Note also that we could use saveIndent as a flag in place of charsWritten.
      // However, it is less confusing(?) to have separate flags.)

      if (saveIndent == -1)
      {
         saveIndent = indent;
      }
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
            writer.write(ENCODING);
            writeQuotedValue(encoding);
         }
      }
   }

   private void writeAttributeValue(String value) throws IOException
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
      writer.write(value);
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
