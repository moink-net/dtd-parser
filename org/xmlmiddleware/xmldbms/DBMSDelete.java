// This software is in the public domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.1: New in version 2.0

package org.xmlmiddleware.xmldbms;

import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.actions.*;
import org.xmlmiddleware.xmldbms.filters.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import java.util.*;

/**
 * Deletes data from the database.
 *
 * <p>DBMSDelete deletes data from the database according to particular Map,
 * FilterSet, and Actions objects. The Map and FilterSet objects define the
 * hierarchy of rows that DBMSDelete processes. The Actions object defines
 * whether DBMSDelete deletes or ignores these rows. (It may be helpful to
 * think of DBMSDelete as being almost identical to DBMSToDOM, except that
 * DBMSDelete deletes rows instead of retrieving them.)</p>
 *
 * <p>For example, the following code deletes sales order number 123 and
 * related records.</p>
 *
 * <pre>
 *    // Create the Map object with a user-defined function.
 *    map = createMap("orders.map");
 *    <br />
 *    // Create an Actions object with a user-defined function.
 *    actions = createActions(map, "deleteorders.ftr");
 *    <br />
 *    // Create the FilterSet object with a user-defined function.
 *    filterSet = createFilterSet(map, "ordersbynumber.ftr");
 *    <br />
 *    // Create a new DBMSDelete object that uses the Xerces parser.
 *    dbmsDelete = new DBMSDelete(new ParserUtilsXerces());
 *    <br />
 *    // Create a data source and data handler for our database, then
 *    // bundle these into a TransferInfo object.
 *    ds = new JDBC1DataSource("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:xmldbms");
 *    handler = new GenericHandler(ds, null, null);
 *    ti = new TransferInfo(map, null, handler);
 *    <br />
 *    // Build the parameters hashtable.
 *    params = new Hashtable();
 *    params.put("$Number", "123");
 *    <br />
 *    // Call deleteDocument to delete the data.
 *    dbmsDelete.deleteDocument(ti, filterSet, params, actions);
 * </pre>
 *
 * @author Tobias Schilgen (mail@tobias-schilgen.de)
 * @author Ronald Bourret
 * @author Jiri Zoth
 * @version 2.0
 */

public class DBMSDelete
{
   private static boolean debug  = false;
//   private static boolean debug  = true;

   // ************************************************************************
   // Private variables
   // ************************************************************************

   private int          commitMode = DataHandler.COMMIT_AFTERSTATEMENT;
   private Map          map = null;
   private TransferInfo transferInfo = null;
   private Actions      actions;
   private FilterBase   filterBase;

   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
    * Construct a new DBMSDelete object. 
    */

   public DBMSDelete()
   { }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Set the current commit mode.
    *
    * <p>If the commit mode is not set, COMMIT_AFTERSTATEMENT is used by default.</p>
    *
    * @param commitMode COMMIT_AFTERSTATEMENT, COMMIT_AFTERDOCUMENT, COMMIT_NONE,
    *    or COMMIT_NOTRANSACTIONS. These are defined in the DataHandler interface.
    */
   public void setCommitMode(int commitMode)
   {
      if ((commitMode != DataHandler.COMMIT_AFTERSTATEMENT) &&
          (commitMode != DataHandler.COMMIT_AFTERDOCUMENT) &&
          (commitMode != DataHandler.COMMIT_NONE) &&
          (commitMode != DataHandler.COMMIT_NOTRANSACTIONS))
         throw new IllegalArgumentException("Invalid commit mode value: " + commitMode);
      this.commitMode = commitMode;
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
    * Delete a document based on the specified map, filter, and action.
    *
    * <p>The filter set must contain at least one root filter.</p>
    *
    * @param transferInfo Map and connection information.
    * @param filterSet The filter set specifying the data to retrieve.
    * @param params A Hashtable containing the names (keys) and values (elements) of
    *    any parameters used in the filters. Null if there are no parameters.
    * @param action Action to be taken for each node of the document. This must be
    *    Action.DELETE or Action.SOFTDELETE.
    */
   public void deleteDocument(TransferInfo transferInfo, FilterSet filterSet, Hashtable params, int action)
      throws SQLException
   {
      Actions defaultActions;
      Action  defaultAction;

      if ((action != Action.DELETE) && (action != Action.SOFTDELETE))
         throw new IllegalArgumentException("action argument must be Action.DELETE or Action.SOFTDELETE.");

      // Build a new Actions object with a single default action.

      defaultActions = new Actions(transferInfo.getMap());
      defaultAction = new Action();
      defaultAction.setAction(action);
      defaultActions.setDefaultAction(defaultAction);

      // Call the other version of deleteDocument.

      deleteDocument(transferInfo, filterSet, params, defaultActions);
   }

   /**
    * Delete a document based on the specified map, filter, and action.
    *
    * <p>The filter set must contain at least one root filter.</p>
    *
    * @param transferInfo Map and connection information.
    * @param filterSet The filter set specifying the data to retrieve.
    * @param params A Hashtable containing the names (keys) and values (elements) of
    *    any parameters used in the filters. Null if there are no parameters.
    * @param actions An Actions object describing how to handle various elements of
    *    a document. The actual actions must be Action.NONE, Action.DELETE, or
    *    Action.SOFTDELETE.
    */
   public void deleteDocument(TransferInfo transferInfo, FilterSet filterSet, Hashtable params, Actions actions)
      throws SQLException
   {
      Enumeration dataHandlers;

      // Initialize the globals

      this.transferInfo = transferInfo;
      map = transferInfo.getMap();
      this.actions = actions;

      // Set the filter parameters. We do this here because the filters are optimized
      // for the parameters only being set once.

      filterSet.setFilterParameters(params);

      // Call startDocument here. This allows data handlers to do any
      // necessary initialization.

      dataHandlers = transferInfo.getDataHandlers(); 
      while (dataHandlers.hasMoreElements())
      {
         ((DataHandler)dataHandlers.nextElement()).startDocument(commitMode);
      }

      // Delete the data.

      processRootTables(filterSet);

      // Call endDocument here. This allows data handlers to do any
      // necessary finalization, such as committing transactions.

      dataHandlers = transferInfo.getDataHandlers(); 
      while (dataHandlers.hasMoreElements())
      {
         ((DataHandler)dataHandlers.nextElement()).endDocument();
      }
   }

   // ************************************************************************
   // Methods to walk database hierarchy
   // ************************************************************************

   // The processing flow is as follows. Property tables and some related
   // class tables are leaf tables; processing stops when these are encountered.
   // Rows are deleted from within methods that process edges (links) in the
   // graph of tables. These are processRootTable, processRelatedTables, and
   // processPropertyTables.
   //
   // If the parent table contains the primary key used to link the parent and
   // child tables, then the method deletes the rows in the child table after
   // processing that table. If the child table contains the primary key, then
   // the method passes information about the rows back to the method processing
   // the parent table. This method deletes the rows after deleting the rows
   // in the parent table. This is horribly recursive and hard to follow, but
   // necessary to maintain referential integrity in the case where each statement
   // is committed after it is executed.
   //
   //          processRootTables
   //                1. |
   //                   v
   //          processRootTable
   //                2. |
   //                   v
   //    |---->processClassResultSet
   //    |           3. |
   //    |              v              7.
   //    |     processRelatedTables--------->processPropertyTables
   // 6. |           4. |
   //    |              v
   //    |     processRelatedClassTables
   //    |           5. |
   //    |              v
   //    |-----processRelatedClassTable

   private void processRootTables(FilterSet filterSet)
      throws SQLException
   {
      Vector filters;
      Object filter;

      // Get the root filters and process them

      filters = filterSet.getFilters();
      for (int i = 0; i < filters.size(); i++)
      {
         // Get the next filter and set the filterBase global. filterBase
         // parallels map in that it contains filters for all class tables
         // to be processed.

         filter = filters.elementAt(i);
         filterBase = (FilterBase)filter;

         // If the filter is a RootFilter, then delete its data. If it is
         // a ResultSetFilter, ignore it.

         if (filter instanceof RootFilter)
         {
            processRootTable((RootFilter)filter);
         }
      }
   }

   private void processRootTable(RootFilter rootFilter)
      throws SQLException
   {
      FilterConditions rootConditions;
      Table            rootTable;
      ClassTableMap    rootTableMap;
      ResultSet        rs;
      RowInfo          rowInfo;
      Vector           pkChildren = new Vector();
      int              action;

      // Get the filter over the root table, the root table, and the
      // ClassTableMap for the root table. Also get the action.

      rootConditions = rootFilter.getRootFilterConditions();
      rootTable = rootConditions.getTable();
      rootTableMap = map.getClassTableMap(rootTable);
      action = getActionFor(rootTableMap.getElementTypeName());

      // Get the result set over the root table and process it.

      rowInfo = new RowInfo(action, rootTable, null, null, rootConditions);
      rs = getResultSet(rowInfo);
      processClassResultSet(rootTableMap, rs, pkChildren);
      rs.close();

      // If the action is DELETE or SOFTDELETE, then delete the row(s) in
      // the root table, followed by any rows from descendant (child, etc.)
      // tables that have primary keys pointing to rows in the root table.

      if ((action == Action.DELETE) || (action == Action.SOFTDELETE))
      {
         deleteRows(rowInfo);
         deleteRows(pkChildren);
      }
   }

   private void processClassResultSet(ClassTableMap classTableMap, ResultSet rs, Vector parentPKChildren)
      throws SQLException
   {
      // Process a result set created over a class table.

      Row         classRow;
      TableFilter classTableFilter;

      // Create a new row.

      classRow = new Row();

      // Process the result set.

      while (rs.next())
      {
         // Cache the row data so we can access it randomly

         classRow.clear();
         classRow.setColumnValues(rs, classTableMap.getTable(), map.emptyStringIsNull());

         // Process the related tables for the row.

         classTableFilter = filterBase.getTableFilter(classTableMap.getTable());
         processRelatedTables(classRow, classTableMap, classTableFilter, parentPKChildren);
      }
   }

   private void processRelatedTables(Row classRow, ClassTableMap classTableMap, TableFilter classTableFilter, Vector parentPKChildren)
      throws SQLException
   {
      processRelatedClassTables(classRow, classTableMap, classTableFilter, parentPKChildren);
      processPropertyTables(classRow, classTableMap, classTableFilter, parentPKChildren);
   }

   private void processRelatedClassTables(Row classRow, ClassTableMap classTableMap, TableFilter classTableFilter, Vector parentPKChildren)
      throws SQLException
   {
      Enumeration          relatedClassTableMaps;
      RelatedClassTableMap relatedClassTableMap;
      Table                relatedClassTable;
      Vector               pkChildren = new Vector();
      RelatedTableFilter   relatedTableFilter;
      int                  action;
      LinkInfo             linkInfo;
      RowInfo              rowInfo;

      // Process the related class tables.

      relatedClassTableMaps = classTableMap.getRelatedClassTableMaps();
      while (relatedClassTableMaps.hasMoreElements())
      {
         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.nextElement();
         relatedTableFilter = (classTableFilter == null) ?
                               null :
                               classTableFilter.getRelatedTableFilter(relatedClassTableMap);

         // Clear the Vector of all information about rows in descendant
         // (grandchild, etc.) tables that need to be deleted after the
         // related (child) table.

         pkChildren.removeAllElements();
 
         // Get the action for the related table. If this is DELETE or SOFTDELETE
         // then we will delete the rows. If it is NONE, then we don't delete the rows.
         // Furthermore, we cannot delete any descendant rows that have primary keys
         // directly or indirectly pointing to rows in the related table.
         //
         // For example, indirect pointing would occur if a grandchild of the
         // related table had a primary key that pointed to a row in a child of the
         // related table, and this in turn had a primary key that pointed to a row
         // in the related table. We can't delete the grandchild until we delete
         // the child. We can't delete the child until we delete the row in the
         // related table. Since we aren't deleting the row in the related table
         // (the action is NONE), we can't delete the child or grandchild row.
         //
         // Note that this doesn't stop us from processing the related table. This
         // is because descendants of the related table might be pointed to by
         // primary keys in parent tables, in which case it is safe to delete them.
         // An easy example is deleting all the line items in a sales order, but
         // not the sales order itself.

         action = getActionFor(relatedClassTableMap.getElementTypeName());

         // If the related table is not a leaf table, then process it recursively.
         // We do not need to process leaf tables -- all we need to do for leaf
         // tables is save the information to delete them

         if (!isLeafTable(relatedClassTableMap));
         {
            processRelatedClassTable(classRow, relatedClassTableMap, relatedTableFilter, pkChildren);
         }

         // Delete the rows or store information so they can be deleted later.

         if ((action == Action.DELETE) || (action == Action.SOFTDELETE))
         {
            linkInfo = relatedClassTableMap.getLinkInfo();
            relatedClassTable = relatedClassTableMap.getClassTableMap().getTable();
            rowInfo = new RowInfo(action, relatedClassTable, linkInfo, classRow, relatedTableFilter);
            if (linkInfo.parentKeyIsUnique())
            {
               // If the related (child) table contains the foreign key, we delete its
               // rows before deleting the rows in the class (parent) table. That means
               // we delete them now. After deleting these rows, delete any rows from
               // descendant (grandchild, etc.) tables that have primary keys pointing to
               // rows in the related (child) table.

                  deleteRows(rowInfo);
                  deleteRows(pkChildren);
            }
            else
            {
               // If the related (child) table contains the primary key, we delete its
               // rows after deleting the rows in the class (parent) table. That means
               // we cannot delete them now. Instead, we add information about how to
               // delete these rows to the parentPKChildren vector. They will be deleted
               // later when this recursive nightmare finally deletes the rows in the
               // class (parent) table. We also add information about how to delete
               // rows from descendant (grandchild, etc.) tables that have primary keys
               // pointing to rows in the related (child) table.

               parentPKChildren.addElement(rowInfo);
               copyElements(parentPKChildren, pkChildren);
            }
         }
      }
   }

   private void processRelatedClassTable(Row classRow, RelatedClassTableMap relatedClassTableMap, RelatedTableFilter relatedTableFilter, Vector pkChildren)
      throws SQLException
   {
      ClassTableMap childClassTableMap;
      Table         childTable;
      LinkInfo      linkInfo;
      RowInfo       rowInfo;
      ResultSet     rs;

      // Get the key values

      childClassTableMap = relatedClassTableMap.getClassTableMap();
      childTable = childClassTableMap.getTable();

      // Get the result set over the related class table and process it.

      linkInfo = relatedClassTableMap.getLinkInfo();
      rowInfo = new RowInfo(Action.NONE, childTable, linkInfo, classRow, relatedTableFilter);
      rs = getResultSet(rowInfo);
      processClassResultSet(childClassTableMap, rs, pkChildren);
      rs.close();
   }

   private void processPropertyTables(Row classRow, ClassTableMap classTableMap, TableFilter classTableFilter, Vector parentPKChildren)
      throws SQLException
   {
      Enumeration        propTableMaps;
      PropertyTableMap   propTableMap;
      RelatedTableFilter relatedTableFilter;
      int                action;
      LinkInfo           linkInfo;
      RowInfo            rowInfo;

      // Process the property tables.

      propTableMaps = classTableMap.getPropertyTableMaps();
      while (propTableMaps.hasMoreElements())
      {
         propTableMap = (PropertyTableMap)propTableMaps.nextElement();
         relatedTableFilter = (classTableFilter == null) ?
                               null :
                               classTableFilter.getRelatedTableFilter(propTableMap);

         // Property tables are always leaf tables, so we don't need to process
         // them recursively. Instead, just delete rows or add information about
         // how to delete rows so they can be deleted later.

         // Get the action for the property table. Note that this is the action
         // for the parent class. This is because we assign actions on a per-class
         // (rather than per-property) basis. If the action is DELETE or SOFTDELETE,
         // then delete the rows. If it is NONE, then don't delete the rows.

         action = getActionFor(classTableMap.getElementTypeName());
         if ((action == Action.DELETE) || (action == Action.SOFTDELETE))
         {
            linkInfo = propTableMap.getLinkInfo();
            rowInfo = new RowInfo(action, propTableMap.getTable(), linkInfo, classRow, relatedTableFilter);
            if (linkInfo.parentKeyIsUnique())
            {
               // If the property (child) table contains the foreign key, we delete its
               // rows before deleting the rows in the class (parent) table. That means
               // we delete them now.

               deleteRows(rowInfo);
            }
            else
            {
               // If the property (child) table contains the primary key, we delete its
               // rows after deleting the rows in the class (parent) table. That means
               // we cannot delete them now. Instead, we add information about how to
               // delete these rows to the parentPKChildren vector. They will be deleted
               // later when this recursive nightmare finally deletes the rows in the
               // class (parent) table.

               parentPKChildren.addElement(rowInfo);
            }
         }
      }
   }

   // ************************************************************************
   // Helper methods
   // ************************************************************************

   private int getActionFor(XMLName elementTypeName)
   {
      // Gets the action for a given element

      Action action;
      int    actionValue;

      action = actions.getAction(elementTypeName);

      if (action == null)
         action = actions.getDefaultAction();

      if (action == null)
         throw new IllegalArgumentException("No default action specified.");

      actionValue = action.getAction();
 
      if ((actionValue != Action.DELETE) &&
          (actionValue != Action.SOFTDELETE) &&
          (actionValue != Action.NONE))
         throw new IllegalArgumentException("INSERT, SOFTINSERT, UPDATEORINSERT, and UPDATE actions cannot be used with DBMSDelete.");

      return actionValue;
   }

   private boolean isLeafTable(RelatedClassTableMap relatedClassTableMap)
   {
      ClassTableMap classTableMap = relatedClassTableMap.getClassTableMap();

      // A RelatedClassTableMap points to a leaf table if the ClassTableMap
      // for that table has no child related class tables or property tables.

      if (classTableMap.getRelatedClassTableMaps().hasMoreElements()) return false;
      if (classTableMap.getPropertyTableMaps().hasMoreElements()) return false;
      return true;
   }

   private void copyElements(Vector target, Vector source)
   {
      for (int i = 0; i < source.size(); i++)
      {
         target.addElement(source.elementAt(i));
      }
   }

   // ************************************************************************
   // Delete rows
   // ************************************************************************

   private void deleteRows(RowInfo r)
      throws SQLException
   {
      DataHandler dataHandler;

      // Get the DataHandler used by the table and delete the row

      dataHandler = transferInfo.getDataHandler(r.table.getDatabaseName());
      try
      {
         dataHandler.delete(r.table, r.key, r.keyValue, r.where, r.columns, r.params);
      }
      catch (SQLException s)
      {
         if (r.action == Action.SOFTDELETE)
         {
            // TODO: Add this to the list of warnings.
         }
         else
            throw s;
      }
   }

   private void deleteRows(Vector rowInfos)
      throws SQLException
   {
      RowInfo r;

      for (int i = 0; i < rowInfos.size(); i++)
      {
         r = (RowInfo)rowInfos.elementAt(i);
         deleteRows(r);
      }
   }

   private ResultSet getResultSet(RowInfo r)
      throws SQLException
   {
      DataHandler dataHandler;

      // Get the DataHandler used by the table and get the result set

      dataHandler = transferInfo.getDataHandler(r.table.getDatabaseName());
      return dataHandler.select(r.table, r.key, r.keyValue, r.where, r.columns, r.params, null);
   }

   class RowInfo
   {
      int      action;
      Table    table;
      Key      key = null;
      Object[] keyValue = null;
      String   where = null;
      Column[] columns = null;
      Object[] params = null;

      RowInfo(int action, Table table, LinkInfo linkInfo, Row row, FilterConditions filterConditions)
      {
         this.action = action;
         this.table = table;
         if (linkInfo != null)
         {
            key = linkInfo.getChildKey();
            keyValue = row.getColumnValues(linkInfo.getParentKey().getColumns());
         }
         if (filterConditions != null)
         {
            where = filterConditions.getWhereCondition();
            columns = filterConditions.getColumns();
            params = filterConditions.getParameterValues();
         }
      }
   }
/*
*******************************************************
?   private static String EMPTYSTRING  = "";
?   private static String DELETEFROM   = "DELETE FROM ";
?   private static String WHERE        = " WHERE ";
?   private static String EQUALS       = " = ";
?   private static String AND          = " AND ";



private String buildDeleteString(String tableName, Column[]
keyColumns, Object[] key)
{
StringBuffer deleteString = new StringBuffer();
if (debug) System.out.println("DBMSDelete.table name: " + tableName);

deleteString.append(DELETEFROM);
deleteString.append(tableName);
deleteString.append(WHERE);
deleteString.append(keyColumns[0].name);
//PM: change to ISNULL due to bug "= nullpointer"
if (key[0] != null) { 
	deleteString.append(EQUALS);
	deleteString.append(key[0].toString());}
else { 	deleteString.append(" IS NULL ");}
for (int i = 1; i < keyColumns.length; i++)
{
deleteString.append(AND);
deleteString.append(keyColumns[i].name);
if (key[i] != null) { 
	deleteString.append(EQUALS);
	deleteString.append(key[i].toString());}
else { 	deleteString.append(" IS NULL ");}
}
if (debug) System.out.println("DBMSDelete.deleteString: " + deleteString.toString());
//PM

return deleteString.toString();
}

/* Original
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
*/
}

