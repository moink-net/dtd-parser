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

package org.xmlmiddleware.xmldbms;

import org.xmlmiddleware.xmldbms.datahandlers.*;
import org.xmlmiddleware.xmldbms.maps.*;

import java.util.*;

/**
 * Contains information needed to transfer data between an XML document and
 * the database.
 *
 * <p>A TransferInfo object encapsulates the mapping metadata and DataHandler objects
 * needed to transfer data between an XML document and the database(s) according
 * to a particular map. It contains a single XMLDBMSMap object and one DataHandler object per
 * database and is roughly equivalent to a XMLDBMSMap object in XML-DBMS version 1.0.
 * Applications that use more than one map -- that is, that transfer data to/from
 * more than one class of XML documents -- should use one TransferInfo object per map.</p>
 *
 * <p>TransferInfo objects are reusable. In fact, reusing them on multiple calls
 * to DOMToDBMS.processDocument, DBMSDelete.delete, and DBMSToDOM.retrieveDocument
 * will be more efficient than recreating them. This is because they contain XMLDBMSMap
 * objects (which don't need to be recompiled), as well as pooled connections and
 * statements (which don't need to be reprepared).</p>
 *
 * <p>It is possible that TransferInfo objects are thread-safe, but I'll have to
 * think about this a bit...</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 * @see DataHandler
 */

public class TransferInfo
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
    * Create a new TransferInfo object.
    *
    * @param map The XMLDBMSMap object to which the TransferInfo object applies
    */
   public TransferInfo(XMLDBMSMap map)
   {
      if (map == null)
         throw new IllegalArgumentException("map argument must not be null.");
      this.map = map;
   }

   /**
    * Create a new TransferInfo object and set the DataHandler.
    *
    * <p>This is a convenience constructor for applications that use a
    * single database.</p>
    *
    * @param map The XMLDBMSMap object to which the TransferInfo object applies
    * @param dbName The name of the database as it appears in the map. If this
    *    is null, the name "Default" is used.
    * @param dataHandler An implementation of DataHandler for the database.
    */
   public TransferInfo(XMLDBMSMap map, String dbName, DataHandler dataHandler)
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
    * Add a new DataHandler.
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
