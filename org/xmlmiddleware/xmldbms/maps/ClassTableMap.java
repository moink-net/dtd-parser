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
// This software was originally developed at the Technical University
// of Darmstadt, Germany.

// Version 2.0
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.XMLName;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Maps a table to a class; <a href="../readme.htm#NotForUse">
 * not for general use</a>.
 *
 * <p>ClassTableMap contains information about a table that is viewed as a
 * class and mapped to an element type. It is optimized for returning arrays
 * of ColumnMaps, PropertyTableMaps, and RelatedClassTableMaps; most other
 * methods require linear searches of one sort or another. This means that
 * map factories will be slower, but data transfer software will be faster.</p>
 *
 * <p>ClassTableMaps are stored in the Map object and in RelatedClassTableMap
 * objects.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 */

public class ClassTableMap extends MapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private Table                  table = null;
   private Table                  baseTable = null;
   private LinkInfo               baseLinkInfo = null;
   private XMLName                elementTypeName = null;
   private Hashtable              columnMaps = new Hashtable();
   private Vector                 relatedClassTableMaps = new Vector();
   private Hashtable              propTableMaps = new Hashtable();

   // ********************************************************************
   // Constructor
   // ********************************************************************

   private ClassTableMap(Table table)
   {
      this.table = table;
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new ClassTableMap.
    *
    * @param table The table being mapped.
    *
    * @return The ClassTableMap.
    */
   public static ClassTableMap create(Table table)
   {
      checkArgNull(table, ARG_TABLE);
      return new ClassTableMap(table);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Class table
   // ********************************************************************

   /**
    * Get the Table that the ClassTableMap maps.
    *
    * @return The Table.
    */
   public final Table getTable()
   {
      return table;
   }

   // ********************************************************************
   // Base table
   // ********************************************************************

   /**
    * Get the base Table.
    *
    * @return The base Table. Null if no base Table exists.
    */
   public final Table getBaseTable()
   {
      return baseTable;
   }

   /**
    * Set the base Table.
    *
    * @param baseTable The base table. If there is no base table,
    *    set this to null, in which case whether to use the base table will
    *    be set to false.
    */
   public void setBaseTable(Table baseTable)
   {
      this.baseTable = baseTable;
      if (baseTable == null)
      {
         this.baseLinkInfo = null;
      }      
   }

   /**
    * Get the LinkInfo used to link the class table to the base class table.
    *
    * @return The LinkInfo. The "parent" table is the base class table. Null if
    *    the base class table is not used. 
    */
   public final LinkInfo getBaseLinkInfo()
   {
      return baseLinkInfo;
   }

   /**
    * Set the LinkInfo used to link the table to the base table.
    *
    * <p>This method may not be called if the base table is null.</p>
    *
    * <p>Setting the baseLinkInfo argument to null when the base table is non-null
    * is useful if you want the map objects to preserve inheritance information
    * but want to store the data for the class in a single table, rather than in a
    * base table and a class table. Inheritance information can then be used elsewhere,
    * such as when an XML Schema is generated from a Map.</p>
    *
    * @param baseLinkInfo The LinkInfo. The "parent" table is the base table.
    *    Null if the base table is not used.
    */
   public void setBaseLinkInfo(LinkInfo baseLinkInfo)
   {
      if (baseTable == null)
         throw new IllegalStateException("Cannot call ClassTableMap.setUseBaseTable() if the base table is null.");
      this.baseLinkInfo = baseLinkInfo;
   }

   // ********************************************************************
   // Element type
   // ********************************************************************

   /**
    * Get the element type name.
    *
    * @return The name of the element type.
    */
   public final XMLName getElementTypeName()
   {
      return elementTypeName;
   }

   /**
    * Set the element type name.
    *
    * @param uri Namespace URI of the element type. May be null.
    * @param localName Local name of the element type.
    * @exception MapException Thrown if the element type name has already been mapped.
    */
   public void setElementTypeName(String uri, String localName)
      throws MapException
   {
      setElementTypeName(XMLName.create(uri, localName));
   }

   /**
    * Set the element type name.
    *
    * @param elementTypeName The element type name.
    * @exception MapException Thrown if the element type name has already been mapped.
    */
   public void setElementTypeName(XMLName elementTypeName)
      throws MapException
   {
      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      this.elementTypeName = elementTypeName;
   }

   // ********************************************************************
   // Column maps
   // ********************************************************************

   /**
    * Get the ColumnMap for a column.
    *
    * @param columnName The column name.
    *
    * @return A ColumnMap for the column. Null if the column
    *  is not mapped.
    */
   public final ColumnMap getColumnMap(String columnName)
   {
      checkArgNull(columnName, ARG_COLUMNNAME);
      return (ColumnMap)columnMaps.get(columnName);
   }

   /**
    * Get the ColumnMaps for all columns.
    *
    * @return An Enumeration of the ColumnMaps for all columns. May be empty.
    */
   public final Enumeration getColumnMaps()
   {
      return columnMaps.elements();
   }

   /**
    * Create a ColumnMap for a column and add it to this ClassTableMap.
    *
    * <p>If the column has already been mapped, returns the existing ColumnMap.</p>
    *
    * @param column The Column being mapped.
    *
    * @return The ColumnMap for the column.
    */
   public ColumnMap createColumnMap(Column column)
   {
      ColumnMap columnMap;
      String    name;

      checkArgNull(column, ARG_COLUMN);
      name = column.getName();
      columnMap = (ColumnMap)columnMaps.get(name);
      if (columnMap == null)
      {
         columnMap = ColumnMap.create(column);
         columnMaps.put(name, columnMap);
      }
      return columnMap;
   }

   /**
    * Add a ColumnMap for a column.
    *
    * @param columnMap ColumnMap for the column.
    * @exception MapException Thrown if the column has already been mapped.
    */
   public void addColumnMap(ColumnMap columnMap)
      throws MapException
   {
      Object o;
      String name;

      checkArgNull(columnMap, ARG_COLUMNMAP);
      name = columnMap.getColumn().getName();
      o = columnMaps.get(name);
      if (o != null)
         throw new MapException("Column " + name + " already mapped.");
      columnMaps.put(name, columnMap);
   }

   /**
    * Remove the ColumnMap for a column.
    *
    * @param columnName The column name.
    *
    * @exception MapException Thrown if the column has not been mapped.
    */
   public void removeColumnMap(String columnName)
      throws MapException
   {
      Object o;

      checkArgNull(columnName, ARG_COLUMNNAME);
      o = columnMaps.remove(columnName);
      if (o == null)
         throw new MapException("Column " + columnName + " not mapped.");
   }

   /**
    * Remove the ColumnMaps for all columns.
    */
   public void removeAllColumnMaps()
   {
      columnMaps.clear();
   }

   // ********************************************************************
   // Related class table maps
   // ********************************************************************

   /**
    * Get the RelatedClassTableMap(s) for a table.
    *
    * <p><b>WARNING:</b> There can be more than one RelatedClassTableMap for
    * a given table. This happens when two element types in a content model
    * both inherit from the same complex type. For example, a ShipToAddress and
    * a BillToAddress could both inherit from Address and both be mapped to the
    * Addresses table.</p>
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return An Enumeration of RelatedClassTableMaps. Empty if
    *    the table is not mapped as a related class.
    */
   public final Enumeration getRelatedClassTableMap(String databaseName, String catalogName, String schemaName, String tableName)
   {
      String               universalName;
      RelatedClassTableMap relatedClassTableMap;
      Vector               matchingMaps = new Vector();

      universalName = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);

      for (int i = 0; i < relatedClassTableMaps.size(); i++)
      {
         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.elementAt(i);
         if (relatedClassTableMap.getClassTableMap().getTable().getUniversalName().equals(universalName))
         {
            matchingMaps.addElement(relatedClassTableMap);
         }
      }
      return matchingMaps.elements();
   }

   /**
    * Get all RelatedClassTableMaps.
    *
    * @return An Enumeration of RelatedClassTableMaps. May be empty.
    */
   public final Enumeration getRelatedClassTableMaps()
   {
      return relatedClassTableMaps.elements();
   }

   /**
    * Add a RelatedClassTableMap.
    *
    * <p><b>WARNING:</b> This method does not return an error if the table has
    * already been mapped as a related class table. For details, see
    * getRelatedClassTableMap.</p>
    *
    * <p>If a table is mapped more than once as a related class table, each
    * RelatedClassTableMap must have a different element type name. This is
    * not checked in this method. Instead, it is checked in MapChecker.</p>
    *
    * @param relatedClassTableMap The RelatedClassTableMap.
    */
   public void addRelatedClassTableMap(RelatedClassTableMap relatedClassTableMap)
   {
      checkArgNull(relatedClassTableMap, ARG_RELATEDCLASSTABLEMAP);
      relatedClassTableMaps.addElement(relatedClassTableMap);
   }

   /**
    * Remove a RelatedClassTableMap(s).
    *
    * <p><b>WARNING:</b> This method removes all RelatedClassTableMaps for the
    * specified table.</p>
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @exception MapException Thrown if the related class table has not been mapped.
    */
   public void removeRelatedClassTableMaps(String databaseName, String catalogName, String schemaName, String tableName)
      throws MapException
   {
      String               universalName;
      RelatedClassTableMap relatedClassTableMap;

      universalName = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);

      for (int i = 0; i < relatedClassTableMaps.size(); i++)
      {
         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.elementAt(i);
         if (relatedClassTableMap.getClassTableMap().getTable().getUniversalName().equals(universalName))
         {
            relatedClassTableMaps.removeElementAt(i);
         }
      }
   }

   /**
    * Remove all RelatedClassTableMaps.
    */
   public void removeAllRelatedClassTableMaps()
   {
      relatedClassTableMaps.removeAllElements();
   }

   // ********************************************************************
   // Property table maps
   // ********************************************************************

   /**
    * Get the PropertyTableMap for a table.
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return A PropertyTableMap. Null if the table is not mapped
    *    as a property table.
    */
   public final PropertyTableMap getPropertyTableMap(String databaseName, String catalogName, String schemaName, String tableName)
   {
      return (PropertyTableMap)propTableMaps.get(Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Get all PropertyTableMaps.
    *
    * @return An Enumeration of the PropertyTableMaps. May be empty.
    */
   public final Enumeration getPropertyTableMaps()
   {
      return propTableMaps.elements();
   }

   /**
    * Create a PropertyTableMap and add it to this ClassTableMap.
    *
    * <p>If the table has already been mapped, returns the
    * existing PropertyTableMap.</p>
    *
    * @param table The property table.
    *
    * @return The PropertyTableMap.
    */
   public PropertyTableMap createPropertyTableMap(Table table)
   {
      PropertyTableMap propTableMap;
      String    name;

      checkArgNull(table, ARG_TABLE);
      name = table.getUniversalName();
      propTableMap = (PropertyTableMap)propTableMaps.get(name);
      if (propTableMap == null)
      {
         propTableMap = PropertyTableMap.create(table);
         propTableMaps.put(name, propTableMap);
      }
      return propTableMap;
   }

   /**
    * Add a PropertyTableMap.
    *
    * @param propTableMap The PropertyTableMap.
    * @exception MapException Thrown if the property table has already been mapped.
    */
   public void addPropertyTableMap(PropertyTableMap propTableMap)
      throws MapException
   {
      Object o;
      String name;

      checkArgNull(propTableMap, ARG_PROPTABLEMAP);
      name = propTableMap.getTable().getUniversalName();
      o = propTableMaps.get(name);
      if (o != null)
         throw new MapException("Property table " + name + " already mapped.");
      propTableMaps.put(name, propTableMap);
   }

   /**
    * Remove a PropertyTableMap.
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @exception MapException Thrown if the related property table has not been mapped.
    */
   public void removePropertyTableMap(String databaseName, String catalogName, String schemaName, String tableName)
      throws MapException
   {
      Object o;
      String name;

      name = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);
      o = propTableMaps.remove(name);
      if (o == null)
         throw new MapException("Property table " + name + " not mapped.");
   }

   /**
    * Remove all PropertyTableMaps.
    */
   public void removeAllPropertyTableMaps()
   {
      propTableMaps.clear();
   }
}
