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
 *    java Transfer -todbms &lt;map-file&gt; &lt;action-file&gt; &lt;xml-file&gt; [insert]
 *    java Transfer -toxml &lt;map-file&gt; &lt;filter-file&gt; &lt;xml-file&gt; [&lt;param&gt;=&lt;value&gt;...]
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
               specialFilename = null,
//               url = "jdbc:odbc:test";
               url = "jdbc:odbc:xmldbms";
//               url = "jdbc:easysoft://localhost:8831/xmldbms";
      Hashtable params = new Hashtable();
      boolean   toxml;
      int       action;

      try
      {
         if (argv.length < 4) throw new IllegalArgumentException("\nUsage: java Transfer -toxml <map-file> <action-file> <xml-file> [insert]\n       java Transfer -todbms <map-file> <filter-file> <xml-file> [param=value...]");

         if ((!argv[0].equals("-todbms")) && (!argv[0].equals("-toxml")))
            throw new IllegalArgumentException("\nUsage: java Transfer -toxml <map-file> <action-file> <xml-file> [insert]\n       java Transfer -todbms <map-file> <filter-file> <xml-file> [param=value...]");

         toxml = (argv[0].equals("-toxml"));
         mapFilename = argv[1];
         specialFilename = argv[2];
         xmlFilename = argv[3];
         if (toxml)
         {
            for (int i = 4; i < argv.length; i++)
            {
               String pair = argv[i];
               int equals = pair.indexOf('=');
               String param = pair.substring(0, equals);
               String value = pair.substring(equals + 1);
               params.put(param, value);
            }
            toXML(url, mapFilename, xmlFilename, specialFilename, params);
         }
         else
         {
            action = (argv.length == 5) ? Action.INSERT : Action.NONE;
            toDBMS(url, mapFilename, xmlFilename, specialFilename, action);
         }
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

   static void toDBMS(String url, String mapFilename, String xmlFilename, String actionsFilename, int action)
      throws Exception
   {
      Map              map;
      Actions          actions;
      FilterSet        filterSet;
      DataSource       ds;
      DataHandler      handler;
      TransferInfo     ti;
      DOMToDBMS        domToDBMS;
      Document         doc;

      // Create the Map object.
      map = createMap(mapFilename);

      // Create a new DBMSToDOM object and transfer the data.
      domToDBMS = new DOMToDBMS();
      domToDBMS.setFilterSetReturned(true);

      ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", url);
      handler = new GenericHandler(ds, null, null);
      ti = new TransferInfo(map, null, handler);

      doc = utils.openDocument(xmlFilename);

      if (action == Action.NONE)
      {
         actions = createActions(map, actionsFilename);
         filterSet = domToDBMS.processDocument(ti, doc, actions);
      }
      else
      {
         filterSet = domToDBMS.processDocument(ti, doc, action);
      }

      writeFilterSet(filterSet, getBasename(xmlFilename));
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

   static Actions createActions(Map map, String actionsFilename) throws Exception
   {
      ActionCompiler compiler;

      // Create a new action compiler and create the Actions.
      compiler = new ActionCompiler(utils.getXMLReader());
      return compiler.compile(map, new InputSource(getFileURL(actionsFilename)));
   }

   static FilterSet createFilterSet(Map map, String filterFilename) throws Exception
   {
      FilterCompiler compiler;

      // Create a new filter compiler and create the FilterSet.
      compiler = new FilterCompiler(utils.getXMLReader());
      return compiler.compile(map, new InputSource(getFileURL(filterFilename)));
   }

   static void writeFilterSet(FilterSet filterSet, String basename)
      throws Exception
   {
      FilterSerializer serializer;
      Writer        writer;

      // Construct a new FileWriter.
      writer = new FileWriter(basename + ".ftr");
    
      // Serialize the map.
      serializer = new FilterSerializer(writer);
      serializer.setPrettyPrinting(true, 3);
      serializer.serialize(filterSet, "filters.dtd", null);

      // Close the file.
      writer.close();
   }

   static String getFileURL(String fileName)
   {
      File   file;

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
}

