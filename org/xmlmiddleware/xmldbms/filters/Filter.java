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

/**
 * A filter to retrieve a document fragment based on a condition on a root
 * table. <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class Filter extends FilterBase
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private FilterConditions rootFilter;

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new Filter object.
    *
    * @param map The Map to which the filter applies.
    */
   protected Filter(Map map)
   {
      super(map);
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //*********************************************************************
   // Root filter
   //*********************************************************************

   /**
    * Get the root filter.
    *
    * @return The root filter
    */
   public final FilterConditions getRootFilter()
   {
      return rootFilter;
   }

   /**
    * Create the root filter.
    *
    * <p>If a root filter already exists, it is overwritten.</p>
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The root filter.
    * @exception IllegalArgumentException Thrown if the table is not mapped as a class table.
    */
   public FilterConditions createRootFilter(String databaseName, String catalogName, String schemaName, String tableName)
   {
      ClassTableMap    classTableMap;
      FilterConditions rootFilter;

      classTableMap = map.getClassTableMap(databaseName, catalogName, schemaName, tableName);
      if (classTableMap == null)
         throw new IllegalArgumentException("Table not mapped as a class table: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));

      rootFilter = new FilterConditions(classTableMap.getTable());
      this.rootFilter = rootFilter;
      return rootFilter;
   }
}