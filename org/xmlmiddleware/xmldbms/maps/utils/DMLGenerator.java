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

package org.xmlmiddleware.xmldbms.maps.utils;

import org.xmlmiddleware.xmldbms.maps.*;
import java.lang.*;
import java.sql.*;
import java.util.*;

/**
 * Generate SELECT, UPDATE, INSERT, and DELETE strings.
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public class DMLGenerator
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private final static String INSERT      = "INSERT INTO ";
   private final static String VALUES      = " VALUES (";
   private final static String UPDATE      = "UPDATE ";
   private final static String SET          = " SET ";
   private final static String DELETE      = "DELETE ";
   
   private final static String PARAM         = "?";
   private final static String COMMA         = ", ";
   private final static String COMMAPARAM   = ", ?";
   private final static String CLOSEPAREN   = ")";
   private final static String OPENPAREN      = " (";
   private final static String SPACE         = " ";
   private final static String SELECT      = "SELECT ";
   private final static String FROM         = " FROM ";
   private final static String WHERE         = " WHERE ";
   private final static String ORDERBY      = " ORDER BY ";
   private final static String DESC         = " DESC";
   private final static String AND         = " AND ";
   private final static String EQUALSPARAM   = " = ?";
   private final static String PERIOD      = ".";

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new DMLGenerator.
    *
    * @param meta A DatabaseMetaData object.
    */
   public DMLGenerator(DatabaseMetaData meta)
      throws SQLException
   {
      m_quote = meta.getIdentifierQuoteString();
      m_isCatalogAtStart = meta.isCatalogAtStart();
      m_catalogSeparator = meta.getCatalogSeparator();
      m_useCatalog = meta.supportsCatalogsInDataManipulation();
      m_useSchema = meta.supportsSchemasInDataManipulation();
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   public String getInsert(Table t)
      throws SQLException
   {
      return getInsert(t, null);
   }
    
   /**
    * Returns an INSERT SQL string for the given table.
    *
    * @param t The table. Must not be null.
    * @return The INSERT string.
    */
   public String getInsert(Table t, Column[] cols)
      throws SQLException
   {
      StringBuffer insert = new StringBuffer(1000);

      // Create the INSERT statement.
      
      insert.append(INSERT);
      appendTableName(insert, t);

      insert.append(OPENPAREN);

      int numCols = 0;

      if(cols == null)
      {
         for(Enumeration e = t.getColumns(); e.hasMoreElements(); )
         {
            appendColumnName(insert, (Column)e.nextElement(), (numCols != 0));
            numCols++;
         }
      }
      else
      {
         for(int i = 0; i < cols.length; i++)
         {
            appendColumnName(insert, cols[i], (numCols != 0));
            numCols++;
         }
      }

      insert.append(CLOSEPAREN);
      
      insert.append(VALUES);
      insert.append(PARAM);
      for (int i = 1; i < numCols; i++)
      {
         insert.append(COMMAPARAM);
      }
      insert.append(CLOSEPAREN);

      return insert.toString();
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
   public String getSelect(Table t, Key key, OrderInfo order)
      throws SQLException
   {
      return buildSelect(t, key.getColumns(), null, order);
   }

   /**
    * Returns a "SELECT key WHERE key = ?" SQL string for a given table.
    *
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with.
    * @return The SELECT string.
    */
   public String getSelect(Table t, Key key)
      throws SQLException
   {
      return buildSelect(t, key.getColumns(), key.getColumns(), null);
   }

   /**
    * Returns a "SELECT cols WHERE Key = ?" SQL string for a given table.
    *
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with.
    * @param cols The columns to select.
    * @return The SELECT string.
    */
   public String getSelect(Table t, Key key, Column[] cols)
   {
      return buildSelect(t, key.getColumns(), cols, null);
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
      StringBuffer update = new StringBuffer(1000);
      boolean         first = true;

      update.append(UPDATE);

      // Add table name.
      appendTableName(update, t);

      update.append(SET);
      
      if(cols != null)
      {
         // Add column names.
         for(int i = 0; i < cols.length; i++)
         {
            if(first)
               update.append(COMMA);

            appendColumnName(update, cols[i], false);
            update.append(EQUALSPARAM);
            first = false;   
         }
      }
      else
      {
         // Add column names.
         for(Enumeration e = t.getColumns(); e.hasMoreElements(); )
         {
            if(first)
               update.append(COMMA);

            appendColumnName(update, (Column)e.nextElement(), false);
            update.append(EQUALSPARAM);
            first = false;
         }
      }

      appendWhereLink(update, key.getColumns());

      return update.toString();
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
      StringBuffer delete = new StringBuffer(1000);
      boolean         first = true;

      delete.append(DELETE);
      delete.append(FROM);

      // Add table name.
      appendTableName(delete, t);

      appendWhereLink(delete, key.getColumns());
      
      return delete.toString();
   }

   //**************************************************************************
   // Private/protected methods
   //**************************************************************************

   protected String buildSelect(Table t, Column[] keyColumns, 
                                Column[] valueColumns, OrderInfo order)
   {
      StringBuffer select = new StringBuffer(1000);
      boolean first = true;

      select.append(SELECT);
      
      if(valueColumns == null)
      {
         // Add column names.
         for(Enumeration e = t.getColumns(); e.hasMoreElements(); )
         {
            appendColumnName(select, (Column)e.nextElement(), first);
            first = false;
         }
      }
      else
      {
          // Add value column names.
          for(int i = 0; i < valueColumns.length; i++)
          {
             appendColumnName(select, valueColumns[i], first);
             first = false;
          }
      }
      
      // Add table name.
      
      select.append(FROM);
      appendTableName(select, t);
      
      if(keyColumns != null)
         appendWhereLink(select, keyColumns);

      // Add ORDER BY clause. We sort in descending order because this
      // gives us better performance in some cases. For more details,
      // see DBMSToDOM.Order.insertChild, which really ought to be
      // rewritten to use a binary search.
      
      if(order != null)
         appendOrderBy(select, order);

      return select.toString();
   }

   protected void appendWhereLink(StringBuffer stmt, Column[] keyColumns)
   {
      // Add WHERE clause.
      boolean first = false;

      stmt.append(WHERE);

      for(int i = 0; i < keyColumns.length; i++)
      {
         if(i != 0)
            stmt.append(AND);
      
         appendColumnName(stmt, keyColumns[i], false);
         stmt.append(EQUALSPARAM);
      }
   }

   protected void appendOrderBy(StringBuffer stmt, OrderInfo order)
   {
      // Just return if we are using fixed order values
      if (order.orderValueIsFixed()) return;

      // Add the ORDER BY clause

      stmt.append(ORDERBY);
      appendColumnName(stmt, order.getOrderColumn(), false);
      if(!order.isAscending())
         stmt.append(DESC);
   }

   protected void appendColumnName(StringBuffer str, Column column, boolean comma)
   {
      if(comma)
         str.append(COMMA);

      appendQuotedName(str, column.getName());
   }

   private void appendTableName(StringBuffer sb, Table table)
   {
      String catalog = null, schema;

      // 6/9/00, Ruben Lainez, Ronald Bourret
      // Use the identifier m_quote character for the table name.

      if(m_useCatalog)
      {
         catalog = table.getCatalogName();
         if((catalog != null) && (m_isCatalogAtStart))
         {
            appendQuotedName(sb, catalog);
            sb.append(m_catalogSeparator);
         }
      }
 
      if(m_useSchema)
      {
         schema = table.getSchemaName();
         if(schema != null)
         {
            appendQuotedName(sb, schema);
            sb.append(PERIOD);
         }
      }
 
      appendQuotedName(sb, table.getTableName());
 
      if(m_useCatalog)
      {
         if((catalog != null) && (!m_isCatalogAtStart))
         {
            sb.append(m_catalogSeparator);
            appendQuotedName(sb, catalog);
         }
      }
   }
 
   private void appendQuotedName(StringBuffer sb, String name)
   {
      sb.append(m_quote);
      sb.append(name);
      sb.append(m_quote);
   }

   //**************************************************************************
   // Member variables
   //**************************************************************************

   protected String m_quote;
   protected String m_catalogSeparator;
   protected boolean m_isCatalogAtStart;
   protected boolean m_useCatalog;
   protected boolean m_useSchema;
}
