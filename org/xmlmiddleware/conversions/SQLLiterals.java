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

package org.xmlmiddleware.conversions;

import org.xmlmiddleware.conversions.formatters.*;
import org.xmlmiddleware.utils.XMLMiddlewareException;

import java.sql.Types;

/**
 * Builds literals that can be used in a JDBC SQL statement.
 *
 * @author Ronald Bourret, 2001
 * @version 2.0
 */

public class SQLLiterals
{
   // ********************************************************************
   // Public methods
   // ********************************************************************

   /**
    * Build a SQL literal.
    *
    * @param type A JDBC Types value indicating the type of the literal to build.
    * @param value The value to use.
    * @return The SQL literal
    */
   public static String buildLiteral(int type, Object value, StringFormatter formatter)
      throws XMLMiddlewareException
   {
      StringBuffer sb;
      int          i;

      // First convert the value to the object corresponding to the target
      // type.

      value = ConvertObject.convertObject(value, type, formatter);

      // Now convert the value to a SQL literal.

      switch(type)
      {
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
            sb = new StringBuffer();
            byte[] bytes = ((ByteArray)value).getBytes();
            sb.append('X');
            sb.append('\'');
            for (i = 0; i < bytes.length; i++)
            {
               // Convert each byte to two hexadecimal digits.

               sb.append(toChar((bytes[i] & 0xF0) >> 4));
               sb.append(toChar(bytes[i] & 0x0F));
            }
            sb.append('\'');
            return sb.toString();

         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
            sb = new StringBuffer();
            sb.append('\'');
            char[] buf = ((String)value).toCharArray();
            for (i = 0; i < buf.length; i++)
            {
               sb.append(buf[i]);
               if (buf[i] == '\'') sb.append('\'');
            }
            sb.append('\'');
            return sb.toString();

         case Types.DOUBLE:
         case Types.FLOAT:
         case Types.REAL:
         case Types.DECIMAL:
         case Types.NUMERIC:
         case Types.BIGINT:
         case Types.INTEGER:
         case Types.SMALLINT:
         case Types.TINYINT:
            return value.toString();

         case Types.BIT:
            return value.toString().toUpperCase();

         case Types.DATE:
         case Types.TIME:
         case Types.TIMESTAMP:
            return value.toString();

/*
// These are ISO literals. Try the JDBC literals first and see
// if drivers accept them.
         case Types.DATE:
            sb = new StringBuffer();
            sb.append(DATE);
            sb.append('\'');
            sb.append(value.toString());
            sb.append('\'');
            return sb.toString();

         case Types.TIME:
            sb = new StringBuffer();
            sb.append(TIME);
            sb.append('\'');
            sb.append(value.toString());
            sb.append('\'');
            return sb.toString();

         case Types.TIMESTAMP:
            sb = new StringBuffer();
            sb.append(TIMESTAMP);
            sb.append('\'');
            sb.append(value.toString());
            sb.append('\'');
            return sb.toString();
*/

         default:
            throw new XMLMiddlewareException("Conversion to specified JDBC type not supported.");
      }
   }

   private static char toChar(int i)
   {
      // Yech. Note that ASCII 48-57 are 0-9 and ASCII 65-70 are A-F

      return ((char)((i < 10) ? i + 48 : i + 55));
   }

}
