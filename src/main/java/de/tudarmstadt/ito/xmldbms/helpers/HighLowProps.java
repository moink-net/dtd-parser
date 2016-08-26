// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.helpers;

/**
 * Properties constants for KeyGeneratorHighLow.
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see KeyGeneratorHighLow
 */

public class HighLowProps
{
   // Catalog, schema, table, column information

   /** Catalog containing the high key table. */
   public static String HIGHLOWCATALOG = "HighLowCatalog";

   /** Schema containing the high key table. */
   public static String HIGHLOWSCHEMA = "HighLowSchema";

   /** Name of the high key table. */
   public static String HIGHLOWTABLE = "HighLowTable";

   /** Name of the column containing the high key value. */
   public static String HIGHLOWCOLUMN = "HighLowColumn";

   // Database connection properties

   /** JDBC driver class name (JDBC 1.0) */
   public static String HIGHLOWDRIVER   = "HighLowDriver";

   /** Database URL (JDBC 1.0) */
   public static String HIGHLOWURL      = "HighLowURL";

    /** Logical name of the database (JDBC 2.0) */
   public static String HIGHLOWDATASOURCE = "HighLowDataSource";

   /** Name of the JNDI Context in which to create the JDBC 2.0 DataSource (JDBC 2.0)*/
   public static String HIGHLOWDBINITIALCONTEXT = "HighLowDBInitialContext";

   /**  Database user name (JDBC 1.0 and 2.0) */
   public static String HIGHLOWUSER     = "HighLowUser";

    /**  Database password (JDBC 1.0 and 2.0) */
   public static String HIGHLOWPASSWORD = "HighLowPassword";

   /** JDBC level. Must be either 1 or 2. */
   public static String HIGHLOWJDBCLEVEL     = "HighLowJDBCLevel";
   
   // Miscellaneous

   /** Character used to separate the schema and table names. Defaults to "." */
   public static String HIGHLOWSCHEMASEPARATOR = "HighLowSchemaSeparator";
}