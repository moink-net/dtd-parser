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
// Changes from version 1.0:
// * Added rsColumnNumbers array and modified constructor.
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmlutils.*;

import java.util.*;

/**
 * Describes a table; <a href="../readme.html#NotForUse">not for general
 * use</a>.
 *
 * <p>Table contains information about a table. Tables are stored in Maps,
 * ClassMaps, PropertyMaps, ClassTableMaps, and PropertyTableMaps.</p>
 *
 * <p>In XML-DBMS, tables have four-part names: database, catalog, schema,
 * and table name. The database name is the name of the XML-DBMS database
 * to use; it describes the connection information to use and has nothing
 * to do with clusters, which are the fourth (top) level of names in SQL '92.
 * Catalog, schema, and table names are as in SQL '92.</p>
 *
 * <p>XML-DBMS also has "universal table names", which have the following
 * form:</p>
 *
 * <pre>
 *    "database-name"."catalog-name"."schema-name"."table-name"
 * </pre>
 *
 * <p>These are used in error messages. XML-DBMS applications do not need to use
 * them. Note that it is probably possible to construct two different tables with
 * the same universal name, but since these names are only used in error messages,
 * it shouldn't matter much.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 */

public class Table extends MapBase
{
   // ********************************************************************
   // Constants
   // ********************************************************************

   private static String DEFAULT = "Default";
   private static String DOUBLEQUOTE = "\"";
   private static String DQPDQ = "\".\"";    // Double Quote Period Double Quote
   private static String PRIMARYKEY = "PrimaryKey";
   private static String SEMICOLON = ";";
   private static String NULL = "null";

   // ********************************************************************
   // Variables
   // ********************************************************************

   private String databaseName = null;
   private String catalogName = null;
   private String schemaName = null;
   private String tableName = null;

   private String hashName = null;

   private Hashtable columns = new Hashtable();
   private Column[]  rsColumns = null;

   private Key primaryKey = null;
   private Hashtable uniqueKeys = new Hashtable();
   private Hashtable foreignKeys = new Hashtable();

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private Table(String databaseName, String catalogName, String schemaName, String tableName)
   {
      this.databaseName = (databaseName != null) ? databaseName : DEFAULT;
      this.catalogName = catalogName;
      this.schemaName = schemaName;
      this.tableName = tableName;
      hashName = getHashName(databaseName, catalogName, schemaName, tableName);
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new Table.
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The Table.
    */
   public static Table create(String databaseName, String catalogName, String schemaName, String tableName)
   {
      checkArgNull(tableName, ARG_TABLENAME);
      return new Table(databaseName, catalogName, schemaName, tableName);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Names
   // ********************************************************************

   /**
    * Get the database name.
    *
    * @return The database name.
    */
   public final String getDatabaseName()
   {
      return databaseName;
   }

   /**
    * Get the catalog name.
    *
    * @return The catalog name.
    */
   public final String getCatalogName()
   {
      return catalogName;
   }

   /**
    * Get the schema name.
    *
    * @return The schema name.
    */
   public final String getSchemaName()
   {
      return schemaName;
   }

   /**
    * Get the table name.
    *
    * @return The table name.
    */
   public final String getTableName()
   {
      return tableName;
   }

   /**
    * Get the universal name of the table.
    *
    * <p>This name should be used only in error messages. For a "universal" name
    * that can safely be hashed on, use getHashName().</p>
    *
    * @return The universal name.
    */
   public final String getUniversalName()
   {
      return getUniversalName(databaseName, catalogName, schemaName, tableName);
   }

   /**
    * Construct a universal table name from the specified names.
    *
    * <p>This name should be used only in error messages. For a "universal" name
    * that can safely be hashed on, use getHashName(String, String, String, String).</p>
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The universal name.
    */
   public static String getUniversalName(String databaseName, String catalogName, String schemaName, String tableName)
   {
      StringBuffer universalName = new StringBuffer();

      checkArgNull(tableName, ARG_TABLENAME);
      universalName.append(DOUBLEQUOTE);
      universalName.append((databaseName != null) ? databaseName : DEFAULT);
      universalName.append(DQPDQ);
      if (catalogName != null) universalName.append(catalogName);
      universalName.append(DQPDQ);
      if (schemaName != null) universalName.append(schemaName);
      universalName.append(DQPDQ);
      universalName.append(tableName);
      universalName.append(DOUBLEQUOTE);
      return universalName.toString();
   }

   /**
    * Get a "universal" name that is safe for hashing.
    *
    * @return The hash name.
    */
   public final String getHashName()
   {
      return hashName;
   }

   /**
    * Get a "universal" name that is safe for hashing.
    *
    * @param databaseName Name of the database. If this is null, uses "Default".
    * @param catalogName Name of the catalog. May be null.
    * @param schemaName Name of the schema. May be null.
    * @param tableName Name of the table.
    *
    * @return The hash name.
    */
   public static String getHashName(String databaseName, String catalogName, String schemaName, String tableName)
   {
      String dHash, cHash, sHash, tHash;

      checkArgNull(tableName, ARG_TABLENAME);
      if (databaseName == null)
      {
         databaseName = DEFAULT;
      }
      dHash = Integer.toString(databaseName.hashCode());
      cHash = (catalogName == null) ? NULL : Integer.toString(catalogName.hashCode());
      sHash = (schemaName == null) ? NULL : Integer.toString(schemaName.hashCode());
      tHash = Integer.toString(tableName.hashCode());

      return (dHash + SEMICOLON + cHash + SEMICOLON + sHash + SEMICOLON + tHash);
   }

   // ********************************************************************
   // Columns
   // ********************************************************************

   /**
    * Get number of Columns.
    *
    * @return The number of Columns. 
    */
   public final int getNumberOfColumns()
   {
      return columns.size();
   }

   /**
    * Get a Column.
    *
    * @param columnName Name of the column.
    *
    * @return The Column. Null if the column does not exist.
    */
   public final Column getColumn(String columnName)
   {
      checkArgNull(columnName, ARG_COLUMNNAME);
      return (Column)columns.get(columnName);
   }

   /**
    * Get all Columns.
    *
    * @return An Enumeration of all Columns. May be empty.
    */
   public final Enumeration getColumns()
   {
      return columns.elements();
   }

   /**
    * Get all Columns in ascending order according to their result set index.
    *
    * <p>For more information, see the Column class.</p>
    *
    * @return The columns
    */
   public final Column[] getResultSetColumns()
   {
      Enumeration enum;
      Column      column;
      long[]      rsIndexes;

      if (rsColumns == null)
      {
         rsIndexes = new long[columns.size()];
         rsColumns = new Column[columns.size()];

         enum = columns.elements();
         for (int i = 0; i < columns.size(); i++)
         {
            column = (Column)enum.nextElement();
            rsColumns[i] = column;
            rsIndexes[i] = (long)column.getResultSetIndex();
         }

         Sort.sort(rsIndexes, rsColumns);
      }

      return rsColumns;
   }

   /**
    * Create a Column and add it to the Table.
    *
    * <p>If the Column already exists, returns the existing Column.</p>
    *
    * @param columnName Name of the column.
    *
    * @return The Column.
    */
   public Column createColumn(String columnName)
   {
      Column column;

      checkArgNull(columnName, ARG_COLUMNNAME);
      column = (Column)columns.get(columnName);
      if (column == null)
      {
         column = Column.create(columnName);
         columns.put(columnName, column);
      }

      rsColumns = null;

      return column;
   }

   /**
    * Add a Column.
    *
    * @param column The Column.
    * @exception XMLMiddlewareException Thrown if the Column already exists.
    */
   public void addColumn(Column column)
      throws XMLMiddlewareException
   {
      Object o;
      String name;

      checkArgNull(column, ARG_COLUMN);
      name = column.getName();
      o = columns.get(name);
      if (o != null)
         throw new XMLMiddlewareException("Column " + name + " already exists in " + getUniversalName() + ".");
      columns.put(name, column);

      rsColumns = null;
   }

   /**
    * Remove a Column.
    *
    * <p>This method should be used carefully, as numerous other map objects
    * point to Columns. Those objects should be deleted before this method is called.</p>
    *
    * @param columnName Name of the column.
    *
    * @exception XMLMiddlewareException Thrown if the column does not exist.
    */
   public void removeColumn(String columnName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(columnName, ARG_COLUMNNAME);

      o = columns.remove(columnName);
      if (o == null)
         throw new XMLMiddlewareException("Column " + columnName + " not found in table " + getUniversalName());

      rsColumns = null;
   }

   /**
    * Remove all columns.
    *
    * <p>This method should be used carefully, as numerous other map objects
    * point to Columns. Those objects should be deleted before this method is called.</p>
    */
   public void removeAllColumns()
   {
      columns.clear();
      rsColumns = null;
   }

   // ********************************************************************
   // Primary key
   // ********************************************************************

   /**
    * Get the primary key.
    *
    * @return The primary key. Null if the primary key does not exist.
    */
   public final Key getPrimaryKey()
   {
      return primaryKey;
   }

   /**
    * Create a primary key and add it to the table.
    *
    * <p>If the primary key already exists, returns the existing primary key.</p>
    *
    * @param keyName Name of the key. Used in map documents and CREATE TABLE
    *    statements. If this is null, "PrimaryKey" is used.
    *
    * @return The primary key.
    */
   public Key createPrimaryKey(String keyName)
   {
      if (keyName == null)
      {
         keyName = PRIMARYKEY;
      }
      if (primaryKey == null)
      {
         primaryKey = Key.createPrimaryKey(keyName);
      }
      else if (!primaryKey.getName().equals(keyName))
      {
         throw new IllegalArgumentException("The primary key already exists, but has a different name. To change the name of the primary key, delete it and create a new primary key with the new name.");
      }
      return primaryKey;
   }

   /**
    * Add a primary key to the table.
    *
    * @param key The primary key.
    * @exception XMLMiddlewareException Thrown if the primary key already exists.
    */
   public void addPrimaryKey(Key key)
      throws XMLMiddlewareException
   {
      if (primaryKey != null)
         throw new XMLMiddlewareException("Primary key already exists.");
      checkArgNull(key, ARG_KEY);
      if (key.getType() != Key.PRIMARY_KEY)
         throw new IllegalArgumentException("Key is not a primary key.");
      primaryKey = key;
   }

   /**
    * Remove the primary key.
    *
    * @exception XMLMiddlewareException Thrown if a primary key does not exist.
    */
   public void removePrimaryKey()
      throws XMLMiddlewareException
   {
      if (primaryKey == null)
         throw new XMLMiddlewareException("Primary key does not exist.");
      primaryKey = null;
   }

   // ********************************************************************
   // Unique keys
   // ********************************************************************

   /**
    * Get a unique key (except the primary key).
    *
    * @param keyName Name of the key.
    *
    * @return The unique key. Null if the key is not found.
    */
   public final Key getUniqueKey(String keyName)
   {
      return getKey(uniqueKeys, keyName);
   }

   /**
    * Get all unique keys (except the primary key).
    *
    * @return An Enumeration of all unique keys. May be empty.
    */
   public final Enumeration getUniqueKeys()
   {
      return uniqueKeys.elements();
   }

   /**
    * Create a unique key (except the primary key) and add it to the table.
    *
    * <p>If the key already exists, returns the existing key.</p>
    *
    * @param keyName Name of the key.
    *
    * @return The unique key.
    */
   public Key createUniqueKey(String keyName)
   {
      return createKey(uniqueKeys, keyName, Key.UNIQUE_KEY);
   }

   /**
    * Add a unique key (except the primary key) to the table.
    *
    * @param key The unique key.
    * @exception XMLMiddlewareException Thrown if the unique key already exists.
    */
   public void addUniqueKey(Key key)
      throws XMLMiddlewareException
   {
      addKey(uniqueKeys, key, Key.UNIQUE_KEY);
   }

   /**
    * Remove a unique key (except the primary key).
    *
    * @param keyName Name of the key.
    *
    * @exception XMLMiddlewareException Thrown if the unique key does not exist.
    */
   public void removeUniqueKey(String keyName)
      throws XMLMiddlewareException
   {
      removeKey(uniqueKeys, keyName);
   }

   /**
    * Remove all unique keys (except the primary key).
    */
   public void removeAllUniqueKeys()
   {
      uniqueKeys.clear();
   }

   // ********************************************************************
   // Foreign keys
   // ********************************************************************

   /**
    * Get a foreign key.
    *
    * @param keyName Name of the key.
    *
    * @return The foreign key. Null if the key is not found.
    */
   public final Key getForeignKey(String keyName)
   {
      return getKey(foreignKeys, keyName);
   }

   /**
    * Get all foreign keys.
    *
    * @return An Enumeration of all foreign keys. May be empty.
    */
   public final Enumeration getForeignKeys()
   {
      return foreignKeys.elements();
   }

   /**
    * Create a foreign key and add it to the table.
    *
    * <p>If the key already exists, returns the existing key.</p>
    *
    * @param keyName Name of the key.
    *
    * @return The foreign key.
    */
   public Key createForeignKey(String keyName)
   {
      return createKey(foreignKeys, keyName, Key.FOREIGN_KEY);
   }

   /**
    * Add a foreign key to the table.
    *
    * @param key The foreign key.
    * @exception XMLMiddlewareException Thrown if the foreign key already exists.
    */
   public void addForeignKey(Key key)
      throws XMLMiddlewareException
   {
      addKey(foreignKeys, key, Key.FOREIGN_KEY);
   }

   /**
    * Remove a foreign key.
    *
    * @param keyName Name of the key.
    *
    * @exception XMLMiddlewareException Thrown if the foreign key does not exist.
    */
   public void removeForeignKey(String keyName)
      throws XMLMiddlewareException
   {
      removeKey(foreignKeys, keyName);
   }

   /**
    * Remove all foreign keys.
    */
   public void removeAllForeignKeys()
   {
      foreignKeys.clear();
   }

   // ********************************************************************
   // Column metadata
   // ********************************************************************

   /**
    * Checks whether metadata has been set for the columns in the table.
    *
    * @return The first Column for which metadata has not been set or
    *    null if metadata has been set for all columns.
    */
   public Column checkMetadata()
   {
      Enumeration columns;
      Column      column;

      columns = getColumns();
      while (columns.hasMoreElements())
      {
         column = (Column)columns.nextElement();
         if (!column.isMetadataSet()) return column;
      }
      return null;
   }

   // ********************************************************************
   // Private methods
   // ********************************************************************

   private final Key getKey(Hashtable hash, String keyName)
   {
      checkArgNull(keyName, ARG_KEYNAME);
      return (Key)hash.get(keyName);
   }

   private Key createKey(Hashtable hash, String keyName, int type)
   {
      Key key;

      checkArgNull(keyName, ARG_KEYNAME);
      key = (Key)hash.get(keyName);
      if (key == null)
      {
         if (type == Key.UNIQUE_KEY)
         {
            key = Key.createUniqueKey(keyName);
         }
         else if (type == Key.FOREIGN_KEY)
         {
            key = Key.createForeignKey(keyName);
         }
         hash.put(keyName, key);
      }
      return key;
   }

   private void addKey(Hashtable hash, Key key, int type)
      throws XMLMiddlewareException
   {
      Object o;
      String name;

      checkArgNull(key, ARG_KEY);
      if (key.getType() != type)
         throw new IllegalArgumentException("Key is not the correct type.");
      name = key.getName();
      o = hash.get(name);
      if (o != null)
         throw new XMLMiddlewareException("Key " + name + " already exists in " + getUniversalName() + ".");
      hash.put(name, key);
   }

   private void removeKey(Hashtable hash, String keyName)
      throws XMLMiddlewareException
   {
      Object o;

      checkArgNull(keyName, ARG_KEYNAME);

      o = hash.remove(keyName);
      if (o == null)
         throw new XMLMiddlewareException("Key " + keyName + " not found in table " + getUniversalName() + ".");
   }
}
