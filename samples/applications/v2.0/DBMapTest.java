// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.factories.MapFactory_Database;
import org.xmlmiddleware.xmldbms.maps.utils.MapSerializer;

import de.tudarmstadt.ito.xmldbms.tools.ProcessProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

public class DBMapTest extends ProcessProperties
{
   MapFactory_Database factory = null;
   Map map = null;

   // ***********************************************************************
   // Main methods
   // ***********************************************************************

   public static void main (String[] argv)
   {
      try
      {
         if (argv.length < 1)
         {
            System.out.println("\nUsage: java DBMapTest <property-value pair>...\n");
            return;
         }

         DBMapTest t = new DBMapTest();

         t.testMap(argv);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   void testMap(String[] argv)
      throws Exception
   {
      factory = new MapFactory_Database();
      Properties props = getProperties(argv, 0);

      // Create and serialize the Map

      map = createMap(props);
      serializeMap(props);
   }

   Map createMap(Properties props)
      throws Exception
   {
      setOptions(props);

      String action = props.getProperty("Action");

      if (action.equals("CreateMapFromTable"))
      {
         return factory.createMap(props.getProperty("Catalog"), props.getProperty("Schema"), props.getProperty("Table"));
      }
      else if (action.equals("CreateMapFromTables"))
      {
         String[] tables = getNumberedProps("Table", props);
         String[] databases = getNumberedProps("Database", props);
         if (databases.length == 0) databases = new String[tables.length];
         String[] catalogs = getNumberedProps("Catalog", props);
         if (catalogs.length == 0) catalogs = new String[tables.length];
         String[] schemas = getNumberedProps("Schema", props);
         if (schemas.length == 0) schemas = new String[tables.length];
         return factory.createMap(databases, catalogs, schemas, tables);
      }
      else if (action.equals("CreateMapWithStopTables"))
      {
         String[] tables = getNumberedProps("Table", props);
         String[] databases = getNumberedProps("Database", props);
         if (databases.length == 0) databases = new String[tables.length];
         String[] catalogs = getNumberedProps("Catalog", props);
         if (catalogs.length == 0) catalogs = new String[tables.length];
         String[] schemas = getNumberedProps("Schema", props);
         if (schemas.length == 0) schemas = new String[tables.length];

         String[] stopTables = getNumberedProps("StopTable", props);
         String[] stopDatabases = getNumberedProps("StopDatabase", props);
         if (stopDatabases.length == 0) stopDatabases = new String[stopTables.length];
         String[] stopCatalogs = getNumberedProps("StopCatalog", props);
         if (stopCatalogs.length == 0) stopCatalogs = new String[stopTables.length];
         String[] stopSchemas = getNumberedProps("StopSchema", props);
         if (stopSchemas.length == 0) stopSchemas = new String[stopTables.length];

         return factory.createMap(databases, catalogs, schemas, tables, stopDatabases, stopCatalogs, stopSchemas, stopTables);
      }
      else if (action.equals("CreateMapFromCatalog"))
      {
      }
      else if (action.equals("CreateMapFromSchema"))
      {
      }
      return null;
   }

   void setOptions(Properties props)
      throws Exception
   {
      setDatabaseOptions(props);

      factory.columnsAreElementTypes(isTrue(props.getProperty("UseElementTypes")));
      factory.followPrimaryKeys(isTrue(props.getProperty("FollowPrimaryKeys")));
      factory.followForeignKeys(isTrue(props.getProperty("FollowForeignKeys")));
   }

   boolean isTrue(String value)
   {
      if (value == null) return true;
      return value.equals("True");
   }

   void setDatabaseOptions(Properties props)
      throws Exception
   {
      Connection conn;

      String driver = props.getProperty("Driver");
      String url = props.getProperty("URL");
      String user = props.getProperty("User");
      String password = props.getProperty("Password");
      Class realDriverClass = Class.forName(driver);

      // The following is hard coded to use the ADDriver driver as a wrapper
      // around the input driver. If you want to avoid this nonsense -- I needed
      // it to implement some metadata functions for MS Access -- comment out
      // this code and uncomment the following code.

      ADDriver wrapperDriver = new ADDriver();
      conn = wrapperDriver.connect(url, new Properties());

      // Normal code follows...
//      conn = DriverManager.getConnection(url, user, password);

      factory.setConnection(conn);
   }

   void serializeMap(Properties props)
      throws Exception
   {
      MapSerializer serializer;
      Writer        writer;

      // Construct a new FileWriter.
      writer = new FileWriter(props.getProperty("MapFile"));
    
      // Serialize the map.
      serializer = new MapSerializer(writer);
      serializer.setPrettyPrinting(true, 3);
      serializer.serialize(map, "xmldbms2.dtd", null);

      // Close the file.
      writer.close();
   }
}
