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

package org.xmlmiddleware.xmldbms.datahandlers;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * <p>DataHandler implementation for databases not directly supported. This
 * includes the JDBC-ODBC bridge. </p>
 *
 * <p>If any keys are generated by the database, they are retrieved by using
 * all other (non-key) values) in the table in the WHERE clause of a SELECT
 * statement. This is necessarily error-prone and is not guaranteed to work.
 * Therefore, this DataHandler should be used with caution when the map specifies
 * database-generated keys.</p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public class GenericHandler
   extends DataHandlerBase
{
   // ************************************************************************
   // Constants
   // ************************************************************************

   private static String FAKE = "fake"; // Fake name for key.

   // ************************************************************************
   // Constructor
   // ************************************************************************

   /**
    * Creates a GenericHandler.
    */
   public GenericHandler()
   {
      super();
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Inserts a row into the table.
    *
    * <p>This method sets the columns in the row of any primary or unique keys
    * that were generated by the database. It does this by selecting rows with
    * all other inserted values.</p>
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
         // Yes this is hokey! I'll say it again. It's H-O-K-E-Y!
         // But it's the best we could come up with.
         // Better ideas totally welcome!

         // Get a list of all the columns we don't need to refresh

         Vector colVec = row.getColumnVectorFor(table);

         for(int i = 0; i < dbGeneratedCols.length; i++)
         {
            colVec.removeElement(dbGeneratedCols[i]);
         }

         Column[] selCols = new Column[colVec.size()];
         colVec.copyInto(selCols);

         // SELECT using those columns as a WHERE clause

         Key key = Key.createUniqueKey(FAKE);
         key.setColumns(selCols);

         String sql = getDMLGenerator().getSelect(table, key, dbGeneratedCols);
         PreparedStatement selStmt = getConnection().prepareStatement(sql);

         // Set the parameters

         Parameters.setParameters(selStmt, 0, selCols, row.getColumnValues(selCols));

         // Execute it

         ResultSet rs = selStmt.executeQuery();

         // Make sure at least 1 row.

         if(!rs.next())
            throw new SQLException("[xmldbms] Couldn't retrieve inserted row due to changed values.");

         // Set them in the row

         for(int i = 0; i < dbGeneratedCols.length; i++)
         {
            setColumnValue(row, dbGeneratedCols[i], rs.getObject(dbGeneratedCols[i].getName()));
         }

         // If more than one row then error.

         if(rs.next())
            throw new SQLException("[xmldbms] Couldn't retrieve inserted row due to multiple rows with identical values.");
      }
   }
}
