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
// * Changed Column.number to Column.rowObjectIndex.
// * Modified constructor for new name.
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.db.*;

import java.sql.*;

/**
 * Describes a column; <a href="../readme.html#NotForUse">not for general
 * use</a>.
 *
 * <p>Column contains information about a column. The column metadata (type,
 * length, nullability, etc.) can be set from a database or from a map
 * document. Columns are stored in Tables, PropertyMaps, PropertyTableMaps,
 * OrderInfos, and Keys.</p>
 *
 * <p>Note that column values are stored in two separate objects in XML-DBMS:</p>
 * 
 * <ul>
 * <li><b>Result sets:</b> These are JDBC ResultSet objects either passed
 * to DBMSToDOM or generated by DBMSToDOM when transferring data from the
 * database to XML.</li>
 *
 * <li><b>Row object:</b> This is an intermediate object used by both
 * DBMSToDOM and DOMToDBMS to buffer column values.</li>
 * </ul>
 *
 * <p>Row objects are hashtable-based and return column values by Column
 * object. These are designed for random access.</p>
 *
 * <p>Result sets are index-based and must be accessed in ascending order for
 * interoperability reasons. To make this possible, each Column is given a
 * result set index and the Table that owns the Column can return the Columns
 * as an array sorted by result set index. This array must be used when (a)
 * building SELECT statements to retrieve the columns in the table, and (b)
 * when retrieving column values from those result sets.</p>
 *
 * @author Ronald Bourret, 1998-1999, 2001
 * @version 2.0
 */

public class Column extends MapBase
{
   // ********************************************************************
   // Private variables
   // ********************************************************************

   private String name = null;

   private int             resultSetIndex;
   private int             type;
   private int             length;
   private int             precision;
   private int             scale;
   private int             nullability;
   private StringFormatter formatter;
   private boolean         precisionExists = false;
   private boolean         scaleExists = false;
   private boolean         lengthExists = false;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private Column(String name)
   {
      this.name = name;
      resetMetadata();
   }

   // ********************************************************************
   // Factory methods
   // ********************************************************************

   /**
    * Create a new column.
    *
    * @param columnName Name of the column.
    *
    * @return The column.
    */
   public static Column create(String columnName)
   {
      checkArgNull(columnName, ARG_COLUMNNAME);
      return new Column(columnName);
   }

   // ********************************************************************
   // Accessors and mutators
   // ********************************************************************

   // ********************************************************************
   // Name
   // ********************************************************************

   /**
    * Get the column name.
    *
    * @return The column name.
    */
   public final String getName()
   {
      return name;
   }

   // ********************************************************************
   // Result setindex
   // ********************************************************************

   /**
    * Get the result set index.
    *
    * <p>This is the index of the column value in the ResultSet. 1-based.</p>
    *
    * @return The result set index.
    */
   public final int getResultSetIndex()
   {
      return resultSetIndex;
   }

   /**
    * Set the result set index.
    *
    * <p>This is the index of the column value in a JDBC ResultSet. 1-based.</p>
    *
    * @param index The result set index.
    */
   public void setResultSetIndex(int index)
   {
      if (index < 1)
         throw new IllegalArgumentException("Result set index must be >= 1");
      resultSetIndex = index;
   }

   // ********************************************************************
   // Type
   // ********************************************************************

   /**
    * Get the column type.
    *
    * <p>This is one of the types in the JDBC Types class.</p>
    *
    * @return The column type.
    */
   public final int getType()
   {
      return type;
   }

   /**
    * Set the column type.
    *
    * <p>This is one of the types in the JDBC Types class. If the type is
    * not binary or character, this method sets the length to -1.</p>
    *
    * @param type The column type.
    */
   public void setType(int type)
   {
      if (!JDBCTypes.typeIsValid(type))
         throw new IllegalArgumentException("Invalid JDBC type: " + type);

      // If the new type is not a character or binary type, set the length to -1.

      if (!JDBCTypes.typeIsChar(type) && !JDBCTypes.typeIsBinary(type))
      {
         this.length = -1;
      }

      // If the new type is not DECIMAL or NUMBER, set the precision and scale to
      // invalid values.

      if ((type != Types.DECIMAL) && (type != Types.NUMERIC))
      {
         this.precision = -1;
         this.scale = Integer.MIN_VALUE;
      }

      // Set the new type.

      this.type = type;
   }

   // ********************************************************************
   // Length
   // ********************************************************************

   /**
    * Whether a length value exists.
    *
    * @return Whether a length value exists
    */
   public final boolean lengthExists()
   {
      return lengthExists;
   }

   /**
    * Get the column length.
    *
    * @return The column length. This is -1 if the length is not set.
    */
   public final int getLength()
   {
      return length;
   }

   /**
    * Set the column length.
    *
    * @return The column length.
    */
   public void setLength(int length)
   {
      if (length < 1)
         throw new IllegalArgumentException("Length must be >= 1");
      this.length = length;
      lengthExists = true;
   }

   // ********************************************************************
   // Precision
   // ********************************************************************

   /**
    * Whether a precision value exists.
    *
    * @return Whether a precision value exists
    */
   public final boolean precisionExists()
   {
      return precisionExists;
   }

   /**
    * Get the column precision.
    *
    * @return The column precision.
    */
   public final int getPrecision()
   {
      return precision;
   }

   /**
    * Set the column precision.
    *
    * @return The column precision.
    */
   public void setPrecision(int precision)
   {
      if (precision < 1)
         throw new IllegalArgumentException("Precision must be >= 1");
      this.precision = precision;
      precisionExists = true;
   }

   // ********************************************************************
   // Scale
   // ********************************************************************

   /**
    * Whether a scale value exists.
    *
    * @return Whether a scale value exists
    */
   public final boolean scaleExists()
   {
      return scaleExists;
   }

   /**
    * Get the column scale.
    *
    * @return The column scale.
    */
   public final int getScale()
   {
      return scale;
   }

   /**
    * Set the column scale.
    *
    * @return The column scale.
    */
   public void setScale(int scale)
   {
      this.scale = scale;
      scaleExists = true;
   }

   // ********************************************************************
   // Nullability
   // ********************************************************************

   /**
    * Whether the column is nullable.
    *
    * @return DatabaseMetaData.columnNullableUnknown, .columnNullable, or .columnNoNulls.
    */
   public final int getNullability()
   {
      return nullability;
   }

   /**
    * Set whether the column is nullable.
    *
    * @param nullability DatabaseMetaData.columnNullableUnknown, .columnNullable,
    *    or .columnNoNulls.
    */
   public void setNullability(int nullability)
   {
      if ((nullability != DatabaseMetaData.columnNullableUnknown) &&
          (nullability != DatabaseMetaData.columnNullable) &&
          (nullability != DatabaseMetaData.columnNoNulls))
         throw new IllegalArgumentException("Invalid nullability value: " + nullability);
      this.nullability = nullability;
   }

   // ********************************************************************
   // Formatting object
   // ********************************************************************

   /**
    * Get the column formatting object.
    *
    * <p>This method returns an object that implements the
    * org.xmlmiddleware.conversions.formatters.StringFormatter interface.</p>
    *
    * @return The formatting object. For a correctly initialized Column object, this
    *    will never be null.
    */
   public final StringFormatter getFormatter()
   {
      return formatter;
   }

   /**
    * Set the column formatting object.
    *
    * <p>The formatting object must implement the
    * org.xmlmiddleware.conversions.formatters.StringFormatter interface.</p>
    *
    * <p><b>WARNING!</b> Map factories must set a formatter for each column.</p>
    *
    * @param formatter The formatting object. May not be null.
    */
   public void setFormatter(StringFormatter formatter)
   {
      checkArgNull(formatter, ARG_FORMATTER);
      this.formatter = formatter;
   }

   // ********************************************************************
   // All metadata
   // ********************************************************************

   /**
    * Sets the metadata (type, length, etc.) to its initial state. Does not change the name.
    */
   public void resetMetadata()
   {
      resultSetIndex = -1;
      type = Types.NULL;
      length = -1;
      precision = -1;
      scale = Integer.MIN_VALUE;
      nullability = DatabaseMetaData.columnNullableUnknown;
      formatter = null;
   }

   /**
    * Checks whether metadata has been set for the column.
    *
    * @return Whether metadata has been set for the column.
    */
   public boolean isMetadataSet()
   {
      if (resultSetIndex == -1) return false;
      if (type == Types.NULL) return false;
      if ((JDBCTypes.typeIsChar(type) || JDBCTypes.typeIsBinary(type)) && (length == -1)) return false;
      if ((type == Types.DECIMAL) || (type == Types.NUMERIC))
      {
         if (precision == -1) return false;
         if (scale == Integer.MIN_VALUE) return false;
      }
      if (formatter == null) return false;
      return true;
   }
}
