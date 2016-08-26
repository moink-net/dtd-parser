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

import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.maps.*;

import java.util.*;

/**
 * A container for filters between a class table and child tables. <b>For internal use.</b>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class TableFilter
{
   //*********************************************************************
   // Constants
   //*********************************************************************

   private static String SEMICOLON = ";";

   //*********************************************************************
   // Class variables
   //*********************************************************************

   private ClassTableMap classTableMap;
   private Hashtable     relatedTableFilters = new Hashtable();

   //*********************************************************************
   // Constructors
   //*********************************************************************

   /**
    * Construct a new TableFilter object.
    *
    * @param classTableMap The ClassTableMap for the table to which the filter applies.
    * @return The TableFilter
    */
   protected TableFilter(ClassTableMap classTableMap)
   {
      this.classTableMap = classTableMap;
   }

   //*********************************************************************
   // Public methods
   //*********************************************************************

   //*********************************************************************
   // Table
   //*********************************************************************

   /**
    * Get the table to which the filter applies.
    *
    * @return The table.
    */
   public final Table getTable()
   {
      return classTableMap.getTable();
   }

   //*********************************************************************
   // Related table filters
   //*********************************************************************

   /**
    * Get the filter for a related table by name.
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    * @param parentKeyName Name of the parent key used to link the table and the
    *    related table. If the related table only appears once as a child of the
    *    parent table, this may be null.
    * @param childKeyName Name of the child key used to link the table and the
    *    related table. If the related table only appears once as a child of the
    *    parent table, this may be null.
    * @return The filter. May be null.
    * @exception XMLMiddlewareException Returned if the specified table is not
    *    related to the table that uses this TableFilter.
    */
   public final RelatedTableFilter getRelatedTableFilter(String databaseName, String catalogName, String schemaName, String tableName, String parentKeyName, String childKeyName)
      throws XMLMiddlewareException
   {
      String hashName;

      hashName = getHashName(databaseName, catalogName, schemaName, tableName, parentKeyName, childKeyName);
      return (RelatedTableFilter)relatedTableFilters.get(hashName);
   }

   /**
    * Get the filter for a related class table.
    *
    * @param relatedClassTableMap RelatedClassTableMap that describes the related table.
    * @return The filter. May be null.
    */
   public final RelatedTableFilter getRelatedTableFilter(RelatedClassTableMap relatedClassTableMap)
   {
      String hashName;

      hashName = getHashName(relatedClassTableMap.getClassTableMap().getTable(), relatedClassTableMap.getLinkInfo());
      return (RelatedTableFilter)relatedTableFilters.get(hashName);
   }

   /**
    * Get the filter for a property table.
    *
    * @param propTableMap PropertyTableMap that describes the related table.
    * @return The filter. May be null.
    */
   public final RelatedTableFilter getRelatedTableFilter(PropertyTableMap propTableMap)
   {
      String hashName;

      hashName = getHashName(propTableMap.getTable(), propTableMap.getLinkInfo());
      return (RelatedTableFilter)relatedTableFilters.get(hashName);
   }

   /**
    * Get the filters for all related tables.
    *
    * @return An Enumeration of RelatedTableFilter objects. May be empty.
    */
   public final Enumeration getRelatedTableFilters()
   {
      return relatedTableFilters.elements();
   }

   /**
    * Create a filter for a related table.
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    * @param parentKeyName Name of the parent key used to link the table and the
    *    related table. If the related table only appears once as a child of the
    *    parent table, this may be null.
    * @param childKeyName Name of the child key used to link the table and the
    *    related table. If the related table only appears once as a child of the
    *    parent table, this may be null.
    * @return The filter
    * @exception XMLMiddlewareException Returned if the filter already exists or the
    *    specified table is not related to the table that uses this TableFilter.
    */
   public RelatedTableFilter createRelatedTableFilter(String databaseName, String catalogName, String schemaName, String tableName, String parentKeyName, String childKeyName)
      throws XMLMiddlewareException
   {
      Object               o;
      PropertyTableMap     propTableMap;
      RelatedClassTableMap relatedClassTableMap;
      Table                table;
      String               hashName;
      LinkInfo             linkInfo;
      RelatedTableFilter   relatedTableFilter;

      o = getRelatedTableMap(databaseName, catalogName, schemaName, tableName, parentKeyName, childKeyName);
      if (o instanceof PropertyTableMap)
      {
         propTableMap = (PropertyTableMap)o;
         table = propTableMap.getTable();
         linkInfo = propTableMap.getLinkInfo();
      }
      else // if (o instanceof RelatedClassTableMap)
      {
         relatedClassTableMap = (RelatedClassTableMap)o;
         table = relatedClassTableMap.getClassTableMap().getTable();
         linkInfo = relatedClassTableMap.getLinkInfo();
      }
      hashName = getHashName(table, linkInfo);
      if (relatedTableFilters.get(hashName) != null)
         throw new XMLMiddlewareException("Related table filter already exists for " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName) + " with the parent key name '" + parentKeyName + "' and the child key name '" + childKeyName + "'.");

      relatedTableFilter = new RelatedTableFilter(table, linkInfo.getParentKey().getName(), linkInfo.getChildKey().getName());
      relatedTableFilters.put(hashName, relatedTableFilter);
      return relatedTableFilter;
   }

   /**
    * Remove a filter for a related table.
    *
    * @param databaseName Name of the database. If this is null, "Default" is used.
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    * @param parentKeyName Name of the parent key used to link the table and the
    *    related table. If the related table only appears once as a child of the
    *    parent table, this may be null.
    * @param childKeyName Name of the child key used to link the table and the
    *    related table. If the related table only appears once as a child of the
    *    parent table, this may be null.
    * @exception XMLMiddlewareException Returned if the filter does not exist.
    */
   public void removeRelatedTableFilter(String databaseName, String catalogName, String schemaName, String tableName, String parentKeyName, String childKeyName)
      throws XMLMiddlewareException
   {
      String hashName;

      hashName = getHashName(databaseName, catalogName, schemaName, tableName, parentKeyName, childKeyName);
      if (relatedTableFilters.remove(hashName) == null)
         throw new XMLMiddlewareException("Related table filter not found for " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName) + " with the parent key name '" + parentKeyName + "' and the child key name '" + childKeyName + "'.");
   }

   /**
    * Remove all related table filters.
    */
   public void removeAllRelatedTableFilters()
   {
      relatedTableFilters.clear();
   }

   //*********************************************************************
   // Private methods
   //*********************************************************************

   private Object getRelatedTableMap(String databaseName, String catalogName, String schemaName, String tableName, String parentKeyName, String childKeyName)
      throws XMLMiddlewareException
   {
      PropertyTableMap     propTableMap;
      Enumeration          relatedClassTableMaps;
      RelatedClassTableMap relatedClassTableMap = null;
      int                  count;
      LinkInfo             linkInfo;
      boolean              tableFound;

      // This method returns a PropertyTableMap or a RelatedClassTableMap for
      // the specified table.

      // Check that the arguments are legal.

      if (((parentKeyName == null) && (childKeyName != null)) ||
          ((parentKeyName != null) && (childKeyName == null)))
         throw new IllegalArgumentException("Parent key name and child key name must both be null or both be non-null.");

      // First check if the table is a property table. If so, check that the correct
      // keys are used (assuming they are specified), then return the PropertyTableMap.

      propTableMap = classTableMap.getPropertyTableMap(databaseName, catalogName, schemaName, tableName);
      if (propTableMap != null)
      {
         linkInfo = propTableMap.getLinkInfo();
         if (parentKeyName != null)
         {
            if ((!parentKeyName.equals(linkInfo.getParentKey().getName())) ||
                (!childKeyName.equals(linkInfo.getChildKey().getName())))
               throw new XMLMiddlewareException("The property table " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName) + " is not linked to the class table " + classTableMap.getTable().getUniversalName() + " with the keys named " + parentKeyName + " and " + childKeyName);
         }
         return propTableMap;
      }

      // If the table is not a property table, check if it is a related class table.
      // We first count the number of times the table appears as a related class table
      // of the parent.

      count = 0;
      relatedClassTableMaps = classTableMap.getRelatedClassTableMap(databaseName, catalogName, schemaName, tableName);
      while (relatedClassTableMaps.hasMoreElements())
      {
         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.nextElement();
         count++;
      }

      // If no RelatedClassTableMaps were found, throw an exception.

      if (relatedClassTableMap == null)
         throw new XMLMiddlewareException("The table " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName) + " does not appear as a related class table or property table of the class table " + classTableMap.getTable().getUniversalName());

      if (count == 1)
      {
         // In the most common case, the child table appears once. In this case, check
         // if the input key names match those used in the LinkInfo. If the input key
         // names are null, use the key names in the LinkInfo.

         linkInfo = relatedClassTableMap.getLinkInfo();
         if (parentKeyName != null)
         {
            if ((!parentKeyName.equals(linkInfo.getParentKey().getName())) ||
                (!childKeyName.equals(linkInfo.getChildKey().getName())))
               throw new XMLMiddlewareException("The related class table " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName) + " is never linked to the class table " + classTableMap.getTable().getUniversalName() + " with the keys named " + parentKeyName + " and " + childKeyName);
         }
      }
      else
      {
         // If the child table appears more than once, the user must specify
         // the parent and child key names.

         if (parentKeyName == null)
            throw new XMLMiddlewareException(Table.getUniversalName(databaseName, catalogName, schemaName, tableName) + " appears more than once as a related class table of " + classTableMap.getTable().getUniversalName() + ". In this case, you must provide the names of the parent and child keys used to link the two tables.");

         // Go through the enumeration of RelatedClassTableMaps again and find
         // the one we want. This is inefficient, but it is a very uncommon
         // case, so we can live with it.

         tableFound = false;
         relatedClassTableMaps = classTableMap.getRelatedClassTableMap(databaseName, catalogName, schemaName, tableName);
         while (relatedClassTableMaps.hasMoreElements())
         {
            relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.nextElement();
            linkInfo = relatedClassTableMap.getLinkInfo();
            if (linkInfo.getParentKey().getName().equals(parentKeyName))
            {
               if (linkInfo.getChildKey().getName().equals(childKeyName))
               {
                  tableFound = true;
                  break;
               }
            }
         }

         if (!tableFound)
            throw new XMLMiddlewareException(Table.getUniversalName(databaseName, catalogName, schemaName, tableName) + " does not appear as a related table of " + classTableMap.getTable().getUniversalName() + " linked with the keys " + parentKeyName + " and " + childKeyName + ".");
      }

      return relatedClassTableMap;
   }

   private String getHashName(String databaseName, String catalogName, String schemaName, String tableName, String parentKeyName, String childKeyName)
      throws XMLMiddlewareException
   {
      Object               o;
      PropertyTableMap     propTableMap;
      RelatedClassTableMap relatedClassTableMap;

      o = getRelatedTableMap(databaseName, catalogName, schemaName, tableName, parentKeyName, childKeyName);
      if (o instanceof PropertyTableMap)
      {
         propTableMap = (PropertyTableMap)o;
         return getHashName(propTableMap.getTable(), propTableMap.getLinkInfo());
      }
      else // if (o instanceof RelatedClassTableMap)
      {
         relatedClassTableMap = (RelatedClassTableMap)o;
         return getHashName(relatedClassTableMap.getClassTableMap().getTable(), relatedClassTableMap.getLinkInfo());
      }
   }

   private String getHashName(Table table, LinkInfo linkInfo)
   {
      String   iParent, iChild;

      // Build a name from the hashcodes of the database, catalog, schema, table,
      // parent key, and child key names. This will be unique and can be used
      // as a multi-part hashkey.

      iParent = Integer.toString(linkInfo.getParentKey().getName().hashCode());
      iChild = Integer.toString(linkInfo.getParentKey().getName().hashCode());
      return table.getHashName() + SEMICOLON + iParent + SEMICOLON + iChild;
   }
}
