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
// Changes from version 1.01: New in version 2.0

package org.xmlmiddleware.xmldbms.datahandlers;

import org.xmlmiddleware.xmldbms.maps.*;
import org.xmlmiddleware.xmldbms.maps.utils.*;

import java.sql.*;
import java.util.*;

/**
 * Get SELECT and DELETE strings.
 *
 * <p>The class caches SELECT and DELETE strings, calling DMLGenerator
 * to generate new strings as needed. It cannot cache INSERT and UPDATE
 * strings because these can contain differing columns on each call.</p>
 *
 * @author Sean Walter, 2001
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.maps.utils.DMLGenerator
 */

public class SQLStrings
{
   //**************************************************************************
   // Member variables
   //**************************************************************************

   protected Hashtable m_strings;
   protected DMLGenerator m_dml;

   //**************************************************************************
   // Constants
   //**************************************************************************

   private static final String SELECT = "SELECT_";
   private static final String DELETE = "DELETE_";
   private static final String DELETEWHERE = "DELETEWHERE_";

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new SQLStrings.
    *
    * @param conn A database connection. This is used to get database metadata.
    * @exception SQLException Thrown if an exception occurs while
    *    getting the database metadata.
    */
   public SQLStrings(Connection conn)
      throws SQLException
   {
      DatabaseMetaData meta = conn.getMetaData();
      m_dml = new DMLGenerator(meta);

      m_strings = new Hashtable();
   }

   /**
    * Construct a new SQLStrings.
    *
    * @param dml A DMLGenerator.
    */
   public SQLStrings(DMLGenerator dml)
   {
      m_dml = dml;
      m_strings = new Hashtable();
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Returns a "SELECT * WHERE key = ? AND &lt;where> ORDER BY ?" SQL string for a 
    * given table. 
    * 
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with. May be null.
    * @param where An additional where clause. May be null.
    * @param order The sort information. May be null.
    * @return The SELECT string.
    */
   public String getSelect(Table t, Key key, String where, OrderInfo order)
   {
      String id, str;

      // Build the id.

      id = SELECT + t.getUniversalName();
      if (key != null)
      {
         id += key.getName();
      }
      if (where != null)
      {
         id += ";" + String.valueOf(where.hashCode());
      }
      if (order != null)
      {
         id += ";" + String.valueOf(order.hashCode());
      }

      // Get the cached string or build and cache the string.

      str = (String)m_strings.get(id);
      if (str == null)
      {
         str = m_dml.getSelect(t, key, where, order);
         m_strings.put(id, str);
      }

      // Return the string.

      return str;
   }

   /**
    * Returns a DELETE SQL string for a given table.
    *
    * @param t The table to delete from. Must not be null.
    * @param key The key to restrict with. Must not be null.
    * @return The DELETE string.
    */
   public String getDelete(Table t, Key key)
   {
      // Build the ID.

      String id = DELETE + t.getUniversalName() + key.getName();

      // Get the cached string or build and cache the string.

      String str = (String)m_strings.get(id);
      if (str == null)
      {
         str = m_dml.getDelete(t, key);
         m_strings.put(id, str);
      }

      // Return the string

      return str;
   }

   /**
    * Returns a "DELETE FROM Table WHERE key = ? AND &lt;where>" SQL string for a 
    * given table. 
    * 
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with. May be null.
    * @param where An additional where clause. May be null.
    * @return The DELETE string.
    */
   public String getDelete(Table t, Key key, String where)
   {
      String id, str;

      // Build the id.

      id = DELETEWHERE + t.getUniversalName();
      if (key != null)
      {
         id += key.getName();
      }
      if (where != null)
      {
         id += ";" + String.valueOf(where.hashCode());
      }

      // Get the cached string or build and cache the string.

      str = (String)m_strings.get(id);
      if (str == null)
      {
         str = m_dml.getDelete(t, key, where);
         m_strings.put(id, str);
      }

      // Return the string.

      return str;
   }
}