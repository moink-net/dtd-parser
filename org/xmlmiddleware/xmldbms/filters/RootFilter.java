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

public class RootFilter extends FilterBase
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private FilterConditions rootFilterConditions;

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new RootFilter object.
    *
    * @param map The Map to which the filter applies.
    */
   protected RootFilter(Map map)
   {
      super(map);
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //*********************************************************************
   // Root filter conditions
   //*********************************************************************

   /**
    * Get the root filter conditions.
    *
    * @return The root filter conditions
    */
   public final FilterConditions getRootFilterConditions()
   {
      return rootFilterConditions;
   }

   /**
    * Create the root filter conditions.
    *
    * <p>If a root filter conditions already exist, they are overwritten.</p>
    *
    * @param databaseName Name of the root database. If this is null, "Default" is used.
    * @param catalogName Name of the root catalog. May be null.
    * @param schemaName Name of the root schema. May be null.
    * @param tableName Name of the root table.
    *
    * @return The root filter conditions.
    * @exception IllegalArgumentException Thrown if the table is not mapped as a class table.
    */
   public FilterConditions createRootFilterConditions(String databaseName, String catalogName, String schemaName, String tableName)
   {
      ClassTableMap    classTableMap;

      classTableMap = map.getClassTableMap(databaseName, catalogName, schemaName, tableName);
      if (classTableMap == null)
         throw new IllegalArgumentException("Table not mapped as a class table: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));

      rootFilterConditions = new FilterConditions(classTableMap.getTable());
      return rootFilterConditions;
   }

   /**
    * Create the root filter conditions.
    *
    * <p>If a root filter conditions already exist, they are overwritten.</p>
    *
    * @param table The root table.
    *
    * @return The root filter conditions.
    * @exception IllegalArgumentException Thrown if the table is not mapped as a class table.
    */
   public FilterConditions createRootFilterConditions(Table table)
   {
      ClassTableMap    classTableMap;

      classTableMap = map.getClassTableMap(table);
      if (classTableMap == null)
         throw new IllegalArgumentException("Table not mapped as a class table: " + table.getUniversalName());

      rootFilterConditions = new FilterConditions(table);
      return rootFilterConditions;
   }
}