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
// Changes from version 1.0:
// * Changed Column.number to Column.rowObjectIndex.
// * Modified constructor for new name.
// Changes from version 1.01: Complete rewrite.

package org.xmlmiddleware.xmldbms.maps;

import java.sql.Types;
import java.sql.DatabaseMetaData;
import java.text.DateFormat;
import java.text.NumberFormat;

import org.xmlmiddleware.db.JDBCTypes;
import org.xmlmiddleware.xmldbms.XMLFormatter;

/**
 * Describes a column; <a href="../readme.html#NotForUse">not for general
 * use</a>.
 *
 * <p>Column contains information about a column. The column metadata (type,
 * length, nullability, etc.) can be set from a database or from a map
 * document. Columns are stored in Tables, PropertyMaps, PropertyTableMaps,
 * OrderInfos, and Keys.</p>
 *
 * <p>Note that column values are stored in three separate objects
 * in XML-DBMS:</p>
 * 
 * <ul>
 * <li><b>Result sets:</b> These are JDBC ResultSet objects either passed
 * to DBMSToDOM or generated by DBMSToDOM when transferring data from the
 * database to XML.</li>
 *
 * <li><b>Row object:</b> This is an intermediate object used by both
 * DBMSToDOM and DOMToDBMS to buffer column values.</li>
 *
 * <li><b>INSERT statement:</b> These are JDBC PreparedStatement objects
 * created by DOMToDBMS when transferring data from XML to the database.</li>
 * </ul>
 *
 * <p>The position of a column value can be different in each of these
 * three objects. For example, it could be in column 5 in the result set,
 * array index 3 in the Row object, and parameter 4 in the INSERT
 * statement. Thus, three sets of column numbers are needed:</p>
 *
 * <ul>
 * <li><b>Result set column number:</b> This is the number of the column in
 * the result set in which the value is stored. The number is 1-based and is
 * stored in the Table.rsColumnNumbers array. (Note that the order in which
 * result set column numbers are stored in this array guarantees that columns
 * in the result set will be accessed in increasing order.)</li>
 *
 * <li><b>Row object column number:</b> This is the index of the position in
 * the Row.columnValues array in which the value is stored. The index is
 * 0-based and is stored in the rowObjectIndex variable of Column. It is
 * also the index of the Table.rsColumnNumbers array; that is, column values
 * are stored in the Row object in the same order they occur in the result
 * set. In XML-DBMS version 1.0, this was named Column.number.</li>
 *
 * <li><b>INSERT statement parameter number:</b> This is the number of the
 * parameter in the INSERT statement in which the column value is stored. The
 * number is 1-based and corresponds to the order in which the Column object is
 * stored in the Table.columns array; it is therefore not stored separately.</li>
 * </ul>
 *
 * <p>In virtually all cases, these three column numbers are the same, except for
 * differences due to being 0- or 1-based. This is because XML-DBMS generates
 * SELECT and INSERT statements itself and therefore places the columns in the
 * same order in each. The only time that they differ is when the calling
 * application passes a result set to DBMSToDOM.retrieveDocument. In this case,
 * the number <i>and</i> order of columns in the result set can differ from the
 * order of the columns as they appear in the Table.columns array. Because of
 * this, the three different numbers are needed.</p>
 *
 * <p>In the future, the Column class should contain information about the
 * nullability and data type of the column as well. This will be used when creating
 * CREATE TABLE statements.</p>
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

   private int    resultSetIndex = -1;
   private int    rowIndex = -1;
   private int    parameterIndex = -1;
   private int    type = Types.NULL;
   private int    length = -1;
   private int    precision = -1;
   private int    scale = Integer.MIN_VALUE;
   private int    nullability = DatabaseMetaData.columnNullableUnknown;
   private Object formatObject = null;

   // ********************************************************************
   // Constructors
   // ********************************************************************

   private Column(String name)
   {
      this.name = name;
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
   // Row, result set, and parameter index
   // ********************************************************************

   /**
    * Get the row index.
    *
    * <p>This is the index of the column value in the Rows.columnValues
    * array. 0-based.</p>
    *
    * @return The row index.
    */
   public final int getRowIndex()
   {
      return rowIndex;
   }

   /**
    * Set the row index.
    *
    * <p>This is the index of the column value in the Rows.columnValues
    * array. 0-based.</p>
    *
    * @param index The row index.
    */
   public void setRowIndex(int index)
   {
      if (index < 0)
         throw new IllegalArgumentException("Row index must be >= 0");
      rowIndex = index;
   }

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

   /**
    * Get the parameter index.
    *
    * <p>This is the index of the column value in a JDBC INSERT PreparedStatement.
    * 1-based.</p>
    *
    * @return The parameter index.
    */
   public final int getParameterIndex()
   {
      return parameterIndex;
   }

   /**
    * Set the parameter index.
    *
    * <p>This is the index of the column value in a JDBC INSERT PreparedStatement.
    * 1-based.</p>
    *
    * @param index The parameter index.
    */
   public void setParameterIndex(int index)
   {
      if (index < 1)
         throw new IllegalArgumentException("Parameter index must be >= 1");
      parameterIndex = index;
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
    * Get the column length.
    *
    * <p>This method should only be called for character and binary types.
    * The return value is undefined for other types.</p>
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
    * <p>This method may only be called for character and binary types.</p>
    *
    * @return The column length.
    */
   public void setLength(int length)
   {
      if (!JDBCTypes.typeIsChar(type) || JDBCTypes.typeIsBinary(type))
         throw new IllegalStateException("setLength may be called only for columns with a character or binary type.");
      if (length < 1)
         throw new IllegalArgumentException("Length must be >= 1");
      this.length = length;
   }

   // ********************************************************************
   // Precision
   // ********************************************************************

   /**
    * Get the column precision.
    *
    * <p>This method should only be called for DECIMAL and NUMERIC types.
    * The return value is undefined for other types.</p>
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
    * <p>This method may only be called for DECIMAL and NUMERIC types.</p>
    *
    * @return The column precision.
    */
   public void setPrecision(int precision)
   {
      if ((type != Types.DECIMAL) && (type != Types.NUMERIC))
         throw new IllegalStateException("setPrecision may be called only for columns with a DECIMAL or NUMERIC type.");
      if (precision < 1)
         throw new IllegalArgumentException("Precision must be >= 1");
      this.precision = precision;
   }

   // ********************************************************************
   // Scale
   // ********************************************************************

   /**
    * Get the column scale.
    *
    * <p>This method should only be called for DECIMAL and NUMERIC types.
    * The return value is undefined for other types.</p>
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
    * <p>This method may only be called for DECIMAL and NUMERIC types.</p>
    *
    * @return The column scale.
    */
   public void setScale(int scale)
   {
      if ((type != Types.DECIMAL) && (type != Types.NUMERIC))
         throw new IllegalStateException("setScale may be called only for columns with a DECIMAL or NUMERIC type.");
      this.scale = scale;
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
   // Format object
   // ********************************************************************

   /**
    * Get the column format object.
    *
    * <p>This method returns a DateFormat object, a NumberFormat object, or an
    * object that implements the org.xmlmiddleware.xmldbms.XMLFormatter interface.
    * The calling method must determine the class of the returned object.</p>
    *
    * @return The format object. May be null.
    */
   public final Object getFormatObject()
   {
      return formatObject;
   }

   /**
    * Set the column format object.
    *
    * <p>The format object can be a DateFormat object, a NumberFormat object, or an object
    * that implements the org.xmlmiddleware.xmldbms.XMLFormatter interface.</p>
    *
    * @param formatObject The format object. If this is null, the default format
    *    object for the column type will be used.
    */
   public void setFormatObject(Object formatObject)
   {
      if (formatObject != null)
      {
         if (!(formatObject instanceof DateFormat) &&
             !(formatObject instanceof NumberFormat) &&
             !(formatObject instanceof XMLFormatter))
            throw new IllegalArgumentException("Format object must be a DateFormat or NumberFormat object or implement the org.xmlmiddleware.xmldbms.XMLFormatter interface.");
      }

      this.formatObject = formatObject;
   }
}