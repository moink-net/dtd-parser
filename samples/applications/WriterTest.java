// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

import org.xmlmiddleware.utils.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import org.xml.sax.*;

// Imports for the Oracle version 2 parser
import oracle.xml.parser.v2.SAXParser;

/*
// Imports for the Sun parser (none needed -- see getSAXParser())
*/

/*
// Imports for the Xerces parser
import org.apache.xerces.parsers.SAXParser;
*/

public class WriterTest implements DocumentHandler
{
   XMLWriter xmlWriter;

   // ***********************************************************************
   // Main methods
   // ***********************************************************************

   public static void main (String[] argv)
   {
      try
      {
         if (argv.length != 1)
         {
            System.out.println("\nUsage: java WriterTest <input-file>\n");
            return;
         }

         WriterTest t = new WriterTest();
         t.testWriter(argv[0]);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void testWriter(String filename)
      throws Exception
   {
      String           basename;
      InputSource      src;
      FileWriter       writer;

      // Create an InputSource over the file.

      src = new InputSource(getFileURL(filename));

      // Get the basename of the file and open an output file.

      basename = getBasename(filename);
      writer = new FileWriter(basename + ".out");
      xmlWriter = new XMLWriter(writer);

      // Get the parser and parse the input file;

      Parser p = getSAXParser();
      p.setDocumentHandler(this);
      p.parse(src);
      writer.close();
   }


   // ***********************************************************************
   // SAX methods
   // ***********************************************************************

   public void setDocumentLocator (Locator locator)
   {
   }

   public void startDocument () throws SAXException
   {
      try {
         xmlWriter.setPrettyPrinting(true, 3);
         xmlWriter.writeXMLDecl();
      } catch (Exception e) {
         throw new SAXException(e);
      }
   }

   public void endDocument() throws SAXException
   {
   }

   public void startElement (String name, AttributeList attrs)
     throws SAXException
   {
      int len = attrs.getLength();

      try {
         xmlWriter.allocateAttrs(len);
         for (int i = 0; i < len; i++)
         {
            xmlWriter.attrs[i] = attrs.getName(i);
            xmlWriter.values[i] = attrs.getValue(i);
         }
         xmlWriter.writeElementStart(name, len, false);
      } catch (Exception e) {
         throw new SAXException(e);
      }
   }

   public void endElement (String name) throws SAXException
   {
      try {
         xmlWriter.writeElementEnd(name);
      } catch (Exception e) {
         throw new SAXException(e);
      }
   }

   public void characters (char ch[], int start, int length)
     throws SAXException
   {
      try {
         xmlWriter.writeCharacters(ch, start, length);
      } catch (Exception e) {
         throw new SAXException(e);
      }
   }
   
   public void ignorableWhitespace (char ch[], int start, int length)
      throws SAXException
   {
   }

   public void processingInstruction (String target, String data)
      throws SAXException
   {
   }

   // ***********************************************************************
   // General utility methods
   // ***********************************************************************

   static String getFileURL(String fileName)
   {
      File   file;

      // Construct a file URL for the file.

      file = new File(fileName);
      return "file:///" + file.getAbsolutePath();
   }

   static String getBasename(String filename)
   {
      int    period;

      // Get the basename of the file.

      period = filename.lastIndexOf('.', filename.length());
      if (period == -1)
      {
         return filename;
      }
      else
      {
         return filename.substring(0, period);
      }
   }

   static String getExtension(String filename)
   {
      int    period;

      // Get the file extension.

      period = filename.lastIndexOf('.', filename.length());
      if (period == -1)
      {
         return "";
      }
      else
      {
         return filename.substring(period + 1, filename.length()).toUpperCase();
      }
   }

   // ***********************************************************************
   // Methods that use the Oracle version 2 parser
   // Comment these methods out if you are using a different parser.
   // ***********************************************************************
/*
   static Parser getSAXParser()
   {
      // WARNING! This code is specific to the Oracle parser.

      SAXParser parser;

      // Instantiate the parser and set various options
      parser = new SAXParser();
      parser.setValidationMode(true);
      return parser;
   }
*/
   // ***********************************************************************
   // Methods that use the Sun parser
   // Comment these methods out if you are using a different parser.
   // ***********************************************************************
/*
   static Parser getSAXParser()
   {
      // WARNING! This code is specific to the Sun parser.

      return new com.sun.xml.parser.Parser();
   }
*/

   // ***********************************************************************
   // Methods that use the Xerces parser
   // Comment these methods out if you are using a different parser.
   // ***********************************************************************

   static Parser getSAXParser()
   {
      // WARNING! This code is specific to the Xerces parser.

      return new SAXParser();
   }

}
