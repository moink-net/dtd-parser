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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.xmldbms.filters;

import org.xmlmiddleware.xmldbms.maps.ClassTableMap;
import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.Table;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Base class for RootFilter and ResultSetFilter; contains TableFilters.
 * <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class FilterBase
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   protected Map       map;
   private   Hashtable tableFilters = new Hashtable();

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new FilterBase object.
    *
    * @param map The Map to which the filter applies.
    */
   protected FilterBase(Map map)
   {
      this.map = map;
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //*********************************************************************
   // Table filters
   //*********************************************************************

   /**
    * Get a table filter.
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The TableFilter. Null if no filter exists for the table.
    */
   public final TableFilter getTableFilter(String databaseName, String catalogName, String schemaName, String tableName)
   {
      return (TableFilter)tableFilters.get(Table.getHashName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Get a table filter.
    *
    * @param table The Table
    *
    * @return The TableFilter. Null if no filter exists for the table.
    */
   public final TableFilter getTableFilter(Table table)
   {
      return (TableFilter)tableFilters.get(table.getHashName());
   }

   /**
    * Gets an Enumeration of all the table filters.
    *
    * @return The Enumeration. May be empty.
    */
   public final Enumeration getTableFilters()
   {
      return tableFilters.elements();
   }

   /**
    * Create a filter for a class table.
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The TableFilter for the table.
    * @exception IllegalArgumentException Thrown if a filter already exists for the
    *    table or the table is not mapped as a class table.
    */
   public TableFilter createTableFilter(String databaseName, String catalogName, String schemaName, String tableName)
   {
      TableFilter   tableFilter;
      ClassTableMap classTableMap;
      String      name;

      classTableMap = map.getClassTableMap(databaseName, catalogName, schemaName, tableName);
      if (classTableMap == null)
         throw new IllegalArgumentException("Table not mapped as a class table: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));

      name = Table.getHashName(databaseName, catalogName, schemaName, tableName);
      if (tableFilters.get(name) != null)
         throw new IllegalArgumentException("Filter already exists for table: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));

      tableFilter = new TableFilter(classTableMap);
      tableFilters.put(name, tableFilter);
      return tableFilter;
   }

   /**
    * Remove the filter for a table.
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @exception IllegalArgumentException Thrown if no filter exists for the table.
    */
   public void removeTableFilter(String databaseName, String catalogName, String schemaName, String tableName)
      throws IllegalArgumentException
   {
      Object o;

      o = tableFilters.remove(Table.getHashName(databaseName, catalogName, schemaName, tableName));
      if (o == null)
         throw new IllegalArgumentException("Filter does not exist for table: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Remove the filters for all tables.
    */
   public void removeAllTableFilters()
   {
      tableFilters.clear();
   }
}