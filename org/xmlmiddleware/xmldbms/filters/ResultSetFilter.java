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

import org.xmlmiddleware.xmldbms.maps.Map;
import org.xmlmiddleware.xmldbms.maps.Table;

/**
 * A filter to retrieve a document fragment based on a result set. <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class ResultSetFilter extends FilterBase
{
   //*********************************************************************
   // Class variables
   //*********************************************************************

   private String databaseName;
   private String catalogName;
   private String schemaName;
   private String tableName;

   //*********************************************************************
   // Constants
   //*********************************************************************

   private static String DEFAULT = "Default";

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new ResultSetFilter object.
    *
    * @param map The Map to which the filter applies.
    */
   protected ResultSetFilter(Map map)
   {
      super(map);
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //*********************************************************************
   // Result set info
   //*********************************************************************

   /**
    * Get the database name used to map the result set.
    *
    * @return The database name
    */
   public final String getDatabaseName()
   {
      return databaseName;
   }

   /**
    * Get the catalog name used to map the result set.
    *
    * @return The catalog name
    */
   public final String getCatalogName()
   {
      return catalogName;
   }

   /**
    * Get the schema name used to map the result set.
    *
    * @return The schema name
    */
   public final String getSchemaName()
   {
      return schemaName;
   }

   /**
    * Get the table name used to map the result set.
    *
    * @return The table name
    */
   public final String getTableName()
   {
      return tableName;
   }

   /**
    * Set the name of the table used to map the result set.
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @exception IllegalArgumentException Thrown if the table is not mapped as a class table.
    */
   public void setTable(String databaseName, String catalogName, String schemaName, String tableName)
   {
      if (map.getClassTableMap(databaseName, catalogName, schemaName, tableName) == null)
         throw new IllegalArgumentException("Table not mapped as a class table: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));

      this.databaseName = (databaseName == null) ? DEFAULT : databaseName;
      this.catalogName = catalogName;
      this.schemaName = schemaName;
      this.tableName = tableName;
   }
}