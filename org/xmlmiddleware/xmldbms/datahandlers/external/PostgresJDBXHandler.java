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
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.xmldbms.datahandlers.external;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import javax.sql.*;

import org.sourceforge.jxdbcon.postgresql.PGPreparedStatement;

/**
 * <p>DataHandler implementation for Postgres using the JDBXCon drivers
 * (http://jxdbcon.sourceforge.net/).</p>
 *
 * <p>Database generated keys are retrieved using the the row oid.</p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public class PostgresJDBXHandler
   extends DataHandlerBase
{
   // ************************************************************************
   // Variables
   // ************************************************************************

   // The key for the 'oid' column
   private Key m_oidKey = null;

   // ************************************************************************
   // Constants
   // ************************************************************************

   protected final static String OIDNAME = "oid";

   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
    * Creates a PostgresJDBXHandler
    */
   public PostgresJDBXHandler()
   {
      super();
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Overrides DataHandlerBase.initialize().
    *
    * @param dataSource The DataSource to get Connections from.
    * @param user The user to connect to the database as. May be null.
    * @param password The password to connect to the database with. May be null.
    * @exception SQLException Thrown if a database error occurs.
    */
   public void initialize(DataSource dataSource, String user, String password)
      throws SQLException
   {
      super.initialize(dataSource, user, password);

      // Create the key

      m_oidKey = createColumnKey(OIDNAME, Types.INTEGER);
   }

   /**
    * Inserts a row into the table.
    *
    * <p>This method sets the columns in the row of any primary or unique keys
    * that were generated by the database. It does this via the oid column.</p>
    *
    * @param table Table to insert into.
    * @param row Row to insert.
    * @exception SQLException A database error occurred while inserting data.
    */
   public void insert(Table table, Row row)
      throws SQLException
   {
      checkState();

      PreparedStatement stmt = buildInsert(table, row);
      int numRows = stmt.executeUpdate();

      databaseModified();

      Column[] dbGeneratedCols = getDBGeneratedKeyCols(table);

      if(dbGeneratedCols.length > 0)
      {
         PGPreparedStatement psqlStmt = (PGPreparedStatement)getRawStatement(stmt);

         // Get the OID of the last row

         ResultSet oidRs = psqlStmt.getGeneratedKeys();
         int oid = oidRs.getInt(OIDNAME);

         // SELECT the columns with that oid

         String sql = getDMLGenerator().getSelect(table, m_oidKey, dbGeneratedCols);
         PreparedStatement selStmt = getConnection().prepareStatement(sql);

         // Put the oid in

         selStmt.setInt(1, oid);

         // Execute it

         ResultSet rs = selStmt.executeQuery();

         if(!rs.next())
            throw new SQLException("[xmldbms] Couldn't retrieve inserted row.");

         // Set them in the row

         for(int i = 0; i < dbGeneratedCols.length; i++)
         {
            setColumnValue(row, dbGeneratedCols[i], rs.getObject(dbGeneratedCols[i].getName()));
         }
      }
   }
}