// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.factories.MapFactory_MapDocument;
import org.xmlmiddleware.xmldbms.maps.utils.MapInverter;
import org.xmlmiddleware.xmldbms.maps.utils.MapSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// Imports for the Xerces parser
import org.apache.xerces.parsers.SAXParser;

public class InverterTest
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
            System.out.println("\nUsage: java InverterTest <input-file>\n");
            return;
         }

         testInverter(argv[0]);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   static void testInverter(String filename)
      throws Exception
   {
      Map              map;
      InputSource      src;
      MapInverter      inverter = new MapInverter();

      // Create an InputSource over the schema/DTD file.

      src = new InputSource(getFileURL(filename));

      // Create, invert, reinvert, and serialize the Map

      map = createMap(src, filename);
      inverter.createDatabaseView(map);
      inverter.createXMLView(map);
      serializeMap(map, filename);
   }

   static Map createMap(InputSource src, String filename)
      throws Exception
   {
      Map map;
      MapFactory_MapDocument factory = new MapFactory_MapDocument(getSAXXMLReader());
      return factory.createMap(src);
   }

   static void serializeMap(Map map, String filename)
      throws Exception
   {
      MapSerializer serializer;
      Writer        writer;
      String        basename;

      // Get the basename of the file.
      basename = getBasename(filename);

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
   // Methods that use the Xerces parser
   // Comment these methods out if you are using a different parser.
   // ***********************************************************************

   static XMLReader getSAXXMLReader()
   {
      // WARNING! This code is specific to the Xerces parser.

      return new SAXParser();
   }

}
