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

   private String m_quote;
   private String m_catalogSeparator;
   private boolean m_isCatalogAtStart;
   private boolean m_useCatalog;
   private boolean m_useSchema;
   private Hashtable typeNames = new Hashtable();
   private Hashtable createParams = new Hashtable();

   //**************************************************************************
   // Constants
   //**************************************************************************

   private final static String CREATETABLE    = "CREATE TABLE";
   private final static String NULL           = "NULL";
   private final static String NOTNULL        = "NOT NULL";
   private final static String CONSTRAINT     = "CONSTRAINT";
   private final static String PRIMARYKEY     = "PRIMARY KEY";
   private final static String UNIQUE         = "UNIQUE";
   private final static String FOREIGNKEY     = "FOREIGN KEY";
   private final static String REFERENCES     = "REFERENCES";
   private final static String LENGTH         = "length";
   private final static String PRECISION      = "precision";
   private final static String SCALE          = "scale";
   private final static String PRECISIONSCALE = "precision, scale";


   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new DDLGenerator.
    *
    */
   public DDLGenerator()
   {
      initDatabaseMetadata();
      initDataTypeMetadata();
   }

   /**
    * Construct a new DDLGenerator.
    *
    * @param meta A DatabaseMetaData object.
    * @exception SQLException Thrown if an error occurs retrieving database metadata.
    */
   public DDLGenerator(DatabaseMetaData meta)
      throws SQLException
   {
      initDatabaseMetadata(meta);
      initDataTypeMetadata(meta);
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
   public Vector getCreateTables(XMLDBMSMap map)
   {
      Enumeration  tables;
      Table        table;
      Vector       strings = new Vector();

      tables = map.getTables();
      while (tables.hasMoreElements())
      {
         table = (Table)tables.nextElement();
         strings.addElement(getCreateTable(table));
      }
      return strings;
   }

   /**
    * Gets the CREATE TABLE string for a specific table.
    *
    * @param table The table
    * @return The CREATE TABLE string
    */
   public String getCreateTable(Table table)
   {
      StringBuffer sb = new StringBuffer();
      boolean      needComma;
      Enumeration  columns, keys;
      Column       column;
      Key          key;

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
      // Use the identifier m_quote character for the table name.

      // If the catalog name exists, is used, and is at the start of the qualified
      // name, add it now.

      if (m_useCatalog)
      {
         catalog = table.getCatalogName();
         if ((catalog != null) && (m_isCatalogAtStart))
         {
            sb.append(getQuotedName(catalog));
            sb.append(m_catalogSeparator);
         }
      }
 
      // If the schema name exists and is used, add it now.

      if (m_useSchema)
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

      if (m_useCatalog)
      {
         if ((catalog != null) && (!m_isCatalogAtStart))
         {
            sb.append(m_catalogSeparator);
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

      sb.append(m_quote);
      sb.append(name);
      sb.append(m_quote);
      return sb.toString();
   }

   private String getDataType(Column column)
   {
      int          type, value = 0;
      StringBuffer sb = new StringBuffer();
      String       params;
      int[]        positions = new int[3];
      String[]     paramTypes = {LENGTH, PRECISION, SCALE};
      boolean      exists, firstParam;

      // Get the type and the type name.

      type = column.getType();
      sb.append((String)typeNames.get(new Integer(type)));

      // Get the parameters for the type, such as length in VARCHAR(length)

      params = (String)createParams.get(new Integer(type));
      if (params != null)
      {
         // Parse the parameters and find out which ones are present.

         positions[0] = params.indexOf(LENGTH);
         positions[1] = params.indexOf(PRECISION);
         positions[2] = params.indexOf(SCALE);

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
      m_quote = "\"";
      m_isCatalogAtStart = true;
      m_catalogSeparator = ".";
      m_useCatalog = true;
      m_useSchema = true;
   }

   private void initDatabaseMetadata(DatabaseMetaData meta)
      throws SQLException
   {
      m_quote = meta.getIdentifierQuoteString();
      if (m_quote == null) m_quote = "";
      m_isCatalogAtStart = meta.isCatalogAtStart();
      m_catalogSeparator = meta.getCatalogSeparator();
      if (m_catalogSeparator == null) m_catalogSeparator = ".";
      if (m_catalogSeparator.length() == 0) m_catalogSeparator = ".";
      m_useCatalog = meta.supportsCatalogsInTableDefinitions();
      m_useSchema = meta.supportsSchemasInTableDefinitions();
   }

   private void initDataTypeMetadata()
   {
      // Use the default names

      typeNames.put(new Integer(Types.BIGINT), JDBCTypes.getName(Types.BIGINT));
      typeNames.put(new Integer(Types.BINARY), JDBCTypes.getName(Types.BINARY));
      typeNames.put(new Integer(Types.BIT), JDBCTypes.getName(Types.BIT));
      typeNames.put(new Integer(Types.CHAR), JDBCTypes.getName(Types.CHAR));
      typeNames.put(new Integer(Types.DATE), JDBCTypes.getName(Types.DATE));
      typeNames.put(new Integer(Types.DECIMAL), JDBCTypes.getName(Types.DECIMAL));
      typeNames.put(new Integer(Types.DOUBLE), JDBCTypes.getName(Types.DOUBLE));
      typeNames.put(new Integer(Types.FLOAT), JDBCTypes.getName(Types.FLOAT));
      typeNames.put(new Integer(Types.INTEGER), JDBCTypes.getName(Types.INTEGER));
      typeNames.put(new Integer(Types.LONGVARBINARY), JDBCTypes.getName(Types.LONGVARBINARY));
      typeNames.put(new Integer(Types.LONGVARCHAR), JDBCTypes.getName(Types.LONGVARCHAR));
      typeNames.put(new Integer(Types.NUMERIC), JDBCTypes.getName(Types.NUMERIC));
      typeNames.put(new Integer(Types.REAL), JDBCTypes.getName(Types.REAL));
      typeNames.put(new Integer(Types.SMALLINT), JDBCTypes.getName(Types.SMALLINT));
      typeNames.put(new Integer(Types.TIME), JDBCTypes.getName(Types.TIME));
      typeNames.put(new Integer(Types.TIMESTAMP), JDBCTypes.getName(Types.TIMESTAMP));
      typeNames.put(new Integer(Types.TINYINT), JDBCTypes.getName(Types.TINYINT));
      typeNames.put(new Integer(Types.VARBINARY), JDBCTypes.getName(Types.VARBINARY));
      typeNames.put(new Integer(Types.VARCHAR), JDBCTypes.getName(Types.VARCHAR));

      // Use the default create parameters

      createParams.put(new Integer(Types.BINARY), LENGTH);
      createParams.put(new Integer(Types.CHAR), LENGTH);
      createParams.put(new Integer(Types.DECIMAL), PRECISIONSCALE);
      createParams.put(new Integer(Types.FLOAT), PRECISION);
      createParams.put(new Integer(Types.LONGVARBINARY), LENGTH);
      createParams.put(new Integer(Types.LONGVARCHAR), LENGTH);
      createParams.put(new Integer(Types.NUMERIC), PRECISIONSCALE);
      createParams.put(new Integer(Types.VARBINARY), LENGTH);
      createParams.put(new Integer(Types.VARCHAR), LENGTH);
   }

   private void initDataTypeMetadata(DatabaseMetaData meta)
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

         // Store the type name and create parameters.

         typeNames.put(type, typeName);
         if (params != null)
         {
            createParams.put(type, params);
         }
      }

      // Close the result set.

      rs.close();
   }
}
