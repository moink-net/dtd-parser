// No copyright, no warranty; use as you will.
// Written by Ronald Bourret, Technical University of Darmstadt, 1998-9

// Version 1.1
// Changes from version 1.0: None
// Changes from version 1.01: None

package de.tudarmstadt.ito.xmldbms;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * Sets parameters in an SQL statement.
 *
 * <P>The JDBC documentation seems to imply that setObject can convert
 * various types to various other types. Unfortunately, this isn't
 * the case for the JDBC-ODBC bridge, which throws a cast error. If
 * the bridge can't do this, and it is built on top of ODBC, which
 * <b>requires</b> this capability, there's not much hope in trying to
 * use it. (Of course, I could just be reading the spec incorrectly,
 * but setObject seems relatively worthless without this ability.)</P>
 *
 * <P>Therefore, this class does all the conversion work. Since Java's
 * narrowing conversions are, in my mind at least, at best meaningless
 * and at worst completely wrong - converting, for example, the double
 * value 12345.67 to the byte value 57 - we do all the actual checking here
 * taking advantage of the fact that Java can at least do widening conversions
 * correctly. Note that conversions are based, when necessary, on the ODBC
 * spec. This is because the JDBC simply doesn't say anything useful with
 * regard to conversion details, such as how to convert the integer 123
 * to a SQL BIT type, which JDBC maps to a boolean by default without ever
 * saying how to do such things as converting integers to booleans. (Is 123
 * true? False? An invalid conversion? Who knows?)</p>
 * 
 * <p>As a final bit of complaining, one would have expected any decent
 * JDBC driver to do all this work, rather than forcing every application
 * in existence to do it, but I suppose that's asking too much...</p>
 *
 * @author Ronald Bourret, Technical University of Darmstadt
 * @version 1.1
 */

class Parameters
{
   //**************************************************************************
   // Variables
   //**************************************************************************

   DateFormat    dateFormatter, timeFormatter, timestampFormatter;
   ParsePosition pos = new ParsePosition(0);

   //**************************************************************************
   // Constants
   //**************************************************************************

   static final BigDecimal MAX_DOUBLE = new BigDecimal(Double.MAX_VALUE),
						   MIN_DOUBLE = new BigDecimal(Double.MIN_VALUE),
						   MAX_FLOAT = new BigDecimal(Float.MAX_VALUE),
						   MIN_FLOAT = new BigDecimal(Float.MIN_VALUE),
						   MAX_LONG = new BigDecimal(Long.MAX_VALUE),
						   MIN_LONG = new BigDecimal(Long.MIN_VALUE),
						   MAX_INTEGER = new BigDecimal(Integer.MAX_VALUE),
						   MIN_INTEGER = new BigDecimal(Integer.MIN_VALUE),
						   MAX_SHORT = new BigDecimal(Short.MAX_VALUE),
						   MIN_SHORT = new BigDecimal(Short.MIN_VALUE),
						   MAX_BYTE = new BigDecimal(Byte.MAX_VALUE),
						   MIN_BYTE = new BigDecimal(Byte.MIN_VALUE),
						   ZERO = new BigDecimal(0.0),
						   ONE = new BigDecimal(1.0);

   //**************************************************************************
   // Constructors
   //**************************************************************************

   Parameters(DateFormat dateFormatter, DateFormat timeFormatter, DateFormat timestampFormatter)
   {
	  this.dateFormatter = dateFormatter;
	  this.timeFormatter = timeFormatter;
	  this.timestampFormatter = timestampFormatter;
   }   

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
	* Set parameters from an array of values.
	*
	* @param p Prepared SQL statement
	* @param values Parameter values. These must match the parameters in the SQL
	*  statement in number and order.
	* @param columns Column objects. These are used to get the data types of the
	*  parameters and must match the parameters in the SQL statement in number
	*  and order.
	* @exception SQLException A database error occurred while setting
	*  the parameter.
	*/
   void setParameters(PreparedStatement p, Object[] values, Column[] columns)
	  throws SQLException
   {

	  for (int i = 0; i < columns.length; i++)
	  {
		 setParameter(p, i + 1, columns[i].type, values[i]);
	  }
   }         

   /**
	* Set parameters from a Row.
	*
	* @param p Prepared SQL statement
	* @param row Row containing the parameter values.
	* @param columns Column objects. These are used to get the data types of the
	*  parameters and must match the parameters in the SQL statement in number
	*  and order.
	* @exception SQLException A database error occurred while setting
	*  the parameter.
	*/
   void setParameters(PreparedStatement p, Row row, Column[] columns)
	  throws SQLException
   {
	  for (int i = 0; i < columns.length; i++)
	  {
		 setParameter(p, i+1, columns[i].type, row.getColumnValue(columns[i]));
	  }
   }   

   /**
	* Set a single parameter.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as an Object
	* @exception SQLException A database error occurred while setting
	*  the parameter.
	*/

   void setParameter(PreparedStatement p, int number, int type, Object value)
	  throws SQLException
   {
	  // Call the correct method to convert the object, based on its type.
	  // Note that we take some guesses here on which types are most common
	  // and check for those first.

	  if (value == null)
	  {
		 p.setNull(number, type);
	  }
	  else if (value instanceof String)
	  {
		 setStringParameter(p, number, type, (String)value);
	  }
	  else if (value instanceof Integer)
	  {
		 setIntegerParameter(p, number, type, (Integer)value);
	  }
	  else if (value instanceof Date)
	  {
		 setDateParameter(p, number, type, (Date)value);
	  }
	  else if (value instanceof Timestamp)
	  {
		 setTimestampParameter(p, number, type, (Timestamp)value);
	  }
	  else if (value instanceof Time)
	  {
		 setTimeParameter(p, number, type, (Time)value);
	  }
	  else if (value instanceof BigDecimal)
	  {
		 setBigDecimalParameter(p, number, type, (BigDecimal)value);
	  }
	  else if (value instanceof Float)
	  {
		 setFloatParameter(p, number, type, (Float)value);
	  }
	  else if (value instanceof Short)
	  {
		 setShortParameter(p, number, type, (Short)value);
	  }
	  else if (value instanceof Long)
	  {
		 setLongParameter(p, number, type, (Long)value);
	  }
	  else if (value instanceof Double)
	  {
		 setDoubleParameter(p, number, type, (Double)value);
	  }
	  else if (value instanceof Boolean)
	  {
		 setBooleanParameter(p, number, type, (Boolean)value);
	  }
	  else if (value instanceof Byte)
	  {
		 setByteParameter(p, number, type, (Byte)value);
	  }
	  else
	  {
		 throw new SQLException("Object used to set parameter has unknown type. Object value: " + value.toString());
	  }
   }   

   // The strategy followed by the numeric conversion functions is as follows:
   //
   // * If it is safe to promote the number to the target type, then do so.
   //   We allow a loss of precision when promoting integers to floating
   //   point numbers; this is in accordance with the ODBC spec, which states
   //   that when converting any number to a floating point type, the
   //   conversion is legal if the "data is within the range of the data type
   //   to which the number is being converted."
   //
   // * If the number must be demoted, then first promote the number to either
   //   long (if it is an integer) or double (if it is a floating point), then
   //   process then safely-promoted number with a generic function. We allow
   //   truncation of fractional digits when converting from floating point to
   //   integer, again in accordance with the ODBC spec.

   /**
	* Set a parameter using a BigDecimal value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setBigDecimalParameter(PreparedStatement p, int number, int type, BigDecimal value)
	  throws SQLException
   {
	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, toBoolean(value));
			break;

		 case Types.TINYINT:
			p.setByte(number, toByte(value));
			break;

		 case Types.SMALLINT:
			p.setShort(number, toShort(value));
			break;

		 case Types.INTEGER:
			p.setInt(number, toInt(value));
			break;

		 case Types.BIGINT:
			p.setLong(number, toLong(value));
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, toFloat(value));
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, toDouble(value));
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, value);
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from BigDecimal to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Boolean value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setBooleanParameter(PreparedStatement p, int number, int type, Boolean value)
	  throws SQLException
   {
	  Long l;

	  if (value.booleanValue())
	  {
		 l = new Long(1);
	  }
	  else
	  {
		 l = new Long(0);
	  }
	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, value.booleanValue());
			break;

		 case Types.TINYINT:
			p.setByte(number, l.byteValue());
			break;

		 case Types.SMALLINT:
			p.setShort(number, l.shortValue());
			break;

		 case Types.INTEGER:
			p.setInt(number, l.intValue());
			break;

		 case Types.BIGINT:
			p.setLong(number, l.longValue());
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, l.floatValue());
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, l.doubleValue());
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, new BigDecimal(l.doubleValue()));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Boolean to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Byte value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setByteParameter(PreparedStatement p, int number, int type, Byte value)
	  throws SQLException
   {
	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, toBoolean(value.longValue()));
			break;

		 case Types.TINYINT:
			p.setByte(number, value.byteValue());
			break;

		 case Types.SMALLINT:
			p.setShort(number, value.shortValue());
			break;

		 case Types.INTEGER:
			p.setInt(number, value.intValue());
			break;

		 case Types.BIGINT:
			p.setLong(number, value.longValue());
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, value.floatValue());
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, value.doubleValue());
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, new BigDecimal(value.doubleValue()));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Byte to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Date value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setDateParameter(PreparedStatement p, int number, int type, Date value)
	  throws SQLException
   {
	  switch(type)
	  {
		 // Date/time conversions: date, timestamp

		 case Types.DATE:
			p.setDate(number, value);
			break;

		 case Types.TIMESTAMP:
			p.setTimestamp(number, new Timestamp(value.getTime()));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: integers, floating points, numerics, binary, time

		 case Types.BIT:
		 case Types.TINYINT:
		 case Types.SMALLINT:
		 case Types.INTEGER:
		 case Types.BIGINT:
		 case Types.REAL:
		 case Types.DOUBLE:
		 case Types.FLOAT:
		 case Types.DECIMAL:
		 case Types.NUMERIC:
		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.TIME:
		 default:
			throw new SQLException("Conversion from Date to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Double value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setDoubleParameter(PreparedStatement p, int number, int type, Double value)
	  throws SQLException
   {
	  double d = value.doubleValue();

	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, toBoolean(d));
			break;

		 case Types.TINYINT:
			p.setByte(number, toByte(d));
			break;

		 case Types.SMALLINT:
			p.setShort(number, toShort(d));
			break;

		 case Types.INTEGER:
			p.setInt(number, toInt(d));
			break;

		 case Types.BIGINT:
			p.setLong(number, toLong(d));
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, toFloat(d));
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, d);
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, new BigDecimal(d));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Double to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Float value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setFloatParameter(PreparedStatement p, int number, int type, Float value)
	  throws SQLException
   {
	  double d = value.doubleValue();

	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, toBoolean(d));
			break;

		 case Types.TINYINT:
			p.setByte(number, toByte(d));
			break;

		 case Types.SMALLINT:
			p.setShort(number, toShort(d));
			break;

		 case Types.INTEGER:
			p.setInt(number, toInt(d));
			break;

		 case Types.BIGINT:
			p.setLong(number, toLong(d));
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, value.floatValue());
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, d);
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, new BigDecimal(d));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Float to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Integer value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setIntegerParameter(PreparedStatement p, int number, int type, Integer value)
	  throws SQLException
   {
	  long l = value.longValue();

	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, toBoolean(l));
			break;

		 case Types.TINYINT:
			p.setByte(number, toByte(l));
			break;

		 case Types.SMALLINT:
			p.setShort(number, toShort(l));
			break;

		 case Types.INTEGER:
			p.setInt(number, value.intValue());
			break;

		 case Types.BIGINT:
			p.setLong(number, l);
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, value.floatValue());
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, value.doubleValue());
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, new BigDecimal(value.doubleValue()));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Integer to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Long value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setLongParameter(PreparedStatement p, int number, int type, Long value)
	  throws SQLException
   {
	  long l = value.longValue();

	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, toBoolean(l));
			break;

		 case Types.TINYINT:
			p.setByte(number, toByte(l));
			break;

		 case Types.SMALLINT:
			p.setShort(number, toShort(l));
			break;

		 case Types.INTEGER:
			p.setInt(number, toInt(l));
			break;

		 case Types.BIGINT:
			p.setLong(number, l);
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, value.floatValue());
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, value.doubleValue());
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, new BigDecimal(value.doubleValue()));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Long to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Short value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setShortParameter(PreparedStatement p, int number, int type, Short value)
	  throws SQLException
   {
	  long l = value.longValue();

	  switch(type)
	  {
		 // Integer conversions: bit, tinyint, smallint, integer, bigint

		 case Types.BIT:
			p.setBoolean(number, toBoolean(l));
			break;

		 case Types.TINYINT:
			p.setByte(number, toByte(l));
			break;

		 case Types.SMALLINT:
			p.setShort(number, value.shortValue());
			break;

		 case Types.INTEGER:
			p.setInt(number, value.intValue());
			break;

		 case Types.BIGINT:
			p.setLong(number, l);
			break;

		 // Floating point conversions: real, float/double

		 case Types.REAL:
			p.setFloat(number, value.floatValue());
			break;

		 case Types.FLOAT:
		 case Types.DOUBLE:
			p.setDouble(number, value.doubleValue());
			break;

		 // Numeric conversions: decimal/numeric

		 case Types.DECIMAL:
		 case Types.NUMERIC:
			p.setBigDecimal(number, new BigDecimal(value.doubleValue()));
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: binary/varbinary/longvarbinary, date, time, timestamp

		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIME:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Short to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a String value. This method assumes that the
	* parameter value is not null.
	*
	* <P>Implementation notes:</P>
	* <UL>
	* <LI>Binary data is not supported.</LI>
	* <LI>The accepted string forms of true are: &quot;1&quot;,
	*     &quot;yes&quot;, and &quot;true&quot;. The accepted string
	*     forms of false are: &quot;0&quot;, &quot;no&quot;, and
	*    &quot;false&quot;. All are case-insensitive.</LI>
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter.
	*/
   void setStringParameter(PreparedStatement p, int number, int type, String value)
	  throws SQLException
   {
	  try
	  {
		 switch(type)
		 {
			case Types.BIGINT:
			   p.setLong(number, Long.parseLong(value));
			   break;
   
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			   throw new SQLException("Binary data not supported");
   
			case Types.BIT:
			   value = value.toLowerCase();
			   if (value.equals("1") || value.equals("yes") || value.equals("true"))
			   {
				  p.setBoolean(number, true);
			   }
			   else if (value.equals("0") || value.equals("no") || value.equals("false"))
			   {
				  p.setBoolean(number, false);
			   }
			   else
			   {
				  throw new SQLException("Invalid boolean value: " + value);
			   }
			   break;
   
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			   p.setString(number, value);
			   break;
   
			case Types.DATE:
			   p.setDate(number, getDate(value));
			   break;
   
			case Types.DECIMAL:
			case Types.NUMERIC:
			   p.setBigDecimal(number, new BigDecimal(value));
			   break;
   
			case Types.FLOAT:
			case Types.DOUBLE:
			   p.setDouble(number, new Double(value).doubleValue());
			   break;
   
			case Types.REAL:
			   p.setFloat(number, new Float(value).floatValue());
			   break;
   
			case Types.INTEGER:
			   p.setInt(number, Integer.parseInt(value));
			   break;
   
			case Types.SMALLINT:
			   p.setShort(number, Short.parseShort(value));
			   break;

			case Types.TINYINT:
			   p.setByte(number, Byte.parseByte(value));
			   break;
   
			case Types.TIME:
			   p.setTime(number, getTime(value));
			   break;
   
			case Types.TIMESTAMP:
			   p.setTimestamp(number, getTimestamp(value));
			   break;
   
			default:
			   throw new SQLException("Unknown data type: " + type);
		 }
	  }
	  catch (NumberFormatException n)
	  {
		 throw new SQLException("Invalid numeric value: " + value);
	  }
   }   

   /**
	* Set a parameter using a Time value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setTimeParameter(PreparedStatement p, int number, int type, Time value)
	  throws SQLException
   {
	  switch(type)
	  {
		 // Date/time conversions: date, timestamp

		 case Types.TIME:
			p.setTime(number, value);
			break;

		 // String conversions: char/varchar/longvarchar
		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: integers, floating points, numerics, binary,
		 // date, timestamp.
		 // 
		 // Note that we follow the JDBC Getting Started manual with
		 // respect to converting Time to Timestamp. ODBC allows this,
		 // setting the date to the current date, but the Getting
		 // Started manual states that setObject cannot do this. Since
		 // the JDBC spec doesn't state whether this conversion is
		 // supported, this is the best we have to go on...

		 case Types.BIT:
		 case Types.TINYINT:
		 case Types.SMALLINT:
		 case Types.INTEGER:
		 case Types.BIGINT:
		 case Types.REAL:
		 case Types.DOUBLE:
		 case Types.DECIMAL:
		 case Types.NUMERIC:
		 case Types.FLOAT:
		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 case Types.DATE:
		 case Types.TIMESTAMP:
		 default:
			throw new SQLException("Conversion from Time to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Set a parameter using a Timestamp value. This method assumes that the
	* parameter value is not null.
	*
	* @param p Prepared SQL statement
	* @param number Parameter number
	* @param type Parameter type
	* @param value Parameter value as a String
	* @exception SQLException A database error occurred while setting
	*  the parameter or the specified conversion was not supported.
	*/
   void setTimestampParameter(PreparedStatement p, int number, int type, Timestamp value)
	  throws SQLException
   {
	  switch(type)
	  {
		 // Date/time conversions: date, timestamp

		 case Types.DATE:
			p.setDate(number, new Date(value.getTime()));
			break;

		 case Types.TIME:
			p.setTime(number, new Time(value.getTime()));
			break;

		 case Types.TIMESTAMP:
			p.setTimestamp(number, value);
			break;

		 // String conversions: char/varchar/longvarchar

		 case Types.CHAR:
		 case Types.VARCHAR:
		 case Types.LONGVARCHAR:
			p.setString(number, value.toString());
			break;

		 // Can't convert: integers, floating points, numerics, binary, time

		 case Types.BIT:
		 case Types.TINYINT:
		 case Types.SMALLINT:
		 case Types.INTEGER:
		 case Types.BIGINT:
		 case Types.REAL:
		 case Types.DOUBLE:
		 case Types.FLOAT:
		 case Types.DECIMAL:
		 case Types.NUMERIC:
		 case Types.BINARY:
		 case Types.VARBINARY:
		 case Types.LONGVARBINARY:
		 default:
			throw new SQLException("Conversion from Timestamp to " + getJDBCTypeName(type) + " not supported.");
	  }
   }   

   /**
	* Returns the name of a JDBC Type constant.
	*
	* @param type Parameter type
	* @exception SQLException An unknown type was passed.
	*/
   String getJDBCTypeName(int type) throws SQLException
   {
	  switch(type)
	  {
		 case Types.BIGINT:
			return "BIGINT";

		 case Types.BINARY:
			return "BINARY";

		 case Types.BIT:
			return "BIT";

		 case Types.CHAR:
			return "CHAR";

		 case Types.DATE:
			return "DATE";

		 case Types.DECIMAL:
			return "DECIMAL";

		 case Types.DOUBLE:
			return "DOUBLE";

		 case Types.FLOAT:
			return "FLOAT";

		 case Types.INTEGER:
			return "INTEGER";

		 case Types.LONGVARBINARY:
			return "LONGVARBINARY";

		 case Types.LONGVARCHAR:
			return "LONGVARCHAR";

		 case Types.NUMERIC:
			return "NUMERIC";

		 case Types.REAL:
			return "REAL";

		 case Types.SMALLINT:
			return "SMALLINT";

		 case Types.TIME:
			return "TIME";

		 case Types.TIMESTAMP:
			return "TIMESTAMP";

		 case Types.TINYINT:
			return "TINYINT";

		 case Types.VARBINARY:
			return "VARBINARY";

		 case Types.VARCHAR:
			return "VARCHAR";

		 default:
			throw new SQLException("Unknown JDBC Type: " + type);
	  }
   }   

   // *************************************************************************
   // Numeric conversion functions -- converting doubles
   //
   // These functions check that the conversion (cast) won't result in
   // truncation of significant digits.
   // *************************************************************************

   float toFloat(double d) throws SQLException
   {
	  if ((d > Float.MAX_VALUE) || (d < Float.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (float)d;
   }   

   long toLong(double d) throws SQLException
   {
	  if ((d > Long.MAX_VALUE) || (d < Long.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (long)d;
   }   

   int toInt(double d) throws SQLException
   {
	  if ((d > Integer.MAX_VALUE) || (d < Integer.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (int)d;
   }   

   short toShort(double d) throws SQLException
   {
	  if ((d > Short.MAX_VALUE) || (d < Short.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (short)d;
   }   

   byte toByte(double d) throws SQLException
   {
	  if ((d > Byte.MAX_VALUE) || (d < Byte.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (byte)d;
   }   

   boolean toBoolean(double d) throws SQLException
   {
	  if (d == 0.0)
	  {
		 return false;
	  }
	  else if (d == 1.0)
	  {
		 return true;
	  }
	  else
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
   }   

   // *************************************************************************
   // Numeric conversion functions -- converting BigDecimals
   //
   // These functions check that the conversion won't result in
   // truncation of significant digits.
   // *************************************************************************

   double toDouble(BigDecimal b) throws SQLException
   {
	  if ((b.compareTo(MAX_DOUBLE) == 1) || (b.compareTo(MIN_DOUBLE) == -1))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return b.doubleValue();
   }   

   float toFloat(BigDecimal b) throws SQLException
   {
	  if ((b.compareTo(MAX_FLOAT) == 1) || (b.compareTo(MIN_FLOAT) == -1))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return b.floatValue();
   }   

   long toLong(BigDecimal b) throws SQLException
   {
	  if ((b.compareTo(MAX_LONG) == 1) || (b.compareTo(MIN_LONG) == -1))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return b.longValue();
   }   

   int toInt(BigDecimal b) throws SQLException
   {
	  if ((b.compareTo(MAX_INTEGER) == 1) || (b.compareTo(MIN_INTEGER) == -1))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return b.intValue();
   }   

   short toShort(BigDecimal b) throws SQLException
   {
	  if ((b.compareTo(MAX_SHORT) == 1) || (b.compareTo(MIN_SHORT) == -1))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return toShort(b.longValue());
   }   

   byte toByte(BigDecimal b) throws SQLException
   {
	  if ((b.compareTo(MAX_BYTE) == 1) || (b.compareTo(MIN_BYTE) == -1))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return toByte(b.longValue());
   }   

   boolean toBoolean(BigDecimal b) throws SQLException
   {
	  if (b.compareTo(ZERO) == 0)
	  {
		 return false;
	  }
	  else if (b.compareTo(ONE) == 0)
	  {
		 return true;
	  }
	  else
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
   }   

   // *************************************************************************
   // Numeric conversion functions -- converting longs
   //
   // These functions check that the conversion (cast) won't result in
   // truncation of significant digits.
   // *************************************************************************

   int toInt(long l) throws SQLException
   {
	  if ((l > Integer.MAX_VALUE) || (l < Integer.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (int)l;
   }   

   short toShort(long l) throws SQLException
   {
	  if ((l > Short.MAX_VALUE) || (l < Short.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (short)l;
   }   

   byte toByte(long l) throws SQLException
   {
	  if ((l > Byte.MAX_VALUE) || (l < Byte.MIN_VALUE))
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
	  return (byte)l;
   }   

   boolean toBoolean(long l) throws SQLException
   {
	  if (l == 0)
	  {
		 return false;
	  }
	  else if (l == 1)
	  {
		 return true;
	  }
	  else
		 throw new SQLException("Conversion resulted in truncation of significant digits.");
   }   

   // *************************************************************************
   // Date conversion functions
   // *************************************************************************

   private Date getDate(String value)
	  throws SQLException
   {
	  return new Date(getUtilDate(dateFormatter, value).getTime());
   }   

   private Time getTime(String value)
	  throws SQLException
   {
	  return new Time(getUtilDate(timeFormatter, value).getTime());
   }   

   private Timestamp getTimestamp(String value)
	  throws SQLException
   {
	  return new Timestamp(getUtilDate(timestampFormatter, value).getTime());
   }   

   private java.util.Date getUtilDate(DateFormat formatter, String value)
	  throws SQLException
   {
	  // Thanks to Matthias Pfisterer for pointing out Java's date
	  // formatting capabilities.

	  java.util.Date date;

	  try
	  {
		 if (formatter instanceof SimpleDateFormat)
		 {
			pos.setIndex(0);
			date = ((SimpleDateFormat)formatter).parse(value, pos);
			if (date == null)
			   throw new SQLException("Date/time value " + value + " does not match format " + ((SimpleDateFormat)formatter).toPattern());
		 }
		 else // if (formatter instanceof DateFormat)
		 {
			date = formatter.parse(value);
			if (date == null)
			   throw new SQLException("Date/time value " + value + " does not match specified format (or default format if no format was specified).");
		 }
		 return date;
	  }
	  catch (ParseException p)
	  {
		 throw new SQLException(p.getMessage());
	  }
   }   


/* -------------------------- NOT CURRENTLY USED -----------------------------
 * Constants
   private static String START_DATE_ESCAPE = "{d '",
						 START_TIME_ESCAPE = "{t '",
						 START_TIMESTAMP_ESCAPE = "{ts '",
						 END_ESCAPE = "'}";

   Object escapeDateTime(Object value, int type)
   {
	  // If a date, time, or timestamp is in string form, place it in an
	  // ODBC escape sequence. This assumes that the date, time, or timestamp
	  // is already in an acceptable form; this is not a valid assumption
	  // in many cases. The forms are:
	  //
	  // date:      yyyy-mm-dd
	  // time:      hh:mm:ss
	  // timestamp: yyyy-mm-dd hh:mm:ss[.f...]

	  try
	  {
		 switch (type)
		 {
			case Types.DATE:
			   return START_DATE_ESCAPE + (String)value + END_ESCAPE;

			case Types.TIME:
			   return START_TIME_ESCAPE + (String)value + END_ESCAPE;

			case Types.TIMESTAMP:
			   return START_TIMESTAMP_ESCAPE + (String)value + END_ESCAPE;

			default:
			   return value;
		 }
	  }
	  catch (ClassCastException c)
	  {
		 // This code is useful only if value is a String. Although that is
		 // generally the case, we need to ignore any cast exceptions.
		 return value;
	  }
   }
-------------------------- NOT CURRENTLY USED ----------------------------- */
/* -------------------------- NOT CURRENTLY USED -----------------------------
 * Constants
   private static String START_DATE_ESCAPE = "{d '",
						 START_TIME_ESCAPE = "{t '",
						 START_TIMESTAMP_ESCAPE = "{ts '",
						 END_ESCAPE = "'}";

   Object escapeDateTime(Object value, int type)
   {
	  // If a date, time, or timestamp is in string form, place it in an
	  // ODBC escape sequence. This assumes that the date, time, or timestamp
	  // is already in an acceptable form; this is not a valid assumption
	  // in many cases. The forms are:
	  //
	  // date:      yyyy-mm-dd
	  // time:      hh:mm:ss
	  // timestamp: yyyy-mm-dd hh:mm:ss[.f...]

	  try
	  {
		 switch (type)
		 {
			case Types.DATE:
			   return START_DATE_ESCAPE + (String)value + END_ESCAPE;

			case Types.TIME:
			   return START_TIME_ESCAPE + (String)value + END_ESCAPE;

			case Types.TIMESTAMP:
			   return START_TIMESTAMP_ESCAPE + (String)value + END_ESCAPE;

			default:
			   return value;
		 }
	  }
	  catch (ClassCastException c)
	  {
		 // This code is useful only if value is a String. Although that is
		 // generally the case, we need to ignore any cast exceptions.
		 return value;
	  }
   }
-------------------------- NOT CURRENTLY USED ----------------------------- */
}