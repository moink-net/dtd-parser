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
// Changes from version 1.1: New in version 2.0

package org.xmlmiddleware.xmldbms.maps.utils;

import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import java.util.*;

/**
 * Initializes the metadata in an XMLDBMSMap object from a database.
 *
 * <p>Metadata must be set for any columns for which data is to be
 * transferred. The result of not setting metadata is undefined. Metadata
 * can be set in a number of ways: (1) from a map document, (2) from a
 * database, (3) from a result set, or (4) directly on Column objects.</p>
 *
 * <p>You can use the methods in this class to set metadata from a database
 * or a result set. When initializing metadata from a database, the "required"
 * argument specifies whether metadata must be found for all tables in the database.
 * In most cases, you set this argument to true. You set it to false if you
 * want to retrieve metadata for some tables from the database, then retrieve
 * metadata for other tables from another source, such as a result set.</p>
 *
 * <p>You can use XMLDBMSMap.checkMetadata() to determine if metadata has been set
 * for all tables.</p>
 *
 * <p>The methods in this class overwrite any existing metadata for tables
 * for which they find metadata.<p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class MetadataInitializer
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   private XMLDBMSMap map;

   //**************************************************************************
   // Constants
   //**************************************************************************

   private static String DEFAULT = "Default";

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new MetadataInitializer.
    *
    * @param map The XMLDBMSMap to initialize.
    */
   public MetadataInitializer(XMLDBMSMap map)
   {
      if (map == null)
         throw new IllegalArgumentException("map argument must not be null.");
      this.map = map;
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Initialize database metadata from a single database.
    *
    * @param databaseName The name of the database. If this is null, "Default" is used.
    * @param conn A connection to the database.
    * @param required Whether database metadata is required for all tables.
    * @exception XMLMiddlewareException Thrown if metadata is not found for a column or if required
    *    is true and metadata is not found for a table.
    */
   public void initializeMetadata(String databaseName, Connection conn, boolean required)
      throws XMLMiddlewareException
   {
      DatabaseMetaData meta;
      Enumeration      tables;
      Table            table;

      if (databaseName == null) databaseName = DEFAULT;

      try
      {
         meta = conn.getMetaData();

         tables = map.getTables();
         while (tables.hasMoreElements())
         {
            table = (Table)tables.nextElement();
            if (table.getDatabaseName().equals(databaseName))
            {
               addColumnMetadata(meta, table, required);
            }
            else if (required)
               throw new XMLMiddlewareException("Connection not found for database: " + table.getDatabaseName());
         }
      }
      catch (SQLException e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   /**
    * Initialize database metadata from multiple databases.
    *
    * @param databaseNames The names of the databases. If any name is null, "Default" is used.
    * @param conns The connections to the databases.
    * @param required Whether database metadata is required for all tables.
    * @exception XMLMiddlewareException Thrown if metadata is not found for a column or if required
    *    is true and metadata is not found for a table.
    */
   public void initializeMetadata(String[] databaseNames, Connection[] conns, boolean required)
      throws XMLMiddlewareException
   {
      Hashtable        connections = new Hashtable();
      String           name;
      Enumeration      tables;
      Table            table;
      Connection       conn;
      DatabaseMetaData meta;

      for (int i = 0; i < databaseNames.length; i++)
      {
         name = (databaseNames[i] == null) ? DEFAULT : databaseNames[i];
         connections.put(name, conns[i]);
      }

      tables = map.getTables();
      while (tables.hasMoreElements())
      {
         table = (Table)tables.nextElement();
         conn = (Connection)connections.get(table.getDatabaseName());
         if (conn != null)
         {
            try
            {
               meta = conn.getMetaData();
            }
            catch (SQLException e)
            {
               throw new XMLMiddlewareException(e);
            }
            addColumnMetadata(meta, table, required);
         }
         else if (required)
            throw new XMLMiddlewareException("Connection not found for database: " + table.getDatabaseName());

      }
   }

   /**
    * Initialize database metadata from a result set.
    *
    * <p>This method initializes metadata only for the specified table. (This is the
    * "table" that maps the result set.) This method does
    * not affect metadata for other tables. Thus, it can be called for different
    * tables, such as when multiple result sets are passed to DBMSToDOM.retrieveDocument.</p>
    *
    * <p>This method is liberal with respect to metadata. That is, if any columns
    * in the result set are not mapped, or if any columns in the specified table
    * are not in the result set, they are ignored. This allows a single mapping
    * to be used for multiple result sets based on the same table.</p>
    *
    * @param databaseName The name of the database used to map the result set.
    *    If this is null, "Default" is used.
    * @param catalogName The name of the catalog used to map the result set. May be null.
    * @param schemaName The name of the schema used to map the result set. May be null.
    * @param tableName The name of the table used to map the result set.
    * @param rs The result set.
    * @exception XMLMiddlewareException Thrown if the table is not found or an error occurs
    *    retrieving the metadata.
    */
   public void initializeMetadata(String    databaseName,
                                  String    catalogName,
                                  String    schemaName,
                                  String    tableName,
                                  ResultSet rs)
      throws XMLMiddlewareException
   {
      Table table;

      table = map.getTable(databaseName, catalogName, schemaName, tableName);
      if (table == null)
         throw new XMLMiddlewareException("Table not found: " + Table.getUniversalName(databaseName, catalogName, schemaName, tableName));

      initializeMetadata(table, rs);

   }

   /**
    * Initialize database metadata from a result set.
    *
    * <p>This method initializes metadata only for the specified table. (This is the
    * "table" that maps the result set.) This method does
    * not affect metadata for other tables. Thus, it can be called for different
    * tables, such as when multiple result sets are passed to DBMSToDOM.retrieveDocument.</p>
    *
    * <p>This method is liberal with respect to metadata. That is, if any columns
    * in the result set are not mapped, or if any columns in the specified table
    * are not in the result set, they are ignored. This allows a single mapping
    * to be used for multiple result sets based on the same table.</p>
    *
    * @param table The Table used to map the result set.
    * @param rs The result set.
    * @exception XMLMiddlewareException Thrown if the table is not found or an error occurs
    *    retrieving the metadata.
    */
   public void initializeMetadata(Table table, ResultSet rs)
      throws XMLMiddlewareException
   {
      ResultSetMetaData meta;
      Enumeration       columns;
      Column            column;

      initColumns(table);

      try
      {
         meta = rs.getMetaData();
         for (int i = 1; i <= meta.getColumnCount(); i++)
         {
            // Get the name of the next column in the result set. If the column
            // isn't mapped -- that is, it is not in the table -- then ignore it.

            column = table.getColumn(meta.getColumnName(i));
            if (column == null) continue;

            setColumnMetadata(column,
                              i,
                              meta.getColumnType(i),
                              meta.isNullable(i),
                              meta.getColumnDisplaySize(i),
                              meta.getPrecision(i),
                              meta.getScale(i));
         }
      }
      catch (SQLException e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   //**************************************************************************
   // Private methods
   //**************************************************************************

   private void addColumnMetadata(DatabaseMetaData meta, Table table, boolean required)
      throws XMLMiddlewareException
   {
      ResultSet  rs;
      Column     column;
      boolean    tableFound = false;
      int        rsIndex = 1, type, length, scale, precision, nullability;
      String     catalogName, schemaName, tableName;
      char[]     escape;

      try
      {
         // Build the catalog, schema, and table names as needed.

         escape = meta.getSearchStringEscape().toCharArray();
         catalogName = (meta.supportsCatalogsInDataManipulation()) ?
                        table.getCatalogName() : null;
         schemaName = (meta.supportsSchemasInDataManipulation()) ?
                       escapeDBName(table.getSchemaName(), escape) : null;
         tableName = escapeDBName(table.getTableName(), escape);

         // Get the column metadata result set and process it. Column 4 is column
         // name, column 5 is data type, and column 7 is length in characters.

         rs = meta.getColumns(catalogName, schemaName, tableName, null);

         while (rs.next())
         {
            tableFound = true;
            initColumns(table);

            // Get the next row of metadata and get the column name. If the column
            // isn't mapped, continue to the following row.

            column = table.getColumn(rs.getString(4));
            if (column == null) continue;

            type = (int)rs.getShort(5);
            length = rs.getInt(7);
            precision = length;
            scale = rs.getInt(9);
            nullability = rs.getInt(11);
            setColumnMetadata(column, rsIndex++, type, nullability, length, precision, scale);
         }

         // Close the result set.
         rs.close();

         // If the table was not found, throw an error.

         // 5/19/00, Ronald Bourret
         // Added comments about checking case to error message.
         //
         // A common problem is that users use a different case in the map document
         // than is used to store an identifier in the database. This is because
         // databases commonly case-fold unquoted identifiers in CREATE TABLE
         // statements before storing them. For example, the identifier Foo in
         // "CREATE TABLE Foo ..." might be stored as FOO. If the user uses Foo in
         // the map document, it is not found because the database uses FOO.
         //
         // Unfortunately, there is no easy technical solution to this problem, in
         // spite of the fact that JDBC provides information about how identifiers
         // are stored in the database. The problem is that the map document does
         // not support quoted identifiers. Thus, Foo could refer to the unquoted
         // identifier Foo (which might need to be case-folded to FOO before
         // comparison) or the quoted identifier "Foo" (which might not need to be
         // case-folded).
         //
         // Although we could support quoted identifiers in the map document,
         // (a) this is not backwards compatible, and (b) this is more complex than
         // simply requiring users to use the exact case.

         if (required && !tableFound)
            throw new XMLMiddlewareException("Table not found: " + table.getUniversalName() +
               ". Check that the table exists, that its name is spelled correctly, " +
               "and that the case used in the map document exactly matches the " +
               "case used in the database. This might be different than the case " +
               "you used when creating the table.");

         // Check that the metadata was set for all columns.

         column = table.checkMetadata();
         if (column != null)
            throw new XMLMiddlewareException("Column " + column.getName() + " not found in table " +
               table.getUniversalName() + ". Check that the column exists, " +
               "that its name is spelled correctly, and that the case " +
               "used in the map document exactly matches the case used in the " +
               "database. This might be different than the case you used when " +
               "creating the column.");
      }
      catch (SQLException e)
      {
         throw new XMLMiddlewareException(e);
      }
   }

   private String escapeDBName(String name, char[] escape)
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

   private void setColumnMetadata(Column column, int rsIndex, int type, int nullability, int length, int precision, int scale)
   {
      StringFormatter formatter;

      column.setResultSetIndex(rsIndex);

      type = fixDateTimeType(type);
      column.setType(type);
      column.setNullability(nullability);

      // Set the column formatter to the default formatter as needed. This
      // is necessary if the formatter has not been set or if the formatter
      // cannot handle the column's type. The latter case occurs when the
      // XMLDBMSMap is re-initialized using a new database and the column type in
      // the new database is different than the column type in the old database.

      formatter = column.getFormatter();
      if ((formatter == null) || (!formatter.canConvert(type)))
      {
         column.setFormatter(map.getDefaultFormatter(type));
      }

      // Set type-specific options.

      switch (type)
      {
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
            column.setLength(length);
            break;

         case Types.NUMERIC:
         case Types.DECIMAL:
            column.setPrecision(precision);
            column.setScale(scale);
            break;

         default:
            break;
      }
   }

   private void initColumns(Table table)
   {
      Enumeration columns;
      Column      column;

      // Initialize all column metadata to its initial state.

      columns = table.getColumns();
      while (columns.hasMoreElements())
      {
         column = (Column)columns.nextElement();
         column.resetMetadata();
      }
   }

   private int fixDateTimeType(int type)
   {
      // Check the column type. The numbers for the date/time data types
      // changed between ODBC 2.0 and ODBC 3.0 and JDBC uses the 3.0 numbers.
      // Thus, if we get the 2.0 numbers (the ODBC Driver Manager or an
      // ODBC-JDBC bridge hasn't converted them for us), we need to convert
      // them ourselves.

      switch (type)
      {
         case 9:
            return Types.DATE;

         case 10:
            return Types.TIME;

         case 11:
            return Types.TIMESTAMP;

         default:
            return type;
      }
   }
}
