// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;

import de.tudarmstadt.ito.domutils.DocumentFactoryException;
import de.tudarmstadt.ito.xmldbms.DOMToDBMS;
import de.tudarmstadt.ito.xmldbms.InvalidMapException;
import de.tudarmstadt.ito.xmldbms.KeyException;
import de.tudarmstadt.ito.xmldbms.TransferEngine;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.w3c.dom.DOMException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * Properties-driven and command line interface to XML-DBMS.
 *
 * <p>Transfer provides a properties-driven interface to XML-DBMS.
 * Properties may be passed either programmatically through
 * dispatch(Properties) or on a command line.</p>
 *
 * <p>The command line form of Transfer is:</p>
 *
 * <pre>
 * java Transfer <property>=<value> [<property>=<value>...]
 * </pre>
 *
 * <p>The special property File states that the value is the name
 * of a Java properties file; Transfer reads the properties from
 * this file. If any property/value pairs contain spaces, they must
 * be enclosed in quotes. Property/value pairs are read in order;
 * if a property is listed more than once, the last value is used.</p>
 *
 * <p>There are four general classes of properties. Database properties
 * (Driver, URL, User, and Password) provide information used to connect
 * to the database. Parser properties (ParserUtilsClass,
 * DocumentFactoryClass, and NameQualifierClass) provide information used
 * to work with the XML parser / DOM implementation. Key generator properties
 * (see the documentation for your key generator) are used to initialize
 * the key generator.</p>
 *
 * The final class is action properties. These tell Transfer what actions
 * to perform, such as transferring data from an XML document to the
 * database. The main action property is Action. The value of Action
 * dictates what other properties are needed. Transfer accepts the
 * following Action values:</p>
 *
 * <p>"StoreDocument". Uses properties:<br />
 *   MapFile<br />
 *   XMLFile<br />
 *   CommitMode ("AfterInsert", "AfterDocument")<br />
 *   KeyGeneratorClass<br />
 *   key generator initialization properties<br />
 *   database properties<br />
 *   parser properties</p>
 *
 * <p>"RetrieveDocumentBySQL". Uses properties:<br />
 *   MapFile<br />
 *   XMLFile<br />
 *   Select<br />
 *   database properties<br />
 *   parser properties</p>
 *
 * <p>"RetrieveDocumentByKey". Uses properties:<br />
 *   MapFile<br />
 *   XMLFile<br />
 *   Table<br />
 *   Key1, Key2, ...<br />
 *   database properties<br />
 *   parser properties</p>
 *
 * <p>"RetrieveDocumentByKeys". Uses properties:<br />
 *   MapFile<br />
 *   XMLFile<br />
 *   Table1, Table2, ...<br />
 *   Key1_1, Key1_2, ... Key2_1, Key2_2, ...<br />
 *   database properties<br />
 *   parser properties</p>
 *
 * <p>For descriptions of individual properties, see
 * de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps.</p>
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see de.tudarmstadt.ito.xmldbms.tools.XMLDBMSProps
 * @see de.tudarmstadt.ito.xmldbms.TransferEngine
 */

public class Transfer extends ProcessProperties
{
   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
	* Construct a Transfer object.
	*/
   public Transfer()
   {
	  super();
   }   

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
	* Runs Transfer from a command line.
	*
	* <p>See introduction for command line syntax.</p>
	*/

   public static void main(String[] args)
   {
	 Properties props;
	 Transfer trans = new Transfer();

	 try
	 {
		if (args.length < 1)
		{
		  System.out.println("Usage: Transfer <property>=<value> [<property>=<value>...]\n" +
					 "See the documentation for a list of valid properties.");
		  return;
		}

		props = trans.getProperties(args, 0);
		trans.dispatch(props);
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
	 TransferEngine engine = new TransferEngine();
	 String         action;

	 // Set up the parser and database
	 engine.setParserProperties(props);
	 engine.setDatabaseProperties(props);

	 // Get the action
	 action = props.getProperty(XMLDBMSProps.ACTION);
	 if (action == null){
	   throw new IllegalArgumentException("Action property not specified.");
	 }
	 // Dispatch the action
	 if (action.equals(XMLDBMSProps.STOREDOCUMENT))
	 {
	   dispatchStoreDocument(engine, props);
	 }
	 else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYSQL))
	 {
	   dispatchRetrieveDocumentBySQL(engine, props);
	 }
	 else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYKEY))
	 {
	   dispatchRetrieveDocumentByKey(engine, props);
	 }
	 else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYKEYS))
	 {
	   dispatchRetrieveDocumentByKeys(engine, props);
	 }
	 else
	 {
	   throw new IllegalArgumentException("Unknown value of Action property: " + action);
	 }
	 
   }                        

   // ************************************************************************
   // Private methods
   // ************************************************************************

   private void dispatchStoreDocument(TransferEngine engine, Properties props)
	  throws Exception
   {
	 String mapFilename, xmlFilename, keyGeneratorClass;
	 int    commitMode;
	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = (String)props.getProperty(XMLDBMSProps.XMLFILE);

	
	 if (props.getProperty(XMLDBMSProps.COMMITMODE) == null)
	 { commitMode = DOMToDBMS.COMMIT_AFTERDOCUMENT;}
	 else {commitMode = getCommitMode((String)props.getProperty(XMLDBMSProps.COMMITMODE));}
	 
	 keyGeneratorClass = (String)props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);

	 engine.storeDocument(mapFilename, xmlFilename, commitMode, keyGeneratorClass, props);
   }                                                         

   private void dispatchRetrieveDocumentBySQL(TransferEngine engine, Properties props) 
	  throws Exception
   {
	 String mapFilename, xmlFilename, select;

	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = (String)props.getProperty(XMLDBMSProps.XMLFILE);
	 select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);

	 engine.retrieveDocument(mapFilename, xmlFilename, select);
   }                           

   private void dispatchRetrieveDocumentByKey(TransferEngine engine, Properties props)
	  throws Exception
   {
	 String   mapFilename, xmlFilename, table;
	 String[] key;

	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = (String)props.getProperty(XMLDBMSProps.XMLFILE);
	 table = (String)props.getProperty(XMLDBMSProps.TABLE);
	 key = getNumberedProps(XMLDBMSProps.KEY, props);

	 engine.retrieveDocument(mapFilename, xmlFilename, table, key);
   }                        

   private void dispatchRetrieveDocumentByKeys(TransferEngine engine, Properties props)
	  throws Exception
   {
	 String     mapFilename, xmlFilename;
	 String[]   tables;
	 String[][] keys;

	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = (String)props.getProperty(XMLDBMSProps.XMLFILE);
	 tables = getNumberedProps(XMLDBMSProps.TABLE, props);
	 keys = getDoubleNumberedProps(XMLDBMSProps.KEY, props, tables.length);

	 engine.retrieveDocument(mapFilename, xmlFilename, tables, keys);
   }                        

   private int getCommitMode(String modeName)
   {
	 if (modeName.equals(XMLDBMSProps.AFTERINSERT))
	 {
	   return DOMToDBMS.COMMIT_AFTERINSERT;
	 }
	 else if (modeName.equals(XMLDBMSProps.AFTERDOCUMENT))
	 {
	   return DOMToDBMS.COMMIT_AFTERDOCUMENT;
	 }
	 else
	   throw new IllegalArgumentException("Invalid commit mode value: " + modeName);
   }   

   public String dispatchRetrieveDocumentByKey_s (TransferEngine engine, Properties props)
	  throws Exception
   {
	 String   mapFilename, xmlFilename, table;
	 String[] key;


	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = (String)props.getProperty(XMLDBMSProps.XMLFILE);
	 table = (String)props.getProperty(XMLDBMSProps.TABLE);
	 key = getNumberedProps(XMLDBMSProps.KEY, props);

	 String s = null;
	 s = engine.retrieveDocument_s(mapFilename, xmlFilename, table, key);
	 return s;
   }                           

   public String dispatchRetrieveDocumentByKeys_s (TransferEngine engine, Properties props)
	  throws Exception
   {
	 String     mapFilename, xmlFilename;
	 String[]   tables;
	 String[][] keys;


	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = (String)props.getProperty(XMLDBMSProps.XMLFILE);
	 tables = getNumberedProps(XMLDBMSProps.TABLE, props);
	 keys = getDoubleNumberedProps(XMLDBMSProps.KEY, props, tables.length);
	 String s = null;
	 s = engine.retrieveDocument_s(mapFilename, xmlFilename, tables, keys);
	 return s;
   }                           

   public String dispatchRetrieveDocumentBySQL_s (TransferEngine engine, Properties props) 
	  throws Exception
   {
	 String mapFilename, xmlFilename, select;


	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = (String)props.getProperty(XMLDBMSProps.XMLFILE);
	 select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);
	 String s = null;
	 s = engine.retrieveDocument_s(mapFilename, xmlFilename, select);
	 return s;
   }                           

   // ************************************************************************
   // Private methods
   // ************************************************************************

   public void dispatchStoreDocument(TransferEngine engine, Properties props,java.io.InputStream xmlFile)
	  throws Exception
   {
	 String mapFilename, keyGeneratorClass;
	 int    commitMode;
	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);

	 if (props.getProperty(XMLDBMSProps.COMMITMODE) == null)
	 { commitMode = DOMToDBMS.COMMIT_AFTERDOCUMENT;}
	 else {commitMode = getCommitMode((String)props.getProperty(XMLDBMSProps.COMMITMODE));}
	 

	 keyGeneratorClass = (String)props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);
	 engine.storeDocument(mapFilename, xmlFile, commitMode, keyGeneratorClass, props);
   }                        

   // ************************************************************************
   // Private methods
   // ************************************************************************

   public void dispatchStoreDocument_s(TransferEngine engine, Properties props, String xml)
	  throws Exception
   {
	 String mapFilename,  keyGeneratorClass;
	 int    commitMode;
	 mapFilename = (String)props.getProperty(XMLDBMSProps.MAPFILE);


	 if (props.getProperty(XMLDBMSProps.COMMITMODE) == null)
	 { commitMode = DOMToDBMS.COMMIT_AFTERDOCUMENT;}
	 else {commitMode = getCommitMode((String)props.getProperty(XMLDBMSProps.COMMITMODE));}
	 
	 keyGeneratorClass = (String)props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);



	byte[] b = xml.getBytes();
	InputStream is = new ByteArrayInputStream(b);

	 
	 engine.storeDocument(mapFilename, is, commitMode, keyGeneratorClass, props);
   }                                       
}