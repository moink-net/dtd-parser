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

package org.xmlmiddleware.utils;

/**
 * Exception thrown by methods in the Pool interface.
 *
 * @author Sean Walter, 2001
 * @version 2.0
 */

public class PoolException extends WrappedException
{
   // ********************************************************************
   // Constructors
   // ********************************************************************
   
   /**
     * Create a new PoolException.
     *
     * @param message The error or warning message.
     */
   public PoolException(String message) 
   {
      super(message);
   }
   
   /**
     * Create a new PoolException wrapping an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, and its message will become the default message for
     * the PoolException.</p>
     *
     * @param e The exception to be wrapped in a PoolException.
     */
   public PoolException(Exception e)
   {
      super(e);
   }
   
   /**
     * Create a new PoolException from an existing exception.
     *
     * <p>The existing exception will be embedded in the new
     * one, but the new exception will have its own message.</p>
     *
     * @param message The detail message.
     * @param e The exception to be wrapped in a PoolException.
     */
   public PoolException(String message, Exception e)
   {
      super(message, e);
   }
}
