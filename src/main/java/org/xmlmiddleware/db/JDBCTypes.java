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

package org.xmlmiddleware.db;

import org.xmlmiddleware.utils.*;

import java.sql.Types;

/**
 * JDBC type names and utilities.
 *
 * <p>All methods in this class are static. Generally, they are used only by map
 * factories and map utilities.</p>
 *
 * <p>This class does not consider Types.NULL to be a valid JDBC type.</p> 
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class JDBCTypes
{
   //*********************************************************************
   // Constants: JDBC type names and tokens
   //*********************************************************************

   // JDBC type names

   /** The string "BIGINT".*/
   public static String JDBCTYPE_BIGINT        = "BIGINT";

   /** The string "BINARY".*/
   public static String JDBCTYPE_BINARY        = "BINARY";

   /** The string "BIT".*/
   public static String JDBCTYPE_BIT           = "BIT";

   /** The string "CHAR".*/
   public static String JDBCTYPE_CHAR          = "CHAR";

   /** The string "DATE".*/
   public static String JDBCTYPE_DATE          = "DATE";

   /** The string "DECIMAL".*/
   public static String JDBCTYPE_DECIMAL       = "DECIMAL";

   /** The string "DOUBLE".*/
   public static String JDBCTYPE_DOUBLE        = "DOUBLE";

   /** The string "FLOAT".*/
   public static String JDBCTYPE_FLOAT         = "FLOAT";

   /** The string "INTEGER".*/
   public static String JDBCTYPE_INTEGER       = "INTEGER";

   /** The string "LONGVARBINARY".*/
   public static String JDBCTYPE_LONGVARBINARY = "LONGVARBINARY";

   /** The string "LONGVARCHAR".*/
   public static String JDBCTYPE_LONGVARCHAR   = "LONGVARCHAR";

   /** The string "NUMERIC".*/
   public static String JDBCTYPE_NUMERIC       = "NUMERIC";

   /** The string "OTHER".*/
   public static String JDBCTYPE_OTHER         = "OTHER";

   /** The string "REAL".*/
   public static String JDBCTYPE_REAL          = "REAL";

   /** The string "SMALLINT".*/
   public static String JDBCTYPE_SMALLINT      = "SMALLINT";

   /** The string "TIME".*/
   public static String JDBCTYPE_TIME          = "TIME";

   /** The string "TIMESTAMP".*/
   public static String JDBCTYPE_TIMESTAMP     = "TIMESTAMP";

   /** The string "TINYINT".*/
   public static String JDBCTYPE_TINYINT       = "TINYINT";

   /** The string "VARBINARY".*/
   public static String JDBCTYPE_VARBINARY     = "VARBINARY";

   /** The string "VARCHAR".*/
   public static String JDBCTYPE_VARCHAR       = "VARCHAR";

   /** Array containing all JDBC type names. */
   public static String[] JDBCTYPES = {
                                       JDBCTYPE_BIGINT,
                                       JDBCTYPE_BINARY,
                                       JDBCTYPE_BIT,
                                       JDBCTYPE_CHAR,
                                       JDBCTYPE_DATE,
                                       JDBCTYPE_DECIMAL,
                                       JDBCTYPE_DOUBLE,
                                       JDBCTYPE_FLOAT,
                                       JDBCTYPE_INTEGER,
                                       JDBCTYPE_LONGVARBINARY,
                                       JDBCTYPE_LONGVARCHAR,
                                       JDBCTYPE_NUMERIC,
                                       JDBCTYPE_OTHER,
                                       JDBCTYPE_REAL,
                                       JDBCTYPE_SMALLINT,
                                       JDBCTYPE_TIME,
                                       JDBCTYPE_TIMESTAMP,
                                       JDBCTYPE_TINYINT,
                                       JDBCTYPE_VARBINARY,
                                       JDBCTYPE_VARCHAR
                                      };

   // JDBC type tokens

   /** Array containing all JDBC Types values. In same order as JDBCTYPES. */
   public static final int[] JDBCTYPE_TOKENS = {
                                                Types.BIGINT,
                                                Types.BINARY,
                                                Types.BIT,
                                                Types.CHAR,
                                                Types.DATE,
                                                Types.DECIMAL,
                                                Types.DOUBLE,
                                                Types.FLOAT,
                                                Types.INTEGER,
                                                Types.LONGVARBINARY,
                                                Types.LONGVARCHAR,
                                                Types.NUMERIC,
                                                Types.OTHER,
                                                Types.REAL,
                                                Types.SMALLINT,
                                                Types.TIME,
                                                Types.TIMESTAMP,
                                                Types.TINYINT,
                                                Types.VARBINARY,
                                                Types.VARCHAR
                                               };

   //*********************************************************************
   // Private variables
   //*********************************************************************

   private static final InvertedTokenList jdbcTypes = new InvertedTokenList(JDBCTYPE_TOKENS, JDBCTYPES, null);
   private static final TokenList jdbcNames = new TokenList(JDBCTYPES, JDBCTYPE_TOKENS, Types.NULL);

   //*********************************************************************
   // Methods
   //*********************************************************************

   /**
    * Get the name of a JDBC type.
    *
    * @param type The type.
    * @return The type name. Returns null if the name is not found.
    */
   public static String getName(int type)
   {
      return jdbcTypes.getTokenName(type);
   }

   /**
    * Get a JDBC type from a name.
    *
    * @param type The name.
    * @return The type. Returns Types.NULL if the name is not found.
    */
   public static int getType(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name argument must not be null.");
      return jdbcNames.getToken(name);
   }

   /**
    * Whether a type is a valid JDBC Types value.
    *
    * <p>This method does not consider Types.NULL to be a valid type.</p>
    *
    * @param type The type.
    * @return Whether the type is a valid JDBC Types value.
    */
   public static boolean typeIsValid(int type)
   {
      return (jdbcTypes.getTokenName(type) != null);
   }

   /**
    * Whether a name is the name of a JDBC type.
    *
    * <p>This method only considers upper-case names to be valid. It does
    * not consider "NULL" to be a valid name.</p>
    *
    * @param name The name.
    * @return Whether the name is the name of a JDBC type.
    */
   public static boolean nameIsValid(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name argument must not be null.");
      return (jdbcNames.getToken(name) != Types.NULL);
   }

   /**
    * Whether the type is Types.OTHER.
    *
    * @param type The type.
    * @return Whether the type is Types.OTHER.
    */
   public static boolean typeIsOther(int type)
   {
      return (type == Types.OTHER);
   }

   /**
    * Whether the type is a binary type.
    *
    * @param type The type.
    * @return Whether the type is a binary type.
    */
   public static boolean typeIsBinary(int type)
   {
      return ((type == Types.BINARY) ||
              (type == Types.LONGVARBINARY) ||
              (type == Types.VARBINARY));
   }

   /**
    * Whether the type is a character type.
    *
    * @param type The type.
    * @return Whether the type is a character type.
    */
   public static boolean typeIsChar(int type)
   {
      return ((type == Types.CHAR) ||
              (type == Types.LONGVARCHAR) ||
              (type == Types.VARCHAR));
   }

   /**
    * Whether the type is a datetime type.
    *
    * @param type The type.
    * @return Whether the type is a datetime type.
    */
   public static boolean typeIsDateTime(int type)
   {
      return ((type == Types.DATE) ||
              (type == Types.TIME) ||
              (type == Types.TIMESTAMP));
   }

   /**
    * Whether the type is a numeric type.
    *
    * @param type The type.
    * @return Whether the type is a numeric type.
    */
   public static boolean typeIsNumeric(int type)
   {
      return ((type == Types.BIGINT) ||
              (type == Types.BIT) ||
              (type == Types.DECIMAL) ||
              (type == Types.DOUBLE) ||
              (type == Types.FLOAT) ||
              (type == Types.INTEGER) ||
              (type == Types.NUMERIC) ||
              (type == Types.REAL) ||
              (type == Types.SMALLINT) ||
              (type == Types.TINYINT));
   }


   /**
    * Convert date/time types from ODBC 2.0 values to ODBC 3.0/JDBC values.
    *
    * <p>The numbers for the date/time data types changed between ODBC 2.0 and
    * ODBC 3.0. JDBC uses the 3.0 numbers. This method converts the ODBC 2.0
    * values to ODBC 3.0/JDBC values. It is possible to get ODBC 2.0 values
    * when the ODBC Driver Manager or the ODBC-JDBC bridge hasn't converted them.</p>
    *
    * @param type The type.
    * @return The converted type. This is the same as the input type if the
    *    type is not an ODBC 2.0 date/time type.
    */
   public static int convertDateTimeType(int type)
   {
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
