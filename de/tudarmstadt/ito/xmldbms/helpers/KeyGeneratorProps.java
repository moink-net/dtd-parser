// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.helpers;

// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

/**
 * Properties constants for KeyGenerator.
 *
 * @author Adam Flinton
 * @author Ronald Bourret
 * @version 1.1
 * @see KeyGeneratorHighLow
 * @see Transfer
 * @see ProcessProperties
 */

public class KeyGeneratorProps
{














































































   // ************************************************************************
   // Constructor
   // ************************************************************************

   public KeyGeneratorProps()
   {
	  super();
   }      

   /**  Database Catalog */
   public static String HIGHLOWCATALOG = "HighLowCatalog";
	public static String HIGHLOWDATASOURCE = "HighLowDataSource";
/** JDBC Level & JDBC 2.0 */	
	public static String HIGHLOWDBINITIALCONTEXT = "HighLowDBInitialContext";
   /** JDBC driver class name */
   public static String HIGHLOWDRIVER   = "HighLowDriver";
	public static String HIGHLOWJDBCLEVEL     = "HighLowJDBCLevel";
   /**  Database password */
   public static String HIGHLOWPASSWORD = "HighLowPassword";
   /**  Database Schema */
   public static String HIGHLOWSCHEMA = "HighLowSchema";
   // Schema, Table etc Information

   
   /**  Database Schema Separator. Assumes "." if not given */
   public static String HIGHLOWSCHEMASEPARATOR = "HighLowSchemaSeparator";
   /**  Database Table */
   public static String HIGHLOWTABLE = "HighLowTable";
   // Database properties

   /** Database URL */
   public static String HIGHLOWURL      = "HighLowURL";
   /**  Database user name */
   public static String HIGHLOWUSER     = "HighLowUser";
}