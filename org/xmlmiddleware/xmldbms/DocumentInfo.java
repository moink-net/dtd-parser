// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 2.0
// Changes from version 1.0: None
// Changes from version 1.01: None
// Changes from version 1.1: None

package org.xmlmiddleware.xmldbms;

import java.util.Vector;
import org.xmlmiddleware.xmldbms.maps.*;

/**
 * Contains information necessary to retrieve a document.
 *
 * <P>The DocumentInfo class contains information necessary to
 * retrieve a single XML document from the database: the names of the
 * root tables in which the document was stored, the names of the
 * key columns in these tables, the applicable key values, and the
 * names of the order columns in these tables.</P>
 *
 * <p>There will be more than one set of values only if the root element
 * type is ignored, in which case there is one set of values for each
 * child. These can point to the same table (the child element types
 * are all the same) or multiple tables (the child element types are
 * different).</P>
 *
 * <P>DocumentInfo objects are returned by DOMToDBMS.storeDocument
 * and may be passed to DBMSToDOM.retrieveDocument(DocumentInfo).</P>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 * @see DOMToDBMS#storeDocument(Document)
 * @see DBMSToDOM#retrieveDocument(DocumentInfo)
 */

public class DocumentInfo
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private Vector tables = new Vector();        // Contains TableInfo's.
   private Vector keyColumns = new Vector();    // Contains String[]'s.
   private Vector keys = new Vector();          // Contains Object[]'s.
/*   private Vector orderColumns = new Vector(); */ // Contains Strings.

   // ********************************************************************
   // Constructors
   // ********************************************************************

   /**
	* Construct a DocumentInfo object.
	*/
   public DocumentInfo()
   {
   }   

   /**
	* Construct a DocumentInfo object and initialize from Table and Column
	* objects.
	*
	* @param table Table object for the table containing the data.
	* @param keyColumns Column objects for the key columns in the table.
	* @param key Key used to retrieve the data.
	* @param orderColumn Column object for the column stating the order in
	*  which the data values are to be retrieved. Null if there is no order
	*  column.
	*/
   public DocumentInfo(Table table, Column[] keyColumns, Object[] key/*, Column orderColumn*/)
   {
	  addInfo(table, keyColumns, key/*, orderColumn*/);
   }   

   /**
	* Construct a DocumentInfo object and initialize it from table and column
	* names.
	*
	* @param tableName Name of the table containing the data.
	* @param keyColumnNames Names of the key columns in the table.
	* @param key Key used to retrieve the data.
	* @param orderColumnName Name of the column stating the order in
	*  which the data values are to be retrieved. Null if there is no order
	*  column.
	*/
   public DocumentInfo(String dbName, String catName, String schName, String tableName, 
                       String[] keyColumnNames, Object[] key/*, String orderColumnName*/)
   {
	  addInfo(dbName, catName, schName, tableName, keyColumnNames, key/*, orderColumnName*/);
   }   

   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
	* Get the number of keys.
	*/
   public int size()
   {
	  return keys.size();
   }  
   
   /**
	* Get the database of the ith table.
	*
	* @param num Table number.
	* @return Name of the database.
	*/
   public String getDatabaseName(int num)
   {
	  return ((TableInfo)tables.elementAt(num)).database;
   }   

   /**
	* Get the catalog of the ith table.
	*
	* @param num Table number.
	* @return Name of the catalog.
	*/
   public String getCatalogName(int num)
   {
	  return ((TableInfo)tables.elementAt(num)).catalog;
   }   

   /**
	* Get the schema of the ith table.
	*
	* @param num Table number.
	* @return Name of the schema.
	*/
   public String getSchemaName(int num)
   {
	  return ((TableInfo)tables.elementAt(num)).schema;
   }   

   /**
	* Get the name of the ith table.
	*
	* @param num Table number.
	* @return Name of the table.
	*/
   public String getTableName(int num)
   {
	  return ((TableInfo)tables.elementAt(num)).table;
   }   

   /**
	* Get the names of the key columns for the ith table.
	*
	* @param num Table number.
	* @return Names of the key columns.
	*/
   public String[] getKeyColumnNames(int num)
   {
	  return (String[])keyColumns.elementAt(num);
   }   

   /**
	* Get the ith key.
	*
	* @param num Key number.
	* @return Key value.
	*/
   public Object[] getKey(int num)
   {
	  return (Object[])keys.elementAt(num);
   }   

   /**
	* Get the name of the order column for the ith table.
	*
	* @param num Table number.
	* @return Name of the order column. Null if there is no order column.
	*/
/*   public String getOrderColumnName(int num)
   {
	  return (String)orderColumns.elementAt(num);
   }   */

   /**
	* Add a set of values to the DocumentInfo object using Table and
	* Column objects.
	*
	* @param table Table object for the table containing the data.
	* @param keyColumns Column objects for the key columns in the table.
	* @param key Key used to retrieve the data.
	* @param orderColumn Column object for the column stating the order in
	*  which the data values are to be retrieved. Null if there is no order
	*  column.
	*/
   public void addInfo(Table table, Column[] keyColumns, Object[] key/*, Column orderColumn*/)
   {
	  String[] keyColumnNames = null;
/*	  String   orderColumnName;*/

	  if (keyColumns != null)
	  {
		 keyColumnNames = new String[keyColumns.length];
		 for (int i = 0; i < keyColumns.length; i++)
		 {
			keyColumnNames[i] = keyColumns[i].getName();
		 }
	  }
/*	  orderColumnName = (orderColumn == null) ? null : orderColumn.getName();*/

	  addInfo(table.getDatabaseName(), table.getCatalogName(), table.getSchemaName(),
              table.getTableName(), keyColumnNames, key/*, orderColumnName*/);
   }   

   /**
	* Add a set of values to the DocumentInfo object using table and column
	* names.
	*
	* @param tableName Name of the table containing the data.
	* @param keyColumnNames Names of the key columns in the table.
	* @param key Key used to retrieve the data.
	* @param orderColumnName Name of the column stating the order in
	*  which the data values are to be retrieved. Null if there is no order
	*  column.
	*/
   public void addInfo(String dbName, String catName, String schName, String tableName, 
                       String[] keyColumnNames, Object[] key/*, String orderColumnName*/)
   {
	  tables.addElement(new TableInfo(dbName, catName, schName, tableName));
	  keyColumns.addElement(keyColumnNames);
	  keys.addElement(key);
/*	  orderColumns.addElement(orderColumnName);*/
   }   


    class TableInfo
    {
        String database;
        String catalog;
        String schema;
        String table;

        TableInfo(String db, String cat, String sch, String tbl)
        {
            database = db;
            catalog = cat;
            schema = sch;
            table = tbl;
        }

    };
}