// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;

import de.tudarmstadt.ito.xmldbms.tools.MapEngine;
import java.util.Properties;

/**
 * Properties-driven and command line interface to the map factories.
 *
 * <p>GenerateMap provides a properties-driven interface to the map factories.
 * Properties may be passed either programmatically through
 * dispatch(Properties) or on a command line. The command line syntax for
 * GenerateMap is:</p>
 *
 * <pre>
 * java GenerateMap <property>=<value> [<property>=<value>...]
 * </pre>
 *
 * <p>The special property File states that the value is the name
 * of a Java properties file; Transfer reads the properties from
 * this file. If any property/value pairs contain spaces, they must
 * be enclosed in quotes. Property/value pairs are read in order;
 * if a property is listed more than once, the last value is used.</p>
 *
 * <p>There are three general classes of properties. Database properties
 * (Driver, URL, User, and Password) provide information used to connect
 * to the database and parser properties (ParserUtilsClass) provide information
 * used to work with the XML parser.</p>
 *
 * The final class is action properties. These tell GenerateMap what actions
 * to perform, such as generating a map from a DTD. The main action property
 * is Action. The value of Action dictates what other properties are needed.
 * GenerateMap accepts the following Action values:</p>
 *
 * <p>"CreateMapFromXMLSchema" (DDML only). Uses properties:<br />
 *   SchemaFile<br />
 *   OrderColumns ("Yes", "No")<br />
 *   Catalog<br />
 *   Schema<br />
 *   SQLSeparator<br />
 *   database properties<br />
 *   parser properties</p>
 *
 * <p>"CreateMapFromDTD". Uses properties:<br />
 *   SchemaFile<br />
 *   OrderColumns ("Yes", "No")<br />
 *   Catalog (not implemented)<br />
 *   Schema (not implemented)<br />
 *   SQLSeparator<br />
 *   Prefix1, Prefix2, ...<br />
 *   NamespaceURI1, NamespaceURI2, ...<br />
 *   database properties</p>
 *
 * <p>"CreateMapFromTable" (not implemented). Uses properties:<br />
 *   database properties<br />
 *   Table<br />
 *   Basename<br />
 *   SQLSeparator<br />
 *   database properties</p>
 *
 * <p>"CreateMapFromTables" (not implemented). Uses properties:<br />
 *   database properties<br />
 *   Table1, Table2, ...<br />
 *   Basename<br />
 *   SQLSeparator<br />
 *   database properties</p>
 *
 * <p>"CreateMapFromSelect" (not implemented). Uses properties:<br />
 *   database properties<br />
 *   Select<br />
 *   Basename<br />
 *   SQLSeparator<br />
 *   database properties</p>
 *
 * <p>For descriptions of individual properties, see
 * de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps
 * @see de.tudarmstadt.ito.xmldbms.MapEngine
 */

public class GenerateMap extends ProcessProperties
{
   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String COLON = ";";

   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
	* Construct a GenerateMap object.
	*/
   public GenerateMap()
   {
	  super();
   }   

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
	* Runs GenerateMap from a command line.
	*
	* <p>See introduction for command line syntax.</p>
	*/

   public static void main(String[] args)
   {
	  GenerateMap g;
	  Properties props;

	  try
	  {
		 g = new GenerateMap();

		 if (args.length < 1)
		 {
			System.out.println("Usage: GenerateMap <property>=<value> [<property>=<value>...]\n" +
			"See the documentation for a list of valid properties");
			return;
		 }

		 props = g.getProperties(args, 0);
		 g.dispatch(props);
	  }
	  catch (Exception e)
	  {
		 e.printStackTrace();
	  }
   }   

   /**
	* Executes the action specified by the Action property.
	*
	* <p>The props argument must contain the parameters required
	* by the action, as well as the database and parser properties
	* needed to execute the action. For details, see the introduction.</p>

	* @param props Properties specifying what action to take.
	*/
   public void dispatch(Properties props)
	  throws Exception
   {
	 MapEngine engine = new MapEngine();
	 String         action;

	 // Set up the parser and database
	 engine.setParserProperties(props);
	 engine.setDatabaseProperties(props);

	 // Get the action
	 action = props.getProperty(XMLDBMSProps.ACTION);
	 if (action == null)
	   throw new IllegalArgumentException("Action property not specified.");

	 // Dispatch the action
	 if (action.equals(XMLDBMSProps.CREATEMAPFROMXMLSCHEMA))
	 {
	   dispatchCreateMapFromXMLSchema(engine, props);
	 }
	 else if (action.equals(XMLDBMSProps.CREATEMAPFROMDTD))
	 {
	   dispatchCreateMapFromDTD(engine, props);
	 }
	 else if (action.equals(XMLDBMSProps.CREATEMAPFROMTABLE))
	 {
	   dispatchCreateMapFromTable(engine, props);
	 }
	 else if (action.equals(XMLDBMSProps.CREATEMAPFROMTABLES))
	 {
	   dispatchCreateMapFromTables(engine, props);
	 }
	 else if (action.equals(XMLDBMSProps.CREATEMAPFROMSELECT))
	 {
	   dispatchCreateMapFromSelect(engine, props);
	 }
	 else
	 {
	   throw new IllegalArgumentException("Unknown value of Action property: " + action);
	 }
   }   

   // ************************************************************************
   // Private methods
   // ************************************************************************

   private void dispatchCreateMapFromXMLSchema(MapEngine engine, Properties props)
	  throws Exception
   {
	 String  schemaFilename, catalog, schema, sqlSeparator;
	 boolean orderColumns;

	 schemaFilename = (String)props.getProperty(XMLDBMSProps.SCHEMAFILE);
	 catalog = (String)props.getProperty(XMLDBMSProps.CATALOG);
	 schema = (String)props.getProperty(XMLDBMSProps.SCHEMA);
	 sqlSeparator = getSQLSeparator(props);
	 orderColumns = getYesNo((String)props.getProperty(XMLDBMSProps.ORDERCOLUMNS));

	 engine.createMap(schemaFilename, catalog, schema, sqlSeparator, orderColumns);
   }                           

   private  void dispatchCreateMapFromDTD(MapEngine engine, Properties props)
	  throws Exception
   {
	 String   dtdFilename, catalog, schema, sqlSeparator;
	 String[] prefixes, namespaceURIs;
	 boolean  orderColumns;

	 dtdFilename = (String)props.getProperty(XMLDBMSProps.SCHEMAFILE);
	 catalog = (String)props.getProperty(XMLDBMSProps.CATALOG);
	 schema = (String)props.getProperty(XMLDBMSProps.SCHEMA);
	 sqlSeparator = getSQLSeparator(props);
	 orderColumns = getYesNo((String)props.getProperty(XMLDBMSProps.ORDERCOLUMNS));

	 prefixes = getNumberedProps(XMLDBMSProps.PREFIX, props);
	 namespaceURIs = getNumberedProps(XMLDBMSProps.NAMESPACEURI, props);

	 //System.out.println("DispatchCreateMap from DTD "+dtdFilename);
	 engine.createMap(dtdFilename, catalog, schema, sqlSeparator, orderColumns, prefixes, namespaceURIs);
   }                              

   private void dispatchCreateMapFromTables(MapEngine engine, Properties props)
	  throws Exception
   {
	 String   basename, sqlSeparator;
	 String[] tables;

	 tables = getNumberedProps (XMLDBMSProps.TABLE, props);
	 basename = (String)props.getProperty(XMLDBMSProps.BASENAME);
	 sqlSeparator = getSQLSeparator(props);

	 engine.createMap(tables, basename, sqlSeparator);
   }                  

   private  void dispatchCreateMapFromSelect(MapEngine engine, Properties props)
	  throws Exception
   {
	 String select, basename, sqlSeparator;

	 select = (String)props.getProperty(XMLDBMSProps.SELECT);
	 basename = (String)props.getProperty(XMLDBMSProps.BASENAME);
	 sqlSeparator = getSQLSeparator(props);

	 engine.createMap(select, basename, sqlSeparator);
   }                  

   private void dispatchCreateMapFromTable(MapEngine engine, Properties props)
	  throws Exception
   {
	 String   basename, sqlSeparator;
	 String[] tables = new String[1];

	 tables[0] = (String)props.getProperty(XMLDBMSProps.TABLE);
	 basename = (String)props.getProperty(XMLDBMSProps.BASENAME);
	 sqlSeparator = getSQLSeparator(props);

	 engine.createMap(tables, basename, sqlSeparator);
   }   

   private String getSQLSeparator(Properties props)
   {
	  String separator;

	  separator = props.getProperty(XMLDBMSProps.SQLSEPARATOR);
	  if (separator == null)
	  {
		 separator = COLON;
	  }
	  return separator;
   }   
}