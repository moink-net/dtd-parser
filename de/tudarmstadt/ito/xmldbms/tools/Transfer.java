// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.tools;


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

import de.tudarmstadt.ito.domutils.ParserUtilsException;

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
	
	 String         action;

	 // Set up the Transfer Engine
	 init(props);
//	 engine.setParserProperties(props);
//	 engine.setDatabaseProperties(props);

	 // Get the action
	 action = props.getProperty(XMLDBMSProps.ACTION);
	 if (action == null){
	   throw new IllegalArgumentException("Action property not specified.");
	 }
	 // Dispatch the action
	 if (action.equals(XMLDBMSProps.STOREDOCUMENT))
	 {
	   dispatchStoreDocument(props);
	 }
	 else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYSQL))
	 {
	   dispatchRetrieveDocumentBySQL(props);
	 }
	 else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYKEY))
	 {
	   dispatchRetrieveDocumentByKey(props);
	 }
	 else if (action.equals(XMLDBMSProps.RETRIEVEDOCUMENTBYKEYS))
	 {
	   dispatchRetrieveDocumentByKeys(props);
	 }
	 else
	 {
	   throw new IllegalArgumentException("Unknown value of Action property: " + action);
	 }
	 
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

	 TransferEngine engine = new TransferEngine();

   private void dispatchRetrieveDocumentByKey(Properties props)
	  throws Exception
   {
	 String   mapFilename, xmlFilename, table;
	 String[] key;

	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	 table = props.getProperty(XMLDBMSProps.TABLE +String.valueOf(1));
	 key = getNumberedProps(XMLDBMSProps.KEY, props);

	 engine.retrieveDocument(mapFilename, xmlFilename, table, key);
   }               

                                 

   private void dispatchRetrieveDocumentByKeys(Properties props)
	  throws Exception
   {
	 String     mapFilename, xmlFilename;
	 String[]   tables;
	 String[][] keys;

	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	 tables = getNumberedProps(XMLDBMSProps.TABLE, props);
	 keys = getDoubleNumberedProps(XMLDBMSProps.KEY, props, tables.length);

	 engine.retrieveDocument(mapFilename, xmlFilename, tables, keys);
   }            

                                 

   private void dispatchRetrieveDocumentBySQL(Properties props) 
	  throws Exception
   {
	 String mapFilename, xmlFilename, select;

	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	 select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);

	 System.out.println("SELECT = " +select);

	 engine.retrieveDocument(mapFilename, xmlFilename, select);
   }               

                                 

   // ************************************************************************
   // Private methods
   // ************************************************************************

   private void dispatchStoreDocument(Properties props)
	  throws Exception
   {
	 String mapFilename, xmlFilename, keyGeneratorClass;
	 int    commitMode;
	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);

	
	 if (props.getProperty(XMLDBMSProps.COMMITMODE) == null)
	 { commitMode = DOMToDBMS.COMMIT_AFTERDOCUMENT;}
	 else {commitMode = getCommitMode(props.getProperty(XMLDBMSProps.COMMITMODE));}
	 
	 keyGeneratorClass = props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);

	 engine.storeDocument(mapFilename, xmlFilename, commitMode, keyGeneratorClass, props);
   }                                       

                              

                                             

/**
 * This is the init method which sets the Transfer Engine up for Transfer.
 * Creation date: (13/08/01 13:10:07)
 * @param props java.util.Properties
 */
public void init(Properties props) throws java.lang.Exception {
	
	engine.init(props);
	
	}

   public String RetrieveDocumentByKey (Properties props,String tableName,Object[] key)
	  throws Exception
   {
	 String   mapFilename,xmlFilename;
	// String[] key;


	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	// table = props.getProperty(XMLDBMSProps.TABLE +String.valueOf(1));
	// key = getNumberedProps(XMLDBMSProps.KEY, props);

	 String s = null;
	 s = engine.retrieveDocument(mapFilename, xmlFilename, tableName, key);
	 return s;
   }                  

   public String RetrieveDocumentByKey (Properties props,String tableName,Object[] key,String xmlFilename)
	  throws Exception
   {
	 String   mapFilename;
	// String[] key;


	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	// xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	// table = props.getProperty(XMLDBMSProps.TABLE +String.valueOf(1));
	// key = getNumberedProps(XMLDBMSProps.KEY, props);

	 String s = null;
	 s = engine.retrieveDocument(mapFilename, xmlFilename, tableName, key);
	 return s;
   }               

   public String RetrieveDocumentByKeys (Properties props,String[] tableNames,Object[][] keys)
	  throws Exception
   {
	 String   mapFilename,xmlFilename;
	// String[] key;


	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	// table = props.getProperty(XMLDBMSProps.TABLE +String.valueOf(1));
	// key = getNumberedProps(XMLDBMSProps.KEY, props);

	 String s = null;
	 s = engine.retrieveDocument(mapFilename, xmlFilename, tableNames, keys);
	 return s;
   }                     

   public String RetrieveDocumentByKeys (Properties props,String[] tableNames,Object[][] keys,String xmlFilename)
	  throws Exception
   {
	 String   mapFilename;
	// String[] key;


	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	// xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	// table = props.getProperty(XMLDBMSProps.TABLE +String.valueOf(1));
	// key = getNumberedProps(XMLDBMSProps.KEY, props);

	 String s = null;
	 s = engine.retrieveDocument(mapFilename, xmlFilename, tableNames, keys);
	 return s;
   }                  

   public String RetrieveDocumentBySQL (Properties props,String sqlString) 
	  throws Exception
   {
	 String mapFilename,xmlFilename,select;


	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);

	if(sqlString != null) {
	 select = concatNumberedProps(XMLDBMSProps.SELECT, props, true) +sqlString;
	}
	else {select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);}
	

	 String s = null;
	 s = engine.retrieveDocument(mapFilename, xmlFilename, select);
	 return s;
   }                        

   public String RetrieveDocumentBySQL (Properties props,String sqlString,String xmlFilename) 
	  throws Exception
   {
	 String mapFilename,select;


	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	// xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);
	if(sqlString != null) {
	 select = concatNumberedProps(XMLDBMSProps.SELECT, props, true) +sqlString;
	}
	else {select = concatNumberedProps(XMLDBMSProps.SELECT, props, true);}
	
	 String s = null;
	 s = engine.retrieveDocument(mapFilename, xmlFilename, select);
	 return s;
   }                  

   // ************************************************************************
   // Private methods
   // ************************************************************************

   public void StoreDocument(Properties props, String xmlFilename)
	  throws Exception
   {
	 String mapFilename,keyGeneratorClass;
	 int    commitMode;
	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);
	 //xmlFilename = props.getProperty(XMLDBMSProps.XMLFILE);

	
	 if (props.getProperty(XMLDBMSProps.COMMITMODE) == null)
	 { commitMode = DOMToDBMS.COMMIT_AFTERDOCUMENT;}
	 else {commitMode = getCommitMode(props.getProperty(XMLDBMSProps.COMMITMODE));}
	 
	 keyGeneratorClass = props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);
	
	
	engine.storeDocument(mapFilename, xmlFilename, commitMode, keyGeneratorClass, props);

	
   }      

   // ************************************************************************
   // Private methods
   // ************************************************************************

   public void StoreXMLString(Properties props, String xmlString)
	  throws Exception
   {
	 String mapFilename,  keyGeneratorClass;
	 int    commitMode;
	 mapFilename = props.getProperty(XMLDBMSProps.MAPFILE);


	 if (props.getProperty(XMLDBMSProps.COMMITMODE) == null)
	 { commitMode = DOMToDBMS.COMMIT_AFTERDOCUMENT;}
	 else {commitMode = getCommitMode(props.getProperty(XMLDBMSProps.COMMITMODE));}
	 
	 keyGeneratorClass = props.getProperty(XMLDBMSProps.KEYGENERATORCLASS);



	byte[] b = xmlString.getBytes();
	InputStream is = new ByteArrayInputStream(b);

	 
	 engine.storeDocument(mapFilename, is, commitMode, keyGeneratorClass, props);
   }         
}