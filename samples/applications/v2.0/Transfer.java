// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.helpers.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.factories.*;
import org.xmlmiddleware.db.*;
import org.xmlmiddleware.domutils.*;
import org.xmlmiddleware.domutils.helpers.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.w3c.dom.Document;


/**
 * This application accepts a map document name, an XML document name, and
 * (in the case of transferring data from the database to an XML document) a
 * table name and key value. It transfers data in the specified direction.
 *
 * <P>The command line syntax for this application is:</P>
 *
 * <PRE>
 *    java Transfer {-todbms|-toxml} &lt;map-file&gt; &lt;xml-file&gt; [&lt;table-name&gt; &lt;key-value&gt;...]
 * </PRE>
 *
 * <P>where:</P>
 *
 * <pre>
 *    -todbms | -toxml indicates the direction of data transfer
 *    &lt;map-file&gt; is the name of the XML-DBMS map document
 *    &lt;xml-file&gt; is the name of the input or output XML document
 *    &lt;table-name&gt; is the name of the table containing the data to retrieve
 *       (-toxml direction only)
 *    &lt;key-value&gt;... is one or more values in a (multi-part) key
 *       (-toxml direction only)
 * </pre>
 *
 * <P>For this application to work, there must be an ODBC data source named
 * "xmldbms" and the tables used to store data must have already been created
 * in the data source. This application uses the Sun JDBC-ODBC Bridge, but
 * includes commented-out code for using the Easysoft JDBC-ODBC Bridge. If
 * you are using a different JDBC driver, you will need to modify the code
 * to use your driver.</p>
 *
 * <p>This application is also hard-coded to use the Oracle version 2 XML parser
 * and DOM implementation. However, it also includes commented-out code to use
 * the Xerces and Sun XML parsers and DOM implementation. To modify Transfer
 * for your XML parser and DOM implementation, comment/uncomment the appropriate
 * import statements, code in toDBMS() and toXML(), versions of
 * getSAXParser(), openDocument(), and writeDocument(). If you are using a parser
 * or DOM implementation other than Oracle version 2, Xerces, or Sun, you will
 * need to modify the code accordingly.</p>
 *
 * <p>If the map document specifies that keys are to be generated, the default
 * KeyGenerator implementation is used; for more information, see
 * de.tudarmstadt.ito.xmldbms.helper.KeyGeneratorImpl.</P>
 */

public class Transfer
{
   // ***********************************************************************
   // Main methods
   // ***********************************************************************

   static ParserUtils utils = new ParserUtilsXerces();

   public static void main (String[] argv)
   {
      String   mapFilename = null,
               xmlFilename = null,
               url = "jdbc:odbc:test",
//               url = "jdbc:odbc:xmldbms",
//               url = "jdbc:easysoft://localhost:8831/xmldbms",
               tableName = null;
      Object[] key = null;
      boolean  toxml;

      try
      {
         if (argv.length < 3) throw new IllegalArgumentException();

         if ((!argv[0].equals("-todbms")) && (!argv[0].equals("-toxml")))
            throw new IllegalArgumentException();

         toxml = (argv[0].equals("-toxml"));
         mapFilename = argv[1];
         xmlFilename = argv[2];
         if (toxml)
         {
            if (argv.length < 5) throw new IllegalArgumentException();

            tableName = argv[3];

            key = new Object[argv.length - 4];
            for (int i = 4; i < argv.length; i++)
            {
               key[i - 4] = argv[i];
            }
         }

         if (toxml)
         {
            toXML(mapFilename, xmlFilename, url, tableName, key);
         }
         else
         {
            toDBMS(mapFilename, xmlFilename, url);
         }
      }
      catch (IllegalArgumentException iae)
      {
         System.out.println("\nUsage: java Transfer {-todbms|-toxml} <map-file> <xml-file> [<table-name> <key-value>...]\n<table-name> and <key-value> are required when -toxml is chosen.\n");
      }
      catch (java.sql.SQLException s)
      {
         System.out.println("SQLSTATE: " + s.getSQLState());
         s.printStackTrace();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   static void toDBMS(String mapFilename, String xmlFilename, String url)
      throws Exception
   {
/*
      Connection       conn1 = null, conn2 = null;
      Map              map;
      Document         doc;
      DOMToDBMS        domToDBMS;
//      KeyGeneratorImpl keyGenerator = null;
      ParserUtils      utils = new ParserUtilsXerces();

      try
      {
         // Get a JDBC driver. You will need to modify this code if
         // you are using a different driver.
         Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
//         Class.forName("easysoft.sql.jobDriver");

         // Connect to the database. 
//         conn1 = DriverManager.getConnection(url);
         conn2 = DriverManager.getConnection(url);

         // Create and initialize a key generator
//         keyGenerator = new KeyGeneratorImpl(conn1);
//         keyGenerator.initialize();

         // Create the Map object and open the XML document.
         map = createMap(mapFilename, conn2);
         doc = utils.openDocument(xmlFilename);

         // Create a new DOMToDBMS object and transfer the data. Note that
         // different DOM implementations need different NameQualifier
         // implementations.
         domToDBMS = new DOMToDBMS(utils);
//         domToDBMS = new DOMToDBMS(map, keyGenerator, new NQ_Sun());
//         domToDBMS = new DOMToDBMS(map, keyGenerator, new NQ_DOM2()); // Xerces
         domToDBMS.storeDocument(doc);
      }
      finally
      {
         if (conn1 != null) conn1.close();
         if (conn2 != null) conn2.close();
      }
*/
   }

   static void toXML(String mapFilename, String xmlFilename, String url, String tableName, Object[]key)
      throws Exception
   {
      Map              map;
      DataSource       ds;
      DataHandler      handler;
      TransferInfo     ti;
      DBMSToDOM        dbmsToDOM;
      Document         doc;

      // Create the Map object.
      map = createMap(mapFilename);

      // Create a new DBMSToDOM object and transfer the data.
      dbmsToDOM = new DBMSToDOM(utils);

      ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", url);
      handler = new GenericHandler(ds, null, null);
      ti = new TransferInfo(map, null, handler);

      doc = dbmsToDOM.retrieveDocument(ti, null, null, null, tableName, key, null, null);

/*
      // this code tests tokenlist.map. it shows the use of multiple tables and keys
      // as well as a wrapper element.
      String[] wrapper = new String[1];
      wrapper[0] = "wrapper";
      String[] uri = new String[1];

      Integer[][] keys = new Integer[3][];
      Integer[] intKey = new Integer[1];
      intKey[0] = new Integer(1);
      keys[0] = intKey;
      keys[1] = intKey;
      keys[2] = intKey;
      String[] tableNames = new String[3];
      tableNames[0] = "Root";
      tableNames[1] = "Price2";
      tableNames[2] = "Price3";
      doc = dbmsToDOM.retrieveDocument(ti, null, null, null, tableNames, keys, uri, wrapper);
*/

/*
      // this code shows how to do heterogeneous joins.
      ti = new TransferInfo(map);

      ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:orders");
      handler = new GenericHandler(ds, null, null);
      ti.addDataHandler("orders", handler);

      ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:items");
      handler = new GenericHandler(ds, null, null);
      ti.addDataHandler("items", handler);

      ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:customers");
      handler = new GenericHandler(ds, null, null);
      ti.addDataHandler("customers", handler);

      ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:parts");
      handler = new GenericHandler(ds, null, null);
      ti.addDataHandler("parts", handler);

      doc = dbmsToDOM.retrieveDocument(ti, "orders", null, null, tableName, key, null, null);
*/

      // Write the DOM tree to a file.
      utils.writeDocument(doc, xmlFilename, null);
   }

   // ***********************************************************************
   // General utility methods
   // ***********************************************************************

   static Map createMap(String mapFilename) throws Exception
   {
      MapCompiler compiler;

      // Create a new map factory and create the Map.
      compiler = new MapCompiler(utils.getXMLReader());
      return compiler.compile(new InputSource(getFileURL(mapFilename)));
   }

   static String getFileURL(String fileName)
   {
      File   file;

      file = new File(fileName);
      return "file:///" + file.getAbsolutePath();
   }
}

