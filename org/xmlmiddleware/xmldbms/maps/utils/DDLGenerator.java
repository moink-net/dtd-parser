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
// Changes from version 1.01: New in version 2.0

package org.xmlmiddleware.xmldbms.maps.utils;

import org.xmlmiddleware.db.JDBCTypes;
import org.xmlmiddleware.utils.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import java.util.*;

/**
 * Generate CREATE TABLE strings.
 *
 * <p>This class generates CREATE TABLE strings that use the following subset of
 * the SQL '92 CREATE TABLE syntax:</p>
 *
 * <pre>
 *    CREATE TABLE table (column type [NOT] NULL [, column type [NOT] NULL...]
 *       [, CONSTRAINT name PRIMARY KEY (column [, column...])]
 *       [, CONSTRAINT name UNIQUE (column [, column...])]
 *       [, CONSTRAINT name FOREIGN KEY (column [, column...])]
 *             REFERENCES table (column [, column...])]
 * </pre>
 *
 * <p>If a DatabaseMetaData object is passed to the constructor, table and column
 * names are checked against the database. (For details, see DBNameChecker.) In addition,
 * database-specific data type names are used. All table and column names use
 * quoted (delimited) identifiers.</p>
 *
 * @author Ronald Bourret, 2002
 * @version 2.0
 */

public class DDLGenerator
{

   //**************************************************************************
   // Class variables
   //**************************************************************************

   private Hashtable        dbInfos;
   private DBInfo           dbInfo;

   //**************************************************************************
   // Constants
   //**************************************************************************

   private final static String DEFAULT        = "Default";
   private final static String CREATETABLE    = "CREATE TABLE";
   private final static String NULL           = "NULL";
   private final static String NOTNULL        = "NOT NULL";
   private final static String CONSTRAINT     = "CONSTRAINT";
   private final static String PRIMARYKEY     = "PRIMARY KEY";
   private final static String UNIQUE         = "UNIQUE";
   private final static String FOREIGNKEY     = "FOREIGN KEY";
   private final static String REFERENCES     = "REFERENCES";
   private final static String LENGTH         = "LENGTH";
   private final static String PRECISION      = "PRECISION";
   private final static String SCALE          = "SCALE";
   private final static String PRECISIONSCALE = "PRECISION, SCALE";


   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new DDLGenerator.
    *
    */
   public DDLGenerator()
   {
      dbInfos = null;
      dbInfo = new DBInfo();
      initDatabaseMetadata();
      initDataTypeMetadata();
   }

   /**
    * Construct a new DDLGenerator.
    *
    * @param databaseName The name of the database to which the DatabaseMetaData
    *    object applies. If this is null, "Default" is used.
    * @param meta A DatabaseMetaData object.
    * @exception SQLException Thrown if an error occurs retrieving database metadata.
    */
   public DDLGenerator(String databaseName, DatabaseMetaData meta)
      throws SQLException
   {
      // Use "Default" if no database name is specified.

      if (databaseName == null) databaseName = DEFAULT;

      // Create a new DBInfo object.

      dbInfo = new DBInfo();
      dbInfos = new Hashtable();
      dbInfos.put(databaseName, dbInfo);

      // Initialize the database metadata

      initDatabaseMetadata(databaseName, meta);
      initDataTypeMetadata(databaseName, meta);
   }

   /**
    * Construct a new DDLGenerator.
    *
    * @param databaseNames The names of the databases to which the DatabaseMetaData
    *    objects apply.
    * @param metas DatabaseMetaData objects.
    * @exception SQLException Thrown if an error occurs retrieving database metadata.
    */
   public DDLGenerator(String[] databaseNames, DatabaseMetaData[] metas)
      throws SQLException
   {
      dbInfos = new Hashtable();

      for (int i = 0; i < databaseNames.length; i++)
      {
         if (dbInfos.get(databaseNames[i]) != null)
            throw new IllegalArgumentException("Database name used more than once: " + databaseNames[i]);

         // Create a new DBInfo object.

         dbInfo = new DBInfo();
         dbInfos.put(databaseNames[i], dbInfo);

         // Initialize the database metadata

         initDatabaseMetadata(databaseNames[i], metas[i]);
         initDataTypeMetadata(databaseNames[i], metas[i]);
      }
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Gets CREATE TABLE strings for all tables in the map.
    *
    * @param map The map
    * @return A Vector of CREATE TABLE strings
    */
   public Vector getCreateTableStrings(XMLDBMSMap map)
   {
      Enumeration  tables;
      Table        table;
      Vector       strings = new Vector();

      tables = map.getTables();
      while (tables.hasMoreElements())
      {
         table = (Table)tables.nextElement();
         strings.addElement(getCreateTableString(table));
      }
      return strings;
   }

   /**
    * Gets the CREATE TABLE string for a specific table.
    *
    * @param table The table
    * @return The CREATE TABLE string
    */
   public String getCreateTableString(Table table)
   {
      StringBuffer sb = new StringBuffer();
      boolean      needComma;
      Enumeration  columns, keys;
      Column       column;
      Key          key;

      // Get the correct metadata for the database. Note that if no DatabaseMetaData
      // objects were passed in, we use the default metadata.

      if (dbInfos != null)
      {
         dbInfo = (DBInfo)dbInfos.get(table.getDatabaseName());
         if (dbInfo == null)
            throw new IllegalArgumentException("No DatabaseMetaData object specified for the " + table.getDatabaseName() + " database.");
      }

      // Start the CREATE TABLE statement

      sb.append(CREATETABLE);
      sb.append(' ');
      sb.append(getTableName(table));
      sb.append(' ');
      sb.append('(');

      // Add the column definitions

      needComma = false;
      columns = table.getColumns();
      while (columns.hasMoreElements())
      {
         column = (Column)columns.nextElement();
         sb.append(getColumnName(column, needComma));
         needComma = true;
         sb.append(' ');
         sb.append(getDataType(column));
         sb.append(' ');
         sb.append(getNull(column));
      }

      // Add the primary key definition, if any.

      key = table.getPrimaryKey();
      if (key != null)
      {
         sb.append(getKeyConstraint(key));
      }

      // Add the unique (non-primary) key definitions, if any.

      keys = table.getUniqueKeys();
      while (keys.hasMoreElements())
      {
         key = (Key)keys.nextElement();
         sb.append(getKeyConstraint(key));
      }

      // Add the foreign key definitions, if any.

      keys = table.getForeignKeys();
      while (keys.hasMoreElements())
      {
         key = (Key)keys.nextElement();
         sb.append(getKeyConstraint(key));
      }

      // End the statement

      sb.append(')');

      // Return the string

      return sb.toString();
   }

   //**************************************************************************
   // Private methods -- string building
   //**************************************************************************

   private String getTableName(Table table)
   {
      String       catalog = null, schema;
      StringBuffer sb = new StringBuffer();

      // 6/9/00, Ruben Lainez, Ronald Bourret
      // Use the identifier quote character for the table name.

      // If the catalog name exists, is used, and is at the start of the qualified
      // name, add it now.

      if (dbInfo.useCatalog)
      {
         catalog = table.getCatalogName();
         if ((catalog != null) && (dbInfo.isCatalogAtStart))
         {
            sb.append(getQuotedName(catalog));
            sb.append(dbInfo.catalogSeparator);
         }
      }
 
      // If the schema name exists and is used, add it now.

      if (dbInfo.useSchema)
      {
         schema = table.getSchemaName();
         if(schema != null)
         {
            sb.append(getQuotedName(schema));
            sb.append('.');
         }
      }

      // Add the table name.
 
      sb.append(getQuotedName(table.getTableName()));
 
      // If the catalog name exists, is used, and is at the end of the qualified
      // name, add it now.

      if (dbInfo.useCatalog)
      {
         if ((catalog != null) && (!dbInfo.isCatalogAtStart))
         {
            sb.append(dbInfo.catalogSeparator);
            sb.append(getQuotedName(catalog));
         }
      }

      // Return the qualified name.

      return sb.toString();
   }

   private String getColumnName(Column column, boolean comma)
   {
      StringBuffer sb = new StringBuffer();

      // Add the column name, preceding it with a comma if necessary.

      if (comma)
      {
         sb.append(',');
         sb.append(' ');
      }
      sb.append(getQuotedName(column.getName()));
      return sb.toString();
   }


   private String getQuotedName(String name)
   {
      StringBuffer sb = new StringBuffer();

      // Create a quoted name.

      sb.append(dbInfo.quote);
      sb.append(name);
      sb.append(dbInfo.quote);
      return sb.toString();
   }

   private String getDataType(Column column)
   {
      int          type, value = 0;
      StringBuffer sb = new StringBuffer();
      String       params;
      long[]       positions = new long[3];
      String[]     paramTypes = {LENGTH, PRECISION, SCALE};
      boolean      exists, firstParam;

      // Get the type and the type name.

      type = column.getType();
      sb.append((String)dbInfo.typeNames.get(new Integer(type)));

      // Get the parameters for the type, such as length in VARCHAR(length)

      params = (String)dbInfo.createParams.get(new Integer(type));
      if (params != null)
      {
         // Parse the parameters and find out which ones are present.

         positions[0] = (long)params.indexOf(LENGTH);
         positions[1] = (long)params.indexOf(PRECISION);
         positions[2] = (long)params.indexOf(SCALE);

         // Sort the parameters.

         Sort.sort(positions, paramTypes);

         // Process the parameters

         firstParam = true;
         for (int i = 0; i < positions.length; i++)
         {
            // If the parameter is not found, skip it.

            if (positions[i] == -1) continue;

            // Check if a value has been specified for the parameter. If so, get that value.

            if (paramTypes[i].equals(LENGTH))
            {
               exists = column.lengthExists();
               if (exists)
               {
                  value = column.getLength();
               }
            }
            else if (paramTypes[i].equals(PRECISION))
            {
               exists = column.precisionExists();
               if (exists)
               {
                  value = column.getPrecision();
               }
            }
            else // if (paramTypes[i].equals(SCALE))
            {
               exists = column.scaleExists();
               if (exists)
               {
                  value = column.getScale();
               }
            }

            // If no value has been specified, skip the parameter. This occurs when
            // the parameter is optional and when the map contains incomplete information.

            if (!exists) continue;

            // Open the parameter clause or append a comma to separate parameters.

            if (firstParam)
            {
               sb.append('(');
               firstParam = false;
            }
            else
            {
               sb.append(',');
               sb.append(' ');
            }

            // Append the parameter value.

            sb.append(String.valueOf(value));
         }

         // If any parameters were used, close the parameter clause.

         if (!firstParam)
         {
            sb.append(')');
         }
      }

      // Return the data type string.

      return sb.toString();
   }

   private String getNull(Column column)
   {
      int nullability = column.getNullability();

      // Build a [NOT] NULL clause.

      switch (column.getNullability())
      {
         case DatabaseMetaData.columnNullable:
            return NULL;

         case DatabaseMetaData.columnNoNulls:
            return NOTNULL;

         default:
            return null;
      }
   }

   private String getKeyConstraint(Key key)
   {
      StringBuffer sb = new StringBuffer();
      int          type;

      // Set the constraint name

      sb.append(',');
      sb.append(' ');
      sb.append(CONSTRAINT);
      sb.append(' ');
      sb.append(key.getName());
      sb.append(' ');

      // Append the key type keyword

      type = key.getType();
      switch (type)
      {
         case Key.PRIMARY_KEY:
            sb.append(PRIMARYKEY);
            break;

         case Key.UNIQUE_KEY:
            sb.append(UNIQUE);
            break;

         case Key.FOREIGN_KEY:
            sb.append(FOREIGNKEY);
            break;
      }

      // List the columns

      sb.append(' ');
      sb.append('(');
      sb.append(getKeyColumns(key));
      sb.append(')');

      // Add a REFERENCES clause for foreign keys

      if (type == Key.FOREIGN_KEY)
      {
         sb.append(' ');
         sb.append(REFERENCES);
         sb.append(' ');
         sb.append(getTableName(key.getRemoteTable()));
         sb.append(' ');
         sb.append('(');
         sb.append(getKeyColumns(key.getRemoteKey()));
         sb.append(')');
      }

      // Return the CONSTRAINT

      return sb.toString();
   }

   private String getKeyColumns(Key key)
   {
      StringBuffer sb = new StringBuffer();
      boolean      needComma = false;
      Column[]     columns;

      columns = key.getColumns();
      for (int i = 0; i < columns.length; i++)
      {
         sb.append(getColumnName(columns[i], needComma));
         needComma = true;
      }
      return sb.toString();
   }

   //**************************************************************************
   // Private methods -- initialization
   //**************************************************************************

   private void initDatabaseMetadata()
   {
      dbInfo.quote = "\"";
      dbInfo.isCatalogAtStart = true;
      dbInfo.catalogSeparator = ".";
      dbInfo.useCatalog = true;
      dbInfo.useSchema = true;
   }

   private void initDatabaseMetadata(String databaseName, DatabaseMetaData meta)
      throws SQLException
   {
      dbInfo.quote = meta.getIdentifierQuoteString();
      if (dbInfo.quote == null) dbInfo.quote = "";

      // Find out whether the database supports catalogs and schemas, and get the
      // related information. The JDBC spec is vague about what drivers should do
      // when the underlying database doesn't support catalogs or schemas, so we
      // wrap these calls in try/catch clauses. Note that we don't know what exception
      // will be thrown, so we simply catch Exception and hope that the driver is
      // not stupid enough to access the database here, which could result in
      // legitimate exceptions.

      try
      {
         dbInfo.useCatalog = meta.supportsCatalogsInTableDefinitions();
         if (dbInfo.useCatalog)
         {
            dbInfo.isCatalogAtStart = meta.isCatalogAtStart();
            dbInfo.catalogSeparator = meta.getCatalogSeparator();
            if (dbInfo.catalogSeparator == null) dbInfo.catalogSeparator = ".";
            if (dbInfo.catalogSeparator.length() == 0) dbInfo.catalogSeparator = ".";
         }
      }
      catch (Exception e)
      {
         dbInfo.useCatalog = false;
      }
      try
      {
         dbInfo.useSchema = meta.supportsSchemasInTableDefinitions();
      }
      catch (Exception e)
      {
         dbInfo.useSchema = false;
      }
   }

   private void initDataTypeMetadata()
   {
      Integer type;
      String  params;

      // Use the default names and create parameters.

      for (int i = 0; i < JDBCTypes.JDBCTYPE_TOKENS.length; i++)
      {
         // Set the type name.

         type = new Integer(JDBCTypes.JDBCTYPE_TOKENS[i]);
         dbInfo.typeNames.put(type, JDBCTypes.getName(JDBCTypes.JDBCTYPE_TOKENS[i]));

         // Set the create parameters, if any.

         params = getDefaultParams(JDBCTypes.JDBCTYPE_TOKENS[i]);
         if (params != null)
         {
            dbInfo.createParams.put(type, params);
         }
      }
   }

   private void initDataTypeMetadata(String databaseName, DatabaseMetaData meta)
      throws SQLException
   {
      ResultSet rs;
      String    typeName, params;
      Integer   type;

      // Get the result set of data type information.

      rs = meta.getTypeInfo();
      while (rs.next())
      {
         // Get the type name, data type indicator, and create parameters.

         typeName = rs.getString(1);
         type = new Integer(rs.getShort(2));
         params = rs.getString(6);
         if (rs.wasNull()) params = null;

         // Store the type name and create parameters. Note that we check first to see
         // if the type indicator is already used. This is because the database might
         // map multiple data types to the same indicator. Although the JDBC spec is
         // (annoyingly, but not surprisingly) silent on this case, the ODBC spec says
         // that types with the same indicator are to be returned in the order from
         // those that most closely match the SQL type to those that least closely
         // match it. Therefore, for a given indicator, we use the type returned first.

         if (dbInfo.typeNames.get(type) == null)
         {
            dbInfo.typeNames.put(type, typeName);
            if (params != null)
            {
               dbInfo.createParams.put(type, params.toUpperCase());
            }
         }
      }

      // Close the result set.

      rs.close();

      // There's no guarantee that a given database supports all types, so we
      // need to fill in any unsupported types with default names and create
      // parameters. Granted, this means that the resulting CREATE TABLE statement
      // will have unsupported types in it, but a compliant database will convert
      // these to its own types. For example, Oracle converts INTEGER to a fixed
      // width type -- NUMERIC(5, 0), if I remember correctly.

      for (int i = 0; i < JDBCTypes.JDBCTYPE_TOKENS.length; i++)
      {
         // Check if the type is supported.

         type = new Integer(JDBCTypes.JDBCTYPE_TOKENS[i]);
         if (dbInfo.typeNames.get(type) == null)
         {
            // If not, set the type name...

            dbInfo.typeNames.put(type, JDBCTypes.getName(JDBCTypes.JDBCTYPE_TOKENS[i]));

            // ... and the create parameters, if any.

            params = getDefaultParams(JDBCTypes.JDBCTYPE_TOKENS[i]);
            if (params != null)
            {
               dbInfo.createParams.put(type, params);
            }
         }
      }
   }

   private String getDefaultParams(int type)
   {
      switch (type)
      {
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            return LENGTH;

         case Types.DECIMAL:
         case Types.NUMERIC:
            return PRECISIONSCALE;

         case Types.FLOAT:
            return PRECISION;

         default:
            return null;
      }
   }

   //**************************************************************************
   // Inner class
   //**************************************************************************

   private class DBInfo
   {
      String    quote = null;
      String    catalogSeparator = null;
      boolean   isCatalogAtStart = true;
      boolean   useCatalog = true;
      boolean   useSchema = true;
      Hashtable typeNames = new Hashtable();
      Hashtable createParams = new Hashtable();
   }
}
