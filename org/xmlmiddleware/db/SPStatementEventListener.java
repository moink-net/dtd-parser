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
import java.util.*;

/**
 * Listener for statement events
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public interface SPStatementEventListener 
   extends EventListener
{
   /**
    * Called when statement is closed.
    *
    * @param event Event object containing Statement as source.
    */
   public void statementClosed(SPStatementEvent event);

   /**
    * Called when error occurs on statement.
    *
    * @param event Event object containing Statement as source.
    */
   public void statementErrorOccurred(SPStatementEvent event);
}