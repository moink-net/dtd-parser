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
 * Get SELECT, UPDATE, INSERT, and DELETE strings.
 *
 * <p>The class caches all strings except UPDATE strings, calling DMLGenerator
 * to generate new strings as needed.</p>
 *
 * @author Sean Walter, 2001
 * @version 2.0
 * @see org.xmlmiddleware.xmldbms.maps.utils.DMLGenerator
 */

public class SQLStrings
{
   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new SQLStrings.
    *
    * @param conn A database connection. This is used to get database metadata.
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
    * Returns an INSERT SQL string for the given table.
    *
    * @param t The table. Must not be null.
    * @return The INSERT string.
    */
   public String getInsert(Table t)
      throws SQLException
   {
      String id = "INSERT_" + t.getUniversalName();

      if(!m_strings.containsKey(id))
         m_strings.put(id, m_dml.getInsert(t));

      return (String)m_strings.get(id);
   }

   /**
    * Returns a "SELECT * WHERE key = ? ORDER BY ?" SQL string for a 
    * given table. 
    * 
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with.
    * @param order The sort information. May be null.
    * @return The SELECT string.
    */
   public String getSelectRow(Table t, Key key, OrderInfo order)
      throws SQLException
   {
      String id;

      id = "SELECTROW_" + t.getUniversalName() + key.getName();
      if (order != null)
      {
         id += ";" + String.valueOf(order.hashCode());
      }
      
      if(!m_strings.containsKey(id))
         m_strings.put(id, m_dml.getSelect(t, key, order));

      return (String)m_strings.get(id);
   }

   /**
    * Returns a "SELECT key WHERE key = ?" SQL string for a given table.
    *
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with.
    * @return The SELECT string.
    */
   public String getSelectKey(Table t, Key key)
      throws SQLException
   {
      String id = "SELECTKEY_" + t.getUniversalName() + key.getName();
      
      if(!m_strings.containsKey(id))
         m_strings.put(id, m_dml.getSelect(t, key));

      return (String)m_strings.get(id);
   }

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
   public String getSelectWhere(Table t, Key key, String where, OrderInfo order)
      throws SQLException
   {
      String id;

      id = "SELECTWHERE_" + t.getUniversalName();
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

      if(!m_strings.containsKey(id))
         m_strings.put(id, m_dml.getSelect(t, key, where, order));

      return (String)m_strings.get(id);
   }

   /**
    * Returns an UPDATE SQL string for a given table, key, and set of columns.
    *
    * @param t The table to update. Must not be null.
    * @param key The key to restrict with. Must not be null.
    * @param cols The columns to update. If this is null, all columns are included.
    * @return The UPDATE string.
    */
   public String getUpdate(Table t, Key key, Column[] cols)
      throws SQLException
   {
      // TODO: We may want to remove this column
      // We don't do caching for this one because chances are
      // the columns are different every time
      return m_dml.getUpdate(t, key, cols);
   }   

   /**
    * Returns a DELETE SQL string for a given table.
    *
    * @param t The table to delete from. Must not be null.
    * @param key The key to restrict with. Must not be null.
    * @return The DELETE string.
    */
   public String getDelete(Table t, Key key)
      throws SQLException
   {        
      String id = "DELETE_" + t.getUniversalName() + key.getName();

      
      if(!m_strings.containsKey(id))
         m_strings.put(id, m_dml.getDelete(t, key));

      return (String)m_strings.get(id);
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
   public String getDeleteWhere(Table t, Key key, String where)
      throws SQLException
   {
      String id;

      id = "DELETEWHERE_" + t.getUniversalName();
      if (key != null)
      {
         id += key.getName();
      }
      if (where != null)
      {
         id += ";" + String.valueOf(where.hashCode());
      }

      if(!m_strings.containsKey(id))
         m_strings.put(id, m_dml.getDeleteWhere(t, key, where));

      return (String)m_strings.get(id);
   }

   //**************************************************************************
   // Member variables
   //**************************************************************************

   protected Hashtable m_strings;
   protected DMLGenerator m_dml;
}

