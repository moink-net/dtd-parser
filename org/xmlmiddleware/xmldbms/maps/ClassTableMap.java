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

public class ClassTableMap
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   // ********************************************************************
   // Variables
   // ********************************************************************

   private Table                  table = null;
   private Table                  baseTable = null;
   private LinkInfo               baseLinkInfo = null;
   private XMLName                elementTypeName = null;
   private Vector                 columnMaps = new Vector();
   private ColumnMap[]            columnMapArray = null;
   private Vector                 relatedClassTableMaps = new Vector();
   private RelatedClassTableMap[] relatedClassTableMapArray = null;
   private Vector                 propTableMaps = new Vector();
   private PropertyTableMap[]     propTableMapArray = null;

   Map     parentMap = null;

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
      if (parentMap != null)
      {
         if (parentMap.xmlNameInClassTableMap(elementTypeName))
            throw MapException("Element type already mapped: " + elementTypeName.getUniversalName());
      }
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
      ColumnMap columnMap;

      checkArgNull(columnName, ARG_COLUMNNAME);

      // Do a linear search of the columnMaps Vector. Return null if no ColumnMap
      // is found with the specified column name.

      for (int i = 0; i < columnMaps.size(); i++)
      {
         columnMap = (ColumnMap)columnMaps.elementAt(i);
         if (columnMap.getColumn().getName().equals(columnName)) return columnMap;
      }
      return null;
   }

   /**
    * Get the ColumnMaps for all columns.
    *
    * @return An array of the ColumnMaps for all columns. May be empty.
    */
   public final ColumnMap[] getColumnMaps()
   {
      // If the columnMapArray hasn't been created, create it now. An array is
      // used for speed in the data transfer classes.

      if (columnMapArray == null)
      {
         columnMapArray = new ColumnMap[columnMaps.size()];
         columnMaps.copyInto(columnMapArray);
      }
      return columnMapArray;
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

      // Get the column's name and ColumnMap, if any.

      name = column.getName();
      columnMap = getColumnMap(name);

      // If the column hasn't yet been mapped, create it and set a pointer
      // to its parent (this ClassTableMap), then add it to the columnMaps
      // Vector. Finally, null out the array of ColumnMaps, since this is
      // no longer valid.

      if (columnMap == null)
      {
         columnMap = ColumnMap.create(column);
         columnMap.parentClassTableMap = this;
         columnMaps.addElement(columnMap);
         columnMapArray = null;
      }

      // Return the ColumnMap for the specified column.

      return columnMap;
   }

   /**
    * Add a ColumnMap for a column.
    *
    * @param columnMap ColumnMap for the column.
    * @exception MapException Thrown if the column has already been mapped or if the
    *    ColumnMap maps an element type, attribute, or PCDATA that has already been mapped.
    */
   public void addColumnMap(ColumnMap columnMap)
      throws MapException
   {
      ColumnMap existingColumnMap;
      String name, xmlObject = null;

      checkArgNull(columnMap, ARG_COLUMNMAP);

      // Get the column's name and ColumnMap, if any.

      name = columnMap.getColumn().getName();
      existingColumnMap = getColumnMap(name);

      // Throw an exception if:
      // o The column has already been mapped.
      // o The ColumnMap has been used in a different parent.
      // o Another map already maps the element type, attribute, or PCDATA.

      if (existingColumnMap != null)
         throw new MapException("Column " + name + " already mapped.");
      if (columnMap.parentClassTableMap != null)
         throw new MapException("The ColumnMap is already used in the ClassTableMap for " + columnMap.parentClassTableMap.getTable().getUniversalName());
      if (xmlNameInDBPropertyMap(columnMap.getXMLName(), columnMap.getType()))
      {
         throw new MapException(columnMap.getXMLObjectName(columnMap.getType()) + columnMap.getXMLName().getUniversalName() + " already mapped in the ClassTableMap for " + this.table.getUniversalName());
      }

      // Set a pointer in the ColumnMap to its parent (this ClassTableMap),
      // then add it to the columnMaps Vector. Now null out the array of
      // ColumnMaps, since this is no longer valid.

      columnMap.parentClassTableMap = this;
      columnMaps.addElement(columnMap);
      columnMapArray = null;
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
      ColumnMap columnMap;

      checkArgNull(columnName, ARG_COLUMNNAME);

      // Do a linear search of the columnMaps Vector. If you find a ColumnMap
      // with the correct name, set its parent pointer to null, remove it from
      // the columnMaps Vector, and null out the array of ColumnMaps, which is
      // no longer valid.

      for (int i = 0; i < columnMaps.size(); i++)
      {
         columnMap = (ColumnMap)columnMaps.elementAt(i);
         if (columnMap.getColumn().getName().equals(columnName))
         {
            columnMap.parentClassTableMap = null;
            columnMaps.removeElementAt(i);
            columnMapArray = null;
            return;
         }
      }
      throw new MapException("Column " + columnName + " not mapped.");
   }

   /**
    * Remove the ColumnMaps for all columns.
    */
   public void removeAllColumnMaps()
   {
      ColumnMap columnMap;

      // Traverse the vector of ColumnMaps and null out the pointer to the
      // parent ClassTableMap in each one, then zero out the vector and the
      // ColumnMaps array.

      for (int i = 0; i < columnMaps.size(); i++)
      {
         columnMap = (ColumnMap)columnMaps.elementAt(columnName);
         columnMap.parentClassTableMap = null;
      }
      columnMaps.removeAllElements();
      columnMapArray = null;
   }

   // ********************************************************************
   // Related class table maps
   // ********************************************************************

   /**
    * Get the RelatedClassTableMap for a table.
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return A RelatedClassTableMap. Null if the table is not mapped
    *    as a related class.
    */
   public final RelatedClassTableMap getRelatedClassTableMap(String databaseName, String catalogName, String schemaName, String tableName)
   {
      String               universalName;
      RelatedClassTableMap relatedClassTableMap;

      universalName = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);

      for (int i = 0; i < relatedClassTableMaps.size(); i++)
      {
         relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.elementAt(i);
         if (relatedClassTableMap.getTableMap().getTable().getUniversalName().equals(universalName))
            return relatedClassTableMap;
      }
      return null;
   }

   /**
    * Get all RelatedClassTableMaps.
    *
    * @return An array of the RelatedClassTableMaps. May be empty.
    */
   public final RelatedClassTableMap[] getRelatedClassTableMaps()
   {
      if (relatedClassTableMapArray == null)
      {
         relatedClassTableMapArray = new RelatedClassTableMap[relatedClassTableMaps.size()];
         relatedClassTableMaps.copyInto(relatedClassTableMapArray);
      }
      return relatedClassTableMapArray;
   }

   /**
    * Create a RelatedClassTableMap and add it to this ClassTableMap.
    *
    * <p>If the related table has already been mapped, returns the
    * existing RelatedClassTableMap.</p>
    *
    * @param classTableMap The ClassTableMap of the related class table.
    *
    * @return The RelatedClassTableMap.
    */
   public RelatedClassTableMap createRelatedClassTableMap(ClassTableMap classTableMap)
   {

?? needs to worry about arc (element type) name
      RelatedClassTableMap relatedClassTableMap;
      String    name;

      checkArgNull(classTableMap, ARG_CLASSTABLEMAP);
      name = classTableMap.getTable().getUniversalName();
      relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.get(name);
      if (relatedClassTableMap == null)
      {
         relatedClassTableMap = RelatedClassTableMap.create(classTableMap);
         relatedClassTableMaps.put(name, relatedClassTableMap);
         relatedClassTableMap.parentClassTableMap = this;
         relatedClassTableMapArray = null;
      }
      return relatedClassTableMap;
   }

   /**
    * Add a RelatedClassTableMap.
    *
    * @param relatedClassTableMap The RelatedClassTableMap.
    * @exception MapException Thrown if the related class table has already been mapped
    *    or if the RelatedClassTableMap maps an element type, attribute, or PCDATA that
    *    has already been mapped.
    */
   public void addRelatedClassTableMap(RelatedClassTableMap relatedClassTableMap)
      throws MapException
   {
?? needs to worry about arc (element type) name
      Object o;
      String name;

      checkArgNull(relatedClassTableMap, ARG_RELATEDCLASSTABLEMAP);
      name = relatedClassTableMap.getClassTableMap().getTable().getUniversalName();
      o = relatedClassTableMaps.get(name);
      if (o != null)
         throw new MapException("Related class table " + name + " already mapped.");
      if (relatedClassTableMap.parentClassTableMap != null)
         throw new MapException("The RelatedClassTableMap is already used in the ClassTableMap for " + relatedClassTableMap.parentClassTableMap.getTable().getUniversalName());
      if (xmlNameInDBPropertyMap(relatedClassTableMap.getXMLName(), PropertyMapBase.ELEMENTTYPE))
      {
         throw new MapException("Element type " + relatedClassTableMap.getXMLName().getUniversalName() + " already mapped in the ClassTableMap for " + this.table.getUniversalName());
      }
      relatedClassTableMap.parentClassTableMap = this;
      relatedClassTableMaps.put(name, relatedClassTableMap);
      relatedClassTableMapArray = null;
   }

   /**
    * Remove a RelatedClassTableMap.
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @exception MapException Thrown if the related class table has not been mapped.
    */
   public void removeRelatedClassTableMap(String databaseName, String catalogName, String schemaName, String tableName)
      throws MapException
   {
      RelatedClassTableMap relatedClassTableMap;
      String               name;

      name = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);
      relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.remove(name);
      if (relatedClassTableMap == null)
         throw new MapException("Related class table " + name + " not mapped.");
      relatedClassTableMap.parentClassTableMap = null;
      relatedClassTableMapArray = null;
   }

   /**
    * Remove all RelatedClassTableMaps.
    */
   public void removeAllRelatedClassTableMaps()
   {
      Enumeration          enum;
      RelatedClassTableMap relatedClassTableMap;

      enum = relatedClassTableMaps.elements();
      while (enum.hasMoreElements())
      {
         relatedClassTableMap = (RelatedClassTableMap)enum.nextElement();
         relatedClassTableMap.parentClassTableMap = null;
      }
      relatedClassTableMaps.clear();
      relatedClassTableMapArray = null;
   }

   // ********************************************************************
   // Related property table maps
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
   public final PropertyTableMap getRelatedPropertyTableMap(String databaseName, String catalogName, String schemaName, String tableName)
   {
      String           universalName;
      PropertyTableMap propTableMap;

      universalName = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);

      for (int i = 0; i < propertyTableMaps.size(); i++)
      {
         propTableMap = (PropertyTableMap)propTableMaps.elementAt(i);
         if (propTableMap.getTable().getUniversalName().equals(universalName))
            return propTableMap;
      }
      return null;
   }

   /**
    * Get all PropertyTableMaps.
    *
    * @return An array of the PropertyTableMaps. May be empty.
    */
   public final Enumeration getPropertyTableMaps()
   {
      if (propTableMapArray == null)
      {
         propTableMapArray = new PropertyTableMap[propTableMaps.size()];
         propTableMaps.copyInto(propTableMapArray);
      }
      return propTableMapArray;
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
         propTableMap.parentClassTableMap = this;
         propTableMapArray = null;
      }
      return propTableMap;
   }

   /**
    * Add a PropertyTableMap.
    *
    * @param propTableMap The PropertyTableMap.
    * @exception MapException Thrown if the property table has already been mapped or
    *    if the PropertyTableMap maps an element type, attribute, or PCDATA that has
    *    already been mapped.
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
      if (propTableMap.parentClassTableMap != null)
         throw new MapException("The PropertyTableMap is already used in the ClassTableMap for " + propTableMap.parentClassTableMap.getTable().getUniversalName());
      if (xmlNameInDBPropertyMap(propTableMap.getXMLName(), propTableMap.getType()))
      {
         throw new MapException(propTableMap.getXMLObjectName(propTableMap.getType()) + propTableMap.getXMLName().getUniversalName() + " already mapped in the ClassTableMap for " + this.table.getUniversalName());
      }
      propTableMap.parentClassTableMap = this;
      propTableMaps.put(name, propTableMap);
      propTableMapArray = null;
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
      PropertyTableMap propTableMap;
      String name;

      name = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);
      propTableMap = (PropertyTableMap)propTableMaps.remove(name);
      if (propTableMap == null)
         throw new MapException("Property table " + name + " not mapped.");
      propTableMap.parentClassTableMap = null;
      propTableMapArray = null;
   }

   /**
    * Remove all PropertyTableMaps.
    */
   public void removeAllPropertyTableMaps()
   {
      Enumeration      enum;
      PropertyTableMap propTableMap;

      enum = propTableMaps.elements();
      while (enum.hasMoreElements())
      {
         propTableMap = (PropertyTableMap)enum.nextElement();
         propTableMap.parentClassTableMap = null;
      }
      propTableMaps.clear();
      propTableMapArray = null;
   }

   // ********************************************************************
   // Package methods
   // ********************************************************************

   boolean xmlNameInDBPropertyMap(XMLName newXMLName, int newType)
   {
      if ((newType == PropertyMapBase.UNKNOWN) || (newXMLName == null)) return false;
      if (xmlNameInPropertyMapBase(columnMaps, newXMLName, newType)) return true;
      if (xmlNameInPropertyMapBase(propTableMaps, newXMLName, newType)) return true;
      if (xmlNameInRelatedClassMap(newXMLName, newType)) return true;
      return false;
   }

   boolean xmlNameInPropertyMap(Vector propMapBases, XMLName newXMLName, int newType)
   {
      XMLName         xmlName;
      PropertyMapBase propMapBase;

      for (int i = 0; i < propMapBases.size(); i++)
      {
         propMapBase = (PropertyMapBase)propMapBases.elementAt(i);
?? if type == elementtype, check if inlinedelementtypes == null, if not, check first element instead of xmlname.
         if (propMapBase.getType() == newType)
         {
            xmlName = propMapBase.getXMLName();
            if (xmlName != null)
            {
               if (xmlName.equals(newXMLName)) return true;
            }
         }
      }
      return false;
   }

   boolean xmlNameInRelatedClassMap(XMLName newXMLName, int newType)
   {
      XMLName xmlName;

      if (newType != PropertyMapBase.ELEMENTTYPE) return false;

      for (int i = 0; i < relatedClassTableMaps.size(); i++)
      {
?? check if inlinedelementtypes == null, if not, check first element instead of xmlname.
         xmlName = ((RelatedClassTableMap)relatedClassTableMaps.elementAt(i)).getElementTypeName();
         if (xmlName != null)
         {
            if (xmlName.equals(newXMLName)) return true;
         }
      }
      return false;
   }
}
