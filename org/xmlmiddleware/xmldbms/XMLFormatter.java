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
// Changes from version 1.0: New in version 2.0

package org.xmlmiddleware.xmldbms;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Interface for custom formatting classes.
 *
 * <p>Classes that are used with the FormatClass element in the XML-DBMS
 * mapping language must implement this interface. Such classes often
 * inherit from org.xmlmiddleware.xmldbms.helpers.XMLFormatterBase (in
 * which all of these methods return null) and override only one pair
 * of methods. For example, a custom formatting class might only perform
 * date formatting and therefore only override the formatDate and parseDate
 * methods.</p>
 *
 * <p><b>NOTE:</b> One possible use of custom formatting classes is a
 * to convert binary data to/from Base64. This could easily be done by
 * wrapping a Base64 converter with an XMLFormatter class.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.helpers.XMLFormatterBase
 */

public interface XMLFormatter
{
   // ********************************************************************
   // Public methods
   // ********************************************************************

   // CHAR, VARCHAR, LONGVARCHAR
   public String formatString(String value) throws XMLFormatterException;
   public String parseString(String string) throws XMLFormatterException;

   // DATE, TIME, TIMESTAMP
   public String formatDate(Date value) throws XMLFormatterException;
   public Date parseDate(String string) throws XMLFormatterException;

   // BIGINT, INTEGER, SMALLINT, TINYINT
   public String formatLong(Long value) throws XMLFormatterException;
   public Long parseLong(String string) throws XMLFormatterException;

   // DECIMAL, NUMERIC
   public String formatDecimal(BigDecimal value) throws XMLFormatterException;
   public BigDecimal parseDecimal(String string) throws XMLFormatterException;

   // DOUBLE, FLOAT, REAL
   public String formatDouble(Double value) throws XMLFormatterException;
   public Double parseDouble(String string) throws XMLFormatterException;

   // BINARY, VARBINARY, LONGVARBINARY
   public String formatBinary(byte[] value) throws XMLFormatterException;
   public byte[] parseBinary(String string) throws XMLFormatterException;

   // BIT
   public String formatBoolean(Boolean value) throws XMLFormatterException;
   public Boolean parseBoolean(String string) throws XMLFormatterException;
}