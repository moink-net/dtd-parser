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

package org.xmlmiddleware.xmldbms.maps;

import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.sql.*;
import java.util.*;
import javax.sql.*;

/**
 * Contains an XMLDBMSMap plus database connection information.
 *
 * <p>A DBEnabledMap object encapsulates mapping metadata and database connection
 * information. It contains a single XMLDBMSMap object and one DataHandler per
 * database. It is roughly equivalent to an XML-DBMS 1.x Map object. Applications
 * should use one DBEnabledMap per XMLDBMSMap and thread.</p>
 *
 * <p>DBEnabledMap objects are reusable. That is, applications should reuse them
 * on subsequent calls to DOMToDBMS.processDocument, DBMSDelete.delete, and
 * DBMSToDOM.retrieveDocument. This avoids having to recompile maps, reconnect to
 * the database, and reprepare statements.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 * @see DataHandler
 */

public class DBEnabledMap
{
   //**************************************************************************
   // Constants
   //**************************************************************************

   private static String DEFAULT = "Default";

   //**************************************************************************
   // Class variables
   //**************************************************************************

   private XMLDBMSMap map;
   private Hashtable  dataHandlers = new Hashtable();

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Create a new DBEnabledMap object.
    *
    * <p>This constructor should be used by applications that use multiple
    * databases. After constructing the DBEnabledMap object, they call
    * addDataSource or addDataHandler for each database.</p>
    *
    * @param map The XMLDBMSMap object to which the DBEnabledMap object applies
    */
   public DBEnabledMap(XMLDBMSMap map)
   {
      if (map == null)
         throw new IllegalArgumentException("map argument must not be null.");
      this.map = map;
   }

   /**
    * Create a new DBEnabledMap object from an XMLDBMSMap and a DataSource.
    *
    * <p>This is the constructor most applications use. It uses a single database
    * and the GenericHandler implementation of DataHandler.</p>
    *
    * @param map The XMLDBMSMap object to which the DBEnabledMap object applies
    * @param dbName The name of the database as it appears in the map. If this
    *    is null, the name "Default" is used.
    * @param ds The DataSource to use.
    * @param user The user name to use. May be null.
    * @param password The password to use. May be null.
    *
    * @exception SQLException Thrown if an error occurs initializing the DataHandler.
    */
   public DBEnabledMap(XMLDBMSMap map, String dbName, DataSource ds, String user, String password)
      throws SQLException
   {
      DataHandler dataHandler;

      if (map == null)
         throw new IllegalArgumentException("map argument must not be null.");
      if (ds == null)
         throw new IllegalArgumentException("ds argument must not be null.");

      this.map = map;
      dataHandler = new GenericHandler();
      dataHandler.initialize(ds, user, password);
      if (dbName == null)
      {
         dbName = DEFAULT;
      }
      dataHandlers.put(dbName, dataHandler);
   }

   /**
    * Create a new DBEnabledMap object from an XMLDBMSMap and a DataHandler.
    *
    * <p>This constructor should be used by applications that use a single
    * database, but want control over the implementation of DataHandler that
    * they use.</p>
    *
    * @param map The XMLDBMSMap object to which the DBEnabledMap object applies
    * @param dbName The name of the database as it appears in the map. If this
    *    is null, the name "Default" is used.
    * @param dataHandler An implementation of DataHandler for the database.
    */
   public DBEnabledMap(XMLDBMSMap map, String dbName, DataHandler dataHandler)
   {
      if (map == null)
         throw new IllegalArgumentException("map argument must not be null.");
      if (dataHandler == null)
         throw new IllegalArgumentException("dataHandler argument must not be null.");
      this.map = map;
      if (dbName == null)
      {
         dbName = DEFAULT;
      }
      dataHandlers.put(dbName, dataHandler);
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Get the XMLDBMSMap object
    *
    * @return The XMLDBMSMap object.
    */
   public final XMLDBMSMap getMap()
   {
      return map;
   }

   /**
    * Add a new DataSource.
    *
    * <p>Applications that use multiple databases call this method once
    * for each database. Each database will use the GenericHandler
    * implementation of DataHandler.</p>
    *
    * @param dbName The name of the database as it appears in the map. If this
    *    is null, the name "Default" is used.
    * @param ds The DataSource to use.
    * @param user The user name to use. May be null.
    * @param password The password to use. May be null.
    *
    * @exception SQLException Thrown if an error occurs initializing the DataHandler.
    */
   public void addDataSource(String dbName, DataSource ds, String user, String password)
      throws SQLException
   {
      DataHandler dataHandler;

      if (ds == null)
         throw new IllegalArgumentException("ds argument must not be null.");
      if (getDataHandler(dbName) != null)
         throw new IllegalArgumentException("Data source for " + dbName + " already exists.");

      dataHandler = new GenericHandler();
      dataHandler.initialize(ds, user, password);
      if (dbName == null)
      {
         dbName = DEFAULT;
      }
      dataHandlers.put(dbName, dataHandler);
   }

   /**
    * Add a new DataHandler.
    *
    * <p>Applications that use multiple databases and want control over the
    * DataHandlers they use call this method once for each database.</p>
    *
    * @param dbName The name of the database as it appears in the map. If this
    *    is null, the name "Default" is used.
    * @param dataHandler An implementation of DataHandler for the database.
    */
   public void addDataHandler(String dbName, DataHandler dataHandler)
   {
      if (dataHandler == null)
         throw new IllegalArgumentException("dataHandler argument must not be null.");
      if (getDataHandler(dbName) != null)
         throw new IllegalArgumentException("DataHandler for " + dbName + " already exists.");
      if (dbName == null)
      {
         dbName = DEFAULT;
      }
      dataHandlers.put(dbName, dataHandler);
   }

   /**
    * Get a DataHandler.
    *
    * @param dbName The name of the database for which to get the DataHandler. May not be null.
    * @return An implementation of DataHandler. This is null if no DataHandler is found.
    */
   public final DataHandler getDataHandler(String dbName)
   {
      return (DataHandler)dataHandlers.get(dbName);
   }

   /**
    * Get all DataHandlers.
    *
    * @return An Enumeration containing DataHandler implementations. May be empty.
    */
   public final Enumeration getDataHandlers()
   {
      return dataHandlers.elements();
   }
}
