// No copyright, no warranty; use as you will.
// written by Tobias Schilgen (mail@tobias-schilgen.de), University of Paderborn,
// based upon ideas of Ron Bourret and schemantix.com
// Dept. of computer science, 2001

package de.tudarmstadt.ito.xmldbms;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Vector;

/**
 *  Deletes data from DB. <P>
 *  
 *  DBMSDelete deletes data from the database according to a particular Map. The 
 *  caller must provide a Map object, and information about how to delete the 
 *  data. The latter can be one or more table names and key values or a 
 *  DocumentInfo object.</P> <P>
 *  
 *  For example, the following code deletes data for catalog entry number 42 from 
 *  the Articles table:</P> <PRE>
 *  
 *  Use a user-defined function to create a map. Map map = 
 *  createMap("article.map", conn); <BR />
 *  Create a new DBMSDelete object. DBMSDelete dbmsDel = new DBMSDelete(map)); <BR />
 *  Create a key and retrieve the data. Object[] key = { new Integer(42) }; 
 *  dbmsDel.deleteDocument("Articles", key);</PRE> <P>
 *  
 *
 *@author     Tobias Schilgen (mail@tobias-schilgen.de), Ron Bourret
 *@created    July 8, 2001
 *@version    1.0.2
 */

public class DBMSDelete
{
   // ************************************************************************
   // Private variables
   // ************************************************************************

   private Map map = null;
   private Parameters parameters;
   private Vector deleteOperations = new Vector();
   private int commitMode          = COMMIT_AFTERDELETE;

   // ************************************************************************
   // Constants
   // ************************************************************************

   /** Call commit after each DELETE statement is executed (default). */

   public static final int COMMIT_AFTERDELETE = 5;

   /** Call commit after the entire document has been deleted. */

   public static final int COMMIT_AFTERDOCUMENT = 2;

   /**
    * Don't call commit. In this case, the application must call commit,
    * such as when the data is part of a larger transaction.
    */

   public static final int COMMIT_NONE = 3;


   private static String EMPTYSTRING  = "";
   private static String DELETEFROM   = "DELETE FROM ";
   private static String WHERE        = " WHERE ";
   private static String EQUALS       = " = ";
   private static String AND          = " AND ";

   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
    *  Standard constructor. 
    */

   public DBMSDelete()
   { }

   /**
    *  Construct a new DBMSDelete object. 
    *
    *@param  map      Map object 
    */

   public DBMSDelete(Map map)
   {
      this.map = map;
   }

   /**
    *  Set the current Map. 
    *
    *@param  map  The current Map. 
    */

   public void setMap(Map map)
   {
      this.map = map;
   }


   // ************************************************************************
   // Public methods
   // ************************************************************************


   /**
    * Set the current commit mode.
    *
    * @param commitMode COMMIT_AFTERDELETE, COMMIT_AFTERDOCUMENT, or COMMIT_NONE.
    */
   public void setCommitMode(int commitMode)
   {
      if ((commitMode == COMMIT_AFTERDELETE) ||
          (commitMode == COMMIT_AFTERDOCUMENT) ||
          (commitMode == COMMIT_NONE))
      {
         this.commitMode = commitMode;
      }
      else
      {
         throw new IllegalArgumentException("Invalid commit mode value: " + commitMode);
      }
   }


   /**
    * Get the current commit mode.
    *
    * @return The current commit mode.
    */
   public int getCommitMode()
   {
      return commitMode;
   }


   /**
    *  Get the current Map. 
    *
    *@return    The current Map. 
    */

   public Map getMap() 
   {
      return map;
   }


   /** delete a document within the database
    @param tableName name of the root table of the document.
    @param key key value(s) of the document instance.
    */

   public void deleteDocument(String tableName, Object[] key)
      throws InvalidMapException, SQLException
   {
      initialize();

      // Set auto-commit, process the document, and commit the transaction
      map.setAutoCommit(commitMode == COMMIT_AFTERDELETE);

      RootTableMap rootTableMap;

      initialize();
      rootTableMap = map.getRootTableMap(tableName);
      processRootTable(rootTableMap, rootTableMap.candidateKey, key);

      if (commitMode == COMMIT_AFTERDOCUMENT)
      {
         map.commit();
      }
   }

   /** delete a document within the database
    @param docInfo DocumentInfo structure that describes the documents place in the database (see xml-dbms).
    */


   public void deleteDocument(DocumentInfo docInfo)
      throws InvalidMapException, SQLException
   {
      RootTableMap rootTableMap;
      Column[]     keyColumns;
      Object[]     key;

      // initialize the DBMSDelete object.

      initialize();

      // Set auto-commit, process the document, and commit the transaction
      map.setAutoCommit(commitMode == COMMIT_AFTERDELETE);

      // Process the entries in the DocumentInfo object.

      for (int i = 0; i < docInfo.size(); i++)
      {
         // Get the RootTableMap for the next table in the DocumentInfo, then process
         // the table.

         rootTableMap = map.getRootTableMap(docInfo.getTableName(i));

         keyColumns = rootTableMap.tableMap.table.getColumns(docInfo.getKeyColumnNames(i));
         key = docInfo.getKey(i);
         processRootTable(rootTableMap, keyColumns, key);
      }

      if (commitMode == COMMIT_AFTERDOCUMENT)
      {
         map.commit();
      }
   }

   // ************************************************************************
   // Methods to walk database hierarchy
   // ************************************************************************

   private void processRootTable(RootTableMap rootTableMap, Column[] keyColumns, Object[] key)
      throws InvalidMapException, SQLException
   {
      PreparedStatement   select;
      ResultSet           rs;
      Vector              delayedDeletes = new Vector();
      String              deleteString;

      // Build a result set over the root table and process it.

      select = map.checkOutSelectStmt(rootTableMap.tableMap.table, keyColumns, null);
      parameters.setParameters(select, key, keyColumns);
      rs = select.executeQuery();
      processClassResultSet(rootTableMap.tableMap, rs, delayedDeletes);
      rs.close();
      map.checkInSelectStmt(select);

      // Add the DELETE string to delete the root table row, then add the strings to
      // delete any delayed deletes (see processClassResultSet for details).

      deleteString = buildDeleteString(rootTableMap.tableMap.table.name, rootTableMap.candidateKey, key);
      deleteOperations.addElement(deleteString);
      transferDeleteStrings(deleteOperations, delayedDeletes);

      // Delete the rows.

      deleteRows();
   }

   private void processClassResultSet(TableMap classTableMap, ResultSet classRS, Vector delayedDeletes)
      throws InvalidMapException, SQLException
   {
      // Process a result set created over a class table.

      Row      row = new Row(classTableMap.table);
      TableMap relatedTableMap;
      String   deleteString;
      Vector   relatedDelayedDeletes = new Vector();

      while (classRS.next())
      {
         // Get the next row of data.

         fillRow(row, classRS);

         // Process the related tables.

         for (int i = 0; i < classTableMap.relatedTables.length; i++)
         {
            // Get the TableMap for the related table.

            relatedTableMap = classTableMap.relatedTables[i];

            // Clear the Vector of DELETE statements for grandchild tables that
            // need to be deleted after the child table.

            relatedDelayedDeletes.removeAllElements();
 
            // If the related table is not a leaf table, then process it recursively.
            // We do not need to process leaf tables -- all we need to do for leaf
            // tables is build a DELETE string over them.

            if (!isLeafTable(relatedTableMap));
            {
               processRelatedTable(classTableMap, i, row, relatedDelayedDeletes);
            }

            // Build a string to delete the rows in the related table and add it
            // as appropriate.

            deleteString = buildDeleteString(relatedTableMap.table.name, classTableMap.childKeys[i], row.getColumnValues(classTableMap.parentKeys[i]));
            if (classTableMap.parentKeyIsCandidate[i])
            {
               // If the related table contains the foreign key, then we delete its
               // rows before the rows in the class table. Therefore, we add a DELETE
               // statement for the related table to the global list of deletes. We also
               // transfer any DELETE statements that need to be executed after the
               // related table rows are deleted.

               deleteOperations.addElement(deleteString);
               transferDeleteStrings(deleteOperations, relatedDelayedDeletes);
            }

            else

            {
               // If the related table contains the primary key, then we delete its
               // rows after the rows in the class table. Therefore, we add a DELETE
               // statement for the related table to the list of delayed DELETE statements
               // that will be processed by the calling instance of processClassResultSet.
               // We also transfer any DELETE statements that need to be executed after
               // the related table rows are deleted.

               delayedDeletes.addElement(deleteString);
               transferDeleteStrings(delayedDeletes, relatedDelayedDeletes);
            }
         }
      }
   }

   private void processRelatedTable(TableMap parentTableMap, int relatedTableNumber, Row parentRow, Vector delayedDeletes)
      throws InvalidMapException, SQLException
   {
      PreparedStatement select;
      ResultSet         rs;

      // Build a result set over the related table.

      select = map.checkOutSelectStmt(parentTableMap.table.number, relatedTableNumber);
      parameters.setParameters(select, parentRow, parentTableMap.parentKeys[relatedTableNumber]);
      rs = select.executeQuery();

      // Process the result set. Note that the related table is always a class table,
      // since property tables are leaf tables and are not passed to this method.

      processClassResultSet(parentTableMap.relatedTables[relatedTableNumber], rs, delayedDeletes);

      // Close the result set.

      rs.close();
      map.checkInSelectStmt(select, parentTableMap.table.number, relatedTableNumber);
   }



   // ************************************************************************
   // helper methods
   // ************************************************************************


   void initialize()
   {
      if (map == null) throw new IllegalStateException("Map not set.");
      parameters = new Parameters(map.dateFormatter, map.timeFormatter, map.timestampFormatter);
      deleteOperations.removeAllElements();
   }

   private boolean isLeafTable(TableMap tableMap)
   {
      if (tableMap.type == TableMap.TYPE_PROPERTYTABLE) return true;
      if (tableMap.relatedTables.length == 0) return true;
      return false;
   }

   private String buildDeleteString(String tableName, Column[] keyColumns, Object[] key)
   {
      StringBuffer deleteString = new StringBuffer();

      deleteString.append(DELETEFROM);
      deleteString.append(tableName);
      deleteString.append(WHERE);
      deleteString.append(keyColumns[0].name);
      deleteString.append(EQUALS);
      deleteString.append(key[0].toString());

      for (int i = 1; i < keyColumns.length; i++)
      {
         deleteString.append(AND);
         deleteString.append(keyColumns[i].name);
         deleteString.append(EQUALS);
         deleteString.append(key[i].toString());
      }

      return deleteString.toString();
   }

   private void deleteRows()
      throws SQLException
   {
      Statement stmt = map.conn.createStatement();
      for(int i=0; i < deleteOperations.size(); i++)
         stmt.executeUpdate((String)deleteOperations.elementAt(i));
   }

   private void transferDeleteStrings(Vector target, Vector source)
   {
      for (int i = 0; i < source.size(); i++)
      {
         target.addElement(source.elementAt(i));
      }
   }

   private void fillRow(Row row, ResultSet rs)
      throws SQLException
   {
      for (int i = 1; i <= row.columnValues.length; i++)
      {
         row.columnValues[i - 1] = rs.getObject(i);

         if (rs.wasNull())
         {
            if (map.emptyStringIsNull)
               row.columnValues[i - 1] = EMPTYSTRING;
            else
               row.columnValues[i - 1] = null;
         }
      }
   }
}

