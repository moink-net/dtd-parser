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

package org.xmlmiddleware.xmldbms.helpers;

import org.xmlmiddleware.xmldbms.XMLFormatter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Base class for custom formatting classes.
 *
 * <p>Classes that are used with the FormatClass element in the XML-DBMS
 * mapping language can use this class as a base class. It contains trivial
 * implementations of the methods in the XMLFormatter class (they all return
 * null) and is useful when a custom formatting class only needs to override
 * one pair of methods. For example, a custom formatting class might only perform
 * date formatting and therefore only override the formatDate and parseDate
 * methods.</p>
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.XMLFormatter
 */

public class XMLFormatterBase implements XMLFormatter
{
   // ********************************************************************
   // Constructors
   // ********************************************************************

   public XMLFormatterBase()
   {
   }

   // ********************************************************************
   // Public methods
   // ********************************************************************

   // CHAR, VARCHAR, LONGVARCHAR
   public String formatString(String value)
   {
      return null;
   }

   public String parseString(String string)
   {
      return null;
   }

   // DATE, TIME, TIMESTAMP
   public String formatDate(Date value)
   {
      return null;
   }

   public Date parseDate(String string)
   {
      return null;
   }

   // BIGINT, INTEGER, SMALLINT, TINYINT
   public String formatLong(Long value)
   {
      return null;
   }

   public Long parseLong(String string)
   {
      return null;
   }

   // DECIMAL, NUMERIC
   public String formatDecimal(BigDecimal value)
   {
      return null;
   }

   public BigDecimal parseDecimal(String string)
   {
      return null;
   }

   // DOUBLE, FLOAT, REAL
   public String formatDouble(Double value)
   {
      return null;
   }

   public Double parseDouble(String string)
   {
      return null;
   }

   // BINARY, VARBINARY, LONGVARBINARY
   public String formatBinary(byte[] value)
   {
      return null;
   }

   public byte[] parseBinary(String string)
   {
      return null;
   }

   // BIT
   public String formatBoolean(Boolean value)
   {
      return null;
   }

   public Boolean parseBoolean(String string)
   {
      return null;
   }
}