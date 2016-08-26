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

import java.sql.*;

/**
 * Interface for methods to add and remove statement event listeners.
 *
 * <p>This interface is roughly parallel to javax.sql.PooledConnection.</p>
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public interface SPStatementEventSource
{
   /**
    * Add an event listener to receive notification of events on this 
    * Statement.
    *
    * @param listener Object to recieive notification.
    */
   public void addStatementEventListener(SPStatementEventListener listener);

   /**
    * Stop notification to an event listener.
    *
    * @param listener Object to stop receiving notification.
    */
   public void removeStatementEventListener(SPStatementEventListener listener);
}



