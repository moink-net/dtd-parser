// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

import org.xmlmiddleware.xmldbms.actions.*;
import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.factories.MapCompiler;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// Imports for the Xerces parser
import org.apache.xerces.parsers.SAXParser;

public class ActionTest
{

   // ***********************************************************************
   // Main methods
   // ***********************************************************************

   public static void main (String[] argv)
   {
      try
      {
         if (argv.length != 2)
         {
            System.out.println("\nUsage: java ActionTest <map-file> <action-file>\n");
            return;
         }

         testAction(argv[0], argv[1]);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   static void testAction(String mapFilename, String actionFilename)
      throws Exception
   {
      InputSource      src;
      Map              map;
      Actions          actions;

      // Create an InputSource over the schema/DTD file.

      src = new InputSource(getFileURL(mapFilename));
      map = createMap(src);

      src = new InputSource(getFileURL(actionFilename));
      actions = createActions(map, src);
   }

   static Map createMap(InputSource src)
      throws Exception
   {
      MapCompiler compiler = new MapCompiler(getSAXXMLReader());
      return compiler.compile(src);
   }

   static Actions createActions(Map map, InputSource src)
      throws Exception
   {
      ActionCompiler compiler = new ActionCompiler(getSAXXMLReader());
      return compiler.compile(map, src);
   }

/*
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
*/

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
   // Methods that use the Xerces parser
   // Comment these methods out if you are using a different parser.
   // ***********************************************************************

   static XMLReader getSAXXMLReader()
   {
      // WARNING! This code is specific to the Xerces parser.

      return new SAXParser();
   }
}
