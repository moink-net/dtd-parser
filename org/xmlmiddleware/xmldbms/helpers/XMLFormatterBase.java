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
// Changes from version 1.0: New in version 2.0

package org.xmlmiddleware.xmldbms.helpers;

import org.xmlmiddleware.xmldbms.XMLFormatter;
import org.xmlmiddleware.xmldbms.XMLFormatterException;

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
   public String formatString(String value) throws XMLFormatterException
   {
      return null;
   }

   public String parseString(String string) throws XMLFormatterException
   {
      return null;
   }

   // DATE, TIME, TIMESTAMP
   public String formatDate(Date value) throws XMLFormatterException
   {
      return null;
   }

   public Date parseDate(String string) throws XMLFormatterException
   {
      return null;
   }

   // BIGINT, INTEGER, SMALLINT, TINYINT
   public String formatLong(Long value) throws XMLFormatterException
   {
      return null;
   }

   public Long parseLong(String string) throws XMLFormatterException
   {
      return null;
   }

   // DECIMAL, NUMERIC
   public String formatDecimal(BigDecimal value) throws XMLFormatterException
   {
      return null;
   }

   public BigDecimal parseDecimal(String string) throws XMLFormatterException
   {
      return null;
   }

   // DOUBLE, FLOAT, REAL
   public String formatDouble(Double value) throws XMLFormatterException
   {
      return null;
   }

   public Double parseDouble(String string) throws XMLFormatterException
   {
      return null;
   }

   // BINARY, VARBINARY, LONGVARBINARY
   public String formatBinary(byte[] value) throws XMLFormatterException
   {
      return null;
   }

   public byte[] parseBinary(String string) throws XMLFormatterException
   {
      return null;
   }

   // BIT
   public String formatBoolean(Boolean value) throws XMLFormatterException
   {
      return null;
   }

   public Boolean parseBoolean(String string) throws XMLFormatterException
   {
      return null;
   }
}
