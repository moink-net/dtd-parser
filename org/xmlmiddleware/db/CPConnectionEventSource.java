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

package org.xmlmiddleware.db;

import java.lang.*;
import java.sql.*;

/**
 * Interface for methods to add and remove connection event listeners.
 *
 * <p>This interface is roughly parallel to javax.sql.PooledConnection.
 * However, we do not use PooledConnection (nor do we use ConnectionEvent or
 * ConnectionEventListener) because of our connection pool architecture. In
 * particular, PooledConnection is designed to be on a different object than
 * Connection. This is because both have close() methods (which therefore collide),
 * and PooledConnection has methods to get and close the underlying connection,
 * neither of which should be exposed to the application. We implement both
 * CPConnectionEventSource and Connection on the CPConnection object.</p>
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public interface CPConnectionEventSource
{
   /**
    * Add an event listener to receive notification of events on this 
    * connection.
    *
    * @param listener Object to receive notification.
    */
   public void addConnectionEventListener(CPConnectionEventListener listener);

   /**
    * Stop notification to an event listener.
    *
    * @param listener Object to stop receiving notification.
    */
   public void removeConnectionEventListener(CPConnectionEventListener listener);
}



