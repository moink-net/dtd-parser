// No copyright, no warranty; use as you will.
// Written by Adam Flinton and Ronald Bourret, 2001

// Version 1.1
// Changes from version 1.01: New in 1.1

package de.tudarmstadt.ito.xmldbms.db;

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

public class DBProps
{

		
   // Database properties

   /** Database URL */
   public static String URL      = "URL";

   /** JDBC driver class name */
   public static String DRIVER   = "Driver";

   /**  Database user name */
   public static String USER     = "User";

   /**  Database password */
   public static String PASSWORD = "Password";






































































   // ************************************************************************
   // Constructor
   // ************************************************************************

   public DBProps()
   {
	  super();
   }               

	public static String DATASOURCE = "DataSource";
		/** JDBC Level & JDBC 2.0 */	
	public static String DBINITIALCONTEXT = "DBInitialContext";
	public static String JDBCLEVEL     = "JDBCLevel";
}