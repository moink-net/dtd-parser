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
//   http://www.informatik.tu-darmstadt.de/DVS1/

// Version 2.0
// Changes from version 1.x: New in version 2.0

package org.xmlmiddleware.xmldbms.datahandlers.external;

import org.xmlmiddleware.xmldbms.*;
import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * <p>DataHandler implementation for JDBC3 drivers.</p>
 *
 * <p><b>WARNING!</b> Incomplete implementation as JDBC3 has not been finalized yet.</p>
 *
 * <p>Database generated keys are retrieved using the method
 * Statement.getGeneratedKeys().</p>
 *
 * @author Sean Walter
 * @version 2.0
 */
public class JDBC3Handler
   extends DataHandlerBase
{
   // ************************************************************************
   // Constructors
   // ************************************************************************

   /**
    * Creates a JDBC3Handler.
    */
   JDBC3Handler()
   {
      super();
   }

   // ************************************************************************
   // Public methods
   // ************************************************************************

   /**
    * Inserts a row into the table.
    *
    * <p>This method sets the columns in the row of any primary or unique keys that were
    * generated by the database. It does this via Statement.getGeneratedKeys().</p>
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

      // IMPL: Statement.executeUpdate has an argument which tells it
      // to return generated keys. PreparedStatement.executeUpdate
      // does not. Submitted as a bug at java.sun.com for JDK V1.4

      int numRows = stmt.executeUpdate();

      databaseModified();

      Vector dbGeneratedCols = getDBGeneratedKeyCols(table);
      if(dbGeneratedCols.size() > 0)
      {
         ResultSet rsGen = stmt.getGeneratedKeys();

         // IMPL: The format of the ResultSet has not been defined
         // just yet. At least I can find no docs on it.

         // IMPL: Read keys from rsGen

         // IMPL: If there are other keys besides those in rsGen,
         // make a SELECT statement and get the rest from there.

         throw new SQLException("[xmldbms] Not implemented yet!");
      }
   }
}
