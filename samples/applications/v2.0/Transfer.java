// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.actions.*;
import org.xmlmiddleware.xmldbms.filters.*;
import org.xmlmiddleware.xmldbms.helpers.*;
import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.factories.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;
import org.xmlmiddleware.db.*;
import org.xmlmiddleware.domutils.*;
import org.xmlmiddleware.domutils.helpers.*;

import java.io.*;
import javax.sql.*;
import java.sql.*;
import java.util.*;

import org.xml.sax.*;
import org.w3c.dom.*;


/**
 * This application accepts a map document name, an XML document name, and
 * (in the case of transferring data from the database to an XML document) a
 * a filter document name and a series of parameter/value pairs.
 * It transfers data in the specified direction.
 *
 * <p>The command line syntax for this application is:</p>
 *
 * <pre>
 *    java Transfer -todbms &lt;map-file&gt; &lt;xml-file&gt;
 *    java Transfer -toxml &lt;map-file&gt; &lt;xml-file&gt; &lt;filter-file&gt; [&lt;param&gt;=&lt;value&gt;...]
 * </pre>
 *
 * <p>where:</p>
 *
 * <pre>
 *    -todbms | -toxml indicates the direction of data transfer
 *    &lt;map-file&gt; is the name of the XML-DBMS map document
 *    &lt;xml-file&gt; is the name of the input or output XML document
 *    &lt;filter-file&gt; is the name of the filter document
 *       (-toxml direction only)
 *    &lt;param&gt;=&lt;value&gt;... is zero or more parameter values
 *       (-toxml direction only)
 * </pre>
 *
 * <p>For this application to work, there must be an ODBC data source named
 * "xmldbms" and the tables used to store data must have already been created
 * in the data source. This application uses the Sun JDBC-ODBC Bridge, but
 * includes commented-out code for using the Easysoft JDBC-ODBC Bridge. If
 * you are using a different JDBC driver, you will need to modify the code
 * to use your driver.</p>
 *
 * <p>This application is also hard-coded to use the Xerces implementation of ParserUtils.
 * If you are using a different parser, specify the correct implementation of ParserUtils.</p>
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
               filterFilename = null,
//               url = "jdbc:odbc:test";
               url = "jdbc:odbc:xmldbms";
//               url = "jdbc:easysoft://localhost:8831/xmldbms";
      Hashtable params = new Hashtable();
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
            filterFilename = argv[3];

            for (int i = 4; i < argv.length; i++)
            {
               String pair = argv[i];
               int equals = pair.indexOf('=');
               String param = pair.substring(0, equals);
               String value = pair.substring(equals + 1);
               params.put(param, value);
            }
         }

         if (toxml)
         {
            toXML(url, mapFilename, xmlFilename, filterFilename, params);
         }
         else
         {
            toDBMS(url, mapFilename, xmlFilename);
         }
      }
//      catch (IllegalArgumentException iae)
//      {
//         System.out.println("\nUsage: java Transfer {-todbms|-toxml} <map-file> <xml-file> [<table-name> <key-value>...]\n<table-name> and <key-value> are required when -toxml is chosen.\n");
//      }
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

   static void toDBMS(String url, String mapFilename, String xmlFilename)
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

   static void toXML(String url, String mapFilename, String xmlFilename, String filterFilename, Hashtable params)
      throws Exception
   {
      Map              map;
      FilterSet        filterSet;
      DataSource       ds;
      DataHandler      handler;
      TransferInfo     ti;
      DBMSToDOM        dbmsToDOM;
      Document         doc;

      // Create the Map object.
      map = createMap(mapFilename);

      // Create the FilterSet object
      filterSet = createFilterSet(map, filterFilename);

      // Create a new DBMSToDOM object and transfer the data.
      dbmsToDOM = new DBMSToDOM(utils);

      ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", url);
      handler = new GenericHandler(ds, null, null);
      ti = new TransferInfo(map, null, handler);

//      doc = dbmsToDOM.retrieveDocument(ti, filterSet, params, null);

/*
      // this code passes a node to retrieveDocument

      DOMImplementation domImpl;
      DocumentType      docType;
      Element           root;

      domImpl = utils.getDOMImplementation();
      docType = domImpl.createDocumentType("foo:bar", null, null);
      doc = domImpl.createDocument("http://www.bar.org", "foo:bar", docType);
      Node node1 = doc.getFirstChild().getNextSibling();
      Node node2 = doc.createElementNS(null, "bleagh");
      node1.appendChild(node2);

      doc = dbmsToDOM.retrieveDocument(ti, filterSet, params, node2);
*/

      // this code uses a result set with retrieve document

      Connection conn1=ds.getConnection();
      Statement s1 = conn1.createStatement();
      ResultSet rs1=s1.executeQuery("SELECT * FROM Orders");
      Statement s2 = conn1.createStatement();
      ResultSet rs2=s2.executeQuery("SELECT * FROM Customers");
      MetadataInitializer mi = new MetadataInitializer(map);
      mi.initializeMetadata(null, null, null, "Orders", rs1);
      mi.initializeMetadata(null, null, null, "Customers", rs2);

      Hashtable h = new Hashtable();
      h.put("Default", rs1);
      h.put("Customers", rs2);

      dbmsToDOM.setDTDInfo("http://www.sales.org/sales.dtd", null);
      doc = dbmsToDOM.retrieveDocument(ti, filterSet, h, params, null);

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

      // Create a new map compiler and create the Map.
      compiler = new MapCompiler(utils.getXMLReader());
      return compiler.compile(new InputSource(getFileURL(mapFilename)));
   }

   static FilterSet createFilterSet(Map map, String filterFilename) throws Exception
   {
      FilterCompiler compiler;

      // Create a new filter compiler and create the FilterSet.
      compiler = new FilterCompiler(utils.getXMLReader());
      return compiler.compile(map, new InputSource(getFileURL(filterFilename)));
   }

   static String getFileURL(String fileName)
   {
      File   file;

      file = new File(fileName);
      return "file:///" + file.getAbsolutePath();
   }
}

