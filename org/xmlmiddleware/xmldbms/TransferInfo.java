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

import org.xmlmiddleware.xmldbms.maps.Map;
import java.util.Hashtable;

/**
 * Contains information needed to transfer data between an XML document and
 * the database.
 *
 * <p>A TransferInfo object encapsulates the mapping metadata and DBAction objects
 * needed to transfer data between an XML document and the database(s) according
 * to a particular map. It contains a single Map object and one DBAction object per
 * database and is roughly equivalent to a Map object in XML-DBMS version 1.0.
 * Applications that use more than one map -- that is, that transfer data to/from
 * more than one class of XML documents -- should use one TransferInfo object per map.</p>
 *
 * <p>TransferInfo objects are reusable. In fact, reusing them on multiple calls
 * to DOMToDBMS.processDocument, DBMSDelete.delete, and DBMSToDOM.retrieveDocument
 * will be more efficient than recreating them. This is because they contain Map
 * objects (which don't need to be recompiled), as well as pooled connections and
 * statements (which don't need to be reprepared).</p>
 *
 * <p>It is possible that TransferInfo objects are thread-safe, but I'll have to
 * think about this a bit...</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 * @see DBAction
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

   Map       map;
   Hashtable dbActions = new Hashtable();

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Create a new TransferInfo object.
    *
    * @param map The Map object to which the TransferInfo object applies
    */
   public TransferInfo(Map map)
   {
      if (map == null)
         throw new IllegalArgumentException("Map argument must not be null.");
      this.map = map;
   }

   /**
    * Create a new TransferInfo object and set the DBAction.
    *
    * <p>This is a convenience constructor for applications that use a
    * single database.</p>
    *
    * @param map The Map object to which the TransferInfo object applies
    * @param dbName The name of the database as it appears in the map. If this
    *    is null, the name "Default" is used.
    * @param dbAction An implementation of DBAction for the database.
    */
   public TransferInfo(Map map, String dbName, DBAction dbAction)
   {
      if (map == null)
         throw new IllegalArgumentException("map argument must not be null.");
      if (dbAction == null)
         throw new IllegalArgumentException("dbAction argument must not be null.");
      this.map = map;
      if (dbName == null)
      {
         dbName = DEFAULT;
      }
      dbActions.put(dbName, dbAction);
   }

   //**************************************************************************
   // Public methods
   //**************************************************************************

   /**
    * Get the Map object
    *
    * @return The Map object.
    */
   public final Map getMap()
   {
      return map;
   }

   /**
    * Add a new DBAction.
    *
    * @param dbName The name of the database as it appears in the map. If this
    *    is null, the name "Default" is used.
    * @param dbAction An implementation of DBAction for the database.
    */
   public void addDBAction(String dbName, DBAction dbAction)
   {
      if (dbAction == null)
         throw new IllegalArgumentException("dbAction argument must not be null.");
      if (getDBAction(dbName) != null)
         throw new IllegalArgumentException("DBAction for " + dbName + " already exists.");
      if (dbName == null)
      {
         dbName = DEFAULT;
      }
      dbActions.put(dbName, dbAction);
   }

   /**
    * Get a DBAction.
    *
    * @param dbName The name of the database for which to get the DBAction. May not be null.
    * @return An implementation of DBAction. This is null if no DBAction is found.
    */
   public final DBAction getDBAction(String dbName)
   {
      return (DBAction)dbActions.get(dbName);
   }
}