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

import java.util.Enumeration;
import java.util.Hashtable;
import org.xmlmiddleware.utils.XMLName;

/**
 * Base class for ClassTableMap and ElementInsertionMap;
 * <a href="../readme.htm#NotForUse"> not for general use</a>.
 *
 * <p>ClassTableMapBase provides accessors and mutators for element type
 * name and child constructs (columns, related class tables, related
 * property tables, and ElementInsertionMaps). It is the base class for
 * ClassTableMap and ElementInsertionMap.</p>
 *
 * <p>ClassTableMapBase is never used directly. It is only used through
 * ClassTableMap and ElementInsertionMap.</p>
 *
 * @author Ronald Bourret, 1998-9, 2001
 * @version 2.0
 * @see ClassTableMap
 * @see ElementInsertionMap
 */

public class ClassTableMapBase extends MapBase
{
   // ********************************************************************
   // Variables
   // ********************************************************************

   private XMLName   elementTypeName = null;
   private Hashtable columnMaps = new Hashtable();
   private Hashtable relatedClassTableMaps = new Hashtable();
   private Hashtable propTableMaps = new Hashtable();
   private Hashtable elementInsertionMaps = new Hashtable();

   // ********************************************************************
   // Constructor
   // ********************************************************************

   ClassTableMapBase()
   {
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

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

   // PROTECTED! Called by ClassTableMap.setElementTypeName() and
   // ElementInsertionMap.create().

   void setElementTypeName(String uri, String localName)
   {
      this.elementTypeName = XMLName.create(uri, localName, null);
   }

   void setElementTypeName(XMLName elementTypeName)
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
      return (RelatedClassTableMap)relatedClassTableMaps.get(Table.getUniversalName(databaseName, catalogName, schemaName, tableName));
   }

   /**
    * Get all RelatedClassTableMaps.
    *
    * @return An Enumeration of the RelatedClassTableMaps. May be empty.
    */
   public final Enumeration getRelatedClassTableMaps()
   {
      return relatedClassTableMaps.elements();
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
      RelatedClassTableMap relatedClassTableMap;
      String    name;

      checkArgNull(classTableMap, ARG_CLASSTABLEMAP);
      name = classTableMap.getTable().getUniversalName();
      relatedClassTableMap = (RelatedClassTableMap)relatedClassTableMaps.get(name);
      if (relatedClassTableMap == null)
      {
         relatedClassTableMap = RelatedClassTableMap.create(classTableMap);
         relatedClassTableMaps.put(name, relatedClassTableMap);
      }
      return relatedClassTableMap;
   }

   /**
    * Add a RelatedClassTableMap.
    *
    * @param relatedClassTableMap The RelatedClassTableMap.
    * @exception MapException Thrown if the related class table has already been mapped.
    */
   public void addRelatedClassTableMap(RelatedClassTableMap relatedClassTableMap)
      throws MapException
   {
      Object o;
      String name;

      checkArgNull(relatedClassTableMap, ARG_RELATEDCLASSTABLEMAP);
      name = relatedClassTableMap.getClassTableMap().getTable().getUniversalName();
      o = relatedClassTableMaps.get(name);
      if (o != null)
         throw new MapException("Related class table " + name + " already mapped.");
      relatedClassTableMaps.put(name, relatedClassTableMap);
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
      Object o;
      String name;

      name = Table.getUniversalName(databaseName, catalogName, schemaName, tableName);
      o = relatedClassTableMaps.remove(name);
      if (o == null)
         throw new MapException("Related class table " + name + " not mapped.");
   }

   /**
    * Remove all RelatedClassTableMaps.
    */
   public void removeAllRelatedClassTableMaps()
   {
      relatedClassTableMaps.clear();
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

   // ********************************************************************
   // Element insertion maps
   // ********************************************************************

   /**
    * Get the ElementInsertionMap for an element type.
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @return An ElementInsertionMap. Null if the element type is not mapped
    *    as an inserted element.
    */
   public final ElementInsertionMap getElementInsertionMap(String uri, String localName)
   {
      return getElementInsertionMap(XMLName.getUniversalName(uri, localName));
   }

   /**
    * Get the ElementInsertionMap for an element type.
    *
    * @param universalName Universal name of the child element type.
    *
    * @return An ElementInsertionMap. Null if the element type is not mapped
    *    as an inserted element.
    */
   public final ElementInsertionMap getElementInsertionMap(String universalName)
   {
      checkArgNull(universalName, ARG_UNIVERSALNAME);
      return (ElementInsertionMap)elementInsertionMaps.get(universalName);
   }

   /**
    * Get all ElementInsertionMaps.
    *
    * @return An Enumeration of the ElementInsertionMaps. May be empty.
    */
   public final Enumeration getElementInsertionMaps()
   {
      return elementInsertionMaps.elements();
   }

   /**
    * Create an ElementInsertionMap and add it to this ClassTableMap.
    *
    * <p>If an ElementInsertionMap has already been created for the element
    * type, returns the existing ElementInsertionMap.</p>
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @return The ElementInsertionMap.
    */
   public ElementInsertionMap createElementInsertionMap(String uri, String localName)
   {
      return createElementInsertionMap(XMLName.create(uri, localName));
   }

   /**
    * Create an ElementInsertionMap and add it to this ClassTableMap.
    *
    * <p>If an ElementInsertionMap has already been created for the element
    * type, returns the existing ElementInsertionMap.</p>
    *
    * @param elementTypeName XMLName of the child element type.
    *
    * @return The ElementInsertionMap.
    */
   public ElementInsertionMap createElementInsertionMap(XMLName elementTypeName)
   {
      ElementInsertionMap elementInsertionMap;
      String              universalName;

      checkArgNull(elementTypeName, ARG_ELEMENTTYPENAME);
      universalName = elementTypeName.getUniversalName();
      elementInsertionMap = (ElementInsertionMap)elementInsertionMaps.get(universalName);
      if (elementInsertionMap == null)
      {
         elementInsertionMap = ElementInsertionMap.create(elementTypeName);
         elementInsertionMaps.put(universalName, elementInsertionMap);
      }
      return elementInsertionMap;
   }

   /**
    * Add an ElementInsertionMap.
    *
    * @param elementInsertionMap The ElementInsertionMap.
    * @exception MapException Thrown if an ElementInsertionMap has already been
    *    added for the element type.
    */
   public void addElementInsertionMap(ElementInsertionMap elementInsertionMap)
      throws MapException
   {
      Object o;
      String name;

      checkArgNull(elementInsertionMap, ARG_ELEMENTINSERTIONMAP);
      name = elementInsertionMap.getElementTypeName().getUniversalName();
      o = elementInsertionMaps.get(name);
      if (o != null)
         throw new MapException("ElementInsertionMap already added for element type " + name + ".");
      elementInsertionMaps.put(name, elementInsertionMap);
   }

   /**
    * Remove an ElementInsertionMap.
    *
    * @param uri Namespace URI of the child element type. May be null.
    * @param localName Local name of the child element type.
    *
    * @exception MapException Thrown if no ElementInsertionMap has been added
    *    for the element type.
    */
   public void removeElementInsertionMap(String uri, String localName)
      throws MapException
   {
      removeElementInsertionMap(XMLName.getUniversalName(uri, localName));
   }

   /**
    * Remove an ElementInsertionMap.
    *
    * @param universalName Universal name of the child element type.
    *
    * @exception MapException Thrown if no ElementInsertionMap has been added
    *    for the element type.
    */
   public void removeElementInsertionMap(String universalName)
      throws MapException
   {
      Object o;

      checkArgNull(universalName, ARG_UNIVERSALNAME);
      o = elementInsertionMaps.remove(universalName);
      if (o == null)
         throw new MapException("ElementInsertionMap not added for element type " + universalName + ".");
   }

   /**
    * Remove all ElementInsertionMaps.
    */
   public void removeAllElementInsertionMaps()
   {
      elementInsertionMaps.clear();
   }
}
