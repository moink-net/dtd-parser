// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.factories.MapFactory_MapDocument;
import org.xmlmiddleware.xmldbms.maps.utils.MapSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;

/*
// Imports for the Oracle version 2 parser
import oracle.xml.parser.v2.SAXParser;
*/

/*
// Imports for the Sun parser (none needed -- see getSAXParser())
*/

// Imports for the Xerces parser
import org.apache.xerces.parsers.SAXParser;

public class MapTest
{

   // ***********************************************************************
   // Main methods
   // ***********************************************************************

   public static void main (String[] argv)
   {
      try
      {
         if (argv.length != 1)
         {
            System.out.println("\nUsage: java MapTest <input-file>\n");
            return;
         }

         testMap(argv[0]);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   static void testMap(String filename)
      throws Exception
   {
      Map              map;
      String           basename;
      InputSource      src;

      // Create an InputSource over the schema/DTD file.

      src = new InputSource(getFileURL(filename));

      // Get the basename of the file.

      basename = getBasename(filename);

      // Create and serialize the Map

      map = createMap(src, filename);
      serializeMap(map, basename);
   }

   static Map createMap(InputSource src, String filename)
      throws Exception
   {
      MapFactory_MapDocument factory = new MapFactory_MapDocument(getSAXParser());
      return factory.createMap(src);
   }

   static void serializeMap(Map map, String basename)
      throws Exception
   {
      MapSerializer serializer;
      Writer        writer;

      // Construct a new FileWriter.
      writer = new FileWriter(basename + ".out");
    
      // Serialize the map.
      serializer = new MapSerializer(writer);
      serializer.setPrettyPrinting(true, 3);
      serializer.serialize(map, "xmldbms2.dtd", null);

      // Close the file.
      writer.close();
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
