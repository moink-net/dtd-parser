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
// Changes from version 1.01: New in version 2.0.

package org.xmlmiddleware.xmldbms.maps.factories;

import org.xmlmiddleware.utils.*;

import java.sql.*;
import java.util.*;

/**
 * Checks if names conform to the rules of a particular database.
 *
 * <p>DBNameChecker checks that table and column names conform to the naming rules
 * of a particular database. It also checks that they do not collide with any table
 * or column names currently in the database or created in the current session. Names
 * are modified as follows:</p>
 *
 * <ol>
 * <li>Any characters not supported by the database are discarded.</li>
 * <li>The name is truncated to the maximum length allowed by the database.</li>
 * <li>The case is changed to the case used by the database.</li>
 * <li>Table names are checked against table names currently in the database, as
 *     well as table names that have been created in the current session. Column
 *     names are checked against the names in the current table. If any collisions
 *     are found, a number (starting with 1) is appended to the name and the
 *     result is re-checked until a non-colliding name is found.</li>
 * </ol>
 *
 * <p>If no database is specified, the legal character set is assumed to be a-z,
 * A-Z, 0-9, and _; the maximum length is assumed to be 30, the case used by
 * the database is assumed to be upper case, and the maximum number of columns
 * in a table is assumed to be 100.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class DBNameChecker
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private DatabaseMetaData meta;
   private String           extraChars;
   private int              maxColumnNameLen,
                            maxColumnsInTable,
                            maxTableNameLen;
   private boolean          mixedCase, lowerCase, upperCase, useCatalogs, useSchemas;
   private Hashtable        catalogNames = new Hashtable();
   private char[]           escape;

   //**************************************************************************
   // Constants
   //**************************************************************************

   private static final String str = new String();

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new DBNameChecker.
    */
   public DBNameChecker()
   {
      initialize();
   }

   /**
    * Construct a new DBNameChecker and set the database connection.
    *
    * @param conn The Connection
    * @exception SQLException Thrown if an error occurs initializing the database
    *    metadata.
    */
   public DBNameChecker(Connection conn)
      throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn argument must not be null.");
      initialize(conn);
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Sets the database connection.
    *
    * @param conn The Connection. May be null.
    * @exception SQLException Thrown if an error occurs initializing the database
    *    metadata.
    */
   public void setConnection(Connection conn)
      throws SQLException
   {
      if (conn == null)
      {
         initialize();
      }
      else
      {
         initialize(conn);
      }
   }

   /**
    * Start a new name-checking session.
    *
    * <p>This method removes all names from the lists of table and column names
    * created during the previous session. Thus, names are checked for collisions
    * only against names created after this call and before the next call to
    * startNewSession().</p>
    */
   public void startNewSession()
   {
      catalogNames.clear();
   }

   /**
    * Checks a table name.
    *
    * <p>If necessary, this method modifies the input table name to meet the naming criteria
    * of the current database.</p>
    *
    * @param catalogName Name of the catalog in which to check for collisions. May be null.
    * @param schemaName Name of the schema in which to check for collisions. May be null.
    * @param tableName Table name to check.
    * @return The modified table name.
    * @exception SQLException Thrown if an error occurs accessing the database.
    * @exception XMLMiddlewareException Thrown if an error occurs constructing a name.
    */
   public String checkTableName(String catalogName, String schemaName,
String tableName)
      throws SQLException, XMLMiddlewareException
   {
      // Constructs a table name that is legal and doesn't collide with
      // any table names that have been constructed in this session or
      // are already in the database.

      String    name;
      Hashtable tableNames;

      if (tableName == null)
         throw new IllegalArgumentException("tableName argument must not be null.");

      // Check the characters, length, and case of the table name.

      name = tableName;
      name = checkCharacters(name);
      name = checkLength(name, maxTableNameLen);
      name = checkCase(name);

      // Get the Hashtable of table names that have been checked for this catalog
      // and schema, then check if the new table name collides against any of these
      // names or any names in the database.

      tableNames = getTableNames(catalogName, schemaName);
      name = checkTableNameCollisions(catalogName, schemaName, name, tableNames);

      // Return the table name.

      return name;
   }

   /**
    * Checks a column name.
    *
    * <p>If necessary, this method modifies the input column name to meet the naming criteria
    * of the current database.</p>
    *
    * @param catalogName Name of the catalog in which to check for collisions. May be null.
    * @param schemaName Name of the schema in which to check for collisions. May be null.
    * @param tableName Name of the table in which to check for collisions. The table name must
    *    have been used in a call to checkTableName in this session.
    * @param columnName Column name to check.
    * @return The modified column name.
    * @exception XMLMiddlewareException Thrown if an error occurs constructing a name.
    */
   public String checkColumnName(String catalogName, String schemaName, String tableName, String columnName)
      throws XMLMiddlewareException
   {
      // Constructs a column name that is legal and doesn't collide with
      // any column names that have been constructed for this table.

      String    name;
      Hashtable tableNames, columnNames;

      if (tableName == null)
         throw new IllegalArgumentException("tableName argument must not be null.");
      if (columnName == null)
         throw new IllegalArgumentException("columnName argument must not be null.");

      // Verify that the table name has already been checked in this session.

      tableNames = getTableNames(catalogName, schemaName);
      columnNames = (Hashtable)tableNames.get(tableName);
      if (columnNames == null)
         throw new IllegalArgumentException("tableName has not been used in a call to checkTableName in this session.");

      // Check the characters, length, and case of the table name, then check that
      // it doesn't collide with any column names already in the table.

      name = columnName;
      name = checkCharacters(name);
      name = checkLength(name, maxColumnNameLen);
      name = checkCase(name);
      name = checkColumnNameCollisions(name, columnNames);

      // Return the column name.

      return name;
   }

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void initialize()
   {
      meta = null;
      extraChars = new String();
      maxColumnNameLen = 30;
      maxColumnsInTable = 32;
      maxTableNameLen = 30;
      mixedCase = false;
      lowerCase = false;
      upperCase = true;
      escape = new char[0];
      useCatalogs = false;
      useSchemas = false;
   }

   private void initialize(Connection conn)
      throws SQLException
   {
      meta = conn.getMetaData();
      extraChars = meta.getExtraNameCharacters();
      maxColumnNameLen = meta.getMaxColumnNameLength();
      maxColumnsInTable = meta.getMaxColumnsInTable();
      maxTableNameLen = meta.getMaxTableNameLength();
      mixedCase = meta.supportsMixedCaseQuotedIdentifiers() ||
                  meta.storesMixedCaseQuotedIdentifiers();
      lowerCase = meta.storesLowerCaseQuotedIdentifiers();
      upperCase = meta.storesUpperCaseQuotedIdentifiers();
      escape = meta.getSearchStringEscape().toCharArray();
      useCatalogs = meta.supportsCatalogsInDataManipulation();
      useSchemas = meta.supportsSchemasInDataManipulation();
   }

   private Hashtable getTableNames(String catalogName, String schemaName)
   {
      Hashtable schemaNames, tableNames;

      // Use a static empty string to represent a null catalog or schema name.

      if (catalogName == null) catalogName = str;
      if (schemaName == null) schemaName = str;

      // Get the Hashtable of schema names for this catalog. If it doesn't exist,
      // create it now.

      schemaNames = (Hashtable)catalogNames.get(catalogName);
      if (schemaNames == null)
      {
         schemaNames = new Hashtable();
         catalogNames.put(catalogName, schemaNames);
      }

      // Get the Hashtable of table names for this schema. If it doesn't exist,
      // create it now.

      tableNames = (Hashtable)schemaNames.get(schemaName);
      if (tableNames == null)
      {
         tableNames = new Hashtable();
         schemaNames.put(schemaName, tableNames);
      }

      // Return the Hashtable of table names.

      return tableNames;
   }

   private String checkCharacters(String name)
   {
      char[] oldName, newName;
      int    position = 0;
      char   character;

      oldName = name.toCharArray();
      newName = new char[oldName.length];

      for (int i = 0; i < oldName.length; i++)
      {
         // If a character in the name is in the range 0-9, a-z, or A-Z,
         // if the character is '_', or if the character is a legal "extra"
         // character, transfer it to the new name. Otherwise, remove the
         // character from the name.

         character = oldName[i];

         if (((character >= '0') && (character <= '9')) ||
             ((character >= 'a') && (character <= 'z')) ||
             ((character >= 'A') && (character <= 'Z')) ||
             (character == '_'))
         {
            newName[position++] = character;
         }
         else if (extraChars.indexOf((int)character) != -1)
         {
            newName[position++] = character;
         }
      }
      return new String(newName, 0, position);
   }

   private String checkLength(String name, int maxLength)
   {
      // Truncate the string to maxLength characters.

      if (name.length() <= maxLength)
      {
         return name;
      }
      else
      {
         return name.substring(0, maxLength);
      }
   }

   private String checkCase(String name)
   {
      // Change the case to the case used by the database

      if (mixedCase)
      {
         return name;
      }
      else if (lowerCase)
      {
         return name.toLowerCase();
      }
      else // if (upperCase)
      {
         return name.toUpperCase();
      }
   }

   private String checkTableNameCollisions(String catalogName, String schemaName, String inputTableName, Hashtable tableNames)
      throws SQLException, XMLMiddlewareException
   {
      String tableName, sessionUniqueName, dbUniqueName;
      int    suffixNum = 1;

      tableName = inputTableName;
      sessionUniqueName = null;
      dbUniqueName = null;

      // We need to create a name that is unique in both the session and the
      // database. We set sessionUniqueName when we have a name that is unique
      // in the session. We set dbUniqueName when we have a name that is unique
      // in the database. We keep our current value in tableName. We are done
      // when tableName equals both sessionUniqueName and dbUniqueName.

      while (!tableName.equals(sessionUniqueName))
      {
         // Get a table name that doesn't collide with any names
         // in the hashtable of new table names. Save this so we
         // can check that tableName doesn't change while we are
         // testing it against the database.

         while (tableNames.get(tableName) != null)
         {
            tableName = getSuffixedName(inputTableName, suffixNum, maxTableNameLen);
            suffixNum++;
         }
         sessionUniqueName = tableName;

         // Get a table name that doesn't collide with any names in
         // the database. Save this so we can check that tableName
         // doesn't change while we are testing it against the
         // session table names.

         if (!tableName.equals(dbUniqueName))
         {
            while (tableNameInDB(catalogName, schemaName, tableName))
            {
               tableName = getSuffixedName(inputTableName, suffixNum, maxTableNameLen);
               suffixNum++;
            }
            dbUniqueName = tableName;
         }
      }

      // Now that we know we have a unique table name, add it to the list of table
      // names we have created in this session and create a new Hashtable to hold
      // the names of the columns in the table.

      tableNames.put(tableName, new Hashtable());
      return tableName;
   }

   private String checkColumnNameCollisions(String inputColumnName, Hashtable columnNames)
      throws XMLMiddlewareException
   {
      String columnName = inputColumnName;
      int    suffixNum = 1;

      // Check if the column name is in the list of new column names.
      // If it is, append a number and check again.

      while (columnNames.get(columnName) != null)
      {
         columnName = getSuffixedName(inputColumnName, suffixNum, maxColumnNameLen);
         suffixNum++;
      }

      // When we have a unique column name, store an object in the columnNames hash
      // table to guard the name against future collisions.

      columnNames.put(columnName, str);
      return columnName;
   }

   private boolean tableNameInDB(String catalogName, String schemaName, String tableName)
      throws SQLException
   {
      // Check if a table name is in the database

      ResultSet rs;
      boolean   tableFound;

      // If no database connection is set, just return false.

      if (meta == null) return false;

      // Check if we even use the catalog and schema names. Search the schema 
      // and table name for JDBC wild card characters and escape them.

      if (!useCatalogs)
      {
         catalogName = null;
      }
      if (!useSchemas)
      {
         schemaName = null;
      }
      else
      {
         schemaName = escapeDBName(schemaName);
      }
      tableName = escapeDBName(tableName);

      // Get the row for the specified catalog, schema, and table, if any.
      // If the result set contains any rows, then the table is already
      // in the database.

      rs = meta.getTables(catalogName, schemaName, tableName, null);
      tableFound = rs.next();
      rs.close();
      return tableFound;
   }

   private String escapeDBName(String name)
   {
      char[] src, dest;
      int    len = 0;

      // Allocate the src and dest arrays. Note that the dest array is
      // allocated for the maximum size -- as if every character needed
      // to be escaped.

      src = name.toCharArray();
      dest = new char[name.length() * (escape.length + 1)];

      // Copy characters from the old name to the new name, escaping
      // them as necessary.

      for (int i = 0; i < name.length(); i++)
      {
         if ((src[i] == '_') || (src[i] == '%'))
         {
            // If the character used in the name is a JDBC wildcard
            // character, escape it.

            for (int j = 0; j < escape.length; j++)
            {
               dest[len] = escape[j];
               len++;
            }
         }

         // Copy the character.

         dest[len] = src[i];
         len++;
      }

      // Return the new string.

      return new String(dest, 0, len);
   }

   private String getSuffixedName(String inputName, int suffixNum, int maxLength)
      throws XMLMiddlewareException
   {
      String baseName, suffix;
      int    newLength;

      // Get a new suffix

      baseName = inputName;
      suffix = String.valueOf(suffixNum);

      // If the suffix is too long, truncate the base name. If the
      // new base name is zero length or less, throw an exception.

      if ((baseName.length() + suffix.length()) > maxLength)
      {
         newLength = maxLength - (baseName.length() + suffix.length() - maxLength);
         if (newLength <= 0)
            throw new XMLMiddlewareException("Cannot construct a unique name from " + baseName);
         baseName = baseName.substring(0, newLength);
      }
      return baseName + suffix;
   }
}
