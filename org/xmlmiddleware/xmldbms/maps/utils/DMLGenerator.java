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
   // Member variables
   //**************************************************************************

   protected String m_quote;
   protected String m_catalogSeparator;
   protected boolean m_isCatalogAtStart;
   protected boolean m_useCatalog;
   protected boolean m_useSchema;

   //**************************************************************************
   // Constants
   //**************************************************************************

   private final static String INSERT      = "INSERT INTO ";
   private final static String VALUES      = " VALUES (";
   private final static String UPDATE      = "UPDATE ";
   private final static String SET         = " SET ";
   private final static String DELETE      = "DELETE ";
   
   private final static String PARAM       = "? ";
   private final static String COMMA       = ", ";
   private final static String COMMAPARAM  = ", ? ";
   private final static String CLOSEPAREN  = ")";
   private final static String OPENPAREN   = " (";
   private final static String SPACE       = " ";
   private final static String SELECT      = "SELECT ";
   private final static String FROM        = " FROM ";
   private final static String WHERE       = " WHERE ";
   private final static String ORDERBY     = " ORDER BY ";
   private final static String DESC        = " DESC";
   private final static String AND         = " AND ";
   private final static String EQUALSPARAM = " = ? ";
   private final static String PERIOD      = ".";

   //**************************************************************************
   // Constructors
   //**************************************************************************

   /**
    * Construct a new DMLGenerator.
    *
    * @param meta A DatabaseMetaData object.
    * @param SQLException Thrown if an error occurs retrieving database metadata.
    */
   public DMLGenerator(DatabaseMetaData meta)
      throws SQLException
   {
      m_quote = meta.getIdentifierQuoteString();
      if (m_quote == null) m_quote = "";

      // Find out whether the database supports catalogs and schemas, and get the
      // related information. The JDBC spec is vague about what drivers should do
      // when the underlying database doesn't support catalogs or schemas, so we
      // wrap these calls in try/catch clauses. Note that we don't know what exception
      // will be thrown, so we simply catch Exception and hope that the driver is
      // not stupid enough to access the database here, which could result in
      // legitimate exceptions.

      try
      {
         m_useCatalog = meta.supportsCatalogsInDataManipulation();
         if (m_useCatalog)
         {
            m_isCatalogAtStart = meta.isCatalogAtStart();
            m_catalogSeparator = meta.getCatalogSeparator();
            if (m_catalogSeparator == null) m_catalogSeparator = ".";
            if (m_catalogSeparator.length() == 0) m_catalogSeparator = ".";
         }
      }
      catch (Exception e)
      {
         m_useCatalog = false;
      }
      try
      {
         m_useSchema = meta.supportsSchemasInDataManipulation();
      }
      catch (Exception e)
      {
         m_useSchema = false;
      }
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Returns an INSERT SQL string for the given table.
    *
    * <p>The INSERT string includes all columns in the table.
    *
    * @param t The table. Must not be null.
    * @return The INSERT string.
    */
   public String getInsert(Table t)
   {
      return getInsert(t, null);
   }

   /**
    * Returns an INSERT SQL string for the given table.
    *
    * @param t The table. Must not be null.
    * @param cols The columns to include in the INSERT statement. If this is null,
    *    all columns are included.
    * @return The INSERT string.
    */
   public String getInsert(Table t, Column[] cols)
   {
      StringBuffer insert = new StringBuffer(1000);

      // Create the INSERT statement.
      
      insert.append(INSERT);
      insert.append(getTableName(t));

      insert.append(OPENPAREN);

      int numCols = 0;

      if(cols == null)
      {
         for(Enumeration e = t.getColumns(); e.hasMoreElements(); )
         {
            insert.append(makeColumnName((Column)e.nextElement(), (numCols != 0)));
            numCols++;
         }
      }
      else
      {
         for(int i = 0; i < cols.length; i++)
         {
            insert.append(makeColumnName(cols[i], (numCols != 0)));
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
   {
      return buildSelect(t, makeWhereLink(key.getColumns()), 
                         t.getResultSetColumns(), order);
   }

   /**
    * Returns a "SELECT key WHERE key = ?" SQL string for a given table.
    *
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with.
    * @return The SELECT string.
    */
   public String getSelect(Table t, Key key)
   {
      return buildSelect(t, makeWhereLink(key.getColumns()), 
                         key.getColumns(), null);
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
      return buildSelect(t, makeWhereLink(key.getColumns()), cols, null);
   }

   /** 
    * Returns a "SELECT * WHERE Key = ? AND &lt;where> ORDER BY ?"
    * SQL string for a given table
    * 
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with. May be null.
    * @param where An additional where constraint. May be null.
    * @param order The sort information. May be null.
    * @return The SELECT string.
    */
   public String getSelect(Table t, Key key, String where, OrderInfo order)
   {
      String whereClause = null;

      if (key != null)
      {
         whereClause = makeWhereLink(key.getColumns());
      }
      if (where != null)
      {
         whereClause = (whereClause == null) ? WHERE + where : whereClause + AND + where;
      }
      return buildSelect(t, whereClause, t.getResultSetColumns(), order);
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
   {
      StringBuffer update = new StringBuffer(1000);
      boolean      first = true;

      update.append(UPDATE);

      // Add table name.
      update.append(getTableName(t));

      update.append(SET);
      
      if(cols != null)
      {
         // Add column names.
         for(int i = 0; i < cols.length; i++)
         {
            if(!first)
               update.append(COMMA);
               
            update.append(makeColumnName(cols[i], false));
            update.append(EQUALSPARAM);

            first = false;   
         }
      }
      else
      {
         // Add column names.
         for(Enumeration e = t.getColumns(); e.hasMoreElements(); )
         {
            if(!first)
               update.append(COMMA);

            update.append(makeColumnName((Column)e.nextElement(), false));
            update.append(EQUALSPARAM);

            first = false;
         }
      }

      update.append(makeWhereLink(key.getColumns()));

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
   {
      StringBuffer delete = new StringBuffer(1000);

      delete.append(DELETE);
      delete.append(FROM);

      // Add table name.
      delete.append(getTableName(t));

      delete.append(makeWhereLink(key.getColumns()));
      
      return delete.toString();
   }

   /** 
    * Returns a "DELETE FROM Table WHERE Key = ? AND &lt;where>"
    * SQL string for a given table
    * 
    * @param t The table to select from. Must not be null.
    * @param key The key to restrict with. May be null.
    * @param where An additional where constraint. May be null.
    * @return The DELETE string.
    */
   public String getDelete(Table t, Key key, String where)
   {
      StringBuffer delete = new StringBuffer(1000);
      String       whereClause = null;

      delete.append(DELETE);
      delete.append(FROM);

      // Add table name.
      delete.append(getTableName(t));

      if (key != null)
      {
         whereClause = makeWhereLink(key.getColumns());
      }
      if (where != null)
      {
         whereClause = (whereClause == null) ? WHERE + where : whereClause + AND + where;
      }

      delete.append(whereClause);
      
      return delete.toString();
   }

   /** 
    * Returns a properly quoted (with schema, catalog if 
    * necessary) table name.
    *
    * @param table The table.
    * @return The quoted table name.
    */
   public String getTableName(Table table)
   {
      String catalog = null, schema;
      StringBuffer name = new StringBuffer(512);

      // 6/9/00, Ruben Lainez, Ronald Bourret
      // Use the identifier m_quote character for the table name.

      if(m_useCatalog)
      {
         catalog = table.getCatalogName();
         if((catalog != null) && (m_isCatalogAtStart))
         {
            name.append(makeQuotedName(catalog));
            name.append(m_catalogSeparator);
         }
      }
 
      if(m_useSchema)
      {
         schema = table.getSchemaName();
         if(schema != null)
         {
            name.append(makeQuotedName(schema));
            name.append(PERIOD);
         }
      }
 
      name.append(makeQuotedName(table.getTableName()));
 
      if(m_useCatalog)
      {
         if((catalog != null) && (!m_isCatalogAtStart))
         {
            name.append(m_catalogSeparator);
            name.append(makeQuotedName(catalog));
         }
      }

      return name.toString();
   }

   //**************************************************************************
   // Private/protected methods
   //**************************************************************************

   protected String buildSelect(Table t, String whereLink, 
                                Column[] valueColumns, OrderInfo order)
   {
      StringBuffer select = new StringBuffer(1000);
      boolean comma = false;

      select.append(SELECT);
      
      // Add value column names.
      for(int i = 0; i < valueColumns.length; i++)
      {
         if (valueColumns[i].getType() == Types.NULL) continue;
         select.append(makeColumnName(valueColumns[i], comma));
         comma = true;
      }
      
      // Add table name.
      
      select.append(FROM);
      select.append(getTableName(t));
      
      if(whereLink != null)
         select.append(whereLink);

      // Add ORDER BY clause.
      
      if(order != null)
         appendOrderBy(select, order);

      return select.toString();
   }

   protected String makeWhereLink(Column[] keyColumns)
   {
      // Add WHERE clause.
      StringBuffer where = new StringBuffer(1000);

      where.append(WHERE);

      for(int i = 0; i < keyColumns.length; i++)
      {
         if(i != 0)
            where.append(AND);
      
         where.append(makeColumnName(keyColumns[i], false));
         where.append(EQUALSPARAM);
      }

      return where.toString();
   }

   protected void appendOrderBy(StringBuffer stmt, OrderInfo order)
   {
      // Just return if we are using fixed order values

      if (order.orderValueIsFixed()) return;

      // Add the ORDER BY clause

      stmt.append(ORDERBY);
      stmt.append(makeColumnName(order.getOrderColumn(), false));
      if(!order.isAscending())
         stmt.append(DESC);
   }

   protected String makeColumnName(Column column, boolean comma)
   {
      String str = makeQuotedName(column.getName());

      if(comma)
         return COMMA + str;
      else
         return str;
   }

   private String makeQuotedName(String name)
   {
      return m_quote + name + m_quote;
   }
}
